package com.ontop.wms.dto;

import java.util.Set;

import lombok.Data;

@Data
public class CreateUserRequest {
    private String username;
    private String password;
    private String name;
    private String email;
    private String role;
    private Set<String> warehouses;
}