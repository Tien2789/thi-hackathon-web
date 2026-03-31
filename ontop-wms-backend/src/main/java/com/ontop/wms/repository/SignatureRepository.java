package com.ontop.wms.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ontop.wms.entity.Signature;

@Repository
public interface SignatureRepository extends JpaRepository<Signature, Integer> {
    Optional<Signature> findBySignatureToken(String signatureToken);
}
