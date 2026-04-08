package com.ontop.wms.service;

import com.ontop.wms.dto.CreateUserRequest;
import com.ontop.wms.dto.UserDTO;

import java.util.List;

public interface UserService {
    List<UserDTO> getAllUsers();
    UserDTO createUser(CreateUserRequest request);
    List<UserDTO> getUsersByWarehouse(Integer warehouseId);
    UserDTO updateUser(Integer userId, UserDTO userDTO);
    void deleteUser(Integer userId);
}
