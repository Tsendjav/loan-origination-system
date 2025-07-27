package com.company.los.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import java.util.UUID;
import com.company.los.entity.DocumentType;

/**
 * Баримт бичгийн төрлийн DTO
 * Document Type Data Transfer Object
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocumentTypeDto {

    private static final Logger logger = LoggerFactory.getLogger(DocumentTypeDto.class);

    private String id;

    @NotBlank(message = "Нэр заавал байх ёстой")
    @Size(max = 50, message = "Нэр 50 тэмдэгтээс ихгүй байх ёстой")
    private String name;

    @Size(max = 2000, message = "Тайлбар 2000 тэмдэгтээс ихгүй байх ёстой")
    private String description;

    private Boolean isRequired = false;

    private Boolean isActive = true;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    private String createdBy;
    private String updatedBy;

    private String displayName;
    private String statusText;
    private Integer documentCount;
    private String icon;

    // Constructors
    public DocumentTypeDto() {
        this.isRequired = false;
        this.isActive = true;
    }

    public DocumentTypeDto(String name, String description, Boolean isRequired) {
        this();
        this.name = name;
        this.description = description;
        this.isRequired = isRequired;
    }

    // Static factory methods
    public static DocumentTypeDto fromEntity(DocumentType documentType) {
        if (documentType == null) {
            return null;
        }

        DocumentTypeDto dto = new DocumentTypeDto();

        if (documentType.getId() != null) {
            dto.setId(documentType.getId().toString());
        }

        dto.setName(documentType.getName());
        dto.setDescription(documentType.getDescription());
        dto.setIsRequired(documentType.getIsRequired());
        dto.setIsActive(documentType.getIsActive());
        dto.setCreatedAt(documentType.getCreatedAt());
        dto.setUpdatedAt(documentType.getUpdatedAt());

        dto.setCreatedBy(documentType.getCreatedBy());
        dto.setUpdatedBy(documentType.getUpdatedBy());

        dto.setDisplayName(dto.calculateDisplayName());
        dto.setStatusText(dto.calculateStatusText());
        dto.setIcon(dto.calculateIcon());
        dto.setDocumentCount(documentType.getDocumentCount());

        return dto;
    }

    public static DocumentTypeDto createSummary(DocumentType documentType) {
        if (documentType == null) {
            return null;
        }

        DocumentTypeDto dto = new DocumentTypeDto();

        if (documentType.getId() != null) {
            dto.setId(documentType.getId().toString());
        }

        dto.setName(documentType.getName());
        dto.setIsRequired(documentType.getIsRequired());
        dto.setIsActive(documentType.getIsActive());

        dto.setDisplayName(dto.calculateDisplayName());
        dto.setDocumentCount(documentType.getDocumentCount());

        return dto;
    }

    public DocumentType toEntity() {
        DocumentType documentType = new DocumentType();

        if (this.id != null) {
            documentType.setId(UUID.fromString(this.id));
        }

        documentType.setName(this.name);
        documentType.setDescription(this.description);
        documentType.setIsRequired(this.isRequired);
        documentType.setIsActive(this.isActive);
        documentType.setCreatedAt(this.createdAt);
        documentType.setUpdatedAt(this.updatedAt);

        documentType.setCreatedBy(this.createdBy);
        documentType.setUpdatedBy(this.updatedBy);

        return documentType;
    }

    // Helper methods
    private String calculateDisplayName() {
        if (name == null) return "";
        switch (name) {
            case "IDENTITY_CARD": return "Иргэний үнэмлэх";
            case "INCOME_STATEMENT": return "Орлогын справка";
            case "BANK_STATEMENT": return "Банкны хуулга";
            case "COLLATERAL_DOCUMENT": return "Барьцааны баримт";
            case "BUSINESS_LICENSE": return "Бизнес лиценз";
            case "PASSPORT": return "Гадаад паспорт";
            case "DRIVER_LICENSE": return "Жолооны үнэмлэх";
            case "UTILITY_BILL": return "Коммунал төлбөрийн баримт";
            case "EMPLOYMENT_CERTIFICATE": return "Ажил олгогчийн справка";
            case "TAX_CERTIFICATE": return "Татварын гэрчилгээ";
            case "LOAN_APPLICATION": return "Зээлийн хүсэлт";
            case "FINANCIAL_STATEMENT": return "Санхүүгийн тайлан";
            case "PROPERTY_CERTIFICATE": return "Өмчийн гэрчилгээ";
            case "VEHICLE_REGISTRATION": return "Тээврийн хэрэгслийн улсын дугаар";
            case "MARRIAGE_CERTIFICATE": return "Гэрлэлтийн гэрчилгээ";
            case "DIVORCE_CERTIFICATE": return "Гэр бүл цуцлуулсан гэрчилгээ";
            case "CREDIT_HISTORY": return "Зээлийн түүх";
            case "EXISTING_LOAN_AGREEMENT": return "Одоо байгаа зээлийн гэрээ";
            case "OTHER": return "Бусад";
            default: return name;
        }
    }

    private String calculateStatusText() {
        if (!isActive) return "Идэвхгүй";
        if (isRequired) return "Заавал шаардлагатай";
        return "Нэмэлт";
    }

    private String calculateIcon() {
        if (name == null) return "fas fa-file";
        switch (name) {
            case "IDENTITY_CARD":
            case "PASSPORT":
            case "DRIVER_LICENSE":
                return "fas fa-id-card";
            case "INCOME_STATEMENT":
            case "TAX_CERTIFICATE":
            case "FINANCIAL_STATEMENT":
                return "fas fa-money-bill";
            case "BANK_STATEMENT":
                return "fas fa-university";
            case "COLLATERAL_DOCUMENT":
            case "PROPERTY_CERTIFICATE":
                return "fas fa-home";
            case "BUSINESS_LICENSE":
                return "fas fa-briefcase";
            case "UTILITY_BILL":
                return "fas fa-receipt";
            case "EMPLOYMENT_CERTIFICATE":
            case "WORK_CONTRACT":
                return "fas fa-handshake";
            case "LOAN_APPLICATION":
                return "fas fa-file-invoice-dollar";
            case "VEHICLE_REGISTRATION":
                return "fas fa-car";
            case "MARRIAGE_CERTIFICATE":
            case "DIVORCE_CERTIFICATE":
                return "fas fa-heart";
            case "CREDIT_HISTORY":
            case "EXISTING_LOAN_AGREEMENT":
                return "fas fa-file-contract";
            default:
                return "fas fa-file";
        }
    }

    // Business logic methods
    public String getRequiredBadgeClass() {
        if (isRequired) return "badge-danger";
        return "badge-info";
    }

    public String getStatusBadgeClass() {
        if (!isActive) return "badge-secondary";
        if (isRequired) return "badge-danger";
        return "badge-success";
    }

    public boolean isValidName() {
        return name != null && !name.trim().isEmpty() && name.length() <= 50;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Boolean getIsRequired() { return isRequired; }
    public void setIsRequired(Boolean isRequired) { this.isRequired = isRequired; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getStatusText() { return statusText; }
    public void setStatusText(String statusText) { this.statusText = statusText; }

    public Integer getDocumentCount() { return documentCount; }
    public void setDocumentCount(Integer documentCount) { this.documentCount = documentCount; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    @Override
    public String toString() {
        return "DocumentTypeDto{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", isRequired=" + isRequired +
                ", isActive=" + isActive +
                '}';
    }
}