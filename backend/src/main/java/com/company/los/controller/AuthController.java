package com.company.los.controller;

import com.company.los.dto.AuthResponseDto;
import com.company.los.dto.LoginRequestDto;
import com.company.los.entity.User;
import com.company.los.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // Энэ аннотацийг нэмсэн
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Authentication Controller
 * Нэвтрэлтийн API endpoints
 * * @author LOS Development Team
 * @version 2.1 - Fixed Compilation Errors
 * @since 2025-07-28
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Нэвтрэлтийн API")
@CrossOrigin(origins = {"http://localhost:3001", "http://localhost:3000"})
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Системд нэвтрэх", description = "Хэрэглэгчийн нэр болон нууц үгээр нэвтрэх")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDto loginRequest) {
        try {
            log.info("Нэвтрэх оролдлого: {}", loginRequest.getUsername());
            
            // Authentication service-ээр нэвтрэх
            AuthResponseDto response = authService.login(loginRequest);
            
            if (response.isSuccess()) {
                log.info("Амжилттай нэвтэрлэв: {}", loginRequest.getUsername());
                return ResponseEntity.ok(response);
            } else {
                log.warn("Нэвтрэх амжилтгүй: {} - {}", loginRequest.getUsername(), response.getMessage());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
        } catch (Exception e) {
            log.error("Нэвтрэх алдаа: {} - {}", loginRequest.getUsername(), e.getMessage());
            
            Map<String, String> error = new HashMap<>();
            error.put("error", "Нэвтрэх алдаа");
            error.put("message", "Хэрэглэгчийн нэр эсвэл нууц үг буруу байна");
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "Системээс гарах")
    public ResponseEntity<?> logout(@RequestHeader(value = "Authorization", required = false) String token) {
        try {
            log.info("Системээс гарах");
            
            if (token != null && token.startsWith("Bearer ")) {
                String jwtToken = token.substring(7);
                authService.logoutUser(jwtToken);
            }
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Амжилттай гарлаа");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Гарах алдаа: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Системээс гарахад алдаа гарлаа"));
        }
    }

    @GetMapping("/me")
    @Operation(summary = "Одоогийн хэрэглэгчийн мэдээлэл")
    public ResponseEntity<?> getCurrentUser(@RequestHeader(value = "Authorization", required = false) String token) {
        try {
            if (token == null || !token.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Token байхгүй"));
            }

            String jwtToken = token.substring(7);
            Optional<User> userOpt = authService.getCurrentUser(jwtToken);
            
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", user.getId());
                userInfo.put("username", user.getUsername());
                userInfo.put("email", user.getEmail());
                userInfo.put("fullName", user.getFirstName() + " " + user.getLastName());
                userInfo.put("roles", user.getRoles());
                userInfo.put("isActive", user.getIsActive());
                userInfo.put("lastLoginAt", user.getLastLoginAt());
                
                return ResponseEntity.ok(userInfo);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Хэрэглэгчийн мэдээлэл олдсонгүй эсвэл token хүчингүй"));
            }
            
        } catch (Exception e) {
            log.error("Хэрэглэгчийн мэдээлэл авах алдаа: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Хэрэглэгчийн мэдээлэл олдсонгүй"));
        }
    }

    @GetMapping("/test")
    @Operation(summary = "Authentication тест")
    public ResponseEntity<?> test() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "Authentication Controller ажиллаж байна");
        response.put("timestamp", System.currentTimeMillis());
        response.put("availableEndpoints", new String[]{
            "POST /api/v1/auth/login",
            "POST /api/v1/auth/logout", 
            "GET /api/v1/auth/me",
            "POST /api/v1/auth/refresh",
            "GET /api/v1/auth/validate",
            "GET /api/v1/auth/test"
        });
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Token сэргээх", description = "Refresh token ашиглан шинэ access token авах")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
        try {
            String refreshToken = request.get("refreshToken");
            if (refreshToken == null || refreshToken.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Refresh token шаардлагатай"));
            }

            AuthResponseDto response = authService.refreshToken(refreshToken);

            if (response.isSuccess()) {
                log.info("Token амжилттай сэргээгдлэв");
                return ResponseEntity.ok(response);
            } else {
                log.warn("Token сэргээх амжилтгүй: {}", response.getMessage());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
        } catch (Exception e) {
            log.error("Refresh token алдаа: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Token сэргээхэд алдаа гарлаа"));
        }
    }

    @GetMapping("/validate")
    @Operation(summary = "Token баталгаажуулах", description = "Access token-ийг баталгаажуулах")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestHeader(value = "Authorization", required = false) String token) {
        try {
            Map<String, Object> response = new HashMap<>();
            
            if (token == null) {
                response.put("valid", false);
                response.put("message", "Token байхгүй");
                return ResponseEntity.ok(response);
            }
            
            String jwtToken = token.startsWith("Bearer ") ? token.substring(7) : token;
            boolean isValid = authService.validateJwtToken(jwtToken);
            
            response.put("valid", isValid);
            if (isValid) {
                response.put("message", "Token хүчинтэй");
                // Token-аас нэмэлт мэдээлэл авах
                String username = authService.getUsernameFromJwtToken(jwtToken);
                if (username != null) {
                    response.put("username", username);
                }
            } else {
                response.put("message", "Token хүчингүй");
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Token баталгаажуулах алдаа: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("valid", false);
            errorResponse.put("message", "Token шалгахад алдаа гарлаа");
            return ResponseEntity.ok(errorResponse);
        }
    }

    @PostMapping("/change-password")
    @Operation(summary = "Нууц үг солих", description = "Хэрэглэгчийн нууц үг солих")
    public ResponseEntity<?> changePassword(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestBody Map<String, String> request) {
        try {
            if (token == null || !token.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Token шаардлагатай"));
            }

            String jwtToken = token.substring(7);
            Optional<User> userOpt = authService.getCurrentUser(jwtToken);
            
            if (!userOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Хэрэглэгч олдсонгүй"));
            }

            String currentPassword = request.get("currentPassword");
            String newPassword = request.get("newPassword");

            if (currentPassword == null || newPassword == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Одоогийн болон шинэ нууц үг шаардлагатай"));
            }

            User user = userOpt.get();
            boolean success = authService.changePassword(user.getId(), currentPassword, newPassword);

            if (success) {
                log.info("Нууц үг амжилттай солигдлоо: {}", user.getUsername());
                return ResponseEntity.ok(Map.of("message", "Нууц үг амжилттай солигдлоо"));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Нууц үг солих амжилтгүй"));
            }

        } catch (Exception e) {
            log.error("Нууц үг солих алдаа: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Нууц үг солихад алдаа гарлаа"));
        }
    }

    @GetMapping("/health")
    @Operation(summary = "Authentication service health check")
    public ResponseEntity<?> healthCheck() {
        try {
            Map<String, Object> health = authService.checkAuthServiceHealth();
            health.put("controller", "OK");
            health.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(health);
        } catch (Exception e) {
            log.error("Health check алдаа: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("status", "ERROR", "message", "Authentication service унтарсан байна"));
        }
    }

    /**
     * Development mode-н тулд test users-ийн жагсаалт
     */
    @GetMapping("/test-users")
    @Operation(summary = "Test users жагсаалт", description = "Development mode-н тулд")
    public ResponseEntity<?> getTestUsers() {
        Map<String, Object> response = new HashMap<>();
        response.put("testUsers", new Object[]{
            Map.of("username", "admin", "password", "admin123", "role", "SUPER_ADMIN", "name", "Системийн админ"),
            Map.of("username", "manager", "password", "manager123", "role", "MANAGER", "name", "Салбарын менежер"),
            Map.of("username", "loan_officer", "password", "loan123", "role", "LOAN_OFFICER", "name", "Зээлийн мэргэжилтэн"),
            Map.of("username", "reviewer", "password", "admin123", "role", "DOCUMENT_REVIEWER", "name", "Баримт хянагч"),
            Map.of("username", "customer_service", "password", "admin123", "role", "CUSTOMER_SERVICE", "name", "Харилцагчийн үйлчилгээ")
        });
        response.put("note", "Эдгээр нь development mode-н test users. Production-д хасах ёстой.");
        
        return ResponseEntity.ok(response);
    }
}