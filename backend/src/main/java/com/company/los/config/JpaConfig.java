package com.company.los.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * ⭐ JPA конфигураци - ЗАСВАРЛАСАН (Simplified) ⭐ 
 * JPA Configuration - зөвхөн AuditorAware bean
 * DatabaseConfig.java файлд @EnableJpaAuditing байгаа тул энд давхардуулахгүй
 * 
 * @author LOS Development Team
 * @version 2.0 - Fixed duplicate configuration issue
 * @since 2025-08-03
 */
@Configuration
public class JpaConfig {

    /**
     * ⭐ ЗАСВАРЛАСАН: Аудитын мэдээлэл олгогч ⭐
     * Auditor provider for JPA auditing
     */
    @Bean("auditorProvider")
    public AuditorAware<String> auditorProvider() {
        return new SpringSecurityAuditorAware();
    }

    /**
     * ⭐ ЗАСВАРЛАСАН: Аудитын мэдээлэл олгогчийн хэрэгжүүлэлт ⭐
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