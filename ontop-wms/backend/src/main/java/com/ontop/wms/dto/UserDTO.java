package com.ontop.wms.dto;

import java.util.Set;

import lombok.Data;

@Data
public class UserDTO {
    private Integer id;
    private String username;
    private String name;
    private String email;
    private Set<String> roles;
    private Set<String> warehouses;
}