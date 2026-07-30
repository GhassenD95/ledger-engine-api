package tn.finix.ledgerengine.service.helper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tn.finix.ledgerengine.entity.Account;
import tn.finix.ledgerengine.exception.AccountNotFoundException;
import tn.finix.ledgerengine.repository.AccountRepository;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AccountLockHelper {

    private final AccountRepository accountRepository;

    public record LockedAccountPair(Account source, Account destination) {}

    public LockedAccountPair lockAndFetchAccounts(UUID sourceId, UUID destId) {
        List<UUID> accountIds = List.of(sourceId, destId);
        List<Account> lockedAccounts = accountRepository.findAllByIdsWithLockSorted(accountIds);

        if (lockedAccounts.size() < 2) {
            throw new AccountNotFoundException("One or both accounts were not found");
        }

        Account sourceAccount = lockedAccounts.stream()
                .filter(a -> a.getId().equals(sourceId))
                .findFirst()
                .orElseThrow(() -> new AccountNotFoundException("Source account not found"));

        Account destAccount = lockedAccounts.stream()
                .filter(a -> a.getId().equals(destId))
                .findFirst()
                .orElseThrow(() -> new AccountNotFoundException("Destination account not found"));

        return new LockedAccountPair(sourceAccount, destAccount);
    }
}
