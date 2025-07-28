package com.company.los.controller;

import com.company.los.dto.AuthResponseDto;
import com.company.los.dto.LoginRequestDto;
import com.company.los.entity.User;
import com.company.los.service.impl.AuthServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * ⭐ AUTH CONTROLLER - API AUTHENTICATION ⭐
 * - /api/v1/auth/login endpoint
 * - /api/v1/auth/logout endpoint
 * - /api/v1/auth/me endpoint
 * - /api/v1/auth/refresh endpoint
 * - CORS тохиргоотой
 * - SecurityConfig-тай уялдаатай
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = {"http://localhost:3001", "http://localhost:3000"})
@RequiredArgsConstructor
public class AuthController {

    private final AuthServiceImpl authService;

    /**
     * API Login endpoint
     * POST /api/v1/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDto loginRequest) {
        log.info("🔐 API Login attempt: {}", loginRequest.getUsername());
        
        try {
            // Normalize the request
            loginRequest.normalizeUsername();
            loginRequest.ensureTimestamp();
            loginRequest.ensurePlatform();
            
            AuthResponseDto response = authService.login(loginRequest);
            
            if (response.isSuccess()) {
                log.info("✅ API Login successful: {}", loginRequest.getUsername());
                return ResponseEntity.ok(response);
            } else {
                log.warn("❌ API Login failed: {} - {}", loginRequest.getUsername(), response.getMessage());
                return ResponseEntity.status(401).body(response);
            }
        } catch (Exception e) {
            log.error("❌ API Login error: {} - {}", loginRequest.getUsername(), e.getMessage(), e);
            
            AuthResponseDto errorResponse = AuthResponseDto.failure("Нэвтрэх нэр эсвэл нууц үг буруу");
            return ResponseEntity.status(401).body(errorResponse);
        }
    }

    /**
     * API Logout endpoint
     * POST /api/v1/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(@RequestHeader(value = "Authorization", required = false) String token) {
        log.info("🚪 API Logout");
        
        try {
            if (token != null) {
                // Bearer token-оос жинхэнэ token-ийг салгах
                String jwtToken = token.startsWith("Bearer ") ? token.substring(7) : token;
                authService.logoutUser(jwtToken);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Амжилттай гарлаа");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Logout error: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Гарахад алдаа гарлаа");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * Current user мэдээлэл авах
     * GET /api/v1/auth/me
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader(value = "Authorization", required = false) String token) {
        try {
            if (token == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Authorization header is missing"));
            }
            
            // Bearer token-оос жинхэнэ token-ийг салгах
            String jwtToken = token.startsWith("Bearer ") ? token.substring(7) : token;
            
            Optional<User> userOpt = authService.getCurrentUser(jwtToken);
            
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", user.getId().toString());
                userInfo.put("username", user.getUsername());
                userInfo.put("email", user.getEmail());
                userInfo.put("firstName", user.getFirstName());
                userInfo.put("lastName", user.getLastName());
                userInfo.put("fullName", user.getFullName());
                userInfo.put("roles", user.getRoles());
                userInfo.put("department", user.getDepartment());
                userInfo.put("position", user.getPosition());
                userInfo.put("isActive", user.getIsActive());
                userInfo.put("status", user.getStatus());
                userInfo.put("lastLoginAt", user.getLastLoginAt());
                
                return ResponseEntity.ok(userInfo);
            } else {
                return ResponseEntity.status(401).body(Map.of("error", "Invalid token"));
            }
        } catch (Exception e) {
            log.error("Get current user error: {}", e.getMessage(), e);
            return ResponseEntity.status(401).body(Map.of("error", "Invalid token"));
        }
    }

    /**
     * Token refresh endpoint
     * POST /api/v1/auth/refresh
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
        try {
            String refreshToken = request.get("refreshToken");
            if (refreshToken == null || refreshToken.trim().isEmpty()) {
                AuthResponseDto errorResponse = AuthResponseDto.failure("Refresh token шаардлагатай");
                return ResponseEntity.status(400).body(errorResponse);
            }
            
            AuthResponseDto response = authService.refreshToken(refreshToken);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(401).body(response);
            }
        } catch (Exception e) {
            log.error("Refresh token error: {}", e.getMessage(), e);
            AuthResponseDto errorResponse = AuthResponseDto.failure("Token сэргээхэд алдаа гарлаа");
            return ResponseEntity.status(401).body(errorResponse);
        }
    }

    /**
     * Token validation endpoint
     * GET /api/v1/auth/validate
     */
    @GetMapping("/validate")
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
            } else {
                response.put("message", "Token хүчингүй");
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Token validation error: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("valid", false);
            response.put("message", "Token шалгахад алдаа гарлаа");
            return ResponseEntity.ok(response);
        }
    }

    /**
     * Health check endpoint
     * GET /api/v1/auth/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "Authentication Service");
        health.put("timestamp", java.time.LocalDateTime.now());
        health.put("version", "1.0.0");
        return ResponseEntity.ok(health);
    }
}