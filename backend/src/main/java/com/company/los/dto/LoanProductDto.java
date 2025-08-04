package com.company.los.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.company.los.entity.LoanProduct;
import jakarta.validation.constraints.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Зээлийн бүтээгдэхүүний DTO
 * Loan Product Data Transfer Object
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoanProductDto {

    private static final Logger logger = LoggerFactory.getLogger(LoanProductDto.class);

    private UUID id;

    @NotBlank(message = "Бүтээгдэхүүний нэр заавал байх ёстой")
    @Size(max = 200, message = "Бүтээгдэхүүний нэр 200 тэмдэгтээс ихгүй байх ёстой")
    private String name;

    @Size(max = 200, message = "Бүтээгдэхүүний нэр 200 тэмдэгтээс ихгүй байх ёстой")
    private String productName;

    @NotNull(message = "Зээлийн төрөл заавал сонгох ёстой")
    private LoanProduct.LoanType loanType;

    private String description;

    @NotNull(message = "Хамгийн бага дүн заавал байх ёстой")
    @DecimalMin(value = "1000.0", message = "Хамгийн бага дүн 1,000-аас их байх ёстой")
    private BigDecimal minAmount;

    @NotNull(message = "Хамгийн их дүн заавал байх ёстой")
    @DecimalMin(value = "1000.0", message = "Хамгийн их дүн 1,000-аас их байх ёстой")
    private BigDecimal maxAmount;

    @NotNull(message = "Хамгийн бага хугацаа заавал байх ёстой")
    @Min(value = 1, message = "Хамгийн бага хугацаа 1 сараас их байх ёстой")
    private Integer minTermMonths;

    @NotNull(message = "Хамгийн их хугацаа заавал байх ёстой")
    @Min(value = 1, message = "Хамгийн их хугацаа 1 сараас их байх ёстой")
    private Integer maxTermMonths;

    @DecimalMin(value = "0.0", message = "Хүү сөрөг байж болохгүй")
    @DecimalMax(value = "1.0", message = "Хүү 100%-аас их байж болохгүй")
    private BigDecimal baseRate;

    @DecimalMin(value = "0.0", message = "Хүү сөрөг байж болохгүй")
    @DecimalMax(value = "1.0", message = "Хүү 100%-аас их байж болохгүй")
    private BigDecimal minInterestRate;

    @DecimalMin(value = "0.0", message = "Хүү сөрөг байж болохгүй")
    @DecimalMax(value = "1.0", message = "Хүү 100%-аас их байж болохгүй")
    private BigDecimal maxInterestRate;

    @DecimalMin(value = "0.0", message = "Хүү сөрөг байж болохгүй")
    @DecimalMax(value = "1.0", message = "Хүү 100%-аас их байж болохгүй")
    private BigDecimal defaultInterestRate;

    // Removed the 'loanTypes' field as it does not exist in the LoanProduct entity.
    // @Size(max = 500, message = "Зээлийн төрлүүд 500 тэмдэгтээс ихгүй байх ёстой")
    // private String loanTypes;

    @DecimalMin(value = "0.0", message = "Автомат зөвшөөрөлийн хязгаар сөрөг байж болохгүй")
    private BigDecimal autoApprovalLimit;

    private Boolean approvalRequired = true;

    private Boolean requiresCollateral = false;

    private Boolean requiresGuarantor = false;

    @Min(value = 300, message = "Хамгийн бага кредит скор 300-аас бага байж болохгүй")
    @Max(value = 850, message = "Хамгийн бага кредит скор 850-аас их байж болохгүй")
    private Integer minCreditScore;

    @DecimalMin(value = "0.0", message = "Хамгийн их өрийн харьцаа сөрөг байж болохгүй")
    @DecimalMax(value = "1.0", message = "Хамгийн их өрийн харьцаа 100%-аас их байж болохгүй")
    private BigDecimal maxDebtRatio;

    @DecimalMin(value = "0.0", message = "Хамгийн бага орлого сөрөг байж болохгүй")
    private BigDecimal minIncome;

    @DecimalMin(value = "0.0", message = "Боловсруулалтын шимтгэл сөрөг байж болохгүй")
    private BigDecimal processingFee;

    @DecimalMin(value = "0.0", message = "Боловсруулалтын шимтгэлийн хувь сөрөг байж болохгүй")
    private BigDecimal processingFeeRate;

    @DecimalMin(value = "0.0", message = "Урьдчилан төлөлтийн торгууль сөрөг байж болохгүй")
    private BigDecimal earlyPaymentPenalty;

    @DecimalMin(value = "0.0", message = "Торгууль сөрөг байж болохгүй")
    private BigDecimal earlyPaymentPenaltyRate;

    @DecimalMin(value = "0.0", message = "Хожуу төлөлтийн торгууль сөрөг байж болохгүй")
    private BigDecimal latePaymentPenalty;

    @DecimalMin(value = "0.0", message = "Хоцрогдлын торгууль сөрөг байж болохгүй")
    private BigDecimal latePaymentPenaltyRate;

    private String requiredDocuments;

    private String termsAndConditions;

    private String specialConditions;

    @Size(max = 1000, message = "Маркетингийн мессеж 1000 тэмдэгтээс ихгүй байх ёстой")
    private String marketingMessage;

    private Boolean isFeatured = false;

    @Min(value = 0, message = "Харуулах дараалал сөрөг байж болохгүй")
    private Integer displayOrder = 0;

    private Boolean isActive = true;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    private String createdBy;
    private String updatedBy;

    // Computed fields
    private String displayName;
    private String loanTypeDisplay;
    private String amountRange;
    private String termRange;
    private String interestRateRange;
    private String formattedMinAmount;
    private String formattedMaxAmount;
    private String formattedBaseRate;
    private String termText;
    private Integer applicationCount;
    private BigDecimal totalApplicationAmount;
    private Boolean hasValidLimits;
    private String validationErrors;

    // Constructors
    public LoanProductDto() {
        this.approvalRequired = true;
        this.requiresCollateral = false;
        this.requiresGuarantor = false;
        this.isFeatured = false;
        this.displayOrder = 0;
        this.isActive = true;
    }

    public LoanProductDto(String name, String productName, LoanProduct.LoanType loanType,
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

    // Static factory methods
    public static LoanProductDto fromEntity(LoanProduct loanProduct) {
        if (loanProduct == null) {
            return null;
        }

        LoanProductDto dto = new LoanProductDto();

        // ID is already UUID in BaseEntity, no conversion needed
        dto.setId(loanProduct.getId());
        dto.setName(loanProduct.getName());
        dto.setProductName(loanProduct.getProductName());
        dto.setLoanType(loanProduct.getLoanType());
        dto.setDescription(loanProduct.getDescription());
        dto.setMinAmount(loanProduct.getMinAmount());
        dto.setMaxAmount(loanProduct.getMaxAmount());
        dto.setMinTermMonths(loanProduct.getMinTermMonths());
        dto.setMaxTermMonths(loanProduct.getMaxTermMonths());
        dto.setBaseRate(loanProduct.getBaseRate());
        dto.setMinInterestRate(loanProduct.getMinInterestRate());
        dto.setMaxInterestRate(loanProduct.getMaxInterestRate());
        dto.setDefaultInterestRate(loanProduct.getDefaultInterestRate());
        // Removed: dto.setLoanTypes(loanProduct.getLoanTypes());
        dto.setAutoApprovalLimit(loanProduct.getAutoApprovalLimit());
        dto.setApprovalRequired(loanProduct.getApprovalRequired());
        dto.setRequiresCollateral(loanProduct.getRequiresCollateral());
        dto.setRequiresGuarantor(loanProduct.getRequiresGuarantor());
        dto.setMinCreditScore(loanProduct.getMinCreditScore());
        dto.setMaxDebtRatio(loanProduct.getMaxDebtRatio());
        dto.setMinIncome(loanProduct.getMinIncome());
        dto.setProcessingFee(loanProduct.getProcessingFee());
        dto.setProcessingFeeRate(loanProduct.getProcessingFeeRate());
        dto.setEarlyPaymentPenalty(loanProduct.getEarlyPaymentPenalty());
        dto.setEarlyPaymentPenaltyRate(loanProduct.getEarlyPaymentPenaltyRate());
        dto.setLatePaymentPenalty(loanProduct.getLatePaymentPenalty());
        dto.setLatePaymentPenaltyRate(loanProduct.getLatePaymentPenaltyRate());
        dto.setRequiredDocuments(loanProduct.getRequiredDocuments());
        dto.setTermsAndConditions(loanProduct.getTermsAndConditions());
        dto.setSpecialConditions(loanProduct.getSpecialConditions());
        dto.setMarketingMessage(loanProduct.getMarketingMessage());
        dto.setIsFeatured(loanProduct.getIsFeatured());
        dto.setDisplayOrder(loanProduct.getDisplayOrder());
        dto.setIsActive(loanProduct.getIsActive());
        dto.setCreatedAt(loanProduct.getCreatedAt());
        dto.setUpdatedAt(loanProduct.getUpdatedAt());

        // Audit fields are String in both DTO and BaseEntity
        dto.setCreatedBy(loanProduct.getCreatedBy());
        dto.setUpdatedBy(loanProduct.getUpdatedBy());

        // Set computed fields with safe method calls
        try {
            dto.setDisplayName(loanProduct.getDisplayName());
        } catch (Exception e) {
            dto.setDisplayName(loanProduct.getName());
        }

        try {
            dto.setLoanTypeDisplay(loanProduct.getLoanTypeDisplay());
        } catch (Exception e) {
            dto.setLoanTypeDisplay(loanProduct.getLoanType() != null ? loanProduct.getLoanType().getMongolianName() : "");
        }

        try {
            dto.setAmountRange(loanProduct.getAmountRange());
        } catch (Exception e) {
            dto.setAmountRange(dto.calculateAmountRange());
        }

        try {
            dto.setTermRange(loanProduct.getTermRange());
        } catch (Exception e) {
            dto.setTermRange(dto.calculateTermRange());
        }

        try {
            dto.setInterestRateRange(loanProduct.getInterestRateRange());
        } catch (Exception e) {
            dto.setInterestRateRange(dto.calculateInterestRateRange());
        }

        try {
            dto.setFormattedMinAmount(loanProduct.getFormattedMinAmount());
        } catch (Exception e) {
            dto.setFormattedMinAmount(dto.formatAmount(dto.getMinAmount()));
        }

        try {
            dto.setFormattedMaxAmount(loanProduct.getFormattedMaxAmount());
        } catch (Exception e) {
            dto.setFormattedMaxAmount(dto.formatAmount(dto.getMaxAmount()));
        }

        try {
            dto.setFormattedBaseRate(loanProduct.getFormattedBaseRate());
        } catch (Exception e) {
            dto.setFormattedBaseRate(dto.formatRate(dto.getBaseRate()));
        }

        try {
            dto.setTermText(loanProduct.getTermText());
        } catch (Exception e) {
            dto.setTermText(dto.calculateTermText());
        }

        try {
            dto.setApplicationCount(loanProduct.getApplicationCount());
        } catch (Exception e) {
            dto.setApplicationCount(0);
        }

        try {
            dto.setTotalApplicationAmount(loanProduct.getTotalApplicationAmount());
        } catch (Exception e) {
            dto.setTotalApplicationAmount(BigDecimal.ZERO);
        }

        try {
            dto.setHasValidLimits(loanProduct.hasValidLimits());
        } catch (Exception e) {
            dto.setHasValidLimits(dto.calculateHasValidLimits());
        }

        try {
            dto.setValidationErrors(loanProduct.getValidationErrors());
        } catch (Exception e) {
            dto.setValidationErrors(dto.calculateValidationErrors());
        }

        return dto;
    }

    public static LoanProductDto createSummary(LoanProduct loanProduct) {
        if (loanProduct == null) {
            return null;
        }

        LoanProductDto dto = new LoanProductDto();

        // ID is already UUID in BaseEntity, no conversion needed
        dto.setId(loanProduct.getId());
        dto.setName(loanProduct.getName());
        dto.setLoanType(loanProduct.getLoanType());
        dto.setMinAmount(loanProduct.getMinAmount());
        dto.setMaxAmount(loanProduct.getMaxAmount());
        dto.setMinTermMonths(loanProduct.getMinTermMonths());
        dto.setMaxTermMonths(loanProduct.getMaxTermMonths());
        dto.setIsActive(loanProduct.getIsActive());

        // Set basic computed fields
        try {
            dto.setDisplayName(loanProduct.getDisplayName());
        } catch (Exception e) {
            dto.setDisplayName(loanProduct.getName());
        }

        try {
            dto.setLoanTypeDisplay(loanProduct.getLoanTypeDisplay());
        } catch (Exception e) {
            dto.setLoanTypeDisplay(loanProduct.getLoanType() != null ? loanProduct.getLoanType().getMongolianName() : "");
        }

        dto.setFormattedMinAmount(dto.formatAmount(dto.getMinAmount()));
        dto.setFormattedMaxAmount(dto.formatAmount(dto.getMaxAmount()));

        return dto;
    }

    public LoanProduct toEntity() {
        LoanProduct loanProduct = new LoanProduct();

        // ID is UUID in both DTO and entity, no conversion needed
        loanProduct.setId(this.id);
        loanProduct.setName(this.name);
        loanProduct.setProductName(this.productName);
        loanProduct.setLoanType(this.loanType);
        loanProduct.setDescription(this.description);
        loanProduct.setMinAmount(this.minAmount);
        loanProduct.setMaxAmount(this.maxAmount);
        loanProduct.setMinTermMonths(this.minTermMonths);
        loanProduct.setMaxTermMonths(this.maxTermMonths);
        loanProduct.setBaseRate(this.baseRate);
        loanProduct.setMinInterestRate(this.minInterestRate);
        loanProduct.setMaxInterestRate(this.maxInterestRate);
        loanProduct.setDefaultInterestRate(this.defaultInterestRate);
        // Removed: loanProduct.setLoanTypes(this.loanTypes);
        loanProduct.setAutoApprovalLimit(this.autoApprovalLimit);
        loanProduct.setApprovalRequired(this.approvalRequired);
        loanProduct.setRequiresCollateral(this.requiresCollateral);
        loanProduct.setRequiresGuarantor(this.requiresGuarantor);
        loanProduct.setMinCreditScore(this.minCreditScore);
        loanProduct.setMaxDebtRatio(this.maxDebtRatio);
        loanProduct.setMinIncome(this.minIncome);
        loanProduct.setProcessingFee(this.processingFee);
        loanProduct.setProcessingFeeRate(this.processingFeeRate);
        loanProduct.setEarlyPaymentPenalty(this.earlyPaymentPenalty);
        loanProduct.setEarlyPaymentPenaltyRate(this.earlyPaymentPenaltyRate);
        loanProduct.setLatePaymentPenalty(this.latePaymentPenalty);
        loanProduct.setLatePaymentPenaltyRate(this.latePaymentPenaltyRate);
        loanProduct.setRequiredDocuments(this.requiredDocuments);
        loanProduct.setTermsAndConditions(this.termsAndConditions);
        loanProduct.setSpecialConditions(this.specialConditions);
        loanProduct.setMarketingMessage(this.marketingMessage);
        loanProduct.setIsFeatured(this.isFeatured);
        loanProduct.setDisplayOrder(this.displayOrder);
        loanProduct.setIsActive(this.isActive);
        loanProduct.setCreatedAt(this.createdAt);
        loanProduct.setUpdatedAt(this.updatedAt);
        
        // Audit fields are String in both DTO and BaseEntity
        loanProduct.setCreatedBy(this.createdBy);
        loanProduct.setUpdatedBy(this.updatedBy);

        return loanProduct;
    }

    // Helper methods for computed fields
    private String calculateAmountRange() {
        if (minAmount != null && maxAmount != null) {
            return String.format("%.0f - %.0f", minAmount, maxAmount);
        }
        return "";
    }

    private String calculateTermRange() {
        if (minTermMonths != null && maxTermMonths != null) {
            return String.format("%d - %d сар", minTermMonths, maxTermMonths);
        }
        return "";
    }

    private String calculateInterestRateRange() {
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

    private String calculateTermText() {
        return String.format("%d - %d сар", minTermMonths != null ? minTermMonths : 0, 
                           maxTermMonths != null ? maxTermMonths : 0);
    }

    private Boolean calculateHasValidLimits() {
        return minAmount != null && maxAmount != null && minAmount.compareTo(maxAmount) <= 0 &&
               minTermMonths != null && maxTermMonths != null && minTermMonths <= maxTermMonths;
    }

    private String calculateValidationErrors() {
        java.util.List<String> errors = new java.util.ArrayList<>();
        
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

    private String formatAmount(BigDecimal amount) {
        return amount != null ? String.format("%,.0f₮", amount) : "";
    }

    private String formatRate(BigDecimal rate) {
        return rate != null ? String.format("%.2f%%", rate.multiply(BigDecimal.valueOf(100))) : "";
    }

    // Business logic methods
    public boolean isAmountWithinLimits(BigDecimal amount) {
        return amount != null && minAmount != null && maxAmount != null &&
               amount.compareTo(minAmount) >= 0 && 
               amount.compareTo(maxAmount) <= 0;
    }

    public boolean isTermWithinLimits(Integer termMonths) {
        return termMonths != null && minTermMonths != null && maxTermMonths != null &&
               termMonths >= minTermMonths && 
               termMonths <= maxTermMonths;
    }

    public boolean isEligibleForAutoApproval(BigDecimal amount) {
        return autoApprovalLimit != null && 
               amount != null && 
               amount.compareTo(autoApprovalLimit) <= 0;
    }

    public String getStatusBadgeClass() {
        if (!isActive) return "badge-secondary";
        if (isFeatured) return "badge-primary";
        return "badge-success";
    }

    public String getLoanTypeBadgeClass() {
        if (loanType == null) return "badge-secondary";
        switch (loanType) {
            case PERSONAL: return "badge-info";
            case BUSINESS: return "badge-primary";
            case MORTGAGE: return "badge-success";
            case CAR: return "badge-warning";
            case EDUCATION: return "badge-info";
            case MEDICAL: return "badge-danger";
            default: return "badge-secondary";
        }
    }

    public boolean isValidConfiguration() {
        return hasValidLimits != null ? hasValidLimits : 
               (minAmount != null && maxAmount != null && minAmount.compareTo(maxAmount) <= 0 &&
                minTermMonths != null && maxTermMonths != null && minTermMonths <= maxTermMonths);
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public LoanProduct.LoanType getLoanType() { return loanType; }
    public void setLoanType(LoanProduct.LoanType loanType) { this.loanType = loanType; }

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

    // Removed the getter/setter for 'loanTypes'
    // public String getLoanTypes() { return loanTypes; }
    // public void setLoanTypes(String loanTypes) { this.loanTypes = loanTypes; }

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

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getLoanTypeDisplay() { return loanTypeDisplay; }
    public void setLoanTypeDisplay(String loanTypeDisplay) { this.loanTypeDisplay = loanTypeDisplay; }

    public String getAmountRange() { return amountRange; }
    public void setAmountRange(String amountRange) { this.amountRange = amountRange; }

    public String getTermRange() { return termRange; }
    public void setTermRange(String termRange) { this.termRange = termRange; }

    public String getInterestRateRange() { return interestRateRange; }
    public void setInterestRateRange(String interestRateRange) { this.interestRateRange = interestRateRange; }

    public String getFormattedMinAmount() { return formattedMinAmount; }
    public void setFormattedMinAmount(String formattedMinAmount) { this.formattedMinAmount = formattedMinAmount; }

    public String getFormattedMaxAmount() { return formattedMaxAmount; }
    public void setFormattedMaxAmount(String formattedMaxAmount) { this.formattedMaxAmount = formattedMaxAmount; }

    public String getFormattedBaseRate() { return formattedBaseRate; }
    public void setFormattedBaseRate(String formattedBaseRate) { this.formattedBaseRate = formattedBaseRate; }

    public String getTermText() { return termText; }
    public void setTermText(String termText) { this.termText = termText; }

    public Integer getApplicationCount() { return applicationCount; }
    public void setApplicationCount(Integer applicationCount) { this.applicationCount = applicationCount; }

    public BigDecimal getTotalApplicationAmount() { return totalApplicationAmount; }
    public void setTotalApplicationAmount(BigDecimal totalApplicationAmount) { this.totalApplicationAmount = totalApplicationAmount; }

    public Boolean getHasValidLimits() { return hasValidLimits; }
    public void setHasValidLimits(Boolean hasValidLimits) { this.hasValidLimits = hasValidLimits; }

    public String getValidationErrors() { return validationErrors; }
    public void setValidationErrors(String validationErrors) { this.validationErrors = validationErrors; }

    @Override
    public String toString() {
        return "LoanProductDto{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", productName='" + productName + '\'' +
                ", loanType=" + loanType +
                ", minAmount=" + minAmount +
                ", maxAmount=" + maxAmount +
                ", isActive=" + isActive +
                '}';
    }
}
