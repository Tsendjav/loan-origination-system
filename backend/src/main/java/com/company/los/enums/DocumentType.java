package com.company.los.enums;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Баримт бичгийн төрөл
 * Document Types for Loan Origination System
 */
public enum DocumentType {
    // Үндсэн баримт бичиг - Basic Documents
    NATIONAL_ID("Иргэний үнэмлэх", "National ID", true, Set.of("jpg", "jpeg", "png", "pdf")),
    PASSPORT("Гадаад паспорт", "Passport", false, Set.of("jpg", "jpeg", "png", "pdf")),
    DRIVER_LICENSE("Жолооны үнэмлэх", "Driver License", false, Set.of("jpg", "jpeg", "png", "pdf")),
    
    // Орлогын баримт - Income Documents
    INCOME_STATEMENT("Орлогын справка", "Income Statement", true, Set.of("pdf", "jpg", "jpeg", "png", "doc", "docx")),
    SALARY_CERTIFICATE("Цалингийн справка", "Salary Certificate", false, Set.of("pdf", "jpg", "jpeg", "png", "doc", "docx")),
    BANK_STATEMENT("Банкны хуулга", "Bank Statement", true, Set.of("pdf", "xls", "xlsx")),
    TAX_STATEMENT("Татварын тайлан", "Tax Statement", false, Set.of("pdf", "jpg", "jpeg", "png", "doc", "docx")),
    
    // Бизнесийн баримт - Business Documents
    BUSINESS_LICENSE("Бизнесийн лиценз", "Business License", false, Set.of("pdf", "jpg", "jpeg", "png")),
    BUSINESS_REGISTRATION("Бизнесийн бүртгэл", "Business Registration", false, Set.of("pdf", "jpg", "jpeg", "png")),
    COMPANY_CHARTER("Компанийн дүрэм", "Company Charter", false, Set.of("pdf", "doc", "docx")),
    FINANCIAL_STATEMENT("Санхүүгийн тайлан", "Financial Statement", false, Set.of("pdf", "xls", "xlsx")),
    
    // Зээлийн баримт - Loan Documents
    LOAN_APPLICATION("Зээлийн хүсэлт", "Loan Application", true, Set.of("pdf", "doc", "docx")),
    LOAN_AGREEMENT("Зээлийн гэрээ", "Loan Agreement", false, Set.of("pdf", "doc", "docx")),
    COLLATERAL_DOCUMENT("Барьцааны баримт", "Collateral Document", false, Set.of("pdf", "jpg", "jpeg", "png", "doc", "docx")),
    GUARANTOR_DOCUMENT("Батлан даагчийн баримт", "Guarantor Document", false, Set.of("pdf", "jpg", "jpeg", "png", "doc", "docx")),
    
    // Өмчийн баримт - Property Documents
    PROPERTY_CERTIFICATE("Өмчийн гэрчилгээ", "Property Certificate", false, Set.of("pdf", "jpg", "jpeg", "png")),
    VEHICLE_REGISTRATION("Тээврийн хэрэгслийн бүртгэл", "Vehicle Registration", false, Set.of("pdf", "jpg", "jpeg", "png")),
    LEASE_AGREEMENT("Түрээсийн гэрээ", "Lease Agreement", false, Set.of("pdf", "doc", "docx")),
    
    // Бусад баримт - Other Documents
    MEDICAL_CERTIFICATE("Эрүүл мэндийн справка", "Medical Certificate", false, Set.of("pdf", "jpg", "jpeg", "png")),
    EMPLOYMENT_CERTIFICATE("Ажил эрхлэлтийн справка", "Employment Certificate", false, Set.of("pdf", "doc", "docx")),
    REFERENCE_LETTER("Санал болгох захидал", "Reference Letter", false, Set.of("pdf", "doc", "docx")),
    OTHER("Бусад", "Other", false, Set.of("pdf", "doc", "docx", "jpg", "jpeg", "png", "xls", "xlsx"));

    private final String mongolianName;
    private final String englishName;
    private final boolean required;
    private final Set<String> allowedExtensions;

    DocumentType(String mongolianName, String englishName, boolean required, Set<String> allowedExtensions) {
        this.mongolianName = mongolianName;
        this.englishName = englishName;
        this.required = required;
        this.allowedExtensions = allowedExtensions;
    }

    public String getMongolianName() {
        return mongolianName;
    }

    public String getEnglishName() {
        return englishName;
    }

    public boolean isRequired() {
        return required;
    }

    public Set<String> getAllowedExtensions() {
        return allowedExtensions;
    }

    public boolean isExtensionAllowed(String extension) {
        if (extension == null) return false;
        return allowedExtensions.contains(extension.toLowerCase());
    }

    /**
     * Заавал шаардлагатай баримт бичгүүдийг буцаах
     */
    public static DocumentType[] getRequiredDocuments() {
        return Arrays.stream(values())
                .filter(DocumentType::isRequired)
                .toArray(DocumentType[]::new);
    }

    /**
     * Зээлийн төрөлөөр шаардлагатай баримт бичгүүдийг буцаах
     */
    public static DocumentType[] getDocumentsByLoanType(String loanType) {
        if (loanType == null) {
            return getRequiredDocuments();
        }
        
        switch (loanType.toUpperCase()) {
            case "BUSINESS":
                return new DocumentType[]{
                    NATIONAL_ID, INCOME_STATEMENT, BUSINESS_LICENSE, 
                    TAX_STATEMENT, BUSINESS_REGISTRATION, FINANCIAL_STATEMENT
                };
            case "PERSONAL":
                return new DocumentType[]{
                    NATIONAL_ID, INCOME_STATEMENT, BANK_STATEMENT, SALARY_CERTIFICATE
                };
            case "MORTGAGE":
                return new DocumentType[]{
                    NATIONAL_ID, INCOME_STATEMENT, BANK_STATEMENT, 
                    PROPERTY_CERTIFICATE, COLLATERAL_DOCUMENT
                };
            case "VEHICLE":
                return new DocumentType[]{
                    NATIONAL_ID, INCOME_STATEMENT, BANK_STATEMENT, 
                    VEHICLE_REGISTRATION, COLLATERAL_DOCUMENT
                };
            default:
                return getRequiredDocuments();
        }
    }

    /**
     * Зээлийн дүнгээр шаардлагатай баримт бичгүүдийг буцаах
     */
    public static List<DocumentType> getDocumentsByAmount(double amount) {
        List<DocumentType> documents = Arrays.asList(NATIONAL_ID, INCOME_STATEMENT, BANK_STATEMENT);
        
        if (amount > 10000000) { // 10 сая төгрөгөөс дээш
            documents = Arrays.asList(
                NATIONAL_ID, INCOME_STATEMENT, BANK_STATEMENT, 
                TAX_STATEMENT, GUARANTOR_DOCUMENT, COLLATERAL_DOCUMENT
            );
        } else if (amount > 5000000) { // 5 сая төгрөгөөс дээш
            documents = Arrays.asList(
                NATIONAL_ID, INCOME_STATEMENT, BANK_STATEMENT, GUARANTOR_DOCUMENT
            );
        }
        
        return documents;
    }

    @Override
    public String toString() {
        return mongolianName;
    }
}