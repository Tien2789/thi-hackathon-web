package com.ontop.wms.service;

import com.ontop.wms.entity.Product;
import com.ontop.wms.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QrScanService {
    private final ProductRepository productRepository;

    public Product findProductByQr(String qrContent) {
        // Simple logic: Assuming QR content is just the SKU or contains the SKU
        return productRepository.findBySkuCode(qrContent)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with QR/SKU: " + qrContent));
    }
}
