package com.erp.finance.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "journal_entries", schema = "finance")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class JournalEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "entry_number", unique = true, nullable = false)
    private String entryNumber;

    @Column(name = "entry_date", nullable = false)
    private Instant entryDate;

    private String description;

    @Enumerated(EnumType.STRING)
    private JournalStatus status = JournalStatus.DRAFT;

    @Column(name = "source_type")
    private String sourceType;

    @Column(name = "source_id")
    private String sourceId;

    @Column(name = "fiscal_period")
    private String fiscalPeriod;

    @Column(name = "total_debit_cents")
    private Long totalDebitCents = 0L;

    @Column(name = "total_credit_cents")
    private Long totalCreditCents = 0L;

    private String currency = "USD";

    @OneToMany(mappedBy = "journalEntry", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<JournalLine> lines = new ArrayList<>();

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    public void addLine(JournalLine line) {
        lines.add(line);
        line.setJournalEntry(this);
    }

    public enum JournalStatus {
        DRAFT, POSTED, REVERSED
    }
}
