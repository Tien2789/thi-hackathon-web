package com.ontop.wms.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ontop.wms.entity.InventoryOut;
import com.ontop.wms.entity.OutDetail;

@Repository
public interface OutDetailRepository extends JpaRepository<OutDetail, Integer> {
    List<OutDetail> findAllByInventoryOut(InventoryOut inventoryOut);
}
