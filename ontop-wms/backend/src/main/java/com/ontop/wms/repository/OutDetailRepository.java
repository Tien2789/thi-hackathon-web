package com.ontop.wms.repository;

import com.ontop.wms.entity.InventoryOut;
import com.ontop.wms.entity.OutDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OutDetailRepository extends JpaRepository<OutDetail, Integer> {
    Optional<OutDetail> findByInventoryOut(InventoryOut inventoryOut);
}
