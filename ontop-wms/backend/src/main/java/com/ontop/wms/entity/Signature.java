package com.ontop.wms.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "signatures")
@Getter
@Setter
public class Signature {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "transaction_type", nullable = false)
    private String transactionType; // INBOUND, OUTBOUND

    @Column(name = "transaction_id", nullable = false)
    private Integer transactionId;

    @Column(name = "signer_role", nullable = false)
    private String signerRole; // KEEPER, ACCOUNTANT, DIRECTOR, PERSON

    @Column(name = "signer_email")
    private String signerEmail;

    @Column(name = "signer_name")
    private String signerName;

    @Column(nullable = false)
    private String status = "PENDING"; // PENDING, SIGNED, REJECTED

    @Column(name = "signed_at")
    private LocalDateTime signedAt;

    @Column(name = "signature_token", unique = true)
    private String signatureToken;

    @Column(name = "ip_address")
    private String ipAddress;
}
