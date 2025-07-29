package com.company.los.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.http.HttpStatus;

/**
 * ⭐ LOAN ORIGINATION SYSTEM SECURITY CONFIGURATION - БҮРЭН ХУВИЛБАР ⭐
 * * Security тохиргоо:
 * - BCrypt password encoding
 * - AuthenticationManager bean (AuthServiceImpl-д шаардлагатай)
 * - DaoAuthenticationProvider
 * - CORS тохиргоо  
 * - API endpoints-д 401 response (302 redirect биш)
 * - H2 Console зөвшөөрөл
 * - Development mode тохиргоо
 * - JWT dependency-гүй simplified хувилбар
 * * @author LOS Development Team
 * @version 4.1 (AuthenticationManager нэмэгдсэн)
 * @since 2025-07-28
 */
@Slf4j // Энэ аннотацийг нэмсэн
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class SecurityConfig {

    @Value("${app.security.bcrypt.strength:12}")
    private int bcryptStrength;

    @Autowired
    @Lazy  // Circular dependency-ийг шийдэх
    private UserDetailsService userDetailsService;

    /**
     * BCrypt Password Encoder бэлдэх
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        log.info("BCrypt password encoder үүсгэж байна. Strength: {}", bcryptStrength);
        return new BCryptPasswordEncoder(bcryptStrength);
    }

    /**
     * ⭐ AuthenticationManager Bean - AuthServiceImpl-д шаардлагатай ⭐
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        log.info("AuthenticationManager bean үүсгэж байна");
        return config.getAuthenticationManager();
    }

    /**
     * DaoAuthenticationProvider Bean
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        log.info("DaoAuthenticationProvider bean үүсгэж байна");
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * ⭐ SIMPLIFIED Security Filter Chain - JWT dependency-гүй ⭐
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.info("Security filter chain тохируулж байна");

        http
            // CSRF тохиргоо
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/h2-console/**")
                .ignoringRequestMatchers("/api/**")
                .ignoringRequestMatchers("/actuator/**")
                .ignoringRequestMatchers("/los/h2-console/**")
                .ignoringRequestMatchers("/los/api/**")
                .ignoringRequestMatchers("/los/actuator/**")
            )
            
            // CORS тохиргоо - CorsConfig.java файлаас ашиглана
            .cors(cors -> {})
            
            // ⭐ Authorization тохиргоо - API endpoints зөвшөөрөгдсөн ⭐
            .authorizeHttpRequests(authz -> authz
                // ✅ PUBLIC ENDPOINTS - AUTHENTICATION ШААРДЛАГАГҮЙ
                .requestMatchers("/h2-console/**", "/los/h2-console/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/los/swagger-ui/**").permitAll()
                .requestMatchers("/swagger-ui.html", "/los/swagger-ui.html").permitAll()
                .requestMatchers("/api-docs/**", "/los/api-docs/**").permitAll()
                .requestMatchers("/v3/api-docs/**", "/los/v3/api-docs/**").permitAll()
                .requestMatchers("/actuator/**", "/los/actuator/**").permitAll()
                .requestMatchers("/webjars/**").permitAll()
                .requestMatchers("/favicon.ico", "/los/favicon.ico").permitAll()
                .requestMatchers("/error", "/los/error").permitAll()
                .requestMatchers("/robots.txt", "/los/robots.txt").permitAll()
                
                // ✅ API HEALTH & AUTH ENDPOINTS - DEVELOPMENT MODE
                .requestMatchers("/api/v1/health/**", "/los/api/v1/health/**").permitAll()
                .requestMatchers("/api/v1/auth/**", "/los/api/v1/auth/**").permitAll()
                .requestMatchers("/api/v1/", "/los/api/v1/").permitAll()
                
                // ✅ LOGIN/LOGOUT PAGES
                .requestMatchers("/login", "/logout").permitAll()
                
                // Static resources
                .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                
                // ⚠️ ХӨГЖҮҮЛЭЛТИЙН РЕЖИМ: API endpoints-г түр зөвшөөрнө
                .requestMatchers("/api/v1/customers/**", "/los/api/v1/customers/**").permitAll()
                .requestMatchers("/api/v1/documents/**", "/los/api/v1/documents/**").permitAll()
                .requestMatchers("/api/v1/loans/**", "/los/api/v1/loans/**").permitAll()
                .requestMatchers("/api/v1/loan-applications/**", "/los/api/v1/loan-applications/**").permitAll()
                .requestMatchers("/api/v1/loan-products/**", "/los/api/v1/loan-products/**").permitAll()
                .requestMatchers("/api/v1/users/**", "/los/api/v1/users/**").permitAll()
                
                // Admin эрх шаардагдах endpoints
                .requestMatchers("/admin/**").hasRole("SUPER_ADMIN")
                .requestMatchers("/api/v1/admin/**", "/los/api/v1/admin/**").hasRole("SUPER_ADMIN")
                
                // Manager эрх шаардагдах endpoints
                .requestMatchers("/api/v1/reports/**", "/los/api/v1/reports/**").hasAnyRole("MANAGER", "SUPER_ADMIN")
                .requestMatchers("/api/v1/manager/**", "/los/api/v1/manager/**").hasAnyRole("SUPER_ADMIN", "MANAGER")
                
                // System endpoints
                .requestMatchers("/api/v1/system/**", "/los/api/v1/system/**").hasRole("SUPER_ADMIN")
                
                // Audit endpoints  
                .requestMatchers("/api/v1/audit/**", "/los/api/v1/audit/**").hasAnyRole("SUPER_ADMIN", "AUDITOR")
                
                // Бусад бүх web request-үүд authentication шаардана
                .anyRequest().authenticated()
            )
            
            // Headers тохиргоо
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.sameOrigin())
                .contentTypeOptions(contentTypeOptions -> {})
                .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                    .maxAgeInSeconds(31536000) // 1 жил  
                    .includeSubDomains(true)
                )
            )
            
            // Authentication provider нэмэх
            .authenticationProvider(authenticationProvider())
            
            // Form Login тохиргоо
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error=true")
                .usernameParameter("username")
                .passwordParameter("password")
                .permitAll()
            )
            
            // Logout тохиргоо
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            
            // Session Management
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .sessionFixation(sessionFixation -> sessionFixation.changeSessionId())
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
            )
            
            // ⭐ Exception Handling - API calls 401 авна, redirect биш ⭐
            .exceptionHandling(ex -> ex
                .accessDeniedPage("/access-denied")
                .authenticationEntryPoint((request, response, authException) -> {
                    String requestUri = request.getRequestURI();
                    // API calls-д 401 JSON response өгнө
                    if (requestUri.startsWith("/api/") || requestUri.startsWith("/los/api/")) {
                        response.setStatus(HttpStatus.UNAUTHORIZED.value());
                        response.setContentType("application/json");
                        response.setCharacterEncoding("UTF-8");
                        response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"Authentication required\",\"status\":401}");
                    } else {
                        // Web pages-г login хуудас руу чиглүүлнэ
                        response.sendRedirect("/login");
                    }
                })
            );

        log.info("Security filter chain амжилттай тохируулагдлаа");
        return http.build();
    }
}

/**
 * =====================================================================================
 * ⭐ БҮРЭН ХУВИЛБАР - AuthenticationManager DEPENDENCY ЗАСВАРЛАСАН ⭐
 * =====================================================================================
 * * ✅ Гол засварууд:
 * 1. Package хадгалагдсан: com.company.los.config
 * 2. AuthenticationManager bean нэмэгдсэн - AuthServiceImpl-д шаардлагатай
 * 3. DaoAuthenticationProvider bean нэмэгдсэн
 * 4. UserDetailsService dependency injection нэмэгдсэн
 * 5. JWT dependencies хэвээр байхгүй - энгийн form-based auth
 * 6. Headers configuration алдаа засварласан
 * 7. CORS тохиргоо CorsConfig.java файлаас ашиглагдана
 * 8. API endpoints зөвхөн /los prefix болон prefix-гүй хоёулаа дэмжинэ
 * 9. Development mode-д бүх API endpoints зөвшөөрөгдсөн
 * 10. Bean зөрчил засварласан
 * * 🔧 Нэмэлт beans:
 * - AuthenticationManager (AuthServiceImpl dependency)
 * - DaoAuthenticationProvider (UserDetailsService + PasswordEncoder)
 * - PasswordEncoder (BCrypt)
 * * 🧪 Тест:
 * - curl http://localhost:8080/los/api/v1/health -> 200 OK
 * - curl http://localhost:8080/api/v1/health -> 200 OK  
 * - curl http://localhost:8080/los/h2-console -> 200 OK
 * * 🌐 CORS тохиргоо: CorsConfig.java файлаас удирдагдана
 * * 🔑 Default хэрэглэгчид:
 * - admin / admin123 (SUPER_ADMIN role)
 * - loan_officer / loan123 (LOAN_OFFICER role)
 * - manager / manager123 (MANAGER role)
 * * ⚠️ АНХААРУУЛГА:
 * Production орчинд API endpoints-н authentication дахин идэвхжүүлэх шаардлагатай
 * * 📁 Файлын байршил: 
 * src/main/java/com/company/los/config/SecurityConfig.java
 * =====================================================================================
 */