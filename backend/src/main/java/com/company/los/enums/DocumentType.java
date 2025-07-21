package com.los.enums;

/**
 * Баримт бичгийн төрөл
 * Document Type Enum
 */
public enum DocumentType {
    // Иргэний бүртгэлийн үнэмлэх
    ID_CARD("Иргэний үнэмлэх", "National ID Card", "png,jpg,jpeg,pdf", true),
    
    // Гэрчилгээ
    PASSPORT("Гадаад паспорт", "Passport", "png,jpg,jpeg,pdf", true),
    
    // Орлогын баримт
    SALARY_CERTIFICATE("Цалингийн гэрчилгээ", "Salary Certificate", "pdf,doc,docx", true),
    
    // Банкны хуудас
    BANK_STATEMENT("Банкны хуудас", "Bank Statement", "pdf,xls,xlsx", true),
    
    // Ажлын байрны лавлагаа
    EMPLOYMENT_LETTER("Ажлын байрны лавлагаа", "Employment Letter", "pdf,doc,docx", true),
    
    // Гэрчилгээ баримт
    INCOME_TAX("Татварын баримт", "Income Tax Document", "pdf", false),
    
    // НДШБ-ны лавлагаа
    SOCIAL_INSURANCE("НДШБ лавлагаа", "Social Insurance Certificate", "pdf", false),
    
    // Гэр орны бүртгэлийн хуудас
    FAMILY_REGISTRATION("Гэр орны бүртгэл", "Family Registration", "png,jpg,jpeg,pdf", false),
    
    // Хөрөнгийн баримт
    ASSET_DOCUMENT("Хөрөнгийн баримт", "Asset Document", "pdf,png,jpg,jpeg", false),
    
    // Барьцааны баримт
    COLLATERAL_DOCUMENT("Барьцааны баримт", "Collateral Document", "pdf,doc,docx", false),
    
    // Хуулийн этгээдийн бүртгэлийн гэрчилгээ
    BUSINESS_REGISTRATION("ХЭ-ийн бүртгэлийн гэрчилгээ", "Business Registration Certificate", "pdf,png,jpg,jpeg", false),
    
    // Санхүүгийн тайлан
    FINANCIAL_STATEMENT("Санхүүгийн тайлан", "Financial Statement", "pdf,xls,xlsx", false),
    
    // Нэмэлт баримт
    ADDITIONAL_DOCUMENT("Нэмэлт баримт", "Additional Document", "pdf,doc,docx,png,jpg,jpeg", false),
    
    // Зээлийн гэрээ
    LOAN_AGREEMENT("Зээлийн гэрээ", "Loan Agreement", "pdf", false),
    
    // Батлан даалтын баримт
    GUARANTOR_DOCUMENT("Батлан даалтын баримт", "Guarantor Document", "pdf,doc,docx", false);

    private final String mongolianName;
    private final String englishName;
    private final String allowedExtensions;
    private final boolean required;

    DocumentType(String mongolianName, String englishName, String allowedExtensions, boolean required) {
        this.mongolianName = mongolianName;
        this.englishName = englishName;
        this.allowedExtensions = allowedExtensions;
        this.required = required;
    }

    public String getMongolianName() {
        return mongolianName;
    }

    public String getEnglishName() {
        return englishName;
    }

    public String getAllowedExtensions() {
        return allowedExtensions;
    }

    public boolean isRequired() {
        return required;
    }

    /**
     * Файлын өргөтгөл зөвшөөрөгдсөн эсэхийг шалгах
     */
    public boolean isExtensionAllowed(String extension) {
        if (extension == null || extension.trim().isEmpty()) {
            return false;
        }
        String cleanExtension = extension.toLowerCase().replace(".", "");
        return allowedExtensions.toLowerCase().contains(cleanExtension);
    }

    /**
     * Заавал шаардлагатай баримтуудыг буцаах
     */
    public static DocumentType[] getRequiredDocuments() {
        return java.util.Arrays.stream(values())
                .filter(DocumentType::isRequired)
                .toArray(DocumentType[]::new);
    }

    /**
     * Зээлийн төрлөөр шаардлагатай баримтуудыг буцаах
     */
    public static DocumentType[] getDocumentsByLoanType(String loanType) {
        switch (loanType.toUpperCase()) {
            case "CONSUMER":
                return new DocumentType[]{ID_CARD, SALARY_CERTIFICATE, BANK_STATEMENT, EMPLOYMENT_LETTER};
            case "BUSINESS":
                return new DocumentType[]{ID_CARD, BUSINESS_REGISTRATION, FINANCIAL_STATEMENT, BANK_STATEMENT};
            case "MORTGAGE":
                return new DocumentType[]{ID_CARD, SALARY_CERTIFICATE, BANK_STATEMENT, COLLATERAL_DOCUMENT, FAMILY_REGISTRATION};
            default:
                return getRequiredDocuments();
        }
    }
}