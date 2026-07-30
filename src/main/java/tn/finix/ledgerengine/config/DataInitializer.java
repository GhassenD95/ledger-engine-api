package tn.finix.ledgerengine.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tn.finix.ledgerengine.entity.Account;
import tn.finix.ledgerengine.entity.AccountEntry;
import tn.finix.ledgerengine.entity.JournalEntry;
import tn.finix.ledgerengine.repository.AccountEntryRepository;
import tn.finix.ledgerengine.repository.AccountRepository;
import tn.finix.ledgerengine.repository.JournalEntryRepository;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final AccountRepository accountRepository;
    private final AccountEntryRepository accountEntryRepository;
    private final JournalEntryRepository journalEntryRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (accountRepository.count() == 0) {
            log.info("No accounts found in DB. Seeding initial test data for Alice and Bob...");

            Account alice = accountRepository.save(Account.builder()
                    .ownerName("Alice")
                    .currency("USD")
                    .build());

            Account bob = accountRepository.save(Account.builder()
                    .ownerName("Bob")
                    .currency("USD")
                    .build());

            JournalEntry initialFundingHeader = journalEntryRepository.save(JournalEntry.builder()
                    .referenceId("INIT-FUNDING-001")
                    .description("Initial Account Seed Data")
                    .build());

            accountEntryRepository.save(AccountEntry.builder()
                    .journalEntry(initialFundingHeader)
                    .account(alice)
                    .amount(new BigDecimal("1000.0000"))
                    .build());

            accountEntryRepository.save(AccountEntry.builder()
                    .journalEntry(initialFundingHeader)
                    .account(bob)
                    .amount(new BigDecimal("500.0000"))
                    .build());

            log.info("Successfully seeded accounts!");
            log.info("--> Alice Account ID: {}", alice.getId());
            log.info("--> Bob Account ID:   {}", bob.getId());
        }
    }
}
