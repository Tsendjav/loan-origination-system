package com.company.los.enums;

/**
 * Зээлийн хүсэлт болон зээлийн статусын enum
 * * @author Tsendjav
 * @version 1.0
 * @since 2025-01-01
 */
public enum LoanStatus {
    
    // Хүсэлтийн статусууд
    DRAFT("Ноорог", "Хүсэлт бэлтгэгдэж байна", "draft"),
    SUBMITTED("Илгээгдсэн", "Хүсэлт илгээгдсэн", "submitted"),
    PENDING("Хүлээгдэж байна", "Хүсэлт хүлээгдэж байна", "pending"),
    PENDING_DOCUMENTS("Нэмэлт баримт бичиг шаардлагатай", "Нэмэлт баримт бичиг эсвэл мэдээлэл шаардлагатай", "pending_documents"), // Added this enum constant
    
    // Шалгалтын статусууд
    UNDER_REVIEW("Шалгагдаж байна", "Хүсэлт шалгагдаж байна", "under_review"),
    ADDITIONAL_INFO_REQUIRED("Нэмэлт мэдээлэл шаардлагатай", "Нэмэлт баримт бичиг эсвэл мэдээлэл шаардлагатай", "additional_info_required"),
    CREDIT_CHECK("Зээлийн түүх шалгагдаж байна", "Харилцагчийн зээлийн түүх шалгагдаж байна", "credit_check"),
    VERIFICATION("Баталгаажуулалт", "Мэдээлэл баталгаажуулагдаж байна", "verification"),
    
    // Шийдвэрийн статусууд
    APPROVED("Зөвшөөрөгдсөн", "Зээлийн хүсэлт зөвшөөрөгдсөн", "approved"),
    CONDITIONALLY_APPROVED("Нөхцөлтэй зөвшөөрөгдсөн", "Тодорхой нөхцөлтэйгөөр зөвшөөрөгдсөн", "conditionally_approved"),
    REJECTED("Татгалзсан", "Зээлийн хүсэлт татгалзагдсан", "rejected"),
    
    // Гэрээний статусууд
    CONTRACT_PREPARATION("Гэрээ бэлтгэгдэж байна", "Зээлийн гэрээ бэлтгэгдэж байна", "contract_preparation"),
    CONTRACT_SIGNED("Гэрээ гарын үсэг зурсан", "Зээлийн гэрээнд гарын үсэг зурсан", "contract_signed"),
    
    // Олголтын статусууд
    DISBURSEMENT_APPROVED("Олголт зөвшөөрөгдсөн", "Зээлийн олголт зөвшөөрөгдсөн", "disbursement_approved"),
    DISBURSED("Олгогдсон", "Зээл олгогдсон", "disbursed"),
    
    // Идэвхтэй зээлийн статусууд
    ACTIVE("Идэвхтэй", "Зээл идэвхтэй төлөгдөж байна", "active"),
    OVERDUE("Хугацаа хэтэрсэн", "Зээлийн төлбөр хугацаа хэтэрсэн", "overdue"),
    DELINQUENT("Хоцрогдсон", "Зээлийн төлбөр хоцрогдсон", "delinquent"),
    
    // Дуусгавар статусууд
    PAID_OFF("Төлөгдсөн", "Зээл бүрэн төлөгдсөн", "paid_off"),
    CLOSED("Хаагдсан", "Зээлийн данс хаагдсан", "closed"),
    CHARGED_OFF("Муу зээл", "Зээл муу зээл болгон хаагдсан", "charged_off"),
    
    // Цуцлагдсан статусууд
    CANCELLED("Цуцлагдсан", "Хүсэлт цуцлагдсан", "cancelled"),
    WITHDRAWN("Татан авсан", "Харилцагч хүсэлтээ татан авсан", "withdrawn"),
    EXPIRED("Хугацаа дууссан", "Хүсэлтийн хугацаа дууссан", "expired");

    private final String mongolianName;
    private final String description;
    private final String code;

    LoanStatus(String mongolianName, String description, String code) {
        this.mongolianName = mongolianName;
        this.description = description;
        this.code = code;
    }

    public String getMongolianName() {
        return mongolianName;
    }

    public String getDescription() {
        return description;
    }

    public String getCode() {
        return code;
    }

    /**
     * Code-оор статус олох
     */
    public static LoanStatus fromCode(String code) {
        for (LoanStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Алдаатай статусын код: " + code);
    }

    /**
     * Статус шинэчлэх боломжтой эсэхийг шалгах
     */
    public boolean canTransitionTo(LoanStatus newStatus) {
        return switch (this) {
            case DRAFT -> newStatus == SUBMITTED || newStatus == CANCELLED;
            
            case SUBMITTED -> newStatus == PENDING || newStatus == WITHDRAWN || newStatus == CANCELLED || newStatus == UNDER_REVIEW || newStatus == ADDITIONAL_INFO_REQUIRED; // Added transitions
            
            case PENDING -> newStatus == UNDER_REVIEW || newStatus == ADDITIONAL_INFO_REQUIRED || 
                           newStatus == REJECTED || newStatus == EXPIRED;
            
            case UNDER_REVIEW -> newStatus == CREDIT_CHECK || newStatus == VERIFICATION || 
                                newStatus == APPROVED || newStatus == CONDITIONALLY_APPROVED || 
                                newStatus == REJECTED || newStatus == ADDITIONAL_INFO_REQUIRED;
            
            case ADDITIONAL_INFO_REQUIRED -> newStatus == UNDER_REVIEW || newStatus == REJECTED || 
                                            newStatus == EXPIRED || newStatus == PENDING_DOCUMENTS; // Added PENDING_DOCUMENTS
            
            case CREDIT_CHECK -> newStatus == VERIFICATION || newStatus == APPROVED || 
                                newStatus == CONDITIONALLY_APPROVED || newStatus == REJECTED;
            
            case VERIFICATION -> newStatus == APPROVED || newStatus == CONDITIONALLY_APPROVED || 
                                newStatus == REJECTED || newStatus == ADDITIONAL_INFO_REQUIRED;
            
            case APPROVED -> newStatus == CONTRACT_PREPARATION || newStatus == EXPIRED || newStatus == DISBURSED; // Added DISBURSED
            
            case CONDITIONALLY_APPROVED -> newStatus == CONTRACT_PREPARATION || newStatus == REJECTED || 
                                          newStatus == EXPIRED;
            
            case CONTRACT_PREPARATION -> newStatus == CONTRACT_SIGNED || newStatus == CANCELLED;
            
            case CONTRACT_SIGNED -> newStatus == DISBURSEMENT_APPROVED || newStatus == CANCELLED;
            
            case DISBURSEMENT_APPROVED -> newStatus == DISBURSED || newStatus == CANCELLED;
            
            case DISBURSED -> newStatus == ACTIVE;
            
            case ACTIVE -> newStatus == OVERDUE || newStatus == PAID_OFF || newStatus == CHARGED_OFF;
            
            case OVERDUE -> newStatus == ACTIVE || newStatus == DELINQUENT || newStatus == PAID_OFF || 
                           newStatus == CHARGED_OFF;
            
            case DELINQUENT -> newStatus == ACTIVE || newStatus == PAID_OFF || newStatus == CHARGED_OFF;
            
            case PAID_OFF -> newStatus == CLOSED;
            
            // Эцсийн статусууд - өөрчлөх боломжгүй
            case REJECTED, CANCELLED, WITHDRAWN, EXPIRED, CLOSED, CHARGED_OFF -> false;
            
            default -> false;
        };
    }

    /**
     * Идэвхтэй зээл эсэхийг шалгах
     */
    public boolean isActive() {
        return this == ACTIVE || this == OVERDUE || this == DELINQUENT;
    }

    /**
     * Дууссан статус эсэхийг шалгах
     */
    public boolean isFinal() {
        return this == PAID_OFF || this == CLOSED || this == CHARGED_OFF || 
               this == REJECTED || this == CANCELLED || this == WITHDRAWN || this == EXPIRED;
    }

    /**
     * Хүсэлтийн статус эсэхийг шалгах
     */
    public boolean isApplicationStatus() {
        return this == DRAFT || this == SUBMITTED || this == PENDING || this == UNDER_REVIEW ||
               this == ADDITIONAL_INFO_REQUIRED || this == CREDIT_CHECK || this == VERIFICATION ||
               this == APPROVED || this == CONDITIONALLY_APPROVED || this == REJECTED ||
               this == CANCELLED || this == WITHDRAWN || this == EXPIRED || this == PENDING_DOCUMENTS; // Added PENDING_DOCUMENTS
    }

    /**
     * Олгогдсон зээлийн статус эсэхийг шалгах
     */
    public boolean isLoanStatus() {
        return this == DISBURSED || this == ACTIVE || this == OVERDUE || this == DELINQUENT ||
               this == PAID_OFF || this == CLOSED || this == CHARGED_OFF;
    }

    /**
     * Статусын түвшинг тодорхойлох (1-10)
     */
    public int getLevel() {
        return switch (this) {
            case DRAFT -> 1;
            case SUBMITTED -> 2;
            case PENDING -> 3;
            case UNDER_REVIEW, ADDITIONAL_INFO_REQUIRED, CREDIT_CHECK, VERIFICATION, PENDING_DOCUMENTS -> 4; // Added PENDING_DOCUMENTS
            case APPROVED, CONDITIONALLY_APPROVED -> 5;
            case CONTRACT_PREPARATION -> 6;
            case CONTRACT_SIGNED -> 7;
            case DISBURSEMENT_APPROVED -> 8;
            case DISBURSED, ACTIVE -> 9;
            case PAID_OFF, CLOSED -> 10;
            case REJECTED, CANCELLED, WITHDRAWN, EXPIRED -> -1;
            case OVERDUE, DELINQUENT, CHARGED_OFF -> 0;
        };
    }

    /**
     * Статусын өнгө (UI-д ашиглах)
     */
    public String getColor() {
        return switch (this) {
            case DRAFT, SUBMITTED, PENDING, PENDING_DOCUMENTS -> "orange"; // Added PENDING_DOCUMENTS
            case UNDER_REVIEW, ADDITIONAL_INFO_REQUIRED, CREDIT_CHECK, VERIFICATION, 
                 CONTRACT_PREPARATION, DISBURSEMENT_APPROVED -> "blue";
            case APPROVED, CONDITIONALLY_APPROVED, CONTRACT_SIGNED, DISBURSED, ACTIVE, PAID_OFF, CLOSED -> "green";
            case OVERDUE, DELINQUENT -> "yellow";
            case REJECTED, CANCELLED, WITHDRAWN, EXPIRED, CHARGED_OFF -> "red";
        };
    }

    @Override
    public String toString() {
        return mongolianName;
    }
}