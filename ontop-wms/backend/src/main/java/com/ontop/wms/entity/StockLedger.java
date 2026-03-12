package com.ontop.wms.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_ledger")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockLedger {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "warehouse_id")
    private Warehouse warehouse;

    @Column(name = "transaction_type", nullable = false)
    private String transactionType;

    @Column(name = "quantity_change", nullable = false)
    private Integer quantityChange;

    @Column(name = "running_balance", nullable = false)
    private Integer runningBalance;

    @Column(name = "reference_id", nullable = false)
    private Integer referenceId;

    @Column(name = "reference_table", nullable = false)
    private String referenceTable;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
