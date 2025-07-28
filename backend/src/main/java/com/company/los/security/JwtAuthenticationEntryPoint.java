package com.company.los.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * JWT Authentication Entry Point
 * Баталгаажуулалтын алдаа гарах үед хариулт өгөх
 */
@Slf4j
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, 
                        HttpServletResponse response,
                        AuthenticationException authException) throws IOException {
        
        log.error("Unauthorized error: {}", authException.getMessage());
        
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding("UTF-8");
        
        String jsonPayload = """
            {
                "success": false,
                "error": "Unauthorized",
                "message": "Та нэвтрэх шаардлагатай байна",
                "timestamp": "%s",
                "path": "%s"
            }
            """.formatted(
                java.time.LocalDateTime.now().toString(),
                request.getRequestURI()
            );
        
        response.getWriter().write(jsonPayload);
    }
}