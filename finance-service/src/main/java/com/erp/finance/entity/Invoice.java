package com.erp.finance.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "invoices", schema = "finance")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "invoice_number", unique = true, nullable = false)
    private String invoiceNumber;

    @Column(name = "invoice_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private InvoiceType invoiceType;

    @Column(name = "customer_id")
    private UUID customerId;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "order_id")
    private UUID orderId;

    @Enumerated(EnumType.STRING)
    private InvoiceStatus status = InvoiceStatus.DRAFT;

    @Column(name = "subtotal_cents")
    private Long subtotalCents = 0L;

    @Column(name = "tax_amount_cents")
    private Long taxAmountCents = 0L;

    @Column(name = "discount_amount_cents")
    private Long discountAmountCents = 0L;

    @Column(name = "total_amount_cents")
    private Long totalAmountCents = 0L;

    @Column(name = "amount_paid_cents")
    private Long amountPaidCents = 0L;

    @Column(name = "amount_due_cents")
    private Long amountDueCents = 0L;

    private String currency = "USD";

    @Column(name = "payment_terms")
    private String paymentTerms;

    @Column(name = "invoice_date", nullable = false)
    private Instant invoiceDate;

    @Column(name = "due_date", nullable = false)
    private Instant dueDate;

    @Column(name = "paid_date")
    private Instant paidDate;

    private String notes;

    @Column(name = "journal_entry_id")
    private UUID journalEntryId;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<InvoiceLine> lines = new ArrayList<>();

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    public enum InvoiceType {
        SALES, PURCHASE, CREDIT_NOTE, DEBIT_NOTE
    }

    public enum InvoiceStatus {
        DRAFT, SENT, PARTIALLY_PAID, PAID, OVERDUE, CANCELLED, VOID
    }
}
