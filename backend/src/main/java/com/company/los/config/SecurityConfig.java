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
import org.springframework.web.cors.CorsConfiguration;

import java.util.Arrays;

/**
 * ⭐ LOAN ORIGINATION SYSTEM SECURITY CONFIGURATION - ЭЦСИЙН ХУВИЛБАР (ЗАСВАРЛАСАН) ⭐
 * * Security тохиргоо:
 * - BCrypt password encoding
 * - Form-based authentication
 * - CORS тохиргоо
 * - H2 Console зөвшөөрөл
 * - API endpoints тохиргоо
 * * Created: 2025-07-26
 * Updated: Spring Security 6.x compatible + алдаанууд засварласан + deprecation warning засвар
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    /**
     * BCrypt Password Encoder бэлдэх
     * - 10 rounds (default)
     * - admin123, loan123, manager123 password-тай тохирно
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // CORS тохиргоо CorsConfig класст байгаа тул энд тусдаа бичихгүй

    /**
     * Security Filter Chain тохиргоо
     * - Form login with admin/admin123
     * - H2 Console зөвшөөрөл
     * - API endpoints authorization
     * - CSRF protection
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CORS тохиргоо - CorsConfig класст тохируулсан байгаа
            .cors(cors -> cors.configurationSource(request -> {
                CorsConfiguration configuration = new CorsConfiguration();
                configuration.setAllowedOriginPatterns(Arrays.asList("*"));
                configuration.setAllowedMethods(Arrays.asList("*"));
                configuration.setAllowedHeaders(Arrays.asList("*"));
                configuration.setAllowCredentials(true);
                return configuration;
            }))
            
            // CSRF тохиргоо - H2 Console болон API-д зориулж disable хийнэ
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/h2-console/**")
                .ignoringRequestMatchers("/api/**")
                .ignoringRequestMatchers("/actuator/**")
            )
            
            // Authorization тохиргоо
            .authorizeHttpRequests(authz -> authz
                // Public endpoints
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/swagger-ui/**").permitAll()
                .requestMatchers("/swagger-ui.html").permitAll()
                .requestMatchers("/api-docs/**").permitAll()
                .requestMatchers("/v3/api-docs/**").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/webjars/**").permitAll()
                .requestMatchers("/favicon.ico").permitAll()
                .requestMatchers("/error").permitAll()
                
                // Authentication endpoints
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/login").permitAll()
                .requestMatchers("/logout").permitAll()
                
                // Static resources
                .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                
                // Admin эрх шаардагдах endpoints
                .requestMatchers("/admin/**").hasRole("SUPER_ADMIN")
                .requestMatchers("/api/v1/admin/**").hasRole("SUPER_ADMIN")
                
                // Manager эрх шаардагдах endpoints  
                .requestMatchers("/api/v1/reports/**").hasAnyRole("MANAGER", "SUPER_ADMIN")
                
                // Loan Officer эрх шаардагдах endpoints
                .requestMatchers("/api/v1/loans/**").hasAnyRole("LOAN_OFFICER", "MANAGER", "SUPER_ADMIN")
                .requestMatchers("/api/v1/customers/**").hasAnyRole("LOAN_OFFICER", "MANAGER", "SUPER_ADMIN")
                .requestMatchers("/api/v1/documents/**").hasAnyRole("LOAN_OFFICER", "DOCUMENT_REVIEWER", "MANAGER", "SUPER_ADMIN")
                
                // Бусад бүх request-үүд authentication шаардана
                .anyRequest().authenticated()
            )
            
            // Headers тохиргоо - H2 Console-д зориулж (Spring Security 6.x compatible)
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.sameOrigin()) // H2 Console iframe-д зориулсан
                .contentTypeOptions(contentTypeOptions -> contentTypeOptions.disable())
                // HSTS-г хөгжүүлэлтийн үед disable хийлээ
                .httpStrictTransportSecurity(hsts -> hsts.disable())
                // ⭐ ЗАСВАР: ReferrerPolicy deprecation warning засвар ⭐
                .referrerPolicy(policy -> policy.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER))
            )
            
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
            
            // Exception Handling
            .exceptionHandling(ex -> ex
                .accessDeniedPage("/access-denied")
                .authenticationEntryPoint((request, response, authException) -> {
                    String requestUri = request.getRequestURI();
                    if (requestUri.startsWith("/api/")) {
                        response.setStatus(401);
                        response.setContentType("application/json");
                        response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"Authentication required\"}");
                    } else {
                        response.sendRedirect("/login");
                    }
                })
            );

        return http.build();
    }
}

/**
 * =====================================================================================
 * ЗАСВАРЛАСАН ХУВИЛБАР - SPRING SECURITY 6.x COMPATIBLE + DEPRECATION WARNING ЗАСВАР
 * =====================================================================================
 * * ✅ Засварууд:
 * - hasRole("ADMIN") -> hasRole("SUPER_ADMIN") (data.sql-тэй тохирно)
 * - setAllowedOriginPatterns ашиглана (Spring Security 6.x-д илүү тохиромжтой)
 * - Exception handling сайжруулсан
 * - ⭐ ReferrerPolicy deprecation warning засвар ⭐
 * * 🔑 Login хэрэглэгчид:
 * - admin / admin123 (SUPER_ADMIN role)
 * - loan_officer / loan123 (LOAN_OFFICER role)
 * - manager / manager123 (MANAGER role)
 * * 🌐 URLs:
 * - Login: http://localhost:8080/los/login
 * - H2 Console: http://localhost:8080/los/h2-console
 * - Dashboard: http://localhost:8080/los/dashboard
 * * 🔒 Security roles:
 * - SUPER_ADMIN: Бүх системийн эрх
 * - MANAGER: Менежерийн эрх (зөвшөөрөх, татгалзах)
 * - LOAN_OFFICER: Зээлийн үйл ажиллагааны эрх
 * - DOCUMENT_REVIEWER: Баримт хянах эрх
 * * =====================================================================================
 */