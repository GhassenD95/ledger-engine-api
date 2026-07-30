package tn.finix.ledgerengine.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.finix.ledgerengine.dto.AccountEntryResponse;
import tn.finix.ledgerengine.dto.AccountResponse;
import tn.finix.ledgerengine.entity.Account;
import tn.finix.ledgerengine.exception.AccountNotFoundException;
import tn.finix.ledgerengine.repository.AccountEntryRepository;
import tn.finix.ledgerengine.repository.AccountRepository;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@Tag(name = "Accounts", description = "View account details and entry history")
public class AccountController {

    private final AccountRepository accountRepository;
    private final AccountEntryRepository accountEntryRepository;

    @GetMapping
    @Operation(summary = "List all accounts with real-time balances")
    @ApiResponse(responseCode = "200", description = "List of all accounts")
    public ResponseEntity<List<AccountResponse>> listAccounts() {
        List<Account> accounts = accountRepository.findAll();
        List<AccountResponse> response = accounts.stream()
                .map(account -> AccountResponse.builder()
                        .id(account.getId())
                        .ownerName(account.getOwnerName())
                        .currency(account.getCurrency())
                        .balance(accountEntryRepository.getBalanceForAccount(account.getId()))
                        .createdAt(account.getCreatedAt())
                        .build())
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get account details with calculated balance")
    @ApiResponse(responseCode = "200", description = "Account found with real-time balance")
    @ApiResponse(responseCode = "404", description = "Account not found")
    public ResponseEntity<AccountResponse> getAccount(@PathVariable UUID id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + id));

        var balance = accountEntryRepository.getBalanceForAccount(id);

        return ResponseEntity.ok(AccountResponse.builder()
                .id(account.getId())
                .ownerName(account.getOwnerName())
                .currency(account.getCurrency())
                .balance(balance)
                .createdAt(account.getCreatedAt())
                .build());
    }

    @GetMapping("/{id}/entries")
    @Operation(summary = "Get paginated account entry history")
    @ApiResponse(responseCode = "200", description = "Paginated list of debit/credit entries")
    @ApiResponse(responseCode = "404", description = "Account not found")
    public ResponseEntity<Page<AccountEntryResponse>> getAccountEntries(@PathVariable UUID id, Pageable pageable) {
        if (!accountRepository.existsById(id)) {
            throw new AccountNotFoundException("Account not found: " + id);
        }

        Page<AccountEntryResponse> response = accountEntryRepository
                .findByAccountIdOrderByCreatedAtDesc(id, pageable)
                .map(entry -> AccountEntryResponse.builder()
                        .id(entry.getId())
                        .journalEntryId(entry.getJournalEntry().getId())
                        .amount(entry.getAmount())
                        .createdAt(entry.getCreatedAt())
                        .build());

        return ResponseEntity.ok(response);
    }
}
