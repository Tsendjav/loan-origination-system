package com.company.los.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Зээлийн бүтээгдэхүүний Entity
 * Loan Product Entity
 */
@Entity
@Table(name = "loan_products", indexes = {
        @Index(name = "idx_loan_product_code", columnList = "product_code", unique = true),
        @Index(name = "idx_loan_product_name", columnList = "product_name"),
        @Index(name = "idx_loan_product_type", columnList = "loan_type")
})
@SQLDelete(sql = "UPDATE loan_products SET is_deleted = true WHERE id = ?")
@Where(clause = "is_deleted = false")
public class LoanProduct extends BaseEntity {

    @Column(name = "product_code", unique = true, nullable = false, length = 20)
    @NotBlank(message = "Бүтээгдэхүүний код заавал байх ёстой")
    @Size(max = 20, message = "Бүтээгдэхүүний код 20 тэмдэгтээс ихгүй байх ёстой")
    private String productCode;

    @Column(name = "product_name", nullable = false, length = 200)
    @NotBlank(message = "Бүтээгдэхүүний нэр заавал байх ёстой")
    @Size(max = 200, message = "Бүтээгдэхүүний нэр 200 тэмдэгтээс ихгүй байх ёстой")
    private String productName;

    @Column(name = "loan_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Зээлийн төрөл заавал сонгох ёстой")
    private LoanType loanType;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // Дүнгийн хязгаар
    @Column(name = "min_amount", nullable = false, precision = 15, scale = 2)
    @NotNull(message = "Хамгийн бага дүн заавал байх ёстой")
    @DecimalMin(value = "1000.0", message = "Хамгийн бага дүн 1,000-аас их байх ёстой")
    private BigDecimal minAmount;

    @Column(name = "max_amount", nullable = false, precision = 15, scale = 2)
    @NotNull(message = "Хамгийн их дүн заавал байх ёстой")
    @DecimalMin(value = "1000.0", message = "Хамгийн их дүн 1,000-аас их байх ёстой")
    private BigDecimal maxAmount;

    // Хугацааны хязгаар
    @Column(name = "min_term_months", nullable = false)
    @NotNull(message = "Хамгийн бага хугацаа заавал байх ёстой")
    @Min(value = 1, message = "Хамгийн бага хугацаа 1 сараас их байх ёстой")
    private Integer minTermMonths;

    @Column(name = "max_term_months", nullable = false)
    @NotNull(message = "Хамгийн их хугацаа заавал байх ёстой")
    @Min(value = 1, message = "Хамгийн их хугацаа 1 сараас их байх ёстой")
    private Integer maxTermMonths;

    // Хүүгийн мэдээлэл
    @Column(name = "min_interest_rate", precision = 5, scale = 4)
    @DecimalMin(value = "0.0", message = "Хүү сөрөг байж болохгүй")
    @DecimalMax(value = "1.0", message = "Хүү 100%-аас их байж болохгүй")
    private BigDecimal minInterestRate;

    @Column(name = "max_interest_rate", precision = 5, scale = 4)
    @DecimalMin(value = "0.0", message = "Хүү сөрөг байж болохгүй")
    @DecimalMax(value = "1.0", message = "Хүү 100%-аас их байж болохгүй")
    private BigDecimal maxInterestRate;

    @Column(name = "default_interest_rate", precision = 5, scale = 4)
    @DecimalMin(value = "0.0", message = "Хүү сөрөг байж болохгүй")
    @DecimalMax(value = "1.0", message = "Хүү 100%-аас их байж болохгүй")
    private BigDecimal defaultInterestRate;

    // Шаардлагууд
    @Column(name = "requires_collateral")
    private Boolean requiresCollateral = false;

    @Column(name = "requires_guarantor")
    private Boolean requiresGuarantor = false;

    @Column(name = "min_credit_score")
    @Min(value = 300, message = "Хамгийн бага кредит скор 300-аас бага байж болохгүй")
    @Max(value = 850, message = "Хамгийн бага кредит скор 850-аас их байж болохгүй")
    private Integer minCreditScore;

    @Column(name = "min_income", precision = 15, scale = 2)
    @DecimalMin(value = "0.0", message = "Хамгийн бага орлого сөрөг байж болохгүй")
    private BigDecimal minIncome;

    // Төлбөрийн мэдээлэл
    @Column(name = "processing_fee_rate", precision = 5, scale = 4)
    @DecimalMin(value = "0.0", message = "Шимтгэл сөрөг байж болохгүй")
    private BigDecimal processingFeeRate;

    @Column(name = "early_payment_penalty_rate", precision = 5, scale = 4)
    @DecimalMin(value = "0.0", message = "Торгууль сөрөг байж болохгүй")
    private BigDecimal earlyPaymentPenaltyRate;

    @Column(name = "late_payment_penalty_rate", precision = 5, scale = 4)
    @DecimalMin(value = "0.0", message = "Хоцрогдлын торгууль сөрөг байж болохгүй")
    private BigDecimal latePaymentPenaltyRate;

    // Менежментийн мэдээлэл
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "approval_required")
    private Boolean approvalRequired = true;

    @Column(name = "auto_approval_limit", precision = 15, scale = 2)
    @DecimalMin(value = "0.0", message = "Автомат зөвшөөрлийн хязгаар сөрөг байж болохгүй")
    private BigDecimal autoApprovalLimit;

    // Зээлийн хүсэлтүүд
    @OneToMany(mappedBy = "loanProduct", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LoanApplication> loanApplications = new ArrayList<>();

    // Зээлийн төрөл enum (inner enum)
    public enum LoanType {
        PERSONAL("PERSONAL", "Хувийн зээл"),
        BUSINESS("BUSINESS", "Бизнесийн зээл"),
        MORTGAGE("MORTGAGE", "Орон сууцны зээл"),
        CAR("CAR", "Автомашины зээл"),
        EDUCATION("EDUCATION", "Боловсролын зээл"),
        MEDICAL("MEDICAL", "Эрүүл мэндийн зээл");

        private final String code;
        private final String mongolianName;

        LoanType(String code, String mongolianName) {
            this.code = code;
            this.mongolianName = mongolianName;
        }

        public String getCode() { return code; }
        public String getMongolianName() { return mongolianName; }
    }

    // Constructors
    public LoanProduct() {
        super();
    }

    public LoanProduct(String productCode, String productName, LoanType loanType,
                      BigDecimal minAmount, BigDecimal maxAmount, Integer minTermMonths, Integer maxTermMonths) {
        this();
        this.productCode = productCode;
        this.productName = productName;
        this.loanType = loanType;
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
        this.minTermMonths = minTermMonths;
        this.maxTermMonths = maxTermMonths;
    }

    // Business methods
    public boolean isAmountWithinLimits(BigDecimal amount) {
        return amount != null && 
               amount.compareTo(minAmount) >= 0 && 
               amount.compareTo(maxAmount) <= 0;
    }

    public boolean isTermWithinLimits(Integer termMonths) {
        return termMonths != null && 
               termMonths >= minTermMonths && 
               termMonths <= maxTermMonths;
    }

    public boolean isEligibleForAutoApproval(BigDecimal amount) {
        return autoApprovalLimit != null && 
               amount != null && 
               amount.compareTo(autoApprovalLimit) <= 0;
    }

    public BigDecimal calculateProcessingFee(BigDecimal loanAmount) {
        if (processingFeeRate == null || loanAmount == null) {
            return BigDecimal.ZERO;
        }
        return loanAmount.multiply(processingFeeRate);
    }

    public BigDecimal calculateMonthlyPayment(BigDecimal loanAmount, Integer termMonths, BigDecimal interestRate) {
        if (!isAmountWithinLimits(loanAmount) || !isTermWithinLimits(termMonths)) {
            throw new IllegalArgumentException("Зээлийн дүн эсвэл хугацаа буруу байна");
        }

        BigDecimal rate = interestRate != null ? interestRate : defaultInterestRate;
        if (rate == null || rate.compareTo(BigDecimal.ZERO) == 0) {
            // Хүүгүй зээл
            return loanAmount.divide(BigDecimal.valueOf(termMonths), 2, BigDecimal.ROUND_HALF_UP);
        }

        // Compound interest calculation
        BigDecimal monthlyRate = rate.divide(BigDecimal.valueOf(12), 6, BigDecimal.ROUND_HALF_UP);
        BigDecimal onePlusRate = BigDecimal.ONE.add(monthlyRate);
        BigDecimal numerator = loanAmount.multiply(monthlyRate);
        BigDecimal denominator = BigDecimal.ONE.subtract(
            BigDecimal.ONE.divide(onePlusRate.pow(termMonths), 6, BigDecimal.ROUND_HALF_UP)
        );
        
        return numerator.divide(denominator, 2, BigDecimal.ROUND_HALF_UP);
    }

    public void enable() {
        this.isActive = true;
        this.setUpdatedAt(java.time.LocalDateTime.now());
    }

    public void disable() {
        this.isActive = false;
        this.setUpdatedAt(java.time.LocalDateTime.now());
    }

    public String getDisplayName() {
        return productName + " (" + productCode + ")";
    }

    public String getLoanTypeDisplay() {
        return loanType != null ? loanType.getMongolianName() : "";
    }

    public String getAmountRange() {
        return String.format("%.0f - %.0f", minAmount, maxAmount);
    }

    public String getTermRange() {
        return String.format("%d - %d сар", minTermMonths, maxTermMonths);
    }

    public String getInterestRateRange() {
        if (minInterestRate != null && maxInterestRate != null) {
            return String.format("%.2f%% - %.2f%%", 
                minInterestRate.multiply(BigDecimal.valueOf(100)), 
                maxInterestRate.multiply(BigDecimal.valueOf(100)));
        } else if (defaultInterestRate != null) {
            return String.format("%.2f%%", defaultInterestRate.multiply(BigDecimal.valueOf(100)));
        }
        return "Тодорхойгүй";
    }

    // Getters and Setters
    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public LoanType getLoanType() { return loanType; }
    public void setLoanType(LoanType loanType) { this.loanType = loanType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getMinAmount() { return minAmount; }
    public void setMinAmount(BigDecimal minAmount) { this.minAmount = minAmount; }

    public BigDecimal getMaxAmount() { return maxAmount; }
    public void setMaxAmount(BigDecimal maxAmount) { this.maxAmount = maxAmount; }

    public Integer getMinTermMonths() { return minTermMonths; }
    public void setMinTermMonths(Integer minTermMonths) { this.minTermMonths = minTermMonths; }

    public Integer getMaxTermMonths() { return maxTermMonths; }
    public void setMaxTermMonths(Integer maxTermMonths) { this.maxTermMonths = maxTermMonths; }

    public BigDecimal getMinInterestRate() { return minInterestRate; }
    public void setMinInterestRate(BigDecimal minInterestRate) { this.minInterestRate = minInterestRate; }

    public BigDecimal getMaxInterestRate() { return maxInterestRate; }
    public void setMaxInterestRate(BigDecimal maxInterestRate) { this.maxInterestRate = maxInterestRate; }

    public BigDecimal getDefaultInterestRate() { return defaultInterestRate; }
    public void setDefaultInterestRate(BigDecimal defaultInterestRate) { this.defaultInterestRate = defaultInterestRate; }

    public Boolean getRequiresCollateral() { return requiresCollateral; }
    public void setRequiresCollateral(Boolean requiresCollateral) { this.requiresCollateral = requiresCollateral; }

    public Boolean getRequiresGuarantor() { return requiresGuarantor; }
    public void setRequiresGuarantor(Boolean requiresGuarantor) { this.requiresGuarantor = requiresGuarantor; }

    public Integer getMinCreditScore() { return minCreditScore; }
    public void setMinCreditScore(Integer minCreditScore) { this.minCreditScore = minCreditScore; }

    public BigDecimal getMinIncome() { return minIncome; }
    public void setMinIncome(BigDecimal minIncome) { this.minIncome = minIncome; }

    public BigDecimal getProcessingFeeRate() { return processingFeeRate; }
    public void setProcessingFeeRate(BigDecimal processingFeeRate) { this.processingFeeRate = processingFeeRate; }

    public BigDecimal getEarlyPaymentPenaltyRate() { return earlyPaymentPenaltyRate; }
    public void setEarlyPaymentPenaltyRate(BigDecimal earlyPaymentPenaltyRate) { this.earlyPaymentPenaltyRate = earlyPaymentPenaltyRate; }

    public BigDecimal getLatePaymentPenaltyRate() { return latePaymentPenaltyRate; }
    public void setLatePaymentPenaltyRate(BigDecimal latePaymentPenaltyRate) { this.latePaymentPenaltyRate = latePaymentPenaltyRate; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public Boolean getApprovalRequired() { return approvalRequired; }
    public void setApprovalRequired(Boolean approvalRequired) { this.approvalRequired = approvalRequired; }

    public BigDecimal getAutoApprovalLimit() { return autoApprovalLimit; }
    public void setAutoApprovalLimit(BigDecimal autoApprovalLimit) { this.autoApprovalLimit = autoApprovalLimit; }

    public List<LoanApplication> getLoanApplications() { return loanApplications; }
    public void setLoanApplications(List<LoanApplication> loanApplications) { this.loanApplications = loanApplications; }

    // toString
    @Override
    public String toString() {
        return "LoanProduct{" +
                "id=" + getId() +
                ", productCode='" + productCode + '\'' +
                ", productName='" + productName + '\'' +
                ", loanType=" + loanType +
                ", minAmount=" + minAmount +
                ", maxAmount=" + maxAmount +
                ", isActive=" + isActive +
                '}';
    }
}