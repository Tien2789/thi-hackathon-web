package com.ontop.wms.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "out_details")
@Getter
@Setter
public class OutDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    private InventoryOut inventoryOut;

    @ManyToOne
    private Product product;

    @Column(name = "requested_quantity")
    private Integer requestedQuantity = 0;

    @Column(name = "actual_quantity")
    private Integer actualQuantity = 0;

    @Column(name = "unit_price")
    private BigDecimal unitPrice;

    @Column
    private Integer quantity = 0; // Keeping this for backward compatibility if needed, or remove later
}