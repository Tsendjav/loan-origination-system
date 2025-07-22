package com.company.los.repository;

import com.company.los.entity.Customer;
import com.company.los.entity.Document;
import com.company.los.entity.LoanApplication;
import com.company.los.enums.DocumentType;
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
 * Баримт бичгийн Repository
 * Document Repository Interface
 */
@Repository
public interface DocumentRepository extends JpaRepository<Document, UUID> {

    // Суурь хайлтууд
    /**
     * Харилцагчийн баримт бичгүүд
     */
    Page<Document> findByCustomer(Customer customer, Pageable pageable);

    /**
     * Харилцагчийн ID-гаар хайх
     */
    Page<Document> findByCustomerId(UUID customerId, Pageable pageable);

    /**
     * Зээлийн хүсэлтийн баримт бичгүүд
     */
    Page<Document> findByLoanApplication(LoanApplication loanApplication, Pageable pageable);

    /**
     * Зээлийн хүсэлтийн ID-гаар хайх
     */
    Page<Document> findByLoanApplicationId(UUID loanApplicationId, Pageable pageable);

    /**
     * Баримтын төрлөөр хайх
     */
    Page<Document> findByDocumentType(DocumentType documentType, Pageable pageable);

    /**
     * Баталгаажуулалтын статусаар хайх
     */
    Page<Document> findByVerificationStatus(Document.VerificationStatus verificationStatus, Pageable pageable);

    // Харилцагч болон төрлөөр хайх
    /**
     * Харилцагчийн тодорхой төрлийн баримт
     */
    Optional<Document> findByCustomerAndDocumentType(Customer customer, DocumentType documentType);

    /**
     * Харилцагчийн ID болон баримтын төрлөөр хайх
     */
    @Query("SELECT d FROM Document d WHERE d.customer.id = :customerId AND d.documentType = :documentType")
    Optional<Document> findByCustomerIdAndDocumentType(@Param("customerId") UUID customerId, 
                                                       @Param("documentType") DocumentType documentType);

    /**
     * Зээлийн хүсэлтийн тодорхой төрлийн баримт
     */
    @Query("SELECT d FROM Document d WHERE d.loanApplication.id = :loanApplicationId AND d.documentType = :documentType")
    List<Document> findByLoanApplicationIdAndDocumentType(@Param("loanApplicationId") UUID loanApplicationId,
                                                          @Param("documentType") DocumentType documentType);

    /**
     * Баримтын төрлүүдээр хайх
     */
    @Query("SELECT d FROM Document d WHERE d.documentType IN :documentTypes")
    List<Document> findByDocumentTypes(@Param("documentTypes") List<DocumentType> documentTypes);

    // Файлын мэдээллээр хайх
    /**
     * Файлын нэрээр хайх
     */
    List<Document> findByOriginalFilename(String originalFilename);

    /**
     * Хадгалсан файлын нэрээр хайх
     */
    Optional<Document> findByStoredFilename(String storedFilename);

    /**
     * Content type-оор хайх
     */
    Page<Document> findByContentType(String contentType, Pageable pageable);

    /**
     * Файлын хэмжээний хязгаараар хайх
     */
    @Query("SELECT d FROM Document d WHERE d.fileSize BETWEEN :minSize AND :maxSize")
    Page<Document> findByFileSizeBetween(@Param("minSize") Long minSize, 
                                       @Param("maxSize") Long maxSize, 
                                       Pageable pageable);

    // Огноогоор хайх
    /**
     * Тодорхой хугацаанд илгээсэн баримтууд
     */
    @Query("SELECT d FROM Document d WHERE d.uploadedAt BETWEEN :startDate AND :endDate")
    Page<Document> findByUploadedAtBetween(@Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate,
                                         Pageable pageable);

    /**
     * Өнөөдөр илгээсэн баримтууд
     */
    @Query("SELECT d FROM Document d WHERE DATE(d.uploadedAt) = CURRENT_DATE")
    List<Document> findTodayUploaded();

    /**
     * Сүүлийн 7 хоногт илгээсэн баримтууд
     */
    @Query("SELECT d FROM Document d WHERE d.uploadedAt >= :sevenDaysAgo ORDER BY d.uploadedAt DESC")
    List<Document> findRecentUploads(@Param("sevenDaysAgo") LocalDateTime sevenDaysAgo);

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

    // Баталгаажуулалттай холбоотой
    /**
     * Баталгаажуулалт хүлээж байгаа баримтууд
     */
    @Query("SELECT d FROM Document d WHERE d.verificationStatus = 'PENDING' ORDER BY d.uploadedAt ASC")
    Page<Document> findPendingVerification(Pageable pageable);

    /**
     * Хянагдаж байгаа баримтууд
     */
    @Query("SELECT d FROM Document d WHERE d.verificationStatus = 'IN_REVIEW' AND d.verifiedBy = :reviewer")
    List<Document> findInReviewByReviewer(@Param("reviewer") String reviewer);

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
    @Query("SELECT d FROM Document d WHERE d.verificationStatus = 'REJECTED' AND d.verifiedBy = :reviewer")
    Page<Document> findRejectedByReviewer(@Param("reviewer") String reviewer, Pageable pageable);

    /**
     * Дахин илгээх шаардлагатай баримтууд
     */
    @Query("SELECT d FROM Document d JOIN d.customer c WHERE " +
           "d.verificationStatus IN ('RESUBMIT_REQUIRED', 'REJECTED') " +
           "ORDER BY d.verifiedAt DESC")
    Page<Document> findRequiringResubmission(Pageable pageable);

    // Хугацаа дууссан баримтууд
    /**
     * Хугацаа дууссан баримтууд
     */
    @Query("SELECT d FROM Document d WHERE d.expiryDate < CURRENT_DATE AND d.verificationStatus = 'APPROVED'")
    List<Document> findExpiredDocuments();

    /**
     * Удахгүй хугацаа дуусах баримтууд
     */
    @Query("SELECT d FROM Document d WHERE d.expiryDate IS NOT NULL AND " +
           "d.expiryDate BETWEEN CURRENT_DATE AND :futureDate")
    List<Document> findExpiringSoonDocuments(@Param("futureDate") LocalDate futureDate);

    /**
     * Удахгүй хугацаа дуусах баримтууд (30 хоногийн дотор)
     */
    @Query("SELECT d FROM Document d WHERE d.expiryDate IS NOT NULL AND " +
           "d.expiryDate BETWEEN CURRENT_DATE AND :thirtyDaysLater")
    List<Document> findExpiringsoon(@Param("thirtyDaysLater") java.time.LocalDate thirtyDaysLater);

    // OCR болон AI холбоотой
    /**
     * OCR боловсруулалт дууссан баримтууд
     */
    @Query("SELECT d FROM Document d WHERE d.processingStatus = 'COMPLETED' AND d.ocrText IS NOT NULL")
    Page<Document> findOcrProcessed(Pageable pageable);

    /**
     * OCR боловсруулалт амжилтгүй болсон баримтууд
     */
    @Query("SELECT d FROM Document d WHERE d.processingStatus = 'FAILED'")
    List<Document> findOcrFailed();

    /**
     * AI итгэлцлийн оноогоор хайх
     */
    @Query("SELECT d FROM Document d WHERE d.aiConfidenceScore >= :minScore")
    Page<Document> findByHighConfidenceScore(@Param("minScore") java.math.BigDecimal minScore, Pageable pageable);

    /**
     * Бага итгэлцлийн оноотой баримтууд
     */
    @Query("SELECT d FROM Document d WHERE d.aiConfidenceScore < :lowThreshold AND d.aiConfidenceScore IS NOT NULL")
    List<Document> findLowConfidenceDocuments(@Param("lowThreshold") java.math.BigDecimal lowThreshold);

    // Дупликат шалгах
    /**
     * Checksum-аар дупликат хайх
     */
    List<Document> findByChecksumAndCustomerNot(String checksum, Customer customer);

    /**
     * Ижил файл хайх (нэр, хэмжээ, checksum)
     */
    @Query("SELECT d FROM Document d WHERE d.originalFilename = :filename AND " +
           "d.fileSize = :fileSize AND d.checksum = :checksum AND d.customer != :customer")
    List<Document> findDuplicates(@Param("filename") String filename,
                                 @Param("fileSize") Long fileSize,
                                 @Param("checksum") String checksum,
                                 @Param("customer") Customer customer);

    // Шаардлагатай баримтууд
    /**
     * Зээлийн хүсэлтэд дутуу баримтууд
     */
    @Query("SELECT dt FROM DocumentType dt WHERE dt NOT IN " +
           "(SELECT d.documentType FROM Document d WHERE d.loanApplication.id = :loanApplicationId " +
           "AND d.verificationStatus = 'APPROVED')")
    List<DocumentType> findMissingRequiredDocuments(@Param("loanApplicationId") UUID loanApplicationId);

    /**
     * Харилцагчийн шаардлагатай баримтуудын статус
     */
    @Query("SELECT d.documentType, d.verificationStatus, COUNT(d) FROM Document d " +
           "WHERE d.customer.id = :customerId AND d.documentType IN :requiredTypes " +
           "GROUP BY d.documentType, d.verificationStatus")
    List<Object[]> getRequiredDocumentStatus(@Param("customerId") UUID customerId,
                                           @Param("requiredTypes") List<DocumentType> requiredTypes);

    // Статистик
    /**
     * Баримтын төрлөөр тоолох
     */
    @Query("SELECT d.documentType, COUNT(d) FROM Document d GROUP BY d.documentType")
    List<Object[]> countByDocumentType();

    /**
     * Баримтын төрлөөр тоолох (параметр байхгүй)
     */
    long countByDocumentType(DocumentType documentType);

    /**
     * Баталгаажуулалтын статусаар тоолох
     */
    @Query("SELECT d.verificationStatus, COUNT(d) FROM Document d GROUP BY d.verificationStatus")
    List<Object[]> countByVerificationStatus();

    /**
     * Баталгаажуулалтын статусаар тоолох (параметр байхгүй)
     */
    long countByVerificationStatus(Document.VerificationStatus verificationStatus);

    /**
     * Сарын баримтын статистик
     */
    @Query("SELECT DATE_FORMAT(d.uploadedAt, '%Y-%m'), COUNT(d), AVG(d.fileSize) FROM Document d " +
           "WHERE d.uploadedAt >= :startDate " +
           "GROUP BY DATE_FORMAT(d.uploadedAt, '%Y-%m') " +
           "ORDER BY DATE_FORMAT(d.uploadedAt, '%Y-%m')")
    List<Object[]> getMonthlyDocumentStats(@Param("startDate") LocalDateTime startDate);

    /**
     * Баталгаажуулагчаар статистик
     */
    @Query("SELECT d.verifiedBy, COUNT(d), " +
           "AVG(TIMESTAMPDIFF(HOUR, d.uploadedAt, d.verifiedAt)) as avgHours FROM Document d " +
           "WHERE d.verifiedBy IS NOT NULL AND d.verifiedAt IS NOT NULL " +
           "GROUP BY d.verifiedBy")
    List<Object[]> getVerifierStats();

    // Performance хяналт
    /**
     * Дундаж баталгаажуулалтын хугацаа
     */
    @Query("SELECT AVG(TIMESTAMPDIFF(HOUR, d.uploadedAt, d.verifiedAt)) FROM Document d " +
           "WHERE d.verifiedAt IS NOT NULL")
    Double getAverageVerificationTimeHours();

    /**
     * Хамгийн удаан баталгаажуулсан баримтууд
     */
    @Query("SELECT d FROM Document d WHERE d.verifiedAt IS NOT NULL " +
           "ORDER BY TIMESTAMPDIFF(HOUR, d.uploadedAt, d.verifiedAt) DESC")
    Page<Document> findSlowestVerified(Pageable pageable);

    // Дэвшилтэт хайлт
    /**
     * Дэвшилтэт филтертэй хайлт
     */
    @Query("SELECT d FROM Document d JOIN d.customer c WHERE " +
           "(:documentType IS NULL OR d.documentType = :documentType) AND " +
           "(:verificationStatus IS NULL OR d.verificationStatus = :verificationStatus) AND " +
           "(:customerType IS NULL OR c.customerType = :customerType) AND " +
           "(:verifiedBy IS NULL OR d.verifiedBy = :verifiedBy) AND " +
           "(:minSize IS NULL OR d.fileSize >= :minSize) AND " +
           "(:maxSize IS NULL OR d.fileSize <= :maxSize) AND " +
           "(:startDate IS NULL OR d.uploadedAt >= :startDate) AND " +
           "(:endDate IS NULL OR d.uploadedAt <= :endDate) AND " +
           "(:hasExpiry IS NULL OR (d.expiryDate IS NOT NULL) = :hasExpiry)")
    Page<Document> findByAdvancedFilters(
            @Param("documentType") DocumentType documentType,
            @Param("verificationStatus") Document.VerificationStatus verificationStatus,
            @Param("customerType") Customer.CustomerType customerType,
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
    @Query("UPDATE Document d SET d.verificationStatus = :newStatus, " +
           "d.verifiedBy = :verifiedBy, d.verifiedAt = :verifiedAt, d.verificationNotes = :notes " +
           "WHERE d.id IN :documentIds")
    int updateVerificationStatus(@Param("documentIds") List<UUID> documentIds,
                               @Param("newStatus") Document.VerificationStatus newStatus,
                               @Param("verifiedBy") String verifiedBy,
                               @Param("verifiedAt") LocalDateTime verifiedAt,
                               @Param("notes") String notes);

    /**
     * Хугацаа дууссан баримтуудын статус өөрчлөх
     */
    @Modifying
    @Query("UPDATE Document d SET d.verificationStatus = 'EXPIRED' WHERE " +
           "d.expiryDate < CURRENT_DATE AND d.verificationStatus = 'APPROVED'")
    int markExpiredDocuments();

    // File cleanup
    /**
     * Устгагдсан баримтуудын файлын зам
     */
    @Query("SELECT d.filePath FROM Document d WHERE d.isDeleted = true")
    List<String> findDeletedDocumentPaths();

    /**
     * Урт хугацаанд ашиглагдаагүй баримтууд
     */
    @Query("SELECT d FROM Document d WHERE d.uploadedAt < :cutoffDate AND " +
           "d.verificationStatus = 'PENDING' AND d.isActive = true")
    List<Document> findUnusedDocuments(@Param("cutoffDate") LocalDateTime cutoffDate);

    // Dashboard статистик
    /**
     * Өнөөдрийн баримтын статистик
     */
    @Query("SELECT " +
           "COUNT(CASE WHEN DATE(d.uploadedAt) = CURRENT_DATE THEN 1 END) as todayUploaded, " +
           "COUNT(CASE WHEN d.verificationStatus = 'PENDING' THEN 1 END) as pendingVerification, " +
           "COUNT(CASE WHEN DATE(d.verifiedAt) = CURRENT_DATE AND d.verificationStatus = 'APPROVED' THEN 1 END) as todayVerified, " +
           "COUNT(CASE WHEN d.verificationStatus = 'REJECTED' THEN 1 END) as rejected " +
           "FROM Document d")
    Object[] getTodayDocumentStats();

    /**
     * Content type-оор топ файлууд
     */
    @Query("SELECT d.contentType, COUNT(d) FROM Document d " +
           "GROUP BY d.contentType ORDER BY COUNT(d) DESC")
    List<Object[]> getTopContentTypes();

    // Version удирдлага
    /**
     * Баримтын сүүлийн хувилбар
     */
    @Query("SELECT d FROM Document d WHERE d.customer.id = :customerId AND d.documentType = :documentType " +
           "ORDER BY d.versionNumber DESC LIMIT 1")
    Optional<Document> findLatestVersion(@Param("customerId") UUID customerId, 
                                       @Param("documentType") DocumentType documentType);

    /**
     * Баримтын бүх хувилбар
     */
    @Query("SELECT d FROM Document d WHERE d.customer.id = :customerId AND d.documentType = :documentType " +
           "ORDER BY d.versionNumber DESC")
    List<Document> findAllVersions(@Param("customerId") UUID customerId, 
                                 @Param("documentType") DocumentType documentType);

    // Тоолох функцууд
    /**
     * Хугацааны хооронд илгээсэн баримтын тоо
     */
    @Query("SELECT COUNT(d) FROM Document d WHERE d.uploadedAt BETWEEN :startDate AND :endDate")
    long countUploadsBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Хугацааны хооронд баталгаажуулсан баримтын тоо
     */
    @Query("SELECT COUNT(d) FROM Document d WHERE d.verifiedAt BETWEEN :startDate AND :endDate AND d.verificationStatus = 'APPROVED'")
    long countVerificationsBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}