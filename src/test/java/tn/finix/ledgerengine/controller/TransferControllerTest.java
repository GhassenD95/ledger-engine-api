package tn.finix.ledgerengine.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import tn.finix.ledgerengine.dto.TransferRequest;
import tn.finix.ledgerengine.dto.TransferResponse;
import tn.finix.ledgerengine.service.TransferService;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransferController.class)
class TransferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransferService transferService;

    @Test
    void shouldReturn200OnValidTransfer() throws Exception {
        var request = TransferRequest.builder()
                .referenceId(UUID.randomUUID().toString())
                .sourceAccountId(UUID.randomUUID())
                .destinationAccountId(UUID.randomUUID())
                .amount(new BigDecimal("100.00"))
                .description("payment")
                .build();

        var response = TransferResponse.builder()
                .journalEntryId(UUID.randomUUID())
                .referenceId(request.getReferenceId())
                .amount(request.getAmount())
                .status("SUCCESS")
                .timestamp(OffsetDateTime.now())
                .build();

        when(transferService.executeTransfer(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.referenceId").value(request.getReferenceId()));
    }

    @Test
    void shouldReturn400OnMissingReferenceId() throws Exception {
        var body = """
                {
                    "sourceAccountId": "%s",
                    "destinationAccountId": "%s",
                    "amount": 100.00,
                    "description": "test"
                }
                """.formatted(UUID.randomUUID(), UUID.randomUUID());

        mockMvc.perform(post("/api/v1/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400OnNegativeAmount() throws Exception {
        var body = """
                {
                    "referenceId": "%s",
                    "sourceAccountId": "%s",
                    "destinationAccountId": "%s",
                    "amount": -50,
                    "description": "test"
                }
                """.formatted(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());

        mockMvc.perform(post("/api/v1/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }
}
