package com.company.los.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/documents")
@CrossOrigin(origins = {"http://localhost:3001", "http://localhost:3000"})
public class DocumentController {

    /**
     * Бүх баримт авах
     * GET /api/v1/documents
     */
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllDocuments() {
        System.out.println("📄 Getting all documents");
        
        List<Map<String, Object>> documents = new ArrayList<>();
        
        return ResponseEntity.ok(documents);
    }

    /**
     * Тодорхой баримт авах
     * GET /api/v1/documents/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getDocument(@PathVariable String id) {
        System.out.println("📄 Getting document: " + id);
        
        Map<String, Object> document = new HashMap<>();
        document.put("id", id);
        document.put("name", "Sample Document " + id);
        document.put("type", "PDF");
        document.put("status", "PENDING");
        document.put("uploadedAt", LocalDateTime.now().toString());
        document.put("size", "1.2MB");
        
        return ResponseEntity.ok(document);
    }

    /**
     * Баримт байршуулах
     * POST /api/v1/documents/upload
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "customerId", required = false) String customerId,
            @RequestParam(value = "documentType", required = false) String documentType) {
        
        System.out.println("📤 Uploading document: " + file.getOriginalFilename());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Баримт амжилттай байршуулагдлаа");
        response.put("id", "doc-" + System.currentTimeMillis());
        response.put("filename", file.getOriginalFilename());
        response.put("size", file.getSize());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Харилцагчийн баримтууд авах
     * GET /api/v1/documents/customer/{customerId}
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Map<String, Object>>> getDocumentsByCustomer(@PathVariable String customerId) {
        System.out.println("📋 Getting documents for customer: " + customerId);
        
        List<Map<String, Object>> documents = new ArrayList<>();
        
        return ResponseEntity.ok(documents);
    }
}