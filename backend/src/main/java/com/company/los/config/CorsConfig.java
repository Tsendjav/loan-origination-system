package com.company.los.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Duration; // Added for Duration
import java.util.Arrays;
import java.util.List;

/**
 * CORS (Cross-Origin Resource Sharing) Configuration
 * * Frontend болон бусад domain-уудаас API руу хандах эрхийг тохируулна
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${app.security.cors.allowed-origins:http://localhost:3001,http://localhost:3000}")
    private String allowedOrigins;

    @Value("${app.security.cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS,PATCH}")
    private String allowedMethods;

    @Value("${app.security.cors.allowed-headers:*}")
    private String allowedHeaders;

    @Value("${app.security.cors.allow-credentials:true}")
    private boolean allowCredentials;

    @Value("${app.security.cors.max-age:3600}")
    private long maxAge; // Changed to long

    /**
     * Global CORS Configuration
     * Бүх API endpoint-д хамаарах CORS тохиргоо
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        List<String> methods = Arrays.asList(allowedMethods.split(","));
        List<String> headers = Arrays.asList(allowedHeaders.split(","));

        registry.addMapping("/api/**")
                .allowedOriginPatterns(origins.toArray(new String[0]))
                .allowedMethods(methods.toArray(new String[0]))
                .allowedHeaders(headers.toArray(new String[0]))
                .allowCredentials(allowCredentials)
                .maxAge(maxAge);

        // Actuator endpoints-д зориулсан CORS
        registry.addMapping("/actuator/**")
                .allowedOriginPatterns(origins.toArray(new String[0]))
                .allowedMethods("GET", "POST", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false)
                .maxAge(maxAge);

        // H2 Console-д зориулсан CORS (development-д зөвхөн)
        registry.addMapping("/h2-console/**")
                .allowedOriginPatterns("http://localhost:*")
                .allowedMethods("GET", "POST", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false)
                .maxAge(maxAge);

        // Swagger UI-д зориулсан CORS
        registry.addMapping("/swagger-ui/**")
                .allowedOriginPatterns(origins.toArray(new String[0]))
                .allowedMethods("GET", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false)
                .maxAge(maxAge);

        registry.addMapping("/v3/api-docs/**")
                .allowedOriginPatterns(origins.toArray(new String[0]))
                .allowedMethods("GET", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false)
                .maxAge(maxAge);
    }

    /**
     * CorsConfigurationSource Bean
     * Spring Security-тэй хамтад ажиллах CORS тохиргоо
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Allowed origins
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        configuration.setAllowedOriginPatterns(origins);

        // Allowed methods
        List<String> methods = Arrays.asList(allowedMethods.split(","));
        configuration.setAllowedMethods(methods);

        // Allowed headers
        if ("*".equals(allowedHeaders)) {
            configuration.addAllowedHeader("*");
        } else {
            List<String> headers = Arrays.asList(allowedHeaders.split(","));
            configuration.setAllowedHeaders(headers);
        }

        // Exposed headers (Frontend-ээс уншиж болох headers)
        configuration.setExposedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Total-Count",
                "X-Page-Number",
                "X-Page-Size",
                "Location"
        ));

        // Allow credentials
        configuration.setAllowCredentials(allowCredentials);

        // Max age
        configuration.setMaxAge(Duration.ofSeconds(maxAge)); // Changed to use Duration.ofSeconds()

        // Apply to all paths
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    /**
     * Production-specific CORS Configuration
     * Production орчинд илүү хатуу CORS тохиргоо
     */
    @Bean
    public CorsConfigurationSource productionCorsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Production-д зөвхөн шаардлагатай domain-уудыг зөвшөөрнө
        configuration.setAllowedOriginPatterns(Arrays.asList(
                "https://*.company.com",
                "https://los.company.com",
                "https://app.company.com"
        ));

        // Limited methods
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "OPTIONS"
        ));

        // Specific headers only
        configuration.setAllowedHeaders(Arrays.asList(
                "Content-Type",
                "Authorization",
                "X-Requested-With",
                "Accept",
                "Origin",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
        ));

        configuration.setExposedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Total-Count"
        ));

        configuration.setAllowCredentials(true);
        configuration.setMaxAge(Duration.ofSeconds(1800)); // Changed to use Duration.ofSeconds()

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);

        return source;
    }

    /**
     * Development-specific CORS Configuration
     * Development орчинд илүү чөлөөтэй CORS тохиргоо
     */
    @Bean
    public CorsConfigurationSource developmentCorsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Development-д бүх localhost-ыг зөвшөөрнө
        configuration.setAllowedOriginPatterns(Arrays.asList(
                "http://localhost:*",
                "http://127.0.0.1:*",
                "http://0.0.0.0:*"
        ));

        // All methods
        configuration.addAllowedMethod("*");

        // All headers
        configuration.addAllowedHeader("*");

        // Expose all headers
        configuration.addExposedHeader("*");

        configuration.setAllowCredentials(true);
        configuration.setMaxAge(Duration.ofSeconds(3600)); // Changed to use Duration.ofSeconds()

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}

/**
 * ⭐ CORS ТОХИРГООНЫ ТАЙЛБАР ⭐
 * * 1. AllowedOrigins: Хэдийн domain-уудаас хандах эрхтэй
 * 2. AllowedMethods: Ямар HTTP методууд ашиглаж болох
 * 3. AllowedHeaders: Ямар header-ууд илгээж болох
 * 4. ExposedHeaders: Frontend-ээс уншиж болох response header-ууд
 * 5. AllowCredentials: Cookie, Authorization header дамжуулах эрх
 * 6. MaxAge: Preflight request-ийн cache хугацаа
 * * Development орчинд: Бүх localhost зөвшөөрөгдсөн
 * Production орчинд: Зөвхөн company domain-ууд зөвшөөрөгдсөн
 */