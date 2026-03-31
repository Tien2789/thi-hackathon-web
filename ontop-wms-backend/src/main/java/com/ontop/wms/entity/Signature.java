package com.ontop.wms.entity;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

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
@Table(name = "signatures")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Signature {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "inventory_in_id")
    private InventoryIn inventoryIn;

    @ManyToOne
    @JoinColumn(name = "inventory_out_id")
    private InventoryOut inventoryOut;

    @Column(nullable = false)
    private String role; // STOREKEEPER, ACCOUNTANT, DIRECTOR, PERSON_IN_CHARGE

    @Column
    private String signerName;

    @Column
    private String signerEmail;

    @Column
    private String signatureToken;

    @Column
    private boolean signed = false;

    @Column
    private LocalDateTime signedAt;

    @CreationTimestamp
    private Timestamp createdAt;
}
