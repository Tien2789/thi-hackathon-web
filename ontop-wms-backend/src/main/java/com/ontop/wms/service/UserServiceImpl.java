package com.ontop.wms.service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Access Denied"));

        // Kiểm tra quyền dựa trên tên Role (hỗ trợ cả tiền tố ROLE_)
        String rawRole = (currentUser.getRole() != null) ? currentUser.getRole().getRoleName() : "";
        boolean isAdmin = "ADMIN".equalsIgnoreCase(rawRole) || "ROLE_ADMIN".equalsIgnoreCase(rawRole);
        boolean isManager = "MANAGER".equalsIgnoreCase(rawRole) || "ROLE_MANAGER".equalsIgnoreCase(rawRole);

        if (isAdmin) {
            return userRepository.findAll().stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        } 
        
        // 2. Nếu là MANAGER -> Lọc theo warehouseId
        if (isManager) {
            if (currentUser.getWarehouse() == null) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied: Tài khoản quản lý chưa được gán kho");
            }
            Integer warehouseId = currentUser.getWarehouse().getId();
            return userRepository.findAll().stream()
                    .filter(u -> u.getWarehouse() != null && warehouseId.equals(u.getWarehouse().getId()))
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied: Bạn không có quyền xem danh sách nhân sự");
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAllRoleNames() {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Access Denied"));

        String rawRole = (currentUser.getRole() != null) ? currentUser.getRole().getRoleName() : "";
        boolean isAdmin = "ADMIN".equalsIgnoreCase(rawRole) || "ROLE_ADMIN".equalsIgnoreCase(rawRole);
        boolean isManager = "MANAGER".equalsIgnoreCase(rawRole) || "ROLE_MANAGER".equalsIgnoreCase(rawRole);

        if (isAdmin) {
            return roleRepository.findAll().stream()
                    .map(Role::getRoleName)
                    .collect(Collectors.toList());
        }
        
        // Đối với Manager, chỉ cho phép lấy vai trò STAFF (hỗ trợ tìm tên chính xác từ DB)
        if (isManager) {
            return roleRepository.findAll().stream()
                    .map(Role::getRoleName)
                    .filter(name -> name.equalsIgnoreCase("STAFF") || name.equalsIgnoreCase("ROLE_STAFF"))
                    .collect(Collectors.toList());
        }
        
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied: Bạn không có quyền xem danh sách vai trò");
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

        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail()); // Giả định Entity User đã có field email
        dto.setRole(roleDTO);
        dto.setWarehouse(warehouseDTO);
        dto.setIsActive(user.getIsActive());
        return dto;
    }
}