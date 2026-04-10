package com.ontop.wms.service.impl;

import com.ontop.wms.dto.CreateUserRequest;
import com.ontop.wms.dto.UserDTO;
import com.ontop.wms.entity.Role;
import com.ontop.wms.entity.User;
import com.ontop.wms.entity.Warehouse;
import com.ontop.wms.repository.RoleRepository;
import com.ontop.wms.repository.UserRepository;
import com.ontop.wms.repository.WarehouseRepository;
import com.ontop.wms.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private WarehouseRepository warehouseRepository;

    @Override
    public UserDTO createUser(CreateUserRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        Role role = roleRepository.findByName(request.getRole())
                .orElseThrow(() -> new RuntimeException("Role not found: " + request.getRole()));
        user.setRole(role);

        // Assign warehouse if provided
        if (request.getWarehouse() != null && !request.getWarehouse().isEmpty()) {
            Warehouse warehouse = warehouseRepository.findByCode(request.getWarehouse())
                    .orElseThrow(() -> new RuntimeException("Warehouse not found: " + request.getWarehouse()));
            user.setWarehouse(warehouse);
        }

        User savedUser = userRepository.save(user);
        return convertToDto(savedUser);
    }

    @Override
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    public UserDTO getUserById(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
        return convertToDto(user);
    }

    @Override
    public List<UserDTO> getUsersByWarehouse(Integer warehouseId) {
        return userRepository.findByWarehouse_Id(warehouseId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDTO updateUser(Integer userId, UserDTO userDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());

        String roleName = userDTO.getRole();
        if (roleName != null && !roleName.isEmpty()) {
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
            user.setRole(role);
        } else {
            user.setRole(null);
        }

        // Update warehouse assignment
        String warehouseCode = userDTO.getWarehouse();
        if (warehouseCode != null && !warehouseCode.isEmpty()) {
            Warehouse warehouse = warehouseRepository.findByCode(warehouseCode)
                    .orElseThrow(() -> new RuntimeException("Warehouse not found: " + warehouseCode));
            user.setWarehouse(warehouse);
        } else {
            user.setWarehouse(null);
        }

        User updatedUser = userRepository.save(user);
        return convertToDto(updatedUser);
    }

    @Override
    public void deleteUser(Integer userId) {
        userRepository.deleteById(userId);
    }

    @SuppressWarnings("null")
    private UserDTO convertToDto(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId().longValue());
        userDTO.setUsername(user.getUsername());
        userDTO.setEmail(user.getEmail());
        userDTO.setRole(user.getRole() != null ? user.getRole().getName() : null);
        userDTO.setWarehouse(user.getWarehouse() != null ? user.getWarehouse().getCode() : null);
        return userDTO;
    }
}
