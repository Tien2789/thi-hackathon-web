package com.ontop.wms.controller;

import java.util.ArrayList;
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
import com.ontop.wms.entity.Role;
import com.ontop.wms.entity.Warehouse;
import com.ontop.wms.repository.WarehouseRepository;
import com.ontop.wms.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/warehouses")
@RequiredArgsConstructor
public class WarehouseController {
    private final WarehouseRepository warehouseRepository;
    private final UserRepository userRepository;

    @GetMapping
    @SuppressWarnings("null")
    public ResponseEntity<List<Warehouse>> getAllWarehouses(org.springframework.security.core.Authentication authentication) {
        com.ontop.wms.entity.User currentUser = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
                
        boolean isAdmin = currentUser.getRoles().stream()
                            .map(Role::getName)
                            .anyMatch(name -> "ADMIN".equals(name));

        if (isAdmin) {
            return ResponseEntity.ok(warehouseRepository.findAll());
        } else {
            return ResponseEntity.ok(new ArrayList<>(currentUser.getWarehouses()));
        }
    }

    @PostMapping
    public ResponseEntity<Warehouse> createWarehouse(@RequestBody Warehouse warehouse) {
        if (warehouse == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(warehouseRepository.save(warehouse));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Warehouse> updateWarehouse(@PathVariable Integer id, @RequestBody Warehouse warehouseDetails) {
        if (id == null) {
            return ResponseEntity.badRequest().build();
        }
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Warehouse not found with id: " + id));

        if (warehouseDetails != null) {
            warehouse.setName(warehouseDetails.getName());
            warehouse.setAddress(warehouseDetails.getAddress());
        }
        return ResponseEntity.ok(warehouseRepository.save(warehouse));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWarehouse(@PathVariable Integer id) {
        if (id != null) {
            warehouseRepository.deleteById(id);
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/bin-stock")
    public ResponseEntity<List<Product>> getBinStock(@PathVariable Integer id) {
        // Return placeholder or implement real logic
        return ResponseEntity.ok(List.of());
    }
}
