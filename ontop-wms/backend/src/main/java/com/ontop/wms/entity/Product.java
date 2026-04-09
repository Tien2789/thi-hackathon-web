package com.ontop.wms.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "sku_code", unique = true, nullable = false)
    private String skuCode;

    @Column(unique = true, nullable = false)
    private String barcode;

    @Column(name = "qr_code")
    private String qrCode;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @ManyToOne
    private Category category;

    @ManyToOne
    private Unit unit;

    @ManyToOne
    private Supplier supplier;

    @ManyToOne
    private BinLocation binLocation;

    @Column(name = "current_stock")
    private Integer currentStock = 0;

    @Column(name = "min_stock")
    private Integer minStock = 0;

    @Column(name = "storage_limit_months")
    private Integer storageLimitMonths = 6;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}