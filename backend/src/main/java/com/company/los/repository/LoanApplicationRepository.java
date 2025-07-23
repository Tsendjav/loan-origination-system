package com.company.los.repository;

import com.company.los.entity.Customer;
import com.company.los.entity.LoanApplication;
import com.company.los.entity.LoanProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Зээлийн хүсэлтийн Repository
 * Loan Application Repository Interface
 */
@Repository
public interface LoanApplicationRepository extends JpaRepository<LoanApplication, String> {

    // Суурь хайлтууд
    /**
     * Хүсэлтийн дугаараар хайх
     */
    Optional<LoanApplication> findByApplicationNumber(String applicationNumber);

    /**
     * Хүсэлтийн дугаар байгаа эсэхийг шалгах
     */
    boolean existsByApplicationNumber(String applicationNumber);

    // Харилцагчаар хайх
    /**
     * Харилцагчийн зээлийн хүсэлтүүд
     */
    Page<LoanApplication> findByCustomer(Customer customer, Pageable pageable);

    /**
     * Харилцагчийн ID-гаар хайх
     */
    Page<LoanApplication> findByCustomerId(String customerId, Pageable pageable);

    /**
     * Харилцагчийн идэвхтэй хүсэлтүүд
     */
    @Query("SELECT la FROM LoanApplication la WHERE la.customer.id = :customerId AND " +
           "la.status IN ('PENDING', 'UNDER_REVIEW', 'APPROVED')")
    List<LoanApplication> findActiveApplicationsByCustomer(@Param("customerId") String customerId);

    /**
     * Харилцагчийн батлагдсан хүсэлтүүд
     */
    @Query("SELECT la FROM LoanApplication la WHERE la.customer.id = :customerId AND la.status = 'APPROVED'")
    List<LoanApplication> findApprovedApplicationsByCustomer(@Param("customerId") String customerId);

    // Бүтээгдэхүүнээр хайх
    /**
     * Зээлийн бүтээгдэхүүний хүсэлтүүд
     */
    Page<LoanApplication> findByLoanProduct(LoanProduct loanProduct, Pageable pageable);

    /**
     * Бүтээгдэхүүний ID-гаар хайх
     */
    Page<LoanApplication> findByLoanProductId(String loanProductId, Pageable pageable);

    /**
     * Бүтээгдэхүүний нэрээр хайх
     */
    @Query("SELECT la FROM LoanApplication la WHERE la.loanProduct.name = :productName")
    Page<LoanApplication> findByLoanProductName(@Param("productName") String productName, Pageable pageable);

    // Статусаар хайх
    /**
     * Статусаар хайх
     */
    Page<LoanApplication> findByStatus(String status, Pageable pageable);

    /**
     * Хүлээгдэж байгаа хүсэлтүүд
     */
    @Query("SELECT la FROM LoanApplication la WHERE la.status = 'PENDING' ORDER BY la.appliedAt ASC")
    Page<LoanApplication> findPendingApplications(Pageable pageable);

    /**
     * Шалгагдаж байгаа хүсэлтүүд
     */
    @Query("SELECT la FROM LoanApplication la WHERE la.status = 'UNDER_REVIEW' ORDER BY la.appliedAt ASC")
    Page<LoanApplication> findUnderReviewApplications(Pageable pageable);

    /**
     * Батлагдсан хүсэлтүүд
     */
    @Query("SELECT la FROM LoanApplication la WHERE la.status = 'APPROVED' ORDER BY la.approvedAt DESC")
    Page<LoanApplication> findApprovedApplications(Pageable pageable);

    /**
     * Цуцлагдсан хүсэлтүүд
     */
    @Query("SELECT la FROM LoanApplication la WHERE la.status = 'REJECTED' ORDER BY la.rejectedAt DESC")
    Page<LoanApplication> findRejectedApplications(Pageable pageable);

    /**
     * Олон статустай хүсэлтүүд
     */
    @Query("SELECT la FROM LoanApplication la WHERE la.status IN :statuses")
    Page<LoanApplication> findByStatusIn(@Param("statuses") List<String> statuses, Pageable pageable);

    /**
     * Идэвхтэй хүсэлтүүд
     */
    @Query("SELECT la FROM LoanApplication la WHERE la.status IN ('PENDING', 'UNDER_REVIEW', 'APPROVED')")
    Page<LoanApplication> findActiveApplications(Pageable pageable);

    /**
     * Хаагдсан хүсэлтүүд
     */
    @Query("SELECT la FROM LoanApplication la WHERE la.status IN ('REJECTED', 'CANCELLED', 'WITHDRAWN')")
    Page<LoanApplication> findClosedApplications(Pageable pageable);

    // Дүнгээр хайх
    /**
     * Дүнгийн хязгаараар хайх
     */
    @Query("SELECT la FROM LoanApplication la WHERE la.requestedAmount BETWEEN :minAmount AND :maxAmount")
    Page<LoanApplication> findByAmountRange(@Param("minAmount") BigDecimal minAmount,
                                          @Param("maxAmount") BigDecimal maxAmount,
                                          Pageable pageable);

    /**
     * Том дүнтэй хүсэлтүүд
     */
    @Query("SELECT la FROM LoanApplication la WHERE la.requestedAmount >= :largeAmountThreshold " +
           "ORDER BY la.requestedAmount DESC")
    Page<LoanApplication> findLargeAmountApplications(@Param("largeAmountThreshold") BigDecimal largeAmountThreshold,
                                                    Pageable pageable);

    /**
     * Жижиг дүнтэй хүсэлтүүд
     */
    @Query("SELECT la FROM LoanApplication la WHERE la.requestedAmount <= :smallAmountThreshold " +
           "ORDER BY la.requestedAmount ASC")
    Page<LoanApplication> findSmallAmountApplications(@Param("smallAmountThreshold") BigDecimal smallAmountThreshold,
                                                    Pageable pageable);

    // Хугацаагаар хайх
    /**
     * Хугацааны хязгаараар хайх
     */
    @Query("SELECT la FROM LoanApplication la WHERE la.requestedTermMonths BETWEEN :minTerm AND :maxTerm")
    Page<LoanApplication> findByTermRange(@Param("minTerm") Integer minTerm,
                                        @Param("maxTerm") Integer maxTerm,
                                        Pageable pageable);

    /**
     * Богино хугацаатай хүсэлтүүд
     */
    @Query("SELECT la FROM LoanApplication la WHERE la.requestedTermMonths <= :shortTermThreshold " +
           "ORDER BY la.requestedTermMonths ASC")
    Page<LoanApplication> findShortTermApplications(@Param("shortTermThreshold") Integer shortTermThreshold,
                                                  Pageable pageable);

    /**
     * Урт хугацаатай хүсэлтүүд
     */
    @Query("SELECT la FROM LoanApplication la WHERE la.requestedTermMonths >= :longTermThreshold " +
           "ORDER BY la.requestedTermMonths DESC")
    Page<LoanApplication> findLongTermApplications(@Param("longTermThreshold") Integer longTermThreshold,
                                                 Pageable pageable);

    // Огноогоор хайх
    /**
     * Хүсэлт гаргасан огноогоор хайх
     */
    @Query("SELECT la FROM LoanApplication la WHERE la.appliedAt BETWEEN :startDate AND :endDate")
    Page<LoanApplication> findByAppliedAtBetween(@Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate,
                                               Pageable pageable);

    /**
     * Өнөөдөр гаргасан хүсэлтүүд
     */
    @Query("SELECT la FROM LoanApplication la WHERE DATE(la.appliedAt) = CURRENT_DATE " +
           "ORDER BY la.appliedAt DESC")
    List<LoanApplication> findTodayApplications();

    /**
     * Энэ сард гаргасан хүсэлтүүд
     */
    @Query("SELECT la FROM LoanApplication la WHERE " +
           "YEAR(la.appliedAt) = YEAR(CURRENT_DATE) AND " +
           "MONTH(la.appliedAt) = MONTH(CURRENT_DATE)")
    List<LoanApplication> findThisMonthApplications();

    /**
     * Шинэ хүсэлтүүд (сүүлийн 7 хоногт)
     */
    @Query("SELECT la FROM LoanApplication la WHERE la.appliedAt >= :oneWeekAgo ORDER BY la.appliedAt DESC")
    List<LoanApplication> findRecentApplications(@Param("oneWeekAgo") LocalDateTime oneWeekAgo);

    /**
     * Батлагдсан огноогоор хайх
     */
    @Query("SELECT la FROM LoanApplication la WHERE la.approvedAt BETWEEN :startDate AND :endDate")
    Page<LoanApplication> findByApprovedAtBetween(@Param("startDate") LocalDateTime startDate,
                                                @Param("endDate") LocalDateTime endDate,
                                                Pageable pageable);

    /**
     * Хүчингүй болсон огноогоор хайх
     */
    @Query("SELECT la FROM LoanApplication la WHERE la.expiresAt < CURRENT_TIMESTAMP AND " +
           "la.status IN ('PENDING', 'UNDER_REVIEW')")
    List<LoanApplication> findExpiredApplications();

    /**
     * Удахгүй хүчингүй болох хүсэлтүүд
     */
    @Query("SELECT la FROM LoanApplication la WHERE la.expiresAt BETWEEN CURRENT_TIMESTAMP AND :futureDate AND " +
           "la.status IN ('PENDING', 'UNDER_REVIEW')")
    List<LoanApplication> findExpiringSoonApplications(@Param("futureDate") LocalDateTime futureDate);

    // Баримттай холбоотой
    /**
     * Баримттай хүсэлтүүд
     */
    @Query("SELECT la FROM LoanApplication la WHERE SIZE(la.documents) > 0")
    Page<LoanApplication> findApplicationsWithDocuments(Pageable pageable);

    /**
     * Баримтгүй хүсэлтүүд
     */
    @Query("SELECT la FROM LoanApplication la WHERE SIZE(la.documents) = 0")
    Page<LoanApplication> findApplicationsWithoutDocuments(Pageable pageable);

    /**
     * Баримтын тоогоор хайх
     */
    @Query("SELECT la FROM LoanApplication la WHERE SIZE(la.documents) >= :minDocuments")
    List<LoanApplication> findApplicationsWithMinimumDocuments(@Param("minDocuments") int minDocuments);

    /**
     * Дутуу баримттай хүсэлтүүд
     */
    @Query("SELECT la FROM LoanApplication la WHERE SIZE(la.documents) < :requiredDocuments AND " +
           "la.status IN ('PENDING', 'UNDER_REVIEW')")
    List<LoanApplication> findApplicationsWithIncompleteDocuments(@Param("requiredDocuments") int requiredDocuments);

    // Дэвшилтэт хайлт
    /**
     * Дэвшилтэт филтертэй хайлт
     */
    @Query("SELECT la FROM LoanApplication la WHERE " +
           "(:customerId IS NULL OR la.customer.id = :customerId) AND " +
           "(:loanProductId IS NULL OR la.loanProduct.id = :loanProductId) AND " +
           "(:status IS NULL OR la.status = :status) AND " +
           "(:minAmount IS NULL OR la.requestedAmount >= :minAmount) AND " +
           "(:maxAmount IS NULL OR la.requestedAmount <= :maxAmount) AND " +
           "(:minTerm IS NULL OR la.requestedTermMonths >= :minTerm) AND " +
           "(:maxTerm IS NULL OR la.requestedTermMonths <= :maxTerm) AND " +
           "(:startDate IS NULL OR la.appliedAt >= :startDate) AND " +
           "(:endDate IS NULL OR la.appliedAt <= :endDate) AND " +
           "(:hasDocuments IS NULL OR (SIZE(la.documents) > 0) = :hasDocuments) AND " +
           "(:isExpired IS NULL OR (la.expiresAt < CURRENT_TIMESTAMP) = :isExpired)")
    Page<LoanApplication> findByAdvancedFilters(
            @Param("customerId") String customerId,
            @Param("loanProductId") String loanProductId,
            @Param("status") String status,
            @Param("minAmount") BigDecimal minAmount,
            @Param("maxAmount") BigDecimal maxAmount,
            @Param("minTerm") Integer minTerm,
            @Param("maxTerm") Integer maxTerm,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("hasDocuments") Boolean hasDocuments,
            @Param("isExpired") Boolean isExpired,
            Pageable pageable);

    // Статистик
    /**
     * Хүсэлтийн үндсэн статистик
     */
    @Query("SELECT " +
           "COUNT(la) as totalApplications, " +
           "COUNT(CASE WHEN la.status = 'PENDING' THEN 1 END) as pendingCount, " +
           "COUNT(CASE WHEN la.status = 'UNDER_REVIEW' THEN 1 END) as reviewCount, " +
           "COUNT(CASE WHEN la.status = 'APPROVED' THEN 1 END) as approvedCount, " +
           "COUNT(CASE WHEN la.status = 'REJECTED' THEN 1 END) as rejectedCount, " +
           "AVG(la.requestedAmount) as avgAmount, " +
           "SUM(la.requestedAmount) as totalAmount " +
           "FROM LoanApplication la")
    Object[] getApplicationStats();

    /**
     * Статусаар тоолох
     */
    @Query("SELECT la.status, COUNT(la) FROM LoanApplication la " +
           "GROUP BY la.status ORDER BY COUNT(la) DESC")
    List<Object[]> countByStatus();

    /**
     * Бүтээгдэхүүнээр тоолох
     */
    @Query("SELECT lp.name, COUNT(la) FROM LoanApplication la JOIN la.loanProduct lp " +
           "GROUP BY lp.name ORDER BY COUNT(la) DESC")
    List<Object[]> countByLoanProduct();

    /**
     * Сарын хүсэлтийн статистик
     */
    @Query("SELECT DATE_FORMAT(la.appliedAt, '%Y-%m'), COUNT(la) FROM LoanApplication la " +
           "WHERE la.appliedAt >= :startDate " +
           "GROUP BY DATE_FORMAT(la.appliedAt, '%Y-%m') " +
           "ORDER BY DATE_FORMAT(la.appliedAt, '%Y-%m')")
    List<Object[]> getMonthlyApplicationStats(@Param("startDate") LocalDateTime startDate);

    /**
     * Дүнгийн бүлгээр тоолох
     */
    @Query("SELECT " +
           "CASE " +
           "WHEN la.requestedAmount < 1000000 THEN 'Under 1M' " +
           "WHEN la.requestedAmount BETWEEN 1000000 AND 5000000 THEN '1M-5M' " +
           "WHEN la.requestedAmount BETWEEN 5000001 AND 10000000 THEN '5M-10M' " +
           "WHEN la.requestedAmount BETWEEN 10000001 AND 50000000 THEN '10M-50M' " +
           "ELSE 'Over 50M' END as amountGroup, " +
           "COUNT(la) " +
           "FROM LoanApplication la " +
           "GROUP BY " +
           "CASE " +
           "WHEN la.requestedAmount < 1000000 THEN 'Under 1M' " +
           "WHEN la.requestedAmount BETWEEN 1000000 AND 5000000 THEN '1M-5M' " +
           "WHEN la.requestedAmount BETWEEN 5000001 AND 10000000 THEN '5M-10M' " +
           "WHEN la.requestedAmount BETWEEN 10000001 AND 50000000 THEN '10M-50M' " +
           "ELSE 'Over 50M' END")
    List<Object[]> countByAmountGroup();

    /**
     * Батлалтын хувь
     */
    @Query("SELECT " +
           "COUNT(CASE WHEN la.status = 'APPROVED' THEN 1 END) * 100.0 / COUNT(la) as approvalRate, " +
           "COUNT(CASE WHEN la.status = 'REJECTED' THEN 1 END) * 100.0 / COUNT(la) as rejectionRate " +
           "FROM LoanApplication la WHERE la.status IN ('APPROVED', 'REJECTED')")
    Object[] getApprovalStats();

    // Bulk операциуд
    /**
     * Олон хүсэлтийн статус өөрчлөх
     */
    @Modifying
    @Query("UPDATE LoanApplication la SET la.status = :newStatus, la.updatedBy = :updatedBy " +
           "WHERE la.id IN :applicationIds")
    int updateStatusForApplications(@Param("applicationIds") List<String> applicationIds,
                                  @Param("newStatus") String newStatus,
                                  @Param("updatedBy") String updatedBy);

    /**
     * Хүчингүй болсон хүсэлтүүдийг цуцлах
     */
    @Modifying
    @Query("UPDATE LoanApplication la SET la.status = 'EXPIRED', la.updatedBy = :updatedBy " +
           "WHERE la.expiresAt < CURRENT_TIMESTAMP AND la.status IN ('PENDING', 'UNDER_REVIEW')")
    int expireOverdueApplications(@Param("updatedBy") String updatedBy);

    /**
     * Батлалтын огноо тохируулах
     */
    @Modifying
    @Query("UPDATE LoanApplication la SET la.approvedAt = CURRENT_TIMESTAMP, la.updatedBy = :updatedBy " +
           "WHERE la.id IN :applicationIds")
    int setApprovedDate(@Param("applicationIds") List<String> applicationIds,
                      @Param("updatedBy") String updatedBy);

    /**
     * Цуцлалтын огноо тохируулах
     */
    @Modifying
    @Query("UPDATE LoanApplication la SET la.rejectedAt = CURRENT_TIMESTAMP, la.updatedBy = :updatedBy " +
           "WHERE la.id IN :applicationIds")
    int setRejectedDate(@Param("applicationIds") List<String> applicationIds,
                      @Param("updatedBy") String updatedBy);

    // Validation
    /**
     * Хүсэлтийн дугаар давхцаж байгаа эсэхийг шалгах
     */
    @Query("SELECT COUNT(la) > 0 FROM LoanApplication la WHERE " +
           "la.applicationNumber = :applicationNumber AND la.id != :excludeId")
    boolean existsByApplicationNumberAndIdNot(@Param("applicationNumber") String applicationNumber,
                                            @Param("excludeId") String excludeId);

    /**
     * Харилцагчийн идэвхтэй хүсэлт байгаа эсэхийг шалгах
     */
    @Query("SELECT COUNT(la) > 0 FROM LoanApplication la WHERE " +
           "la.customer.id = :customerId AND la.status IN ('PENDING', 'UNDER_REVIEW', 'APPROVED')")
    boolean hasActiveApplications(@Param("customerId") String customerId);

    // Business logic
    /**
     * Шаардагатай анхаарал хүсэлтүүд (урт хугацаанд шалгагдаагүй)
     */
    @Query("SELECT la FROM LoanApplication la WHERE " +
           "la.status IN ('PENDING', 'UNDER_REVIEW') AND " +
           "la.appliedAt < :attentionThreshold " +
           "ORDER BY la.appliedAt ASC")
    List<LoanApplication> findApplicationsNeedingAttention(@Param("attentionThreshold") LocalDateTime attentionThreshold);

    /**
     * Эрсдэлтэй хүсэлтүүд (том дүн, урт хугацаа)
     */
    @Query("SELECT la FROM LoanApplication la WHERE " +
           "la.requestedAmount >= :highRiskAmount OR " +
           "la.requestedTermMonths >= :highRiskTerm " +
           "ORDER BY la.requestedAmount DESC")
    List<LoanApplication> findHighRiskApplications(@Param("highRiskAmount") BigDecimal highRiskAmount,
                                                 @Param("highRiskTerm") Integer highRiskTerm);

    /**
     * Хурдан батлах боломжтой хүсэлтүүд
     */
    @Query("SELECT la FROM LoanApplication la WHERE " +
           "la.requestedAmount <= :fastApprovalAmount AND " +
           "la.requestedTermMonths <= :fastApprovalTerm AND " +
           "la.status = 'PENDING' AND " +
           "SIZE(la.documents) >= :minDocuments " +
           "ORDER BY la.appliedAt ASC")
    List<LoanApplication> findFastApprovalCandidates(@Param("fastApprovalAmount") BigDecimal fastApprovalAmount,
                                                   @Param("fastApprovalTerm") Integer fastApprovalTerm,
                                                   @Param("minDocuments") int minDocuments);

    // Dashboard статистик
    /**
     * Өнөөдрийн хүсэлтийн статистик
     */
    @Query("SELECT " +
           "COUNT(CASE WHEN DATE(la.appliedAt) = CURRENT_DATE THEN 1 END) as newToday, " +
           "COUNT(CASE WHEN DATE(la.approvedAt) = CURRENT_DATE THEN 1 END) as approvedToday, " +
           "COUNT(CASE WHEN DATE(la.rejectedAt) = CURRENT_DATE THEN 1 END) as rejectedToday, " +
           "COUNT(CASE WHEN la.status = 'PENDING' THEN 1 END) as pendingTotal " +
           "FROM LoanApplication la")
    Object[] getTodayApplicationStats();

    /**
     * Ажлын ачаалалын статистик
     */
    @Query("SELECT " +
           "COUNT(CASE WHEN la.status = 'PENDING' THEN 1 END) as pendingWorkload, " +
           "COUNT(CASE WHEN la.status = 'UNDER_REVIEW' THEN 1 END) as reviewWorkload, " +
           "COUNT(CASE WHEN la.expiresAt < :nearExpiry AND la.status IN ('PENDING', 'UNDER_REVIEW') THEN 1 END) as nearExpiryCount " +
           "FROM LoanApplication la")
    Object[] getWorkloadStats(@Param("nearExpiry") LocalDateTime nearExpiry);

    // Performance monitoring
    /**
     * Хамгийн их хүсэлттэй харилцагчид
     */
    @Query("SELECT c.firstName, c.lastName, COUNT(la) as applicationCount FROM LoanApplication la " +
           "JOIN la.customer c " +
           "GROUP BY c.id, c.firstName, c.lastName " +
           "ORDER BY applicationCount DESC")
    Page<Object[]> findCustomersWithMostApplications(Pageable pageable);

    /**
     * Хамгийн их ашиглагддаг бүтээгдэхүүн
     */
    @Query("SELECT lp.name, COUNT(la) as applicationCount FROM LoanApplication la " +
           "JOIN la.loanProduct lp " +
           "GROUP BY lp.id, lp.name " +
           "ORDER BY applicationCount DESC")
    Page<Object[]> findMostPopularProducts(Pageable pageable);

    /**
     * Сүүлийн өөрчлөлт хийсэн хүсэлтүүд
     */
    @Query("SELECT la FROM LoanApplication la ORDER BY la.updatedAt DESC")
    Page<LoanApplication> findRecentlyModified(Pageable pageable);

    // Data quality
    /**
     * Дутуу мэдээлэлтэй хүсэлтүүд
     */
    @Query("SELECT la FROM LoanApplication la WHERE " +
           "la.requestedAmount IS NULL OR " +
           "la.requestedTermMonths IS NULL OR " +
           "la.purpose IS NULL OR la.purpose = ''")
    List<LoanApplication> findApplicationsWithIncompleteData();

    /**
     * Хүчингүй хязгаартай хүсэлтүүд
     */
    @Query("SELECT la FROM LoanApplication la WHERE " +
           "la.requestedAmount <= 0 OR " +
           "la.requestedTermMonths <= 0")
    List<LoanApplication> findApplicationsWithInvalidData();

    // Cleanup
    /**
     * Хуучин цуцлагдсан хүсэлтүүд
     */
    @Query("SELECT la FROM LoanApplication la WHERE " +
           "la.status IN ('REJECTED', 'CANCELLED', 'WITHDRAWN', 'EXPIRED') AND " +
           "la.updatedAt < :oldDate")
    List<LoanApplication> findOldClosedApplications(@Param("oldDate") LocalDateTime oldDate);

    // Processing queue
    /**
     * Дараагийн боловсруулах хүсэлтүүд
     */
    @Query("SELECT la FROM LoanApplication la WHERE la.status = 'PENDING' " +
           "ORDER BY la.appliedAt ASC")
    Page<LoanApplication> findNextToProcess(Pageable pageable);

    /**
     * Шалгалт хийх ээлжиндхүсэлтүүд
     */
    @Query("SELECT la FROM LoanApplication la WHERE la.status = 'UNDER_REVIEW' " +
           "ORDER BY la.appliedAt ASC")
    Page<LoanApplication> findForReview(Pageable pageable);
}