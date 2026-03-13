package com.ontop.wms.service;

import com.ontop.wms.dto.AuthResponse;
import com.ontop.wms.dto.LoginRequest;
import com.ontop.wms.entity.User;
import com.ontop.wms.repository.UserRepository;
import com.ontop.wms.security.JwtTokenUtil;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;

    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));

        String token = jwtTokenUtil.generateToken(user.getUsername());

        return AuthResponse.builder()
                .token(token)
                .role(user.getRole().getRoleName())
                .warehouseId(user.getWarehouse() != null ? user.getWarehouse().getId().longValue() : null)
                .warehouseName(user.getWarehouse() != null ? user.getWarehouse().getName() : null)
                .build();
    }
}