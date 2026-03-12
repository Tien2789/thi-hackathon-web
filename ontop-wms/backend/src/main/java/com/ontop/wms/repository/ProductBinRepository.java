package com.ontop.wms.repository;

import com.ontop.wms.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductBinRepository extends JpaRepository<Product, Integer> {
    List<Product> findByBinLocationWarehouseId(Integer warehouseId);
}
