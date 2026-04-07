package com.ontop.wms.service.impl;

import com.ontop.wms.dto.CreateUserRequest;
import com.ontop.wms.dto.UserDTO;
import com.ontop.wms.entity.Role;
import com.ontop.wms.entity.User;
import com.ontop.wms.entity.Warehouse;
import com.ontop.wms.repository.RoleRepository;
import com.ontop.wms.repository.UserRepository;
import com.ontop.wms.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDTO createUser(CreateUserRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        Role role = roleRepository.findByName(request.getRole())
                .orElseThrow(() -> new RuntimeException("Role not found: " + request.getRole()));
        user.setRoles(Collections.singleton(role));

        User savedUser = userRepository.save(user);
        return convertToDto(savedUser);
    }

    @Override
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    public List<UserDTO> getUsersByWarehouse(Integer warehouseId) {
        return userRepository.findByWarehouses_Id(warehouseId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDTO updateUser(Long userId, UserDTO userDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());

        Set<Role> roles = userDTO.getRoles().stream()
                .map(roleName -> roleRepository.findByName(roleName)
                        .orElseThrow(() -> new RuntimeException("Role not found: " + roleName)))
                .collect(Collectors.toSet());
        user.setRoles(roles);

        User updatedUser = userRepository.save(user);
        return convertToDto(updatedUser);
    }

    @Override
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    @SuppressWarnings("null")
    private UserDTO convertToDto(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setUsername(user.getUsername());
        userDTO.setEmail(user.getEmail());
        userDTO.setRoles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()));
        userDTO.setWarehouses(user.getWarehouses().stream().map(Warehouse::getCode).collect(Collectors.toSet()));
        return userDTO;
    }
}
