package com.ontop.wms.service;

import com.ontop.wms.entity.Warehouse;
import com.ontop.wms.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;

    public List<Warehouse> getAllWarehouses() {
        return warehouseRepository.findAll();
    }

    public Warehouse getById(Integer id) {
        return warehouseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Warehouse not found: " + id));
    }

    public Warehouse create(Warehouse warehouse) {
        return warehouseRepository.save(warehouse);
    }

    public Warehouse update(Integer id, Warehouse data) {
        Warehouse warehouse = getById(id);
        warehouse.setName(data.getName());
        warehouse.setCode(data.getCode());
        warehouse.setAddress(data.getAddress());
        return warehouseRepository.save(warehouse);
    }

    public void delete(Integer id) {
        warehouseRepository.deleteById(id);
    }
}
