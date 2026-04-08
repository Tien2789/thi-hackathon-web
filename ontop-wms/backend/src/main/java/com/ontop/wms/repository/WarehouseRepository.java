package com.ontop.wms.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ontop.wms.entity.Warehouse;

public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {
    Optional<Warehouse> findByCode(String code);
}