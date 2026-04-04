package com.ontop.wms.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

@Data
public class ApproveRequest {
    private Integer productId;
    private Integer quantity; // Main quantity
    
    // Inbound specifics
    private BigDecimal unitPrice;
    private LocalDate manufacturingDate;
    private LocalDate expiryDate;
    
    // Outbound specifics
    private Integer requestedQuantity;

    // Optional New Product info
    private String productName;
    private String skuCode;
    private Integer categoryId;
    private Integer unitId;
    private String barcode;
    
    // On-the-fly Category/Unit creation
    private String categoryName;
    private String unitName;
}