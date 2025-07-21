package com.company.los.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class HealthController {
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "LOS Backend");
        response.put("version", "1.0.0");
        response.put("java.version", System.getProperty("java.version"));
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping({"/", ""})
    public ResponseEntity<Map<String, String>> home() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Зээлийн хүсэлтийн системд тавтай морил");
        response.put("status", "Running");
        response.put("api-docs", "/los/swagger-ui.html");
        response.put("h2-console", "/los/h2-console");
        return ResponseEntity.ok(response);
    }
}
