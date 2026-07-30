package tn.finix.ledgerengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.finix.ledgerengine.entity.JournalEntry;

import java.util.Optional;
import java.util.UUID;
@Repository
public interface JournalEntryRepository extends JpaRepository<JournalEntry, UUID> {



    //Idempotency check, to find duplicate transactions by reference id
    Optional<JournalEntry> findByReferenceId(String referenceId);


}
