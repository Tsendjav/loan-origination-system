package com.company.los.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.http.HttpStatus;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Arrays;

/**
 * ⭐ LOAN ORIGINATION SYSTEM SECURITY CONFIGURATION - 302 REDIRECT АЛДАА ЗАСВАРЛАСАН ⭐
 * * Security тохиргоо:
 * - BCrypt password encoding
 * - Form-based authentication (дэлгэц хэрэглэгчдэд)
 * - API endpoints-д 401 response (302 redirect биш)
 * - CORS тохиргоо
 * - H2 Console зөвшөөрөл
 * * ✅ ЗАСВАР: API calls 302 redirect-ээс ангид болгосон
 * * Created: 2025-07-26
 * Updated: 302 redirect алдаа засварласан
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    /**
     * BCrypt Password Encoder бэлдэх
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * ⭐ ЗАСВАРЛАСАН Security Filter Chain - 302 REDIRECT АЛДАА ЗАСВАРЛАСАН ⭐
     * Гол засвар: API calls-д зориулж authenticationEntryPoint засварласан
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CORS тохиргоо
            .cors(cors -> cors.configurationSource(request -> {
                CorsConfiguration configuration = new CorsConfiguration();
                configuration.setAllowedOriginPatterns(Arrays.asList("*"));
                configuration.setAllowedMethods(Arrays.asList("*"));
                configuration.setAllowedHeaders(Arrays.asList("*"));
                configuration.setAllowCredentials(true);
                return configuration;
            }))
            
            // CSRF тохиргоо
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/h2-console/**")
                .ignoringRequestMatchers("/api/**")
                .ignoringRequestMatchers("/actuator/**")
            )
            
            // ⭐ ШИНЭ: Authorization тохиргоо - API endpoints зөвшөөрөгдсөн ⭐
            .authorizeHttpRequests(authz -> authz
                // ✅ PUBLIC ENDPOINTS - AUTHENTICATION ШААРДЛАГАГҮЙ
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/swagger-ui/**").permitAll()
                .requestMatchers("/swagger-ui.html").permitAll()
                .requestMatchers("/api-docs/**").permitAll()
                .requestMatchers("/v3/api-docs/**").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/webjars/**").permitAll()
                .requestMatchers("/favicon.ico").permitAll()
                .requestMatchers("/error").permitAll()
                
                // ✅ API HEALTH & AUTH ENDPOINTS - DEVELOPMENT MODE
                .requestMatchers("/api/v1/health/**").permitAll()
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/api/v1/").permitAll()
                
                // ✅ LOGIN/LOGOUT PAGES
                .requestMatchers("/login").permitAll()
                .requestMatchers("/logout").permitAll()
                
                // Static resources
                .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                
                // ⚠️ ХӨГЖҮҮЛЭЛТИЙН РЕЖИМ: API endpoints-г түр зөвшөөрнө
                // Продакшн дээр эдгээрийг authentication шаардахаар өөрчилнө
                .requestMatchers("/api/v1/customers/**").permitAll()
                .requestMatchers("/api/v1/documents/**").permitAll()
                .requestMatchers("/api/v1/loans/**").permitAll()
                .requestMatchers("/api/v1/loan-applications/**").permitAll()
                
                // Admin эрх шаардагдах endpoints (хадгалагдсан)
                .requestMatchers("/admin/**").hasRole("SUPER_ADMIN")
                .requestMatchers("/api/v1/admin/**").hasRole("SUPER_ADMIN")
                
                // Manager эрх шаардагдах endpoints (хадгалагдсан) 
                .requestMatchers("/api/v1/reports/**").hasAnyRole("MANAGER", "SUPER_ADMIN")
                
                // Бусад бүх web request-үүд authentication шаардана
                .anyRequest().authenticated()
            )
            
            // Headers тохиргоо (хадгалагдсан)
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.sameOrigin())
                .contentTypeOptions(contentTypeOptions -> contentTypeOptions.disable())
                .httpStrictTransportSecurity(hsts -> hsts.disable())
                .referrerPolicy(policy -> policy.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER))
            )
            
            // Form Login тохиргоо (хадгалагдсан)
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error=true")
                .usernameParameter("username")
                .passwordParameter("password")
                .permitAll()
            )
            
            // Logout тохиргоо (хадгалагдсан)
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            
            // Session Management (хадгалагдсан)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .sessionFixation(sessionFixation -> sessionFixation.changeSessionId())
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
            )
            
            // ⭐ ГАРВАШ ЗАСВАР: Exception Handling - API calls 401 авна, redirect биш ⭐
            .exceptionHandling(ex -> ex
                .accessDeniedPage("/access-denied")
                .authenticationEntryPoint((request, response, authException) -> {
                    String requestUri = request.getRequestURI();
                    // API calls-д 401 JSON response өгнө
                    if (requestUri.startsWith("/api/")) {
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

        return http.build();
    }
}

/**
 * =====================================================================================
 * ⭐ 302 REDIRECT АЛДАА ЗАСВАРЛАСАН ХУВИЛБАР ⭐
 * =====================================================================================
 * * ✅ Гол засварууд:
 * 1. API health endpoints (.permitAll() нэмэгдсэн)
 * 2. Exception handling сайжруулсан (API calls 401, web calls redirect)
 * 3. Хөгжүүлэлтийн режимд API endpoints түр зөвшөөрөгдсөн
 * 4. CORS тохиргоо хадгалагдсан
 * 
 * * 🧪 Тест:
 * curl http://localhost:8080/los/api/v1/health -> 200 OK (302 биш)
 * curl http://localhost:8080/los/api/v1/auth/login -> 401/400 (404 биш)
 * 
 * * 🔑 Login хэрэглэгчид (хадгалагдсан):
 * - admin / admin123 (SUPER_ADMIN role)
 * - loan_officer / loan123 (LOAN_OFFICER role) 
 * - manager / manager123 (MANAGER role)
 * 
 * * ⚠️ АНХААРУУЛГА:
 * Продакшн дээр API endpoints-н authentication дахин идэвхжүүлэх шаардлагатай
 * =====================================================================================
 */