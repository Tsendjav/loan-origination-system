package com.company.los.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Properties;

/**
 * Database configuration for the Loan Origination System
 * Configures data source, transaction management, and JPA settings
 * @author LOS Development Team
 * @version 1.0
 */
@Configuration
@EnableJpaRepositories(
    basePackages = "com.company.los.repository",
    entityManagerFactoryRef = "entityManagerFactory",
    transactionManagerRef = "transactionManager"
)
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
@EnableTransactionManagement
public class DatabaseConfig {

    @Value("${spring.datasource.url:jdbc:h2:mem:losdb}")
    private String jdbcUrl;

    @Value("${spring.datasource.username:sa}")
    private String username;

    @Value("${spring.datasource.password:}")
    private String password;

    @Value("${spring.datasource.driverClassName:org.h2.Driver}")
    private String driverClassName;

    @Value("${spring.datasource.hikari.maximum-pool-size:20}")
    private int maximumPoolSize;

    @Value("${spring.datasource.hikari.minimum-idle:5}")
    private int minimumIdle;

    @Value("${spring.datasource.hikari.connection-timeout:30000}")
    private long connectionTimeout;

    @Value("${spring.datasource.hikari.idle-timeout:600000}")
    private long idleTimeout;

    @Value("${spring.datasource.hikari.max-lifetime:1800000}")
    private long maxLifetime;

    @Value("${spring.datasource.hikari.leak-detection-threshold:60000}")
    private long leakDetectionThreshold;

    @Value("${spring.jpa.hibernate.ddl-auto:create-drop}")
    private String ddlAuto;

    @Value("${spring.jpa.show-sql:true}")
    private boolean showSql;

    @Value("${spring.jpa.properties.hibernate.format_sql:true}")
    private boolean formatSql;

    @Value("${spring.jpa.properties.hibernate.jdbc.batch_size:20}")
    private int batchSize;

    /**
     * Configure HikariCP data source for optimal performance
     * @return Configured DataSource
     */
    @Bean
    @Primary
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        
        // Basic connection settings
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName(driverClassName);
        
        // Pool configuration
        config.setMaximumPoolSize(maximumPoolSize);
        config.setMinimumIdle(minimumIdle);
        config.setConnectionTimeout(connectionTimeout);
        config.setIdleTimeout(idleTimeout);
        config.setMaxLifetime(maxLifetime);
        config.setLeakDetectionThreshold(leakDetectionThreshold);
        
        // Connection pool name for monitoring
        config.setPoolName("LOS-HikariCP");
        
        // Connection test query based on database type
        if (jdbcUrl.contains("postgresql")) {
            config.setConnectionTestQuery("SELECT 1");
        } else if (jdbcUrl.contains("mysql")) {
            config.setConnectionTestQuery("SELECT 1");
        } else if (jdbcUrl.contains("h2")) {
            config.setConnectionTestQuery("SELECT 1");
        }
        
        // Performance optimizations
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        
        // Security settings
        config.addDataSourceProperty("useSSL", "false");
        config.addDataSourceProperty("requireSSL", "false");
        
        return new HikariDataSource(config);
    }

    /**
     * Configure JPA Entity Manager Factory
     * @param dataSource The data source
     * @return Entity Manager Factory
     */
    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.company.los.entity");
        
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        
        em.setJpaProperties(hibernateProperties());
        
        return em;
    }

    /**
     * Configure Hibernate properties
     * @return Hibernate properties
     */
    private Properties hibernateProperties() {
        Properties properties = new Properties();
        
        // Database dialect based on URL
        if (jdbcUrl.contains("postgresql")) {
            properties.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        } else if (jdbcUrl.contains("mysql")) {
            properties.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");
        } else if (jdbcUrl.contains("h2")) {
            properties.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        }
        
        // Schema management
        properties.setProperty("hibernate.hbm2ddl.auto", ddlAuto);
        
        // SQL logging
        properties.setProperty("hibernate.show_sql", String.valueOf(showSql));
        properties.setProperty("hibernate.format_sql", String.valueOf(formatSql));
        properties.setProperty("hibernate.use_sql_comments", String.valueOf(formatSql));
        
        // Performance optimizations
        properties.setProperty("hibernate.jdbc.batch_size", String.valueOf(batchSize));
        properties.setProperty("hibernate.order_inserts", "true");
        properties.setProperty("hibernate.order_updates", "true");
        properties.setProperty("hibernate.jdbc.batch_versioned_data", "true");
        
        // Second-level cache configuration - DISABLED for development
        properties.setProperty("hibernate.cache.use_second_level_cache", "false");
        properties.setProperty("hibernate.cache.use_query_cache", "false");
        
        // Connection handling
        properties.setProperty("hibernate.connection.provider_disables_autocommit", "true");
        
        // Naming strategy
        properties.setProperty("hibernate.physical_naming_strategy", 
                              "org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl");
        
        // Statistics (enable in development/staging)
        if (!"prod".equals(System.getProperty("spring.profiles.active"))) {
            properties.setProperty("hibernate.generate_statistics", "true");
        }
        
        return properties;
    }

    /**
     * Configure JPA Transaction Manager
     * @param entityManagerFactory The entity manager factory
     * @return Transaction Manager
     */
    @Bean
    @Primary
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory);
        return transactionManager;
    }

    /**
     * Configure JDBC Transaction Manager for non-JPA operations
     * @param dataSource The data source
     * @return JDBC Transaction Manager
     */
    @Bean
    public DataSourceTransactionManager jdbcTransactionManager(DataSource dataSource) {
        DataSourceTransactionManager transactionManager = new DataSourceTransactionManager();
        transactionManager.setDataSource(dataSource);
        return transactionManager;
    }

    /**
     * Configure JDBC Template for custom queries
     * @param dataSource The data source
     * @return JDBC Template
     */
    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.setQueryTimeout(30); // 30 seconds timeout
        return jdbcTemplate;
    }

    /**
     * Development-specific data source configuration for H2
     */
    @Configuration
    @Profile("dev")
    static class DevelopmentDatabaseConfig {
        
        @Bean
        @ConditionalOnProperty(name = "app.dev.data-loader.enabled", havingValue = "true")
        public DataLoader dataLoader() {
            return new DataLoader();
        }
        
        /**
         * Load sample data for development
         */
        static class DataLoader {
            // This would contain logic to load sample data
            // Implementation would depend on specific requirements
        }
    }

    /**
     * Production-specific database configuration
     */
    @Configuration
    @Profile("prod")
    static class ProductionDatabaseConfig {
        
        // Additional production-specific beans can be defined here
        // Such as connection pool monitoring, backup configurations, etc.
    }
}