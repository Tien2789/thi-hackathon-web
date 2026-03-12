package com.ontop.wms.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ontop.wms.entity.InDetail;

public interface InDetailRepository extends JpaRepository<InDetail, Integer> {
}