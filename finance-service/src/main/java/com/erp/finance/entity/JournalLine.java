package com.erp.finance.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "journal_lines", schema = "finance")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class JournalLine {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journal_entry_id", nullable = false)
    private JournalEntry journalEntry;

    @Column(name = "line_number")
    private Integer lineNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "debit_amount_cents")
    private Long debitAmountCents = 0L;

    @Column(name = "credit_amount_cents")
    private Long creditAmountCents = 0L;

    private String currency = "USD";

    private String description;

    @Column(name = "department_id")
    private String departmentId;

    @Column(name = "cost_center")
    private String costCenter;
}
