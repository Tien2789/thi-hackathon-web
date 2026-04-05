package com.ontop.wms.service;

import java.util.List;
import com.ontop.wms.dto.CreateUserRequest;
import com.ontop.wms.dto.UserDTO;

public interface UserService {
    List<UserDTO> getAllUsers(); // Can we just use SecurityContextHolder inside UserServiceImpl directly? Yes!
    List<String> getAllRoleNames();
    UserDTO createUser(CreateUserRequest request);
    void lockUser(Integer id); // Renamed from deleteUser to lockUser
}