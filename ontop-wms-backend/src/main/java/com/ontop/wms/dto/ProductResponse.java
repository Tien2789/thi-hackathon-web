package com.ontop.wms.dto;

public record ProductResponse(
    Integer id,
    String skuCode,
    String productName,
    String categoryName,
    Integer currentStock,
    Integer minStock
) {}
