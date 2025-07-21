package com.company.los.service;

import com.company.los.dto.CreateLoanRequestDto;
import com.company.los.dto.LoanApplicationDto;
import com.company.los.entity.Customer;
import com.company.los.entity.LoanApplication;
import com.company.los.enums.LoanStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Зээлийн хүсэлтийн Service Interface
 * Loan Application Service Interface
 */
public interface LoanApplicationService {

    // CRUD операциуд
    /**
     * Шинэ зээлийн хүсэлт үүсгэх
     */
    LoanApplicationDto createLoanApplication(CreateLoanRequestDto createRequest);

    /**
     * Зээлийн хүсэлтийн мэдээлэл авах
     */
    LoanApplicationDto getLoanApplicationById(UUID id);

    /**
     * Зээлийн хүсэлт шинэчлэх
     */
    LoanApplicationDto updateLoanApplication(UUID id, LoanApplicationDto loanApplicationDto);

    /**
     * Зээлийн хүсэлт устгах (soft delete)
     */
    void deleteLoanApplication(UUID id);

    /**
     * Устгасан хүсэлт сэргээх
     */
    LoanApplicationDto restoreLoanApplication(UUID id);

    // Хайлт операциуд
    /**
     * Бүх зээлийн хүсэлтийн жагсаалт
     */
    Page<LoanApplicationDto> getAllLoanApplications(Pageable pageable);

    /**
     * Хүсэлтийн дугаараар хайх
     */
    LoanApplicationDto getLoanApplicationByNumber(String applicationNumber);

    /**
     * Харилцагчийн зээлийн хүсэлтүүд
     */
    Page<LoanApplicationDto> getLoanApplicationsByCustomer(UUID customerId, Pageable pageable);

    /**
     * Статусаар хайх
     */
    Page<LoanApplicationDto> getLoanApplicationsByStatus(LoanStatus status, Pageable pageable);

    /**
     * Зээлийн төрлөөр хайх
     */
    Page<LoanApplicationDto> getLoanApplicationsByType(LoanApplication.LoanType loanType, Pageable pageable);

    /**
     * Ерөнхий хайлт
     */
    Page<LoanApplicationDto> searchLoanApplications(String searchTerm, Pageable pageable);

    // Дэвшилтэт хайлт
    /**
     * Филтертэй дэвшилтэт хайлт
     */
    Page<LoanApplicationDto> searchLoanApplicationsWithFilters(
            LoanStatus status,
            LoanApplication.LoanType loanType,
            Customer.CustomerType customerType,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            LocalDateTime startDate,
            LocalDateTime endDate,
            String assignedTo,
            Integer priority,
            Pageable pageable
    );

    // Workflow операциуд
    /**
     * Зээлийн хүсэлт илгээх
     */
    LoanApplicationDto submitLoanApplication(UUID id);

    /**
     * Зээлийн хүсэлт зөвшөөрөх
     */
    LoanApplicationDto approveLoanApplication(UUID id, BigDecimal approvedAmount, 
                                            Integer approvedTermMonths, BigDecimal approvedRate, 
                                            String reason);

    /**
     * Зээлийн хүсэлт татгалзах
     */
    LoanApplicationDto rejectLoanApplication(UUID id, String reason);

    /**
     * Зээл олгох
     */
    LoanApplicationDto disburseLoan(UUID id);

    /**
     * Зээлийн хүсэлт цуцлах
     */
    LoanApplicationDto cancelLoanApplication(UUID id, String reason);

    /**
     * Нэмэлт мэдээлэл шаардах
     */
    LoanApplicationDto requestAdditionalInfo(UUID id, String requestedInfo);

    // Хүлээлгэн өгөх
    /**
     * Хүсэлт хүлээлгэн өгөх
     */
    LoanApplicationDto assignLoanApplication(UUID id, String assignedTo);

    /**
     * Олон хүсэлт хүлээлгэн өгөх
     */
    int assignLoanApplications(List<UUID> applicationIds, String assignedTo);

    /**
     * Хүлээлгэсэн хүсэлтүүд
     */
    Page<LoanApplicationDto> getAssignedLoanApplications(String assignedTo, Pageable pageable);

    // Тэргүүлэх эрэмбэ
    /**
     * Тэргүүлэх эрэмбэ өөрчлөх
     */
    LoanApplicationDto updatePriority(UUID id, Integer priority);

    /**
     * Тэргүүлэх эрэмбээр хайх
     */
    Page<LoanApplicationDto> getLoanApplicationsByPriority(Integer priority, Pageable pageable);

    // Validation
    /**
     * Хүсэлтийн дугаар байгаа эсэхийг шалгах
     */
    boolean existsByApplicationNumber(String applicationNumber);

    /**
     * Зээлийн хүсэлт хүчинтэй эсэхийг шалгах
     */
    boolean validateLoanApplication(LoanApplicationDto loanApplicationDto);

    /**
     * Харилцагч засах боломжтой эсэхийг шалгах
     */
    boolean canEditLoanApplication(UUID id);

    /**
     * Зээл зөвшөөрөх боломжтой эсэхийг шалгах
     */
    boolean canApproveLoanApplication(UUID id);

    // Тооцоолол
    /**
     * Сарын төлбөр тооцоолох
     */
    BigDecimal calculateMonthlyPayment(BigDecimal principal, Integer termMonths, BigDecimal annualRate);

    /**
     * Нийт төлөх дүн тооцоолох
     */
    BigDecimal calculateTotalPayment(BigDecimal principal, Integer termMonths, BigDecimal annualRate);

    /**
     * Хүүгийн дүн тооцоолох
     */
    BigDecimal calculateTotalInterest(BigDecimal principal, Integer termMonths, BigDecimal annualRate);

    /**
     * Зээлийн хүүгийн хүснэгт үүсгэх
     */
    List<Map<String, Object>> generateAmortizationSchedule(BigDecimal principal, Integer termMonths, BigDecimal annualRate);

    // Эрсдэлийн үнэлгээ
    /**
     * Эрсдэлийн үнэлгээ хийх
     */
    Map<String, Object> performRiskAssessment(UUID id);

    /**
     * Зээлийн оноо тооцоолох
     */
    BigDecimal calculateCreditScore(UUID customerId);

    /**
     * Өндөр эрсдэлийн хүсэлтүүд
     */
    Page<LoanApplicationDto> getHighRiskApplications(BigDecimal riskThreshold, Pageable pageable);

    /**
     * Бага эрсдэлийн хүсэлтүүд
     */
    Page<LoanApplicationDto> getLowRiskApplications(BigDecimal riskThreshold, Pageable pageable);

    // Хугацаа хэтэрсэн хүсэлтүүд
    /**
     * Хугацаа хэтэрсэн хүсэлтүүд
     */
    Page<LoanApplicationDto> getOverdueApplications(Pageable pageable);

    /**
     * Урт хугацаа хүлээж байгаа хүсэлтүүд
     */
    List<LoanApplicationDto> getPendingTooLong(LoanStatus status, int days);

    // Статистик
    /**
     * Зээлийн хүсэлтийн статистик
     */
    Map<String, Object> getLoanApplicationStatistics();

    /**
     * Статусаар статистик
     */
    Map<LoanStatus, Long> getLoanApplicationCountByStatus();

    /**
     * Зээлийн төрлөөр статистик
     */
    Map<LoanApplication.LoanType, Long> getLoanApplicationCountByType();

    /**
     * Сарын статистик
     */
    List<Map<String, Object>> getMonthlyLoanApplicationStats(int months);

    /**
     * Зөвшөөрөл хувь тооцоолох
     */
    Map<String, Object> getApprovalRates(LocalDateTime startDate);

    // Performance хяналт
    /**
     * Дундаж боловсруулах хугацаа
     */
    Double getAverageProcessingDays();

    /**
     * Хамгийн хурдан зөвшөөрсөн хүсэлтүүд
     */
    Page<LoanApplicationDto> getFastestApprovedApplications(Pageable pageable);

    // Dashboard статистик
    /**
     * Өнөөдрийн dashboard статистик
     */
    Map<String, Object> getTodayDashboardStats();

    /**
     * Энэ сарын dashboard статистик
     */
    Map<String, Object> getThisMonthDashboardStats();

    // Тайлан
    /**
     * Хугацааны зээлийн тайлан
     */
    List<Map<String, Object>> getLoanReport(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Performance тайлан
     */
    List<Map<String, Object>> getPerformanceReport(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Зээлийн тайлан export
     */
    byte[] exportLoanApplicationsToExcel(List<UUID> applicationIds);

    // Харилцагчтай холбоотой
    /**
     * Харилцагчийн сүүлийн зээлийн хүсэлт
     */
    LoanApplicationDto getLatestLoanApplicationByCustomer(UUID customerId);

    /**
     * Харилцагчийн идэвхтэй зээлийн тоо
     */
    int getActiveLoansCountForCustomer(UUID customerId);

    /**
     * Харилцагчийн зээлийн түүх
     */
    List<LoanApplicationDto> getCustomerLoanHistory(UUID customerId);

    // Bulk операциуд
    /**
     * Олон хүсэлтийн статус өөрчлөх
     */
    int updateStatusForApplications(List<UUID> applicationIds, LoanStatus currentStatus, 
                                  LoanStatus newStatus);

    // Business rules
    /**
     * Зээлийн хязгаар шалгах
     */
    boolean checkLoanLimits(UUID customerId, BigDecimal requestedAmount);

    /**
     * Харилцагчийн зээлийн чадавх үнэлэх
     */
    Map<String, Object> assessLoanCapacity(UUID customerId, BigDecimal requestedAmount);

    // Notification
    /**
     * Хүсэлтийн статус өөрчлөгдөхөд мэдэгдэл илгээх
     */
    boolean sendStatusChangeNotification(UUID id);

    /**
     * Хугацаа хэтэрсэн хүсэлтийн мэдэгдэл
     */
    boolean sendOverdueNotification(UUID id);

    // Auto processing
    /**
     * Автомат зөвшөөрөлд шалгах
     */
    boolean checkAutoApprovalEligibility(UUID id);

    /**
     * Автомат зөвшөөрөх
     */
    LoanApplicationDto processAutoApproval(UUID id);

    // Quality assurance
    /**
     * Зээлийн хүсэлт дахин шалгах
     */
    Map<String, Object> reviewLoanApplication(UUID id);

    /**
     * Өгөгдлийн бүрэн бус байдлыг шалгах
     */
    Map<String, Object> validateDataIntegrity();

    // Audit & History
    /**
     * Хүсэлтийн өөрчлөлтийн түүх
     */
    List<Map<String, Object>> getLoanApplicationAuditHistory(UUID id);

    /**
     * Хүсэлтийн үйл ажиллагааны лого
     */
    List<Map<String, Object>> getLoanApplicationActivityLog(UUID id);
}