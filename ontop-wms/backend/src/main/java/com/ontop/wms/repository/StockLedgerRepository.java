package com.ontop.wms.repository;

import com.ontop.wms.entity.StockLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StockLedgerRepository extends JpaRepository<StockLedger, Long> {
    Optional<StockLedger> findByReferenceIdAndReferenceTable(Integer referenceId, String referenceTable);
}
