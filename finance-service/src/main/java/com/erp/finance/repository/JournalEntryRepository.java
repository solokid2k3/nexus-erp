package com.erp.finance.repository;

import com.erp.finance.entity.JournalEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.UUID;

@Repository
public interface JournalEntryRepository extends JpaRepository<JournalEntry, UUID> {

    Page<JournalEntry> findByStatus(JournalEntry.JournalStatus status, Pageable pageable);

    Page<JournalEntry> findByEntryDateBetween(Instant start, Instant end, Pageable pageable);

    @Query("SELECT COALESCE(MAX(CAST(SUBSTRING(j.entryNumber, 4, 10) AS integer)), 0) + 1 FROM JournalEntry j")
    Integer getNextEntrySequence();

    Page<JournalEntry> findBySourceType(String sourceType, Pageable pageable);
}
