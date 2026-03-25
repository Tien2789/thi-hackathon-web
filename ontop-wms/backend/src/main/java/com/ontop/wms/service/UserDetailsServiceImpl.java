package com.ontop.wms.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ontop.wms.entity.User;
import com.ontop.wms.repository.UserRepository;
import com.ontop.wms.security.CustomUserDetails;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

            if (user.getRole() == null || user.getRole().getRoleName() == null || user.getRole().getRoleName().isEmpty()) {
                // A user without a role should not be able to log in.
                throw new UsernameNotFoundException("User '" + username + "' has no valid role assigned.");
            }

            return new CustomUserDetails(user);
        } catch (Exception e) {
            // Catch any DB integrity issues (EntityNotFound) or other runtime errors
            throw new UsernameNotFoundException("Error loading user details: " + e.getMessage());
        }
    }
}
