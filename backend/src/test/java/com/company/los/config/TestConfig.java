package com.company.los.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.sql.DataSource;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import java.util.Set;
import java.io.File;

/**
 * ⭐ MAIN TEST CONFIGURATION - ЗАСВАРЛАСАН ⭐
 * * Central test configuration for all test types:
 * - Unit tests
 * - Integration tests  
 * - Controller tests
 * * @author LOS Development Team
 * @version 2.0 - Fixed ApplicationContext loading issues
 * @since 2025-08-03
 */
@TestConfiguration
@Profile("test")
public class TestConfig {

    /**
     * ⭐ ЗАСВАРЛАСАН: H2 In-Memory Database for tests ⭐
     * DatabaseConfig-тэй конфликт арилгах зорилгоор зөвхөн test profile-д ажиллана
     */
    @Bean("dataSource")
    @Primary
    public DataSource testDataSource() {
        try {
            return new EmbeddedDatabaseBuilder()
                    .setType(EmbeddedDatabaseType.H2)
                    .setName("testdb")
                    .addScript("classpath:schema.sql")
                    .addScript("classpath:test-data.sql")
                    .build();
        } catch (Exception e) {
            // ⭐ FALLBACK: Script файлууд байхгүй бол зөвхөн H2 database үүсгэнэ ⭐
            System.err.println("⚠️ Warning: Could not load SQL scripts, creating empty database: " + e.getMessage());
            return new EmbeddedDatabaseBuilder()
                    .setType(EmbeddedDatabaseType.H2)
                    .setName("testdb")
                    .build();
        }
    }

    /**
     * Simple in-memory cache manager for tests
     */
    @Bean
    @Primary
    public CacheManager testCacheManager() {
        return new ConcurrentMapCacheManager(
            "customers", 
            "loan-applications", 
            "documents", 
            "userCache", 
            "authCache"
        );
    }

    /**
     * Password encoder for tests (lower strength for faster tests)
     */
    @Bean
    @Primary
    public PasswordEncoder testPasswordEncoder() {
        return new BCryptPasswordEncoder(4);
    }

    /**
     * ⭐ ЗАСВАРЛАСАН: Document storage configuration for tests ⭐
     */
    @Bean
    @Primary
    public DocumentStorageConfig testDocumentStorageConfig() {
        DocumentStorageConfig config = new DocumentStorageConfig();
        
        try {
            // Create temp directory for test uploads
            String tempDir = System.getProperty("java.io.tmpdir") + File.separator + "test-uploads";
            File uploadDir = new File(tempDir);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }
            
            config.setUploadDir(tempDir);
            config.setMaxFileSize(10 * 1024 * 1024L); // 10MB
            config.setAllowedTypes(Set.of("pdf", "jpg", "jpeg", "png", "doc", "docx"));
            
            System.out.println("✅ Test DocumentStorageConfig configured: " + tempDir);
            
        } catch (Exception e) {
            System.err.println("⚠️ Warning: Could not setup document storage: " + e.getMessage());
            // ⭐ FALLBACK: Default тохиргоо ⭐
            config.setUploadDir("./test-uploads");
            config.setMaxFileSize(10 * 1024 * 1024L);
            config.setAllowedTypes(Set.of("pdf", "jpg", "jpeg", "png"));
        }
        
        return config;
    }
}