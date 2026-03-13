package com.ontop.wms.service;

import com.ontop.wms.dto.AuthResponse;
import com.ontop.wms.dto.LoginRequest;

public interface AuthService {
    AuthResponse login(LoginRequest request);
}