package com.ontop.wms.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ontop.wms.entity.Signature;
import com.ontop.wms.service.InventoryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/signatures")
@RequiredArgsConstructor
public class SignatureController {

    private final InventoryService inventoryService;

    @GetMapping("/{token}")
    public Signature getSignature(@PathVariable String token) {
        return inventoryService.getSignatureByToken(token);
    }

    @PostMapping("/{token}/confirm")
    public Signature confirmSignature(@PathVariable String token) {
        return inventoryService.confirmSignature(token);
    }
}
