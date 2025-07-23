package com.company.los.repository;

import com.company.los.entity.Customer;
import com.company.los.entity.Document;
import com.company.los.entity.DocumentType;
import com.company.los.entity.LoanApplication;
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

/**
 * Баримт бичгийн Repository
 * Document Repository Interface
 */
@Repository
public interface DocumentRepository extends JpaRepository<Document, String> {

    // Суурь хайлтууд
    /**
     * Харилцагчийн ID-гаар хайх
     */
    Page<Document> findByCustomerId(String customerId, Pageable pageable);
    List<Document> findByCustomerId(String customerId);

    /**
     * Зээлийн хүсэлтийн ID-гаар хайх
     */
    Page<Document> findByLoanApplicationId(String loanApplicationId, Pageable pageable);
    List<Document> findByLoanApplicationId(String loanApplicationId);

    /**
     * Баримтын төрлөөр хайх
     */
    Page<Document> findByDocumentType(DocumentType documentType, Pageable pageable);
    List<Document> findByDocumentType(DocumentType documentType);

    /**
     * Баталгаажуулсан хүнээр хайх
     */
    List<Document> findByVerifiedBy(String verifiedBy);

    /**
     * Харилцагчийн тодорхой төрлийн баримт
     */
    @Query("SELECT d FROM Document d WHERE d.customer.id = :customerId AND d.documentType = :documentType")
    Optional<Document> findByCustomerIdAndDocumentType(@Param("customerId") String customerId,
                                                       @Param("documentType") DocumentType documentType);

    /**
     * Харилцагчийн тодорхой төрлийн баримтууд
     */
    @Query("SELECT d FROM Document d WHERE d.customer.id = :customerId AND d.documentType = :documentType")
    List<Document> findAllByCustomerIdAndDocumentType(@Param("customerId") String customerId,
                                                      @Param("documentType") DocumentType documentType);

    /**
     * Зээлийн хүсэлтийн тодорхой төрлийн баримт
     */
    @Query("SELECT d FROM Document d WHERE d.loanApplication.id = :loanApplicationId AND d.documentType = :documentType")
    List<Document> findByLoanApplicationAndDocumentType(@Param("loanApplicationId") String loanApplicationId,
                                                       @Param("documentType") DocumentType documentType);

    // Баталгаажуулалтын статусаар хайх
    /**
     * Баталгаажуулалтын статусаар хайх
     */
    @Query("SELECT d FROM Document d WHERE d.verificationStatus = :status")
    Page<Document> findByVerificationStatus(@Param("status") Document.VerificationStatus status, Pageable pageable);

    /**
     * Хүлээгдэж байгаа баталгаажуулалт
     */
    @Query("SELECT d FROM Document d WHERE d.verificationStatus = 'PENDING'")
    Page<Document> findPendingVerification(Pageable pageable);

    /**
     * Хянаж байгаа баримтууд
     */
    @Query("SELECT d FROM Document d WHERE d.verifiedBy = :reviewerName AND d.verificationStatus = 'IN_REVIEW'")
    List<Document> findInReviewByReviewer(@Param("reviewerName") String reviewerName);

    /**
     * Баталгаажуулсан баримтууд
     */
    @Query("SELECT d FROM Document d WHERE d.verificationStatus = 'APPROVED' AND d.verifiedAt BETWEEN :startDate AND :endDate")
    Page<Document> findApprovedBetween(@Param("startDate") LocalDateTime startDate,
                                      @Param("endDate") LocalDateTime endDate,
                                      Pageable pageable);

    /**
     * Татгалзсан баримтууд
     */
    @Query("SELECT d FROM Document d WHERE d.verifiedBy = :reviewerName AND d.verificationStatus = 'REJECTED'")
    Page<Document> findRejectedByReviewer(@Param("reviewerName") String reviewerName, Pageable pageable);

    /**
     * Дахин илгээх шаардлагатай баримтууд
     */
    @Query("SELECT d FROM Document d WHERE d.verificationStatus = 'RESUBMIT_REQUIRED'")
    Page<Document> findRequiringResubmission(Pageable pageable);

    // Хугацаатай холбоотой хайлт
    /**
     * Хугацаа дууссан баримтууд
     */
    @Query("SELECT d FROM Document d WHERE d.expiryDate < CURRENT_DATE")
    List<Document> findExpiredDocuments();

    /**
     * Удахгүй хугацаа дуусах баримтууд
     */
    @Query("SELECT d FROM Document d WHERE d.expiryDate <= :futureDate AND d.expiryDate > CURRENT_DATE")
    List<Document> findExpiringSoonDocuments(@Param("futureDate") LocalDate futureDate);

    /**
     * Сүүлийн баримтууд
     */
    @Query("SELECT d FROM Document d ORDER BY d.uploadedAt DESC")
    List<Document> findRecentDocuments(int limit);

    /**
     * Хуучин баримтууд
     */
    @Query("SELECT d FROM Document d WHERE d.uploadedAt < :cutoffDate")
    List<Document> findOldDocuments(@Param("cutoffDate") LocalDateTime cutoffDate);

    // OCR болон AI холбоотой
    /**
     * OCR амжилтгүй болсон баримтууд
     */
    @Query("SELECT d FROM Document d WHERE d.processingStatus = 'FAILED'")
    List<Document> findOcrFailed();

    /**
     * Өндөр итгэлцлийн оноотой баримтууд
     */
    @Query("SELECT d FROM Document d WHERE d.aiConfidenceScore >= :minScore")
    Page<Document> findByHighConfidenceScore(@Param("minScore") BigDecimal minScore, Pageable pageable);

    /**
     * Бага итгэлцлийн оноотой баримтууд
     */
    @Query("SELECT d FROM Document d WHERE d.aiConfidenceScore < :threshold")
    List<Document> findLowConfidenceDocuments(@Param("threshold") BigDecimal threshold);

    // Дупликат хайлт
    /**
     * Дупликат баримт хайх (энгийн хувилбар)
     */
    @Query("SELECT d FROM Document d WHERE (d.originalFilename = :filename OR d.checksum = :checksum OR d.fileSize = :fileSize) AND d.customer != :excludeCustomer")
    List<Document> findDuplicates(@Param("filename") String filename,
                                 @Param("fileSize") Long fileSize,
                                 @Param("checksum") String checksum,
                                 @Param("excludeCustomer") Customer excludeCustomer);

    /**
     * Checksum-аар хайх
     */
    @Query("SELECT d FROM Document d WHERE d.checksum = :checksum AND d.customer != :excludeCustomer")
    List<Document> findByChecksumAndCustomerNot(@Param("checksum") String checksum,
                                               @Param("excludeCustomer") Customer excludeCustomer);

    // Шаардлагатай баримтууд
    /**
     * Дутуу шаардлагатай баримтууд
     */
    @Query("SELECT dt FROM DocumentType dt WHERE dt.isRequired = true AND dt NOT IN " +
           "(SELECT d.documentType FROM Document d WHERE d.loanApplication.id = :loanApplicationId)")
    List<DocumentType> findMissingRequiredDocuments(@Param("loanApplicationId") String loanApplicationId);

    /**
     * Харилцагчийн шаардлагатай баримтуудын статус
     */
    @Query("SELECT d.documentType, d.verificationStatus FROM Document d WHERE d.customer.id = :customerId AND d.documentType IN :requiredTypes")
    List<Object[]> getRequiredDocumentStatus(@Param("customerId") String customerId,
                                           @Param("requiredTypes") List<DocumentType> requiredTypes);

    // Хувилбар удирдлага
    /**
     * Сүүлийн хувилбар
     */
    @Query("SELECT d FROM Document d WHERE d.customer.id = :customerId AND d.documentType = :documentType ORDER BY d.versionNumber DESC")
    Optional<Document> findLatestVersion(@Param("customerId") String customerId,
                                        @Param("documentType") DocumentType documentType);

    /**
     * Бүх хувилбарууд
     */
    @Query("SELECT d FROM Document d WHERE d.customer.id = :customerId AND d.documentType = :documentType ORDER BY d.versionNumber ASC")
    List<Document> findAllVersions(@Param("customerId") String customerId,
                                  @Param("documentType") DocumentType documentType);

    // Дэвшилтэт хайлт
    /**
     * Дэвшилтэт филтертэй хайлт (энгийн хувилбар)
     */
    @Query("SELECT d FROM Document d WHERE " +
           "(:documentType IS NULL OR d.documentType = :documentType) AND " +
           "(:verificationStatus IS NULL OR d.verificationStatus = :verificationStatus) AND " +
           "(:verifiedBy IS NULL OR d.verifiedBy = :verifiedBy) AND " +
           "(:minSize IS NULL OR d.fileSize >= :minSize) AND " +
           "(:maxSize IS NULL OR d.fileSize <= :maxSize) AND " +
           "(:startDate IS NULL OR d.uploadedAt >= :startDate) AND " +
           "(:endDate IS NULL OR d.uploadedAt <= :endDate)")
    Page<Document> findByAdvancedFilters(
            @Param("documentType") String documentType,
            @Param("verificationStatus") String verificationStatus,
            @Param("customerType") String customerType,
            @Param("verifiedBy") String verifiedBy,
            @Param("minSize") Long minSize,
            @Param("maxSize") Long maxSize,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("hasExpiry") Boolean hasExpiry,
            Pageable pageable);

    // Bulk операциуд
    /**
     * Олон баримтын баталгаажуулалтын статус өөрчлөх
     */
    @Modifying
    @Query("UPDATE Document d SET d.verificationStatus = :newStatus, d.verifiedBy = :verifierName, " +
           "d.verifiedAt = :verifiedAt, d.verificationNotes = :notes WHERE d.id IN :documentIds")
    int updateVerificationStatus(@Param("documentIds") List<String> documentIds,
                               @Param("newStatus") Document.VerificationStatus newStatus,
                               @Param("verifierName") String verifierName,
                               @Param("verifiedAt") LocalDateTime verifiedAt,
                               @Param("notes") String notes);

    /**
     * Хугацаа дууссан баримтуудыг тэмдэглэх
     */
    @Modifying
    @Query("UPDATE Document d SET d.verificationStatus = 'EXPIRED' WHERE d.expiryDate < CURRENT_DATE")
    int markExpiredDocuments();

    // Цэвэрлэлт
    /**
     * Ашиглагдаагүй баримтууд
     */
    @Query("SELECT d FROM Document d WHERE d.uploadedAt < :cutoffDate AND d.customer IS NULL AND d.loanApplication IS NULL")
    List<Document> findUnusedDocuments(@Param("cutoffDate") LocalDateTime cutoffDate);

    // Статистик
    /**
     * Баталгаажуулалтын статусаар тоолох
     */
    @Query("SELECT COUNT(d) FROM Document d WHERE d.verificationStatus = :status")
    long countByVerificationStatus(@Param("status") Document.VerificationStatus status);

    /**
     * Баримтын төрлөөр тоолох
     */
    @Query("SELECT COUNT(d) FROM Document d WHERE d.documentType = :documentType")
    long countByDocumentType(@Param("documentType") DocumentType documentType);

    /**
     * Статусаар тоолох (статистикийн хувьд)
     */
    @Query("SELECT d.verificationStatus, COUNT(d) FROM Document d GROUP BY d.verificationStatus")
    List<Object[]> countByVerificationStatus();

    /**
     * Төрлөөр тоолох (статистикийн хувьд)
     */
    @Query("SELECT d.documentType, COUNT(d) FROM Document d GROUP BY d.documentType")
    List<Object[]> countByDocumentType();

    // Сарын статистик
    /**
     * Сарын баримтын статистик
     */
    @Query("SELECT YEAR(d.uploadedAt), MONTH(d.uploadedAt), COUNT(d), AVG(d.fileSize) FROM Document d " +
           "WHERE d.uploadedAt >= :startDate GROUP BY YEAR(d.uploadedAt), MONTH(d.uploadedAt) " +
           "ORDER BY YEAR(d.uploadedAt), MONTH(d.uploadedAt)")
    List<Object[]> getMonthlyDocumentStats(@Param("startDate") LocalDateTime startDate);

    // Хянаж байгаа хүмүүсийн статистик
    /**
     * Хянагчийн статистик
     */
    @Query("SELECT d.verifiedBy, COUNT(d), AVG(TIMESTAMPDIFF(HOUR, d.uploadedAt, d.verifiedAt)) FROM Document d " +
           "WHERE d.verifiedBy IS NOT NULL AND d.verifiedAt IS NOT NULL " +
           "GROUP BY d.verifiedBy ORDER BY COUNT(d) DESC")
    List<Object[]> getVerifierStats();

    // Өнөөдрийн статистик
    /**
     * Өнөөдрийн баримтын статистик
     */
    @Query("SELECT " +
           "COUNT(CASE WHEN DATE(d.uploadedAt) = CURRENT_DATE THEN 1 END), " +
           "COUNT(CASE WHEN d.verificationStatus = 'PENDING' THEN 1 END), " +
           "COUNT(CASE WHEN DATE(d.verifiedAt) = CURRENT_DATE THEN 1 END), " +
           "COUNT(CASE WHEN d.verificationStatus = 'REJECTED' THEN 1 END) " +
           "FROM Document d")
    Object[] getTodayDocumentStats();

    /**
     * Популяр content type-ууд
     */
    @Query("SELECT d.contentType, COUNT(d) FROM Document d GROUP BY d.contentType ORDER BY COUNT(d) DESC")
    List<Object[]> getTopContentTypes();

    // Performance хяналт
    /**
     * Дундаж баталгаажуулалтын хугацаа
     */
    @Query("SELECT AVG(TIMESTAMPDIFF(HOUR, d.uploadedAt, d.verifiedAt)) FROM Document d WHERE d.verifiedAt IS NOT NULL")
    Double getAverageVerificationTimeHours();

    /**
     * Хамгийн удаан баталгаажуулсан баримтууд
     */
    @Query("SELECT d FROM Document d WHERE d.verifiedAt IS NOT NULL " +
           "ORDER BY TIMESTAMPDIFF(HOUR, d.uploadedAt, d.verifiedAt) DESC")
    Page<Document> findSlowestVerified(Pageable pageable);

    // Хайлт
    /**
     * Файлын нэрээр хайх
     */
    @Query("SELECT d FROM Document d WHERE LOWER(d.originalFilename) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Document> findByFileNameContaining(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Тайлбараар хайх
     */
    @Query("SELECT d FROM Document d WHERE LOWER(COALESCE(d.description, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Document> findByDescriptionContaining(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Существование проверок
    /**
     * Хүсэлтийн дугаараар байгаа эсэхийг шалгах
     */
    boolean existsByApplicationNumber(String applicationNumber);

    /**
     * Файлын нэрээр байгаа эсэхийг шалгах
     */
    boolean existsByOriginalFilename(String originalFilename);

    // Хэрэглэгчийн хандалт
    /**
     * Хэрэглэгчийн хандах эрхтэй баримтууд
     */
    @Query("SELECT d FROM Document d WHERE d.customer.id = :customerId OR " +
           "d.loanApplication.id IN (SELECT la.id FROM LoanApplication la WHERE la.customer.id = :customerId)")
    List<Document> findAccessibleDocuments(@Param("customerId") String customerId);

    // Валидация
    /**
     * Харилцагчийн тодорхой төрлийн баримт байгаа эсэхийг шалгах
     */
    @Query("SELECT COUNT(d) > 0 FROM Document d WHERE d.customer.id = :customerId AND d.documentType = :documentType")
    boolean customerHasDocumentType(@Param("customerId") String customerId, @Param("documentType") DocumentType documentType);

    /**
     * Зээлийн хүсэлтийн тодорхой төрлийн баримт байгаа эсэхийг шалгах
     */
    @Query("SELECT COUNT(d) > 0 FROM Document d WHERE d.loanApplication.id = :loanApplicationId AND d.documentType = :documentType")
    boolean applicationHasDocumentType(@Param("loanApplicationId") String loanApplicationId, @Param("documentType") DocumentType documentType);

    // Additional helper methods
    /**
     * Content type-аар хайх
     */
    Page<Document> findByContentType(String contentType, Pageable pageable);

    /**
     * Файлын хэмжээний хязгаараар хайх
     */
    @Query("SELECT d FROM Document d WHERE d.fileSize BETWEEN :minSize AND :maxSize")
    Page<Document> findByFileSizeRange(@Param("minSize") Long minSize, @Param("maxSize") Long maxSize, Pageable pageable);

    /**
     * Огноогоор хайх
     */
    @Query("SELECT d FROM Document d WHERE d.uploadedAt BETWEEN :startDate AND :endDate")
    Page<Document> findByUploadedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                          @Param("endDate") LocalDateTime endDate, 
                                          Pageable pageable);
}