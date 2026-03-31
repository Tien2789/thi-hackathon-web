package com.ontop.wms.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ontop.wms.dto.ApproveRequest;
import com.ontop.wms.dto.InventoryRequest;
import com.ontop.wms.service.InventoryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    // --- PHẦN NHẬP KHO (INBOUND) ---

    @GetMapping("/inbounds")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<?> getInbounds() {
        return ResponseEntity.ok(inventoryService.getAllInbounds());
    }

    @PostMapping("/inbounds")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<?> createInbound(@RequestBody InventoryRequest request) {
        return ResponseEntity.ok(inventoryService.createInbound(request));
    }

    @PostMapping("/inbounds/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<?> approveInbound(@PathVariable Integer id, @RequestBody ApproveRequest request) {
        return ResponseEntity.ok(inventoryService.approveInbound(id, request));
    }

    // --- PHẦN XUẤT KHO (OUTBOUND) ---

    @GetMapping("/outbounds")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<?> getOutbounds() {
        return ResponseEntity.ok(inventoryService.getAllOutbounds());
    }

    @PostMapping("/outbounds")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<?> createOutbound(@RequestBody InventoryRequest request) {
        return ResponseEntity.ok(inventoryService.createOutbound(request));
    }

    @PostMapping("/outbounds/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<?> approveOutbound(@PathVariable Integer id, @RequestBody ApproveRequest request) {
        return ResponseEntity.ok(inventoryService.approveOutbound(id, request));
    }

    @PostMapping("/outbounds/{id}/undo")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<?> undoOutbound(@PathVariable Integer id) {
        return ResponseEntity.ok(inventoryService.undoOutbound(id));
    }
}