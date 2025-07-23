package com.company.los.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Dashboard Repository
 * Dashboard statistics and real-time metrics
 */
@Repository
public interface DashboardRepository extends JpaRepository<Object, String> {

    // Main Dashboard Overview
    /**
     * Үндсэн dashboard статистик
     */
    @Query("SELECT " +
           "(SELECT COUNT(c) FROM Customer c) as totalCustomers, " +
           "(SELECT COUNT(la) FROM LoanApplication la) as totalApplications, " +
           "(SELECT COUNT(la) FROM LoanApplication la WHERE la.status = 'PENDING') as pendingApplications, " +
           "(SELECT COUNT(la) FROM LoanApplication la WHERE la.status = 'APPROVED') as approvedApplications, " +
           "(SELECT COUNT(la) FROM LoanApplication la WHERE la.status = 'REJECTED') as rejectedApplications, " +
           "(SELECT COUNT(d) FROM Document d) as totalDocuments, " +
           "(SELECT COUNT(lp) FROM LoanProduct lp WHERE lp.isActive = true) as activeProducts, " +
           "(SELECT COUNT(u) FROM User u WHERE u.isActive = true) as activeUsers")
    Object[] getMainDashboardStats();

    /**
     * Өнөөдрийн үйл ажиллагааны статистик
     */
    @Query("SELECT " +
           "(SELECT COUNT(c) FROM Customer c WHERE DATE(c.createdAt) = CURRENT_DATE) as newCustomersToday, " +
           "(SELECT COUNT(la) FROM LoanApplication la WHERE DATE(la.appliedAt) = CURRENT_DATE) as newApplicationsToday, " +
           "(SELECT COUNT(la) FROM LoanApplication la WHERE DATE(la.approvedAt) = CURRENT_DATE) as approvedToday, " +
           "(SELECT COUNT(la) FROM LoanApplication la WHERE DATE(la.rejectedAt) = CURRENT_DATE) as rejectedToday, " +
           "(SELECT COUNT(d) FROM Document d WHERE DATE(d.uploadedAt) = CURRENT_DATE) as documentsUploadedToday, " +
           "(SELECT COUNT(al) FROM AuditLog al WHERE DATE(al.changedAt) = CURRENT_DATE) as systemActionsToday")
    Object[] getTodayActivityStats();

    // Financial Dashboard
    /**
     * Санхүүгийн dashboard статистик
     */
    @Query("SELECT " +
           "(SELECT COALESCE(SUM(la.requestedAmount), 0) FROM LoanApplication la WHERE la.status = 'PENDING') as pendingAmount, " +
           "(SELECT COALESCE(SUM(la.requestedAmount), 0) FROM LoanApplication la WHERE la.status = 'APPROVED') as approvedAmount, " +
           "(SELECT COALESCE(SUM(la.requestedAmount), 0) FROM LoanApplication la WHERE DATE(la.appliedAt) = CURRENT_DATE) as todayRequestedAmount, " +
           "(SELECT COALESCE(SUM(la.requestedAmount), 0) FROM LoanApplication la WHERE DATE(la.approvedAt) = CURRENT_DATE) as todayApprovedAmount, " +
           "(SELECT COALESCE(AVG(la.requestedAmount), 0) FROM LoanApplication la WHERE la.status = 'APPROVED') as avgApprovedAmount, " +
           "(SELECT COALESCE(MAX(la.requestedAmount), 0) FROM LoanApplication la WHERE la.status = 'APPROVED') as maxApprovedAmount")
    Object[] getFinancialDashboardStats();

    // Performance Metrics
    /**
     * Гүйцэтгэлийн үзүүлэлт
     */
    @Query("SELECT " +
           "COALESCE(ROUND(" +
           "  (SELECT COUNT(la) FROM LoanApplication la WHERE la.status = 'APPROVED') * 100.0 / " +
           "  NULLIF((SELECT COUNT(la) FROM LoanApplication la WHERE la.status IN ('APPROVED', 'REJECTED')), 0)" +
           ", 2), 0) as approvalRate, " +
           "COALESCE((" +
           "  SELECT AVG(DATEDIFF(COALESCE(la.approvedAt, la.rejectedAt, CURRENT_DATE), la.appliedAt)) " +
           "  FROM LoanApplication la " +
           "  WHERE la.status IN ('APPROVED', 'REJECTED')" +
           "), 0) as avgProcessingDays, " +
           "(SELECT COUNT(DISTINCT la.customer.id) FROM LoanApplication la WHERE la.status = 'APPROVED') as uniqueApprovedCustomers, " +
           "(SELECT COUNT(la) FROM LoanApplication la WHERE la.expiresAt < CURRENT_TIMESTAMP AND la.status IN ('PENDING', 'UNDER_REVIEW')) as expiredApplications")
    Object[] getPerformanceMetrics();

    // Workload Dashboard
    /**
     * Ажлын ачаалалын статистик
     */
    @Query("SELECT " +
           "(SELECT COUNT(la) FROM LoanApplication la WHERE la.status = 'PENDING') as pendingWorkload, " +
           "(SELECT COUNT(la) FROM LoanApplication la WHERE la.status = 'UNDER_REVIEW') as reviewWorkload, " +
           "(SELECT COUNT(la) FROM LoanApplication la WHERE la.expiresAt BETWEEN CURRENT_TIMESTAMP AND :nearExpiry AND la.status IN ('PENDING', 'UNDER_REVIEW')) as nearExpiryCount, " +
           "(SELECT COUNT(la) FROM LoanApplication la WHERE DATEDIFF(CURRENT_DATE, la.appliedAt) > 7 AND la.status IN ('PENDING', 'UNDER_REVIEW')) as overdue, " +
           "(SELECT COUNT(c) FROM Customer c WHERE c.id NOT IN (SELECT DISTINCT d.customer.id FROM Document d WHERE d.documentType.isRequired = true)) as customersNeedingDocs")
    Object[] getWorkloadStats(@Param("nearExpiry") LocalDateTime nearExpiry);

    // Recent Activity Dashboard
    /**
     * Сүүлийн үйл ажиллагаа (топ 10)
     */
    @Query("SELECT " +
           "al.changedAt, " +
           "al.tableName, " +
           "al.action, " +
           "al.recordId, " +
           "al.changedBy " +
           "FROM AuditLog al " +
           "ORDER BY al.changedAt DESC " +
           "LIMIT 10")
    List<Object[]> getRecentActivity();

    /**
     * Сүүлийн зээлийн хүсэлтүүд (топ 5)
     */
    @Query("SELECT " +
           "la.applicationNumber, " +
           "CONCAT(c.firstName, ' ', c.lastName) as customerName, " +
           "lp.name as productName, " +
           "la.requestedAmount, " +
           "la.status, " +
           "la.appliedAt " +
           "FROM LoanApplication la " +
           "JOIN la.customer c " +
           "JOIN la.loanProduct lp " +
           "ORDER BY la.appliedAt DESC " +
           "LIMIT 5")
    List<Object[]> getRecentLoanApplications();

    /**
     * Сүүлийн шинэ харилцагчид (топ 5)
     */
    @Query("SELECT " +
           "CONCAT(c.firstName, ' ', c.lastName) as customerName, " +
           "c.email, " +
           "c.phone, " +
           "c.monthlyIncome, " +
           "c.createdAt " +
           "FROM Customer c " +
           "ORDER BY c.createdAt DESC " +
           "LIMIT 5")
    List<Object[]> getRecentCustomers();

    // Alert Dashboard
    /**
     * Анхааруулгын статистик
     */
    @Query("SELECT " +
           "(SELECT COUNT(la) FROM LoanApplication la WHERE la.expiresAt < CURRENT_TIMESTAMP AND la.status IN ('PENDING', 'UNDER_REVIEW')) as expiredApplications, " +
           "(SELECT COUNT(la) FROM LoanApplication la WHERE DATEDIFF(CURRENT_DATE, la.appliedAt) > 10 AND la.status = 'PENDING') as stalledApplications, " +
           "(SELECT COUNT(c) FROM Customer c WHERE SIZE(c.loanApplications) = 0 AND c.createdAt < :oldCustomerDate) as inactiveCustomers, " +
           "(SELECT COUNT(d) FROM Document d WHERE d.fileSize > 104857600) as largeDocs, " +
           "(SELECT COUNT(u) FROM User u WHERE u.lastLoginAt < :inactiveUserDate) as inactiveUsers, " +
           "(SELECT COUNT(DISTINCT al.ipAddress) FROM AuditLog al WHERE al.changedAt >= :recentTime GROUP BY al.ipAddress HAVING COUNT(al) > 100) as suspiciousActivity")
    Object[] getAlertStats(@Param("oldCustomerDate") LocalDateTime oldCustomerDate,
                         @Param("inactiveUserDate") LocalDateTime inactiveUserDate,
                         @Param("recentTime") LocalDateTime recentTime);

    // Trend Analysis
    /**
     * 7 хоногийн чиг хандлага
     */
    @Query("SELECT " +
           "DATE(dt.day) as date, " +
           "COALESCE(stats.applicationCount, 0) as applicationCount, " +
           "COALESCE(stats.approvedCount, 0) as approvedCount, " +
           "COALESCE(stats.customerCount, 0) as customerCount " +
           "FROM (" +
           "  SELECT DATE(CURRENT_DATE - INTERVAL (a.a + (10 * b.a)) DAY) as day " +
           "  FROM (SELECT 0 as a UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6) as a " +
           "  CROSS JOIN (SELECT 0 as a) as b " +
           "  ORDER BY day DESC " +
           "  LIMIT 7" +
           ") dt " +
           "LEFT JOIN (" +
           "  SELECT " +
           "    DATE(la.appliedAt) as date, " +
           "    COUNT(la.id) as applicationCount, " +
           "    COUNT(CASE WHEN la.status = 'APPROVED' THEN 1 END) as approvedCount, " +
           "    0 as customerCount " +
           "  FROM LoanApplication la " +
           "  WHERE la.appliedAt >= CURRENT_DATE - INTERVAL 7 DAY " +
           "  GROUP BY DATE(la.appliedAt) " +
           "  UNION ALL " +
           "  SELECT " +
           "    DATE(c.createdAt) as date, " +
           "    0 as applicationCount, " +
           "    0 as approvedCount, " +
           "    COUNT(c.id) as customerCount " +
           "  FROM Customer c " +
           "  WHERE c.createdAt >= CURRENT_DATE - INTERVAL 7 DAY " +
           "  GROUP BY DATE(c.createdAt) " +
           ") stats ON DATE(dt.day) = stats.date " +
           "ORDER BY dt.day DESC")
    List<Object[]> getWeeklyTrends();

    /**
     * 12 сарын чиг хандлага
     */
    @Query("SELECT " +
           "DATE_FORMAT(la.appliedAt, '%Y-%m') as month, " +
           "COUNT(la.id) as applicationCount, " +
           "COUNT(CASE WHEN la.status = 'APPROVED' THEN 1 END) as approvedCount, " +
           "COALESCE(SUM(CASE WHEN la.status = 'APPROVED' THEN la.requestedAmount ELSE 0 END), 0) as approvedAmount " +
           "FROM LoanApplication la " +
           "WHERE la.appliedAt >= DATE_SUB(CURRENT_DATE, INTERVAL 12 MONTH) " +
           "GROUP BY DATE_FORMAT(la.appliedAt, '%Y-%m') " +
           "ORDER BY month DESC " +
           "LIMIT 12")
    List<Object[]> getMonthlyTrends();

    // Product Dashboard
    /**
     * Бүтээгдэхүүний гүйцэтгэл (топ 5)
     */
    @Query("SELECT " +
           "lp.name as productName, " +
           "COUNT(la.id) as applicationCount, " +
           "COUNT(CASE WHEN la.status = 'APPROVED' THEN 1 END) as approvedCount, " +
           "COALESCE(ROUND(COUNT(CASE WHEN la.status = 'APPROVED' THEN 1 END) * 100.0 / NULLIF(COUNT(la.id), 0), 2), 0) as approvalRate, " +
           "COALESCE(SUM(CASE WHEN la.status = 'APPROVED' THEN la.requestedAmount ELSE 0 END), 0) as approvedAmount " +
           "FROM LoanProduct lp " +
           "LEFT JOIN lp.loanApplications la " +
           "WHERE lp.isActive = true " +
           "GROUP BY lp.id, lp.name " +
           "ORDER BY applicationCount DESC " +
           "LIMIT 5")
    List<Object[]> getTopProductPerformance();

    // Geographic Dashboard
    /**
     * Хот/аймгийн статистик (топ 5)
     */
    @Query("SELECT " +
           "c.province, " +
           "COUNT(DISTINCT c.id) as customerCount, " +
           "COUNT(la.id) as applicationCount, " +
           "COALESCE(SUM(CASE WHEN la.status = 'APPROVED' THEN la.requestedAmount ELSE 0 END), 0) as approvedAmount " +
           "FROM Customer c " +
           "LEFT JOIN c.loanApplications la " +
           "WHERE c.province IS NOT NULL " +
           "GROUP BY c.province " +
           "ORDER BY customerCount DESC " +
           "LIMIT 5")
    List<Object[]> getTopProvinceStats();

    // User Activity Dashboard
    /**
     * Хамгийн идэвхтэй хэрэглэгчид (топ 5)
     */
    @Query("SELECT " +
           "al.changedBy as username, " +
           "COUNT(al.id) as actionCount, " +
           "COUNT(DISTINCT al.tableName) as tablesModified, " +
           "MAX(al.changedAt) as lastActivity " +
           "FROM AuditLog al " +
           "WHERE al.changedAt >= :startDate " +
           "AND al.changedBy IS NOT NULL " +
           "GROUP BY al.changedBy " +
           "ORDER BY actionCount DESC " +
           "LIMIT 5")
    List<Object[]> getMostActiveUsers(@Param("startDate") LocalDateTime startDate);

    // Document Dashboard
    /**
     * Баримтын статистик
     */
    @Query("SELECT " +
           "(SELECT COUNT(d) FROM Document d) as totalDocuments, " +
           "(SELECT COUNT(d) FROM Document d WHERE DATE(d.uploadedAt) = CURRENT_DATE) as uploadedToday, " +
           "(SELECT COALESCE(SUM(d.fileSize), 0) FROM Document d) as totalFileSize, " +
           "(SELECT COUNT(DISTINCT d.customer.id) FROM Document d) as customersWithDocs, " +
           "(SELECT COUNT(c) FROM Customer c WHERE c.id NOT IN (SELECT DISTINCT d2.customer.id FROM Document d2)) as customersWithoutDocs, " +
           "(SELECT COUNT(d) FROM Document d WHERE d.fileSize > 10485760) as largeFiles")
    Object[] getDocumentDashboardStats();

    // System Performance Dashboard
    /**
     * Системийн гүйцэтгэлийн статистик
     */
    @Query("SELECT " +
           "(SELECT COUNT(al) FROM AuditLog al WHERE al.changedAt >= :recentTime) as recentActions, " +
           "(SELECT COUNT(DISTINCT al.changedBy) FROM AuditLog al WHERE al.changedAt >= :recentTime) as activeUsers, " +
           "(SELECT COUNT(la) FROM LoanApplication la WHERE la.updatedAt >= :recentTime) as recentUpdates, " +
           "(SELECT COUNT(DISTINCT al.ipAddress) FROM AuditLog al WHERE al.changedAt >= :recentTime) as uniqueIPs, " +
           "(SELECT AVG(DATEDIFF(CURRENT_DATE, la.appliedAt)) FROM LoanApplication la WHERE la.status = 'PENDING') as avgPendingDays")
    Object[] getSystemPerformanceStats(@Param("recentTime") LocalDateTime recentTime);

    // Risk Dashboard
    /**
     * Эрсдэлийн статистик
     */
    @Query("SELECT " +
           "(SELECT COUNT(la) FROM LoanApplication la WHERE la.requestedAmount >= 50000000 AND la.status = 'PENDING') as highAmountPending, " +
           "(SELECT COUNT(c) FROM Customer c JOIN c.loanApplications la WHERE la.status = 'REJECTED' GROUP BY c.id HAVING COUNT(la) >= 3) as multipleRejectedCustomers, " +
           "(SELECT COUNT(la) FROM LoanApplication la WHERE la.requestedAmount > (SELECT lp.maxAmount FROM LoanProduct lp WHERE lp.id = la.loanProduct.id)) as exceededLimitApps, " +
           "(SELECT COUNT(DISTINCT c.id) FROM Customer c WHERE c.monthlyIncome IS NULL AND SIZE(c.loanApplications) > 0) as noIncomeWithLoans, " +
           "(SELECT COUNT(la) FROM LoanApplication la WHERE SIZE(la.documents) = 0 AND la.status IN ('PENDING', 'UNDER_REVIEW')) as noDocsApplications")
    Object[] getRiskDashboardStats();

    // Quick Stats for Cards
    /**
     * Хурдан статистик картууд
     */
    @Query("SELECT " +
           "'CUSTOMERS' as metric, " +
           "(SELECT COUNT(c) FROM Customer c) as value, " +
           "(SELECT COUNT(c) FROM Customer c WHERE DATE(c.createdAt) = CURRENT_DATE) as todayChange, " +
           "'+' as changeDirection " +
           "UNION ALL " +
           "SELECT " +
           "'APPLICATIONS' as metric, " +
           "(SELECT COUNT(la) FROM LoanApplication la) as value, " +
           "(SELECT COUNT(la) FROM LoanApplication la WHERE DATE(la.appliedAt) = CURRENT_DATE) as todayChange, " +
           "'+' as changeDirection " +
           "UNION ALL " +
           "SELECT " +
           "'PENDING' as metric, " +
           "(SELECT COUNT(la) FROM LoanApplication la WHERE la.status = 'PENDING') as value, " +
           "(SELECT COUNT(la) FROM LoanApplication la WHERE la.status = 'PENDING' AND DATE(la.appliedAt) = CURRENT_DATE) as todayChange, " +
           "'+' as changeDirection " +
           "UNION ALL " +
           "SELECT " +
           "'APPROVED_AMOUNT' as metric, " +
           "(SELECT COALESCE(SUM(la.requestedAmount), 0) FROM LoanApplication la WHERE la.status = 'APPROVED') as value, " +
           "(SELECT COALESCE(SUM(la.requestedAmount), 0) FROM LoanApplication la WHERE la.status = 'APPROVED' AND DATE(la.approvedAt) = CURRENT_DATE) as todayChange, " +
           "'+' as changeDirection")
    List<Object[]> getQuickStatsCards();

    // Custom Dashboard Query
    /**
     * Тусгайлсан dashboard query
     */
    @Query("SELECT " +
           "DATE_FORMAT(:date, '%Y-%m-%d') as queryDate, " +
           "(SELECT COUNT(la) FROM LoanApplication la WHERE DATE(la.appliedAt) = :date) as applicationsOnDate, " +
           "(SELECT COUNT(c) FROM Customer c WHERE DATE(c.createdAt) = :date) as customersOnDate, " +
           "(SELECT COUNT(d) FROM Document d WHERE DATE(d.uploadedAt) = :date) as documentsOnDate, " +
           "(SELECT COUNT(al) FROM AuditLog al WHERE DATE(al.changedAt) = :date) as actionsOnDate")
    Object[] getDashboardStatsForDate(@Param("date") LocalDateTime date);
}