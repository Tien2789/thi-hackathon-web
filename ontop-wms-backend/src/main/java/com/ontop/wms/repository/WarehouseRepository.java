package com.ontop.wms.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ontop.wms.entity.Warehouse;

public interface WarehouseRepository extends JpaRepository<Warehouse, Integer> {
}