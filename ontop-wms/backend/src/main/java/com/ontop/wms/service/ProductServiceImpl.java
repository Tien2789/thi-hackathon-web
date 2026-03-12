package com.ontop.wms.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.ontop.wms.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public List<com.ontop.wms.entity.Product> getAllProducts() {
        return productRepository.findAll();
    }
}