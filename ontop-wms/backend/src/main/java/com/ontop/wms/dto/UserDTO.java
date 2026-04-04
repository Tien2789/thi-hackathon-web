package com.ontop.wms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Integer id;
    private String username;
    private RoleDTO role;
    private WarehouseDTO warehouse;
    private Boolean isActive; // Added for frontend status rendering

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoleDTO {
        private String roleName;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WarehouseDTO {
        private Integer id;
        private String name;
    }
}