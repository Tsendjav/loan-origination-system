package com.company.los.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.company.los.entity.Document;
import com.company.los.entity.DocumentType; // Changed: Corrected import for DocumentType
import jakarta.validation.constraints.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Баримт бичгийн DTO
 * Document Data Transfer Object
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocumentDto {

    private static final Logger logger = LoggerFactory.getLogger(DocumentDto.class);

    private UUID id;

    private UUID customerId;
    private String customerName;

    private UUID loanApplicationId;
    private String loanApplicationNumber;

    private UUID documentTypeId;
    private DocumentType documentType;
    private String documentTypeName;

    @NotBlank(message = "Файлын нэр заавал байх ёстой")
    @Size(max = 500, message = "Файлын нэр 500 тэмдэгтээс ихгүй байх ёстой")
    private String originalFilename;

    private String storedFilename;
    private String filePath;

    @NotBlank(message = "Файлын төрөл заавал байх ёстой")
    @Size(max = 100, message = "Файлын төрөл 100 тэмдэгтээс ихгүй байх ёстой")
    private String contentType;

    @NotNull(message = "Файлын хэмжээ заавал байх ёстой")
    @Min(value = 1, message = "Файлын хэмжээ 0-ээс их байх ёстой")
    private Long fileSize;

    private String fileSizeFormatted;

    @Size(max = 256, message = "Checksum 256 тэмдэгтээс ихгүй байх ёстой")
    private String checksum;

    @Size(max = 1000, message = "Тайлбар 1000 тэмдэгтээс ихгүй байх ёстой")
    private String description;

    @Size(max = 1000, message = "Tags 1000 тэмдэгтээс ихгүй байх ёстой")
    private String tags;

    @Min(value = 1, message = "Хувилбарын дугаар 1-ээс бага байж болохгүй")
    private Integer versionNumber;

    private UUID previousDocumentId;

    // Баталгаажуулалтын мэдээлэл
    @NotNull(message = "Баталгаажуулалтын статус заавал байх ёстой")
    private Document.VerificationStatus verificationStatus;

    @Size(max = 100, message = "Баталгаажуулсан хүн 100 тэмдэгтээс ихгүй байх ёстой")
    private String verifiedBy;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime verifiedAt;

    private String verificationNotes;

    // Хугацаа
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expiryDate;

    // Шаардлага
    private Boolean isRequired;

    // Боловсруулалтын мэдээлэл
    @Size(max = 50, message = "Боловсруулалтын статус 50 тэмдэгтээс ихгүй байх ёстой")
    private String processingStatus;

    private String processingError;
    private String ocrText;
    private String extractedData;

    @DecimalMin(value = "0.0", message = "AI итгэлцлийн оноо сөрөг байж болохгүй")
    @DecimalMax(value = "1.0", message = "AI итгэлцлийн оноо 1.0-аас их байж болохгүй")
    private BigDecimal aiConfidenceScore;

    // Илгээх мэдээлэл
    @NotNull(message = "Илгээсэн огноо заавал байх ёстой")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime uploadedAt;

    @Size(max = 100, message = "Илгээсэн хүн 100 тэмдэгтээс ихгүй байх ёстой")
    private String uploadedBy;

    // Метаданные
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    private String createdBy;
    private String updatedBy;

    // Computed fields (read-only)
    private String verificationStatusText;
    private String fileExtension;
    private Boolean isImage;
    private Boolean isPdf;
    private Boolean isOfficeDocument;
    private Boolean isExpired;
    private Boolean needsResubmission;
    private Integer daysSinceUpload;
    private Integer daysSinceVerification;

    // Constructors
    public DocumentDto() {
        this.verificationStatus = Document.VerificationStatus.PENDING;
        this.versionNumber = 1;
        this.isRequired = false;
        this.uploadedAt = LocalDateTime.now();
    }

    public DocumentDto(UUID customerId, UUID documentTypeId, String originalFilename,
                      String contentType, Long fileSize) {
        this();
        this.customerId = customerId;
        this.documentTypeId = documentTypeId;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.fileSize = fileSize;
    }

    // Static factory methods
    public static DocumentDto fromEntity(Document document) {
        if (document == null) {
            return null;
        }

        DocumentDto dto = new DocumentDto();

        // ID is already UUID in BaseEntity, no conversion needed
        dto.setId(document.getId());
        dto.setOriginalFilename(document.getOriginalFilename());
        dto.setStoredFilename(document.getStoredFilename());
        dto.setFilePath(document.getFilePath());
        dto.setContentType(document.getContentType());
        dto.setFileSize(document.getFileSize());
        dto.setChecksum(document.getChecksum());
        dto.setDescription(document.getDescription());
        dto.setTags(document.getTags());
        dto.setVersionNumber(document.getVersionNumber());

        // Previous document ID is already UUID, no conversion needed
        dto.setPreviousDocumentId(document.getPreviousDocumentId());
        dto.setVerificationStatus(document.getVerificationStatus());
        dto.setVerifiedBy(document.getVerifiedBy());
        dto.setVerifiedAt(document.getVerifiedAt());
        dto.setVerificationNotes(document.getVerificationNotes());
        dto.setExpiryDate(document.getExpiryDate());
        dto.setIsRequired(document.getIsRequired());
        dto.setProcessingStatus(document.getProcessingStatus());
        dto.setProcessingError(document.getProcessingError());
        dto.setOcrText(document.getOcrText());
        dto.setExtractedData(document.getExtractedData());
        dto.setAiConfidenceScore(document.getAiConfidenceScore());
        dto.setUploadedAt(document.getUploadedAt());
        dto.setUploadedBy(document.getUploadedBy());
        dto.setCreatedAt(document.getCreatedAt());
        dto.setUpdatedAt(document.getUpdatedAt());

        // Audit fields are String in both DTO and BaseEntity
        dto.setCreatedBy(document.getCreatedBy());
        dto.setUpdatedBy(document.getUpdatedBy());

        // Safe customer ID extraction - Customer entity has UUID id
        if (document.getCustomer() != null && document.getCustomer().getId() != null) {
            dto.setCustomerId(document.getCustomer().getId());
            dto.setCustomerName(document.getCustomer().getDisplayName());
        }

        // Safe loan application ID extraction - LoanApplication entity has UUID id
        if (document.getLoanApplication() != null && document.getLoanApplication().getId() != null) {
            dto.setLoanApplicationId(document.getLoanApplication().getId());
            dto.setLoanApplicationNumber(document.getLoanApplication().getApplicationNumber());
        }

        // Safe document type extraction
        dto.setDocumentType(document.getDocumentType());
        if (document.getDocumentType() != null) {
            dto.setDocumentTypeId(document.getDocumentType().getId());
            dto.setDocumentTypeName(document.getDocumentType().getName());
        }

        // Computed fields with safe method calls
        try {
            dto.setVerificationStatusText(document.getVerificationStatusText());
        } catch (Exception e) {
            if (document.getVerificationStatus() != null) {
                dto.setVerificationStatusText(document.getVerificationStatus().getMongolianName());
            }
        }

        dto.setFileSizeFormatted(formatFileSize(dto.getFileSize()));
        dto.setFileExtension(getFileExtension(dto.getOriginalFilename()));

        try {
            dto.setIsImage(document.isImage());
        } catch (Exception e) {
            dto.setIsImage(dto.getContentType() != null && dto.getContentType().startsWith("image/"));
        }

        try {
            dto.setIsPdf(document.isPdf());
        } catch (Exception e) {
            dto.setIsPdf("application/pdf".equals(dto.getContentType()));
        }

        try {
            dto.setIsOfficeDocument(document.isOfficeDocument());
        } catch (Exception e) {
            String contentType = dto.getContentType();
            dto.setIsOfficeDocument(contentType != null &&
                (contentType.contains("msword") || contentType.contains("excel") || contentType.contains("powerpoint")));
        }

        try {
            dto.setIsExpired(document.isExpired());
        } catch (Exception e) {
            dto.setIsExpired(dto.getExpiryDate() != null && dto.getExpiryDate().isBefore(LocalDate.now()));
        }

        try {
            dto.setNeedsResubmission(document.needsResubmission());
        } catch (Exception e) {
            dto.setNeedsResubmission(dto.getVerificationStatus() == Document.VerificationStatus.RESUBMIT_REQUIRED ||
                                   dto.getVerificationStatus() == Document.VerificationStatus.REJECTED);
        }

        // Calculate days since upload
        if (dto.getUploadedAt() != null) {
            dto.setDaysSinceUpload((int) java.time.Duration.between(dto.getUploadedAt(), LocalDateTime.now()).toDays());
        }

        // Calculate days since verification
        if (dto.getVerifiedAt() != null) {
            dto.setDaysSinceVerification((int) java.time.Duration.between(dto.getVerifiedAt(), LocalDateTime.now()).toDays());
        }

        return dto;
    }

    public static DocumentDto createSummary(Document document) {
        if (document == null) {
            return null;
        }

        DocumentDto dto = new DocumentDto();

        // ID is already UUID in BaseEntity, no conversion needed
        dto.setId(document.getId());
        dto.setOriginalFilename(document.getOriginalFilename());
        dto.setContentType(document.getContentType());
        dto.setFileSize(document.getFileSize());
        dto.setVerificationStatus(document.getVerificationStatus());
        dto.setUploadedAt(document.getUploadedAt());

        // Safe customer ID extraction - Customer entity has UUID id
        if (document.getCustomer() != null && document.getCustomer().getId() != null) {
            dto.setCustomerId(document.getCustomer().getId());
            dto.setCustomerName(document.getCustomer().getDisplayName());
        }

        // Safe document type extraction
        dto.setDocumentType(document.getDocumentType());
        if (document.getDocumentType() != null) {
            dto.setDocumentTypeId(document.getDocumentType().getId());
            dto.setDocumentTypeName(document.getDocumentType().getName());
        }

        // Set computed fields
        dto.setFileSizeFormatted(formatFileSize(dto.getFileSize()));
        dto.setFileExtension(getFileExtension(dto.getOriginalFilename()));

        try {
            dto.setVerificationStatusText(document.getVerificationStatusText());
        } catch (Exception e) {
            if (document.getVerificationStatus() != null) {
                dto.setVerificationStatusText(document.getVerificationStatus().getMongolianName());
            }
        }

        return dto;
    }

    public Document toEntity() {
        Document document = new Document();

        // ID is UUID in both DTO and entity, no conversion needed
        document.setId(this.id);
        document.setOriginalFilename(this.originalFilename);
        document.setStoredFilename(this.storedFilename);
        document.setFilePath(this.filePath);
        document.setContentType(this.contentType);
        document.setFileSize(this.fileSize);
        document.setChecksum(this.checksum);
        document.setDescription(this.description);
        document.setTags(this.tags);
        document.setVersionNumber(this.versionNumber);
        document.setVerificationStatus(this.verificationStatus);
        document.setVerifiedBy(this.verifiedBy);
        document.setVerifiedAt(this.verifiedAt);
        document.setVerificationNotes(this.verificationNotes);
        document.setExpiryDate(this.expiryDate);
        document.setIsRequired(this.isRequired);
        document.setProcessingStatus(this.processingStatus);
        document.setProcessingError(this.processingError);
        document.setOcrText(this.ocrText);
        document.setExtractedData(this.extractedData);
        document.setAiConfidenceScore(this.aiConfidenceScore);
        document.setUploadedAt(this.uploadedAt);
        document.setUploadedBy(this.uploadedBy);
        document.setCreatedAt(this.createdAt);
        document.setUpdatedAt(this.updatedAt);

        // Audit fields are String in both DTO and BaseEntity
        document.setCreatedBy(this.createdBy);
        document.setUpdatedBy(this.updatedBy);

        // Previous document ID is UUID in both DTO and entity
        document.setPreviousDocumentId(this.previousDocumentId);

        return document;
    }

    // Helper method for file size formatting
    private static String formatFileSize(Long size) {
        if (size == null) return "0 B";

        long fileSize = size;
        if (fileSize < 1024) return fileSize + " B";
        if (fileSize < 1024 * 1024) return String.format("%.1f KB", fileSize / 1024.0);
        if (fileSize < 1024 * 1024 * 1024) return String.format("%.1f MB", fileSize / (1024.0 * 1024));
        return String.format("%.1f GB", fileSize / (1024.0 * 1024 * 1024));
    }

    // Helper method for file extension extraction
    private static String getFileExtension(String filename) {
        if (filename == null) return "";
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 ? filename.substring(lastDot + 1).toLowerCase() : "";
    }

    // Business logic methods
    public String getVerificationStatusDisplay() {
        return verificationStatus != null ? verificationStatus.getMongolianName() : "Тодорхойгүй";
    }

    public String getStatusBadgeClass() {
        if (verificationStatus == null) return "badge-secondary";
        switch (verificationStatus) {
            case PENDING: return "badge-warning";
            case IN_REVIEW: return "badge-info";
            case APPROVED: return "badge-success";
            case REJECTED: case RESUBMIT_REQUIRED: return "badge-danger";
            case EXPIRED: return "badge-dark";
            case ON_HOLD: return "badge-secondary";
            default: return "badge-secondary";
        }
    }

    public boolean isValidUpload() {
        return customerId != null &&
               (documentType != null || documentTypeId != null) &&
               originalFilename != null && !originalFilename.trim().isEmpty() &&
               contentType != null && !contentType.trim().isEmpty() &&
               fileSize != null && fileSize > 0;
    }

    public boolean canBeVerified() {
        return verificationStatus == Document.VerificationStatus.PENDING ||
               verificationStatus == Document.VerificationStatus.IN_REVIEW;
    }

    public boolean canBeDeleted() {
        return verificationStatus != Document.VerificationStatus.APPROVED ||
               (versionNumber != null && versionNumber > 1);
    }

    public boolean hasOcrResults() {
        return ocrText != null && !ocrText.trim().isEmpty();
    }

    public boolean hasAiProcessing() {
        return aiConfidenceScore != null ||
               (processingStatus != null && !"PENDING".equals(processingStatus));
    }

    public String getDownloadUrl() {
        return "/api/v1/documents/" + id + "/download";
    }

    public String getPreviewUrl() {
        if (Boolean.TRUE.equals(isImage) || Boolean.TRUE.equals(isPdf)) {
            return "/api/v1/documents/" + id + "/preview";
        }
        return null;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public UUID getLoanApplicationId() { return loanApplicationId; }
    public void setLoanApplicationId(UUID loanApplicationId) { this.loanApplicationId = loanApplicationId; }

    public String getLoanApplicationNumber() { return loanApplicationNumber; }
    public void setLoanApplicationNumber(String loanApplicationNumber) { this.loanApplicationNumber = loanApplicationNumber; }

    public UUID getDocumentTypeId() { return documentTypeId; }
    public void setDocumentTypeId(UUID documentTypeId) { this.documentTypeId = documentTypeId; }

    public DocumentType getDocumentType() { return documentType; }
    public void setDocumentType(DocumentType documentType) { this.documentType = documentType; }

    public String getDocumentTypeName() { return documentTypeName; }
    public void setDocumentTypeName(String documentTypeName) { this.documentTypeName = documentTypeName; }

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

    public String getFileSizeFormatted() { return fileSizeFormatted; }
    public void setFileSizeFormatted(String fileSizeFormatted) { this.fileSizeFormatted = fileSizeFormatted; }

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

    public Document.VerificationStatus getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(Document.VerificationStatus verificationStatus) { this.verificationStatus = verificationStatus; }

    public String getVerifiedBy() { return verifiedBy; }
    public void setVerifiedBy(String verifiedBy) { this.verifiedBy = verifiedBy; }

    public LocalDateTime getVerifiedAt() { return verifiedAt; }
    public void setVerifiedAt(LocalDateTime verifiedAt) { this.verifiedAt = verifiedAt; }

    public String getVerificationNotes() { return verificationNotes; }
    public void setVerificationNotes(String verificationNotes) { this.verificationNotes = verificationNotes; }

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

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    public String getVerificationStatusText() { return verificationStatusText; }
    public void setVerificationStatusText(String verificationStatusText) { this.verificationStatusText = verificationStatusText; }

    public String getFileExtension() { return fileExtension; }
    public void setFileExtension(String fileExtension) { this.fileExtension = fileExtension; }

    public Boolean getIsImage() { return isImage; }
    public void setIsImage(Boolean isImage) { this.isImage = isImage; }

    public Boolean getIsPdf() { return isPdf; }
    public void setIsPdf(Boolean isPdf) { this.isPdf = isPdf; }

    public Boolean getIsOfficeDocument() { return isOfficeDocument; }
    public void setIsOfficeDocument(Boolean isOfficeDocument) { this.isOfficeDocument = isOfficeDocument; }

    public Boolean getIsExpired() { return isExpired; }
    public void setIsExpired(Boolean isExpired) { this.isExpired = isExpired; }

    public Boolean getNeedsResubmission() { return needsResubmission; }
    public void setNeedsResubmission(Boolean needsResubmission) { this.needsResubmission = needsResubmission; }

    public Integer getDaysSinceUpload() { return daysSinceUpload; }
    public void setDaysSinceUpload(Integer daysSinceUpload) { this.daysSinceUpload = daysSinceUpload; }

    public Integer getDaysSinceVerification() { return daysSinceVerification; }
    public void setDaysSinceVerification(Integer daysSinceVerification) { this.daysSinceVerification = daysSinceVerification; }

    @Override
    public String toString() {
        return "DocumentDto{" +
                "id=" + (id != null ? id.toString() : "null") +
                ", documentType=" + (documentType != null ? documentType.getName() : "null") +
                ", originalFilename='" + originalFilename + '\'' +
                ", verificationStatus=" + verificationStatus +
                ", fileSize=" + fileSize +
                ", uploadedAt=" + uploadedAt +
                '}';
    }
}
