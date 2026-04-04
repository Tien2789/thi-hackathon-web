package com.ontop.wms.service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ontop.wms.entity.Signature;
import com.ontop.wms.repository.SignatureRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SignatureService {

    private final SignatureRepository signatureRepository;

    @Transactional
    public void initiateSignatures(String transactionType, Integer transactionId, List<String> emails) {
        // Standard roles for Circular 200/2014/TT-BTC
        List<String> roles = Arrays.asList("KEEPER", "ACCOUNTANT", "DIRECTOR", "PERSON");
        
        for (int i = 0; i < roles.size(); i++) {
            Signature signature = new Signature();
            signature.setTransactionType(transactionType);
            signature.setTransactionId(transactionId);
            signature.setSignerRole(roles.get(i));
            if (emails != null && i < emails.size()) {
                signature.setSignerEmail(emails.get(i));
            }
            signature.setSignatureToken(UUID.randomUUID().toString());
            signature.setStatus("PENDING");
            signatureRepository.save(signature);
            // In a real app, send email here with link: /signature/verify?token=...
        }
    }

    public List<Signature> getSignaturesForTransaction(String type, Integer id) {
        return signatureRepository.findByTransactionTypeAndTransactionId(type, id);
    }

    public Optional<Signature> getSignatureByToken(String token) {
        return signatureRepository.findBySignatureToken(token);
    }

    @Transactional
    public Signature verifyAndSign(String token, String signerName, String ipAddress) {
        Signature signature = signatureRepository.findBySignatureToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid signature token"));

        if (!"PENDING".equals(signature.getStatus())) {
            throw new IllegalStateException("This document has already been signed or rejected.");
        }

        signature.setSignerName(signerName);
        signature.setIpAddress(ipAddress);
        signature.setSignedAt(LocalDateTime.now());
        signature.setStatus("SIGNED");
        
        return signatureRepository.save(signature);
    }

    public boolean isFullySigned(String type, Integer id) {
        List<Signature> signatures = getSignaturesForTransaction(type, id);
        return !signatures.isEmpty() && signatures.stream().allMatch(s -> "SIGNED".equals(s.getStatus()));
    }
}
