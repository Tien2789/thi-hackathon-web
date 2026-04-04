package com.ontop.wms.controller;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ontop.wms.entity.Unit;
import com.ontop.wms.repository.UnitRepository;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/units")
@RequiredArgsConstructor
public class UnitController {
    private final UnitRepository unitRepository;

    @GetMapping
    public List<Unit> getAllUnits() {
        return unitRepository.findAll();
    }
}
