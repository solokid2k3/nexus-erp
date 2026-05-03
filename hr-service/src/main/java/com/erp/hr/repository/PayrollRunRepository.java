package com.erp.hr.repository;

import com.erp.hr.entity.PayrollRun;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface PayrollRunRepository extends JpaRepository<PayrollRun, UUID> {
    Page<PayrollRun> findByStatus(PayrollRun.PayrollStatus status, Pageable pageable);
    Page<PayrollRun> findByPayFrequency(String payFrequency, Pageable pageable);

    @Query("SELECT COALESCE(MAX(CAST(SUBSTRING(p.runNumber, 4, 10) AS integer)), 0) + 1 FROM PayrollRun p")
    Integer getNextRunSequence();
}
