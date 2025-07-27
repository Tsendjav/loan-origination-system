package com.company.los.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.company.los.entity.LoanApplication;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Зээлийн хүсэлт үүсгэх DTO
 * Create Loan Request Data Transfer Object
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateLoanRequestDto {

    @NotNull(message = "Харилцагч заавал сонгох ёстой")
    private UUID customerId;

    @NotNull(message = "Зээлийн бүтээгдэхүүн заавал сонгох ёстой")
    private UUID loanProductId;

    @NotNull(message = "Зээлийн төрөл заавал сонгох ёстой")
    private LoanApplication.LoanType loanType;

    @NotNull(message = "Хүсэх дүн заавал бөглөх ёстой")
    @DecimalMin(value = "100000.0", message = "Хамгийн бага зээлийн хэмжээ 100,000₮")
    @DecimalMax(value = "100000000.0", message = "Хамгийн их зээлийн хэмжээ 100,000,000₮")
    private BigDecimal requestedAmount;

    @NotNull(message = "Хүсэх хугацаа заавал бөглөх ёстой")
    @Min(value = 3, message = "Хамгийн бага хугацаа 3 сар")
    @Max(value = 300, message = "Хамгийн их хугацаа 300 сар")
    private Integer requestedTermMonths;

    @Size(max = 500, message = "Зорилго 500 тэмдэгтээс ихгүй байх ёстой")
    private String purpose;

    @DecimalMin(value = "0.0", message = "Орлого сөрөг байж болохгүй")
    private BigDecimal declaredIncome;

    // Optional fields for loan calculation
    @DecimalMin(value = "0.0", message = "Одоо төлж байгаа зээлийн дүн сөрөг байж болохгүй")
    private BigDecimal existingLoanAmount;

    @DecimalMin(value = "0.0", message = "Барьцааны үнэлгээ сөрөг байж болохгүй")
    private BigDecimal collateralValue;

    @Size(max = 200, message = "Барьцааны тайлбар 200 тэмдэгтээс ихгүй байх ёстой")
    private String collateralDescription;

    @Min(value = 1, message = "Тэргүүлэх эрэмбэ 1-ээс бага байж болохгүй")
    @Max(value = 5, message = "Тэргүүлэх эрэмбэ 5-аас их байж болохгүй")
    private Integer priority;

    // Optional workflow fields
    @Size(max = 100, message = "Хүлээлгэх хүн 100 тэмдэгтээс ихгүй байх ёстой")
    private String assignTo;

    @Size(max = 1000, message = "Тэмдэглэл 1000 тэмдэгтээс ихгүй байх ёстой")
    private String notes;

    // Auto-save settings
    private Boolean saveAsDraft = false;
    private Boolean autoSubmit = false;

    // Constructors
    public CreateLoanRequestDto() {
        this.priority = 3; // Default medium priority
    }

    public CreateLoanRequestDto(UUID customerId, LoanApplication.LoanType loanType, 
                               BigDecimal requestedAmount, Integer requestedTermMonths) {
        this();
        this.customerId = customerId;
        this.loanType = loanType;
        this.requestedAmount = requestedAmount;
        this.requestedTermMonths = requestedTermMonths;
    }

    // Validation methods
    public boolean isValidRequest() {
        return customerId != null &&
               loanType != null &&
               requestedAmount != null && requestedAmount.compareTo(BigDecimal.ZERO) > 0 &&
               requestedTermMonths != null && requestedTermMonths > 0;
    }

    public boolean isWithinLoanTypeLimits() {
        if (loanType == null || requestedAmount == null || requestedTermMonths == null) {
            return false;
        }
        
        // Use hardcoded limits since enum doesn't have these methods
        BigDecimal minAmount = getLoanTypeMinAmount(loanType);
        BigDecimal maxAmount = getLoanTypeMaxAmount(loanType);
        Integer minTermMonths = getLoanTypeMinTermMonths(loanType);
        Integer maxTermMonths = getLoanTypeMaxTermMonths(loanType);
        
        return requestedAmount.compareTo(minAmount) >= 0 &&
               requestedAmount.compareTo(maxAmount) <= 0 &&
               requestedTermMonths >= minTermMonths &&
               requestedTermMonths <= maxTermMonths;
    }

    public boolean hasCollateral() {
        return collateralValue != null && collateralValue.compareTo(BigDecimal.ZERO) > 0 &&
               collateralDescription != null && !collateralDescription.trim().isEmpty();
    }

    public boolean hasExistingDebt() {
        return existingLoanAmount != null && existingLoanAmount.compareTo(BigDecimal.ZERO) > 0;
    }

    // Business logic methods
    public String getFormattedRequestedAmount() {
        return requestedAmount != null ? String.format("%,.0f₮", requestedAmount) : "";
    }

    public String getFormattedDeclaredIncome() {
        return declaredIncome != null ? String.format("%,.0f₮", declaredIncome) : "";
    }

    public String getFormattedCollateralValue() {
        return collateralValue != null ? String.format("%,.0f₮", collateralValue) : "";
    }

    public String getTermText() {
        return requestedTermMonths != null ? requestedTermMonths + " сар" : "";
    }

    public String getLoanTypeDisplay() {
        return loanType != null ? loanType.getMongolianName() : "";
    }

    public String getPriorityText() {
        if (priority == null) return "Дунд";
        switch (priority) {
            case 1: case 2: return "Өндөр";
            case 3: return "Дунд";
            case 4: case 5: return "Бага";
            default: return "Дунд";
        }
    }

    // Calculate debt-to-income ratio if both values are provided
    public BigDecimal getDebtToIncomeRatio() {
        if (declaredIncome == null || declaredIncome.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        
        BigDecimal totalDebt = requestedAmount;
        if (existingLoanAmount != null) {
            totalDebt = totalDebt.add(existingLoanAmount);
        }
        
        return totalDebt.divide(declaredIncome, 4, BigDecimal.ROUND_HALF_UP);
    }

    // Calculate loan-to-value ratio for secured loans
    public BigDecimal getLoanToValueRatio() {
        if (collateralValue == null || collateralValue.compareTo(BigDecimal.ZERO) == 0 ||
            requestedAmount == null || requestedAmount.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        
        return requestedAmount.divide(collateralValue, 4, BigDecimal.ROUND_HALF_UP);
    }

    // Convert to LoanApplicationDto
    public LoanApplicationDto toLoanApplicationDto() {
        LoanApplicationDto dto = new LoanApplicationDto();
        dto.setCustomerId(this.customerId);
        dto.setLoanProductId(this.loanProductId);
        dto.setLoanType(this.loanType);
        dto.setRequestedAmount(this.requestedAmount);
        dto.setRequestedTermMonths(this.requestedTermMonths);
        dto.setPurpose(this.purpose);
        dto.setDeclaredIncome(this.declaredIncome);
        dto.setPriority(this.priority != null ? this.priority : 3);
        dto.setAssignedTo(this.assignTo);
        
        // Add notes to decision reason if provided
        if (this.notes != null && !this.notes.trim().isEmpty()) {
            dto.setDecisionReason("Хүсэлт үүсгэх үеийн тэмдэглэл: " + this.notes);
        }
        
        return dto;
    }

    // Validation error messages
    public String getValidationSummary() {
        StringBuilder sb = new StringBuilder();
        
        if (customerId == null) {
            sb.append("• Харилцагч сонгоогүй\n");
        }
        
        if (loanType == null) {
            sb.append("• Зээлийн төрөл сонгоогүй\n");
        }
        
        if (requestedAmount == null || requestedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            sb.append("• Хүсэх дүн буруу\n");
        }
        
        if (requestedTermMonths == null || requestedTermMonths <= 0) {
            sb.append("• Хүсэх хугацаа буруу\n");
        }
        
        if (!isWithinLoanTypeLimits()) {
            sb.append("• Зээлийн төрлийн хязгаараас хэтэрсэн\n");
        }
        
        return sb.toString();
    }

    public boolean hasValidationErrors() {
        return !isValidRequest() || !isWithinLoanTypeLimits();
    }

    // Risk assessment hints
    public String getRiskAssessmentHints() {
        StringBuilder hints = new StringBuilder();
        
        // Check debt-to-income ratio
        BigDecimal dtiRatio = getDebtToIncomeRatio();
        if (dtiRatio != null) {
            if (dtiRatio.compareTo(BigDecimal.valueOf(0.4)) > 0) {
                hints.append("• Өндөр өр/орлогын харьцаа (").append(String.format("%.1f", dtiRatio.multiply(BigDecimal.valueOf(100)))).append("%)\n");
            }
        }
        
        // Check loan-to-value ratio
        BigDecimal ltvRatio = getLoanToValueRatio();
        if (ltvRatio != null) {
            if (ltvRatio.compareTo(BigDecimal.valueOf(0.8)) > 0) {
                hints.append("• Өндөр зээл/барьцааны харьцаа (").append(String.format("%.1f", ltvRatio.multiply(BigDecimal.valueOf(100)))).append("%)\n");
            }
        }
        
        // Check amount vs loan type
        if (loanType != null && requestedAmount != null) {
            BigDecimal maxAmount = getLoanTypeMaxAmount(loanType);
            if (requestedAmount.compareTo(maxAmount.multiply(BigDecimal.valueOf(0.9))) > 0) {
                hints.append("• Их дүнгийн зээлийн хүсэлт\n");
            }
        }
        
        // Check term vs loan type
        if (loanType != null && requestedTermMonths != null) {
            Integer maxTermMonths = getLoanTypeMaxTermMonths(loanType);
            if (requestedTermMonths > maxTermMonths * 0.8) {
                hints.append("• Урт хугацааны зээлийн хүсэлт\n");
            }
        }
        
        return hints.toString();
    }

    // Helper methods for loan type limits (since enum doesn't have these methods)
    private BigDecimal getLoanTypeMinAmount(LoanApplication.LoanType loanType) {
        switch (loanType) {
            case PERSONAL: return new BigDecimal("100000");
            case BUSINESS: return new BigDecimal("500000");
            case MORTGAGE: return new BigDecimal("5000000");
            case CAR_LOAN: return new BigDecimal("2000000");
            case CONSUMER: return new BigDecimal("50000");
            case EDUCATION: return new BigDecimal("100000");
            case MEDICAL: return new BigDecimal("50000");
            default: return new BigDecimal("100000");
        }
    }

    private BigDecimal getLoanTypeMaxAmount(LoanApplication.LoanType loanType) {
        switch (loanType) {
            case PERSONAL: return new BigDecimal("10000000");
            case BUSINESS: return new BigDecimal("100000000");
            case MORTGAGE: return new BigDecimal("500000000");
            case CAR_LOAN: return new BigDecimal("50000000");
            case CONSUMER: return new BigDecimal("5000000");
            case EDUCATION: return new BigDecimal("20000000");
            case MEDICAL: return new BigDecimal("10000000");
            default: return new BigDecimal("10000000");
        }
    }

    private Integer getLoanTypeMinTermMonths(LoanApplication.LoanType loanType) {
        switch (loanType) {
            case PERSONAL: return 3;
            case BUSINESS: return 6;
            case MORTGAGE: return 12;
            case CAR_LOAN: return 6;
            case CONSUMER: return 3;
            case EDUCATION: return 6;
            case MEDICAL: return 3;
            default: return 3;
        }
    }

    private Integer getLoanTypeMaxTermMonths(LoanApplication.LoanType loanType) {
        switch (loanType) {
            case PERSONAL: return 60;
            case BUSINESS: return 120;
            case MORTGAGE: return 360;
            case CAR_LOAN: return 84;
            case CONSUMER: return 36;
            case EDUCATION: return 120;
            case MEDICAL: return 60;
            default: return 60;
        }
    }

    // Getters and Setters
    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }

    public UUID getLoanProductId() { return loanProductId; }
    public void setLoanProductId(UUID loanProductId) { this.loanProductId = loanProductId; }

    public LoanApplication.LoanType getLoanType() { return loanType; }
    public void setLoanType(LoanApplication.LoanType loanType) { this.loanType = loanType; }

    public BigDecimal getRequestedAmount() { return requestedAmount; }
    public void setRequestedAmount(BigDecimal requestedAmount) { this.requestedAmount = requestedAmount; }

    public Integer getRequestedTermMonths() { return requestedTermMonths; }
    public void setRequestedTermMonths(Integer requestedTermMonths) { this.requestedTermMonths = requestedTermMonths; }

    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }

    public BigDecimal getDeclaredIncome() { return declaredIncome; }
    public void setDeclaredIncome(BigDecimal declaredIncome) { this.declaredIncome = declaredIncome; }

    public BigDecimal getExistingLoanAmount() { return existingLoanAmount; }
    public void setExistingLoanAmount(BigDecimal existingLoanAmount) { this.existingLoanAmount = existingLoanAmount; }

    public BigDecimal getCollateralValue() { return collateralValue; }
    public void setCollateralValue(BigDecimal collateralValue) { this.collateralValue = collateralValue; }

    public String getCollateralDescription() { return collateralDescription; }
    public void setCollateralDescription(String collateralDescription) { this.collateralDescription = collateralDescription; }

    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }

    public String getAssignTo() { return assignTo; }
    public void setAssignTo(String assignTo) { this.assignTo = assignTo; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Boolean getSaveAsDraft() { return saveAsDraft; }
    public void setSaveAsDraft(Boolean saveAsDraft) { this.saveAsDraft = saveAsDraft; }

    public Boolean getAutoSubmit() { return autoSubmit; }
    public void setAutoSubmit(Boolean autoSubmit) { this.autoSubmit = autoSubmit; }

    @Override
    public String toString() {
        return "CreateLoanRequestDto{" +
                "customerId=" + customerId +
                ", loanProductId=" + loanProductId +
                ", loanType=" + loanType +
                ", requestedAmount=" + requestedAmount +
                ", requestedTermMonths=" + requestedTermMonths +
                ", purpose='" + purpose + '\'' +
                ", priority=" + priority +
                '}';
    }
}