package com.company.los.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * ⭐ AUTH CONTROLLER - API AUTHENTICATION ⭐
 * - /api/v1/auth/login endpoint
 * - /api/v1/auth/logout endpoint
 * - /api/v1/auth/me endpoint
 * - CORS тохиргоотой
 * - SecurityConfig-тай уялдаатай
 */
@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = {"http://localhost:3001", "http://localhost:3000"})
public class AuthController {

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * API Login endpoint
     * POST /api/v1/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");
        
        System.out.println("🔐 API Login attempt: " + username);
        
        // Хөгжүүлэлтийн зориулалтаар энгийн шалгалт
        // Продакшн дээр UserDetailsService ашиглах хэрэгтэй
        // isValidUser функцэд LoginRequest объект дамжуулахын тулд түр зуурын LoginRequest үүсгэж байна.
        // Хэрэв та LoginRequest классыг үүсгээгүй бол энэ хэсгийг өөрчлөх шаардлагатай.
        LoginRequest loginRequest = new LoginRequest(username, password); // LoginRequest класс байхгүй бол үүсгэх эсвэл шууд username, password дамжуулах
        if (isValidUser(loginRequest)) { // isValidUser функцэд LoginRequest объект дамжуулж байна
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("token", "dummy-jwt-token-" + System.currentTimeMillis());
            response.put("user", createUserInfo(username));
            
            System.out.println("✅ API Login successful: " + username);
            return ResponseEntity.ok(response);
        } else {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Нэвтрэх нэр эсвэл нууц үг буруу");
            
            System.out.println("❌ API Login failed: " + username);
            return ResponseEntity.status(401).body(errorResponse);
        }
    }

    /**
     * API Logout endpoint
     * POST /api/v1/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout() {
        System.out.println("🚪 API Logout");
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Амжилттай гарлаа");
        return ResponseEntity.ok(response);
    }

    /**
     * Current user мэдээлэл авах
     * GET /api/v1/auth/me
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser() {
        // Продакшн дээр JWT token-оос user мэдээлэл авах хэрэгтэй
        // Одоо хөгжүүлэлтийн зориулалтаар default user буцаана
        Map<String, Object> user = createUserInfo("admin");
        return ResponseEntity.ok(user);
    }

    /**
     * User шалгах - хөгжүүлэлтийн зориулалт
     * Продакшн дээр UserDetailsService ашиглана
     */
    private boolean isValidUser(LoginRequest request) { // Параметрийг LoginRequest болгон өөрчилсөн
        // NULL CHECK нэмэх
        if (request == null || request.getUsername() == null || request.getPassword() == null) {
            return false;
        }
        
        // data.sql файлтай тохирсон credentials
        Map<String, String> defaultUsers = Map.of( // validUsers-ийг defaultUsers болгон өөрчилсөн
            "admin", "admin123",
            "loan_officer", "loan123", 
            "manager", "manager123"
        );
        
        return defaultUsers.containsKey(request.getUsername()) && 
               defaultUsers.get(request.getUsername()).equals(request.getPassword());
    }

    /**
     * User мэдээлэл үүсгэх
     */
    private Map<String, Object> createUserInfo(String username) {
        Map<String, Object> user = new HashMap<>();
        
        switch (username) {
            case "admin":
                user.put("id", "1");
                user.put("username", "admin");
                user.put("role", "SUPER_ADMIN");
                user.put("name", "Админ хэрэглэгч");
                break;
            case "loan_officer":
                user.put("id", "2");
                user.put("username", "loan_officer");
                user.put("role", "LOAN_OFFICER");
                user.put("name", "Зээлийн ажилтан");
                break;
            case "manager":
                user.put("id", "3");
                user.put("username", "manager");
                user.put("role", "MANAGER");
                user.put("name", "Менежер");
                break;
            default:
                user.put("id", "1");
                user.put("username", "admin");
                user.put("role", "SUPER_ADMIN");
                user.put("name", "Админ хэрэглэгч");
        }
        
        return user;
    }

    // LoginRequest классыг нэмсэн, хэрэв танд ийм класс байхгүй бол
    // эсвэл таны төсөлд өөр газар тодорхойлогдсон бол үүнийг хасах боломжтой.
    public static class LoginRequest {
        private String username;
        private String password;

        public LoginRequest(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
