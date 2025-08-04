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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ⭐ ЗАСВАРЛАСАН LOAN APPLICATION CONTROLLER - ЭЦСИЙН ХУВИЛБАР ⭐
 * 
 * ЗАСВАРУУД:
 * ✅ Service method signatures тохируулсан
 * ✅ Exception handling засварласан  
 * ✅ Validation logic нэмэгдсэн
 * ✅ Response format нийцүүлсэн
 * ✅ Field naming consistency засварласан
 * ✅ Error messages англи хэл дээр
 * 
 * @author LOS Development Team
 * @version 9.0 - COMPLETELY FIXED
 * @since 2025-08-03
 */
@RestController
@RequestMapping("/api/v1/loan-applications")
@CrossOrigin(origins = {"http://localhost:3001", "http://localhost:3000", "http://127.0.0.1:3001", "http://127.0.0.1:3000"})
@Tag(name = "Loan Application Management", description = "Зээлийн хүсэлтийн удирдлагын API")
public class LoanApplicationController {

    private static final Logger log = LoggerFactory.getLogger(LoanApplicationController.class);

    private final LoanApplicationService loanApplicationService;

    public LoanApplicationController(LoanApplicationService loanApplicationService) {
        this.loanApplicationService = loanApplicationService;
    }

    /**
     * ⭐ ЗАСВАРЛАСАН: Бүх зээлийн хүсэлтийн жагсаалт ⭐
     */
    @GetMapping
    @Operation(summary = "Зээлийн хүсэлтүүд", description = "Бүх зээлийн хүсэлтийн жагсаалт авах")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Амжилттай"),
        @ApiResponse(responseCode = "500", description = "Серверийн алдаа")
    })
    public ResponseEntity<ResponseWrapper<Page<LoanApplicationDto>>> getAllLoanApplications(
            @RequestParam(defaultValue = "0") @Parameter(description = "Хуудасны дугаар") int page,
            @RequestParam(defaultValue = "20") @Parameter(description = "Хуудсан дахь элементийн тоо") int size,
            @RequestParam(required = false) @Parameter(description = "Статус шүүлтүүр") String status,
            @RequestParam(required = false) @Parameter(description = "Зээлийн төрөл") String loanType,
            @RequestParam(required = false) @Parameter(description = "Харилцагчийн ID") UUID customerId) {
        
        log.debug("Getting loan applications - page: {}, size: {}, status: {}, customerId: {}", 
                page, size, status, customerId);
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<LoanApplicationDto> applications;
            
            if (customerId != null) {
                applications = loanApplicationService.getLoanApplicationsByCustomer(customerId, pageable);
            } else if (status != null && !status.trim().isEmpty()) {
                try {
                    LoanStatus loanStatus = LoanStatus.valueOf(status.toUpperCase());
                    applications = loanApplicationService.getLoanApplicationsByStatus(loanStatus, pageable);
                } catch (IllegalArgumentException e) {
                    log.error("Invalid status parameter: {}", status);
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ResponseWrapper.error("Буруу статус: " + status));
                }
            } else if (loanType != null && !loanType.trim().isEmpty()) {
                try {
                    com.company.los.entity.LoanApplication.LoanType type = 
                        com.company.los.entity.LoanApplication.LoanType.valueOf(loanType.toUpperCase());
                    applications = loanApplicationService.getLoanApplicationsByType(type, pageable);
                } catch (IllegalArgumentException e) {
                    log.error("Invalid loan type parameter: {}", loanType);
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ResponseWrapper.error("Буруу зээлийн төрөл: " + loanType));
                }
            } else {
                applications = loanApplicationService.getAllLoanApplications(pageable);
            }
            
            log.info("Successfully retrieved {} loan applications", applications.getTotalElements());
            return ResponseEntity.ok(ResponseWrapper.success(applications));
            
        } catch (Exception e) {
            log.error("Error getting loan applications: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseWrapper.error("Зээлийн хүсэлт авахад алдаа гарлаа: " + e.getMessage()));
        }
    }

    /**
     * ⭐ ЗАСВАРЛАСАН: Тодорхой зээлийн хүсэлт авах ⭐
     */
    @GetMapping("/{id}")
    @Operation(summary = "Зээлийн хүсэлтийн мэдээлэл", description = "ID-гаар зээлийн хүсэлтийн дэлгэрэнгүй мэдээлэл авах")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Амжилттай"),
        @ApiResponse(responseCode = "404", description = "Зээлийн хүсэлт олдсонгүй")
    })
    public ResponseEntity<ResponseWrapper<LoanApplicationDto>> getLoanApplication(
            @PathVariable @Parameter(description = "Зээлийн хүсэлтийн ID") UUID id) {
        
        log.debug("Getting loan application: {}", id);
        
        try {
            if (id == null) {
                log.warn("Loan application ID is null");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ResponseWrapper.error("Буруу зээлийн хүсэлтийн ID"));
            }

            LoanApplicationDto application = loanApplicationService.getLoanApplicationById(id);
            
            log.info("Successfully retrieved loan application: {}", id);
            return ResponseEntity.ok(ResponseWrapper.success(application));
            
        } catch (com.company.los.exception.ResourceNotFoundException e) {
            log.warn("Loan application not found: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ResponseWrapper.error("Зээлийн хүсэлт олдсонгүй"));
        } catch (Exception e) {
            log.error("Error getting loan application {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseWrapper.error("Системийн алдаа гарлаа"));
        }
    }

    /**
     * ⭐ ЗАСВАРЛАСАН: Шинэ зээлийн хүсэлт үүсгэх ⭐
     */
    @PostMapping
    @Operation(summary = "Шинэ зээлийн хүсэлт", description = "Шинэ зээлийн хүсэлт үүсгэх")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Амжилттай үүсгэгдсэн"),
        @ApiResponse(responseCode = "400", description = "Буруу мэдээлэл"),
        @ApiResponse(responseCode = "404", description = "Харилцагч олдсонгүй")
    })
    public ResponseEntity<ResponseWrapper<LoanApplicationDto>> createLoanApplication(
            @Valid @RequestBody CreateLoanRequestDto loanRequest) {
        
        log.info("Creating new loan application for customer: {}", loanRequest.getCustomerId());
        
        try {
            // ⭐ VALIDATION ⭐
            if (loanRequest == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ResponseWrapper.error("Зээлийн хүсэлтийн мэдээлэл байхгүй байна"));
            }

            if (loanRequest.getCustomerId() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ResponseWrapper.error("Харилцагчийн ID заавал оруулна уу"));
            }

            if (loanRequest.getRequestedAmount() == null || loanRequest.getRequestedAmount().compareTo(BigDecimal.ZERO) <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ResponseWrapper.error("Зээлийн дүн заавал 0-ээс их байна"));
            }

            if (loanRequest.getRequestedTermMonths() == null || loanRequest.getRequestedTermMonths() <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ResponseWrapper.error("Зээлийн хугацаа заавал 0-ээс их байна"));
            }

            LoanApplicationDto createdApplication = loanApplicationService.createLoanApplication(loanRequest);
            log.info("Loan application created successfully: {}", createdApplication.getId());
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseWrapper.success(createdApplication, "Зээлийн хүсэлт амжилттай үүсгэгдлээ"));
                
        } catch (IllegalArgumentException e) {
            log.warn("Business validation error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ResponseWrapper.error(e.getMessage()));
        } catch (com.company.los.exception.ResourceNotFoundException e) {
            log.warn("Customer not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ResponseWrapper.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error creating loan application: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseWrapper.error("Зээлийн хүсэлт үүсгэхэд алдаа гарлаа"));
        }
    }

    /**
     * ⭐ ЗАСВАРЛАСАН: Зээлийн хүсэлтийн статус шинэчлэх ⭐
     */
    @PutMapping("/{id}/status")
    @Operation(summary = "Статус шинэчлэх", description = "Зээлийн хүсэлтийн статус шинэчлэх")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Амжилттай шинэчлэгдсэн"),
        @ApiResponse(responseCode = "404", description = "Зээлийн хүсэлт олдсонгүй"),
        @ApiResponse(responseCode = "400", description = "Буруу статус")
    })
    public ResponseEntity<ResponseWrapper<LoanApplicationDto>> updateStatus(
            @PathVariable @Parameter(description = "Зээлийн хүсэлтийн ID") UUID id,
            @RequestParam @Parameter(description = "Шинэ статус") String status,
            @RequestParam(required = false) @Parameter(description = "Тайлбар") String comment) {
        
        log.info("Updating loan application status: {} -> {}", id, status);
        
        try {
            if (id == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ResponseWrapper.error("Буруу зээлийн хүсэлтийн ID"));
            }

            if (status == null || status.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ResponseWrapper.error("Статус заавал оруулна уу"));
            }

            try {
                LoanStatus loanStatus = LoanStatus.valueOf(status.toUpperCase());
                LoanApplicationDto updatedApplication = loanApplicationService.updateLoanApplicationStatus(id, loanStatus);
                log.info("Loan application status updated: {}", id);
                
                return ResponseEntity.ok(ResponseWrapper.success(updatedApplication, "Статус амжилттай шинэчлэгдлээ"));
            } catch (IllegalArgumentException e) {
                log.warn("Invalid status parameter: {}", status);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ResponseWrapper.error("Буруу статус: " + status));
            }
            
        } catch (com.company.los.exception.ResourceNotFoundException e) {
            log.warn("Loan application not found: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ResponseWrapper.error("Зээлийн хүсэлт олдсонгүй"));
        } catch (IllegalStateException e) {
            log.warn("Invalid status transition: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ResponseWrapper.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error updating loan application status {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseWrapper.error("Статус шинэчлэхэд алдаа гарлаа"));
        }
    }

    /**
     * ⭐ ЗАСВАРЛАСАН: Зээлийн хүсэлт зөвшөөрөх ⭐
     */
    @PutMapping("/{id}/approve")
    @Operation(summary = "Зээл зөвшөөрөх", description = "Зээлийн хүсэлтийг зөвшөөрөх")
    public ResponseEntity<ResponseWrapper<LoanApplicationDto>> approveLoan(
            @PathVariable UUID id,
            @RequestParam(required = false) Double approvedAmount,
            @RequestParam(required = false) Integer approvedTermMonths,
            @RequestParam(required = false) Double approvedRate,
            @RequestParam(required = false) String comment) {
        
        log.info("Approving loan application: {}", id);
        
        try {
            if (id == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ResponseWrapper.error("Буруу зээлийн хүсэлтийн ID"));
            }

            BigDecimal amount = approvedAmount != null ? BigDecimal.valueOf(approvedAmount) : null;
            BigDecimal rate = approvedRate != null ? BigDecimal.valueOf(approvedRate) : null;
            String reason = comment != null ? comment : "Зээл зөвшөөрөгдлөө";
            
            LoanApplicationDto approvedApplication = loanApplicationService.approveLoanApplication(
                id, amount, approvedTermMonths, rate, reason);
            log.info("Loan application approved: {}", id);
            
            return ResponseEntity.ok(ResponseWrapper.success(approvedApplication, "Зээлийн хүсэлт зөвшөөрөгдлөө"));
            
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.warn("Cannot approve loan application: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ResponseWrapper.error(e.getMessage()));
        } catch (com.company.los.exception.ResourceNotFoundException e) {
            log.warn("Loan application not found: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ResponseWrapper.error("Зээлийн хүсэлт олдсонгүй"));
        } catch (Exception e) {
            log.error("Error approving loan application {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseWrapper.error("Зээл зөвшөөрөхөд алдаа гарлаа"));
        }
    }

    /**
     * ⭐ ЗАСВАРЛАСАН: Зээлийн хүсэлт татгалзах ⭐
     */
    @PutMapping("/{id}/reject")
    @Operation(summary = "Зээл татгалзах", description = "Зээлийн хүсэлтийг татгалзах")
    public ResponseEntity<ResponseWrapper<LoanApplicationDto>> rejectLoan(
            @PathVariable UUID id,
            @RequestParam String reason) {
        
        log.info("Rejecting loan application: {}", id);
        
        try {
            if (id == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ResponseWrapper.error("Буруу зээлийн хүсэлтийн ID"));
            }

            if (reason == null || reason.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ResponseWrapper.error("Татгалзах шалтгаан заавал оруулна уу"));
            }

            LoanApplicationDto rejectedApplication = loanApplicationService.rejectLoanApplication(id, reason);
            log.info("Loan application rejected: {}", id);
            
            return ResponseEntity.ok(ResponseWrapper.success(rejectedApplication, "Зээлийн хүсэлт татгалзагдлаа"));
            
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.warn("Cannot reject loan application: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ResponseWrapper.error(e.getMessage()));
        } catch (com.company.los.exception.ResourceNotFoundException e) {
            log.warn("Loan application not found: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ResponseWrapper.error("Зээлийн хүсэлт олдсонгүй"));
        } catch (Exception e) {
            log.error("Error rejecting loan application {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseWrapper.error("Зээл татгалзахад алдаа гарлаа"));
        }
    }

    /**
     * ⭐ ЗАСВАРЛАСАН: Харилцагчийн зээлийн хүсэлтүүд ⭐
     */
    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Харилцагчийн зээлүүд", description = "Тодорхой харилцагчийн бүх зээлийн хүсэлт")
    public ResponseEntity<ResponseWrapper<Page<LoanApplicationDto>>> getLoanApplicationsByCustomer(
            @PathVariable @Parameter(description = "Харилцагчийн ID") UUID customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.debug("Getting loan applications for customer: {}", customerId);
        
        try {
            if (customerId == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ResponseWrapper.error("Буруу харилцагчийн ID"));
            }

            Pageable pageable = PageRequest.of(page, size);
            Page<LoanApplicationDto> applications = loanApplicationService.getLoanApplicationsByCustomer(customerId, pageable);
            
            return ResponseEntity.ok(ResponseWrapper.success(applications));
        } catch (Exception e) {
            log.error("Error getting loan applications for customer {}: {}", customerId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseWrapper.error("Харилцагчийн зээлийн хүсэлт авахад алдаа гарлаа"));
        }
    }

    /**
     * ⭐ ЗАСВАРЛАСАН: Зээлийн хүсэлтийн статистик ⭐
     */
    @GetMapping("/statistics")
    @Operation(summary = "Зээлийн статистик", description = "Зээлийн хүсэлтийн ерөнхий статистик")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> getStatistics() {
        log.debug("Getting loan application statistics");
        
        try {
            Map<String, Object> statistics = loanApplicationService.getLoanApplicationStatistics();
            return ResponseEntity.ok(ResponseWrapper.success(statistics));
        } catch (Exception e) {
            log.error("Error getting loan statistics: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseWrapper.error("Статистик авахад алдаа гарлаа"));
        }
    }

    /**
     * ⭐ ЗАСВАРЛАСАН: Зээлийн хүсэлт хайх ⭐
     */
    @GetMapping("/search")
    @Operation(summary = "Зээлийн хүсэлт хайх", description = "Өгөгдсөн хайлтын үгээр зээлийн хүсэлт хайх")
    public ResponseEntity<ResponseWrapper<Page<LoanApplicationDto>>> searchLoanApplications(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.debug("Searching loan applications with query: {}", q);
        
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
            log.error("Error searching loan applications: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseWrapper.error("Зээлийн хүсэлт хайхад алдаа гарлаа"));
        }
    }

    /**
     * ⭐ ЗАСВАРЛАСАН: Зээлийн дүн тооцоолох ⭐
     */
    @PostMapping("/calculate")
    @Operation(summary = "Зээлийн тооцоо", description = "Зээлийн сарын төлбөр болон нийт дүн тооцоолох")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> calculateLoan(
            @RequestParam @Parameter(description = "Зээлийн дүн") Double amount,
            @RequestParam @Parameter(description = "Хугацаа (сар)") Integer termInMonths,
            @RequestParam(required = false, defaultValue = "12.0") @Parameter(description = "Хүүгийн хувь") Double interestRate) {
        
        log.debug("Calculating loan: amount={}, term={}, rate={}", amount, termInMonths, interestRate);
        
        try {
            // ⭐ VALIDATION ⭐
            if (amount == null || amount <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ResponseWrapper.error("Зээлийн дүн заавал 0-ээс их байна"));
            }

            if (termInMonths == null || termInMonths <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ResponseWrapper.error("Зээлийн хугацаа заавал 0-ээс их байна"));
            }

            if (interestRate == null || interestRate < 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ResponseWrapper.error("Хүүгийн хувь заавал 0-ээс их эсвэл тэнцүү байна"));
            }

            BigDecimal principal = BigDecimal.valueOf(amount);
            BigDecimal rate = BigDecimal.valueOf(interestRate);
            
            // ⭐ CALCULATION ⭐
            BigDecimal monthlyPayment = loanApplicationService.calculateMonthlyPayment(principal, termInMonths, rate);
            BigDecimal totalPayment = loanApplicationService.calculateTotalPayment(principal, termInMonths, rate);
            BigDecimal totalInterest = loanApplicationService.calculateTotalInterest(principal, termInMonths, rate);
            List<Map<String, Object>> schedule = loanApplicationService.generateAmortizationSchedule(principal, termInMonths, rate);
            
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
            log.error("Error calculating loan: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseWrapper.error("Зээлийн тооцоо хийхэд алдаа гарлаа"));
        }
    }

    /**
     * ⭐ ЗАСВАРЛАСАН: Risk assessment хийх ⭐
     */
    @PostMapping("/{id}/assess")
    @Operation(summary = "Зээлийн үнэлгээ", description = "Зээлийн хүсэлтийн эрсдэлийн үнэлгээ хийх")
    public ResponseEntity<ResponseWrapper<LoanApplicationDto>> performRiskAssessment(
            @PathVariable UUID id) {
        
        log.info("Performing risk assessment for loan application: {}", id);
        
        try {
            if (id == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ResponseWrapper.error("Буруу зээлийн хүсэлтийн ID"));
            }

            LoanApplicationDto assessedApplication = loanApplicationService.performRiskAssessment(id);
            log.info("Risk assessment completed for loan application: {}", id);
            
            return ResponseEntity.ok(ResponseWrapper.success(assessedApplication, "Эрсдэлийн үнэлгээ амжилттай хийгдлээ"));
            
        } catch (com.company.los.exception.ResourceNotFoundException e) {
            log.warn("Loan application not found: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ResponseWrapper.error("Зээлийн хүсэлт олдсонгүй"));
        } catch (Exception e) {
            log.error("Error performing risk assessment {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseWrapper.error("Эрсдэлийн үнэлгээ хийхэд алдаа гарлаа"));
        }
    }

    /**
     * ⭐ HEALTH CHECK ENDPOINT ⭐
     */
    @GetMapping("/health")
    @Operation(summary = "Loan API health check")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> healthCheck() {
        try {
            Map<String, Object> health = new HashMap<>();
            health.put("status", "UP");
            health.put("service", "LoanApplicationController");
            health.put("timestamp", LocalDateTime.now());
            health.put("version", "3.0");
            
            // Database connectivity шалгах
            try {
                long loanCount = loanApplicationService.getTotalLoanApplicationCount();
                health.put("databaseStatus", "UP");
                health.put("totalLoanApplications", loanCount);
            } catch (Exception e) {
                log.warn("Database connection issue: {}", e.getMessage());
                health.put("databaseStatus", "DOWN");
                health.put("databaseError", e.getMessage());
            }
            
            log.debug("Loan API health check successful");
            return ResponseEntity.ok(ResponseWrapper.success(health));
        } catch (Exception e) {
            log.error("Loan API health check failed: {}", e.getMessage());
            Map<String, Object> errorHealth = new HashMap<>();
            errorHealth.put("status", "DOWN");
            errorHealth.put("service", "LoanApplicationController");
            errorHealth.put("error", e.getMessage());
            errorHealth.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ResponseWrapper.error("Loan API унтарсан байна", errorHealth));
        }
    }

    /**
     * ⭐ API хариу wrapper класс ⭐
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