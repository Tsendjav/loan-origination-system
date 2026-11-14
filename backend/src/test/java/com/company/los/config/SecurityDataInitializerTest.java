package com.company.los.config;

import com.company.los.dto.AuthResponseDto;
import com.company.los.dto.LoginRequestDto;
import com.company.los.entity.Role;
import com.company.los.entity.User;
import com.company.los.repository.PermissionRepository;
import com.company.los.repository.RoleRepository;
import com.company.los.repository.UserRepository;
import com.company.los.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Seed + DB хэрэглэгчээр нэвтрэх end-to-end тест (H2).
 *
 * <p>Docker/production-д `demo-users-enabled=false` тул нэвтрэлт зөвхөн
 * өгөгдлийн сангийн хэрэглэгчээр явагдана. Энэ тест нь seed-ийг ажиллуулж,
 * `admin/admin123`-аар DB-ээр нэвтэрч байгааг батална.</p>
 */
@SpringBootTest(
        classes = com.company.los.LoanOriginationApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                "spring.profiles.active=test",
                "app.seed.enabled=true",
                "app.auth.demo-users-enabled=false",
                "spring.datasource.url=jdbc:h2:mem:seedtest;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.sql.init.mode=never",
                "logging.level.org.springframework=WARN",
                "logging.level.org.hibernate=WARN"
        })
@ActiveProfiles("test")
class SecurityDataInitializerTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthService authService;

    @Test
    @DisplayName("Seed нь эрх, роль, хэрэглэгчийг үүсгэнэ")
    void seedCreatesUsersRolesAndPermissions() {
        assertThat(permissionRepository.count()).isGreaterThanOrEqualTo(10);
        assertThat(roleRepository.count()).isGreaterThanOrEqualTo(5);
        assertThat(userRepository.findByUsername("admin")).isPresent();

        User admin = userRepository.findByUsername("admin").orElseThrow();
        assertThat(admin.getRoles()).isNotEmpty();
        assertThat(admin.getRoles().stream().map(Role::getName))
                .contains("SUPER_ADMIN");
    }

    @Test
    @DisplayName("Нууц үг BCrypt-ээр хадгалагдана")
    void passwordIsHashed() {
        User admin = userRepository.findByUsername("admin").orElseThrow();

        assertThat(admin.getPasswordHash()).isNotEqualTo("admin123");
        assertThat(passwordEncoder.matches("admin123", admin.getPasswordHash())).isTrue();
    }

    @Test
    @DisplayName("demo users унтраалттай үед DB хэрэглэгчээр нэвтэрнэ")
    void loginWorksWithDatabaseUser() {
        LoginRequestDto request = new LoginRequestDto();
        request.setUsername("admin");
        request.setPassword("admin123");

        AuthResponseDto response = authService.login(request);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getToken()).isNotBlank();
        assertThat(response.getUsername()).isEqualTo("admin");
    }
}
