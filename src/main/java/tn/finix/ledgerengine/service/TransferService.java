package tn.finix.ledgerengine.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import java.time.OffsetDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransferService {

    private final JournalEntryRepository journalEntryRepository;
    private final AccountEntryRepository accountEntryRepository;
    private final AccountLockHelper lockHelper;
    private final OutboxPublisherHelper outboxPublisherHelper;
    private final IdempotencyGuard idempotencyGuard;

    @Transactional
    public TransferResponse executeTransfer(TransferRequest request) {
        log.info("Processing transfer ref: {} [{} -> {}] Amount: {}",
                request.getReferenceId(), request.getSourceAccountId(),
                request.getDestinationAccountId(), request.getAmount());

        // 1. Idempotency Check (Redis fast path, then DB fallback)
        if (idempotencyGuard.isAlreadyProcessed(request.getReferenceId())) {
            log.info("Idempotency guard hit for referenceId: {}. Checking DB for cached response.", request.getReferenceId());
        }

        Optional<JournalEntry> existingJournal = journalEntryRepository.findByReferenceId(request.getReferenceId());
        if (existingJournal.isPresent()) {
            idempotencyGuard.markProcessed(request.getReferenceId());
            log.warn("Duplicate request for referenceId: {}. Returning cached response.", request.getReferenceId());
            return TransferResponse.builder()
                    .journalEntryId(existingJournal.get().getId())
                    .referenceId(request.getReferenceId())
                    .sourceAccountId(request.getSourceAccountId())
                    .destinationAccountId(request.getDestinationAccountId())
                    .amount(request.getAmount())
                    .status("SUCCESS_DUPLICATE_IGNORED")
                    .timestamp(existingJournal.get().getCreatedAt())
                    .build();
        }

        // 2. Lock & Fetch Accounts
        var lockedPair = lockHelper.lockAndFetchAccounts(request.getSourceAccountId(), request.getDestinationAccountId());
        Account sourceAccount = lockedPair.source();
        Account destAccount = lockedPair.destination();

        if (!sourceAccount.getCurrency().equalsIgnoreCase(destAccount.getCurrency())) {
            throw new IllegalArgumentException("Cross-currency transfers are not supported");
        }

        // 3. Balance Check
        BigDecimal currentBalance = accountEntryRepository.getBalanceForAccount(sourceAccount.getId());
        if (currentBalance.compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException(
                    String.format("Insufficient balance. Current: %s, Requested: %s", currentBalance, request.getAmount())
            );
        }

        // 4. Record Double-Entry Journal
        JournalEntry journalEntry = journalEntryRepository.save(JournalEntry.builder()
                .referenceId(request.getReferenceId())
                .description(request.getDescription())
                .build());

        accountEntryRepository.save(AccountEntry.builder()
                .journalEntry(journalEntry)
                .account(sourceAccount)
                .amount(request.getAmount().negate())
                .build());

        accountEntryRepository.save(AccountEntry.builder()
                .journalEntry(journalEntry)
                .account(destAccount)
                .amount(request.getAmount())
                .build());

        // 5. Publish Outbox Event
        outboxPublisherHelper.recordTransferOutboxEvent(
                journalEntry, sourceAccount.getId(), destAccount.getId(),
                request.getAmount(), sourceAccount.getCurrency(), request.getReferenceId()
        );

        idempotencyGuard.markProcessed(request.getReferenceId());

        log.info("Transfer ref: {} completed successfully.", request.getReferenceId());

        return TransferResponse.builder()
                .journalEntryId(journalEntry.getId())
                .referenceId(request.getReferenceId())
                .sourceAccountId(sourceAccount.getId())
                .destinationAccountId(destAccount.getId())
                .amount(request.getAmount())
                .status("SUCCESS")
                .timestamp(OffsetDateTime.now())
                .build();
    }
}
