package tn.finix.ledgerengine.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JournalEntryResponse {
    private UUID id;
    private String referenceId;
    private String description;
    private OffsetDateTime createdAt;
    private List<AccountEntryResponse> entries;
}
