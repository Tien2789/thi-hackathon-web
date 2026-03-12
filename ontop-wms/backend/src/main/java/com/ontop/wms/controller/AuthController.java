package com.ontop.wms.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ontop.wms.dto.LoginRequest;
import com.ontop.wms.dto.RegisterRequest;
import com.ontop.wms.service.AuthService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        logger.info("Attempting login for user: '{}'", loginRequest.getUsername());
        try {
            return ResponseEntity.ok(authService.login(loginRequest));
        } catch (UsernameNotFoundException | BadCredentialsException e) {
            logger.warn("Login failed for user: '{}'", loginRequest.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Tai khoan hoac mat khau khong chinh xac"));
        } catch (DisabledException e) {
            logger.warn("Login failed for user: '{}'. Reason: Account is disabled.", loginRequest.getUsername());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Tai khoan cua ban da bi khoa."));
        }
    }

    // helper endpoint so clients can self-register or tests can create users without going through /users
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        logger.info("Registering new user '{}', role: {}", request.username(), request.roleName());
        try {
            authService.register(request);
            return ResponseEntity.ok(Map.of("message", "Dang ky thanh cong"));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }
}