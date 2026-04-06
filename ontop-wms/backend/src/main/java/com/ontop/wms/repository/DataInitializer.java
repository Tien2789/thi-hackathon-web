package com.ontop.wms.repository;

import com.ontop.wms.entity.Role;
import com.ontop.wms.entity.User;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // 1. Seed Roles
        List<String> rolesToSeed = Arrays.asList("ADMIN", "MANAGER", "STAFF");
        for (String roleName : rolesToSeed) {
            if (roleRepository.findByName(roleName).isEmpty()) {
                Role role = new Role();
                role.setName(roleName);
                roleRepository.save(role);
            }
        }

        // 2. Seed Default Admin
        if (userRepository.findByUsername("admin").isEmpty()) {
            Role adminRole = roleRepository.findByName("ADMIN")
                    .orElseThrow(() -> new RuntimeException("ADMIN role not found after seeding"));
            
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            Set<Role> roles = new HashSet<>();
            roles.add(adminRole);
            admin.setRoles(roles);
            userRepository.save(admin);
            
            System.out.println(">>> Seeded default admin user: admin / admin123");
        }
    }
}
