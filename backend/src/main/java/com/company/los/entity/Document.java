package com.company.los.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Баримт бичгийн Entity
 * Document Entity
 */
@Entity
@Table(name = "documents", indexes = {
        @Index(name = "idx_documents_customer_id", columnList = "customer_id"),
        @Index(name = "idx_documents_loan_application_id", columnList = "loan_application_id"),
        @Index(name = "idx_documents_document_type_id", columnList = "document_type_id"),
        @Index(name = "idx_documents_verification_status", columnList = "verification_status")
})
@SQLDelete(sql = "UPDATE documents SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
public class Document extends BaseEntity {

    // Enum definitions
    public enum VerificationStatus {
        PENDING("PENDING", "Хүлээгдэж байгаа"),
        IN_REVIEW("IN_REVIEW", "Шалгаж байгаа"),
        APPROVED("APPROVED", "Баталгаажуулсан"),
        REJECTED("REJECTED", "Татгалзсан"),
        EXPIRED("EXPIRED", "Хугацаа дууссан"),
        RESUBMIT_REQUIRED("RESUBMIT_REQUIRED", "Дахин илгээх"),
        ON_HOLD("ON_HOLD", "Түр зогсоосон");

        private final String code;
        private final String mongolianName;

        VerificationStatus(String code, String mongolianName) {
            this.code = code;
            this.mongolianName = mongolianName;
        }

        public String getCode() { return code; }
        public String getMongolianName() { return mongolianName; }
    }

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false, columnDefinition = "VARCHAR(36)",
                foreignKey = @ForeignKey(name = "fk_document_customer"))
    @NotNull(message = "Харилцагч заавал байх ёстой")
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_application_id", columnDefinition = "VARCHAR(36)",
                foreignKey = @ForeignKey(name = "fk_document_loan_app"))
    private LoanApplication loanApplication;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_type_id", nullable = false, columnDefinition = "VARCHAR(36)",
                foreignKey = @ForeignKey(name = "fk_document_type"))
    @NotNull(message = "Баримтын төрөл заавал сонгох ёстой")
    private DocumentType documentType;

    @Column(name = "original_filename", nullable = false, length = 500)
    @NotBlank(message = "Файлын нэр заавал байх ёстой")
    @Size(max = 500, message = "Файлын нэр 500 тэмдэгтээс ихгүй байх ёстой")
    private String originalFilename;

    @Column(name = "stored_filename", nullable = false, length = 500)
    @NotBlank(message = "Хадгалсан файлын нэр заавал байх ёстой")
    @Size(max = 500, message = "Хадгалсан файлын нэр 500 тэмдэгтээс ихгүй байх ёстой")
    private String storedFilename;

    @Column(name = "file_path", nullable = false, columnDefinition = "TEXT")
    @NotBlank(message = "Файлын зам заавал байх ёстой")
    private String filePath;

    @Column(name = "content_type", nullable = false, length = 100)
    @NotBlank(message = "Файлын төрөл заавал байх ёстой")
    @Size(max = 100, message = "Файлын төрөл 100 тэмдэгтээс ихгүй байх ёстой")
    private String contentType;

    @Column(name = "file_size", nullable = false)
    @NotNull(message = "Файлын хэмжээ заавал байх ёстой")
    @Min(value = 1, message = "Файлын хэмжээ 0-ээс их байх ёстой")
    private Long fileSize;

    @Column(name = "checksum", length = 256)
    @Size(max = 256, message = "Checksum 256 тэмдэгтээс ихгүй байх ёстой")
    private String checksum;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "tags", length = 1000)
    @Size(max = 1000, message = "Таг 1000 тэмдэгтээс ихгүй байх ёстой")
    private String tags;

    @Column(name = "version_number")
    @Min(value = 1, message = "Хувилбарын дугаар 1-ээс бага байж болохгүй")
    private Integer versionNumber = 1;

    @Column(name = "previous_document_id", columnDefinition = "VARCHAR(36)")
    private UUID previousDocumentId;

    @Column(name = "verification_status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Баталгаажуулалтын статус заавал байх ёстой")
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    @Column(name = "verified_by", length = 100)
    @Size(max = 100, message = "Баталгаажуулсан хүний нэр 100 тэмдэгтээс ихгүй байх ёстой")
    private String verifiedBy;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "verification_notes", columnDefinition = "TEXT")
    private String verificationNotes;

    @Column(name = "status", length = 50)
    @Size(max = 50, message = "Статус 50 тэмдэгтээс ихгүй байх ёстой")
    private String status = "PENDING";

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "is_required", nullable = false)
    private Boolean isRequired = false;

    @Column(name = "processing_status", length = 50)
    @Size(max = 50, message = "Боловсруулалтын статус 50 тэмдэгтээс ихгүй байх ёстой")
    private String processingStatus;

    @Column(name = "processing_error", columnDefinition = "TEXT")
    private String processingError;

    @Column(name = "ocr_text", columnDefinition = "TEXT")
    private String ocrText;

    @Column(name = "extracted_data", columnDefinition = "TEXT")
    private String extractedData;

    @Column(name = "ai_confidence_score", precision = 5, scale = 4)
    @DecimalMin(value = "0.0", message = "AI итгэлцлийн оноо сөрөг байж болохгүй")
    @DecimalMax(value = "1.0", message = "AI итгэлцлийн оноо 1.0-аас их байж болохгүй")
    private BigDecimal aiConfidenceScore;

    @Column(name = "uploaded_at", nullable = false)
    @NotNull(message = "Илгээсэн огноо заавал байх ёстой")
    private LocalDateTime uploadedAt;

    @Column(name = "uploaded_by", length = 100)
    @Size(max = 100, message = "Илгээсэн хүний нэр 100 тэмдэгтээс ихгүй байх ёстой")
    private String uploadedBy;

    @Column(name = "is_active")
    private Boolean isActive = true;

    // Constructors
    public Document() {
        super();
        this.uploadedAt = LocalDateTime.now();
    }

    public Document(Customer customer, DocumentType documentType, String originalFilename,
                   String storedFilename, String filePath, String contentType, Long fileSize) {
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
        this.status = "APPROVED";
    }

    public void reject(String verifierName, String reason) {
        this.verificationStatus = VerificationStatus.REJECTED;
        this.verifiedAt = LocalDateTime.now();
        this.verifiedBy = verifierName;
        this.verificationNotes = reason;
        this.status = "REJECTED";
    }

    public void startReview(String reviewerName) {
        this.verificationStatus = VerificationStatus.IN_REVIEW;
        this.verifiedBy = reviewerName;
        this.status = "IN_REVIEW";
    }

    public boolean isVerified() {
        return VerificationStatus.APPROVED.equals(verificationStatus);
    }

    public boolean isExpired() {
        return expiryDate != null && expiryDate.isBefore(LocalDate.now());
    }

    public boolean needsResubmission() {
        return VerificationStatus.RESUBMIT_REQUIRED.equals(verificationStatus) ||
               VerificationStatus.REJECTED.equals(verificationStatus);
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

    public String getVerificationStatusText() {
        return verificationStatus != null ? verificationStatus.getMongolianName() : "Тодорхойгүй";
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

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public Integer getVersionNumber() { return versionNumber; }
    public void setVersionNumber(Integer versionNumber) { this.versionNumber = versionNumber; }

    public UUID getPreviousDocumentId() { return previousDocumentId; }
    public void setPreviousDocumentId(UUID previousDocumentId) { this.previousDocumentId = previousDocumentId; }

    public VerificationStatus getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(VerificationStatus verificationStatus) { 
        this.verificationStatus = verificationStatus;
        this.status = verificationStatus != null ? verificationStatus.getCode() : "PENDING";
    }

    public String getVerifiedBy() { return verifiedBy; }
    public void setVerifiedBy(String verifiedBy) { this.verifiedBy = verifiedBy; }

    public LocalDateTime getVerifiedAt() { return verifiedAt; }
    public void setVerifiedAt(LocalDateTime verifiedAt) { this.verifiedAt = verifiedAt; }

    public String getVerificationNotes() { return verificationNotes; }
    public void setVerificationNotes(String verificationNotes) { this.verificationNotes = verificationNotes; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public Boolean getIsRequired() { return isRequired; }
    public void setIsRequired(Boolean isRequired) { this.isRequired = isRequired; }

    public String getProcessingStatus() { return processingStatus; }
    public void setProcessingStatus(String processingStatus) { this.processingStatus = processingStatus; }

    public String getProcessingError() { return processingError; }
    public void setProcessingError(String processingError) { this.processingError = processingError; }

    public String getOcrText() { return ocrText; }
    public void setOcrText(String ocrText) { this.ocrText = ocrText; }

    public String getExtractedData() { return extractedData; }
    public void setExtractedData(String extractedData) { this.extractedData = extractedData; }

    public BigDecimal getAiConfidenceScore() { return aiConfidenceScore; }
    public void setAiConfidenceScore(BigDecimal aiConfidenceScore) { this.aiConfidenceScore = aiConfidenceScore; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }

    public String getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(String uploadedBy) { this.uploadedBy = uploadedBy; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    @Override
    public String toString() {
        return "Document{" +
                "id=" + getId() +
                ", documentType=" + (documentType != null ? documentType.getName() : "null") +
                ", originalFilename='" + originalFilename + '\'' +
                ", verificationStatus=" + verificationStatus +
                ", fileSize=" + getFileSizeFormatted() +
                ", uploadedAt=" + uploadedAt +
                '}';
    }
}