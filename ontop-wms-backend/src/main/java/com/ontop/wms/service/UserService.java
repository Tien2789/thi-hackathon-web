package com.ontop.wms.service;

import java.util.List;

import com.ontop.wms.dto.CreateUserRequest;
import com.ontop.wms.dto.UserDTO;

public interface UserService {
    List<UserDTO> getAllUsers();
    List<String> getAllRoleNames();
    UserDTO createUser(CreateUserRequest request);
    void deleteUser(Long id);
}