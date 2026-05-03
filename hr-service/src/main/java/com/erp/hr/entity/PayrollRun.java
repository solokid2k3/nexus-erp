package com.erp.hr.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "payroll_runs", schema = "hr")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PayrollRun {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "run_number", unique = true, nullable = false)
    private String runNumber;

    @Column(nullable = false)
    private String period;

    @Column(name = "pay_frequency", nullable = false)
    private String payFrequency;

    @Enumerated(EnumType.STRING)
    private PayrollStatus status = PayrollStatus.DRAFT;

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    @Column(name = "total_gross_cents")
    private Long totalGrossCents = 0L;

    @Column(name = "total_deductions_cents")
    private Long totalDeductionsCents = 0L;

    @Column(name = "total_net_cents")
    private Long totalNetCents = 0L;

    @Column(name = "total_employer_cost_cents")
    private Long totalEmployerCostCents = 0L;

    private String currency = "USD";

    @Column(name = "employee_count")
    private Integer employeeCount = 0;

    @Column(name = "created_by")
    private String createdBy;
    @Column(name = "created_at")
    private Instant createdAt;

    @PrePersist
    protected void onCreate() { createdAt = Instant.now(); }

    public enum PayrollStatus {
        DRAFT, CALCULATED, APPROVED, PROCESSING, COMPLETED, CANCELLED
    }
}
