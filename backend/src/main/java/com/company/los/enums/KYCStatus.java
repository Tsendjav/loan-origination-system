package com.company.los.enums;

/**
 * KYC Status enum (External API interface)
 * Харилцагчийн KYC шалгалтын төлөв
 * 
 * @author LOS Development Team
 */
public enum KYCStatus {
    /**
     * Хүлээгдэж байна
     */
    PENDING("Хүлээгдэж байна"),
    
    /**
     * Шалгагдаж байна
     */
    IN_PROGRESS("Шалгагдаж байна"),
    
    /**
     * Дууссан
     */
    COMPLETED("Дууссан"),
    
    /**
     * Татгалзсан
     */
    REJECTED("Татгалзсан"),
    
    /**
     * Амжилтгүй
     */
    FAILED("Амжилтгүй");

    private final String mongolianName;

    KYCStatus(String mongolianName) {
        this.mongolianName = mongolianName;
    }

    public String getMongolianName() {
        return mongolianName;
    }

    public boolean isCompleted() {
        return this == COMPLETED;
    }

    public boolean isPending() {
        return this == PENDING;
    }

    public boolean isInProgress() {
        return this == IN_PROGRESS;
    }

    public boolean isRejected() {
        return this == REJECTED;
    }

    public boolean isFailed() {
        return this == FAILED;
    }

    /**
     * Active status эсэх (Completed биш бол KYC ажиллагаа хэрэгтэй)
     */
    public boolean requiresAction() {
        return this != COMPLETED;
    }

    /**
     * Display badge class for UI
     */
    public String getBadgeClass() {
        switch (this) {
            case PENDING:
                return "badge-warning";
            case IN_PROGRESS:
                return "badge-info";
            case COMPLETED:
                return "badge-success";
            case REJECTED:
            case FAILED:
                return "badge-danger";
            default:
                return "badge-secondary";
        }
    }

    @Override
    public String toString() {
        return mongolianName;
    }
}