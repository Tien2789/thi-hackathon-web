package com.ontop.wms.repository;

import com.ontop.wms.entity.Role;
import com.ontop.wms.entity.User;
import com.ontop.wms.entity.Warehouse;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final WarehouseRepository warehouseRepository;

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

        Role adminRole   = roleRepository.findByName("ADMIN").orElseThrow();
        Role managerRole = roleRepository.findByName("MANAGER").orElseThrow();
        Role staffRole   = roleRepository.findByName("STAFF").orElseThrow();

        // 2. Seed or Update Default Admin (no warehouse)
        User admin = userRepository.findByUsername("admin").orElse(new User());
        admin.setUsername("admin");
        admin.setEmail("admin@ontop.com");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRole(adminRole);
        admin.setWarehouse(null);
        userRepository.save(admin);
        System.out.println(">>> Seeded admin / admin123");

        // 3. Seed 5 Warehouses
        String[][] warehouses = {
            {"KHO001", "Kho Hà Nội",   "123 Đường Láng, Hà Nội"},
            {"KHO002", "Kho TP.HCM",   "45 Nguyễn Tất Thành, TP.HCM"},
            {"KHO003", "Kho Đà Nẵng",  "78 Trần Phú, Đà Nẵng"},
            {"KHO004", "Kho Hải Phòng","21 Lạch Tray, Hải Phòng"},
            {"KHO005", "Kho Cần Thơ",  "99 Nguyễn Văn Cừ, Cần Thơ"}
        };

        for (String[] wh : warehouses) {
            if (warehouseRepository.findByCode(wh[0]).isEmpty()) {
                Warehouse warehouse = new Warehouse();
                warehouse.setCode(wh[0]);
                warehouse.setName(wh[1]);
                warehouse.setAddress(wh[2]);
                warehouseRepository.save(warehouse);
                System.out.println(">>> Seeded warehouse: " + wh[1]);
            }
        }

        // 4. Seed Managers & Staff spread across 5 warehouses
        seedUser("manager_hn",  "manager_hn@ontop.com",  "manager123", managerRole, "KHO001");
        seedUser("manager_hcm", "manager_hcm@ontop.com", "manager123", managerRole, "KHO002");
        seedUser("manager_dn",  "manager_dn@ontop.com",  "manager123", managerRole, "KHO003");
        seedUser("manager_hp",  "manager_hp@ontop.com",  "manager123", managerRole, "KHO004");
        seedUser("manager_ct",  "manager_ct@ontop.com",  "manager123", managerRole, "KHO005");

        seedUser("staff_hn1",  "staff_hn1@ontop.com",   "staff123", staffRole, "KHO001");
        seedUser("staff_hn2",  "staff_hn2@ontop.com",   "staff123", staffRole, "KHO001");
        seedUser("staff_hcm1", "staff_hcm1@ontop.com",  "staff123", staffRole, "KHO002");
        seedUser("staff_hcm2", "staff_hcm2@ontop.com",  "staff123", staffRole, "KHO002");
        seedUser("staff_dn1",  "staff_dn1@ontop.com",   "staff123", staffRole, "KHO003");
        seedUser("staff_hp1",  "staff_hp1@ontop.com",   "staff123", staffRole, "KHO004");
        seedUser("staff_ct1",  "staff_ct1@ontop.com",   "staff123", staffRole, "KHO005");
    }

    private void seedUser(String username, String email, String password, Role role, String warehouseCode) {
        if (userRepository.findByUsername(username).isPresent()) return;
        Warehouse warehouse = warehouseRepository.findByCode(warehouseCode).orElse(null);
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setWarehouse(warehouse);
        userRepository.save(user);
        System.out.println(">>> Seeded user: " + username + " -> " + warehouseCode);
    }
}
