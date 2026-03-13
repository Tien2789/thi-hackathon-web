package com.ontop.wms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String username;
    private RoleDTO role;
    private WarehouseDTO warehouse;

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
        private Long id;
        private String name;
    }
}