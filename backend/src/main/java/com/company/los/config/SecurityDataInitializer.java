package com.company.los.config;

import com.company.los.entity.Permission;
import com.company.los.entity.Role;
import com.company.los.entity.User;
import com.company.los.repository.PermissionRepository;
import com.company.los.repository.RoleRepository;
import com.company.los.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Эрх, роль, хэрэглэгчийн анхны өгөгдлийг (seed) үүсгэнэ.
 *
 * <p>Idempotent — дахин ажиллахад байгаа мөрүүдийг давхарлахгүй. Тест профайл
 * дээр ажиллахгүй. Production-д `app.seed.enabled=false` болгож унтраана.</p>
 */
@Component
@Order(1) // DataInitializer (харилцагч) -ээс өмнө ажиллана
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
public class SecurityDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SecurityDataInitializer.class);

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.enabled:true}")
    private boolean seedEnabled;

    @Value("${app.seed.admin-password:admin123}")
    private String adminPassword;

    public SecurityDataInitializer(PermissionRepository permissionRepository,
                                   RoleRepository roleRepository,
                                   UserRepository userRepository,
                                   PasswordEncoder passwordEncoder) {
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!seedEnabled) {
            log.info("ℹ️ Эрх/роль/хэрэглэгчийн seed унтраалттай (app.seed.enabled=false)");
            return;
        }

        Map<String, Permission> perms = new LinkedHashMap<>();
        // Анхаар: `action` нь schema-гийн CHECK-тэй тохирох ёстой:
        // CREATE, READ, UPDATE, DELETE, APPROVE, REJECT, EXPORT, IMPORT, CONFIG
        perms.put("CUSTOMER_VIEW",   permission("CUSTOMER_VIEW",   "Харилцагч харах",   "customer", "READ",   "CUSTOMER"));
        perms.put("CUSTOMER_CREATE", permission("CUSTOMER_CREATE", "Харилцагч бүртгэх", "customer", "CREATE", "CUSTOMER"));
        perms.put("CUSTOMER_UPDATE", permission("CUSTOMER_UPDATE", "Харилцагч засах",   "customer", "UPDATE", "CUSTOMER"));
        perms.put("LOAN_VIEW",       permission("LOAN_VIEW",       "Хүсэлт харах",      "loan",     "READ",   "LOAN"));
        perms.put("LOAN_CREATE",     permission("LOAN_CREATE",     "Хүсэлт үүсгэх",     "loan",     "CREATE", "LOAN"));
        perms.put("LOAN_APPROVE",    permission("LOAN_APPROVE",    "Зээл батлах",       "loan",     "APPROVE","LOAN"));
        perms.put("LOAN_REJECT",     permission("LOAN_REJECT",     "Зээл татгалзах",    "loan",     "REJECT", "LOAN"));
        perms.put("DOCUMENT_VERIFY", permission("DOCUMENT_VERIFY", "Баримт хянах",      "document", "UPDATE", "DOCUMENT"));
        perms.put("USER_MANAGE",     permission("USER_MANAGE",     "Хэрэглэгч удирдах", "user",     "CONFIG", "USER"));
        perms.put("REPORT_VIEW",     permission("REPORT_VIEW",     "Тайлан харах",      "report",   "READ",   "REPORT"));

        Map<String, Role> roles = new LinkedHashMap<>();
        roles.put("SUPER_ADMIN", role("SUPER_ADMIN", "Системийн админ", 10, perms.values()));
        roles.put("MANAGER", role("MANAGER", "Салбарын менежер", 30, List.of(
                perms.get("CUSTOMER_VIEW"), perms.get("CUSTOMER_CREATE"), perms.get("CUSTOMER_UPDATE"),
                perms.get("LOAN_VIEW"), perms.get("LOAN_CREATE"), perms.get("LOAN_APPROVE"),
                perms.get("LOAN_REJECT"), perms.get("DOCUMENT_VERIFY"), perms.get("REPORT_VIEW"))));
        roles.put("LOAN_OFFICER", role("LOAN_OFFICER", "Зээлийн мэргэжилтэн", 50, List.of(
                perms.get("CUSTOMER_VIEW"), perms.get("CUSTOMER_CREATE"), perms.get("CUSTOMER_UPDATE"),
                perms.get("LOAN_VIEW"), perms.get("LOAN_CREATE"))));
        roles.put("DOCUMENT_REVIEWER", role("DOCUMENT_REVIEWER", "Баримт хянагч", 60, List.of(
                perms.get("CUSTOMER_VIEW"), perms.get("LOAN_VIEW"), perms.get("DOCUMENT_VERIFY"))));
        roles.put("CUSTOMER_SERVICE", role("CUSTOMER_SERVICE", "Харилцагчийн үйлчилгээ", 70, List.of(
                perms.get("CUSTOMER_VIEW"), perms.get("CUSTOMER_CREATE"), perms.get("CUSTOMER_UPDATE"),
                perms.get("LOAN_VIEW"), perms.get("LOAN_CREATE"))));

        user("admin",            "admin@los.mn",    "Системийн", "Админ",       adminPassword, roles.get("SUPER_ADMIN"));
        user("manager",          "manager@los.mn",  "Салбарын",  "Менежер",     "manager123",  roles.get("MANAGER"));
        user("loan_officer",     "loan@los.mn",     "Зээлийн",   "Мэргэжилтэн", "loan123",     roles.get("LOAN_OFFICER"));
        user("reviewer",         "reviewer@los.mn", "Баримт",    "Хянагч",      "review123",   roles.get("DOCUMENT_REVIEWER"));
        user("customer_service", "service@los.mn",  "Үйлчилгээ", "Ажилтан",     "service123",  roles.get("CUSTOMER_SERVICE"));

        log.info("✅ Seed дууслаа: {} эрх, {} роль, {} хэрэглэгч",
                permissionRepository.count(), roleRepository.count(), userRepository.count());
    }

    private Permission permission(String name, String displayNameMn,
                                  String resource, String action, String category) {
        return permissionRepository.findByName(name).orElseGet(() -> {
            Permission permission = new Permission();
            permission.setName(name);
            permission.setDisplayName(name);
            permission.setDisplayNameMn(displayNameMn);
            permission.setResource(resource);
            permission.setAction(action);
            permission.setCategory(category);
            permission.setIsActive(true);
            permission.setIsDeleted(false);
            return permissionRepository.save(permission);
        });
    }

    private Role role(String name, String displayNameMn, int priority, Collection<Permission> permissions) {
        Role role = roleRepository.findByName(name).orElseGet(() -> {
            Role created = new Role();
            created.setName(name);
            created.setCode(name);
            created.setDisplayName(name);
            created.setDescription(displayNameMn);
            created.setPriority(priority);
            created.setType(Role.RoleType.BUSINESS);
            created.setStatus(Role.RoleStatus.ACTIVE);
            created.setIsActive(true);
            created.setIsDefault(false);
            created.setPermissions(new ArrayList<>());
            return roleRepository.save(created);
        });

        if (role.getPermissions() == null) {
            role.setPermissions(new ArrayList<>());
        }
        for (Permission permission : permissions) {
            boolean exists = role.getPermissions().stream()
                    .anyMatch(existing -> existing.getName().equals(permission.getName()));
            if (!exists) {
                role.getPermissions().add(permission);
            }
        }
        return roleRepository.save(role);
    }

    private void user(String username, String email, String firstName, String lastName,
                      String rawPassword, Role role) {
        if (userRepository.findByUsername(username).isPresent()) {
            return;
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setIsActive(true);
        user.setIsDeleted(false);
        user.setRoles(new ArrayList<>(List.of(role)));
        userRepository.save(user);
    }
}
