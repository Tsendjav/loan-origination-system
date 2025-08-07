package com.company.los.controller;

import com.company.los.dto.AuthResponseDto;
import com.company.los.dto.LoginRequestDto;
import com.company.los.entity.User;
import com.company.los.service.AuthService;
import com.company.los.util.LogUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * ⭐ ЭЦСИЙН САЙЖРУУЛСАН Authentication Controller ⭐
 * 
 * САЙЖРУУЛАЛТУУД:
 * ✅ JSON deserialization засварласан (@RequestBody нэмэгдсэн)
 * ✅ Null check сайжруулсан  
 * ✅ Validation messages тодорхой болгосон
 * ✅ Character encoding алдаа засварласан
 * ✅ Логлалт сайжруулсан (emoji болон LogUtil)
 * ✅ IP address tracking нэмэгдсэн
 * ✅ Error handling сайжруулсан
 * ✅ Бүх endpoint-уудыг хадгалсан
 *
 * @author LOS Development Team  
 * @version 5.0 - FINAL IMPROVED VERSION
 * @since 2025-08-06
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Нэвтрэлтийн API")
@CrossOrigin(origins = {"http://localhost:3001", "http://localhost:3000", "http://127.0.0.1:3001", "http://127.0.0.1:3000"})
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;

    /**
     * ⭐ ЭЦСИЙН САЙЖРУУЛСАН LOGIN METHOD ⭐
     * JSON deserialization, validation болон error handling сайжруулсан
     */
    @PostMapping(value = "/login", produces = "application/json;charset=UTF-8")
    @Operation(summary = "Системд нэвтрэх", description = "Хэрэглэгчийн нэр болон нууц үгээр нэвтрэх")
    public ResponseEntity<Map<String, Object>> login(
            @Valid @RequestBody LoginRequestDto loginRequest, // ⭐ @RequestBody НЭМЭГДСЭН! ⭐
            HttpServletRequest request) {
        
        try {
            // ⭐ IMPROVED NULL CHECK ⭐
            if (loginRequest == null) {
                log.error("❌ LoginRequestDto null байна - JSON deserialization алдаа");
                return createErrorResponse("Login мэдээлэл хүлээн авч чадсангүй", HttpStatus.BAD_REQUEST);
            }

            // ⭐ DEBUG МЭДЭЭЛЭЛ ⭐
            log.info("🔐 Login хүсэлт хүлээн авлаа - Raw data debug:");
            log.info("   Username: [{}]", loginRequest.getUsername() != null ? LogUtil.maskSensitiveData(loginRequest.getUsername()) : "NULL");
            log.info("   Password: [{}]", loginRequest.getPassword() != null ? "PROVIDED" : "NULL");
            
            // ⭐ USERNAME VALIDATION САЙЖРУУЛСАН ⭐
            if (loginRequest.getUsername() == null) {
                log.warn("⚠️ Username field нь null байна");
                return createValidationErrorResponse("username", "Хэрэглэгчийн нэр заавал оруулна уу");
            }
            
            if (loginRequest.getUsername().trim().isEmpty()) {
                log.warn("⚠️ Username хоосон string байна");
                return createValidationErrorResponse("username", "Хэрэглэгчийн нэр хоосон байж болохгүй");
            }

            // ⭐ PASSWORD VALIDATION САЙЖРУУЛСАН ⭐  
            if (loginRequest.getPassword() == null) {
                log.warn("⚠️ Password field нь null байна");
                return createValidationErrorResponse("password", "Нууц үг заавал оруулна уу");
            }
            
            if (loginRequest.getPassword().trim().isEmpty()) {
                log.warn("⚠️ Password хоосон string байна");
                return createValidationErrorResponse("password", "Нууц үг хоосон байж болохгүй");
            }

            // ⭐ INPUT SANITIZATION ⭐
            loginRequest.sanitizeInputs();
            loginRequest.normalizeUsername();
            loginRequest.ensureTimestamp();
            loginRequest.ensurePlatform();

            // ⭐ VALIDATION ⭐
            String validationError = LoginRequestDto.Validator.validateLoginRequest(loginRequest);
            if (validationError != null) {
                log.warn("⚠️ Validation алдаа: {} - {}", LogUtil.maskSensitiveData(loginRequest.getUsername()), validationError);
                return createErrorResponse(validationError, HttpStatus.BAD_REQUEST);
            }

            // ⭐ ADDITIONAL VALIDATION CHECK ⭐
            if (!loginRequest.isValid()) {
                String customError = loginRequest.getValidationError();
                log.warn("⚠️ Custom validation алдаа: {} - {}", LogUtil.maskSensitiveData(loginRequest.getUsername()), customError);
                return createErrorResponse(customError != null ? customError : "Нэвтрэх мэдээлэл буруу байна", HttpStatus.BAD_REQUEST);
            }

            // ⭐ IP ADDRESS МЭДЭЭЛЭЛ НЭМЭХ ⭐
            String clientIp = getClientIpAddress(request);
            loginRequest.setIpAddress(clientIp);
            loginRequest.setUserAgent(request.getHeader("User-Agent"));

            log.info("✅ Validation амжилттай: {} оролдож байна", LogUtil.maskSensitiveData(loginRequest.getUsername()));
            
            // ⭐ AUTHENTICATION SERVICE-ЭЭР НЭВТРЭХ ⭐
            AuthResponseDto response = authService.login(loginRequest);
            
            if (response == null) {
                log.error("❌ AuthService null response буцаалаа");
                return createErrorResponse("Нэвтрэх үйлдэлд алдаа гарлаа", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            
            if (response.isSuccess()) {
                log.info("✅ Амжилттай нэвтэрлэв: {}", LogUtil.maskSensitiveData(loginRequest.getUsername()));
                
                // ⭐ SUCCESS RESPONSE UTF-8 ТЭМДЭГЛЭЛ ⭐
                Map<String, Object> successResponse = new HashMap<>();
                successResponse.put("success", true);
                successResponse.put("message", "Амжилттай нэвтэрлээ");
                successResponse.put("token", response.getToken());
                successResponse.put("refreshToken", response.getRefreshToken());
                successResponse.put("tokenType", response.getTokenType());
                successResponse.put("expiresIn", response.getExpiresIn());
                
                // User мэдээлэл
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", response.getUserId());
                userInfo.put("username", response.getUsername());
                userInfo.put("email", response.getEmail());
                userInfo.put("fullName", response.getFullName());
                userInfo.put("roles", response.getRoles());
                successResponse.put("user", userInfo);
                
                successResponse.put("timestamp", System.currentTimeMillis());
                
                return ResponseEntity.ok()
                        .header("Content-Type", "application/json;charset=UTF-8")
                        .body(successResponse);
            } else {
                log.warn("⚠️ Нэвтрэх амжилтгүй: {} - {}", 
                    LogUtil.maskSensitiveData(loginRequest.getUsername()), response.getMessage());
                
                return createErrorResponse(
                    response.getMessage() != null ? response.getMessage() : "Хэрэглэгчийн нэр эсвэл нууц үг буруу байна", 
                    HttpStatus.UNAUTHORIZED
                );
            }
            
        } catch (IllegalArgumentException e) {
            log.warn("⚠️ Login параметр алдаа: {} - {}", 
                loginRequest != null ? LogUtil.maskSensitiveData(loginRequest.getUsername()) : "null", e.getMessage());
            return createErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
            
        } catch (Exception e) {
            log.error("❌ Login системийн алдаа: {} - {}", 
                loginRequest != null ? LogUtil.maskSensitiveData(loginRequest.getUsername()) : "null", e.getMessage(), e);
            
            return createErrorResponse("Нэвтрэхэд алдаа гарлаа. Дахин оролдоно уу", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * ⭐ САЙЖРУУЛСАН: Хэрэглэгч гарах ⭐
     */
    @PostMapping(value = "/logout", produces = "application/json;charset=UTF-8")
    @Operation(summary = "Системээс гарах")
    public ResponseEntity<Map<String, Object>> logout(HttpServletRequest request) {
        try {
            log.info("🚪 Logout хүсэлт хүлээн авлаа");
            
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String jwtToken = authHeader.substring(7);
                boolean success = authService.logoutUser(jwtToken);
                
                if (success) {
                    log.info("✅ Хэрэглэгч амжилттай гарлаа");
                } else {
                    log.warn("⚠️ Logout амжилтгүй боллоо");
                }
            }
            
            return createSuccessResponse("Амжилттай гарлаа");
            
        } catch (Exception e) {
            log.error("❌ Logout алдаа: {}", e.getMessage());
            return createErrorResponse("Системээс гарахад алдаа гарлаа", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * ⭐ САЙЖРУУЛСАН: Одоогийн хэрэглэгчийн мэдээлэл авах ⭐
     */
    @GetMapping(value = "/me", produces = "application/json;charset=UTF-8")
    @Operation(summary = "Одоогийн хэрэглэгчийн мэдээлэл")
    public ResponseEntity<Map<String, Object>> getCurrentUser(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return createErrorResponse("Token байхгүй", HttpStatus.UNAUTHORIZED);
            }

            String jwtToken = authHeader.substring(7);
            
            if (!authService.validateJwtToken(jwtToken)) {
                return createErrorResponse("Token хүчингүй байна", HttpStatus.UNAUTHORIZED);
            }
            
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
            log.error("❌ Хэрэглэгчийн мэдээлэл авах алдаа: {}", e.getMessage());
            return createErrorResponse("Хэрэглэгчийн мэдээлэл олдсонгүй", HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * ⭐ САЙЖРУУЛСАН: Token сэргээх ⭐
     */
    @PostMapping(value = "/refresh", produces = "application/json;charset=UTF-8")
    @Operation(summary = "Token сэргээх", description = "Refresh token ашиглан шинэ access token авах")
    public ResponseEntity<Map<String, Object>> refreshToken(@RequestBody(required = false) Map<String, String> request) {
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
                log.info("✅ Token амжилттай сэргээгдлэв");
                
                Map<String, Object> successResponse = new HashMap<>();
                successResponse.put("success", true);
                successResponse.put("token", response.getToken());
                successResponse.put("refreshToken", response.getRefreshToken());
                successResponse.put("tokenType", response.getTokenType());
                successResponse.put("expiresIn", response.getExpiresIn());
                successResponse.put("timestamp", System.currentTimeMillis());
                
                return ResponseEntity.ok()
                        .header("Content-Type", "application/json;charset=UTF-8")
                        .body(successResponse);
            } else {
                log.warn("⚠️ Token сэргээх амжилтгүй: {}", response != null ? response.getMessage() : "Тодорхойгүй алдаа");
                return createErrorResponse("Token сэргээх амжилтгүй", HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            log.error("❌ Refresh token алдаа: {}", e.getMessage());
            return createErrorResponse("Token сэргээхэд алдаа гарлаа", HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * ⭐ САЙЖРУУЛСАН: Token баталгаажуулах ⭐
     */
    @GetMapping(value = "/validate", produces = "application/json;charset=UTF-8")
    @Operation(summary = "Token баталгаажуулах", description = "Access token-ийг баталгаажуулах")
    public ResponseEntity<Map<String, Object>> validateToken(HttpServletRequest request) {
        try {
            Map<String, Object> response = new HashMap<>();
            String authHeader = request.getHeader("Authorization");
            
            if (authHeader == null) {
                response.put("valid", false);
                response.put("message", "Token байхгүй");
                response.put("success", true);
                response.put("timestamp", System.currentTimeMillis());
                return ResponseEntity.ok()
                        .header("Content-Type", "application/json;charset=UTF-8")
                        .body(response);
            }
            
            String jwtToken = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
            boolean isValid = authService.validateJwtToken(jwtToken);
            
            response.put("valid", isValid);
            response.put("timestamp", System.currentTimeMillis());
            response.put("success", true);
            
            if (isValid) {
                response.put("message", "Token хүчинтэй");
                String username = authService.getUsernameFromJwtToken(jwtToken);
                if (username != null) {
                    response.put("username", LogUtil.maskSensitiveData(username));
                }
            } else {
                response.put("message", "Token хүчингүй");
            }
            
            return ResponseEntity.ok()
                    .header("Content-Type", "application/json;charset=UTF-8")
                    .body(response);
        } catch (Exception e) {
            log.error("❌ Token баталгаажуулах алдаа: {}", e.getMessage());
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

    /**
     * ⭐ САЙЖРУУЛСАН: Нууц үг солих ⭐
     */
    @PostMapping(value = "/change-password", produces = "application/json;charset=UTF-8")
    @Operation(summary = "Нууц үг солих", description = "Хэрэглэгчийн нууц үг солих")
    public ResponseEntity<Map<String, Object>> changePassword(
            HttpServletRequest request,
            @RequestBody(required = false) Map<String, String> passwordRequest) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return createErrorResponse("Token шаардлагатай", HttpStatus.UNAUTHORIZED);
            }

            if (passwordRequest == null) {
                return createErrorResponse("Нууц үг солих мэдээлэл байхгүй", HttpStatus.BAD_REQUEST);
            }

            String jwtToken = authHeader.substring(7);
            Optional<User> userOpt = authService.getCurrentUser(jwtToken);
            
            if (!userOpt.isPresent()) {
                return createErrorResponse("Хэрэглэгч олдсонгүй", HttpStatus.UNAUTHORIZED);
            }

            String currentPassword = passwordRequest.get("currentPassword");
            String newPassword = passwordRequest.get("newPassword");

            if (currentPassword == null || newPassword == null) {
                return createErrorResponse("Одоогийн болон шинэ нууц үг шаардлагатай", HttpStatus.BAD_REQUEST);
            }

            User user = userOpt.get();
            boolean success = authService.changePassword(user.getId(), currentPassword, newPassword);

            if (success) {
                log.info("✅ Нууц үг амжилттай солигдлоо: {}", LogUtil.maskSensitiveData(user.getUsername()));
                return createSuccessResponse("Нууц үг амжилттай солигдлоо");
            } else {
                return createErrorResponse("Нууц үг солих амжилтгүй", HttpStatus.BAD_REQUEST);
            }

        } catch (Exception e) {
            log.error("❌ Нууц үг солих алдаа: {}", e.getMessage());
            return createErrorResponse("Нууц үг солихад алдаа гарлаа", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * ⭐ ТЕСТ ENDPOINTS ⭐
     */
    @GetMapping(value = "/test", produces = "application/json;charset=UTF-8")
    @Operation(summary = "Authentication тест")
    public ResponseEntity<Map<String, Object>> test() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "Authentication Controller ажиллаж байна");
        response.put("timestamp", System.currentTimeMillis());
        response.put("success", true);
        response.put("charset", "UTF-8");
        response.put("version", "5.0");
        response.put("availableEndpoints", new String[]{
            "POST /api/v1/auth/login",
            "POST /api/v1/auth/logout", 
            "GET /api/v1/auth/me",
            "POST /api/v1/auth/refresh",
            "GET /api/v1/auth/validate",
            "POST /api/v1/auth/change-password",
            "GET /api/v1/auth/test",
            "GET /api/v1/auth/health",
            "GET /api/v1/auth/test-users",
            "POST /api/v1/auth/test-validation",
            "POST /api/v1/auth/debug-json"
        });
        
        return ResponseEntity.ok()
                .header("Content-Type", "application/json;charset=UTF-8")
                .body(response);
    }

    @GetMapping(value = "/health", produces = "application/json;charset=UTF-8")
    @Operation(summary = "Authentication service health check")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        try {
            Map<String, Object> health = new HashMap<>();
            health.put("status", "UP");
            health.put("service", "AuthController");
            health.put("timestamp", System.currentTimeMillis());
            health.put("version", "5.0");
            health.put("success", true);
            health.put("charset", "UTF-8");
            
            try {
                Map<String, Object> serviceHealth = authService.checkAuthServiceHealth();
                health.putAll(serviceHealth);
            } catch (Exception e) {
                log.warn("⚠️ Auth service health check failed: {}", e.getMessage());
                health.put("serviceStatus", "DEGRADED");
                health.put("serviceMessage", e.getMessage());
            }
            
            return ResponseEntity.ok()
                    .header("Content-Type", "application/json;charset=UTF-8")
                    .body(health);
        } catch (Exception e) {
            log.error("❌ Health check алдаа: {}", e.getMessage());
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

    @GetMapping(value = "/test-users", produces = "application/json;charset=UTF-8")
    @Operation(summary = "Test users жагсаалт", description = "Development mode-н тулд")
    public ResponseEntity<Map<String, Object>> getTestUsers() {
        Map<String, Object> response = new HashMap<>();
        response.put("testUsers", new Object[]{
            Map.of("username", "admin", "password", "admin123", "role", "SUPER_ADMIN", "name", "Системийн админ"),
            Map.of("username", "manager", "password", "manager123", "role", "MANAGER", "name", "Салбарын менежер"),
            Map.of("username", "loan_officer", "password", "loan123", "role", "LOAN_OFFICER", "name", "Зээлийн мэргэжилтэн"),
            Map.of("username", "reviewer", "password", "admin123", "role", "DOCUMENT_REVIEWER", "name", "Баримт хянагч"),
            Map.of("username", "customer_service", "password", "admin123", "role", "CUSTOMER_SERVICE", "name", "Харилцагчийн үйлчилгээ")
        });
        response.put("note", "⚠️ Эдгээр нь development mode-н test users. Production-д хасах ёстой.");
        response.put("success", true);
        response.put("timestamp", System.currentTimeMillis());
        response.put("charset", "UTF-8");
        
        return ResponseEntity.ok()
                .header("Content-Type", "application/json;charset=UTF-8")
                .body(response);
    }

    /**
     * ⭐ JSON FORMAT DEBUG ENDPOINT ⭐
     * Frontend-с илгээсэн JSON format шалгахад зориулсан
     */
    @PostMapping(value = "/debug-json", produces = "application/json;charset=UTF-8")
    @Operation(summary = "JSON format debug", description = "Frontend-с илгээсэн JSON format шалгах")
    public ResponseEntity<Map<String, Object>> debugJson(@RequestBody(required = false) Object rawRequest, HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            log.info("🔍 Debug JSON хүсэлт хүлээн авлаа");
            
            // Raw request мэдээлэл
            response.put("success", true);
            response.put("message", "JSON format шалгагдлаа");
            response.put("receivedData", rawRequest);
            response.put("dataType", rawRequest != null ? rawRequest.getClass().getSimpleName() : "null");
            response.put("timestamp", System.currentTimeMillis());
            
            // Request headers
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", request.getContentType());
            headers.put("Content-Length", request.getHeader("Content-Length"));
            response.put("headers", headers);
            
            // Хэрэв Map бол утгуудыг шалгах
            if (rawRequest instanceof Map) {
                Map<?, ?> mapData = (Map<?, ?>) rawRequest;
                Map<String, Object> fieldAnalysis = new HashMap<>();
                
                for (Map.Entry<?, ?> entry : mapData.entrySet()) {
                    String key = String.valueOf(entry.getKey());
                    Object value = entry.getValue();
                    
                    Map<String, Object> fieldInfo = new HashMap<>();
                    fieldInfo.put("value", value);
                    fieldInfo.put("type", value != null ? value.getClass().getSimpleName() : "null");
                    fieldInfo.put("isEmpty", value == null || (value instanceof String && ((String) value).trim().isEmpty()));
                    
                    fieldAnalysis.put(key, fieldInfo);
                }
                
                response.put("fieldAnalysis", fieldAnalysis);
            }
            
            return ResponseEntity.ok()
                    .header("Content-Type", "application/json;charset=UTF-8")
                    .body(response);
            
        } catch (Exception e) {
            log.error("❌ JSON debug алдаа: {}", e.getMessage());
            response.put("success", false);
            response.put("error", "JSON debug хийхэд алдаа гарлаа");
            response.put("exception", e.getMessage());
            return ResponseEntity.ok()
                    .header("Content-Type", "application/json;charset=UTF-8")
                    .body(response);
        }
    }

    /**
     * ⭐ LOGIN FORM VALIDATION ТЕСТ ENDPOINT ⭐
     */
    @PostMapping(value = "/test-validation", produces = "application/json;charset=UTF-8")
    @Operation(summary = "Login form validation тест", description = "Frontend validation тест хийх")
    public ResponseEntity<Map<String, Object>> testValidation(@RequestBody(required = false) LoginRequestDto loginRequest) {
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
                response.put("username", LogUtil.maskSensitiveData(loginRequest.getUsername()));
                response.put("isEmail", loginRequest.isEmail());
            }
            
            response.put("success", true);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok()
                    .header("Content-Type", "application/json;charset=UTF-8")
                    .body(response);
            
        } catch (Exception e) {
            log.error("❌ Validation тест алдаа: {}", e.getMessage());
            response.put("valid", false);
            response.put("error", "Validation тест хийхэд алдаа гарлаа");
            response.put("success", false);
            return ResponseEntity.ok()
                    .header("Content-Type", "application/json;charset=UTF-8")
                    .body(response);
        }
    }

    // ⭐ HELPER METHODS ⭐

    /**
     * Амжилттай хариу үүсгэх
     */
    private ResponseEntity<Map<String, Object>> createSuccessResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok()
                .header("Content-Type", "application/json;charset=UTF-8")
                .body(response);
    }

    /**
     * Validation алдааны хариу үүсгэх - specific field мэдээлэлтэй
     */
    private ResponseEntity<Map<String, Object>> createValidationErrorResponse(String field, String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("error", "Validation Failed");
        error.put("message", "Нэвтрэх мэдээлэл буруу байна");
        error.put("timestamp", System.currentTimeMillis());
        
        Map<String, Object> details = new HashMap<>();
        details.put(field, message);
        error.put("details", details);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .header("Content-Type", "application/json;charset=UTF-8")
                .body(error);
    }

    /**
     * Алдааны хариу үүсгэх - UTF-8 character encoding дэмжтэй
     */
    private ResponseEntity<Map<String, Object>> createErrorResponse(String message, HttpStatus status) {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("error", message);
        error.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.status(status)
                .header("Content-Type", "application/json;charset=UTF-8")
                .body(error);
    }

    /**
     * Client IP address олох
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
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