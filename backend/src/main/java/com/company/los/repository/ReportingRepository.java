package com.company.los.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Тайлангийн Repository
 * Reporting Repository for cross-entity complex queries
 */
@Repository
public interface ReportingRepository { // Removed JpaRepository<Object, String>

    // Loan Application Reports
    /**
     * Зээлийн хүсэлтийн дэлгэрэнгүй тайлан
     */
    @Query("SELECT " +
           "la.applicationNumber, " +
           "CONCAT(c.firstName, ' ', c.lastName) as customerName, " +
           "c.registrationNumber, " +
           "lp.name as productName, " +
           "la.requestedAmount, " +
           "la.requestedTermMonths, " +
           "la.status, " +
           "la.appliedAt, " +
           "la.approvedAt, " +
           "la.rejectedAt, " +
           "COUNT(d.id) as documentCount " +
           "FROM LoanApplication la " +
           "JOIN la.customer c " +
           "JOIN la.loanProduct lp " +
           "LEFT JOIN la.documents d " +
           "WHERE la.appliedAt BETWEEN :startDate AND :endDate " +
           "GROUP BY la.id, la.applicationNumber, c.firstName, c.lastName, c.registrationNumber, " +
           "lp.name, la.requestedAmount, la.requestedTermMonths, la.status, la.appliedAt, la.approvedAt, la.rejectedAt " +
           "ORDER BY la.appliedAt DESC")
    List<Object[]> getLoanApplicationReport(@Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate);

    /**
     * Зээлийн батлалтын тайлан
     */
    @Query("SELECT " +
           "lp.name as productName, " +
           "COUNT(la.id) as totalApplications, " +
           "COUNT(CASE WHEN la.status = 'APPROVED' THEN 1 END) as approvedCount, " +
           "COUNT(CASE WHEN la.status = 'REJECTED' THEN 1 END) as rejectedCount, " +
           "COUNT(CASE WHEN la.status = 'PENDING' THEN 1 END) as pendingCount, " +
           "ROUND(COUNT(CASE WHEN la.status = 'APPROVED' THEN 1 END) * 100.0 / COUNT(la.id), 2) as approvalRate, " +
           "SUM(CASE WHEN la.status = 'APPROVED' THEN la.requestedAmount ELSE 0 END) as approvedAmount, " +
           "AVG(CASE WHEN la.status = 'APPROVED' THEN la.requestedAmount END) as avgApprovedAmount " +
           "FROM LoanApplication la " +
           "JOIN la.loanProduct lp " +
           "WHERE la.appliedAt BETWEEN :startDate AND :endDate " +
           "GROUP BY lp.id, lp.name " +
           "ORDER BY approvalRate DESC")
    List<Object[]> getLoanApprovalReport(@Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate);

    /**
     * Харилцагчийн зээлийн түүх тайлан
     */
    @Query("SELECT " +
           "CONCAT(c.firstName, ' ', c.lastName) as customerName, " +
           "c.registrationNumber, " +
           "c.email, " +
           "c.phone, " +
           "c.monthlyIncome, " +
           "COUNT(la.id) as totalApplications, " +
           "COUNT(CASE WHEN la.status = 'APPROVED' THEN 1 END) as approvedLoans, " +
           "COUNT(CASE WHEN la.status = 'REJECTED' THEN 1 END) as rejectedLoans, " +
           "SUM(CASE WHEN la.status = 'APPROVED' THEN la.requestedAmount ELSE 0 END) as totalApprovedAmount, " +
           "MAX(la.appliedAt) as lastApplicationDate, " +
           "COUNT(d.id) as totalDocuments " +
           "FROM Customer c " +
           "LEFT JOIN c.loanApplications la " +
           "LEFT JOIN c.documents d " +
           "WHERE c.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY c.id, c.firstName, c.lastName, c.registrationNumber, c.email, c.phone, c.monthlyIncome " +
           "ORDER BY totalApprovedAmount DESC")
    List<Object[]> getCustomerLoanHistoryReport(@Param("startDate") LocalDateTime startDate,
                                              @Param("endDate") LocalDateTime endDate);

    // Performance Reports
    /**
     * Сарын гүйцэтгэлийн тайлан
     */
    @Query("SELECT " +
           "FUNCTION('DATE_FORMAT', la.appliedAt, '%Y-%m') as month, " + // Changed DATE_FORMAT to FUNCTION('DATE_FORMAT', ...)
           "COUNT(la.id) as totalApplications, " +
           "COUNT(CASE WHEN la.status = 'APPROVED' THEN 1 END) as approvedCount, " +
           "COUNT(CASE WHEN la.status = 'REJECTED' THEN 1 END) as rejectedCount, " +
           "SUM(CASE WHEN la.status = 'APPROVED' THEN la.requestedAmount ELSE 0 END) as approvedAmount, " +
           "AVG(CASE WHEN la.status = 'APPROVED' THEN la.requestedAmount END) as avgLoanAmount, " +
           "COUNT(DISTINCT la.customer.id) as uniqueCustomers " +
           "FROM LoanApplication la " +
           "WHERE la.appliedAt >= :startDate " +
           "GROUP BY FUNCTION('DATE_FORMAT', la.appliedAt, '%Y-%m') " + // Changed DATE_FORMAT to FUNCTION('DATE_FORMAT', ...)
           "ORDER BY month DESC")
    List<Object[]> getMonthlyPerformanceReport(@Param("startDate") LocalDateTime startDate);

    /**
     * Бүтээгдэхүүний гүйцэтгэлийн тайлан
     */
    @Query("SELECT " +
           "lp.name as productName, " +
           "lp.baseRate, " +
           "lp.minAmount, " +
           "lp.maxAmount, " +
           "COUNT(la.id) as applicationCount, " +
           "COUNT(CASE WHEN la.status = 'APPROVED' THEN 1 END) as approvedCount, " +
           "ROUND(COUNT(CASE WHEN la.status = 'APPROVED' THEN 1 END) * 100.0 / COUNT(la.id), 2) as approvalRate, " +
           "SUM(CASE WHEN la.status = 'APPROVED' THEN la.requestedAmount ELSE 0 END) as totalApprovedAmount, " +
           "AVG(CASE WHEN la.status = 'APPROVED' THEN la.requestedAmount END) as avgApprovedAmount, " +
           "MIN(la.appliedAt) as firstApplication, " +
           "MAX(la.appliedAt) as lastApplication " +
           "FROM LoanProduct lp " +
           "LEFT JOIN lp.loanApplications la " +
           "WHERE lp.createdAt <= :endDate " +
           "GROUP BY lp.id, lp.name, lp.baseRate, lp.minAmount, lp.maxAmount " +
           "ORDER BY applicationCount DESC")
    List<Object[]> getProductPerformanceReport(@Param("endDate") LocalDateTime endDate);

    // Risk Analysis Reports
    /**
     * Эрсдэлийн дүн шинжилгээний тайлан
     */
    @Query("SELECT " +
           "CASE " +
           "  WHEN la.requestedAmount >= 50000000 THEN 'High Amount (50M+)' " +
           "  WHEN la.requestedAmount >= 10000000 THEN 'Medium Amount (10M-50M)' " +
           "  ELSE 'Low Amount (<10M)' " +
           "END as riskCategory, " +
           "COUNT(la.id) as totalApplications, " +
           "COUNT(CASE WHEN la.status = 'APPROVED' THEN 1 END) as approvedCount, " +
           "COUNT(CASE WHEN la.status = 'REJECTED' THEN 1 END) as rejectedCount, " +
           "ROUND(COUNT(CASE WHEN la.status = 'APPROVED' THEN 1 END) * 100.0 / COUNT(la.id), 2) as approvalRate, " +
           "SUM(la.requestedAmount) as totalRequestedAmount, " +
           "SUM(CASE WHEN la.status = 'APPROVED' THEN la.requestedAmount ELSE 0 END) as approvedAmount " +
           "FROM LoanApplication la " +
           "WHERE la.appliedAt BETWEEN :startDate AND :endDate " +
           "GROUP BY " +
           "CASE " +
           "  WHEN la.requestedAmount >= 50000000 THEN 'High Amount (50M+)' " +
           "  WHEN la.requestedAmount >= 10000000 THEN 'Medium Amount (10M-50M)' " +
           "  ELSE 'Low Amount (<10M)' " +
           "END " +
           "ORDER BY totalRequestedAmount DESC")
    List<Object[]> getRiskAnalysisReport(@Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate);

    /**
     * Орлогын түвшингээр эрсдэлийн тайлан
     */
    @Query("SELECT " +
           "CASE " +
           "  WHEN c.monthlyIncome >= 2000000 THEN 'High Income (2M+)' " +
           "  WHEN c.monthlyIncome >= 1000000 THEN 'Medium Income (1M-2M)' " +
           "  WHEN c.monthlyIncome >= 500000 THEN 'Low Income (500K-1M)' " +
           "  ELSE 'Very Low Income (<500K)' " +
           "END as incomeCategory, " +
           "COUNT(la.id) as applicationCount, " +
           "COUNT(CASE WHEN la.status = 'APPROVED' THEN 1 END) as approvedCount, " +
           "ROUND(COUNT(CASE WHEN la.status = 'APPROVED' THEN 1 END) * 100.0 / COUNT(la.id), 2) as approvalRate, " +
           "AVG(la.requestedAmount) as avgRequestedAmount, " +
           "AVG(CASE WHEN la.status = 'APPROVED' THEN la.requestedAmount END) as avgApprovedAmount " +
           "FROM LoanApplication la " +
           "JOIN la.customer c " +
           "WHERE la.appliedAt BETWEEN :startDate AND :endDate " +
           "AND c.monthlyIncome IS NOT NULL " +
           "GROUP BY " +
           "CASE " +
           "  WHEN c.monthlyIncome >= 2000000 THEN 'High Income (2M+)' " +
           "  WHEN c.monthlyIncome >= 1000000 THEN 'Medium Income (1M-2M)' " +
           "  WHEN c.monthlyIncome >= 500000 THEN 'Low Income (500K-1M)' " +
           "  ELSE 'Very Low Income (<500K)' " +
           "END " +
           "ORDER BY approvalRate DESC")
    List<Object[]> getIncomeRiskReport(@Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate);

    // Document Analysis Reports
    /**
     * Баримтын статистикийн тайлан
     */
    @Query("SELECT " +
           "dt.name as documentType, " +
           "dt.isRequired, " +
           "COUNT(d.id) as totalDocuments, " +
           "COUNT(DISTINCT d.customer.id) as customersWithDocument, " +
           "COUNT(DISTINCT d.loanApplication.id) as applicationsWithDocument, " +
           "AVG(d.fileSize) as avgFileSize, " +
           "SUM(d.fileSize) as totalFileSize " +
           "FROM DocumentType dt " +
           "LEFT JOIN dt.documents d " +
           "GROUP BY dt.id, dt.name, dt.isRequired " +
           "ORDER BY totalDocuments DESC")
    List<Object[]> getDocumentStatisticsReport();

    /**
     * Баримт дутуу зээлийн хүсэлтийн тайлан
     */
    @Query("SELECT " +
           "la.applicationNumber, " +
           "CONCAT(c.firstName, ' ', c.lastName) as customerName, " +
           "lp.name as productName, " +
           "la.requestedAmount, " +
           "la.status, " +
           "COUNT(d.id) as documentCount, " +
           "COUNT(CASE WHEN dt.isRequired = true THEN 1 END) as requiredDocumentTypes, " +
           "COUNT(CASE WHEN dt.isRequired = true AND d.id IS NOT NULL THEN 1 END) as providedRequiredDocs, " +
           "la.appliedAt " +
           "FROM LoanApplication la " +
           "JOIN la.customer c " +
           "JOIN la.loanProduct lp " +
           "LEFT JOIN la.documents d " +
           "LEFT JOIN d.documentType dt " +
           "CROSS JOIN DocumentType allDt " +
           "WHERE allDt.isRequired = true " +
           "AND la.status IN ('PENDING', 'UNDER_REVIEW') " +
           "GROUP BY la.id, la.applicationNumber, c.firstName, c.lastName, lp.name, " +
           "la.requestedAmount, la.status, la.appliedAt " +
           "HAVING COUNT(CASE WHEN dt.isRequired = true AND d.id IS NOT NULL THEN 1 END) < " +
           "       (SELECT COUNT(*) FROM DocumentType WHERE isRequired = true) " +
           "ORDER BY la.appliedAt ASC")
    List<Object[]> getIncompleteDocumentReport();

    // Geographic Reports
    /**
     * Газарзүйн тайлан
     */
    @Query("SELECT " +
           "c.province, " +
           "c.city, " +
           "COUNT(DISTINCT c.id) as customerCount, " +
           "COUNT(la.id) as applicationCount, " +
           "COUNT(CASE WHEN la.status = 'APPROVED' THEN 1 END) as approvedCount, " +
           "SUM(CASE WHEN la.status = 'APPROVED' THEN la.requestedAmount ELSE 0 END) as approvedAmount, " +
           "AVG(c.monthlyIncome) as avgIncome " +
           "FROM Customer c " +
           "LEFT JOIN c.loanApplications la " +
           "WHERE c.province IS NOT NULL AND c.city IS NOT NULL " +
           "GROUP BY c.province, c.city " +
           "ORDER BY c.province, c.city")
    List<Object[]> getGeographicReport();

    // Age Demographics Report
    /**
     * Насны бүлгийн тайлан
     */
    @Query("SELECT " +
           "CASE " +
           "  WHEN FUNCTION('YEAR', CURRENT_DATE) - FUNCTION('YEAR', c.dateOfBirth) < 25 THEN 'Under 25' " + // Changed YEAR to FUNCTION('YEAR', ...)
           "  WHEN FUNCTION('YEAR', CURRENT_DATE) - FUNCTION('YEAR', c.dateOfBirth) BETWEEN 25 AND 35 THEN '25-35' " + // Changed YEAR to FUNCTION('YEAR', ...)
           "  WHEN FUNCTION('YEAR', CURRENT_DATE) - FUNCTION('YEAR', c.dateOfBirth) BETWEEN 36 AND 50 THEN '36-50' " + // Changed YEAR to FUNCTION('YEAR', ...)
           "  WHEN FUNCTION('YEAR', CURRENT_DATE) - FUNCTION('YEAR', c.dateOfBirth) BETWEEN 51 AND 65 THEN '51-65' " + // Changed YEAR to FUNCTION('YEAR', ...)
           "  ELSE 'Over 65' " +
           "END as ageGroup, " +
           "COUNT(DISTINCT c.id) as customerCount, " +
           "COUNT(la.id) as applicationCount, " +
           "COUNT(CASE WHEN la.status = 'APPROVED' THEN 1 END) as approvedCount, " +
           "ROUND(COUNT(CASE WHEN la.status = 'APPROVED' THEN 1 END) * 100.0 / COUNT(la.id), 2) as approvalRate, " +
           "AVG(c.monthlyIncome) as avgIncome, " +
           "AVG(CASE WHEN la.status = 'APPROVED' THEN la.requestedAmount END) as avgApprovedAmount " +
           "FROM Customer c " +
           "LEFT JOIN c.loanApplications la " +
           "WHERE c.dateOfBirth IS NOT NULL " +
           "GROUP BY " +
           "CASE " +
           "  WHEN FUNCTION('YEAR', CURRENT_DATE) - FUNCTION('YEAR', c.dateOfBirth) < 25 THEN 'Under 25' " + // Changed YEAR to FUNCTION('YEAR', ...)
           "  WHEN FUNCTION('YEAR', CURRENT_DATE) - FUNCTION('YEAR', c.dateOfBirth) BETWEEN 25 AND 35 THEN '25-35' " + // Changed YEAR to FUNCTION('YEAR', ...)
           "  WHEN FUNCTION('YEAR', CURRENT_DATE) - FUNCTION('YEAR', c.dateOfBirth) BETWEEN 36 AND 50 THEN '36-50' " + // Changed YEAR to FUNCTION('YEAR', ...)
           "  WHEN FUNCTION('YEAR', CURRENT_DATE) - FUNCTION('YEAR', c.dateOfBirth) BETWEEN 51 AND 65 THEN '51-65' " + // Changed YEAR to FUNCTION('YEAR', ...)
           "  ELSE 'Over 65' " +
           "END " +
           "ORDER BY customerCount DESC")
    List<Object[]> getAgeDemographicsReport();

    // Processing Time Reports
    /**
     * Боловсруулах хугацааны тайлан
     */
    @Query("SELECT " +
           "lp.name as productName, " +
           "la.status, " +
           "COUNT(la.id) as applicationCount, " +
           "AVG(CASE " +
           "  WHEN la.status = 'APPROVED' AND la.approvedAt IS NOT NULL " +
           "  THEN FUNCTION('DATEDIFF', la.approvedAt, la.appliedAt) " + // Changed DATEDIFF to FUNCTION('DATEDIFF', ...)
           "  WHEN la.status = 'REJECTED' AND la.rejectedAt IS NOT NULL " +
           "  THEN FUNCTION('DATEDIFF', la.rejectedAt, la.appliedAt) " + // Changed DATEDIFF to FUNCTION('DATEDIFF', ...)
           "  ELSE FUNCTION('DATEDIFF', CURRENT_DATE, la.appliedAt) " + // Changed DATEDIFF to FUNCTION('DATEDIFF', ...)
           "END) as avgProcessingDays, " +
           "MIN(CASE " +
           "  WHEN la.status = 'APPROVED' AND la.approvedAt IS NOT NULL " +
           "  THEN FUNCTION('DATEDIFF', la.approvedAt, la.appliedAt) " + // Changed DATEDIFF to FUNCTION('DATEDIFF', ...)
           "  WHEN la.status = 'REJECTED' AND la.rejectedAt IS NOT NULL " +
           "  THEN FUNCTION('DATEDIFF', la.rejectedAt, la.appliedAt) " + // Changed DATEDIFF to FUNCTION('DATEDIFF', ...)
           "  ELSE FUNCTION('DATEDIFF', CURRENT_DATE, la.appliedAt) " + // Changed DATEDIFF to FUNCTION('DATEDIFF', ...)
           "END) as minProcessingDays, " +
           "MAX(CASE " +
           "  WHEN la.status = 'APPROVED' AND la.approvedAt IS NOT NULL " +
           "  THEN FUNCTION('DATEDIFF', la.approvedAt, la.appliedAt) " + // Changed DATEDIFF to FUNCTION('DATEDIFF', ...)
           "  WHEN la.status = 'REJECTED' AND la.rejectedAt IS NOT NULL " +
           "  THEN FUNCTION('DATEDIFF', la.rejectedAt, la.appliedAt) " + // Changed DATEDIFF to FUNCTION('DATEDIFF', ...)
           "  ELSE FUNCTION('DATEDIFF', CURRENT_DATE, la.appliedAt) " + // Changed DATEDIFF to FUNCTION('DATEDIFF', ...)
           "END) as maxProcessingDays " +
           "FROM LoanApplication la " +
           "JOIN la.loanProduct lp " +
           "WHERE la.appliedAt BETWEEN :startDate AND :endDate " +
           "GROUP BY lp.id, lp.name, la.status " +
           "ORDER BY lp.name, la.status")
    List<Object[]> getProcessingTimeReport(@Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);

    // User Activity Report
    /**
     * Хэрэглэгчийн үйл ажиллагааны тайлан
     */
    @Query("SELECT " +
           "al.changedBy as username, " +
           "COUNT(al.id) as totalActions, " +
           "COUNT(CASE WHEN al.action = 'CREATE' THEN 1 END) as createActions, " +
           "COUNT(CASE WHEN al.action = 'UPDATE' THEN 1 END) as updateActions, " +
           "COUNT(CASE WHEN al.action = 'DELETE' THEN 1 END) as deleteActions, " +
           "COUNT(DISTINCT al.tableName) as tablesModified, " +
           "MIN(al.changedAt) as firstActivity, " +
           "MAX(al.changedAt) as lastActivity " +
           "FROM AuditLog al " +
           "WHERE al.changedAt BETWEEN :startDate AND :endDate " +
           "AND al.changedBy IS NOT NULL " +
           "GROUP BY al.changedBy " +
           "ORDER BY totalActions DESC")
    List<Object[]> getUserActivityReport(@Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate);

    // System Health Report
    /**
     * Системийн эрүүл мэндийн тайлан
     */
    @Query("SELECT " +
           "'Customers' as entityType, " +
           "COUNT(c.id) as totalCount, " +
           "COUNT(CASE WHEN c.email IS NULL OR c.email = '' THEN 1 END) as incompleteCount, " +
           "COUNT(CASE WHEN FUNCTION('DATE', c.createdAt) = CURRENT_DATE THEN 1 END) as createdToday, " + // Changed DATE to FUNCTION('DATE', ...)
           "COUNT(CASE WHEN FUNCTION('DATE', c.updatedAt) = CURRENT_DATE THEN 1 END) as updatedToday " + // Changed DATE to FUNCTION('DATE', ...)
           "FROM Customer c " +
           "UNION ALL " +
           "SELECT " +
           "'Loan Applications' as entityType, " +
           "COUNT(la.id) as totalCount, " +
           "COUNT(CASE WHEN la.expiresAt < CURRENT_TIMESTAMP AND la.status IN ('PENDING', 'UNDER_REVIEW') THEN 1 END) as incompleteCount, " +
           "COUNT(CASE WHEN FUNCTION('DATE', la.appliedAt) = CURRENT_DATE THEN 1 END) as createdToday, " + // Changed DATE to FUNCTION('DATE', ...)
           "COUNT(CASE WHEN FUNCTION('DATE', la.updatedAt) = CURRENT_DATE THEN 1 END) as updatedToday " + // Changed DATE to FUNCTION('DATE', ...)
           "FROM LoanApplication la " +
           "UNION ALL " +
           "SELECT " +
           "'Documents' as entityType, " +
           "COUNT(d.id) as totalCount, " +
           "COUNT(CASE WHEN d.fileSize <= 0 THEN 1 END) as incompleteCount, " +
           "COUNT(CASE WHEN FUNCTION('DATE', d.uploadedAt) = CURRENT_DATE THEN 1 END) as createdToday, " + // Changed DATE to FUNCTION('DATE', ...)
           "COUNT(CASE WHEN FUNCTION('DATE', d.updatedAt) = CURRENT_DATE THEN 1 END) as updatedToday " + // Changed DATE to FUNCTION('DATE', ...)
           "FROM Document d")
    List<Object[]> getSystemHealthReport();

    // Top Performers Report
    /**
     * Шилдэг гүйцэтгэлийн тайлан
     */
    @Query("SELECT " +
           "CONCAT(c.firstName, ' ', c.lastName) as customerName, " +
           "c.registrationNumber, " +
           "c.monthlyIncome, " +
           "COUNT(la.id) as totalApplications, " +
           "COUNT(CASE WHEN la.status = 'APPROVED' THEN 1 END) as approvedApplications, " +
           "SUM(CASE WHEN la.status = 'APPROVED' THEN la.requestedAmount ELSE 0 END) as totalApprovedAmount, " +
           "AVG(CASE WHEN la.status = 'APPROVED' THEN la.requestedAmount END) as avgApprovedAmount, " +
           "MAX(la.appliedAt) as lastApplicationDate " +
           "FROM Customer c " +
           "JOIN c.loanApplications la " +
           "WHERE la.status = 'APPROVED' " +
           "GROUP BY c.id, c.firstName, c.lastName, c.registrationNumber, c.monthlyIncome " +
           "HAVING COUNT(CASE WHEN la.status = 'APPROVED' THEN 1 END) >= :minApprovedLoans " +
           "ORDER BY totalApprovedAmount DESC")
    List<Object[]> getTopPerformersReport(@Param("minApprovedLoans") int minApprovedLoans);

    // Trending Report
    /**
     * Чиг хандлагын тайлан
     */
    @Query("SELECT " +
           "FUNCTION('DATE_FORMAT', la.appliedAt, '%Y-%m') as month, " + // Changed DATE_FORMAT to FUNCTION('DATE_FORMAT', ...)
           "lp.name as productName, " +
           "COUNT(la.id) as applicationCount, " +
           "AVG(la.requestedAmount) as avgAmount, " +
           "COUNT(CASE WHEN la.status = 'APPROVED' THEN 1 END) as approvedCount " +
           "FROM LoanApplication la " +
           "JOIN la.loanProduct lp " +
           "WHERE la.appliedAt >= :startDate " +
           "GROUP BY FUNCTION('DATE_FORMAT', la.appliedAt, '%Y-%m'), lp.id, lp.name " + // Changed DATE_FORMAT to FUNCTION('DATE_FORMAT', ...)
           "ORDER BY month DESC, applicationCount DESC")
    List<Object[]> getTrendingReport(@Param("startDate") LocalDateTime startDate);

    // Custom Report Query
    /**
     * Тусгай тайлан (параметртэй)
     */
    @Query("SELECT " +
           "la.applicationNumber, " +
           "CONCAT(c.firstName, ' ', c.lastName) as customerName, " +
           "c.monthlyIncome, " +
           "lp.name as productName, " +
           "la.requestedAmount, " +
           "la.requestedTermMonths, " +
           "la.status, " +
           "la.appliedAt " +
           "FROM LoanApplication la " +
           "JOIN la.customer c " +
           "JOIN la.loanProduct lp " +
           "WHERE (:status IS NULL OR la.status = :status) " +
           "AND (:minAmount IS NULL OR la.requestedAmount >= :minAmount) " +
           "AND (:maxAmount IS NULL OR la.requestedAmount <= :maxAmount) " +
           "AND (:productId IS NULL OR lp.id = :productId) " +
           "AND (:startDate IS NULL OR la.appliedAt >= :startDate) " +
           "AND (:endDate IS NULL OR la.appliedAt <= :endDate) " +
           "ORDER BY la.appliedAt DESC")
    Page<Object[]> getCustomReport(
            @Param("status") String status,
            @Param("minAmount") BigDecimal minAmount,
            @Param("maxAmount") BigDecimal maxAmount,
            @Param("productId") String productId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);
}
