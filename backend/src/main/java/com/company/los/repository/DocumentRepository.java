package com.company.los.repository;

import com.company.los.entity.Document;
import com.company.los.entity.DocumentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Баримт бичгийн Repository
 * Document Repository Interface
 */
@Repository
public interface DocumentRepository extends JpaRepository<Document, UUID> {

    // Суурь хайлтууд
    /**
     * Харилцагчийн баримтууд
     */
    Page<Document> findByCustomerId(UUID customerId, Pageable pageable);

    /**
     * Зээлийн хүсэлтийн баримтууд
     */
    Page<Document> findByLoanApplicationId(UUID loanApplicationId, Pageable pageable);

    /**
     * Баримтын төрлөөр хайх
     */
    Page<Document> findByDocumentType(DocumentType documentType, Pageable pageable);

    /**
     * Харилцагч болон баримтын төрлөөр хайх
     */
    Optional<Document> findByCustomerIdAndDocumentType(UUID customerId, DocumentType documentType);

    /**
     * Баталгаажуулалтын статусаар хайх
     */
    Page<Document> findByVerificationStatus(Document.VerificationStatus status, Pageable pageable);

    /**
     * Баталгаажуулсан хүнээр хайх
     */
    List<Document> findByVerifiedBy(String verifiedBy);

    /**
     * Орлогын дугаараар хайх
     */
    List<Document> findByOriginalFilename(String originalFilename);

    /**
     * Checksum-аар хайх
     */
    List<Document> findByChecksum(String checksum);

    /**
     * Файлын хэмжээгээр хайх
     */
    List<Document> findByFileSize(Long fileSize);

    /**
     * Content type-аар хайх
     */
    Page<Document> findByContentType(String contentType, Pageable pageable);

    // Хугацаатай холбоотой
    /**
     * Хугацаа дууссан баримтууд
     */
    @Query("SELECT d FROM Document d WHERE d.expiryDate < CURRENT_DATE")
    List<Document> findExpiredDocuments();

    /**
     * Удахгүй хугацаа дуусах баримтууд
     */
    @Query("SELECT d FROM Document d WHERE d.expiryDate BETWEEN CURRENT_DATE AND :futureDate")
    List<Document> findDocumentsExpiringSoon(@Param("futureDate") LocalDate futureDate);

    /**
     * Хугацаатай баримтууд
     */
    @Query("SELECT d FROM Document d WHERE d.expiryDate IS NOT NULL")
    List<Document> findDocumentsWithExpiry();

    // Огноогоор хайх
    /**
     * Огноогоор илгээсэн баримтууд
     */
    @Query("SELECT d FROM Document d WHERE FUNCTION('DATE', d.uploadedAt) = :date")
    List<Document> findByUploadDate(@Param("date") LocalDate date);

    /**
     * Хугацааны завсраар илгээсэн баримтууд
     */
    @Query("SELECT d FROM Document d WHERE d.uploadedAt BETWEEN :startDate AND :endDate")
    Page<Document> findByUploadDateRange(@Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate,
                                        Pageable pageable);

    /**
     * Баталгаажуулсан огноогоор хайх
     */
    @Query("SELECT d FROM Document d WHERE FUNCTION('DATE', d.verifiedAt) = :date")
    List<Document> findByVerificationDate(@Param("date") LocalDate date);

    /**
     * Хугацааны завсраар баталгаажуулсан баримтууд
     */
    @Query("SELECT d FROM Document d WHERE d.verifiedAt BETWEEN :startDate AND :endDate")
    Page<Document> findByVerificationDateRange(@Param("startDate") LocalDateTime startDate,
                                              @Param("endDate") LocalDateTime endDate,
                                              Pageable pageable);

    // Сүүлийн үйл ажиллагаа
    /**
     * Сүүлийн илгээсэн баримтууд
     */
    @Query("SELECT d FROM Document d ORDER BY d.uploadedAt DESC")
    Page<Document> findRecentlyUploaded(Pageable pageable);

    /**
     * Сүүлийн баталгаажуулсан баримтууд
     */
    @Query("SELECT d FROM Document d WHERE d.verifiedAt IS NOT NULL ORDER BY d.verifiedAt DESC")
    Page<Document> findRecentlyVerified(Pageable pageable);

    /**
     * Сүүлийн өөрчлөлт хийсэн баримтууд
     */
    @Query("SELECT d FROM Document d ORDER BY d.updatedAt DESC")
    Page<Document> findRecentlyModified(Pageable pageable);

    // Хувилбартай холбоотой
    /**
     * Баримтын бүх хувилбарууд
     */
    @Query("SELECT d FROM Document d WHERE d.customer.id = :customerId AND d.documentType = :documentType " +
           "ORDER BY d.versionNumber DESC")
    List<Document> findAllVersions(@Param("customerId") UUID customerId,
                                  @Param("documentType") DocumentType documentType);

    /**
     * Сүүлийн хувилбар
     */
    @Query("SELECT d FROM Document d WHERE d.customer.id = :customerId AND d.documentType = :documentType " +
           "ORDER BY d.versionNumber DESC")
    Page<Document> findLatestVersion(@Param("customerId") UUID customerId,
                                    @Param("documentType") DocumentType documentType,
                                    Pageable pageable);

    /**
     * Өмнөх баримттай холбоотой
     */
    @Query("SELECT d FROM Document d WHERE d.previousDocumentId = :documentId")
    List<Document> findByPreviousDocumentId(@Param("documentId") UUID documentId);

    // OCR болон AI
    /**
     * OCR текст бүхий баримтууд
     */
    @Query("SELECT d FROM Document d WHERE d.ocrText IS NOT NULL AND d.ocrText != ''")
    List<Document> findDocumentsWithOcrText();

    /**
     * OCR амжилтгүй болсон баримтууд
     */
    @Query("SELECT d FROM Document d WHERE d.processingStatus = 'FAILED' AND d.processingError IS NOT NULL")
    List<Document> findOcrFailedDocuments();

    /**
     * AI боловсруулсан баримтууд
     */
    @Query("SELECT d FROM Document d WHERE d.extractedData IS NOT NULL AND d.extractedData != ''")
    List<Document> findDocumentsWithExtractedData();

    /**
     * Итгэлцлийн оноо бүхий баримтууд
     */
    @Query("SELECT d FROM Document d WHERE d.aiConfidenceScore IS NOT NULL AND d.aiConfidenceScore >= :minScore")
    Page<Document> findDocumentsWithMinConfidence(@Param("minScore") BigDecimal minScore,
                                                 Pageable pageable);

    /**
     * Бага итгэлцлийн баримтууд
     */
    @Query("SELECT d FROM Document d WHERE d.aiConfidenceScore IS NOT NULL AND d.aiConfidenceScore < :threshold")
    List<Document> findLowConfidenceDocuments(@Param("threshold") BigDecimal threshold);

    // Дэвшилтэт хайлт
    /**
     * Тагаар хайх
     */
    @Query("SELECT d FROM Document d WHERE d.tags LIKE %:tag%")
    Page<Document> findByTagsContaining(@Param("tag") String tag, Pageable pageable);

    /**
     * Тайлбараар хайх
     */
    @Query("SELECT d FROM Document d WHERE LOWER(d.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Document> findByDescriptionContaining(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Файлын нэрээр хайх
     */
    @Query("SELECT d FROM Document d WHERE LOWER(d.originalFilename) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Document> findByFilenameContaining(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Ерөнхий хайлт
     */
    @Query("SELECT d FROM Document d WHERE " +
           "LOWER(d.originalFilename) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(COALESCE(d.description, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(COALESCE(d.tags, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Document> findBySearchTerm(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Файлын хэмжээгээр хайх
    /**
     * Файлын хэмжээний хязгаараар хайх
     */
    @Query("SELECT d FROM Document d WHERE d.fileSize >= :minSize AND d.fileSize <= :maxSize")
    Page<Document> findByFileSizeRange(@Param("minSize") Long minSize,
                                      @Param("maxSize") Long maxSize,
                                      Pageable pageable);

    /**
     * Том файлууд
     */
    @Query("SELECT d FROM Document d WHERE d.fileSize > :threshold ORDER BY d.fileSize DESC")
    List<Document> findLargeFiles(@Param("threshold") Long threshold);

    // Шаардлагатай баримтууд
    /**
     * Шаардлагатай баримтууд
     */
    @Query("SELECT d FROM Document d WHERE d.isRequired = true")
    List<Document> findRequiredDocuments();

    /**
     * Харилцагчийн шаардлагатай баримтууд
     */
    @Query("SELECT d FROM Document d WHERE d.customer.id = :customerId AND d.isRequired = true")
    List<Document> findRequiredDocumentsByCustomer(@Param("customerId") UUID customerId);

    /**
     * Зээлийн хүсэлтийн шаардлагатай баримтууд
     */
    @Query("SELECT d FROM Document d WHERE d.loanApplication.id = :loanApplicationId AND d.isRequired = true")
    List<Document> findRequiredDocumentsByLoanApplication(@Param("loanApplicationId") UUID loanApplicationId);

    // Дуплик шалгах
    /**
     * Потенциаль дуплик баримтууд
     */
    @Query("SELECT d FROM Document d WHERE " +
           "(d.originalFilename = :filename OR d.checksum = :checksum OR d.fileSize = :fileSize) " +
           "AND (:excludeCustomerId IS NULL OR d.customer.id != :excludeCustomerId)")
    List<Document> findPotentialDuplicates(@Param("filename") String filename,
                                          @Param("checksum") String checksum,
                                          @Param("fileSize") Long fileSize,
                                          @Param("excludeCustomerId") UUID excludeCustomerId);

    // Статистик
    /**
     * Баримтын тоог тооцох
     */
    @Query("SELECT COUNT(d) FROM Document d")
    long countAllDocuments();

    /**
     * Статусаар тооцох
     */
    @Query("SELECT COUNT(d) FROM Document d WHERE d.verificationStatus = :status")
    long countByVerificationStatus(@Param("status") Document.VerificationStatus status);

    /**
     * Харилцагчийн баримтын тоо
     */
    @Query("SELECT COUNT(d) FROM Document d WHERE d.customer.id = :customerId")
    long countByCustomerId(@Param("customerId") UUID customerId);

    /**
     * Зээлийн хүсэлтийн баримтын тоо
     */
    @Query("SELECT COUNT(d) FROM Document d WHERE d.loanApplication.id = :loanApplicationId")
    long countByLoanApplicationId(@Param("loanApplicationId") UUID loanApplicationId);

    /**
     * Зээлийн хүсэлтийн файлын хэмжээний нийлбэр
     */
    @Query("SELECT COALESCE(SUM(d.fileSize), 0) FROM Document d WHERE d.loanApplication.id = :loanApplicationId")
    Long sumFileSizeByLoanApplication(@Param("loanApplicationId") UUID loanApplicationId);

    /**
     * Баримтын төрлөөр тооцох
     */
    @Query("SELECT COUNT(d) FROM Document d WHERE d.documentType = :documentType")
    long countByDocumentType(@Param("documentType") DocumentType documentType);

    /**
     * Өнөөдрийн статистик
     */
    @Query("SELECT new map(" +
           "COUNT(CASE WHEN FUNCTION('DATE', d.uploadedAt) = CURRENT_DATE THEN 1 END) as uploadedToday, " +
           "COUNT(CASE WHEN FUNCTION('DATE', d.verifiedAt) = CURRENT_DATE THEN 1 END) as verifiedToday) " +
           "FROM Document d")
    Map<String, Long> getTodayStats();

    /**
     * Баримтын төрлөөр статистик
     */
    @Query("SELECT d.documentType.name, COUNT(d) FROM Document d " +
           "GROUP BY d.documentType.name ORDER BY COUNT(d) DESC")
    List<Object[]> countByDocumentType();

    /**
     * Статусаар статистик
     */
    @Query("SELECT d.verificationStatus, COUNT(d) FROM Document d " +
           "GROUP BY d.verificationStatus ORDER BY COUNT(d) DESC")
    List<Object[]> countByStatus();

    /**
     * Сарын статистик
     */
    @Query("SELECT FUNCTION('DATE_FORMAT', d.uploadedAt, '%Y-%m'), COUNT(d) FROM Document d " +
           "WHERE d.uploadedAt >= :startDate " +
           "GROUP BY FUNCTION('DATE_FORMAT', d.uploadedAt, '%Y-%m') " +
           "ORDER BY FUNCTION('DATE_FORMAT', d.uploadedAt, '%Y-%m')")
    List<Object[]> getMonthlyUploadStats(@Param("startDate") LocalDateTime startDate);

    // Data quality
    /**
     * Дутуу мэдээлэлтэй баримтууд
     */
    @Query("SELECT d FROM Document d WHERE " +
           "d.originalFilename IS NULL OR d.originalFilename = '' OR " +
           "d.contentType IS NULL OR d.contentType = '' OR " +
           "d.fileSize IS NULL OR d.fileSize <= 0")
    List<Document> findDocumentsWithIncompleteData();

    /**
     * Checksum-гүй баримтууд
     */
    @Query("SELECT d FROM Document d WHERE d.checksum IS NULL OR d.checksum = ''")
    List<Document> findDocumentsWithoutChecksum();

    /**
     * Хугацаагүй шаардлагатай баримтууд
     */
    @Query("SELECT d FROM Document d WHERE d.isRequired = true AND d.expiryDate IS NULL")
    List<Document> findRequiredDocumentsWithoutExpiry();

    // Cleanup
    /**
     * Хуучин баримтууд
     */
    @Query("SELECT d FROM Document d WHERE d.uploadedAt < :cutoffDate")
    List<Document> findOldDocuments(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Устгагдсан баримтууд
     */
    @Query("SELECT d FROM Document d WHERE d.isDeleted = true")
    List<Document> findDeletedDocuments();

    /**
     * Идэвхгүй харилцагчийн баримтууд
     */
    @Query("SELECT d FROM Document d WHERE d.customer.isActive = false")
    List<Document> findDocumentsByInactiveCustomers();

    // Performance monitoring
    /**
     * Баталгаажуулалтын дундаж хугацаа
     */
    @Query("SELECT AVG(FUNCTION('TIMESTAMPDIFF', HOUR, d.uploadedAt, d.verifiedAt)) FROM Document d " +
           "WHERE d.verifiedAt IS NOT NULL")
    Double getAverageVerificationTimeHours();

    /**
     * Хамгийн удаан баталгаажуулсан баримтууд
     */
    @Query("SELECT d FROM Document d WHERE d.verifiedAt IS NOT NULL " +
           "ORDER BY FUNCTION('TIMESTAMPDIFF', HOUR, d.uploadedAt, d.verifiedAt) DESC")
    Page<Document> findSlowestVerifiedDocuments(Pageable pageable);

    // Advanced filters
    /**
     * Дэвшилтэт филтертэй хайлт
     */
    @Query("SELECT d FROM Document d WHERE " +
           "(:documentType IS NULL OR d.documentType = :documentType) AND " +
           "(:verificationStatus IS NULL OR d.verificationStatus = :verificationStatus) AND " +
           "(:verifiedBy IS NULL OR d.verifiedBy = :verifiedBy) AND " +
           "(:minSize IS NULL OR d.fileSize >= :minSize) AND " +
           "(:maxSize IS NULL OR d.fileSize <= :maxSize) AND " +
           "(:startDate IS NULL OR d.uploadedAt >= :startDate) AND " +
           "(:endDate IS NULL OR d.uploadedAt <= :endDate) AND " +
           "(:hasExpiry IS NULL OR (:hasExpiry = TRUE AND d.expiryDate IS NOT NULL) OR (:hasExpiry = FALSE AND d.expiryDate IS NULL))")
    Page<Document> findByAdvancedFilters(
            @Param("documentType") DocumentType documentType,
            @Param("verificationStatus") Document.VerificationStatus verificationStatus,
            @Param("verifiedBy") String verifiedBy,
            @Param("minSize") Long minSize,
            @Param("maxSize") Long maxSize,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("hasExpiry") Boolean hasExpiry,
            Pageable pageable);

    // Business queries
    /**
     * Хүлээгдэж байгаа баталгаажуулалт
     */
    @Query("SELECT d FROM Document d WHERE d.verificationStatus = 'PENDING' ORDER BY d.uploadedAt ASC")
    Page<Document> findPendingVerification(Pageable pageable);

    /**
     * Баталгаажуулагчийн ажил
     */
    @Query("SELECT d FROM Document d WHERE d.verificationStatus = 'IN_REVIEW' AND d.verifiedBy = :verifier")
    List<Document> findInReviewByVerifier(@Param("verifier") String verifier);

    /**
     * Дахин илгээх шаардлагатай
     */
    @Query("SELECT d FROM Document d WHERE d.verificationStatus = 'RESUBMIT_REQUIRED'")
    List<Document> findRequiringResubmission();
}