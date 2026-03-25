package com.ontop.wms.service;

import java.util.List;

import com.ontop.wms.entity.Product;

public interface ProductService {
    List<Product> getAllProducts();
    Product createProduct(Product product);
    void deleteProduct(Integer id);
}