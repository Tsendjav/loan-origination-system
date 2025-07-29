package com.company.los.controller;

import com.company.los.dto.LoanApplicationDto;
import com.company.los.dto.CreateLoanRequestDto;
import com.company.los.enums.LoanStatus;
import com.company.los.service.LoanApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.math.BigDecimal;

/**
 * Зээлийн хүсэлтийн REST API Controller - Сайжруулсан хувилбар
 * Loan Application REST API Controller - Enhanced Version
 * 
 * Байршил: backend/src/main/java/com/company/los/controller/LoanApplicationController.java
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/loan-applications")
@CrossOrigin(origins = {"http://localhost:3001", "http://localhost:3000", "http://127.0.0.1:3001", "http://127.0.0.1:3000"})
@RequiredArgsConstructor
@Tag(name = "Loan Application Management", description = "Зээлийн хүсэлтийн удирдлагын API")
public class LoanApplicationController {

    private final LoanApplicationService loanApplicationService;

    /**
     * Бүх зээлийн хүсэлтийн жагсаалт
     * GET /api/v1/loan-applications
     */
    @GetMapping
    @Operation(summary = "Зээлийн хүсэлтүүд", description = "Бүх зээлийн хүсэлтийн жагсаалт авах")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Амжилттай"),
        @ApiResponse(responseCode = "500", description = "Серверийн алдаа")
    })
    //@PreAuthorize("hasAuthority('loan:view')")
    public ResponseEntity<ResponseWrapper<Page<LoanApplicationDto>>> getAllLoanApplications(
            @RequestParam(defaultValue = "0") @Parameter(description = "Хуудасны дугаар") int page,
            @RequestParam(defaultValue = "20") @Parameter(description = "Хуудсан дахь элементийн тоо") int size,
            @RequestParam(required = false) @Parameter(description = "Статус шүүлтүүр") String status,
            @RequestParam(required = false) @Parameter(description = "Харилцагчийн ID") UUID customerId) {
        
        log.debug("💰 Getting loan applications - page: {}, size: {}, status: {}, customerId: {}", 
                page, size, status, customerId);
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<LoanApplicationDto> applications;
            
            if (customerId != null) {
                applications = loanApplicationService.getLoanApplicationsByCustomer(customerId, pageable);
            } else if (status != null && !status.trim().isEmpty()) {
                // String-ээс LoanStatus enum руу хөрвүүлэх
                LoanStatus loanStatus = LoanStatus.valueOf(status.toUpperCase());
                applications = loanApplicationService.getLoanApplicationsByStatus(loanStatus, pageable);
            } else {
                applications = loanApplicationService.getAllLoanApplications(pageable);
            }
            
            log.info("✅ Successfully retrieved {} loan applications", applications.getTotalElements());
            return ResponseEntity.ok(ResponseWrapper.success(applications));
            
        } catch (IllegalArgumentException e) {
            log.error("❌ Invalid status parameter: {}", status);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ResponseWrapper.error("Буруу статус: " + status));
        } catch (Exception e) {
            log.error("❌ Error getting loan applications: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseWrapper.error("Зээлийн хүсэлт авахад алдаа гарлаа: " + e.getMessage()));
        }
    }

    /**
     * Тодорхой зээлийн хүсэлт авах
     * GET /api/v1/loan-applications/{id}
     */
    @GetMapping("/{id}")
    @Operation(summary = "Зээлийн хүсэлтийн мэдээлэл", description = "ID-гаар зээлийн хүсэлтийн дэлгэрэнгүй мэдээлэл авах")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Амжилттай"),
        @ApiResponse(responseCode = "404", description = "Зээлийн хүсэлт олдсонгүй")
    })
    //@PreAuthorize("hasAuthority('loan:view')")
    public ResponseEntity<ResponseWrapper<LoanApplicationDto>> getLoanApplication(
            @PathVariable @Parameter(description = "Зээлийн хүсэлтийн ID") UUID id) {
        
        log.debug("🔍 Getting loan application: {}", id);
        
        try {
            LoanApplicationDto application = loanApplicationService.getLoanApplicationById(id);
            log.info("✅ Successfully retrieved loan application: {}", id);
            return ResponseEntity.ok(ResponseWrapper.success(application));
            
        } catch (IllegalArgumentException e) {
            log.warn("⚠️ Loan application not found: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ResponseWrapper.error("Зээлийн хүсэлт олдсонгүй"));
        } catch (Exception e) {
            log.error("❌ Error getting loan application {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseWrapper.error("Системийн алдаа гарлаа"));
        }
    }

    /**
     * Шинэ зээлийн хүсэлт үүсгэх
     * POST /api/v1/loan-applications
     */
    @PostMapping
    @Operation(summary = "Шинэ зээлийн хүсэлт", description = "Шинэ зээлийн хүсэлт үүсгэх")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Амжилттай үүсгэгдсэн"),
        @ApiResponse(responseCode = "400", description = "Буруу мэдээлэл"),
        @ApiResponse(responseCode = "404", description = "Харилцагч олдсонгүй")
    })
    //@PreAuthorize("hasAuthority('loan:create')")
    public ResponseEntity<ResponseWrapper<LoanApplicationDto>> createLoanApplication(
            @Valid @RequestBody CreateLoanRequestDto loanRequest) {
        
        log.info("➕ Creating new loan application for customer: {}", loanRequest.getCustomerId());
        
        try {
            LoanApplicationDto createdApplication = loanApplicationService.createLoanApplication(loanRequest);
            log.info("✅ Loan application created successfully: {}", createdApplication.getId());
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseWrapper.success(createdApplication, "Зээлийн хүсэлт амжилттай үүсгэгдлээ"));
                
        } catch (IllegalArgumentException e) {
            log.warn("⚠️ Business validation error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ResponseWrapper.error(e.getMessage()));
        } catch (Exception e) {
            log.error("❌ Error creating loan application: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseWrapper.error("Зээлийн хүсэлт үүсгэхэд алдаа гарлаа"));
        }
    }

    /**
     * Зээлийн хүсэлтийн статус шинэчлэх
     * PUT /api/v1/loan-applications/{id}/status
     */
    @PutMapping("/{id}/status")
    @Operation(summary = "Статус шинэчлэх", description = "Зээлийн хүсэлтийн статус шинэчлэх")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Амжилттай шинэчлэгдсэн"),
        @ApiResponse(responseCode = "404", description = "Зээлийн хүсэлт олдсонгүй"),
        @ApiResponse(responseCode = "400", description = "Буруу статус")
    })
    //@PreAuthorize("hasAuthority('loan:update')")
    public ResponseEntity<ResponseWrapper<LoanApplicationDto>> updateStatus(
            @PathVariable @Parameter(description = "Зээлийн хүсэлтийн ID") UUID id,
            @RequestParam @Parameter(description = "Шинэ статус") String status,
            @RequestParam(required = false) @Parameter(description = "Тайлбар") String comment) {
        
        log.info("📊 Updating loan application status: {} -> {}", id, status);
        
        try {
            // String-ээс LoanStatus enum руу хөрвүүлэх
            LoanStatus loanStatus = LoanStatus.valueOf(status.toUpperCase());
            LoanApplicationDto updatedApplication = loanApplicationService.updateLoanApplicationStatus(id, loanStatus);
            log.info("✅ Loan application status updated: {}", id);
            
            return ResponseEntity.ok(ResponseWrapper.success(updatedApplication, "Статус амжилттай шинэчлэгдлээ"));
            
        } catch (IllegalArgumentException e) {
            log.warn("⚠️ Loan application not found or invalid status: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ResponseWrapper.error(e.getMessage()));
        } catch (Exception e) {
            log.error("❌ Error updating loan application status {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseWrapper.error("Статус шинэчлэхэд алдаа гарлаа"));
        }
    }

    /**
     * Зээлийн хүсэлт зөвшөөрөх
     * PUT /api/v1/loan-applications/{id}/approve
     */
    @PutMapping("/{id}/approve")
    @Operation(summary = "Зээл зөвшөөрөх", description = "Зээлийн хүсэлтийг зөвшөөрөх")
    //@PreAuthorize("hasAuthority('loan:approve')")
    public ResponseEntity<ResponseWrapper<LoanApplicationDto>> approveLoan(
            @PathVariable UUID id,
            @RequestParam(required = false) String comment,
            @RequestParam(required = false) Double approvedAmount) {
        
        log.info("✅ Approving loan application: {}", id);
        
        try {
            // BigDecimal руу хөрвүүлэх, хэрэв approvedAmount null бол анхны дүнг ашиглана
            BigDecimal amount = approvedAmount != null ? BigDecimal.valueOf(approvedAmount) : null;
            LoanApplicationDto approvedApplication = loanApplicationService.approveLoanApplication(
                id, amount, null, null, comment != null ? comment : "Зээл зөвшөөрөгдлөө");
            log.info("✅ Loan application approved: {}", id);
            
            return ResponseEntity.ok(ResponseWrapper.success(approvedApplication, "Зээлийн хүсэлт зөвшөөрөгдлөө"));
            
        } catch (IllegalArgumentException e) {
            log.warn("⚠️ Cannot approve loan application: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ResponseWrapper.error(e.getMessage()));
        } catch (Exception e) {
            log.error("❌ Error approving loan application {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseWrapper.error("Зээл зөвшөөрөхөд алдаа гарлаа"));
        }
    }

    /**
     * Зээлийн хүсэлт татгалзах
     * PUT /api/v1/loan-applications/{id}/reject
     */
    @PutMapping("/{id}/reject")
    @Operation(summary = "Зээл татгалзах", description = "Зээлийн хүсэлтийг татгалзах")
    //@PreAuthorize("hasAuthority('loan:approve')")
    public ResponseEntity<ResponseWrapper<LoanApplicationDto>> rejectLoan(
            @PathVariable UUID id,
            @RequestParam String reason) {
        
        log.info("❌ Rejecting loan application: {}", id);
        
        try {
            LoanApplicationDto rejectedApplication = loanApplicationService.rejectLoanApplication(id, reason);
            log.info("❌ Loan application rejected: {}", id);
            
            return ResponseEntity.ok(ResponseWrapper.success(rejectedApplication, "Зээлийн хүсэлт татгалзагдлаа"));
            
        } catch (IllegalArgumentException e) {
            log.warn("⚠️ Cannot reject loan application: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ResponseWrapper.error(e.getMessage()));
        } catch (Exception e) {
            log.error("❌ Error rejecting loan application {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseWrapper.error("Зээл татгалзахад алдаа гарлаа"));
        }
    }

    /**
     * Харилцагчийн зээлийн хүсэлтүүд
     * GET /api/v1/loan-applications/customer/{customerId}
     */
    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Харилцагчийн зээлүүд", description = "Тодорхой харилцагчийн бүх зээлийн хүсэлт")
    //@PreAuthorize("hasAuthority('loan:view')")
    public ResponseEntity<ResponseWrapper<Page<LoanApplicationDto>>> getLoanApplicationsByCustomer(
            @PathVariable @Parameter(description = "Харилцагчийн ID") UUID customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.debug("📋 Getting loan applications for customer: {}", customerId);
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<LoanApplicationDto> applications = loanApplicationService.getLoanApplicationsByCustomer(customerId, pageable);
            
            return ResponseEntity.ok(ResponseWrapper.success(applications));
        } catch (Exception e) {
            log.error("❌ Error getting loan applications for customer {}: {}", customerId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseWrapper.error("Харилцагчийн зээлийн хүсэлт авахад алдаа гарлаа"));
        }
    }

    /**
     * Зээлийн хүсэлтийн статистик
     * GET /api/v1/loan-applications/statistics
     */
    @GetMapping("/statistics")
    @Operation(summary = "Зээлийн статистик", description = "Зээлийн хүсэлтийн ерөнхий статистик")
    //@PreAuthorize("hasAuthority('loan:view')")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> getStatistics() {
        log.debug("📊 Getting loan application statistics");
        
        try {
            Map<String, Object> statistics = loanApplicationService.getLoanApplicationStatistics();
            return ResponseEntity.ok(ResponseWrapper.success(statistics));
        } catch (Exception e) {
            log.error("❌ Error getting loan statistics: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseWrapper.error("Статистик авахад алдаа гарлаа"));
        }
    }

    /**
     * Зээлийн хүсэлт хайх
     * GET /api/v1/loan-applications/search
     */
    @GetMapping("/search")
    @Operation(summary = "Зээлийн хүсэлт хайх", description = "Өгөгдсөн хайлтын үгээр зээлийн хүсэлт хайх")
    //@PreAuthorize("hasAuthority('loan:view')")
    public ResponseEntity<ResponseWrapper<Page<LoanApplicationDto>>> searchLoanApplications(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.debug("🔍 Searching loan applications with query: {}", q);
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<LoanApplicationDto> applications;
            
            if (q == null || q.trim().isEmpty()) {
                applications = loanApplicationService.getAllLoanApplications(pageable);
            } else {
                applications = loanApplicationService.searchLoanApplications(q.trim(), pageable);
            }
            
            return ResponseEntity.ok(ResponseWrapper.success(applications));
        } catch (Exception e) {
            log.error("❌ Error searching loan applications: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseWrapper.error("Зээлийн хүсэлт хайхад алдаа гарлаа"));
        }
    }

    /**
     * Зээлийн дүн тооцоолох
     * POST /api/v1/loan-applications/calculate
     */
    @PostMapping("/calculate")
    @Operation(summary = "Зээлийн тооцоо", description = "Зээлийн сарын төлбөр болон нийт дүн тооцоолох")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> calculateLoan(
            @RequestParam @Parameter(description = "Зээлийн дүн") Double amount,
            @RequestParam @Parameter(description = "Хугацаа (сар)") Integer termInMonths,
            @RequestParam(required = false, defaultValue = "12.0") @Parameter(description = "Хүүгийн хувь") Double interestRate) {
        
        log.debug("🧮 Calculating loan: amount={}, term={}, rate={}", amount, termInMonths, interestRate);
        
        try {
            // BigDecimal руу хөрвүүлэх
            BigDecimal principal = BigDecimal.valueOf(amount);
            BigDecimal rate = BigDecimal.valueOf(interestRate / 100); // Хувиас decimal руу
            
            // Тооцоолол хийх
            BigDecimal monthlyPayment = loanApplicationService.calculateMonthlyPayment(principal, termInMonths, rate);
            BigDecimal totalPayment = loanApplicationService.calculateTotalPayment(principal, termInMonths, rate);
            BigDecimal totalInterest = loanApplicationService.calculateTotalInterest(principal, termInMonths, rate);
            
            // Амортизацийн хүснэгт үүсгэх
            List<Map<String, Object>> schedule = loanApplicationService.generateAmortizationSchedule(principal, termInMonths, rate);
            
            // Үр дүнг Map-д хуваалцах
            Map<String, Object> calculation = new HashMap<>();
            calculation.put("principal", amount);
            calculation.put("termInMonths", termInMonths);
            calculation.put("interestRate", interestRate);
            calculation.put("monthlyPayment", monthlyPayment);
            calculation.put("totalPayment", totalPayment);
            calculation.put("totalInterest", totalInterest);
            calculation.put("amortizationSchedule", schedule);
            
            return ResponseEntity.ok(ResponseWrapper.success(calculation));
        } catch (Exception e) {
            log.error("❌ Error calculating loan: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseWrapper.error("Зээлийн тооцоо хийхэд алдаа гарлаа"));
        }
    }

    /**
     * API хариу wrapper класс
     * Swagger-ийн ApiResponse-тай зөрчлөөс зайлсхийхийн тулд ResponseWrapper гэж нэрлэв
     */
    public static class ResponseWrapper<T> {
        private boolean success;
        private T data;
        private String message;
        private String error;
        private Map<String, Object> meta;
        private long timestamp;

        public ResponseWrapper() {
            this.timestamp = System.currentTimeMillis();
        }

        public ResponseWrapper(boolean success, T data, String message) {
            this();
            this.success = success;
            this.data = data;
            this.message = message;
        }

        public static <T> ResponseWrapper<T> success(T data) {
            return new ResponseWrapper<>(true, data, null);
        }

        public static <T> ResponseWrapper<T> success(T data, String message) {
            return new ResponseWrapper<>(true, data, message);
        }

        public static <T> ResponseWrapper<T> error(String error) {
            ResponseWrapper<T> response = new ResponseWrapper<>();
            response.success = false;
            response.error = error;
            return response;
        }

        public static <T> ResponseWrapper<T> error(String error, Map<String, Object> meta) {
            ResponseWrapper<T> response = error(error);
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