package com.ontop.wms.repository;

import com.ontop.wms.entity.InventoryIn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryInRepository extends JpaRepository<InventoryIn, Integer> {
    Optional<InventoryIn> findByReceiptCode(String receiptCode);
}
