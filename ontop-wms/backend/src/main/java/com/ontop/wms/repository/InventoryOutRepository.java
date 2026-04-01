package com.ontop.wms.repository;

import com.ontop.wms.entity.InventoryOut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryOutRepository extends JpaRepository<InventoryOut, Integer> {
    Optional<InventoryOut> findByIssueCode(String issueCode);
}
