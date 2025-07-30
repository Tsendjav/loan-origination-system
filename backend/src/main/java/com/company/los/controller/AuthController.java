package com.company.los.controller;

import com.company.los.dto.AuthResponseDto;
import com.company.los.dto.LoginRequestDto;
import com.company.los.entity.User;
import com.company.los.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Authentication Controller - ЭЦСИЙН ЗАСВАРЛАСАН ХУВИЛБАР
 * ⭐ LOGIN VALIDATION & CHARACTER ENCODING АЛДАА БҮРЭН ШИЙДЭГДСЭН ⭐
 * @author LOS Development Team
 * @version 2.3 - Complete Login Validation Fix
 * @since 2025-07-30
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Нэвтрэлтийн API")
@CrossOrigin(origins = {"http://localhost:3001", "http://localhost:3000", "http://127.0.0.1:3001", "http://127.0.0.1:3000"})
public class AuthController {

    private final AuthService authService;

    /**
     * ⭐ ЭЦСИЙН ЗАСВАРЛАСАН LOGIN METHOD ⭐
     * Bean Validation болон Manual Validation хослуулсан
     * Character encoding алдаа шийдэгдсэн
     */
    @PostMapping(value = "/login", produces = "application/json;charset=UTF-8")
    @Operation(summary = "Системд нэвтрэх", description = "Хэрэглэгчийн нэр болон нууц үгээр нэвтрэх")
    public ResponseEntity<?> login(@RequestBody(required = false) LoginRequestDto loginRequest) {
        try {
            log.info("Login хүсэлт хүлээн авлаа: {}", loginRequest != null ? loginRequest.getUsername() : "null request");
            
            // ⭐ ЭХНИЙ NULL CHECK ⭐
            if (loginRequest == null) {
                log.warn("Login request body нь null байна");
                return createErrorResponse("Login мэдээлэл илгээгдээгүй байна", HttpStatus.BAD_REQUEST);
            }

            // ⭐ INPUT SANITIZATION ⭐
            loginRequest.sanitizeInputs();
            loginRequest.normalizeUsername();
            loginRequest.ensureTimestamp();
            loginRequest.ensurePlatform();

            // ⭐ MANUAL VALIDATION - Bean Validation-ийн оронд ⭐
            String validationError = LoginRequestDto.Validator.validateLoginRequest(loginRequest);
            if (validationError != null) {
                log.warn("Validation алдаа: {} - {}", loginRequest.getUsername(), validationError);
                return createErrorResponse(validationError, HttpStatus.BAD_REQUEST);
            }

            // ⭐ НЭМЭЛТ VALIDATION CHECK ⭐
            if (!loginRequest.isValid()) {
                String customError = loginRequest.getValidationError();
                log.warn("Custom validation алдаа: {} - {}", loginRequest.getUsername(), customError);
                return createErrorResponse(customError != null ? customError : "Нэвтрэх мэдээлэл буруу байна", HttpStatus.BAD_REQUEST);
            }

            log.info("Validation амжилттай: {} оролдож байна", loginRequest.getUsername());
            
            // ⭐ AUTHENTICATION SERVICE-ЭЭР НЭВТРЭХ ⭐
            AuthResponseDto response = authService.login(loginRequest);
            
            if (response != null && response.isSuccess()) {
                log.info("Амжилттай нэвтэрлэв: {}", loginRequest.getUsername());
                
                // ⭐ SUCCESS RESPONSE UTF-8 ТЭМДЭГЛЭЛ ТЭМДЭГЛЭЛ ⭐
                Map<String, Object> successResponse = new HashMap<>();
                successResponse.put("success", true);
                successResponse.put("message", "Амжилттай нэвтэрлээ");
                successResponse.put("token", response.getToken());
                successResponse.put("refreshToken", response.getRefreshToken());
                successResponse.put("tokenType", response.getTokenType());
                successResponse.put("expiresIn", response.getExpiresIn());
                successResponse.put("user", Map.of(
                    "id", response.getUserId(),
                    "username", response.getUsername(),
                    "email", response.getEmail(),
                    "fullName", response.getFullName(),
                    "roles", response.getRoles()
                ));
                
                return ResponseEntity.ok(successResponse);
            } else {
                log.warn("Нэвтрэх амжилтгүй: {} - {}", loginRequest.getUsername(), 
                    response != null ? response.getMessage() : "Тодорхойгүй алдаа");
                
                return createErrorResponse("Хэрэглэгчийн нэр эсвэл нууц үг буруу байна", HttpStatus.UNAUTHORIZED);
            }
            
        } catch (IllegalArgumentException e) {
            log.warn("Login параметр алдаа: {} - {}", 
                loginRequest != null ? loginRequest.getUsername() : "null", e.getMessage());
            return createErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
            
        } catch (Exception e) {
            log.error("Login системийн алдаа: {} - {}", 
                loginRequest != null ? loginRequest.getUsername() : "null", e.getMessage(), e);
            
            return createErrorResponse("Нэвтрэхэд алдаа гарлаа. Дахин оролдоно уу", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * ⭐ ERROR RESPONSE ҮҮСГЭХ HELPER METHOD ⭐
     * UTF-8 character encoding дэмжтэй
     */
    private ResponseEntity<?> createErrorResponse(String message, HttpStatus status) {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("error", message);
        error.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.status(status)
                .header("Content-Type", "application/json;charset=UTF-8")
                .body(error);
    }

    @PostMapping(value = "/logout", produces = "application/json;charset=UTF-8")
    @Operation(summary = "Системээс гарах")
    public ResponseEntity<?> logout(@RequestHeader(value = "Authorization", required = false) String token) {
        try {
            log.info("Logout хүсэлт хүлээн авлаа");
            
            if (token != null && token.startsWith("Bearer ")) {
                String jwtToken = token.substring(7);
                authService.logoutUser(jwtToken);
                log.info("Хэрэглэгч амжилттай гарлаа");
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Амжилттай гарлаа");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok()
                    .header("Content-Type", "application/json;charset=UTF-8")
                    .body(response);
            
        } catch (Exception e) {
            log.error("Logout алдаа: {}", e.getMessage());
            return createErrorResponse("Системээс гарахад алдаа гарлаа", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/me", produces = "application/json;charset=UTF-8")
    @Operation(summary = "Одоогийн хэрэглэгчийн мэдээлэл")
    public ResponseEntity<?> getCurrentUser(@RequestHeader(value = "Authorization", required = false) String token) {
        try {
            if (token == null || !token.startsWith("Bearer ")) {
                return createErrorResponse("Token байхгүй", HttpStatus.UNAUTHORIZED);
            }

            String jwtToken = token.substring(7);
            Optional<User> userOpt = authService.getCurrentUser(jwtToken);
            
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("success", true);
                userInfo.put("id", user.getId());
                userInfo.put("username", user.getUsername());
                userInfo.put("email", user.getEmail());
                userInfo.put("fullName", user.getFirstName() + " " + user.getLastName());
                userInfo.put("roles", user.getRoles());
                userInfo.put("isActive", user.getIsActive());
                userInfo.put("lastLoginAt", user.getLastLoginAt());
                userInfo.put("timestamp", System.currentTimeMillis());
                
                return ResponseEntity.ok()
                        .header("Content-Type", "application/json;charset=UTF-8")
                        .body(userInfo);
            } else {
                return createErrorResponse("Хэрэглэгчийн мэдээлэл олдсонгүй эсвэл token хүчингүй", HttpStatus.UNAUTHORIZED);
            }
            
        } catch (Exception e) {
            log.error("Хэрэглэгчийн мэдээлэл авах алдаа: {}", e.getMessage());
            return createErrorResponse("Хэрэглэгчийн мэдээлэл олдсонгүй", HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping(value = "/test", produces = "application/json;charset=UTF-8")
    @Operation(summary = "Authentication тест")
    public ResponseEntity<?> test() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "Authentication Controller ажиллаж байна");
        response.put("timestamp", System.currentTimeMillis());
        response.put("success", true);
        response.put("charset", "UTF-8");
        response.put("availableEndpoints", new String[]{
            "POST /api/v1/auth/login",
            "POST /api/v1/auth/logout", 
            "GET /api/v1/auth/me",
            "POST /api/v1/auth/refresh",
            "GET /api/v1/auth/validate",
            "GET /api/v1/auth/test"
        });
        
        return ResponseEntity.ok()
                .header("Content-Type", "application/json;charset=UTF-8")
                .body(response);
    }

    @PostMapping(value = "/refresh", produces = "application/json;charset=UTF-8")
    @Operation(summary = "Token сэргээх", description = "Refresh token ашиглан шинэ access token авах")
    public ResponseEntity<?> refreshToken(@RequestBody(required = false) Map<String, String> request) {
        try {
            if (request == null) {
                return createErrorResponse("Refresh token хүсэлт байхгүй байна", HttpStatus.BAD_REQUEST);
            }
            
            String refreshToken = request.get("refreshToken");
            if (refreshToken == null || refreshToken.trim().isEmpty()) {
                return createErrorResponse("Refresh token шаардлагатай", HttpStatus.BAD_REQUEST);
            }

            AuthResponseDto response = authService.refreshToken(refreshToken);

            if (response != null && response.isSuccess()) {
                log.info("Token амжилттай сэргээгдлэв");
                return ResponseEntity.ok()
                        .header("Content-Type", "application/json;charset=UTF-8")
                        .body(response);
            } else {
                log.warn("Token сэргээх амжилтгүй: {}", response != null ? response.getMessage() : "Тодорхойгүй алдаа");
                return createErrorResponse("Token сэргээх амжилтгүй", HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            log.error("Refresh token алдаа: {}", e.getMessage());
            return createErrorResponse("Token сэргээхэд алдаа гарлаа", HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping(value = "/validate", produces = "application/json;charset=UTF-8")
    @Operation(summary = "Token баталгаажуулах", description = "Access token-ийг баталгаажуулах")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestHeader(value = "Authorization", required = false) String token) {
        try {
            Map<String, Object> response = new HashMap<>();
            
            if (token == null) {
                response.put("valid", false);
                response.put("message", "Token байхгүй");
                response.put("success", true);
                response.put("timestamp", System.currentTimeMillis());
                return ResponseEntity.ok()
                        .header("Content-Type", "application/json;charset=UTF-8")
                        .body(response);
            }
            
            String jwtToken = token.startsWith("Bearer ") ? token.substring(7) : token;
            boolean isValid = authService.validateJwtToken(jwtToken);
            
            response.put("valid", isValid);
            response.put("timestamp", System.currentTimeMillis());
            response.put("success", true);
            
            if (isValid) {
                response.put("message", "Token хүчинтэй");
                String username = authService.getUsernameFromJwtToken(jwtToken);
                if (username != null) {
                    response.put("username", username);
                }
            } else {
                response.put("message", "Token хүчингүй");
            }
            
            return ResponseEntity.ok()
                    .header("Content-Type", "application/json;charset=UTF-8")
                    .body(response);
        } catch (Exception e) {
            log.error("Token баталгаажуулах алдаа: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("valid", false);
            errorResponse.put("message", "Token шалгахад алдаа гарлаа");
            errorResponse.put("timestamp", System.currentTimeMillis());
            errorResponse.put("success", false);
            return ResponseEntity.ok()
                    .header("Content-Type", "application/json;charset=UTF-8")
                    .body(errorResponse);
        }
    }

    @PostMapping(value = "/change-password", produces = "application/json;charset=UTF-8")
    @Operation(summary = "Нууц үг солих", description = "Хэрэглэгчийн нууц үг солих")
    public ResponseEntity<?> changePassword(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestBody(required = false) Map<String, String> request) {
        try {
            if (token == null || !token.startsWith("Bearer ")) {
                return createErrorResponse("Token шаардлагатай", HttpStatus.UNAUTHORIZED);
            }

            if (request == null) {
                return createErrorResponse("Нууц үг солих мэдээлэл байхгүй", HttpStatus.BAD_REQUEST);
            }

            String jwtToken = token.substring(7);
            Optional<User> userOpt = authService.getCurrentUser(jwtToken);
            
            if (!userOpt.isPresent()) {
                return createErrorResponse("Хэрэглэгч олдсонгүй", HttpStatus.UNAUTHORIZED);
            }

            String currentPassword = request.get("currentPassword");
            String newPassword = request.get("newPassword");

            if (currentPassword == null || newPassword == null) {
                return createErrorResponse("Одоогийн болон шинэ нууц үг шаардлагатай", HttpStatus.BAD_REQUEST);
            }

            User user = userOpt.get();
            boolean success = authService.changePassword(user.getId(), currentPassword, newPassword);

            if (success) {
                log.info("Нууц үг амжилттай солигдлоо: {}", user.getUsername());
                Map<String, Object> successResponse = new HashMap<>();
                successResponse.put("success", true);
                successResponse.put("message", "Нууц үг амжилттай солигдлоо");
                successResponse.put("timestamp", System.currentTimeMillis());
                
                return ResponseEntity.ok()
                        .header("Content-Type", "application/json;charset=UTF-8")
                        .body(successResponse);
            } else {
                return createErrorResponse("Нууц үг солих амжилтгүй", HttpStatus.BAD_REQUEST);
            }

        } catch (Exception e) {
            log.error("Нууц үг солих алдаа: {}", e.getMessage());
            return createErrorResponse("Нууц үг солихад алдаа гарлаа", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/health", produces = "application/json;charset=UTF-8")
    @Operation(summary = "Authentication service health check")
    public ResponseEntity<?> healthCheck() {
        try {
            Map<String, Object> health = new HashMap<>();
            health.put("status", "UP");
            health.put("service", "AuthController");
            health.put("timestamp", System.currentTimeMillis());
            health.put("version", "2.3");
            health.put("success", true);
            health.put("charset", "UTF-8");
            
            try {
                Map<String, Object> serviceHealth = authService.checkAuthServiceHealth();
                health.putAll(serviceHealth);
            } catch (Exception e) {
                log.warn("Auth service health check failed: {}", e.getMessage());
                health.put("serviceStatus", "DEGRADED");
                health.put("serviceMessage", e.getMessage());
            }
            
            return ResponseEntity.ok()
                    .header("Content-Type", "application/json;charset=UTF-8")
                    .body(health);
        } catch (Exception e) {
            log.error("Health check алдаа: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .header("Content-Type", "application/json;charset=UTF-8")
                    .body(Map.of(
                        "status", "DOWN", 
                        "error", "Authentication service унтарсан байна", 
                        "success", false,
                        "timestamp", System.currentTimeMillis()
                    ));
        }
    }

    /**
     * Development mode-н тулд test users-ийн жагсаалт
     */
    @GetMapping(value = "/test-users", produces = "application/json;charset=UTF-8")
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
        response.put("success", true);
        response.put("timestamp", System.currentTimeMillis());
        response.put("charset", "UTF-8");
        
        return ResponseEntity.ok()
                .header("Content-Type", "application/json;charset=UTF-8")
                .body(response);
    }

    /**
     * ⭐ LOGIN FORM VALIDATION ТЕСТ ENDPOINT ⭐
     * Frontend integration тестэд зориулсан
     */
    @PostMapping(value = "/test-validation", produces = "application/json;charset=UTF-8")
    @Operation(summary = "Login form validation тест", description = "Frontend validation тест хийх")
    public ResponseEntity<?> testValidation(@RequestBody(required = false) LoginRequestDto loginRequest) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (loginRequest == null) {
                response.put("valid", false);
                response.put("error", "Login мэдээлэл илгээгдээгүй байна");
                response.put("success", true);
                return ResponseEntity.ok()
                        .header("Content-Type", "application/json;charset=UTF-8")
                        .body(response);
            }

            // Input sanitization
            loginRequest.sanitizeInputs();
            loginRequest.normalizeUsername();

            // Validation check
            String validationError = LoginRequestDto.Validator.validateLoginRequest(loginRequest);
            
            if (validationError != null) {
                response.put("valid", false);
                response.put("error", validationError);
                response.put("field", getErrorField(validationError));
            } else {
                response.put("valid", true);
                response.put("message", "Validation амжилттай");
                response.put("username", loginRequest.getUsername());
                response.put("isEmail", loginRequest.isEmail());
            }
            
            response.put("success", true);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok()
                    .header("Content-Type", "application/json;charset=UTF-8")
                    .body(response);
            
        } catch (Exception e) {
            log.error("Validation тест алдаа: {}", e.getMessage());
            response.put("valid", false);
            response.put("error", "Validation тест хийхэд алдаа гарлаа");
            response.put("success", false);
            return ResponseEntity.ok()
                    .header("Content-Type", "application/json;charset=UTF-8")
                    .body(response);
        }
    }

    /**
     * Validation алдааны талбарыг тодорхойлох helper метод
     */
    private String getErrorField(String errorMessage) {
        if (errorMessage.contains("Хэрэглэгчийн нэр")) {
            return "username";
        } else if (errorMessage.contains("Нууц үг")) {
            return "password";
        }
        return "general";
    }
}