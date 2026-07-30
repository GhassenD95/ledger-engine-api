package tn.finix.ledgerengine.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import tn.finix.ledgerengine.entity.AccountEntry;
import tn.finix.ledgerengine.entity.JournalEntry;
import tn.finix.ledgerengine.repository.AccountEntryRepository;
import tn.finix.ledgerengine.repository.JournalEntryRepository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(JournalController.class)
class JournalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JournalEntryRepository journalEntryRepository;

    @MockBean
    private AccountEntryRepository accountEntryRepository;

    @Test
    void shouldReturnJournalEntryWithEntries() throws Exception {
        var refId = "TX-001";
        var journalId = UUID.randomUUID();
        var journal = JournalEntry.builder()
                .id(journalId)
                .referenceId(refId)
                .description("test")
                .createdAt(OffsetDateTime.now())
                .build();

        var entry1 = AccountEntry.builder()
                .id(UUID.randomUUID())
                .journalEntry(journal)
                .amount(new BigDecimal("-100.00"))
                .createdAt(OffsetDateTime.now())
                .build();

        var entry2 = AccountEntry.builder()
                .id(UUID.randomUUID())
                .journalEntry(journal)
                .amount(new BigDecimal("100.00"))
                .createdAt(OffsetDateTime.now())
                .build();

        when(journalEntryRepository.findByReferenceId(refId)).thenReturn(Optional.of(journal));
        when(accountEntryRepository.findByJournalEntryId(journalId)).thenReturn(List.of(entry1, entry2));

        mockMvc.perform(get("/api/v1/transactions/{referenceId}", refId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.referenceId").value(refId))
                .andExpect(jsonPath("$.entries.length()").value(2));
    }

    @Test
    void shouldReturn404WhenTransactionNotFound() throws Exception {
        when(journalEntryRepository.findByReferenceId("UNKNOWN")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/transactions/{referenceId}", "UNKNOWN"))
                .andExpect(status().isNotFound());
    }
}
