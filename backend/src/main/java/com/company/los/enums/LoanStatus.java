package com.company.los.enums;

/**
 * Зээлийн хүсэлтийн статус enum
 * Loan Application Status Enumeration
 */
public enum LoanStatus {
    // Шинэ хүсэлт
    DRAFT("DRAFT", "Ноорог"),
    
    // Хүсэлт илгээсэн
    SUBMITTED("SUBMITTED", "Илгээсэн"),
    
    // Хүлээгдэж байна
    PENDING("PENDING", "Хүлээгдэж байгаа"),
    
    // Документ шалгаж байна
    DOCUMENT_REVIEW("DOCUMENT_REVIEW", "Баримт шалгаж байгаа"),
    
    // КТС шалгаж байна
    CREDIT_CHECK("CREDIT_CHECK", "Зээлийн түүх шалгаж байгаа"),
    
    // Эрсдэлийн үнэлгээ
    RISK_ASSESSMENT("RISK_ASSESSMENT", "Эрсдэл үнэлж байгаа"),
    
    // Менежер шалгаж байна
    MANAGER_REVIEW("MANAGER_REVIEW", "Менежер хянаж байгаа"),
    
    // Зөвшөөрсөн
    APPROVED("APPROVED", "Зөвшөөрсөн"),
    
    // Татгалзсан
    REJECTED("REJECTED", "Татгалзсан"),
    
    // Цуцалсан
    CANCELLED("CANCELLED", "Цуцалсан"),
    
    // Зээл олгосон
    DISBURSED("DISBURSED", "Олгосон"),
    
    // Нэмэлт мэдээлэл хэрэгтэй
    PENDING_INFO("PENDING_INFO", "Нэмэлт мэдээлэл"),
    
    // Хүлээлгэн өгсөн (гадаад систем)
    DELEGATED("DELEGATED", "Хүлээлгэн өгсөн");

    private final String code;
    private final String mongolianName;

    LoanStatus(String code, String mongolianName) {
        this.code = code;
        this.mongolianName = mongolianName;
    }

    public String getCode() { 
        return code; 
    }
    
    public String getMongolianName() { 
        return mongolianName; 
    }

    /**
     * Статусыг дарааллын дагуу эрэмбэлэх
     */
    public int getOrder() {
        switch (this) {
            case DRAFT: return 1;
            case SUBMITTED: return 2;
            case PENDING: return 3;
            case DOCUMENT_REVIEW: return 4;
            case CREDIT_CHECK: return 5;
            case RISK_ASSESSMENT: return 6;
            case MANAGER_REVIEW: return 7;
            case APPROVED: return 8;
            case DISBURSED: return 9;
            case REJECTED: return 10;
            case CANCELLED: return 11;
            case PENDING_INFO: return 12;
            case DELEGATED: return 13;
            default: return 0;
        }
    }

    /**
     * Идэвхтэй статус эсэхийг шалгах
     */
    public boolean isActiveStatus() {
        return this == SUBMITTED || this == PENDING || this == DOCUMENT_REVIEW || 
               this == CREDIT_CHECK || this == RISK_ASSESSMENT || this == MANAGER_REVIEW ||
               this == PENDING_INFO;
    }

    /**
     * Төгссөн статус эсэхийг шалгах
     */
    public boolean isFinalStatus() {
        return this == APPROVED || this == REJECTED || this == CANCELLED || this == DISBURSED;
    }

    /**
     * Код-оор статус олох
     */
    public static LoanStatus fromCode(String code) {
        for (LoanStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Алдаатай статусын код: " + code);
    }
}