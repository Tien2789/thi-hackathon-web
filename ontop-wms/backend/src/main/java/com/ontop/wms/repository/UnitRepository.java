package com.ontop.wms.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.ontop.wms.entity.Unit;

@Repository
public interface UnitRepository extends JpaRepository<Unit, Integer> {
    Optional<Unit> findByName(String name);
}
