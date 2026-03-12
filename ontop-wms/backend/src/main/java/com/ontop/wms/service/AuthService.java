package com.ontop.wms.service;

import com.ontop.wms.dto.LoginRequest;
import com.ontop.wms.dto.LoginResponse;
import com.ontop.wms.dto.RegisterRequest;
import com.ontop.wms.entity.User;

public interface AuthService {
    LoginResponse login(LoginRequest loginRequest);
    User register(RegisterRequest registerRequest);
    User getCurrentUser(String username);
}