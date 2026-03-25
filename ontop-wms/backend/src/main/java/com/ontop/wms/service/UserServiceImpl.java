package com.ontop.wms.service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ontop.wms.dto.CreateUserRequest;
import com.ontop.wms.dto.UserDTO;
import com.ontop.wms.entity.Role;
import com.ontop.wms.entity.User;
import com.ontop.wms.entity.Warehouse;
import com.ontop.wms.repository.RoleRepository;
import com.ontop.wms.repository.UserRepository;
import com.ontop.wms.repository.WarehouseRepository;

import lombok.RequiredArgsConstructor;

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
        return userRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
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

        if (request.getWarehouseId() != null) {
            Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid warehouse ID: " + request.getWarehouseId()));
            user.setWarehouse(warehouse);
        }

        User savedUser = userRepository.save(user);
        return convertToDto(savedUser);
    }

    @Override
    @Transactional
    public void deleteUser(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        // Lấy thông tin người dùng đang thực hiện hành động
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        User userToDelete = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));

        // Ngăn người dùng tự xóa tài khoản của chính mình
        if (Objects.equals(userToDelete.getUsername(), currentUsername)) {
            throw new IllegalArgumentException("You cannot delete your own account.");
        }
        userRepository.deleteById(id);
    }

    private UserDTO convertToDto(User user) {
        UserDTO.RoleDTO roleDTO = user.getRole() != null ? new UserDTO.RoleDTO(user.getRole().getRoleName()) : null;
        UserDTO.WarehouseDTO warehouseDTO = user.getWarehouse() != null ? new UserDTO.WarehouseDTO(user.getWarehouse().getId(), user.getWarehouse().getName()) : null;
        return new UserDTO(
                user.getId(),
                user.getUsername(),
                roleDTO,
                warehouseDTO
        );
    }
}