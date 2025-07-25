package com.company.los.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Баримт бичгийн төрөл Entity
 * Document Type Entity
 */
@Entity
@Table(name = "document_types", indexes = {
        @Index(name = "idx_document_type_name", columnList = "name", unique = true)
})
@SQLDelete(sql = "UPDATE document_types SET is_deleted = true WHERE id = ?")
@Where(clause = "is_deleted = false")
public class DocumentType extends BaseEntity {

    @Column(name = "name", nullable = false, length = 50, unique = true)
    @NotBlank(message = "Баримтын төрлийн нэр заавал байх ёстой")
    @Size(max = 50, message = "Нэр 50 тэмдэгтээс ихгүй байх ёстой")
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_required", nullable = false)
    private Boolean isRequired = false;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    // Documents using this type
    @OneToMany(mappedBy = "documentType", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Document> documents = new ArrayList<>();

    // Constructors
    public DocumentType() {
        super();
    }

    public DocumentType(String name, String description, Boolean isRequired) {
        this();
        this.name = name;
        this.description = description;
        this.isRequired = isRequired != null ? isRequired : false;
    }

    public DocumentType(String name, String description) {
        this(name, description, false);
    }

    // Business methods
    public boolean isExtensionAllowed(String extension) {
        if (extension == null) return false;
        String ext = extension.toLowerCase();
        
        switch (name.toUpperCase()) {
            case "IDENTITY_CARD":
            case "PASSPORT":
            case "DRIVER_LICENSE":
                return ext.equals("jpg") || ext.equals("jpeg") || ext.equals("png") || ext.equals("pdf");
            case "INCOME_STATEMENT":
            case "SALARY_CERTIFICATE":
            case "BANK_STATEMENT":
            case "EMPLOYMENT_CERTIFICATE":
            case "FINANCIAL_STATEMENT":
                return ext.equals("pdf") || ext.equals("doc") || ext.equals("docx") || 
                       ext.equals("xls") || ext.equals("xlsx");
            case "BUSINESS_LICENSE":
            case "TAX_CERTIFICATE":
            case "PROPERTY_CERTIFICATE":
            case "COLLATERAL_DOCUMENT":
            case "VEHICLE_REGISTRATION":
                return ext.equals("pdf") || ext.equals("jpg") || ext.equals("jpeg") || ext.equals("png");
            default:
                return ext.equals("pdf") || ext.equals("jpg") || ext.equals("jpeg") || 
                       ext.equals("png") || ext.equals("doc") || ext.equals("docx");
        }
    }

    public String getMongolianName() {
        switch (name.toUpperCase()) {
            case "IDENTITY_CARD": return "Иргэний үнэмлэх";
            case "PASSPORT": return "Гадаад паспорт";
            case "DRIVER_LICENSE": return "Жолооны үнэмлэх";
            case "INCOME_STATEMENT": return "Орлогын справка";
            case "SALARY_CERTIFICATE": return "Цалингийн справка";
            case "BANK_STATEMENT": return "Банкны хуулга";
            case "EMPLOYMENT_CERTIFICATE": return "Ажил олгогчийн справка";
            case "WORK_CONTRACT": return "Хөдөлмөрийн гэрээ";
            case "BUSINESS_LICENSE": return "Бизнесийн үйл ажиллагааны гэрчилгээ";
            case "TAX_CERTIFICATE": return "Татварын гэрчилгээ";
            case "FINANCIAL_STATEMENT": return "Санхүүгийн тайлан";
            case "PROPERTY_CERTIFICATE": return "Өмчийн гэрчилгээ";
            case "COLLATERAL_DOCUMENT": return "Барьцааны баримт";
            case "VEHICLE_REGISTRATION": return "Тээврийн хэрэгслийн улсын дугаар";
            case "UTILITY_BILL": return "Коммунальные услуги";
            case "MARRIAGE_CERTIFICATE": return "Гэрлэлтийн гэрчилгээ";
            case "DIVORCE_CERTIFICATE": return "Гэр бүл цуцлуулсан гэрчилгээ";
            case "CREDIT_HISTORY": return "Зээлийн түүх";
            case "EXISTING_LOAN_AGREEMENT": return "Одоо байгаа зээлийн гэрээ";
            case "OTHER": return "Бусад";
            default: return name;
        }
    }

    public String getDisplayName() {
        return getMongolianName();
    }

    public boolean canBeDeleted() {
        return documents == null || documents.isEmpty();
    }

    public int getDocumentCount() {
        return documents != null ? documents.size() : 0;
    }

    public void enable() {
        this.isActive = true;
        this.setUpdatedAt(LocalDateTime.now());
    }

    public void disable() {
        this.isActive = false;
        this.setUpdatedAt(LocalDateTime.now());
    }

    public void makeRequired() {
        this.isRequired = true;
        this.setUpdatedAt(LocalDateTime.now());
    }

    public void makeOptional() {
        this.isRequired = false;
        this.setUpdatedAt(LocalDateTime.now());
    }

    // Static methods for commonly used document types
    public static DocumentType createIdentityCard() {
        return new DocumentType("IDENTITY_CARD", "Identity card for personal identification", true);
    }

    public static DocumentType createIncomeStatement() {
        return new DocumentType("INCOME_STATEMENT", "Income statement for loan application", true);
    }

    public static DocumentType createBankStatement() {
        return new DocumentType("BANK_STATEMENT", "Bank account statement", true);
    }

    public static DocumentType createEmploymentCertificate() {
        return new DocumentType("EMPLOYMENT_CERTIFICATE", "Employment verification certificate", true);
    }

    public static DocumentType createBusinessLicense() {
        return new DocumentType("BUSINESS_LICENSE", "Business operation license", false);
    }

    public static DocumentType createFinancialStatement() {
        return new DocumentType("FINANCIAL_STATEMENT", "Company financial statements", true);
    }

    public static DocumentType createTaxCertificate() {
        return new DocumentType("TAX_CERTIFICATE", "Tax clearance certificate", true);
    }

    /**
     * Returns a list of all predefined DocumentType instances that are considered 'required'.
     * @return List of required DocumentType instances.
     */
    public static List<DocumentType> getRequiredDocuments() {
        return Arrays.asList(
            createIdentityCard(),
            createIncomeStatement(),
            createBankStatement(),
            createEmploymentCertificate()
        ).stream().filter(DocumentType::getIsRequired).collect(Collectors.toList());
    }

    /**
     * Returns a list of DocumentType instances relevant to a specific loan type.
     * @param loanType The type of loan (e.g., "PERSONAL", "BUSINESS").
     * @return List of DocumentType instances relevant to the given loan type.
     */
    public static List<DocumentType> getDocumentsByLoanType(String loanType) {
        switch (loanType.toUpperCase()) {
            case "PERSONAL":
                return Arrays.asList(
                    createIdentityCard(),
                    createIncomeStatement(),
                    createBankStatement(),
                    createEmploymentCertificate()
                );
            case "BUSINESS":
                return Arrays.asList(
                    createIdentityCard(),
                    createBusinessLicense(),
                    createFinancialStatement(),
                    createTaxCertificate()
                );
            default:
                return new ArrayList<>();
        }
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { 
        this.name = name;
        this.setUpdatedAt(LocalDateTime.now());
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { 
        this.description = description;
        this.setUpdatedAt(LocalDateTime.now());
    }

    public Boolean getIsRequired() { return isRequired; }
    public void setIsRequired(Boolean isRequired) { 
        this.isRequired = isRequired;
        this.setUpdatedAt(LocalDateTime.now());
    }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { 
        this.isActive = isActive;
        this.setUpdatedAt(LocalDateTime.now());
    }

    public List<Document> getDocuments() { return documents; }
    public void setDocuments(List<Document> documents) { 
        this.documents = documents != null ? documents : new ArrayList<>();
        this.setUpdatedAt(LocalDateTime.now());
    }

    // toString
    @Override
    public String toString() {
        return "DocumentType{" +
                "id='" + getId() + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", isRequired=" + isRequired +
                ", isActive=" + isActive +
                ", documentCount=" + getDocumentCount() +
                ", isDeleted=" + getIsDeleted() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DocumentType that = (DocumentType) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}