package com.smartinventory.config;

import com.smartinventory.user.entity.Role;
import com.smartinventory.user.entity.User;
import com.smartinventory.user.enums.RoleName;
import com.smartinventory.user.repository.RoleRepository;
import com.smartinventory.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private static final String DEFAULT_ADMIN_EMAIL = "admin@firma.com";
    private static final String DEFAULT_ADMIN_PASSWORD = "admin123";

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        createRoleIfNotExists(RoleName.WAREHOUSE_MANAGER, "Depo Sorumlusu - Tam yetki");
        createRoleIfNotExists(RoleName.DEPARTMENT_EMPLOYEE, "Bölüm Çalışanı - Sınırlı yetki");
        createDefaultAdminIfNotExists();
    }

    private void createRoleIfNotExists(RoleName roleName, String description) {
        if (roleRepository.findByName(roleName).isEmpty()) {
            Role role = Role.builder()
                    .name(roleName)
                    .description(description)
                    .build();
            roleRepository.save(role);
            log.info("Rol oluşturuldu: {}", roleName);
        }
    }

    private void createDefaultAdminIfNotExists() {
        if (userRepository.existsByEmail(DEFAULT_ADMIN_EMAIL)) {
            return;
        }
        Role managerRole = roleRepository.findByName(RoleName.WAREHOUSE_MANAGER)
                .orElseThrow(() -> new IllegalStateException("WAREHOUSE_MANAGER rolü bulunamadı"));

        User admin = User.builder()
                .firstName("Sistem")
                .lastName("Yöneticisi")
                .email(DEFAULT_ADMIN_EMAIL)
                .password(passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD))
                .department("Bilgi İşlem")
                .role(managerRole)
                .active(true)
                .build();
        userRepository.save(admin);
        log.info("Varsayılan yönetici oluşturuldu: {} / {}", DEFAULT_ADMIN_EMAIL, DEFAULT_ADMIN_PASSWORD);
    }
}
