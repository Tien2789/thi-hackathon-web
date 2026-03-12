package com.ontop.wms.dto;

import lombok.Data;

@Data
public class CreateUserRequest {
    private String username;
    private String password;
    private String roleName;
    private Long warehouseId;
}