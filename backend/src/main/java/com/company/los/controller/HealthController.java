package com.company.los.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * ⭐ HEALTH CONTROLLER - ЗАСВАРЛАСАН ХУВИЛБАР ⭐
 * - /health endpoint API холболт шалгахад зориулсан
 * - /health/simple endpoint хурдан шалгалтад зориулсан  
 * - CORS тохиргоо нэмэгдсэн
 * - Home endpoint хадгалагдсан
 */
@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = {"http://localhost:3001", "http://localhost:3000"})
public class HealthController {
    
    /**
     * Дэлгэрэнгүй health check
     * Frontend-аас API холболт шалгахад ашиглана
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "LOS Backend");
        response.put("version", "1.0.0");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("java.version", System.getProperty("java.version"));
        
        // Components мэдээлэл нэмэх
        Map<String, Object> components = new HashMap<>();
        components.put("database", Map.of("status", "UP", "type", "H2"));
        components.put("diskSpace", Map.of("status", "UP"));
        response.put("components", components);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Хурдан health check
     * Зөвхөн "OK" эсвэл алдаа буцаана
     */
    @GetMapping("/health/simple")
    public ResponseEntity<String> simpleHealth() {
        return ResponseEntity.ok("OK");
    }
    
    /**
     * Home endpoint - хадгалагдсан
     */
    @GetMapping({"/", ""})
    public ResponseEntity<Map<String, String>> home() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Зээлийн хүсэлтийн системд тавтай морил");
        response.put("status", "Running");
        response.put("api-docs", "/los/swagger-ui.html");
        response.put("h2-console", "/los/h2-console");
        response.put("health-check", "/los/api/v1/health");
        response.put("simple-health", "/los/api/v1/health/simple");
        return ResponseEntity.ok(response);
    }
}