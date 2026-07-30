package tn.finix.ledgerengine.service.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tn.finix.ledgerengine.entity.JournalEntry;
import tn.finix.ledgerengine.entity.OutboxStatus;
import tn.finix.ledgerengine.entity.TransactionOutbox;
import tn.finix.ledgerengine.repository.TransactionOutboxRepository;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OutboxPublisherHelper {

    private final TransactionOutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public void recordTransferOutboxEvent(JournalEntry journalEntry, UUID sourceId, UUID destId, BigDecimal amount, String currency, String referenceId) {
        try {
            String payloadJson = objectMapper.writeValueAsString(Map.of(
                    "journalEntryId", journalEntry.getId(),
                    "referenceId", referenceId,
                    "sourceAccountId", sourceId,
                    "destinationAccountId", destId,
                    "amount", amount,
                    "currency", currency
            ));

            outboxRepository.save(TransactionOutbox.builder()
                    .eventType("TRANSFER_COMPLETED")
                    .payload(payloadJson)
                    .status(OutboxStatus.PENDING)
                    .build());

        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize outbox event payload", e);
        }
    }
}
