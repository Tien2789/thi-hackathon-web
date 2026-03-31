package com.ontop.wms.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ontop.wms.entity.Product;
import com.ontop.wms.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    @Transactional
    public Product createProduct(Product product) {
        // Tự động tạo SKU Code từ tên sản phẩm
        if (product.getProductName() != null && !product.getProductName().isEmpty()) {
            product.setSkuCode(generateSku(product.getProductName()));
        }
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        if (product.getCurrentStock() == null) product.setCurrentStock(0);
        if (product.getMinStock() == null) product.setMinStock(0);
        return productRepository.save(product);
    }

    @Override
    @Transactional
    public void deleteProduct(Integer id) {
        productRepository.deleteById(id);
    }

    private String generateSku(String name) {
        // Chuyển thành chữ hoa, bỏ dấu tiếng Việt, thay khoảng trắng bằng gạch ngang, xóa ký tự đặc biệt
        return name.trim().toUpperCase()
                .replaceAll("[àáạảãâầấậẩẫăằắặẳẵ]", "A")
                .replaceAll("[èéẹẻẽêềếệểễ]", "E")
                .replaceAll("[ìíịỉĩ]", "I")
                .replaceAll("[òóọỏõôồốộổỗơờớợởỡ]", "O")
                .replaceAll("[ùúụủũưừứựửữ]", "U")
                .replaceAll("[ỳýỵỷỹ]", "Y")
                .replaceAll("Đ", "D")
                .replaceAll("[^A-Z0-9\\s]", "")
                .replaceAll("\\s+", "-");
    }
}