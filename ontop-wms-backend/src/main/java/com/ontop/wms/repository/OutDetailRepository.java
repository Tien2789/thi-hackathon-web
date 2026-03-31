package com.ontop.wms.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ontop.wms.entity.InventoryOut;
import com.ontop.wms.entity.OutDetail;

public interface OutDetailRepository extends JpaRepository<OutDetail, Integer> {
    Optional<OutDetail> findByInventoryOut(InventoryOut inventoryOut);
}