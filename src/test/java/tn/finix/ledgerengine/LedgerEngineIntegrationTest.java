package tn.finix.ledgerengine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import tn.finix.ledgerengine.dto.TransferRequest;
import tn.finix.ledgerengine.entity.Account;
import tn.finix.ledgerengine.entity.OutboxStatus;
import tn.finix.ledgerengine.exception.InsufficientBalanceException;
import tn.finix.ledgerengine.repository.AccountEntryRepository;
import tn.finix.ledgerengine.repository.AccountRepository;
import tn.finix.ledgerengine.repository.JournalEntryRepository;
import tn.finix.ledgerengine.repository.TransactionOutboxRepository;
import tn.finix.ledgerengine.service.TransferService;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
@Disabled("Requires Docker with API version >= 1.44 (Docker 25+). Run manually.")
class LedgerEngineIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("ledger_db")
            .withUsername("ledger_app")
            .withPassword("1995");

    @Container
    static RabbitMQContainer rabbitmq = new RabbitMQContainer("rabbitmq:3.13-management-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.rabbitmq.host", rabbitmq::getHost);
        registry.add("spring.rabbitmq.port", rabbitmq::getAmqpPort);
        registry.add("spring.rabbitmq.username", () -> "guest");
        registry.add("spring.rabbitmq.password", () -> "guest");
        registry.add("spring.data.redis.host", () -> "localhost");
        registry.add("spring.data.redis.port", () -> "16379");
    }

    @Autowired
    private TransferService transferService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccountEntryRepository accountEntryRepository;

    @Autowired
    private JournalEntryRepository journalEntryRepository;

    @Autowired
    private TransactionOutboxRepository outboxRepository;

    private Account alice;
    private Account bob;

    @BeforeEach
    void setUp() {
        outboxRepository.deleteAll();
        accountEntryRepository.deleteAll();
        journalEntryRepository.deleteAll();
        accountRepository.deleteAll();

        alice = accountRepository.save(Account.builder()
                .ownerName("Alice")
                .currency("USD")
                .build());
        bob = accountRepository.save(Account.builder()
                .ownerName("Bob")
                .currency("USD")
                .build());
    }

    @Test
    void shouldExecuteTransferAndUpdateBalances() {
        var refId = UUID.randomUUID().toString();
        var request = TransferRequest.builder()
                .referenceId(refId)
                .sourceAccountId(alice.getId())
                .destinationAccountId(bob.getId())
                .amount(new BigDecimal("100.00"))
                .description("integration test")
                .build();

        var response = transferService.executeTransfer(request);

        assertEquals("SUCCESS", response.getStatus());

        var aliceBalance = accountEntryRepository.getBalanceForAccount(alice.getId());
        var bobBalance = accountEntryRepository.getBalanceForAccount(bob.getId());
        assertEquals(0, new BigDecimal("-100.00").compareTo(aliceBalance));
        assertEquals(0, new BigDecimal("100.00").compareTo(bobBalance));
    }

    @Test
    void shouldRejectDuplicateReferenceId() {
        var refId = UUID.randomUUID().toString();
        var request = TransferRequest.builder()
                .referenceId(refId)
                .sourceAccountId(alice.getId())
                .destinationAccountId(bob.getId())
                .amount(new BigDecimal("50.00"))
                .description("first attempt")
                .build();

        transferService.executeTransfer(request);

        var duplicate = TransferRequest.builder()
                .referenceId(refId)
                .sourceAccountId(alice.getId())
                .destinationAccountId(bob.getId())
                .amount(new BigDecimal("50.00"))
                .description("duplicate attempt")
                .build();

        var response = transferService.executeTransfer(duplicate);

        assertEquals("SUCCESS_DUPLICATE_IGNORED", response.getStatus());

        var aliceBalance = accountEntryRepository.getBalanceForAccount(alice.getId());
        assertEquals(0, new BigDecimal("-50.00").compareTo(aliceBalance));
    }

    @Test
    void shouldCreateOutboxEventOnTransfer() {
        var refId = UUID.randomUUID().toString();
        var request = TransferRequest.builder()
                .referenceId(refId)
                .sourceAccountId(alice.getId())
                .destinationAccountId(bob.getId())
                .amount(new BigDecimal("75.00"))
                .description("outbox test")
                .build();

        transferService.executeTransfer(request);

        var pending = outboxRepository.findByStatus(OutboxStatus.PENDING);
        assertEquals(1, pending.size());
        assertEquals("TRANSFER_COMPLETED", pending.get(0).getEventType());
    }

    @Test
    void shouldThrowOnInsufficientBalance() {
        var refId = UUID.randomUUID().toString();
        var request = TransferRequest.builder()
                .referenceId(refId)
                .sourceAccountId(alice.getId())
                .destinationAccountId(bob.getId())
                .amount(new BigDecimal("999999.00"))
                .description("overdraft test")
                .build();

        assertThrows(InsufficientBalanceException.class, () -> transferService.executeTransfer(request));
    }

    @Test
    void shouldRejectCrossCurrencyTransfer() {
        var eurAccount = accountRepository.save(Account.builder()
                .ownerName("EUR Account")
                .currency("EUR")
                .build());

        var refId = UUID.randomUUID().toString();
        var request = TransferRequest.builder()
                .referenceId(refId)
                .sourceAccountId(alice.getId())
                .destinationAccountId(eurAccount.getId())
                .amount(new BigDecimal("100.00"))
                .description("cross-currency test")
                .build();

        assertThrows(IllegalArgumentException.class, () -> transferService.executeTransfer(request));
    }
}
