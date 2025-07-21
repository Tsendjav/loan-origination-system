package com.company.los.enums;

/**
 * Зээлийн хүсэлтийн статус
 * Loan Application Status Enum
 */
public enum LoanStatus {
    // Шинэ хүсэлт
    DRAFT("Ноорог", "Draft application"),
    
    // Хүсэлт илгээсэн
    SUBMITTED("Илгээсэн", "Submitted for review"),
    
    // Документ шалгаж байна
    DOCUMENT_REVIEW("Документ шалгалт", "Document review in progress"),
    
    // КТС шалгаж байна
    CREDIT_CHECK("Зээлийн түүх шалгалт", "Credit history check"),
    
    // Эрсдэлийн үнэлгээ
    RISK_ASSESSMENT("Эрсдэлийн үнэлгээ", "Risk assessment"),
    
    // Менежер шалгаж байна
    MANAGER_REVIEW("Менежерийн шалгалт", "Manager review"),
    
    // Зөвшөөрсөн
    APPROVED("Зөвшөөрсөн", "Approved"),
    
    // Татгалзсан
    REJECTED("Татгалзсан", "Rejected"),
    
    // Зээл олгосон
    DISBURSED("Зээл олгосон", "Loan disbursed"),
    
    // Цуцалсан
    CANCELLED("Цуцалсан", "Cancelled"),
    
    // Нэмэлт мэдээлэл хэрэгтэй
    PENDING_INFO("Нэмэлт мэдээлэл", "Pending additional information"),
    
    // Хүлээлгэн өгсөн (гадаад систем)
    DELEGATED("Хүлээлгэн өгсөн", "Delegated to external system");

    private final String mongolianName;
    private final String englishDescription;

    LoanStatus(String mongolianName, String englishDescription) {
        this.mongolianName = mongolianName;
        this.englishDescription = englishDescription;
    }

    public String getMongolianName() {
        return mongolianName;
    }

    public String getEnglishDescription() {
        return englishDescription;
    }

    /**
     * Статусыг дарааллын дагуу эрэмбэлэх
     */
    public int getOrder() {
        switch (this) {
            case DRAFT: return 1;
            case SUBMITTED: return 2;
            case DOCUMENT_REVIEW: return 3;
            case CREDIT_CHECK: return 4;
            case RISK_ASSESSMENT: return 5;
            case MANAGER_REVIEW: return 6;
            case APPROVED: return 7;
            case DISBURSED: return 8;
            case REJECTED: return 9;
            case CANCELLED: return 10;
            case PENDING_INFO: return 11;
            case DELEGATED: return 12;
            default: return 0;
        }
    }

    /**
     * Төгссөн статус эсэхийг шалгах
     */
    public boolean isFinalStatus() {
        return this == APPROVED || 
               this == REJECTED || 
               this == DISBURSED || 
               this == CANCELLED;
    }

    /**
     * Идэвхтэй статус эсэхийг шалгах
     */
    public boolean isActiveStatus() {
        return this == SUBMITTED || 
               this == DOCUMENT_REVIEW || 
               this == CREDIT_CHECK || 
               this == RISK_ASSESSMENT || 
               this == MANAGER_REVIEW ||
               this == PENDING_INFO;
    }
}