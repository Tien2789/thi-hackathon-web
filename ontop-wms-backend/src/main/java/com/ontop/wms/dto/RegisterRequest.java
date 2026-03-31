package com.ontop.wms.dto;

public record RegisterRequest(String username, String password, String roleName, Integer warehouseId) {}
