package com.company.los.enums;

/**
 * Харилцагчийн төрөл enum (External API interface)
 * Customer Type Enumeration for external API
 * 
 * @author LOS Development Team
 */
public enum CustomerType {
    /**
     * Хувь хүн
     */
    INDIVIDUAL("Хувь хүн"),
    
    /**
     * Байгууллага
     */
    BUSINESS("Байгууллага");

    private final String mongolianName;

    CustomerType(String mongolianName) {
        this.mongolianName = mongolianName;
    }

    public String getMongolianName() {
        return mongolianName;
    }

    /**
     * Хувь хүн эсэх
     */
    public boolean isIndividual() {
        return this == INDIVIDUAL;
    }

    /**
     * Байгууллага эсэх
     */
    public boolean isBusiness() {
        return this == BUSINESS;
    }

    /**
     * Display badge class for UI
     */
    public String getBadgeClass() {
        switch (this) {
            case INDIVIDUAL:
                return "badge-primary";
            case BUSINESS:
                return "badge-info";
            default:
                return "badge-secondary";
        }
    }

    /**
     * Icon for UI
     */
    public String getIcon() {
        switch (this) {
            case INDIVIDUAL:
                return "user";
            case BUSINESS:
                return "building";
            default:
                return "help-circle";
        }
    }

    /**
     * Internal Customer.CustomerType руу хөрвүүлэх
     */
    public com.company.los.entity.Customer.CustomerType toInternalType() {
        switch (this) {
            case INDIVIDUAL:
                return com.company.los.entity.Customer.CustomerType.INDIVIDUAL;
            case BUSINESS:
                return com.company.los.entity.Customer.CustomerType.BUSINESS;
            default:
                throw new IllegalArgumentException("Unknown CustomerType: " + this);
        }
    }

    /**
     * Internal Customer.CustomerType-аас хөрвүүлэх
     */
    public static CustomerType fromInternalType(com.company.los.entity.Customer.CustomerType internalType) {
        switch (internalType) {
            case INDIVIDUAL:
                return INDIVIDUAL;
            case BUSINESS:
                return BUSINESS;
            default:
                throw new IllegalArgumentException("Unknown internal CustomerType: " + internalType);
        }
    }

    @Override
    public String toString() {
        return mongolianName;
    }
}