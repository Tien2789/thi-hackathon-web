package com.ontop.wms.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.ontop.wms.dto.AuthResponse;
import com.ontop.wms.dto.LoginRequest;
import com.ontop.wms.entity.User;
import com.ontop.wms.repository.UserRepository;
import com.ontop.wms.security.JwtTokenUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        try {
            // Step 1: Authenticate the user. If this fails, it will throw an AuthenticationException.
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            // Step 2: Re-fetch the user to get associated data like role and warehouse.
            // The UserRepository's findByUsername should use @EntityGraph to prevent LazyInitializationException.
            User user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User authenticated but not found"));

            // Step 3: Validate that the user has a role.
            if (user.getRole() == null) {
                logger.error("User '{}' authenticated successfully but has no role assigned.", request.getUsername());
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Authenticated user has no assigned role.");
            }

            // Step 4: Generate JWT token.
            String token = jwtTokenUtil.generateToken(user.getUsername());
            
            // Step 5: Build the response DTO.
            Integer warehouseId = null;
            String warehouseName = null;
            if (user.getWarehouse() != null) {
                warehouseId = user.getWarehouse().getId();
                warehouseName = user.getWarehouse().getName();
            }

            return AuthResponse.builder()
                    .token(token)
                    .role(user.getRole().getRoleName())
                    .warehouseId(warehouseId)
                    .warehouseName(warehouseName)
                    .build();

        } catch (AuthenticationException e) {
            logger.warn("Authentication failed for user '{}'", request.getUsername());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        } catch (Throwable e) {
            // Catch any other unexpected errors during the login process.
            // Lưu ý: Không log object 'e' đầy đủ nếu nghi ngờ lỗi StackOverflow do Lombok
            logger.error("Error logging in user '{}': {}", request.getUsername(), e.getMessage());
            e.printStackTrace(); // In toàn bộ lỗi ra Terminal của VS Code để bạn kiểm tra
            // Throw a 500 error to reflect a server-side problem.
            // Tạm thời trả về message lỗi cụ thể để debug ở Frontend
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi Server: " + e.toString());
        }
    }
}