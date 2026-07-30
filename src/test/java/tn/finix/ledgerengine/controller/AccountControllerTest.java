package tn.finix.ledgerengine.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import tn.finix.ledgerengine.entity.Account;
import tn.finix.ledgerengine.exception.AccountNotFoundException;
import tn.finix.ledgerengine.repository.AccountEntryRepository;
import tn.finix.ledgerengine.repository.AccountRepository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountRepository accountRepository;

    @MockBean
    private AccountEntryRepository accountEntryRepository;

    @Test
    void shouldReturnAccountWithBalance() throws Exception {
        var id = UUID.randomUUID();
        var account = Account.builder()
                .id(id)
                .ownerName("Alice")
                .currency("USD")
                .createdAt(OffsetDateTime.now())
                .build();

        when(accountRepository.findById(id)).thenReturn(Optional.of(account));
        when(accountEntryRepository.getBalanceForAccount(id)).thenReturn(new BigDecimal("1000.00"));

        mockMvc.perform(get("/api/v1/accounts/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.ownerName").value("Alice"))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.balance").value(1000.00));
    }

    @Test
    void shouldReturn404WhenAccountNotFound() throws Exception {
        var id = UUID.randomUUID();
        when(accountRepository.findById(id)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/accounts/{id}", id))
                .andExpect(status().isNotFound());
    }
}
