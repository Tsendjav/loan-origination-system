package com.company.los.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/loan-applications")  
@CrossOrigin(origins = {"http://localhost:3001", "http://localhost:3000"})
public class LoanApplicationController {

    /**
     * Бүх зээлийн хүсэлт авах
     * GET /api/v1/loan-applications
     */
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllLoanApplications() {
        System.out.println("💰 Getting all loan applications");
        
        List<Map<String, Object>> loanApplications = new ArrayList<>();
        
        return ResponseEntity.ok(loanApplications);
    }

    /**
     * Тодорхой зээлийн хүсэлт авах
     * GET /api/v1/loan-applications/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getLoanApplication(@PathVariable String id) {
        System.out.println("💰 Getting loan application: " + id);
        
        Map<String, Object> loanApp = new HashMap<>();
        loanApp.put("id", id);
        loanApp.put("customerId", "customer-123");
        loanApp.put("amount", 10000000.0);
        loanApp.put("currency", "MNT");
        loanApp.put("status", "PENDING");
        loanApp.put("purpose", "Жишээ зорилго");
        loanApp.put("term", 12); // months
        loanApp.put("appliedAt", LocalDateTime.now().toString());
        
        return ResponseEntity.ok(loanApp);
    }

    /**
     * Шинэ зээлийн хүсэлт үүсгэх
     * POST /api/v1/loan-applications
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createLoanApplication(@RequestBody Map<String, Object> applicationData) {
        System.out.println("➕ Creating new loan application");
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Зээлийн хүсэлт амжилттай үүсгэгдлээ");
        response.put("id", "loan-" + System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Харилцагчийн зээлийн хүсэлтүүд авах
     * GET /api/v1/loan-applications/customer/{customerId}
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Map<String, Object>>> getLoanApplicationsByCustomer(@PathVariable String customerId) {
        System.out.println("📋 Getting loan applications for customer: " + customerId);
        
        List<Map<String, Object>> loanApplications = new ArrayList<>();
        
        return ResponseEntity.ok(loanApplications);
    }
}