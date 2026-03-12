package com.ontop.wms.service;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ontop.wms.dto.LoginRequest;
import com.ontop.wms.dto.LoginResponse;
import com.ontop.wms.dto.RegisterRequest;
import com.ontop.wms.entity.Role;
import com.ontop.wms.entity.User;
import com.ontop.wms.entity.Warehouse;
import com.ontop.wms.repository.RoleRepository;
import com.ontop.wms.repository.UserRepository;
import com.ontop.wms.repository.WarehouseRepository;
import com.ontop.wms.security.CustomUserDetails;
import com.ontop.wms.security.JwtTokenUtil;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final JwtTokenUtil jwtTokenUtil;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final WarehouseRepository warehouseRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        final User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + loginRequest.getUsername()));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        final CustomUserDetails userDetails = new CustomUserDetails(user);
        
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        final String token = jwtTokenUtil.generateToken(user.getUsername());

        return new LoginResponse(
                token,
                user.getRole() != null ? user.getRole().getRoleName() : null,
                user.getWarehouse() != null ? user.getWarehouse().getId() : null,
                user.getWarehouse() != null ? user.getWarehouse().getName() : null
        );
    }

    @Override
    @Transactional
    public User register(RegisterRequest request) {
        if (userRepository.findByUsername(request.username()).isPresent()) {
            throw new IllegalArgumentException("Username is already taken!");
        }
        
        User newUser = new User();
        newUser.setUsername(request.username());
        newUser.setPassword(passwordEncoder.encode(request.password()));
        newUser.setIsActive(true);

        if (request.roleName() != null) {
            Role role = roleRepository.findByRoleName(request.roleName())
                    .orElseThrow(() -> new EntityNotFoundException("Role not found: " + request.roleName()));
            newUser.setRole(role);
        }

        if (request.warehouseId() != null) {
            Warehouse warehouse = warehouseRepository.findById(request.warehouseId())
                    .orElseThrow(() -> new EntityNotFoundException("Warehouse not found: " + request.warehouseId()));
            newUser.setWarehouse(warehouse);
        }
        return userRepository.save(newUser);
    }

    @Override
    public User getCurrentUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));
    }
}