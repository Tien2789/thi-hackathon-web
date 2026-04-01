package com.ontop.wms.dto;

public record AuthResponse(String token, String username, String role, Integer warehouseId, String warehouseName) {
}
