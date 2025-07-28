package com.company.los.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

/**
 * Swagger/OpenAPI 3 configuration for the Loan Origination System API documentation
 * 
 * @author LOS Development Team
 * @version 1.0
 */
@Configuration
public class SwaggerConfig {

    @Value("${app.name:LOS API}")
    private String appName;

    @Value("${app.version:1.0.0}")
    private String appVersion;

    @Value("${app.description:Loan Origination System API}")
    private String appDescription;

    @Value("${server.servlet.context-path:/los}")
    private String contextPath;

    @Value("${server.port:8080}")
    private String serverPort;

    /**
     * Configure OpenAPI 3 documentation
     * 
     * @return OpenAPI configuration
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(createApiInfo())
                .servers(createServers())
                .tags(createTags())
                .components(createComponents())
                .addSecurityItem(createSecurityRequirement());
    }

    /**
     * Create API information
     * 
     * @return API Info object
     */
    private Info createApiInfo() {
        return new Info()
                .title(appName + " API Documentation")
                .version(appVersion)
                .description(createApiDescription())
                .contact(createContact())
                .license(createLicense());
    }

    /**
     * Create detailed API description
     * 
     * @return API description
     */
    private String createApiDescription() {
        return """
                # Loan Origination System (LOS) API
                
                Welcome to the LOS API documentation. This system provides comprehensive 
                loan processing capabilities including:
                
                ## Features
                - **Customer Management**: Complete customer lifecycle management
                - **Loan Applications**: End-to-end loan application processing
                - **Document Management**: Secure document upload and verification
                - **Risk Assessment**: Automated risk evaluation and scoring
                - **Workflow Management**: Configurable approval workflows
                - **Reporting**: Comprehensive reporting and analytics
                
                ## Authentication
                This API uses JWT (JSON Web Token) for authentication. Include the token 
                in the Authorization header as: `Bearer <your-jwt-token>`
                
                ## Rate Limiting
                API calls are rate-limited to ensure fair usage and system stability.
                
                ## Error Handling
                The API returns standard HTTP status codes and detailed error messages 
                in JSON format for all operations.
                
                ## Support
                For technical support, please contact the development team at 
                los-dev-team@company.com
                """;
    }

    /**
     * Create contact information
     * 
     * @return Contact object
     */
    private Contact createContact() {
        return new Contact()
                .name("LOS Development Team")
                .email("los-dev-team@company.com")
                .url("https://company.com/los");
    }

    /**
     * Create license information
     * 
     * @return License object
     */
    private License createLicense() {
        return new License()
                .name("Proprietary License")
                .url("https://company.com/license");
    }

    /**
     * Create server configurations
     * 
     * @return List of servers
     */
    private List<Server> createServers() {
        return Arrays.asList(
                new Server()
                        .url("http://localhost:" + serverPort + contextPath)
                        .description("Local Development Server"),
                new Server()
                        .url("https://los-api-dev.company.com" + contextPath)
                        .description("Development Server"),
                new Server()
                        .url("https://los-api-staging.company.com" + contextPath)
                        .description("Staging Server"),
                new Server()
                        .url("https://los-api.company.com" + contextPath)
                        .description("Production Server")
        );
    }

    /**
     * Create API tags for organization
     * 
     * @return List of tags
     */
    private List<Tag> createTags() {
        return Arrays.asList(
                new Tag()
                        .name("Authentication")
                        .description("User authentication and authorization operations"),
                new Tag()
                        .name("Customers")
                        .description("Customer management operations"),
                new Tag()
                        .name("Loan Applications")
                        .description("Loan application processing operations"),
                new Tag()
                        .name("Loan Products")
                        .description("Loan product configuration operations"),
                new Tag()
                        .name("Documents")
                        .description("Document management operations"),
                new Tag()
                        .name("Users")
                        .description("User management operations"),
                new Tag()
                        .name("Roles & Permissions")
                        .description("Role and permission management operations"),
                new Tag()
                        .name("System")
                        .description("System configuration and health operations"),
                new Tag()
                        .name("Audit")
                        .description("Audit log and monitoring operations"),
                new Tag()
                        .name("Reports")
                        .description("Reporting and analytics operations")
        );
    }

    /**
     * Create security components
     * 
     * @return Components with security schemes
     */
    private Components createComponents() {
        return new Components()
                .addSecuritySchemes("bearerAuth", createJwtSecurityScheme())
                .addSecuritySchemes("basicAuth", createBasicSecurityScheme());
    }

    /**
     * Create JWT security scheme
     * 
     * @return JWT SecurityScheme
     */
    private SecurityScheme createJwtSecurityScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("JWT token for API authentication");
    }

    /**
     * Create Basic Auth security scheme
     * 
     * @return Basic Auth SecurityScheme
     */
    private SecurityScheme createBasicSecurityScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("basic")
                .description("Basic authentication for admin operations");
    }

    /**
     * Create security requirement
     * 
     * @return SecurityRequirement
     */
    private SecurityRequirement createSecurityRequirement() {
        return new SecurityRequirement()
                .addList("bearerAuth");
    }
}