package com.ontop.wms.repository;

import com.ontop.wms.entity.BinLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BinLocationRepository extends JpaRepository<BinLocation, Integer> {
    List<BinLocation> findByWarehouseId(Integer warehouseId);
}
