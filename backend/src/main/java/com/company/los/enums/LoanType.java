package com.company.los.enums;

/**
 * Зээлийн төрлийн Enum
 * Loan Type Enum
 */
public enum LoanType {
    PERSONAL("PERSONAL", "Хувийн зээл"),
    BUSINESS("BUSINESS", "Бизнесийн зээл"),
    MORTGAGE("MORTGAGE", "Орон сууцны зээл"),
    CAR_LOAN("CAR_LOAN", "Автомашины зээл"),
    CONSUMER("CONSUMER", "Хэрэглээний зээл"),
    EDUCATION("EDUCATION", "Боловсролын зээл"),
    MEDICAL("MEDICAL", "Эмнэлгийн зээл");

    private final String code;
    private final String mongolianName;

    LoanType(String code, String mongolianName) {
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
     * Кодоор LoanType-ийг олох
     * @param code Код
     * @return LoanType эсвэл null
     */
    public static LoanType fromCode(String code) {
        for (LoanType type : LoanType.values()) {
            if (type.getCode().equalsIgnoreCase(code)) {
                return type;
            }
        }
        return null;
    }
}