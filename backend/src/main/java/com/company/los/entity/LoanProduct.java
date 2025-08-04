package com.company.los.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "loan_products", indexes = {
        @Index(name = "idx_loan_product_name", columnList = "name"),
        @Index(name = "idx_loan_product_type", columnList = "loan_type")
})
@SQLDelete(sql = "UPDATE loan_products SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
public class LoanProduct extends BaseEntity {

    @Column(name = "name", nullable = false, length = 200)
    @NotBlank(message = "Бүтээгдэхүүний нэр заавал байх ёстой")
    @Size(max = 200, message = "Бүтээгдэхүүний нэр 200 тэмдэгтээс ихгүй байх ёстой")
    private String name;

    @Column(name = "product_name", length = 200)
    @Size(max = 200, message = "Бүтээгдэхүүний нэр 200 тэмдэгтээс ихгүй байх ёстой")
    private String productName;

    @Column(name = "loan_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Зээлийн төрөл заавал сонгох ёстой")
    private LoanType loanType;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "min_amount", nullable = false, precision = 18, scale = 2)
    @NotNull(message = "Хамгийн бага дүн заавал байх ёстой")
    @DecimalMin(value = "1000.0", message = "Хамгийн бага дүн 1,000-аас их байх ёстой")
    private BigDecimal minAmount;

    @Column(name = "max_amount", nullable = false, precision = 18, scale = 2)
    @NotNull(message = "Хамгийн их дүн заавал байх ёстой")
    @DecimalMin(value = "1000.0", message = "Хамгийн их дүн 1,000-аас их байх ёстой")
    private BigDecimal maxAmount;

    @Column(name = "min_term_months", nullable = false)
    @NotNull(message = "Хамгийн бага хугацаа заавал байх ёстой")
    @Min(value = 1, message = "Хамгийн бага хугацаа 1 сараас их байх ёстой")
    private Integer minTermMonths;

    @Column(name = "max_term_months", nullable = false)
    @NotNull(message = "Хамгийн их хугацаа заавал байх ёстой")
    @Min(value = 1, message = "Хамгийн их хугацаа 1 сараас их байх ёстой")
    private Integer maxTermMonths;

    @Column(name = "base_rate", precision = 7, scale = 4)
    @DecimalMin(value = "0.0", message = "Хүү сөрөг байж болохгүй")
    @DecimalMax(value = "1.0", message = "Хүү 100%-аас их байж болохгүй")
    private BigDecimal baseRate;

    @Column(name = "min_interest_rate", precision = 7, scale = 4)
    @DecimalMin(value = "0.0", message = "Хүү сөрөг байж болохгүй")
    @DecimalMax(value = "1.0", message = "Хүү 100%-аас их байж болохгүй")
    private BigDecimal minInterestRate;

    @Column(name = "max_interest_rate", precision = 7, scale = 4)
    @DecimalMin(value = "0.0", message = "Хүү сөрөг байж болохгүй")
    @DecimalMax(value = "1.0", message = "Хүү 100%-аас их байж болохгүй")
    private BigDecimal maxInterestRate;

    @Column(name = "default_interest_rate", precision = 7, scale = 4)
    @DecimalMin(value = "0.0", message = "Хүү сөрөг байж болохгүй")
    @DecimalMax(value = "1.0", message = "Хүү 100%-аас их байж болохгүй")
    private BigDecimal defaultInterestRate;

    @Column(name = "auto_approval_limit", precision = 18, scale = 2)
    @DecimalMin(value = "0.0", message = "Автомат зөвшөөрөлийн хязгаар сөрөг байж болохгүй")
    private BigDecimal autoApprovalLimit;

    @Column(name = "approval_required")
    private Boolean approvalRequired = true;

    @Column(name = "requires_collateral")
    private Boolean requiresCollateral = false;

    @Column(name = "requires_guarantor")
    private Boolean requiresGuarantor = false;

    @Column(name = "min_credit_score")
    @Min(value = 300, message = "Хамгийн бага кредит скор 300-аас бага байж болохгүй")
    @Max(value = 850, message = "Хамгийн бага кредит скор 850-аас их байж болохгүй")
    private Integer minCreditScore;

    @Column(name = "max_debt_ratio", precision = 7, scale = 4)
    @DecimalMin(value = "0.0", message = "Хамгийн их өрийн харьцаа сөрөг байж болохгүй")
    @DecimalMax(value = "1.0", message = "Хамгийн их өрийн харьцаа 100%-аас их байж болохгүй")
    private BigDecimal maxDebtRatio;

    @Column(name = "min_income", precision = 15, scale = 2)
    @DecimalMin(value = "0.0", message = "Хамгийн бага орлого сөрөг байж болохгүй")
    private BigDecimal minIncome;

    @Column(name = "processing_fee", precision = 15, scale = 2)
    @DecimalMin(value = "0.0", message = "Боловсруулалтын шимтгэл сөрөг байж болохгүй")
    private BigDecimal processingFee;

    @Column(name = "processing_fee_rate", precision = 7, scale = 4)
    @DecimalMin(value = "0.0", message = "Боловсруулалтын шимтгэлийн хувь сөрөг байж болохгүй")
    private BigDecimal processingFeeRate;

    @Column(name = "early_payment_penalty", precision = 15, scale = 2)
    @DecimalMin(value = "0.0", message = "Урьдчилан төлөлтийн торгууль сөрөг байж болохгүй")
    private BigDecimal earlyPaymentPenalty;

    @Column(name = "early_payment_penalty_rate", precision = 7, scale = 4)
    @DecimalMin(value = "0.0", message = "Торгууль сөрөг байж болохгүй")
    private BigDecimal earlyPaymentPenaltyRate;

    @Column(name = "late_payment_penalty", precision = 15, scale = 2)
    @DecimalMin(value = "0.0", message = "Хожуу төлөлтийн торгууль сөрөг байж болохгүй")
    private BigDecimal latePaymentPenalty;

    @Column(name = "late_payment_penalty_rate", precision = 7, scale = 4)
    @DecimalMin(value = "0.0", message = "Хоцрогдлын торгууль сөрөг байж болохгүй")
    private BigDecimal latePaymentPenaltyRate;

    @Column(name = "required_documents", columnDefinition = "TEXT")
    private String requiredDocuments;

    @Column(name = "terms_and_conditions", columnDefinition = "TEXT")
    private String termsAndConditions;

    @Column(name = "special_conditions", columnDefinition = "TEXT")
    private String specialConditions;

    @Column(name = "marketing_message", length = 1000)
    @Size(max = 1000, message = "Маркетингийн мессеж 1000 тэмдэгтээс ихгүй байх ёстой")
    private String marketingMessage;

    @Column(name = "is_featured")
    private Boolean isFeatured = false;

    @Column(name = "display_order")
    @Min(value = 0, message = "Харуулах дараалал сөрөг байж болохгүй")
    private Integer displayOrder = 0;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @OneToMany(mappedBy = "loanProduct", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LoanApplication> loanApplications = new ArrayList<>();

    public enum LoanType {
        PERSONAL("PERSONAL", "Хувийн зээл"),
        BUSINESS("BUSINESS", "Бизнесийн зээл"),
        MORTGAGE("MORTGAGE", "Орон сууцны зээл"),
        CAR("CAR", "Автомашины зээл"),
        EDUCATION("EDUCATION", "Боловсролын зээл"),
        MEDICAL("MEDICAL", "Эрүүл мэндийн зээл"),
        CONSUMER("CONSUMER", "Хэрэглээний зээл");

        private final String code;
        private final String mongolianName;

        LoanType(String code, String mongolianName) {
            this.code = code;
            this.mongolianName = mongolianName;
        }

        public String getCode() { return code; }
        public String getMongolianName() { return mongolianName; }
    }

    public LoanProduct() {
        super();
    }

    public LoanProduct(String name, String productName, LoanType loanType,
                      BigDecimal minAmount, BigDecimal maxAmount, Integer minTermMonths, Integer maxTermMonths) {
        this();
        this.name = name;
        this.productName = productName;
        this.loanType = loanType;
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
        this.minTermMonths = minTermMonths;
        this.maxTermMonths = maxTermMonths;
    }

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
            return processingFee != null ? processingFee : BigDecimal.ZERO;
        }
        return loanAmount.multiply(processingFeeRate);
    }

    public BigDecimal calculateMonthlyPayment(BigDecimal loanAmount, Integer termMonths, BigDecimal interestRate) {
        if (!isAmountWithinLimits(loanAmount) || !isTermWithinLimits(termMonths)) {
            throw new IllegalArgumentException("Зээлийн дүн эсвэл хугацаа буруу байна");
        }

        BigDecimal rate = interestRate != null ? interestRate : defaultInterestRate != null ? defaultInterestRate : baseRate;
        if (rate == null || rate.compareTo(BigDecimal.ZERO) == 0) {
            return loanAmount.divide(BigDecimal.valueOf(termMonths), 2, RoundingMode.HALF_UP);
        }

        BigDecimal monthlyRate = rate.divide(BigDecimal.valueOf(12), 6, RoundingMode.HALF_UP);
        BigDecimal onePlusRate = BigDecimal.ONE.add(monthlyRate);
        BigDecimal numerator = loanAmount.multiply(monthlyRate);
        BigDecimal denominator = BigDecimal.ONE.subtract(
            BigDecimal.ONE.divide(onePlusRate.pow(termMonths), 6, RoundingMode.HALF_UP)
        );
        
        return numerator.divide(denominator, 2, RoundingMode.HALF_UP);
    }

    public void enable() {
        this.setIsActive(true);
        this.setUpdatedAt(java.time.LocalDateTime.now());
    }

    public void disable() {
        this.setIsActive(false);
        this.setUpdatedAt(java.time.LocalDateTime.now());
    }

    public String getDisplayName() {
        return name + (productName != null && !productName.equals(name) ? " (" + productName + ")" : "");
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
        } else if (baseRate != null) {
            return String.format("%.2f%%", baseRate.multiply(BigDecimal.valueOf(100)));
        }
        return "Тодорхойгүй";
    }

    public String getFormattedMinAmount() {
        return minAmount != null ? String.format("%,.0f₮", minAmount) : "";
    }

    public String getFormattedMaxAmount() {
        return maxAmount != null ? String.format("%,.0f₮", maxAmount) : "";
    }

    public String getFormattedBaseRate() {
        return baseRate != null ? String.format("%.2f%%", baseRate.multiply(BigDecimal.valueOf(100))) : "";
    }

    public String getTermText() {
        return String.format("%d - %d сар", minTermMonths != null ? minTermMonths : 0, 
                           maxTermMonths != null ? maxTermMonths : 0);
    }

    public Integer getApplicationCount() {
        return loanApplications != null ? loanApplications.size() : 0;
    }

    public BigDecimal getTotalApplicationAmount() {
        if (loanApplications == null || loanApplications.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return loanApplications.stream()
                .filter(app -> app.getRequestedAmount() != null)
                .map(LoanApplication::getRequestedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Boolean hasValidLimits() {
        return minAmount != null && maxAmount != null && minAmount.compareTo(maxAmount) <= 0 &&
               minTermMonths != null && maxTermMonths != null && minTermMonths <= maxTermMonths;
    }

    public String getValidationErrors() {
        List<String> errors = new ArrayList<>();
        
        if (minAmount != null && maxAmount != null && minAmount.compareTo(maxAmount) > 0) {
            errors.add("Хамгийн бага дүн их дүнээс их байна");
        }
        
        if (minTermMonths != null && maxTermMonths != null && minTermMonths > maxTermMonths) {
            errors.add("Хамгийн бага хугацаа их хугацаанаас их байна");
        }
        
        if (minInterestRate != null && maxInterestRate != null && minInterestRate.compareTo(maxInterestRate) > 0) {
            errors.add("Хамгийн бага хүү их хүүнээс их байна");
        }
        
        return errors.isEmpty() ? null : String.join(", ", errors);
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

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

    public BigDecimal getBaseRate() { return baseRate; }
    public void setBaseRate(BigDecimal baseRate) { this.baseRate = baseRate; }

    public BigDecimal getMinInterestRate() { return minInterestRate; }
    public void setMinInterestRate(BigDecimal minInterestRate) { this.minInterestRate = minInterestRate; }

    public BigDecimal getMaxInterestRate() { return maxInterestRate; }
    public void setMaxInterestRate(BigDecimal maxInterestRate) { this.maxInterestRate = maxInterestRate; }

    public BigDecimal getDefaultInterestRate() { return defaultInterestRate; }
    public void setDefaultInterestRate(BigDecimal defaultInterestRate) { this.defaultInterestRate = defaultInterestRate; }

    public BigDecimal getAutoApprovalLimit() { return autoApprovalLimit; }
    public void setAutoApprovalLimit(BigDecimal autoApprovalLimit) { this.autoApprovalLimit = autoApprovalLimit; }

    public Boolean getApprovalRequired() { return approvalRequired; }
    public void setApprovalRequired(Boolean approvalRequired) { this.approvalRequired = approvalRequired; }

    public Boolean getRequiresCollateral() { return requiresCollateral; }
    public void setRequiresCollateral(Boolean requiresCollateral) { this.requiresCollateral = requiresCollateral; }

    public Boolean getRequiresGuarantor() { return requiresGuarantor; }
    public void setRequiresGuarantor(Boolean requiresGuarantor) { this.requiresGuarantor = requiresGuarantor; }

    public Integer getMinCreditScore() { return minCreditScore; }
    public void setMinCreditScore(Integer minCreditScore) { this.minCreditScore = minCreditScore; }

    public BigDecimal getMaxDebtRatio() { return maxDebtRatio; }
    public void setMaxDebtRatio(BigDecimal maxDebtRatio) { this.maxDebtRatio = maxDebtRatio; }

    public BigDecimal getMinIncome() { return minIncome; }
    public void setMinIncome(BigDecimal minIncome) { this.minIncome = minIncome; }

    public BigDecimal getProcessingFee() { return processingFee; }
    public void setProcessingFee(BigDecimal processingFee) { this.processingFee = processingFee; }

    public BigDecimal getProcessingFeeRate() { return processingFeeRate; }
    public void setProcessingFeeRate(BigDecimal processingFeeRate) { this.processingFeeRate = processingFeeRate; }

    public BigDecimal getEarlyPaymentPenalty() { return earlyPaymentPenalty; }
    public void setEarlyPaymentPenalty(BigDecimal earlyPaymentPenalty) { this.earlyPaymentPenalty = earlyPaymentPenalty; }

    public BigDecimal getEarlyPaymentPenaltyRate() { return earlyPaymentPenaltyRate; }
    public void setEarlyPaymentPenaltyRate(BigDecimal earlyPaymentPenaltyRate) { this.earlyPaymentPenaltyRate = earlyPaymentPenaltyRate; }

    public BigDecimal getLatePaymentPenalty() { return latePaymentPenalty; }
    public void setLatePaymentPenalty(BigDecimal latePaymentPenalty) { this.latePaymentPenalty = latePaymentPenalty; }

    public BigDecimal getLatePaymentPenaltyRate() { return latePaymentPenaltyRate; }
    public void setLatePaymentPenaltyRate(BigDecimal latePaymentPenaltyRate) { this.latePaymentPenaltyRate = latePaymentPenaltyRate; }

    public String getRequiredDocuments() { return requiredDocuments; }
    public void setRequiredDocuments(String requiredDocuments) { this.requiredDocuments = requiredDocuments; }

    public String getTermsAndConditions() { return termsAndConditions; }
    public void setTermsAndConditions(String termsAndConditions) { this.termsAndConditions = termsAndConditions; }

    public String getSpecialConditions() { return specialConditions; }
    public void setSpecialConditions(String specialConditions) { this.specialConditions = specialConditions; }

    public String getMarketingMessage() { return marketingMessage; }
    public void setMarketingMessage(String marketingMessage) { this.marketingMessage = marketingMessage; }

    public Boolean getIsFeatured() { return isFeatured; }
    public void setIsFeatured(Boolean isFeatured) { this.isFeatured = isFeatured; }

    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public List<LoanApplication> getLoanApplications() { return loanApplications; }
    public void setLoanApplications(List<LoanApplication> loanApplications) { this.loanApplications = loanApplications; }

    @Override
    public String toString() {
        return "LoanProduct{" +
                "id=" + getId() +
                ", name='" + name + '\'' +
                ", productName='" + productName + '\'' +
                ", loanType=" + loanType +
                ", minAmount=" + minAmount +
                ", maxAmount=" + maxAmount +
                ", isActive=" + isActive +
                '}';
    }
}