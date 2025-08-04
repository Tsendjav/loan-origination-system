package com.company.los.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * JWT Access Denied Handler
 * Эрх хангалтгүй үед хариулт өгөх
 * ⭐ ЗАСВАРЛАСАН - LOGGER АЛДАА ШИЙДЭГДСЭН ⭐
 */
@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    // ⭐ ЗАСВАР: @Slf4j annotation-ийн оронд шууд Logger ашиглах ⭐
    private static final Logger log = LoggerFactory.getLogger(JwtAccessDeniedHandler.class);

    @Override
    public void handle(HttpServletRequest request,
                      HttpServletResponse response,
                      AccessDeniedException accessDeniedException) throws IOException {

        log.error("Access denied error: {}", accessDeniedException.getMessage());

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setCharacterEncoding("UTF-8");

        String jsonPayload = """
            {
                "success": false,
                "error": "Access Denied",
                "message": "Энэ үйлдэл хийхэд танд эрх хүрэхгүй байна",
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