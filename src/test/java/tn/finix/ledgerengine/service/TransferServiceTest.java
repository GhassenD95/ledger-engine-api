package tn.finix.ledgerengine.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.finix.ledgerengine.dto.TransferRequest;
import tn.finix.ledgerengine.dto.TransferResponse;
import tn.finix.ledgerengine.entity.Account;
import tn.finix.ledgerengine.entity.AccountEntry;
import tn.finix.ledgerengine.entity.JournalEntry;
import tn.finix.ledgerengine.exception.InsufficientBalanceException;
import tn.finix.ledgerengine.repository.AccountEntryRepository;
import tn.finix.ledgerengine.repository.JournalEntryRepository;
import tn.finix.ledgerengine.service.helper.AccountLockHelper;
import tn.finix.ledgerengine.service.helper.IdempotencyGuard;
import tn.finix.ledgerengine.service.helper.OutboxPublisherHelper;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private JournalEntryRepository journalEntryRepository;

    @Mock
    private AccountEntryRepository accountEntryRepository;

    @Mock
    private AccountLockHelper lockHelper;

    @Mock
    private OutboxPublisherHelper outboxPublisherHelper;

    @Mock
    private IdempotencyGuard idempotencyGuard;

    @InjectMocks
    private TransferService transferService;

    private final UUID sourceId = UUID.randomUUID();
    private final UUID destId = UUID.randomUUID();
    private final String referenceId = UUID.randomUUID().toString();

    private TransferRequest validRequest() {
        return TransferRequest.builder()
                .referenceId(referenceId)
                .sourceAccountId(sourceId)
                .destinationAccountId(destId)
                .amount(new BigDecimal("100.00"))
                .description("test transfer")
                .build();
    }

    private Account account(UUID id, String currency) {
        return Account.builder().id(id).ownerName("test").currency(currency).build();
    }

    @Test
    void shouldExecuteTransferSuccessfully() {
        var request = validRequest();
        var source = account(sourceId, "USD");
        var dest = account(destId, "USD");

        when(journalEntryRepository.findByReferenceId(referenceId)).thenReturn(Optional.empty());
        when(lockHelper.lockAndFetchAccounts(sourceId, destId)).thenReturn(
                new AccountLockHelper.LockedAccountPair(source, dest));
        when(accountEntryRepository.getBalanceForAccount(sourceId)).thenReturn(new BigDecimal("500.00"));
        when(journalEntryRepository.save(any())).thenAnswer(inv -> {
            JournalEntry je = inv.getArgument(0);
            je.setId(UUID.randomUUID());
            return je;
        });

        TransferResponse response = transferService.executeTransfer(request);

        assertEquals("SUCCESS", response.getStatus());
        assertEquals(referenceId, response.getReferenceId());
        verify(accountEntryRepository, times(2)).save(any(AccountEntry.class));
        verify(outboxPublisherHelper).recordTransferOutboxEvent(any(), eq(sourceId), eq(destId),
                eq(new BigDecimal("100.00")), eq("USD"), eq(referenceId));
    }

    @Test
    void shouldReturnDuplicateIgnoredForExistingReferenceId() {
        var request = validRequest();
        var existingJournal = JournalEntry.builder()
                .id(UUID.randomUUID())
                .referenceId(referenceId)
                .build();

        when(journalEntryRepository.findByReferenceId(referenceId)).thenReturn(Optional.of(existingJournal));

        TransferResponse response = transferService.executeTransfer(request);

        assertEquals("SUCCESS_DUPLICATE_IGNORED", response.getStatus());
        assertEquals(existingJournal.getId(), response.getJournalEntryId());
        verify(lockHelper, never()).lockAndFetchAccounts(any(), any());
    }

    @Test
    void shouldUseRedisGuardFastPath() {
        var request = validRequest();
        var existingJournal = JournalEntry.builder()
                .id(UUID.randomUUID())
                .referenceId(referenceId)
                .build();

        when(idempotencyGuard.isAlreadyProcessed(referenceId)).thenReturn(true);
        when(journalEntryRepository.findByReferenceId(referenceId)).thenReturn(Optional.of(existingJournal));

        TransferResponse response = transferService.executeTransfer(request);

        assertEquals("SUCCESS_DUPLICATE_IGNORED", response.getStatus());
        verify(idempotencyGuard).isAlreadyProcessed(referenceId);
    }

    @Test
    void shouldMarkProcessedInRedisOnSuccess() {
        var request = validRequest();
        var source = account(sourceId, "USD");
        var dest = account(destId, "USD");

        when(journalEntryRepository.findByReferenceId(referenceId)).thenReturn(Optional.empty());
        when(lockHelper.lockAndFetchAccounts(sourceId, destId)).thenReturn(
                new AccountLockHelper.LockedAccountPair(source, dest));
        when(accountEntryRepository.getBalanceForAccount(sourceId)).thenReturn(new BigDecimal("500.00"));
        when(journalEntryRepository.save(any())).thenAnswer(inv -> {
            JournalEntry je = inv.getArgument(0);
            je.setId(UUID.randomUUID());
            return je;
        });

        transferService.executeTransfer(request);

        verify(idempotencyGuard).markProcessed(referenceId);
    }

    @Test
    void shouldMarkProcessedInRedisOnDuplicate() {
        var request = validRequest();
        var existingJournal = JournalEntry.builder()
                .id(UUID.randomUUID())
                .referenceId(referenceId)
                .build();

        when(journalEntryRepository.findByReferenceId(referenceId)).thenReturn(Optional.of(existingJournal));

        transferService.executeTransfer(request);

        verify(idempotencyGuard).markProcessed(referenceId);
    }

    @Test
    void shouldThrowOnCrossCurrencyTransfer() {
        var request = validRequest();
        var source = account(sourceId, "USD");
        var dest = account(destId, "EUR");

        when(journalEntryRepository.findByReferenceId(referenceId)).thenReturn(Optional.empty());
        when(lockHelper.lockAndFetchAccounts(sourceId, destId)).thenReturn(
                new AccountLockHelper.LockedAccountPair(source, dest));

        assertThrows(IllegalArgumentException.class, () -> transferService.executeTransfer(request));
    }

    @Test
    void shouldThrowOnInsufficientBalance() {
        var request = validRequest();
        var source = account(sourceId, "USD");
        var dest = account(destId, "USD");

        when(journalEntryRepository.findByReferenceId(referenceId)).thenReturn(Optional.empty());
        when(lockHelper.lockAndFetchAccounts(sourceId, destId)).thenReturn(
                new AccountLockHelper.LockedAccountPair(source, dest));
        when(accountEntryRepository.getBalanceForAccount(sourceId)).thenReturn(new BigDecimal("50.00"));

        assertThrows(InsufficientBalanceException.class, () -> transferService.executeTransfer(request));
    }
}
