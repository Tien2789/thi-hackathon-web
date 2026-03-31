package com.ontop.wms.config;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.ontop.wms.entity.Role;
import com.ontop.wms.entity.User;
import com.ontop.wms.repository.RoleRepository;
import com.ontop.wms.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BootstrapDataRunner implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(BootstrapDataRunner.class);

    // Placeholder hash currently present in ScriptOnTop.sql (often copied from examples).
    // If your DB was seeded with this value, admin login will fail.
    private static final String KNOWN_BAD_ADMIN_HASH =
            "$2a$10$vI8aWBnW3f.TsMCsw./xO.ZnqM2V8s4q9N1U8sY3.d3E.f5G6h7I8";

    private static final String DEFAULT_ADMIN_USERNAME = "admin";
    private static final String DEFAULT_ADMIN_PASSWORD = "admin123";

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        ensureRoles(List.of("ADMIN", "MANAGER", "STAFF"));
        ensureAdminUser();
    }

    private void ensureRoles(List<String> roleNames) {
        for (String roleName : roleNames) {
            if (roleRepository.findByRoleName(roleName).isEmpty()) {
                Role role = new Role();
                role.setRoleName(roleName);
                roleRepository.save(role);
                logger.info("Bootstrapped role: {}", roleName);
            }
        }
    }

    private void ensureAdminUser() {
        Role adminRole = roleRepository.findByRoleName("ADMIN")
                .orElseThrow(() -> new IllegalStateException("Missing role ADMIN (bootstrap should have created it)"));

        Optional<User> existingAdminOpt = userRepository.findByUsername(DEFAULT_ADMIN_USERNAME);
        if (existingAdminOpt.isEmpty()) {
            User admin = new User();
            admin.setUsername(DEFAULT_ADMIN_USERNAME);
            admin.setPassword(passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD));
            admin.setRole(adminRole);
            admin.setIsActive(true);
            userRepository.save(admin);
            logger.info("Bootstrapped default admin user '{}' with password '{}'.", DEFAULT_ADMIN_USERNAME, DEFAULT_ADMIN_PASSWORD);
            return;
        }

        User admin = existingAdminOpt.get();
        boolean shouldReset = false;

        // If password is plain text or a known bad placeholder, reset to a valid BCrypt hash.
        String stored = admin.getPassword();
        if (stored == null || stored.isBlank()) {
            shouldReset = true;
        } else if (KNOWN_BAD_ADMIN_HASH.equals(stored)) {
            shouldReset = true;
        } else if (!looksLikeBCrypt(stored)) {
            shouldReset = true;
        } else {
            try {
                // Validate hash format; don't force-reset if user intentionally changed password.
                passwordEncoder.matches(DEFAULT_ADMIN_PASSWORD, stored);
            } catch (RuntimeException ex) {
                // Covers invalid BCrypt hash formats.
                shouldReset = true;
            }
        }

        // Keep role consistent for admin.
        if (admin.getRole() == null || !"ADMIN".equals(admin.getRole().getRoleName())) {
            admin.setRole(adminRole);
            userRepository.save(admin);
            logger.info("Updated role for user '{}' to ADMIN.", DEFAULT_ADMIN_USERNAME);
        }

        if (shouldReset) {
            admin.setPassword(passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD));
            userRepository.save(admin);
            logger.warn("Reset password for user '{}' to default value '{}'.", DEFAULT_ADMIN_USERNAME, DEFAULT_ADMIN_PASSWORD);
        }
    }

    private static boolean looksLikeBCrypt(String hash) {
        // Accept common prefixes and minimum length; BCrypt hashes are typically 60 chars.
        return (hash.startsWith("$2a$") || hash.startsWith("$2b$") || hash.startsWith("$2y$")) && hash.length() >= 50;
    }
}
