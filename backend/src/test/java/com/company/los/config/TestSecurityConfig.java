package com.company.los.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * 🔐 TEST SECURITY CONFIGURATION - ЭЦСИЙН ЗАСВАРЛАСАН ХУВИЛБАР
 * 
 * ⭐ ЗАСВАРУУД:
 * ✅ Бүх endpoint-д хандахыг зөвшөөрнө
 * ✅ CSRF disable
 * ✅ Session stateless
 * ✅ H2 console зөвшөөрөх
 * ✅ Test profile-д зөвхөн ажиллана
 * 
 * @author LOS Development Team
 * @version 2.0 - FINAL TEST SECURITY FIX
 * @since 2025-08-03
 */
@TestConfiguration
@EnableWebSecurity
@Profile("test")
public class TestSecurityConfig {

    @Bean("testSecurityFilterChain")
    @Primary
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            // ⭐ CSRF бүрэн идэвхгүй болгох
            .csrf(csrf -> csrf.disable())
            
            // ⭐ Session stateless болгох
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // ⭐ Бүх хүсэлтийг зөвшөөрөх
            .authorizeHttpRequests(authz -> authz
                .requestMatchers(AntPathRequestMatcher.antMatcher("/h2-console/**")).permitAll()
                .requestMatchers(AntPathRequestMatcher.antMatcher("/api/**")).permitAll()
                .requestMatchers(AntPathRequestMatcher.antMatcher("/**")).permitAll()
                .anyRequest().permitAll()
            )
            
            // ⭐ H2 console-ын тулд frame options disable
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.disable())
                .contentTypeOptions(contentType -> contentType.disable())
            )
            
            // ⭐ HTTP Basic authentication disable
            .httpBasic(httpBasic -> httpBasic.disable())
            
            // ⭐ Form login disable
            .formLogin(formLogin -> formLogin.disable());
            
        return http.build();
    }
}