package com.los.service;

import com.los.dto.DocumentDto;
import com.los.entity.Customer;
import com.los.entity.Document;
import com.los.enums.DocumentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Баримт бичгийн Service Interface
 * Document Service Interface
 */
public interface DocumentService {

    // CRUD операциуд
    /**
     * Баримт бичиг upload хийх
     */
    DocumentDto uploadDocument(UUID customerId, UUID loanApplicationId, DocumentType documentType,
                              MultipartFile file, String description, String tags);

    /**
     * Баримт бичгийн мэдээлэл авах
     */
    DocumentDto getDocumentById(UUID id);

    /**
     * Баримт бичиг шинэчлэх
     */
    DocumentDto updateDocument(UUID id, DocumentDto documentDto);

    /**
     * Баримт бичиг устгах (soft delete)
     */
    void deleteDocument(UUID id);

    /**
     * Устгасан баримт сэргээх
     */
    DocumentDto restoreDocument(UUID id);

    /**
     * Баримт бичгийг хувилбараар солих
     */
    DocumentDto replaceDocument(UUID id, MultipartFile newFile, String reason);

    // File operations
    /**
     * Баримт бичиг татаж авах
     */
    byte[] downloadDocument(UUID id);

    /**
     * Баримт бичгийн урьдчилан харах
     */
    byte[] previewDocument(UUID id);

    /**
     * Файлын мэдээлэл авах
     */
    Map<String, Object> getFileInfo(UUID id);

    // Хайлт операциуд
    /**
     * Бүх баримт бичгийн жагсаалт
     */
    Page<DocumentDto> getAllDocuments(Pageable pageable);

    /**
     * Харилцагчийн баримт бичгүүд
     */
    Page<DocumentDto> getDocumentsByCustomer(UUID customerId, Pageable pageable);

    /**
     * Зээлийн хүсэлтийн баримт бичгүүд
     */
    Page<DocumentDto> getDocumentsByLoanApplication(UUID loanApplicationId, Pageable pageable);

    /**
     * Баримтын төрлөөр хайх
     */
    Page<DocumentDto> getDocumentsByType(DocumentType documentType, Pageable pageable);

    /**
     * Баталгаажуулалтын статусаар хайх
     */
    Page<DocumentDto> getDocumentsByVerificationStatus(Document.VerificationStatus status, Pageable pageable);

    /**
     * Харилцагчийн тодорхой төрлийн баримт
     */
    DocumentDto getDocumentByCustomerAndType(UUID customerId, DocumentType documentType);

    // Дэвшилтэт хайлт
    /**
     * Филтертэй дэвшилтэт хайлт
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

    // Баталгаажуулалт
    /**
     * Баримт баталгаажуулах
     */
    DocumentDto approveDocument(UUID id, String verifierName, String notes);

    /**
     * Баримт татгалзах
     */
    DocumentDto rejectDocument(UUID id, String verifierName, String reason);

    /**
     * Баримт хянаж эхлэх
     */
    DocumentDto startReview(UUID id, String reviewerName);

    /**
     * Баримт хянах зогсоох
     */
    DocumentDto pauseReview(UUID id, String reason);

    /**
     * Дахин илгээх шаардах
     */
    DocumentDto requireResubmission(UUID id, String verifierName, String reason);

    // Баталгаажуулалт хүлээж байгаа баримтууд
    /**
     * Баталгаажуулалт хүлээж байгаа баримтууд
     */
    Page<DocumentDto> getPendingVerificationDocuments(Pageable pageable);

    /**
     * Хянагч хянаж байгаа баримтууд
     */
    List<DocumentDto> getDocumentsInReviewByReviewer(String reviewerName);

    /**
     * Баталгаажуулсан баримтууд
     */
    Page<DocumentDto> getApprovedDocuments(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * Татгалзсан баримтууд
     */
    Page<DocumentDto> getRejectedDocuments(String reviewerName, Pageable pageable);

    /**
     * Дахин илгээх шаардлагатай баримтууд
     */
    Page<DocumentDto> getDocumentsRequiringResubmission(Pageable pageable);

    // Хугацаа дууссан баримтууд
    /**
     * Хугацаа дууссан баримтууд
     */
    List<DocumentDto> getExpiredDocuments();

    /**
     * Удахгүй хугацаа дуусах баримтууд
     */
    List<DocumentDto> getDocumentsExpiringSoon();

    /**
     * Баримтын хугацаа сунгах
     */
    DocumentDto extendDocumentExpiry(UUID id, java.time.LocalDate newExpiryDate);

    // OCR болон AI процесс
    /**
     * OCR процесс эхлүүлэх
     */
    DocumentDto startOcrProcessing(UUID id);

    /**
     * OCR үр дүн хадгалах
     */
    DocumentDto saveOcrResults(UUID id, String ocrText, String extractedData, BigDecimal confidenceScore);

    /**
     * OCR амжилтгүй болсон баримтууд
     */
    List<DocumentDto> getOcrFailedDocuments();

    /**
     * Өндөр итгэлцлийн оноотой баримтууд
     */
    Page<DocumentDto> getHighConfidenceDocuments(BigDecimal minScore, Pageable pageable);

    /**
     * Бага итгэлцлийн оноотой баримтууд
     */
    List<DocumentDto> getLowConfidenceDocuments(BigDecimal threshold);

    // Дупликат шалгалт
    /**
     * Дупликат баримт хайх
     */
    List<DocumentDto> findDuplicateDocuments(String filename, Long fileSize, String checksum, UUID excludeCustomerId);

    /**
     * Checksum-аар дупликат шалгах
     */
    List<DocumentDto> findDocumentsByChecksum(String checksum, UUID excludeCustomerId);

    // Шаардлагатай баримтууд
    /**
     * Зээлийн хүсэлтэд дутуу баримтууд
     */
    List<DocumentType> getMissingRequiredDocuments(UUID loanApplicationId);

    /**
     * Харилцагчийн шаардлагатай баримтуудын статус
     */
    Map<DocumentType, Document.VerificationStatus> getRequiredDocumentStatus(UUID customerId);

    /**
     * Зээлийн төрлийн шаардлагатай баримтууд
     */
    List<DocumentType> getRequiredDocumentsForLoanType(String loanType);

    // Validation
    /**
     * Файлын төрөл зөвшөөрөгдсөн эсэхийг шалгах
     */
    boolean isFileTypeAllowed(DocumentType documentType, String contentType, String filename);

    /**
     * Файлын хэмжээ зөвшөөрөгдсөн эсэхийг шалгах
     */
    boolean isFileSizeValid(Long fileSize);

    /**
     * Баримт засах боломжтой эсэхийг шалгах
     */
    boolean canEditDocument(UUID id);

    /**
     * Баримт устгах боломжтой эсэхийг шалгах
     */
    boolean canDeleteDocument(UUID id);

    // Статистик
    /**
     * Баримт бичгийн статистик
     */
    Map<String, Object> getDocumentStatistics();

    /**
     * Баримтын төрлөөр статистик
     */
    Map<DocumentType, Long> getDocumentCountByType();

    /**
     * Баталгаажуулалтын статусаар статистик
     */
    Map<Document.VerificationStatus, Long> getDocumentCountByVerificationStatus();

    /**
     * Сарын баримтын статистик
     */
    List<Map<String, Object>> getMonthlyDocumentStats(int months);

    /**
     * Баталгаажуулагчаар статистик
     */
    List<Map<String, Object>> getVerifierStats();

    // Performance хяналт
    /**
     * Дундаж баталгаажуулалтын хугацаа
     */
    Double getAverageVerificationTimeHours();

    /**
     * Хамгийн удаан баталгаажуулсан баримтууд
     */
    Page<DocumentDto> getSlowestVerifiedDocuments(Pageable pageable);

    // Dashboard
    /**
     * Өнөөдрийн баримтын статистик
     */
    Map<String, Object> getTodayDocumentStats();

    /**
     * Content type-оор топ файлууд
     */
    Map<String, Long> getTopContentTypes();

    // Version удирдлага
    /**
     * Баримтын сүүлийн хувилбар
     */
    DocumentDto getLatestDocumentVersion(UUID customerId, DocumentType documentType);

    /**
     * Баримтын бүх хувилбар
     */
    List<DocumentDto> getAllDocumentVersions(UUID customerId, DocumentType documentType);

    /**
     * Шинэ хувилбар үүсгэх
     */
    DocumentDto createNewVersion(UUID originalDocumentId, MultipartFile newFile, String changeReason);

    // Bulk операциуд
    /**
     * Олон баримтын баталгаажуулалтын статус өөрчлөх
     */
    int updateVerificationStatusForDocuments(List<UUID> documentIds, Document.VerificationStatus newStatus,
                                           String verifierName, String notes);

    /**
     * Хугацаа дууссан баримтуудын статус өөрчлөх
     */
    int markExpiredDocuments();

    /**
     * Олон баримт export хийх
     */
    byte[] exportDocumentsToZip(List<UUID> documentIds);

    // File cleanup
    /**
     * Устгагдсан баримтуудын файл цэвэрлэх
     */
    int cleanupDeletedDocuments();

    /**
     * Ашиглагдаагүй баримтууд цэвэрлэх
     */
    int cleanupUnusedDocuments(int unusedDays);

    /**
     * Устгагдсан файлуудын зам авах
     */
    List<String> getDeletedDocumentPaths();

    // Notification
    /**
     * Баримт хугацаа дуусах мэдэгдэл
     */
    boolean sendExpiryNotification(UUID documentId);

    /**
     * Дахин илгээх мэдэгдэл
     */
    boolean sendResubmissionNotification(UUID documentId);

    /**
     * Баталгаажуулалтын үр дүнгийн мэдэгдэл
     */
    boolean sendVerificationResultNotification(UUID documentId);

    // Tags удирдлага
    /**
     * Баримтад таг нэмэх
     */
    DocumentDto addTagsToDocument(UUID id, String tags);

    /**
     * Баримтаас таг хасах
     */
    DocumentDto removeTagsFromDocument(UUID id, String tags);

    /**
     * Таг-аар хайх
     */
    Page<DocumentDto> getDocumentsByTags(String tags, Pageable pageable);

    // Audit & History
    /**
     * Баримтын өөрчлөлтийн түүх
     */
    List<Map<String, Object>> getDocumentAuditHistory(UUID id);

    /**
     * Баримтын үйл ажиллагааны лого
     */
    List<Map<String, Object>> getDocumentActivityLog(UUID id);

    // Quality assurance
    /**
     * Баримт дахин шалгах
     */
    Map<String, Object> reviewDocumentQuality(UUID id);

    /**
     * Өгөгдлийн бүрэн бус байдлыг шалгах
     */
    Map<String, Object> validateDataIntegrity();
}