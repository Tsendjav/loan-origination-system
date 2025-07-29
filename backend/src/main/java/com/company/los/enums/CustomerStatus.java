package com.company.los.enums;

/**
 * Харилцагчийн статус enum
 * Customer Status Enumeration
 * 
 * @author LOS Development Team
 */
public enum CustomerStatus {
    /**
     * Шалгагдах хүлээлттэй
     */
    PENDING_VERIFICATION("Шалгагдах хүлээлттэй"),
    
    /**
     * Идэвхтэй
     */
    ACTIVE("Идэвхтэй"),
    
    /**
     * Идэвхгүй
     */
    INACTIVE("Идэвхгүй"),
    
    /**
     * Түр зогсоосон
     */
    SUSPENDED("Түр зогсоосон"),
    
    /**
     * Блоклогдсон
     */
    BLOCKED("Блоклогдсон"),
    
    /**
     * Архивлагдсан
     */
    ARCHIVED("Архивлагдсан"),
    
    /**
     * Устгагдсан
     */
    DELETED("Устгагдсан");

    private final String mongolianName;

    CustomerStatus(String mongolianName) {
        this.mongolianName = mongolianName;
    }

    public String getMongolianName() {
        return mongolianName;
    }

    /**
     * Идэвхтэй статус эсэх
     */
    public boolean isActive() {
        return this == ACTIVE;
    }

    /**
     * Блоклогдсон статус эсэх
     */
    public boolean isBlocked() {
        return this == BLOCKED || this == SUSPENDED;
    }

    /**
     * Устгагдсан статус эсэх
     */
    public boolean isDeleted() {
        return this == DELETED || this == ARCHIVED;
    }

    /**
     * Үйлдэл хийх боломжтой эсэх
     */
    public boolean canPerformActions() {
        return this == ACTIVE || this == PENDING_VERIFICATION;
    }

    /**
     * Зээл авах боломжтой эсэх
     */
    public boolean canApplyForLoan() {
        return this == ACTIVE;
    }

    /**
     * Display badge class for UI
     */
    public String getBadgeClass() {
        switch (this) {
            case PENDING_VERIFICATION:
                return "badge-warning";
            case ACTIVE:
                return "badge-success";
            case INACTIVE:
                return "badge-secondary";
            case SUSPENDED:
                return "badge-warning";
            case BLOCKED:
                return "badge-danger";
            case ARCHIVED:
                return "badge-info";
            case DELETED:
                return "badge-dark";
            default:
                return "badge-secondary";
        }
    }

    /**
     * Status icon for UI
     */
    public String getIcon() {
        switch (this) {
            case PENDING_VERIFICATION:
                return "clock";
            case ACTIVE:
                return "check-circle";
            case INACTIVE:
                return "pause-circle";
            case SUSPENDED:
                return "alert-triangle";
            case BLOCKED:
                return "x-circle";
            case ARCHIVED:
                return "archive";
            case DELETED:
                return "trash-2";
            default:
                return "help-circle";
        }
    }

    /**
     * Статусын тайлбар
     */
    public String getDescription() {
        switch (this) {
            case PENDING_VERIFICATION:
                return "Харилцагчийн мэдээлэл шалгагдах хүлээлттэй байна";
            case ACTIVE:
                return "Харилцагч идэвхтэй байгаа бөгөөд бүх үйлчилгээ ашиглах боломжтой";
            case INACTIVE:
                return "Харилцагч түр хугацаанд идэвхгүй байна";
            case SUSPENDED:
                return "Харилцагчийн эрх түр хугацаанд хязгаарлагдсан";
            case BLOCKED:
                return "Харилцагчийн эрх бүрмөсөн хязгаарлагдсан";
            case ARCHIVED:
                return "Харилцагчийн мэдээлэл архивлагдсан";
            case DELETED:
                return "Харилцагчийн мэдээлэл устгагдсан";
            default:
                return "Тодорхойгүй статус";
        }
    }

    /**
     * Дараагийн боломжит статусууд
     */
    public CustomerStatus[] getNextPossibleStatuses() {
        switch (this) {
            case PENDING_VERIFICATION:
                return new CustomerStatus[]{ACTIVE, BLOCKED, DELETED};
            case ACTIVE:
                return new CustomerStatus[]{INACTIVE, SUSPENDED, BLOCKED, ARCHIVED};
            case INACTIVE:
                return new CustomerStatus[]{ACTIVE, SUSPENDED, ARCHIVED};
            case SUSPENDED:
                return new CustomerStatus[]{ACTIVE, BLOCKED, ARCHIVED};
            case BLOCKED:
                return new CustomerStatus[]{SUSPENDED, ARCHIVED};
            case ARCHIVED:
                return new CustomerStatus[]{DELETED};
            case DELETED:
                return new CustomerStatus[]{}; // No transitions from deleted
            default:
                return new CustomerStatus[]{};
        }
    }

    /**
     * Тодорхой статус руу шилжих боломжтой эсэх
     */
    public boolean canTransitionTo(CustomerStatus newStatus) {
        CustomerStatus[] possibleStatuses = getNextPossibleStatuses();
        for (CustomerStatus status : possibleStatuses) {
            if (status == newStatus) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return mongolianName;
    }
}