package com.company.los.repository;

import com.company.los.entity.Customer;
import com.company.los.entity.LoanApplication;
import com.company.los.enums.LoanStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Зээлийн хүсэлтийн Repository
 * Loan Application Repository Interface
 */
@Repository
public interface LoanApplicationRepository extends JpaRepository<LoanApplication, UUID> {

    // Суурь хайлтууд
    /**
     * Хүсэлтийн дугаараар хайх
     */
    Optional<LoanApplication> findByApplicationNumber(String applicationNumber);

    /**
     * Харилцагчийн бүх зээлийн хүсэлт
     */
    Page<LoanApplication> findByCustomer(Customer customer, Pageable pageable);

    /**
     * Харилцагчийн ID-гаар хайх
     */
    Page<LoanApplication> findByCustomerId(UUID customerId, Pageable pageable);

    /**
     * Статусаар хайх
     */
    Page<LoanApplication> findByStatus(LoanStatus status, Pageable pageable);

    /**
     * Зээлийн төрлөөр хайх
     */
    Page<LoanApplication> findByLoanType(LoanApplication.LoanType loanType, Pageable pageable);

    /**
     * Хүсэлтийн дугаар байгаа эсэхийг шалгах
     */
    boolean existsByApplicationNumber(String applicationNumber);

    // Статус болон огноогоор хайх
    /**
     * Статус болон огнооны хязгаараар хайх
     */
    @Query("SELECT la FROM LoanApplication la WHERE " +
           "la.status = :status AND la.submittedDate BETWEEN :startDate AND :endDate")
    Page<LoanApplication> findByStatusAndSubmittedDateBetween(
            @Param("status") LoanStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    /**
     * Өнөөдөр илгээсэн хүсэлтүүд
     */
    @Query("SELECT la FROM LoanApplication la WHERE DATE(la.submittedDate) = CURRENT_DATE")
    List<LoanApplication> findTodaySubmitted();

    /**
     * Энэ сард илгээсэн хүсэлтүүд
     */
    @Query("SELECT la FROM LoanApplication la WHERE " +
           "YEAR(la.submittedDate) = YEAR(CURRENT_DATE) AND " +
           "MONTH(la.submittedDate) = MONTH(CURRENT_DATE)")
    List<LoanApplication> findThisMonthSubmitted();

    // Дүнгийн хязгаараар хайх
    /**
     * Хүсэх дүнгийн хязгаараар хайх
     */
    @Query("SELECT la FROM LoanApplication la WHERE " +
           "la.requestedAmount BETWEEN :minAmount AND :maxAmount")
    Page<LoanApplication> findByRequestedAmountBetween(
            @Param("minAmount") BigDecimal minAmount,
            @Param("maxAmount") BigDecimal maxAmount,
            Pageable pageable);

    /**
     * Зөвшөөрсөн дүнгийн хязгаараар хайх
     */
    @Query("SELECT la FROM LoanApplication la WHERE " +
           "la.approvedAmount BETWEEN :minAmount AND :maxAmount")
    Page<LoanApplication> findByApprovedAmountBetween(
            @Param("minAmount") BigDecimal minAmount,
            @Param("maxAmount") BigDecimal maxAmount,
            Pageable pageable);

    // Workflow холбоотой
    /**
     * Хүлээлгэн өгсөн хүсэлтүүд
     */
    @Query("SELECT la FROM LoanApplication la WHERE la.assignedTo = :assignedTo AND la.status IN :activeStatuses")
    Page<LoanApplication> findAssignedApplications(
            @Param("assignedTo") String assignedTo,
            @Param("activeStatuses") List<LoanStatus> activeStatuses,
            Pageable pageable);

    /**
     * Одоогийн алхамаар хайх
     */
    Page<LoanApplication> findByCurrentStep(String currentStep, Pageable pageable);

    /**
     * Тэргүүлэх эрэмбээр хайх
     */
    Page<LoanApplication> findByPriority(Integer priority, Pageable pageable);

    // Хугацаа хэтэрсэн хүсэлтүүд
    /**
     * Хугацаа хэтэрсэн хүсэлтүүд (14 хоногоос илүү идэвхтэй)
     */
    @Query("SELECT la FROM LoanApplication la WHERE " +
           "la.status IN :activeStatuses AND " +
           "la.submittedDate < :overdueDate")
    Page<LoanApplication> findOverdueApplications(
            @Param("activeStatuses") List<LoanStatus> activeStatuses,
            @Param("overdueDate") LocalDateTime overdueDate,
            Pageable pageable);

    /**
     * Урт хугацаа хүлээж байгаа хүсэлтүүд
     */
    @Query("SELECT la FROM LoanApplication la WHERE " +
           "la.status = :status AND " +
           "la.submittedDate < :thresholdDate")
    List<LoanApplication> findPendingTooLong(
            @Param("status") LoanStatus status,
            @Param("thresholdDate") LocalDateTime thresholdDate);

    // Ерөнхий хайлт
    /**
     * Ерөнхий хайлт - хүсэлтийн дугаар, харилцагчийн нэр
     */
    @Query("SELECT la FROM LoanApplication la JOIN la.customer c WHERE " +
           "LOWER(la.applicationNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.registerNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.phone) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "(c.customerType = 'INDIVIDUAL' AND (" +
           "LOWER(COALESCE(c.firstName, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(COALESCE(c.lastName, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%')))) OR " +
           "(c.customerType = 'BUSINESS' AND " +
           "LOWER(COALESCE(c.companyName, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<LoanApplication> findBySearchTerm(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Статистик
    /**
     * Статусаар тоолох
     */
    @Query("SELECT la.status, COUNT(la) FROM LoanApplication la GROUP BY la.status")
    List<Object[]> countByStatus();

    /**
     * Зээлийн төрлөөр тоолох
     */
    @Query("SELECT la.loanType, COUNT(la) FROM LoanApplication la GROUP BY la.loanType")
    List<Object[]> countByLoanType();

    /**
     * Сарын статистик
     */
    @Query("SELECT DATE_FORMAT(la.submittedDate, '%Y-%m'), COUNT(la), AVG(la.requestedAmount) " +
           "FROM LoanApplication la WHERE la.submittedDate >= :startDate " +
           "GROUP BY DATE_FORMAT(la.submittedDate, '%Y-%m') " +
           "ORDER BY DATE_FORMAT(la.submittedDate, '%Y-%m')")
    List<Object[]> getMonthlyStats(@Param("startDate") LocalDateTime startDate);

    /**
     * Зөвшөөрөл хувь тооцоолох
     */
    @Query("SELECT " +
           "COUNT(CASE WHEN la.status = 'APPROVED' THEN 1 END) as approved, " +
           "COUNT(CASE WHEN la.status = 'REJECTED' THEN 1 END) as rejected, " +
           "COUNT(la) as total " +
           "FROM LoanApplication la WHERE la.submittedDate >= :startDate")
    Object[] getApprovalRates(@Param("startDate") LocalDateTime startDate);

    // Performance хяналт
    /**
     * Дундаж хугацаа (илгээснээс зөвшөөрөх хүртэл)
     */
    @Query("SELECT AVG(DATEDIFF(la.approvedDate, la.submittedDate)) " +
           "FROM LoanApplication la WHERE la.approvedDate IS NOT NULL AND la.submittedDate IS NOT NULL")
    Double getAverageProcessingDays();

    /**
     * Хамгийн хурдан зөвшөөрсөн хүсэлтүүд
     */
    @Query("SELECT la FROM LoanApplication la WHERE " +
           "la.approvedDate IS NOT NULL AND la.submittedDate IS NOT NULL " +
           "ORDER BY DATEDIFF(la.approvedDate, la.submittedDate) ASC")
    Page<LoanApplication> findFastestApproved(Pageable pageable);

    // Дэвшилтэт хайлт
    /**
     * Дэвшилтэт филтертэй хайлт
     */
    @Query("SELECT la FROM LoanApplication la JOIN la.customer c WHERE " +
           "(:status IS NULL OR la.status = :status) AND " +
           "(:loanType IS NULL OR la.loanType = :loanType) AND " +
           "(:customerType IS NULL OR c.customerType = :customerType) AND " +
           "(:minAmount IS NULL OR la.requestedAmount >= :minAmount) AND " +
           "(:maxAmount IS NULL OR la.requestedAmount <= :maxAmount) AND " +
           "(:startDate IS NULL OR la.submittedDate >= :startDate) AND " +
           "(:endDate IS NULL OR la.submittedDate <= :endDate) AND " +
           "(:assignedTo IS NULL OR la.assignedTo = :assignedTo) AND " +
           "(:priority IS NULL OR la.priority = :priority)")
    Page<LoanApplication> findByAdvancedFilters(
            @Param("status") LoanStatus status,
            @Param("loanType") LoanApplication.LoanType loanType,
            @Param("customerType") Customer.CustomerType customerType,
            @Param("minAmount") BigDecimal minAmount,
            @Param("maxAmount") BigDecimal maxAmount,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("assignedTo") String assignedTo,
            @Param("priority") Integer priority,
            Pageable pageable);

    // Dashboard статистик
    /**
     * Өнөөдрийн dashboard статистик
     */
    @Query("SELECT " +
           "COUNT(CASE WHEN DATE(la.submittedDate) = CURRENT_DATE THEN 1 END) as todaySubmitted, " +
           "COUNT(CASE WHEN la.status IN ('SUBMITTED', 'DOCUMENT_REVIEW', 'CREDIT_CHECK', 'RISK_ASSESSMENT', 'MANAGER_REVIEW') THEN 1 END) as pending, " +
           "COUNT(CASE WHEN DATE(la.approvedDate) = CURRENT_DATE THEN 1 END) as todayApproved, " +
           "COUNT(CASE WHEN DATE(la.disbursedDate) = CURRENT_DATE THEN 1 END) as todayDisbursed " +
           "FROM LoanApplication la")
    Object[] getTodayDashboardStats();

    /**
     * Энэ сарын dashboard статистик
     */
    @Query("SELECT " +
           "COUNT(CASE WHEN YEAR(la.submittedDate) = YEAR(CURRENT_DATE) AND MONTH(la.submittedDate) = MONTH(CURRENT_DATE) THEN 1 END) as thisMonthSubmitted, " +
           "SUM(CASE WHEN YEAR(la.approvedDate) = YEAR(CURRENT_DATE) AND MONTH(la.approvedDate) = MONTH(CURRENT_DATE) THEN la.approvedAmount ELSE 0 END) as thisMonthApprovedAmount, " +
           "COUNT(CASE WHEN YEAR(la.approvedDate) = YEAR(CURRENT_DATE) AND MONTH(la.approvedDate) = MONTH(CURRENT_DATE) THEN 1 END) as thisMonthApproved " +
           "FROM LoanApplication la")
    Object[] getThisMonthDashboardStats();

    // Эрсдэлийн шинжилгээ
    /**
     * Өндөр эрсдэлийн хүсэлтүүд
     */
    @Query("SELECT la FROM LoanApplication la WHERE " +
           "la.riskScore IS NOT NULL AND la.riskScore >= :highRiskThreshold")
    Page<LoanApplication> findHighRiskApplications(@Param("highRiskThreshold") BigDecimal highRiskThreshold, 
                                                 Pageable pageable);

    /**
     * Бага эрсдэлийн хүсэлтүүд
     */
    @Query("SELECT la FROM LoanApplication la WHERE " +
           "la.riskScore IS NOT NULL AND la.riskScore <= :lowRiskThreshold")
    Page<LoanApplication> findLowRiskApplications(@Param("lowRiskThreshold") BigDecimal lowRiskThreshold, 
                                                Pageable pageable);

    // Bulk операциуд
    /**
     * Олон хүсэлтийн статус өөрчлөх
     */
    @Modifying
    @Query("UPDATE LoanApplication la SET la.status = :newStatus, la.updatedBy = :updatedBy " +
           "WHERE la.id IN :applicationIds AND la.status = :currentStatus")
    int updateStatusForApplications(@Param("applicationIds") List<UUID> applicationIds,
                                  @Param("currentStatus") LoanStatus currentStatus,
                                  @Param("newStatus") LoanStatus newStatus,
                                  @Param("updatedBy") String updatedBy);

    /**
     * Хүлээлгэн өгөх
     */
    @Modifying
    @Query("UPDATE LoanApplication la SET la.assignedTo = :assignedTo, la.updatedBy = :updatedBy " +
           "WHERE la.id IN :applicationIds")
    int assignApplications(@Param("applicationIds") List<UUID> applicationIds,
                         @Param("assignedTo") String assignedTo,
                         @Param("updatedBy") String updatedBy);

    // Тэргүүлэх эрэмбэ өөрчлөх
    @Modifying
    @Query("UPDATE LoanApplication la SET la.priority = :priority, la.updatedBy = :updatedBy " +
           "WHERE la.id = :applicationId")
    int updatePriority(@Param("applicationId") UUID applicationId,
                     @Param("priority") Integer priority,
                     @Param("updatedBy") String updatedBy);

    // Тайлангийн query-ууд
    /**
     * Хугацааны зээлийн тайлан
     */
    @Query("SELECT " +
           "la.loanType, " +
           "COUNT(la) as count, " +
           "SUM(la.requestedAmount) as totalRequested, " +
           "SUM(CASE WHEN la.status = 'APPROVED' THEN la.approvedAmount ELSE 0 END) as totalApproved, " +
           "AVG(la.requestedAmount) as avgRequested " +
           "FROM LoanApplication la " +
           "WHERE la.submittedDate BETWEEN :startDate AND :endDate " +
           "GROUP BY la.loanType")
    List<Object[]> getLoanReport(@Param("startDate") LocalDateTime startDate,
                               @Param("endDate") LocalDateTime endDate);

    /**
     * Performance тайлан
     */
    @Query("SELECT " +
           "DATE_FORMAT(la.submittedDate, '%Y-%m-%d') as date, " +
           "COUNT(la) as submitted, " +
           "COUNT(CASE WHEN la.approvedDate IS NOT NULL THEN 1 END) as approved, " +
           "AVG(CASE WHEN la.approvedDate IS NOT NULL THEN DATEDIFF(la.approvedDate, la.submittedDate) END) as avgProcessingDays " +
           "FROM LoanApplication la " +
           "WHERE la.submittedDate BETWEEN :startDate AND :endDate " +
           "GROUP BY DATE_FORMAT(la.submittedDate, '%Y-%m-%d') " +
           "ORDER BY date")
    List<Object[]> getPerformanceReport(@Param("startDate") LocalDateTime startDate,
                                      @Param("endDate") LocalDateTime endDate);

    /**
     * Харилцагчийн сүүлийн зээлийн хүсэлт
     */
    @Query("SELECT la FROM LoanApplication la WHERE la.customer.id = :customerId " +
           "ORDER BY la.submittedDate DESC LIMIT 1")
    Optional<LoanApplication> findLatestByCustomerId(@Param("customerId") UUID customerId);

    /**
     * Харилцагчийн идэвхтэй зээлийн тоо
     */
    @Query("SELECT COUNT(la) FROM LoanApplication la WHERE " +
           "la.customer.id = :customerId AND la.status = 'DISBURSED'")
    int countActiveLoansForCustomer(@Param("customerId") UUID customerId);
}