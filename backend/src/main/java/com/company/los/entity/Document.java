package com.company.los.entity;

import com.company.los.entity.BaseEntity;
import com.company.los.enums.DocumentType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;

/**
 * Баримт бичгийн Entity
 * Document Entity
 */
@Entity
@Table(name = "documents", indexes = {
        @Index(name = "idx_document_customer", columnList = "customer_id"),
        @Index(name = "idx_document_loan_application", columnList = "loan_application_id"),
        @Index(name = "idx_document_type", columnList = "document_type"),
        @Index(name = "idx_document_status", columnList = "verification_status"),
        @Index(name = "idx_document_upload_date", columnList = "uploaded_at")
})
@SQLDelete(sql = "UPDATE documents SET is_deleted = true WHERE id = ?")
@Where(clause = "is_deleted = false")
public class Document extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false, foreignKey = @ForeignKey(name = "fk_document_customer"))
    @NotNull(message = "Харилцагч заавал байх ёстой")
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_application_id", foreignKey = @ForeignKey(name = "fk_document_loan_application"))
    private LoanApplication loanApplication;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 50)
    @NotNull(message = "Баримтын төрөл заавал сонгох ёстой")
    private DocumentType documentType;

    @Column(name = "original_filename", nullable = false, length = 255)
    @NotBlank(message = "Файлын нэр заавал байх ёстой")
    @Size(max = 255, message = "Файлын нэр 255 тэмдэгтээс ихгүй байх ёстой")
    private String originalFilename;

    @Column(name = "stored_filename", nullable = false, length = 500)
    @NotBlank(message = "Хадгалсан файлын нэр заавал байх ёстой")
    private String storedFilename;

    @Column(name = "file_path", nullable = false, length = 1000)
    @NotBlank(message = "Файлын зам заавал байх ёстой")
    private String filePath;

    @Column(name = "content_type", nullable = false, length = 100)
    @NotBlank(message = "Файлын төрөл заавал байх ёстой")
    private String contentType;

    @Column(name = "file_size", nullable = false)
    @NotNull(message = "Файлын хэмжээ заавал байх ёстой")
    @Min(value = 1, message = "Файлын хэмжээ 0-ээс их байх ёстой")
    @Max(value = 52428800, message = "Файлын хэмжээ 50MB-аас бага байх ёстой")
    private Long fileSize;

    @Column(name = "checksum", length = 64)
    @Size(max = 64, message = "Checksum 64 тэмдэгтээс ихгүй байх ёстой")
    private String checksum;

    @Column(name = "uploaded_at", nullable = false)
    @NotNull(message = "Илгээсэн огноо заавал байх ёстой")
    private LocalDateTime uploadedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false, length = 30)
    @NotNull(message = "Баталгаажуулалтын статус заавал байх ёстой")
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "verified_by", length = 100)
    @Size(max = 100, message = "Баталгаажуулсан хүний нэр 100 тэмдэгтээс ихгүй байх ёстой")
    private String verifiedBy;

    @Column(name = "verification_notes", length = 1000)
    @Size(max = 1000, message = "Баталгаажуулалтын тэмдэглэл 1000 тэмдэгтээс ихгүй байх ёстой")
    private String verificationNotes;

    @Column(name = "ocr_text", columnDefinition = "TEXT")
    private String ocrText;

    @Column(name = "extracted_data", columnDefinition = "TEXT")
    private String extractedData;

    @Column(name = "ai_confidence_score", precision = 5, scale = 2)
    @DecimalMin(value = "0.0", message = "AI итгэлцлийн оноо сөрөг байж болохгүй")
    @DecimalMax(value = "100.0", message = "AI итгэлцлийн оноо 100-аас их байж болохгүй")
    private BigDecimal aiConfidenceScore;

    @Column(name = "processing_status", length = 30)
    private String processingStatus;

    @Column(name = "processing_error", length = 500)
    @Size(max = 500, message = "Процессын алдаа 500 тэмдэгтээс ихгүй байх ёстой")
    private String processingError;

    @Column(name = "description", length = 500)
    @Size(max = 500, message = "Тайлбар 500 тэмдэгтээс ихгүй байх ёстой")
    private String description;

    @Column(name = "tags", length = 255)
    @Size(max = 255, message = "Таг 255 тэмдэгтээс ихгүй байх ёстой")
    private String tags;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "is_required", nullable = false)
    private Boolean isRequired = false;

    @Column(name = "version_number")
    @Min(value = 1, message = "Хувилбарын дугаар 1-ээс бага байж болохгүй")
    private Integer versionNumber = 1;

    @Column(name = "previous_document_id", columnDefinition = "uuid")
    private java.util.UUID previousDocumentId;

    // Баталгаажуулалтын статус
    public enum VerificationStatus {
        PENDING("Хүлээгдэж байгаа", "Waiting for verification"),
        IN_REVIEW("Шалгаж байгаа", "Under review"),
        APPROVED("Баталгаажуулсан", "Verified and approved"),
        REJECTED("Татгалзсан", "Verification failed"),
        EXPIRED("Хугацаа дууссан", "Document expired"),
        RESUBMIT_REQUIRED("Дахин илгээх", "Resubmission required"),
        ON_HOLD("Түр зогсоосон", "On hold for additional review");

        private final String mongolianName;
        private final String englishDescription;

        VerificationStatus(String mongolianName, String englishDescription) {
            this.mongolianName = mongolianName;
            this.englishDescription = englishDescription;
        }

        public String getMongolianName() { return mongolianName; }
        public String getEnglishDescription() { return englishDescription; }

        public boolean isFinalStatus() {
            return this == APPROVED || this == REJECTED || this == EXPIRED;
        }

        public boolean isActionRequired() {
            return this == RESUBMIT_REQUIRED || this == REJECTED;
        }
    }

    // Constructors
    public Document() {
        super();
        this.uploadedAt = LocalDateTime.now();
    }

    public Document(Customer customer, DocumentType documentType, String originalFilename, String storedFilename, String filePath, String contentType, Long fileSize) {
        this();
        this.customer = customer;
        this.documentType = documentType;
        this.originalFilename = originalFilename;
        this.storedFilename = storedFilename;
        this.filePath = filePath;
        this.contentType = contentType;
        this.fileSize = fileSize;
    }

    // Business methods
    public void approve(String verifierName, String notes) {
        this.verificationStatus = VerificationStatus.APPROVED;
        this.verifiedAt = LocalDateTime.now();
        this.verifiedBy = verifierName;
        this.verificationNotes = notes;
    }

    public void reject(String verifierName, String reason) {
        this.verificationStatus = VerificationStatus.REJECTED;
        this.verifiedAt = LocalDateTime.now();
        this.verifiedBy = verifierName;
        this.verificationNotes = reason;
    }

    public void startReview(String reviewerName) {
        this.verificationStatus = VerificationStatus.IN_REVIEW;
        this.verifiedBy = reviewerName;
    }

    public boolean isVerified() {
        return verificationStatus == VerificationStatus.APPROVED;
    }

    public boolean isExpired() {
        return expiryDate != null && expiryDate.isBefore(LocalDate.now());
    }

    public boolean needsResubmission() {
        return verificationStatus == VerificationStatus.RESUBMIT_REQUIRED || 
               verificationStatus == VerificationStatus.REJECTED;
    }

    public String getFileExtension() {
        if (originalFilename == null) return "";
        int lastDotIndex = originalFilename.lastIndexOf('.');
        return lastDotIndex > 0 ? originalFilename.substring(lastDotIndex + 1).toLowerCase() : "";
    }

    public String getFileSizeFormatted() {
        if (fileSize == null) return "0 B";
        
        long size = fileSize;
        String[] units = {"B", "KB", "MB", "GB"};
        int unitIndex = 0;
        
        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }
        
        return String.format("%d %s", size, units[unitIndex]);
    }

    public boolean isImage() {
        return contentType != null && contentType.startsWith("image/");
    }

    public boolean isPdf() {
        return "application/pdf".equals(contentType);
    }

    public boolean isOfficeDocument() {
        return contentType != null && (
            contentType.contains("document") || 
            contentType.contains("spreadsheet") ||
            contentType.contains("presentation") ||
            contentType.contains("msword") ||
            contentType.contains("excel") ||
            contentType.contains("powerpoint")
        );
    }

    public void updateOcrResults(String ocrText, String extractedData, BigDecimal confidenceScore) {
        this.ocrText = ocrText;
        this.extractedData = extractedData;
        this.aiConfidenceScore = confidenceScore;
        this.processingStatus = "COMPLETED";
    }

    public void markProcessingFailed(String errorMessage) {
        this.processingStatus = "FAILED";
        this.processingError = errorMessage;
    }

    // Getters and Setters
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }

    public LoanApplication getLoanApplication() { return loanApplication; }
    public void setLoanApplication(LoanApplication loanApplication) { this.loanApplication = loanApplication; }

    public DocumentType getDocumentType() { return documentType; }
    public void setDocumentType(DocumentType documentType) { this.documentType = documentType; }

    public String getOriginalFilename() { return originalFilename; }
    public void setOriginalFilename(String originalFilename) { this.originalFilename = originalFilename; }

    public String getStoredFilename() { return storedFilename; }
    public void setStoredFilename(String storedFilename) { this.storedFilename = storedFilename; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public String getChecksum() { return checksum; }
    public void setChecksum(String checksum) { this.checksum = checksum; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }

    public VerificationStatus getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(VerificationStatus verificationStatus) { this.verificationStatus = verificationStatus; }

    public LocalDateTime getVerifiedAt() { return verifiedAt; }
    public void setVerifiedAt(LocalDateTime verifiedAt) { this.verifiedAt = verifiedAt; }

    public String getVerifiedBy() { return verifiedBy; }
    public void setVerifiedBy(String verifiedBy) { this.verifiedBy = verifiedBy; }

    public String getVerificationNotes() { return verificationNotes; }
    public void setVerificationNotes(String verificationNotes) { this.verificationNotes = verificationNotes; }

    public String getOcrText() { return ocrText; }
    public void setOcrText(String ocrText) { this.ocrText = ocrText; }

    public String getExtractedData() { return extractedData; }
    public void setExtractedData(String extractedData) { this.extractedData = extractedData; }

    public BigDecimal getAiConfidenceScore() { return aiConfidenceScore; }
    public void setAiConfidenceScore(BigDecimal aiConfidenceScore) { this.aiConfidenceScore = aiConfidenceScore; }

    public String getProcessingStatus() { return processingStatus; }
    public void setProcessingStatus(String processingStatus) { this.processingStatus = processingStatus; }

    public String getProcessingError() { return processingError; }
    public void setProcessingError(String processingError) { this.processingError = processingError; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public Boolean getIsRequired() { return isRequired; }
    public void setIsRequired(Boolean isRequired) { this.isRequired = isRequired; }

    public Integer getVersionNumber() { return versionNumber; }
    public void setVersionNumber(Integer versionNumber) { this.versionNumber = versionNumber; }

    public java.util.UUID getPreviousDocumentId() { return previousDocumentId; }
    public void setPreviousDocumentId(java.util.UUID previousDocumentId) { this.previousDocumentId = previousDocumentId; }

    // toString
    @Override
    public String toString() {
        return "Document{" +
                "id=" + getId() +
                ", documentType=" + documentType +
                ", originalFilename='" + originalFilename + '\'' +
                ", verificationStatus=" + verificationStatus +
                ", fileSize=" + getFileSizeFormatted() +
                ", uploadedAt=" + uploadedAt +
                '}';
    }
}