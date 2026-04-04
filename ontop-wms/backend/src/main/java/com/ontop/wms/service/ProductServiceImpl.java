package com.ontop.wms.service;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ontop.wms.entity.Product;
import com.ontop.wms.repository.CategoryRepository;
import com.ontop.wms.repository.ProductRepository;
import com.ontop.wms.repository.UnitRepository;
import com.ontop.wms.repository.SupplierRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UnitRepository unitRepository;
    private final SupplierRepository supplierRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    @Transactional
    public Product saveProduct(Product product) {
        if (product == null) return null;

        // Resolve nested entities before saving with null safety
        if (product.getCategory() != null) {
            Integer cid = product.getCategory().getId();
            if (cid != null) {
                categoryRepository.findById(cid).ifPresent(product::setCategory);
            }
        }
        
        if (product.getUnit() != null) {
            Integer uid = product.getUnit().getId();
            if (uid != null) {
                unitRepository.findById(uid).ifPresent(product::setUnit);
            }
        }
        
        if (product.getSupplier() != null) {
            Integer sid = product.getSupplier().getId();
            if (sid != null) {
                supplierRepository.findById(sid).ifPresent(product::setSupplier);
            }
        }
        
        return productRepository.save(product);
    }
}