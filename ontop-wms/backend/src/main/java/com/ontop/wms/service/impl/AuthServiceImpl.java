package com.ontop.wms.service.impl;

import com.ontop.wms.dto.AuthResponse;
import com.ontop.wms.dto.LoginRequest;
import com.ontop.wms.entity.Role;
import com.ontop.wms.entity.User;
import com.ontop.wms.entity.Warehouse;
import com.ontop.wms.repository.UserRepository;
import com.ontop.wms.service.AuthService;
import com.ontop.wms.service.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public AuthServiceImpl(AuthenticationManager authenticationManager, UserRepository userRepository, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + request.email()));

        String jwt = jwtService.generateToken(user);

        String roleName = user.getRoles().stream().map(Role::getName).findFirst().orElse(null);

        Optional<Warehouse> warehouseOptional = user.getWarehouses().stream().findFirst();
        Long warehouseId = warehouseOptional.map(warehouse -> Long.valueOf(warehouse.getId())).orElse(null);
        String warehouseName = warehouseOptional.map(Warehouse::getName).orElse(null);

        return new AuthResponse(jwt, user.getUsername(), roleName, warehouseId, warehouseName);
    }
}
