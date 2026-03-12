package com.ontop.wms.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ontop.wms.entity.Product;
import com.ontop.wms.entity.Warehouse;
import com.ontop.wms.repository.WarehouseRepository;
import jakarta.persistence.EntityNotFoundException;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/warehouses")
@RequiredArgsConstructor
public class WarehouseController {
    private final WarehouseRepository warehouseRepository;

    private final com.ontop.wms.service.AuthService authService;

    @GetMapping
    public ResponseEntity<List<Warehouse>> getAllWarehouses(org.springframework.security.core.Authentication authentication) {
        com.ontop.wms.entity.User currentUser = authService.getCurrentUser(authentication.getName());
        com.ontop.wms.entity.Role userRole = currentUser.getRole();
        
        if (userRole == null) {
             return ResponseEntity.ok(List.of());
        }
        String role = userRole.getRoleName();

        if ("ADMIN".equals(role)) {
            return ResponseEntity.ok(warehouseRepository.findAll());
        } else {
            if (currentUser.getWarehouse() == null) {
                return ResponseEntity.ok(List.of());
            }
            return ResponseEntity.ok(List.of(currentUser.getWarehouse()));
        }
    }

    @PostMapping
    public ResponseEntity<Warehouse> createWarehouse(@RequestBody Warehouse warehouse) {
        return ResponseEntity.ok(warehouseRepository.save(warehouse));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Warehouse> updateWarehouse(@PathVariable Integer id, @RequestBody Warehouse warehouseDetails) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Warehouse not found with id: " + id));

        warehouse.setName(warehouseDetails.getName());
        warehouse.setAddress(warehouseDetails.getAddress());
        return ResponseEntity.ok(warehouseRepository.save(warehouse));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWarehouse(@PathVariable Integer id) {
        warehouseRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/bin-stock")
    public ResponseEntity<List<Product>> getBinStock(@PathVariable Integer id) {
        // Return placeholder or implement real logic
        return ResponseEntity.ok(List.of());
    }
}
