package com.aerotech.ced_ops_backend.common.config;

import com.aerotech.ced_ops_backend.role.entity.Role;
import com.aerotech.ced_ops_backend.role.repository.RoleRepository;
import com.aerotech.ced_ops_backend.user.entity.User;
import com.aerotech.ced_ops_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (roleRepository.count() > 0) {
            log.info("Roles already seeded, skipping");
        } else {
            List<Role> roles = List.of(
                    Role.builder().name("SUPER_ADMIN").description("Super Administrator").build(),
                    Role.builder().name("ADMIN").description("Administrator").build(),
                    Role.builder().name("OPERATOR").description("Operator").build(),
                    Role.builder().name("INSPECTOR").description("Inspector").build()
            );
            roleRepository.saveAll(roles);
            log.info("Seeded {} roles", roles.size());
        }

        if (userRepository.count() > 0) {
            log.info("Users already exist, skipping default admin seed");
        } else {
            Role superAdmin = roleRepository.findByName("SUPER_ADMIN")
                    .orElseThrow(() -> new IllegalStateException("SUPER_ADMIN role not found after seeding"));

            User admin = User.builder()
                    .employeeId("ADMIN001")
                    .firstName("System")
                    .lastName("Admin")
                    .mobileNumber("9999999999")
                    .password(passwordEncoder.encode("admin123"))
                    .role(superAdmin)
                    .active(true)
                    .build();

            userRepository.save(admin);
            log.info("Seeded default SUPER_ADMIN user: employeeId=ADMIN001, password=admin123");
        }
    }
}
