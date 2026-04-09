package com.smartinventory.config;

import com.smartinventory.user.entity.Role;
import com.smartinventory.user.enums.RoleName;
import com.smartinventory.user.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        createRoleIfNotExists(RoleName.WAREHOUSE_MANAGER, "Depo Sorumlusu - Tam yetki");
        createRoleIfNotExists(RoleName.DEPARTMENT_EMPLOYEE, "Bölüm Çalışanı - Sınırlı yetki");
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
}
