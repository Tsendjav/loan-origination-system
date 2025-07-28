package com.company.los.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * JPA конфигураци
 * JPA Configuration
 * 
 * JPA Auditing-д зориулсан AuditorAware provider
 * DatabaseConfig.java файлаас @EnableJpaAuditing ашиглагдана
 */
@Configuration
public class JpaConfig {

    /**
     * Аудитын мэдээлэл олгогч
     * Auditor provider for JPA auditing
     * DatabaseConfig.java файлд auditorAwareRef = "auditorProvider" гэж заасан
     */
    @Bean
    public AuditorAware<String> auditorProvider() {
        return new SpringSecurityAuditorAware();
    }

    /**
     * Аудитын мэдээлэл олгогчийн хэрэгжүүлэлт
     * Implementation of AuditorAware that uses Spring Security (with fallback)
     */
    public static class SpringSecurityAuditorAware implements AuditorAware<String> {

        @Override
        public Optional<String> getCurrentAuditor() {
            try {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                
                if (authentication == null || !authentication.isAuthenticated() 
                    || "anonymousUser".equals(authentication.getPrincipal())) {
                    return Optional.of("system");
                }
                
                return Optional.of(authentication.getName());
            } catch (Exception e) {
                // Fallback to system user if Spring Security is not available
                return Optional.of("system");
            }
        }
    }
}