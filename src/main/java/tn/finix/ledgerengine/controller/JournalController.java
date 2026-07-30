package tn.finix.ledgerengine.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.finix.ledgerengine.dto.AccountEntryResponse;
import tn.finix.ledgerengine.dto.JournalEntryResponse;
import tn.finix.ledgerengine.entity.JournalEntry;
import tn.finix.ledgerengine.repository.AccountEntryRepository;
import tn.finix.ledgerengine.repository.JournalEntryRepository;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Retrieve journal entry details")
public class JournalController {

    private final JournalEntryRepository journalEntryRepository;
    private final AccountEntryRepository accountEntryRepository;

    @GetMapping("/{referenceId}")
    @Operation(summary = "Get journal entry by reference ID with debit/credit lines")
    @ApiResponse(responseCode = "200", description = "Journal entry found")
    @ApiResponse(responseCode = "404", description = "Transaction not found")
    public ResponseEntity<JournalEntryResponse> getTransaction(@PathVariable String referenceId) {
        JournalEntry journal = journalEntryRepository.findByReferenceId(referenceId)
                .orElse(null);

        if (journal == null) {
            return ResponseEntity.notFound().build();
        }

        var entries = accountEntryRepository.findByJournalEntryId(journal.getId())
                .stream()
                .map(e -> AccountEntryResponse.builder()
                        .id(e.getId())
                        .journalEntryId(e.getJournalEntry().getId())
                        .amount(e.getAmount())
                        .createdAt(e.getCreatedAt())
                        .build())
                .toList();

        return ResponseEntity.ok(JournalEntryResponse.builder()
                .id(journal.getId())
                .referenceId(journal.getReferenceId())
                .description(journal.getDescription())
                .createdAt(journal.getCreatedAt())
                .entries(entries)
                .build());
    }
}
