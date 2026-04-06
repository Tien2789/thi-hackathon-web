package com.ontop.wms.service;

import com.ontop.wms.dto.CreateUserRequest;
import com.ontop.wms.dto.UserDTO;
import com.ontop.wms.entity.Role;
import com.ontop.wms.entity.User;
import com.ontop.wms.entity.Warehouse;
import com.ontop.wms.repository.RoleRepository;
import com.ontop.wms.repository.UserRepository;
import com.ontop.wms.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final WarehouseRepository warehouseRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new IllegalArgumentException("Current user not found"));

        // Assuming the first role is the primary role
        String primaryRole = currentUser.getRoles().stream().findFirst()
                                      .map(Role::getName)
                                      .orElse(null);

        if ("ADMIN".equals(primaryRole)) {
            return userRepository.findAll().stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        } else if ("MANAGER".equals(primaryRole)) {
            // Assuming a manager is associated with at most one warehouse
            return currentUser.getWarehouses().stream().findFirst()
                    .map(warehouse -> userRepository.findByWarehouse_Id(warehouse.getId()).stream()
                                                .map(this::convertToDto)
                                                .collect(Collectors.toList()))
                    .orElse(List.of());
        }
        return List.of(); // Other roles cannot see the user list
    }

    @Override
    @Transactional
    public UserDTO createUser(CreateUserRequest request) {
        userRepository.findByUsername(request.getUsername()).ifPresent(u -> {
            throw new IllegalArgumentException("Username already exists");
        });

        Role role = roleRepository.findByName(request.getRole())
                .orElseThrow(() -> new IllegalArgumentException("Invalid role specified: " + request.getRole()));

        Set<Warehouse> warehouses = new HashSet<>();
        if (request.getWarehouses() != null && !request.getWarehouses().isEmpty()) {
            warehouses = request.getWarehouses().stream()
                    .map(warehouseCode -> warehouseRepository.findByCode(warehouseCode)
                            .orElseThrow(() -> new IllegalArgumentException("Invalid warehouse code: " + warehouseCode)))
                    .collect(Collectors.toSet());
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        
        Set<Role> roles = new HashSet<>();
        roles.add(role);
        user.setRoles(roles);
        user.setWarehouses(warehouses);

        User savedUser = userRepository.save(user);
        return convertToDto(savedUser);
    }

    // Method to convert User entity to UserDTO
    private UserDTO convertToDto(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRoles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()));
        dto.setWarehouses(user.getWarehouses().stream().map(Warehouse::getCode).collect(Collectors.toSet()));
        return dto;
    }
    
    // The lockUser functionality is removed as the isActive property is no longer in use.
    // If user deactivation is needed, a new implementation (e.g., a 'status' field) is required.
}
