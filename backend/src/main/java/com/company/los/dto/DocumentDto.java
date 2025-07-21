package com.company.los.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.company.los.entity.Document;
import com.company.los.enums.DocumentType;
import jakarta.validation.constraints.*;

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

    private UUID id;

    @NotNull(message = "Харилцагч заавал байх ёстой")
    private UUID customerId;

    private UUID loanApplicationId;

    @NotNull(message = "Баримтын төрөл заавал сонгох ёстой")
    private DocumentType documentType;

    @NotBlank(message = "Файлын нэр заавал байх ёстой")
    @Size(max = 255, message = "Файлын нэр 255 тэмдэгтээс ихгүй байх ёстой")
    private String originalFilename;

    private String storedFilename;
    private String filePath;

    @NotBlank(message = "Файлын төрөл заавал байх ёстой")
    private String contentType;

    @NotNull(message = "Файлын хэмжээ заавал байх ёстой")
    @Min(value = 1, message = "Файлын хэмжээ 0-ээс их байх ёстой")
    @Max(value = 52428800, message = "Файлын хэмжээ 50MB-аас бага байх ёстой")
    private Long fileSize;

    private String checksum;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime uploadedAt;

    private Document.VerificationStatus verificationStatus;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime verifiedAt;

    @Size(max = 100, message = "Баталгаажуулсан хүний нэр 100 тэмдэгтээс ихгүй байх ёстой")
    private String verifiedBy;

    @Size(max = 1000, message = "Баталгаажуулалтын тэмдэглэл 1000 тэмдэгтээс ихгүй байх ёстой")
    private String verificationNotes;

    // OCR болон AI мэдээлэл
    private String ocrText;
    private String extractedData;

    @DecimalMin(value = "0.0", message = "AI итгэлцлийн оноо сөрөг байж болохгүй")
    @DecimalMax(value = "100.0", message = "AI итгэлцлийн оноо 100-аас их байж болохгүй")
    private BigDecimal aiConfidenceScore;

    private String processingStatus;
    private String processingError;

    // Metadata
    @Size(max = 500, message = "Тайлбар 500 тэмдэгтээс ихгүй байх ёстой")
    private String description;

    @Size(max = 255, message = "Таг 255 тэмдэгтээс ихгүй байх ёстой")
    private String tags;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expiryDate;

    private Boolean isRequired;

    @Min(value = 1, message = "Хувилбарын дугаар 1-ээс бага байж болохгүй")
    private Integer versionNumber;

    private UUID previousDocumentId;

    // Audit fields
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    private String createdBy;
    private String updatedBy;

    // Related objects
    private CustomerDto customer;
    private LoanApplicationDto loanApplication;

    // Computed fields (read-only)
    private String documentTypeDisplay;
    private String verificationStatusDisplay;
    private String fileSizeFormatted;
    private String fileExtension;
    private Boolean isVerified;
    private Boolean isExpired;
    private Boolean needsResubmission;
    private Boolean isImage;
    private Boolean isPdf;
    private Boolean isOfficeDocument;
    private String statusBadgeClass;
    private Integer verificationDaysElapsed;

    // Constructors
    public DocumentDto() {
        this.uploadedAt = LocalDateTime.now();
        this.verificationStatus = Document.VerificationStatus.PENDING;
        this.versionNumber = 1;
        this.isRequired = false;
    }

    public DocumentDto(UUID customerId, DocumentType documentType, String originalFilename, String contentType, Long fileSize) {
        this();
        this.customerId = customerId;
        this.documentType = documentType;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.fileSize = fileSize;
    }

    // Static factory methods
    public static DocumentDto fromEntity(Document document) {
        DocumentDto dto = new DocumentDto();
        dto.setId(document.getId());
        dto.setCustomerId(document.getCustomer().getId());
        
        if (document.getLoanApplication() != null) {
            dto.setLoanApplicationId(document.getLoanApplication().getId());
        }
        
        dto.setDocumentType(document.getDocumentType());
        dto.setOriginalFilename(document.getOriginalFilename());
        dto.setStoredFilename(document.getStoredFilename());
        dto.setFilePath(document.getFilePath());
        dto.setContentType(document.getContentType());
        dto.setFileSize(document.getFileSize());
        dto.setChecksum(document.getChecksum());
        dto.setUploadedAt(document.getUploadedAt());
        dto.setVerificationStatus(document.getVerificationStatus());
        dto.setVerifiedAt(document.getVerifiedAt());
        dto.setVerifiedBy(document.getVerifiedBy());
        dto.setVerificationNotes(document.getVerificationNotes());
        dto.setOcrText(document.getOcrText());
        dto.setExtractedData(document.getExtractedData());
        dto.setAiConfidenceScore(document.getAiConfidenceScore());
        dto.setProcessingStatus(document.getProcessingStatus());
        dto.setProcessingError(document.getProcessingError());
        dto.setDescription(document.getDescription());
        dto.setTags(document.getTags());
        dto.setExpiryDate(document.getExpiryDate());
        dto.setIsRequired(document.getIsRequired());
        dto.setVersionNumber(document.getVersionNumber());
        dto.setPreviousDocumentId(document.getPreviousDocumentId());
        dto.setCreatedAt(document.getCreatedAt());
        dto.setUpdatedAt(document.getUpdatedAt());
        dto.setCreatedBy(document.getCreatedBy());
        dto.setUpdatedBy(document.getUpdatedBy());

        // Set customer info if needed
        if (document.getCustomer() != null) {
            dto.setCustomer(CustomerDto.createSummary(document.getCustomer()));
        }

        // Computed fields
        dto.setDocumentTypeDisplay(document.getDocumentType().getMongolianName());
        dto.setVerificationStatusDisplay(document.getVerificationStatus().getMongolianName());
        dto.setFileSizeFormatted(document.getFileSizeFormatted());
        dto.setFileExtension(document.getFileExtension());
        dto.setIsVerified(document.isVerified());
        dto.setIsExpired(document.isExpired());
        dto.setNeedsResubmission(document.needsResubmission());
        dto.setIsImage(document.isImage());
        dto.setIsPdf(document.isPdf());
        dto.setIsOfficeDocument(document.isOfficeDocument());
        dto.setStatusBadgeClass(dto.calculateStatusBadgeClass());
        
        // Calculate verification time
        if (document.getVerifiedAt() != null && document.getUploadedAt() != null) {
            long days = java.time.Duration.between(document.getUploadedAt(), document.getVerifiedAt()).toDays();
            dto.setVerificationDaysElapsed((int) days);
        }

        return dto;
    }

    public static DocumentDto createSummary(Document document) {
        DocumentDto dto = new DocumentDto();
        dto.setId(document.getId());
        dto.setDocumentType(document.getDocumentType());
        dto.setOriginalFilename(document.getOriginalFilename());
        dto.setVerificationStatus(document.getVerificationStatus());
        dto.setUploadedAt(document.getUploadedAt());
        dto.setFileSize(document.getFileSize());
        dto.setContentType(document.getContentType());
        dto.setDocumentTypeDisplay(document.getDocumentType().getMongolianName());
        dto.setVerificationStatusDisplay(document.getVerificationStatus().getMongolianName());
        dto.setFileSizeFormatted(document.getFileSizeFormatted());
        dto.setFileExtension(document.getFileExtension());
        dto.setIsVerified(document.isVerified());
        dto.setStatusBadgeClass(dto.calculateStatusBadgeClass());
        return dto;
    }

    public Document toEntity() {
        Document document = new Document();
        document.setId(this.id);
        document.setDocumentType(this.documentType);
        document.setOriginalFilename(this.originalFilename);
        document.setStoredFilename(this.storedFilename);
        document.setFilePath(this.filePath);
        document.setContentType(this.contentType);
        document.setFileSize(this.fileSize);
        document.setChecksum(this.checksum);
        document.setUploadedAt(this.uploadedAt);
        document.setVerificationStatus(this.verificationStatus);
        document.setVerifiedAt(this.verifiedAt);
        document.setVerifiedBy(this.verifiedBy);
        document.setVerificationNotes(this.verificationNotes);
        document.setOcrText(this.ocrText);
        document.setExtractedData(this.extractedData);
        document.setAiConfidenceScore(this.aiConfidenceScore);
        document.setProcessingStatus(this.processingStatus);
        document.setProcessingError(this.processingError);
        document.setDescription(this.description);
        document.setTags(this.tags);
        document.setExpiryDate(this.expiryDate);
        document.setIsRequired(this.isRequired);
        document.setVersionNumber(this.versionNumber);
        document.setPreviousDocumentId(this.previousDocumentId);
        return document;
    }

    // Validation methods
    public boolean isValidUpload() {
        return documentType != null &&
               originalFilename != null && !originalFilename.trim().isEmpty() &&
               contentType != null && !contentType.trim().isEmpty() &&
               fileSize != null && fileSize > 0 &&
               customerId != null;
    }

    public boolean isAllowedFileType() {
        if (documentType == null || contentType == null) {
            return false;
        }
        
        String extension = getFileExtension();
        return documentType.isExtensionAllowed(extension);
    }

    public boolean isFileSizeValid() {
        return fileSize != null && fileSize > 0 && fileSize <= 52428800; // 50MB
    }

    // Business logic methods
    public String calculateStatusBadgeClass() {
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

    public String getDownloadUrl() {
        return "/api/v1/documents/" + id + "/download";
    }

    public String getPreviewUrl() {
        if (isImage || isPdf) {
            return "/api/v1/documents/" + id + "/preview";
        }
        return null;
    }

    public boolean canBeVerified() {
        return verificationStatus == Document.VerificationStatus.PENDING ||
               verificationStatus == Document.VerificationStatus.IN_REVIEW;
    }

    public boolean canBeDeleted() {
        return verificationStatus != Document.VerificationStatus.APPROVED ||
               versionNumber > 1;
    }

    public boolean hasOcrResults() {
        return ocrText != null && !ocrText.trim().isEmpty();
    }

    public boolean hasAiProcessing() {
        return aiConfidenceScore != null || 
               (processingStatus != null && !"PENDING".equals(processingStatus));
    }

    public String getProcessingStatusDisplay() {
        if (processingStatus == null) return "Хүлээгдэж байна";
        switch (processingStatus) {
            case "PROCESSING": return "Боловсруулж байна";
            case "COMPLETED": return "Дууссан";
            case "FAILED": return "Амжилтгүй";
            default: return processingStatus;
        }
    }

    public String getConfidenceScoreText() {
        if (aiConfidenceScore == null) return "";
        if (aiConfidenceScore.compareTo(BigDecimal.valueOf(80)) >= 0) return "Өндөр итгэлцэл";
        if (aiConfidenceScore.compareTo(BigDecimal.valueOf(60)) >= 0) return "Дунд итгэлцэл";
        return "Бага итгэлцэл";
    }

    public boolean isExpiringSoon() {
        if (expiryDate == null) return false;
        LocalDate thirtyDaysLater = LocalDate.now().plusDays(30);
        return expiryDate.isBefore(thirtyDaysLater) && expiryDate.isAfter(LocalDate.now());
    }

    public String getExpiryText() {
        if (expiryDate == null) return "";
        if (isExpired) return "Хугацаа дууссан";
        if (isExpiringSoon()) return "Удахгүй дуусна";
        return "Хүчинтэй";
    }

    public String[] getTagList() {
        if (tags == null || tags.trim().isEmpty()) {
            return new String[0];
        }
        return tags.split(",");
    }

    public boolean hasTag(String tag) {
        String[] tagList = getTagList();
        for (String t : tagList) {
            if (t.trim().equalsIgnoreCase(tag.trim())) {
                return true;
            }
        }
        return false;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }

    public UUID getLoanApplicationId() { return loanApplicationId; }
    public void setLoanApplicationId(UUID loanApplicationId) { this.loanApplicationId = loanApplicationId; }

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

    public Document.VerificationStatus getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(Document.VerificationStatus verificationStatus) { this.verificationStatus = verificationStatus; }

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

    public UUID getPreviousDocumentId() { return previousDocumentId; }
    public void setPreviousDocumentId(UUID previousDocumentId) { this.previousDocumentId = previousDocumentId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    public CustomerDto getCustomer() { return customer; }
    public void setCustomer(CustomerDto customer) { this.customer = customer; }

    public LoanApplicationDto getLoanApplication() { return loanApplication; }
    public void setLoanApplication(LoanApplicationDto loanApplication) { this.loanApplication = loanApplication; }

    public String getDocumentTypeDisplay() { return documentTypeDisplay; }
    public void setDocumentTypeDisplay(String documentTypeDisplay) { this.documentTypeDisplay = documentTypeDisplay; }

    public String getVerificationStatusDisplay() { return verificationStatusDisplay; }
    public void setVerificationStatusDisplay(String verificationStatusDisplay) { this.verificationStatusDisplay = verificationStatusDisplay; }

    public String getFileSizeFormatted() { return fileSizeFormatted; }
    public void setFileSizeFormatted(String fileSizeFormatted) { this.fileSizeFormatted = fileSizeFormatted; }

    public String getFileExtension() { return fileExtension; }
    public void setFileExtension(String fileExtension) { this.fileExtension = fileExtension; }

    public Boolean getIsVerified() { return isVerified; }
    public void setIsVerified(Boolean isVerified) { this.isVerified = isVerified; }

    public Boolean getIsExpired() { return isExpired; }
    public void setIsExpired(Boolean isExpired) { this.isExpired = isExpired; }

    public Boolean getNeedsResubmission() { return needsResubmission; }
    public void setNeedsResubmission(Boolean needsResubmission) { this.needsResubmission = needsResubmission; }

    public Boolean getIsImage() { return isImage; }
    public void setIsImage(Boolean isImage) { this.isImage = isImage; }

    public Boolean getIsPdf() { return isPdf; }
    public void setIsPdf(Boolean isPdf) { this.isPdf = isPdf; }

    public Boolean getIsOfficeDocument() { return isOfficeDocument; }
    public void setIsOfficeDocument(Boolean isOfficeDocument) { this.isOfficeDocument = isOfficeDocument; }

    public String getStatusBadgeClass() { return statusBadgeClass; }
    public void setStatusBadgeClass(String statusBadgeClass) { this.statusBadgeClass = statusBadgeClass; }

    public Integer getVerificationDaysElapsed() { return verificationDaysElapsed; }
    public void setVerificationDaysElapsed(Integer verificationDaysElapsed) { this.verificationDaysElapsed = verificationDaysElapsed; }

    @Override
    public String toString() {
        return "DocumentDto{" +
                "id=" + id +
                ", documentType=" + documentType +
                ", originalFilename='" + originalFilename + '\'' +
                ", verificationStatus=" + verificationStatus +
                ", fileSize=" + fileSize +
                ", uploadedAt=" + uploadedAt +
                '}';
    }
}