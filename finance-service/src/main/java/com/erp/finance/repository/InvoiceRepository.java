package com.erp.finance.repository;

import com.erp.finance.entity.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    Page<Invoice> findByStatus(Invoice.InvoiceStatus status, Pageable pageable);

    Page<Invoice> findByCustomerIdAndStatus(UUID customerId, Invoice.InvoiceStatus status, Pageable pageable);

    Page<Invoice> findByInvoiceType(Invoice.InvoiceType type, Pageable pageable);

    @Query("SELECT i FROM Invoice i WHERE i.status NOT IN ('PAID','CANCELLED','VOID') AND i.dueDate < :now")
    List<Invoice> findOverdueInvoices(Instant now);

    @Query("SELECT i FROM Invoice i WHERE i.customerId = :customerId AND i.status NOT IN ('PAID','CANCELLED','VOID')")
    List<Invoice> findOutstandingByCustomer(UUID customerId);

    @Query("SELECT COALESCE(MAX(CAST(SUBSTRING(i.invoiceNumber, 5, 10) AS integer)), 0) + 1 FROM Invoice i")
    Integer getNextInvoiceSequence();
}
