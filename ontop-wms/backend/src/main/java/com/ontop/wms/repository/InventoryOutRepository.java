package com.ontop.wms.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ontop.wms.entity.InventoryOut;

public interface InventoryOutRepository extends JpaRepository<InventoryOut, Integer> {
}