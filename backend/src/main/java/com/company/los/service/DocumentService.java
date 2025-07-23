package com.company.los.service;

import com.company.los.dto.DocumentDto;
import com.company.los.entity.Customer;
import com.company.los.entity.Document;
import com.company.los.entity.DocumentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Баримт бичгийн Service Interface
 * Document Service Interface
 */
public interface DocumentService {

    // =============================================================================
    // CRUD ОПЕРАЦИУД / CRUD OPERATIONS
    // =============================================================================

    /**
     * Баримт байршуулах
     * Upload document
     */
    DocumentDto uploadDocument(UUID customerId, UUID loanApplicationId, DocumentType documentType,
                              MultipartFile file, String description, String tags) throws IOException;

    /**
     * Баримтын мэдээлэл авах
     * Get document information
     */
    DocumentDto getDocumentById(UUID id);

    /**
     * Баримт шинэчлэх
     * Update document
     */
    DocumentDto updateDocument(UUID id, DocumentDto documentDto);

    /**
     * Баримт устгах (soft delete)
     * Delete document (soft delete)
     */
    void deleteDocument(UUID id);

    /**
     * Устгасан баримт сэргээх
     * Restore deleted document
     */
    DocumentDto restoreDocument(UUID id);

    /**
     * Баримт солих
     * Replace document
     */
    DocumentDto replaceDocument(UUID oldDocumentId, MultipartFile newFile, String reason) throws IOException;

    // =============================================================================
    // ХУВИЛБАР УДИРДЛАГА / VERSION MANAGEMENT
    // =============================================================================

    /**
     * Шинэ хувилбар үүсгэх
     * Create new version
     */
    DocumentDto createNewVersion(UUID originalDocumentId, MultipartFile newFile, String changeReason) throws IOException;

    /**
     * Баримтын сүүлийн хувилбар
     * Get latest document version
     */
    DocumentDto getLatestDocumentVersion(UUID customerId, DocumentType documentType);

    /**
     * Баримтын бүх хувилбарууд
     * Get all document versions
     */
    List<DocumentDto> getAllDocumentVersions(UUID customerId, DocumentType documentType);

    // =============================================================================
    // БАТАЛГААЖУУЛАЛТ / VERIFICATION
    // =============================================================================

    /**
     * Баримт баталгаажуулах
     * Verify document
     */
    DocumentDto verifyDocument(UUID id, Document.VerificationStatus status, String verifierName, String notes);

    /**
     * Баримт зөвшөөрөх
     * Approve document
     */
    DocumentDto approveDocument(UUID id, String verifierName, String notes);

    /**
     * Баримт татгалзах
     * Reject document
     */
    DocumentDto rejectDocument(UUID id, String verifierName, String reason);

    /**
     * Дахин илгээх шаардах
     * Request resubmission
     */
    DocumentDto requestResubmission(UUID id, String verifierName, String reason);

    /**
     * Баримт хянаж эхлэх
     * Start document review
     */
    DocumentDto startReview(UUID id, String reviewerName);

    /**
     * Баримт хянах зогсоох
     * Pause document review
     */
    DocumentDto pauseReview(UUID id, String reason);

    /**
     * Баримтын хугацаа сунгах
     * Extend document expiry
     */
    DocumentDto extendDocumentExpiry(UUID id, LocalDate newExpiryDate);

    // =============================================================================
    // ХАЙЛТ БОЛОН ЖАГСААЛТ / SEARCH AND LISTING
    // =============================================================================

    /**
     * Бүх баримт
     * Get all documents
     */
    Page<DocumentDto> getAllDocuments(Pageable pageable);

    /**
     * Харилцагчийн баримтууд
     * Get documents by customer
     */
    Page<DocumentDto> getDocumentsByCustomer(UUID customerId, Pageable pageable);

    /**
     * Зээлийн хүсэлтийн баримтууд
     * Get documents by loan application
     */
    Page<DocumentDto> getDocumentsByLoanApplication(UUID loanApplicationId, Pageable pageable);

    /**
     * Төрлөөр баримт хайх
     * Get documents by type
     */
    Page<DocumentDto> getDocumentsByType(DocumentType documentType, Pageable pageable);

    /**
     * Баталгаажуулалтын статусаар хайх
     * Get documents by verification status
     */
    Page<DocumentDto> getDocumentsByVerificationStatus(Document.VerificationStatus status, Pageable pageable);

    /**
     * Харилцагчийн тодорхой төрлийн баримт
     * Get document by customer and type
     */
    DocumentDto getDocumentByCustomerAndType(UUID customerId, DocumentType documentType);

    /**
     * Филтертэй дэвшилтэт хайлт
     * Advanced search with filters
     */
    Page<DocumentDto> searchDocumentsWithFilters(
            DocumentType documentType,
            Document.VerificationStatus verificationStatus,
            Customer.CustomerType customerType,
            String verifiedBy,
            Long minSize,
            Long maxSize,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Boolean hasExpiry,
            Pageable pageable
    );

    /**
     * Таг-аар хайх
     * Search by tags
     */
    Page<DocumentDto> getDocumentsByTags(String tags, Pageable pageable);

    // =============================================================================
    // СТАТУС БОЛОН ХҮЛЭЭЛТИЙН БАРИМТУУД / STATUS AND PENDING DOCUMENTS
    // =============================================================================

    /**
     * Хүлээгдэж байгаа баримтууд
     * Pending verification documents
     */
    Page<DocumentDto> getPendingVerificationDocuments(Pageable pageable);

    /**
     * Хянагч хянаж байгаа баримтууд
     * Documents in review by reviewer
     */
    List<DocumentDto> getDocumentsInReviewByReviewer(String reviewerName);

    /**
     * Баталгаажуулсан баримтууд
     * Approved documents
     */
    Page<DocumentDto> getApprovedDocuments(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * Татгалзсан баримтууд
     * Rejected documents
     */
    Page<DocumentDto> getRejectedDocuments(String reviewerName, Pageable pageable);

    /**
     * Дахин илгээх шаардлагатай баримтууд
     * Documents requiring resubmission
     */
    Page<DocumentDto> getDocumentsRequiringResubmission(Pageable pageable);

    // =============================================================================
    // ХУГАЦАА / EXPIRY MANAGEMENT
    // =============================================================================

    /**
     * Хугацаа дууссан баримтууд
     * Expired documents
     */
    List<DocumentDto> getExpiredDocuments();

    /**
     * Удахгүй хугацаа дуусах баримтууд
     * Documents expiring soon
     */
    List<DocumentDto> getExpiringSoonDocuments(int days);

    /**
     * Сүүлийн байршуулсан баримтууд
     * Recently uploaded documents
     */
    List<DocumentDto> getRecentDocuments(int limit);

    // =============================================================================
    // OCR БОЛОН AI БОЛОВСРУУЛАЛТ / OCR AND AI PROCESSING
    // =============================================================================

    /**
     * OCR-ээр боловсруулах
     * Process document with OCR
     */
    DocumentDto processDocumentWithOCR(UUID id);

    /**
     * OCR үр дүн хадгалах
     * Save OCR results
     */
    DocumentDto saveOcrResults(UUID id, String ocrText, String extractedData, BigDecimal confidenceScore);

    /**
     * AI-ээр мэдээлэл задлах
     * Extract data with AI
     */
    DocumentDto extractDataWithAI(UUID id);

    /**
     * OCR амжилтгүй болсон баримтууд
     * OCR failed documents
     */
    List<DocumentDto> getOcrFailedDocuments();

    /**
     * Өндөр итгэлцлийн оноотой баримтууд
     * High confidence documents
     */
    Page<DocumentDto> getHighConfidenceDocuments(BigDecimal minScore, Pageable pageable);

    /**
     * Бага итгэлцлийн оноотой баримтууд
     * Low confidence documents
     */
    List<DocumentDto> getLowConfidenceDocuments(BigDecimal threshold);

    // =============================================================================
    // ФАЙЛ ОПЕРАЦИУД / FILE OPERATIONS
    // =============================================================================

    /**
     * Баримт татаж авах
     * Download document
     */
    byte[] downloadDocument(UUID id);

    /**
     * Баримтын урьдчилан харах зураг үүсгэх
     * Generate document preview
     */
    byte[] generateDocumentPreview(UUID id);

    /**
     * Файлын мэдээлэл авах
     * Get file information
     */
    Map<String, Object> getFileInfo(UUID id);

    // =============================================================================
    // ДУПЛИКАТ ШАЛГАЛТ / DUPLICATE DETECTION
    // =============================================================================

    /**
     * Дупликат баримт хайх
     * Find duplicate documents
     */
    List<DocumentDto> findDuplicateDocuments(String filename, Long fileSize, String checksum, UUID excludeCustomerId);

    /**
     * Checksum-аар дупликат шалгах
     * Find documents by checksum
     */
    List<DocumentDto> findDocumentsByChecksum(String checksum, UUID excludeCustomerId);

    // =============================================================================
    // ШААРДЛАГАТАЙ БАРИМТУУД / REQUIRED DOCUMENTS
    // =============================================================================

    /**
     * Зээлийн хүсэлтэд дутуу баримтууд
     * Missing required documents
     */
    List<DocumentType> getMissingRequiredDocuments(UUID loanApplicationId);

    /**
     * Харилцагчийн шаардлагатай баримтуудын статус
     * Required document status
     */
    Map<DocumentType, Document.VerificationStatus> getRequiredDocumentStatus(UUID customerId);

    /**
     * Зээлийн төрлийн шаардлагатай баримтууд
     * Required documents for loan type
     */
    List<DocumentType> getRequiredDocumentsForLoanType(String loanType);

    // =============================================================================
    // BULK ОПЕРАЦИУД / BATCH OPERATIONS
    // =============================================================================

    /**
     * Олон баримт байршуулах
     * Upload multiple documents
     */
    List<DocumentDto> uploadMultipleDocuments(UUID customerId, UUID loanApplicationId,
                                             Map<DocumentType, MultipartFile> files) throws IOException;

    /**
     * Олон баримтын баталгаажуулалтын статус өөрчлөх
     * Update verification status for multiple documents
     */
    int updateVerificationStatusForDocuments(List<UUID> documentIds, Document.VerificationStatus newStatus,
                                            String verifierName, String notes);

    /**
     * Хугацаа дууссан баримтуудын статус өөрчлөх
     * Mark expired documents
     */
    int markExpiredDocuments();

    /**
     * Олон баримт export хийх
     * Export documents to zip
     */
    byte[] exportDocumentsToZip(List<UUID> documentIds);

    // =============================================================================
    // ЦЭВЭРЛЭЛТ / CLEANUP OPERATIONS
    // =============================================================================

    /**
     * Хуучин баримтуудыг архивлах
     * Archive old documents
     */
    void archiveOldDocuments(int daysOld);

    /**
     * Түр файлуудыг цэвэрлэх
     * Cleanup temp files
     */
    void cleanupTempFiles();

    /**
     * Устгагдсан баримтуудыг цэвэрлэх
     * Cleanup deleted documents
     */
    int cleanupDeletedDocuments();

    /**
     * Ашиглагдаагүй баримтууд цэвэрлэх
     * Cleanup unused documents
     */
    int cleanupUnusedDocuments(int unusedDays);

    // =============================================================================
    // ВАЛИДАЦИ / VALIDATION
    // =============================================================================

    /**
     * Файлын төрөл зөвшөөрөгдсөн эсэхийг шалгах
     * Validate file type
     */
    boolean isFileTypeAllowed(DocumentType documentType, String contentType, String filename);

    /**
     * Файлын хэмжээ зөвшөөрөгдсөн эсэхийг шалгах
     * Validate file size
     */
    boolean isFileSizeValid(Long fileSize);

    /**
     * Баримт засах боломжтой эсэхийг шалгах
     * Can edit document
     */
    boolean canEditDocument(UUID id);

    /**
     * Баримт устгах боломжтой эсэхийг шалгах
     * Can delete document
     */
    boolean canDeleteDocument(UUID id);

    // =============================================================================
    // СТАТИСТИК БОЛОН ТАЙЛАН / STATISTICS AND REPORTING
    // =============================================================================

    /**
     * Баримтын статистик
     * Document statistics
     */
    Map<String, Object> getDocumentStatistics();

    /**
     * Баримтын хэмжүүр
     * Document metrics
     */
    Map<String, Object> getDocumentMetrics(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Баримтын төрлөөр статистик
     * Document count by type
     */
    Map<DocumentType, Long> getDocumentCountByType();

    /**
     * Баталгаажуулалтын статусаар статистик
     * Document count by verification status
     */
    Map<Document.VerificationStatus, Long> getDocumentCountByVerificationStatus();

    /**
     * Сарын баримтын статистик
     * Monthly document statistics
     */
    List<Map<String, Object>> getMonthlyDocumentStats(int months);

    /**
     * Баталгаажуулагчаар статистик
     * Verifier statistics
     */
    List<Map<String, Object>> getVerifierStats();

    /**
     * Өнөөдрийн баримтын статистик
     * Today's document statistics
     */
    Map<String, Object> getTodayDocumentStats();

    /**
     * Content type-оор топ файлууд
     * Top content types
     */
    Map<String, Long> getTopContentTypes();

    // =============================================================================
    // PERFORMANCE ХЯНАЛТ / PERFORMANCE MONITORING
    // =============================================================================

    /**
     * Дундаж баталгаажуулалтын хугацаа
     * Average verification time
     */
    Double getAverageVerificationTimeHours();

    /**
     * Хамгийн удаан баталгаажуулсан баримтууд
     * Slowest verified documents
     */
    Page<DocumentDto> getSlowestVerifiedDocuments(Pageable pageable);

    // =============================================================================
    // МЭДЭГДЭЛ / NOTIFICATIONS
    // =============================================================================

    /**
     * Баримт хугацаа дуусах мэдэгдэл
     * Send expiry notification
     */
    boolean sendExpiryNotification(UUID documentId);

    /**
     * Дахин илгээх мэдэгдэл
     * Send resubmission notification
     */
    boolean sendResubmissionNotification(UUID documentId);

    /**
     * Баталгаажуулалтын үр дүнгийн мэдэгдэл
     * Send verification result notification
     */
    boolean sendVerificationResultNotification(UUID documentId);

    // =============================================================================
    // TAGS УДИРДЛАГА / TAGS MANAGEMENT
    // =============================================================================

    /**
     * Баримтад таг нэмэх
     * Add tags to document
     */
    DocumentDto addTagsToDocument(UUID id, String tags);

    /**
     * Баримтаас таг хасах
     * Remove tags from document
     */
    DocumentDto removeTagsFromDocument(UUID id, String tags);

    // =============================================================================
    // АУДИТ БОЛОН ТҮҮХ / AUDIT AND HISTORY
    // =============================================================================

    /**
     * Баримтын өөрчлөлтийн түүх
     * Document audit history
     */
    List<Map<String, Object>> getDocumentAuditHistory(UUID id);

    /**
     * Баримтын үйл ажиллагааны лог
     * Document activity log
     */
    List<Map<String, Object>> getDocumentActivityLog(UUID id);

    // =============================================================================
    // ЧАНАРЫН БАТАЛГАА / QUALITY ASSURANCE
    // =============================================================================

    /**
     * Баримт дахин шалгах
     * Review document quality
     */
    Map<String, Object> reviewDocumentQuality(UUID id);

    /**
     * Өгөгдлийн бүрэн байдлыг шалгах
     * Validate data integrity
     */
    Map<String, Object> validateDataIntegrity();
}