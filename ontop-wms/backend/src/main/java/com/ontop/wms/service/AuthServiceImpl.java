package com.ontop.wms.service;

import com.ontop.wms.dto.AuthResponse;
import com.ontop.wms.dto.LoginRequest;
import com.ontop.wms.entity.User;
import com.ontop.wms.entity.Warehouse;
import com.ontop.wms.repository.UserRepository;
import com.ontop.wms.security.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;

    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));

        // The User entity itself can be used as UserDetails if it implements the interface
        // For this example, let's adapt it to ensure compatibility
        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
            .username(user.getUsername())
            .password(user.getPassword()) // Password must be present, even if not used directly for token
            .authorities(user.getRoles().stream()
                .map(role -> role.getName()) // Use the role name directly
                .toArray(String[]::new))
            .build();

        String token = jwtTokenUtil.generateToken(userDetails);

        // For simplicity, returning the first role and warehouse. 
        // A more complex app might return all roles/warehouses or a primary one.
        String roleName = user.getRoles().stream().findFirst().map(role -> role.getName()).orElse(null);
        Warehouse firstWarehouse = user.getWarehouses().stream().findFirst().orElse(null);
        Integer warehouseId = firstWarehouse != null ? firstWarehouse.getId() : null;
        String warehouseName = firstWarehouse != null ? firstWarehouse.getName() : null;

        return new AuthResponse(
                token,
                user.getUsername(),
                roleName,
                warehouseId,
                warehouseName
        );
    }
}
