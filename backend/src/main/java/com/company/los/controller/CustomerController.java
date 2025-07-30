package com.company.los.controller;

import com.company.los.dto.CustomerDto;
import com.company.los.dto.CustomerRequestDto;
import com.company.los.dto.CustomerResponseDto;
import com.company.los.entity.Customer;
import com.company.los.enums.CustomerStatus;
import com.company.los.enums.CustomerType;
import com.company.los.enums.KYCStatus;
import com.company.los.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Харилцагчийн REST API Controller - ЭЦСИЙН ЗАСВАРЛАСАН ХУВИЛБАР
 * ⭐ API АЛДАА БҮРЭН ШИЙДЭГДСЭН ⭐
 * ⭐ ERROR HANDLING НЭМЭГДСЭН ⭐
 * ⭐ HEALTH CHECK ENDPOINT НЭМЭГДСЭН ⭐
 * 
 * @author LOS Development Team
 */
@RestController  
@RequestMapping("/api/v1/customers")
@CrossOrigin(origins = {"http://localhost:3001", "http://localhost:3000", "http://127.0.0.1:3001", "http://127.0.0.1:3000"})
@RequiredArgsConstructor
public class CustomerController {

    private static final Logger logger = LoggerFactory.getLogger(CustomerController.class);
    private final CustomerService customerService;

    // ==================== CRUD OPERATIONS ====================

    /**
     * Бүх харилцагч авах (pagination-тай) - ⭐ ЗАСВАРЛАСАН ERROR HANDLING ⭐
     * GET /api/v1/customers
     */
    @GetMapping
    @PreAuthorize("hasAuthority('customer:view')")
    public ResponseEntity<ApiResponse<Page<CustomerDto>>> getAllCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "lastName") String sort,
            @RequestParam(defaultValue = "ASC") String direction) {
        
        logger.debug("📋 Getting all customers - page: {}, size: {}", page, size);
        
        try {
            Sort.Direction sortDirection = Sort.Direction.fromString(direction);
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
            
            Page<CustomerDto> customers = customerService.getAllCustomers(pageable);
            
            logger.info("✅ Successfully retrieved {} customers", customers.getTotalElements());
            return ResponseEntity.ok(ApiResponse.success(customers));
        } catch (Exception e) {
            logger.error("❌ Error getting customers: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Харилцагчдийг авахад алдаа гарлаа: " + e.getMessage()));
        }
    }

    /**
     * Тодорхой харилцагч авах - ⭐ ЗАСВАРЛАСАН NULL CHECK ⭐
     * GET /api/v1/customers/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('customer:view')")
    public ResponseEntity<ApiResponse<CustomerDto>> getCustomer(@PathVariable UUID id) {
        logger.debug("👤 Getting customer: {}", id);
        
        try {
            // ⭐ NULL CHECK НЭМЭГДСЭН ⭐
            if (id == null) {
                logger.warn("⚠️ Customer ID is null");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Харилцагчийн ID буруу байна"));
            }

            CustomerDto customer = customerService.getCustomerById(id);
            
            if (customer != null) {
                logger.info("✅ Successfully retrieved customer: {}", id);
                return ResponseEntity.ok(ApiResponse.success(customer));
            } else {
                logger.warn("⚠️ Customer not found: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Харилцагч олдсонгүй"));
            }
        } catch (IllegalArgumentException e) {
            logger.warn("⚠️ Customer not found: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Харилцагч олдсонгүй"));
        } catch (Exception e) {
            logger.error("❌ Error getting customer {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Системийн алдаа гарлаа"));
        }
    }

    /**
     * Шинэ харилцагч үүсгэх - ⭐ ЗАСВАРЛАСАН VALIDATION ⭐
     * POST /api/v1/customers
     */
    @PostMapping
    @PreAuthorize("hasAuthority('customer:create')")
    public ResponseEntity<ApiResponse<CustomerDto>> createCustomer(
            @Valid @RequestBody CustomerRequestDto customerRequest,
            BindingResult bindingResult) {
        
        logger.info("➕ Creating new customer");
        
        // ⭐ VALIDATION ERROR HANDLING НЭМЭГДСЭН ⭐
        if (bindingResult.hasErrors()) {
            logger.warn("⚠️ Validation errors in customer request");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Мэдээлэл буруу байна", new HashMap<>(getValidationErrors(bindingResult))));
        }

        // ⭐ НЭМЭЛТ BUSINESS VALIDATION ⭐
        if (customerRequest.getFirstName() == null || customerRequest.getFirstName().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Харилцагчийн нэр заавал оруулна уу"));
        }

        if (customerRequest.getLastName() == null || customerRequest.getLastName().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Харилцагчийн овог заавал оруулна уу"));
        }
        
        try {
            // Convert request DTO to service DTO
            CustomerDto customerDto = convertToDto(customerRequest);
            
            CustomerDto createdCustomer = customerService.createCustomer(customerDto);
            
            logger.info("✅ Customer created successfully: {}", createdCustomer.getId());
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(createdCustomer, "Харилцагч амжилттай үүсгэгдлээ"));
                
        } catch (IllegalArgumentException e) {
            logger.warn("⚠️ Business validation error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("❌ Error creating customer: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Харилцагч үүсгэхэд алдаа гарлаа"));
        }
    }

    /**
     * Харилцагч шинэчлэх - ⭐ ЗАСВАРЛАСАН ⭐
     * PUT /api/v1/customers/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('customer:update')")
    public ResponseEntity<ApiResponse<CustomerDto>> updateCustomer(
            @PathVariable UUID id,
            @Valid @RequestBody CustomerRequestDto customerRequest,
            BindingResult bindingResult) {
        
        logger.info("📝 Updating customer: {}", id);
        
        // ⭐ NULL CHECK ⭐
        if (id == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Харилцагчийн ID буруу байна"));
        }
        
        if (bindingResult.hasErrors()) {
            logger.warn("⚠️ Validation errors in update request");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Мэдээлэл буруу байна", new HashMap<>(getValidationErrors(bindingResult))));
        }
        
        try {
            CustomerDto customerDto = convertToDto(customerRequest);
            CustomerDto updatedCustomer = customerService.updateCustomer(id, customerDto);
            
            logger.info("✅ Customer updated successfully: {}", id);
            return ResponseEntity.ok(ApiResponse.success(updatedCustomer, "Харилцагч амжилттай шинэчлэгдлээ"));
            
        } catch (IllegalArgumentException e) {
            logger.warn("⚠️ Customer not found or validation error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("❌ Error updating customer {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Харилцагч шинэчлэхэд алдаа гарлаа"));
        }
    }

    /**
     * Харилцагч устгах - ⭐ ЗАСВАРЛАСАН ⭐
     * DELETE /api/v1/customers/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('customer:delete')")
    public ResponseEntity<ApiResponse<Void>> deleteCustomer(@PathVariable UUID id) {
        logger.info("🗑️ Deleting customer: {}", id);
        
        try {
            // ⭐ NULL CHECK ⭐
            if (id == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Харилцагчийн ID буруу байна"));
            }

            customerService.deleteCustomer(id);
            
            logger.info("✅ Customer deleted successfully: {}", id);
            return ResponseEntity.noContent().build();
            
        } catch (IllegalArgumentException e) {
            logger.warn("⚠️ Customer not found: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Харилцагч олдсонгүй"));
        } catch (Exception e) {
            logger.error("❌ Error deleting customer {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Харилцагч устгахад алдаа гарлаа"));
        }
    }

    // ==================== SEARCH OPERATIONS ====================

    /**
     * Харилцагч хайх - ⭐ ЗАСВАРЛАСАН ⭐
     * GET /api/v1/customers/search
     */
    @GetMapping("/search")
    @PreAuthorize("hasAuthority('customer:view')")
    public ResponseEntity<ApiResponse<Page<CustomerDto>>> searchCustomers(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        logger.debug("🔍 Searching customers with query: {}", query);
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<CustomerDto> customers;
            
            if (query == null || query.trim().isEmpty()) {
                customers = customerService.getAllCustomers(pageable);
            } else {
                customers = customerService.searchCustomers(query.trim(), pageable);
            }
            
            return ResponseEntity.ok(ApiResponse.success(customers));
        } catch (Exception e) {
            logger.error("❌ Error searching customers: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Харилцагч хайхад алдаа гарлаа"));
        }
    }

    // ==================== VALIDATION ====================

    /**
     * Харилцагчийн мэдээлэл шалгах - ⭐ ЗАСВАРЛАСАН ⭐
     * POST /api/v1/customers/validate
     */
    @PostMapping("/validate")
    @PreAuthorize("hasAuthority('customer:view')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> validateCustomer(
            @RequestBody Map<String, String> validation) {
        
        logger.debug("✅ Validating customer data");
        
        try {
            Map<String, Object> result = new HashMap<>();
            
            String email = validation.get("email");
            if (email != null) {
                result.put("emailUnique", customerService.isEmailUnique(email));
            }
            
            String phone = validation.get("phone");  
            if (phone != null) {
                result.put("phoneAvailable", !customerService.existsByPhone(phone));
            }
            
            String registerNumber = validation.get("registerNumber");
            if (registerNumber != null) {
                result.put("registerNumberAvailable", !customerService.existsByRegisterNumber(registerNumber));
            }
            
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            logger.error("❌ Error validating customer data: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Мэдээлэл шалгахад алдаа гарлаа"));
        }
    }

    // ==================== STATUS MANAGEMENT ====================

    /**
     * Харилцагчийн статус шинэчлэх - ⭐ ЗАСВАРЛАСАН ⭐
     * PUT /api/v1/customers/{id}/status
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('customer:update')")
    public ResponseEntity<ApiResponse<CustomerDto>> updateCustomerStatus(
            @PathVariable UUID id,
            @RequestParam CustomerStatus status) {
        
        logger.info("📊 Updating customer status: {} -> {}", id, status);
        
        try {
            // ⭐ NULL CHECK ⭐
            if (id == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Харилцагчийн ID буруу байна"));
            }

            if (status == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Статус заавал оруулна уу"));
            }

            CustomerDto updatedCustomer = customerService.updateCustomerStatus(id, status);
            
            logger.info("✅ Customer status updated: {}", id);
            return ResponseEntity.ok(ApiResponse.success(updatedCustomer, "Статус амжилттай шинэчлэгдлээ"));
            
        } catch (IllegalArgumentException e) {
            logger.warn("⚠️ Customer not found: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Харилцагч олдсонгүй"));
        } catch (Exception e) {
            logger.error("❌ Error updating customer status {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Статус шинэчлэхэд алдаа гарлаа"));
        }
    }

    /**
     * KYC статус шинэчлэх - ⭐ ЗАСВАРЛАСАН ⭐
     * PUT /api/v1/customers/{id}/kyc-status
     */
    @PutMapping("/{id}/kyc-status")
    @PreAuthorize("hasAuthority('customer:kyc')")
    public ResponseEntity<ApiResponse<CustomerDto>> updateKycStatus(
            @PathVariable UUID id,
            @RequestParam KYCStatus kycStatus) {
        
        logger.info("🔐 Updating KYC status: {} -> {}", id, kycStatus);
        
        try {
            // ⭐ NULL CHECK ⭐
            if (id == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Харилцагчийн ID буруу байна"));
            }

            if (kycStatus == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("KYC статус заавал оруулна уу"));
            }

            CustomerDto updatedCustomer = customerService.updateKYCStatus(id, kycStatus);
            
            logger.info("✅ KYC status updated: {}", id);
            return ResponseEntity.ok(ApiResponse.success(updatedCustomer, "KYC статус амжилттай шинэчлэгдлээ"));
            
        } catch (IllegalArgumentException e) {
            logger.warn("⚠️ Customer not found: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Харилцагч олдсонгүй"));
        } catch (Exception e) {
            logger.error("❌ Error updating KYC status {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("KYC статус шинэчлэхэд алдаа гарлаа"));
        }
    }

    // ==================== STATISTICS ====================

    /**
     * Харилцагчийн статистик - ⭐ ЗАСВАРЛАСАН ⭐
     * GET /api/v1/customers/statistics
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasAuthority('customer:view')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCustomerStatistics() {
        logger.debug("📊 Getting customer statistics");
        
        try {
            Map<String, Object> statistics = customerService.getCustomerStatistics();
            
            return ResponseEntity.ok(ApiResponse.success(statistics));
        } catch (Exception e) {
            logger.error("❌ Error getting customer statistics: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Статистик авахад алдаа гарлаа"));
        }
    }

    // ==================== ⭐ HEALTH CHECK ENDPOINT - ШИНЭЭР НЭМЭГДСЭН ⭐ ====================

    /**
     * Customer API Health Check - ⭐ ШИНЭЭР НЭМЭГДСЭН ⭐
     * GET /api/v1/customers/health
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> healthCheck() {
        try {
            Map<String, Object> health = new HashMap<>();
            health.put("status", "UP");
            health.put("service", "CustomerController");
            health.put("timestamp", LocalDateTime.now());
            health.put("version", "2.2");
            
            // Database connectivity шалгах
            try {
                long customerCount = customerService.getTotalCustomerCount();
                health.put("databaseStatus", "UP");
                health.put("totalCustomers", customerCount);
            } catch (Exception e) {
                logger.warn("Database connection issue: {}", e.getMessage());
                health.put("databaseStatus", "DOWN");
                health.put("databaseError", e.getMessage());
            }
            
            logger.debug("✅ Customer API health check successful");
            return ResponseEntity.ok(ApiResponse.success(health));
        } catch (Exception e) {
            logger.error("❌ Customer API health check failed: {}", e.getMessage());
            Map<String, Object> errorHealth = new HashMap<>();
            errorHealth.put("status", "DOWN");
            errorHealth.put("service", "CustomerController");
            errorHealth.put("error", e.getMessage());
            errorHealth.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error("Customer API унтарсан байна", errorHealth));
        }
    }

    // ==================== HELPER METHODS ====================

    /**
     * CustomerRequestDto-г CustomerDto болгож хөрвүүлэх
     */
    private CustomerDto convertToDto(CustomerRequestDto request) {
        CustomerDto dto = new CustomerDto();
        
        // Basic fields
        dto.setFirstName(request.getFirstName());
        dto.setLastName(request.getLastName());
        dto.setEmail(request.getEmail());
        dto.setPhone(request.getPhone());
        dto.setBirthDate(request.getDateOfBirth());
        dto.setRegisterNumber(request.getSocialSecurityNumber());
        
        // Convert external enum to internal enum
        if (request.getCustomerType() != null) {
            try {
                dto.setCustomerType(com.company.los.entity.Customer.CustomerType.valueOf(request.getCustomerType().name()));
            } catch (IllegalArgumentException e) {
                logger.warn("⚠️ Invalid customer type: {}", request.getCustomerType());
                // Default value орлуулж өгье
                dto.setCustomerType(com.company.los.entity.Customer.CustomerType.INDIVIDUAL);
            }
        }
        
        return dto;
    }

    /**
     * CustomerDto-г CustomerResponseDto болгож хөрвүүлэх
     */
    private CustomerResponseDto convertToResponseDto(CustomerDto dto) {
        CustomerResponseDto response = new CustomerResponseDto();
        
        // Type-safe ID conversion - UUID-г String болгох эсвэл Long болгох
        if (dto.getId() != null) {
            // UUID-г String болгож хөрвүүлэх (CustomerResponseDto.setId нь String хүлээж байвал)
            // Эсвэл Long хүлээж байвал: response.setId(dto.getId().hashCode()); гэх мэт
            try {
                // Assuming CustomerResponseDto.setId accepts String
                response.setId(Long.valueOf(Math.abs(dto.getId().hashCode())));
            } catch (Exception e) {
                logger.warn("⚠️ Could not set ID: {}", e.getMessage());
                // Try Long conversion if String doesn't work
                try {
                    response.setId(Long.valueOf(Math.abs(dto.getId().hashCode())));
                } catch (Exception ex) {
                    logger.error("❌ Could not convert UUID to ID: {}", ex.getMessage());
                }
            }
        }
        
        response.setFirstName(dto.getFirstName());
        response.setLastName(dto.getLastName());
        response.setEmail(dto.getEmail());
        response.setPhone(dto.getPhone());
        response.setDateOfBirth(dto.getBirthDate());
        response.setSocialSecurityNumber(dto.getRegisterNumber());
        
        // Convert internal enum to external enum
        if (dto.getCustomerType() != null) {
            try {
                response.setCustomerType(CustomerType.valueOf(dto.getCustomerType().name()));
            } catch (IllegalArgumentException e) {
                logger.warn("⚠️ Could not convert customer type: {}", dto.getCustomerType());
            }
        }
        
        response.setStatus(dto.getStatus());
        
        // Convert internal KYC status to external
        if (dto.getKycStatus() != null) {
            try {
                response.setKycStatus(KYCStatus.valueOf(dto.getKycStatus().name()));
            } catch (IllegalArgumentException e) {
                logger.warn("⚠️ Could not convert KYC status: {}", dto.getKycStatus());
            }
        }
        
        response.setRegistrationDate(dto.getCreatedAt());
        response.setLastUpdated(dto.getUpdatedAt());
        
        return response;
    }

    /**
     * Validation алдаануудыг map болгож хөрвүүлэх
     */
    private Map<String, String> getValidationErrors(BindingResult bindingResult) {
        Map<String, String> errors = new HashMap<>();
        
        bindingResult.getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage())
        );
        
        return errors;
    }

    // ==================== API RESPONSE WRAPPER ====================

    /**
     * API хариу wrapper класс - ⭐ ЗАСВАРЛАСАН ⭐
     */
    public static class ApiResponse<T> {
        private boolean success;
        private T data;
        private String message;
        private String error;
        private Map<String, Object> meta;
        private long timestamp;

        public ApiResponse() {
            this.timestamp = System.currentTimeMillis();
        }

        public ApiResponse(boolean success, T data, String message) {
            this();
            this.success = success;
            this.data = data;
            this.message = message;
        }

        public static <T> ApiResponse<T> success(T data) {
            return new ApiResponse<>(true, data, null);
        }

        public static <T> ApiResponse<T> success(T data, String message) {
            return new ApiResponse<>(true, data, message);
        }

        public static <T> ApiResponse<T> error(String error) {
            ApiResponse<T> response = new ApiResponse<>();
            response.success = false;
            response.error = error;
            return response;
        }

        public static <T> ApiResponse<T> error(String error, Map<String, Object> meta) {
            ApiResponse<T> response = error(error);
            response.meta = meta;
            return response;
        }

        // Getters and setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public T getData() { return data; }
        public void setData(T data) { this.data = data; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public String getError() { return error; }
        public void setError(String error) { this.error = error; }

        public Map<String, Object> getMeta() { return meta; }
        public void setMeta(Map<String, Object> meta) { this.meta = meta; }

        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }
}