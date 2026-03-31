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
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "in_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "inbound_id")
    private InventoryIn inventoryIn;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @Column
    private Integer quantity = 0;

    @Column
    private Integer remainingQuantity = 0;

    @Column
    private BigDecimal unitPrice;

    @Column
    private BigDecimal totalPrice;

    @Column
    private java.time.LocalDate receivedDate;

    @Column
    private java.time.LocalDate expiryDate;
}