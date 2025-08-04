package com.company.los.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * JWT Authentication Entry Point
 * Баталгаажуулалтын алдаа гарах үед хариулт өгөх
 * ⭐ ЗАСВАРЛАСАН - log variable алдаа шийдэгдсэн ⭐
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationEntryPoint.class);

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