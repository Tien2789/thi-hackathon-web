package com.ontop.wms.entity;

import java.sql.Timestamp;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "inventory_out")
@Getter
@Setter
public class InventoryOut {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false)
    private String issueCode;

    @Column(name = "receiver_name")
    private String receiverName;

    @Column(name = "reason")
    private String reason;

    @Column(name = "document_number")
    private String documentNumber;

    @Column
    private String status = "PENDING";

    @ManyToOne
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @ManyToOne
    private Warehouse warehouse;

    @CreationTimestamp
    private Timestamp createdAt;

    @OneToMany(mappedBy = "inventoryOut")
    private Set<OutDetail> details;
}