package tn.finix.ledgerengine.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountEntryResponse {
    private UUID id;
    private UUID journalEntryId;
    private BigDecimal amount;
    private OffsetDateTime createdAt;
}
