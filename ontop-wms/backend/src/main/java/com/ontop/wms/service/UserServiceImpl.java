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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if ("ADMIN".equals(currentUser.getRole().getRoleName())) {
            return userRepository.findAll().stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        } else if ("MANAGER".equals(currentUser.getRole().getRoleName())) {
            if (currentUser.getWarehouse() == null) {
                return List.of();
            }
            return userRepository.findByWarehouse_Id(currentUser.getWarehouse().getId()).stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        }
        return List.of(); // Users cannot see
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAllRoleNames() {
        return roleRepository.findAll().stream()
                .map(Role::getRoleName)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserDTO createUser(CreateUserRequest request) {
        userRepository.findByUsername(request.getUsername()).ifPresent(u -> {
            throw new IllegalArgumentException("Username already exists");
        });

        Role role = roleRepository.findByRoleName(request.getRoleName())
                .orElseThrow(() -> new IllegalArgumentException("Invalid role: " + request.getRoleName()));

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        user.setIsActive(true);

        Integer warehouseId = request.getWarehouseId();
        if (warehouseId != null) {
            Warehouse warehouse = warehouseRepository.findById(warehouseId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid warehouse ID: " + warehouseId));
            user.setWarehouse(warehouse);
        }

        User savedUser = userRepository.save(user);
        return convertToDto(savedUser);
    }


    @Override
    @Transactional
    public void lockUser(Integer id) { 
        if (id == null) return;
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
        
        if ("admin".equals(user.getUsername()) || "ADMIN".equals(user.getRole().getRoleName())) {
            throw new IllegalArgumentException("Cannot lock the admin account");
        }

        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(currentUsername).orElseThrow();

        if ("MANAGER".equals(currentUser.getRole().getRoleName())) {
            if (user.getWarehouse() == null || currentUser.getWarehouse() == null ||
                !user.getWarehouse().getId().equals(currentUser.getWarehouse().getId())) {
                throw new IllegalArgumentException("Manager can only lock users in their own warehouse");
            }
        }
        
        user.setIsActive(user.getIsActive() == null || !user.getIsActive());
        userRepository.save(user);
    }

    private UserDTO convertToDto(User user) {
        UserDTO.RoleDTO roleDTO = user.getRole() != null ? new UserDTO.RoleDTO(user.getRole().getRoleName()) : null;
        UserDTO.WarehouseDTO warehouseDTO = user.getWarehouse() != null ? new UserDTO.WarehouseDTO(user.getWarehouse().getId(), user.getWarehouse().getName()) : null;
        return new UserDTO(
                user.getId(),
                user.getUsername(),
                roleDTO,
                warehouseDTO,
                user.getIsActive() == null || user.getIsActive()
        );
    }
}