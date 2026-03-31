package com.ontop.wms.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ontop.wms.entity.InventoryIn;

public interface InventoryInRepository extends JpaRepository<InventoryIn, Integer> {
}