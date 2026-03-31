package com.ontop.wms.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ontop.wms.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Integer> {
    Optional<Product> findBySkuCode(String skuCode);
    List<Product> findByBinLocation_Warehouse_Id(Integer warehouseId);
}