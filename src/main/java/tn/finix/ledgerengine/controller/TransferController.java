package tn.finix.ledgerengine.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.finix.ledgerengine.dto.TransferRequest;
import tn.finix.ledgerengine.dto.TransferResponse;
import tn.finix.ledgerengine.service.TransferService;

@RestController
@RequestMapping("/api/v1/transfers")
@RequiredArgsConstructor
@Tag(name = "Transfers", description = "Execute and manage fund transfers")
public class TransferController {

    private final TransferService transferService;

    @PostMapping
    @Operation(summary = "Execute a fund transfer", description = "Creates a double-entry journal entry with debit/credit entries. Idempotent via referenceId.")
    @ApiResponse(responseCode = "200", description = "Transfer executed or duplicate ignored")
    @ApiResponse(responseCode = "400", description = "Validation error, insufficient balance, or cross-currency transfer")
    public ResponseEntity<TransferResponse> executeTransfer(@Valid @RequestBody TransferRequest request) {
        TransferResponse response = transferService.executeTransfer(request);
        return ResponseEntity.ok(response);
    }
}
