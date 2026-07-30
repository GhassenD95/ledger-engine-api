package tn.finix.ledgerengine.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.finix.ledgerengine.entity.AccountEntry;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface AccountEntryRepository extends JpaRepository<AccountEntry, UUID> {

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM AccountEntry e WHERE e.account.id = :accountId")
    BigDecimal getBalanceForAccount(@Param("accountId") UUID accountId);

    Page<AccountEntry> findByAccountIdOrderByCreatedAtDesc(UUID accountId, Pageable pageable);

    List<AccountEntry> findByJournalEntryId(UUID journalEntryId);
}
