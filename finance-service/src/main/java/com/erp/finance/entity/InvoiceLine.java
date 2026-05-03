package com.erp.finance.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "invoice_lines", schema = "finance")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InvoiceLine {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @Column(name = "line_number")
    private Integer lineNumber;

    private String description;

    @Column(name = "product_id")
    private UUID productId;

    private Integer quantity = 1;

    @Column(name = "unit_price_cents", nullable = false)
    private Long unitPriceCents;

    @Column(name = "tax_rate")
    private BigDecimal taxRate = BigDecimal.ZERO;

    @Column(name = "tax_amount_cents")
    private Long taxAmountCents = 0L;

    @Column(name = "line_total_cents", nullable = false)
    private Long lineTotalCents;

    private String currency = "USD";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;
}
