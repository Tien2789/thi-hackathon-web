package com.ontop.wms.repository;

import java.util.List;

import com.ontop.wms.entity.InventoryIn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ontop.wms.entity.InDetail;
import com.ontop.wms.entity.Product;

@Repository
public interface InDetailRepository extends JpaRepository<InDetail, Integer> {
    @Query("SELECT d FROM InDetail d WHERE d.product = :product AND d.remainingQuantity > 0 ORDER BY d.inventoryIn.createdAt ASC")
    List<InDetail> findAvailableStockForFIFO(Product product);

    @Query("SELECT d FROM InDetail d WHERE d.inventoryIn = :inventoryIn ORDER BY d.inventoryIn.createdAt ASC")
    List<InDetail> findByInventoryInOrderByInventoryIn_CreatedAtAsc(InventoryIn inventoryIn);

    @Query("SELECT d FROM InDetail d WHERE d.inventoryIn = :inventoryIn")
    List<InDetail> findByInventoryIn(InventoryIn inventoryIn);
}
