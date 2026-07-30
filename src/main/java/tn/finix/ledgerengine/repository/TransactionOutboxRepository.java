package tn.finix.ledgerengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tn.finix.ledgerengine.entity.OutboxStatus;
import tn.finix.ledgerengine.entity.TransactionOutbox;

import java.util.List;
import java.util.UUID;

public interface TransactionOutboxRepository extends JpaRepository<TransactionOutbox, UUID> {

    List<TransactionOutbox> findByStatus(OutboxStatus status);

    @Query(value = """
        SELECT * FROM transaction_outbox 
        WHERE status = 'PENDING' 
        ORDER BY created_at ASC 
        LIMIT 50 
        FOR UPDATE SKIP LOCKED
        """, nativeQuery = true)
    List<TransactionOutbox> fetchPendingEvents();
}
