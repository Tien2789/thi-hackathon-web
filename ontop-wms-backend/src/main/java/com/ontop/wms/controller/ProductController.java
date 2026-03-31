package com.ontop.wms.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ontop.wms.service.ProductService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/products") // Khớp với api.get('/products')
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<?> getAllProducts() {
        // Trả về danh sách sản phẩm DTO với các trường cần thiết cho frontend
        return ResponseEntity.ok(productService.getAllProducts());
    }
}