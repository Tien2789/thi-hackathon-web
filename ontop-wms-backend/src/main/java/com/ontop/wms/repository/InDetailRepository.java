package com.ontop.wms.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ontop.wms.entity.InDetail;

import java.util.List;
import com.ontop.wms.entity.Product;

public interface InDetailRepository extends JpaRepository<InDetail, Integer> {
    List<InDetail> findByProductAndRemainingQuantityGreaterThanOrderByReceivedDateAsc(Product product, Integer remainingQuantity);
}