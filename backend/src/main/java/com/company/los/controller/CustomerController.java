package com.company.los.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController  
@RequestMapping("/api/v1/customers")
@CrossOrigin(origins = {"http://localhost:3001", "http://localhost:3000"})
public class CustomerController {

    /**
     * Бүх харилцагч авах
     * GET /api/v1/customers
     */
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllCustomers() {
        System.out.println("📋 Getting all customers");
        
        // Хөгжүүлэлтийн зориулалтаар хоосон жагсаалт буцаана
        // Дараа нь database-аас бодит өгөгдөл авна
        List<Map<String, Object>> customers = new ArrayList<>();
        
        return ResponseEntity.ok(customers);
    }

    /**
     * Тодорхой харилцагч авах
     * GET /api/v1/customers/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getCustomer(@PathVariable String id) {
        System.out.println("👤 Getting customer: " + id);
        
        Map<String, Object> customer = new HashMap<>();
        customer.put("id", id);
        customer.put("name", "Sample Customer " + id);
        customer.put("email", "customer" + id + "@example.com");
        customer.put("phone", "+976-1234-5678");
        customer.put("createdAt", LocalDateTime.now().toString());
        customer.put("status", "ACTIVE");
        
        return ResponseEntity.ok(customer);
    }

    /**
     * Шинэ харилцагч үүсгэх
     * POST /api/v1/customers
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createCustomer(@RequestBody Map<String, Object> customerData) {
        System.out.println("➕ Creating new customer");
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Харилцагч амжилттай үүсгэгдлээ");
        response.put("id", "customer-" + System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }
}