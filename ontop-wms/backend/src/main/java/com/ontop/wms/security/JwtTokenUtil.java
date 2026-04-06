package com.ontop.wms.security;

import java.io.Serializable;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenUtil implements Serializable {
    public String generateToken(UserDetails userDetails) {
        // Placeholder for JWT generation logic
        return "";
    }
    
    // Add validation and extraction methods as needed
}