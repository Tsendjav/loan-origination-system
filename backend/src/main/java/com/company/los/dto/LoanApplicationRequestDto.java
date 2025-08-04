package com.company.los.dto;

import com.company.los.entity.LoanApplication;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Зээлийн хүсэлт үүсгэх request DTO
 * Loan Application Request DTO for REST API
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanApplicationRequestDto {

    @NotNull(message = "Харилцагчийн ID заавал байх ёстой")
    private UUID customerId;

    @NotNull(message = "Зээлийн бүтээгдэхүүний ID заавал байх ёстой")
    private UUID loanProductId;

    @NotNull(message = "Зээлийн төрөл заавал сонгох ёстой")
    private LoanApplication.LoanType loanType;

    @NotNull(message = "Хүссэн дүн заавал байх ёстой")
    @DecimalMin(value = "1000.0", message = "Хүссэн дүн 1,000₮-аас их байх ёстой")
    @DecimalMax(value = "1000000000.0", message = "Зээлийн дүн хэт их байна")
    private BigDecimal requestedAmount;

    @NotNull(message = "Зээлийн хугацаа заавал байх ёстой")
    @Min(value = 1, message = "Зээлийн хугацаа 1 сараас их байх ёстой")
    @Max(value = 360, message = "Зээлийн хугацаа 360 сараас бага байх ёстой")
    private Integer requestedTermMonths;

    @DecimalMin(value = "0.0", message = "Хүү сөрөг байж болохгүй")
    @DecimalMax(value = "1.0", message = "Хүү 100%-аас их байж болохгүй")
    private BigDecimal interestRate;

    @NotBlank(message = "Зээлийн зорилго заавал байх ёстой")
    @Size(max = 2000, message = "Зээлийн зорилго 2000 тэмдэгтээс ихгүй байх ёстой")
    private String purpose;

    @Size(max = 1000, message = "Тайлбар 1000 тэмдэгтээс ихгүй байх ёстой")
    private String description;

    @DecimalMin(value = "0.0", message = "Орлого сөрөг байж болохгүй")
    private BigDecimal declaredIncome;

    @Size(max = 100, message = "Хүлээлгэх хүн 100 тэмдэгтээс ихгүй байх ёстой")
    private String assignedTo;

    @Min(value = 300, message = "Кредит скор 300-аас бага байж болохгүй")
    @Max(value = 850, message = "Кредит скор 850-аас их байж болохгүй")
    private Integer creditScore;

    @DecimalMin(value = "0.0", message = "Өрийн харьцаа сөрөг байж болохгүй")
    @DecimalMax(value = "1.0", message = "Өрийн харьцаа 100%-аас их байж болохгүй")
    private BigDecimal debtToIncomeRatio;

    @Builder.Default
    private Boolean requiresCollateral = false;

    @Builder.Default
    private Boolean requiresGuarantor = false;

    private LocalDate expectedDisbursementDate;

    @DecimalMin(value = "0.0", message = "Боловсруулалтын шимтгэл сөрөг байж болохгүй")
    private BigDecimal processingFee;

    @DecimalMin(value = "0.0", message = "Бусад зардал сөрөг байж болохгүй")
    private BigDecimal otherCharges;

    @Min(value = 1, message = "Эрэмбэ 1-ээс их байх ёстой")
    @Max(value = 10, message = "Эрэмбэ 10-аас бага байх ёстой")
    @Builder.Default
    private Integer priority = 1;

    @Size(max = 500, message = "Гэрээний нөхцөл 500 тэмдэгтээс ихгүй байх ёстой")
    private String contractTerms;

    @Size(max = 500, message = "Тусгай нөхцөл 500 тэмдэгтээс ихгүй байх ёстой")
    private String specialConditions;

    @Size(max = 1000, message = "Тэмдэглэл 1000 тэмдэгтээс ихгүй байх ёстой")
    private String notes;

    // Flow control fields
    @Builder.Default
    private Boolean saveAsDraft = false;
    @Builder.Default
    private Boolean autoSubmit = false;
    @Builder.Default
    private Boolean skipValidation = false;

    /**
     * CreateLoanRequestDto руу хөрвүүлэх
     */
    public CreateLoanRequestDto toCreateLoanRequestDto() {
        return CreateLoanRequestDto.builder()
                .customerId(this.customerId)
                .loanProductId(this.loanProductId)
                .loanType(this.loanType)
                .requestedAmount(this.requestedAmount)
                .requestedTermMonths(this.requestedTermMonths)
                .interestRate(this.interestRate)
                .purpose(this.purpose)
                .description(this.description)
                .declaredIncome(this.declaredIncome)
                .requiresCollateral(this.requiresCollateral)
                .requiresGuarantor(this.requiresGuarantor)
                .processingFee(this.processingFee)
                .otherCharges(this.otherCharges)
                .priority(this.priority)
                .specialConditions(this.specialConditions)
                .assignedTo(this.assignedTo)
                .notes(this.notes)
                .saveAsDraft(this.saveAsDraft)
                .build();
    }

    /**
     * LoanApplicationDto руу шууд хөрвүүлэх
     */
    public LoanApplicationDto toLoanApplicationDto() {
        return LoanApplicationDto.builder()
                .customerId(this.customerId)
                .loanProductId(this.loanProductId)
                .loanType(this.loanType)
                .requestedAmount(this.requestedAmount)
                .requestedTermMonths(this.requestedTermMonths)
                .interestRate(this.interestRate)
                .purpose(this.purpose)
                .description(this.description)
                .declaredIncome(this.declaredIncome)
                .assignedTo(this.assignedTo)
                .creditScore(this.creditScore)
                .debtToIncomeRatio(this.debtToIncomeRatio)
                .requiresCollateral(this.requiresCollateral)
                .requiresGuarantor(this.requiresGuarantor)
                .expectedDisbursementDate(this.expectedDisbursementDate)
                .processingFee(this.processingFee)
                .otherCharges(this.otherCharges)
                .priority(this.priority)
                .contractTerms(this.contractTerms)
                .specialConditions(this.specialConditions)
                .reviewerNotes(this.notes)
                .status(determineInitialStatus())
                .isActive(true)
                .build();
    }

    /**
     * Анхны статус тодорхойлох
     */
    private LoanApplication.ApplicationStatus determineInitialStatus() {
        if (saveAsDraft != null && saveAsDraft) {
            return LoanApplication.ApplicationStatus.DRAFT;
        } else if (autoSubmit != null && autoSubmit) {
            return LoanApplication.ApplicationStatus.SUBMITTED;
        } else {
            return LoanApplication.ApplicationStatus.PENDING;
        }
    }

    /**
     * Хүсэлт хүчинтэй эсэхийг шалгах
     */
    public boolean isValid() {
        return customerId != null &&
               loanProductId != null &&
               loanType != null &&
               requestedAmount != null && requestedAmount.compareTo(BigDecimal.ZERO) > 0 &&
               requestedTermMonths != null && requestedTermMonths > 0 &&
               purpose != null && !purpose.trim().isEmpty();
    }

    /**
     * Эрсдэлтэй хүсэлт эсэхийг шалгах
     */
    public boolean isHighRisk() {
        return (requestedAmount != null && requestedAmount.compareTo(new BigDecimal("50000000")) > 0) ||
               (requestedTermMonths != null && requestedTermMonths > 60) ||
               (creditScore != null && creditScore < 600) ||
               (debtToIncomeRatio != null && debtToIncomeRatio.compareTo(new BigDecimal("0.5")) > 0);
    }

    /**
     * Хурдан батлах боломжтой эсэхийг шалгах
     */
    public boolean isFastTrackEligible() {
        return requestedAmount != null && requestedAmount.compareTo(new BigDecimal("5000000")) <= 0 &&
               requestedTermMonths != null && requestedTermMonths <= 24 &&
               (requiresCollateral == null || !requiresCollateral) &&
               (requiresGuarantor == null || !requiresGuarantor) &&
               (creditScore == null || creditScore >= 700);
    }

    /**
     * Automatic approval eligible эсэхийг шалгах
     */
    public boolean isAutoApprovalEligible() {
        return isFastTrackEligible() &&
               requestedAmount != null && requestedAmount.compareTo(new BigDecimal("2000000")) <= 0 &&
               (creditScore != null && creditScore >= 750) &&
               (debtToIncomeRatio != null && debtToIncomeRatio.compareTo(new BigDecimal("0.3")) <= 0);
    }

    /**
     * Required documentation level
     */
    public String getRequiredDocumentationLevel() {
        if (isHighRisk()) {
            return "FULL";
        } else if (isFastTrackEligible()) {
            return "MINIMAL";
        } else {
            return "STANDARD";
        }
    }

    /**
     * Estimated processing time in days
     */
    public Integer getEstimatedProcessingDays() {
        if (isAutoApprovalEligible()) {
            return 1;
        } else if (isFastTrackEligible()) {
            return 3;
        } else if (isHighRisk()) {
            return 14;
        } else {
            return 7;
        }
    }

    /**
     * Format requested amount for display
     */
    public String getFormattedRequestedAmount() {
        return requestedAmount != null ? String.format("%,.0f₮", requestedAmount) : "";
    }

    /**
     * Format declared income for display
     */
    public String getFormattedDeclaredIncome() {
        return declaredIncome != null ? String.format("%,.0f₮", declaredIncome) : "";
    }

    /**
     * Get term display text
     */
    public String getTermText() {
        return requestedTermMonths != null ? requestedTermMonths + " сар" : "";
    }

    /**
     * Get loan type display name
     */
    public String getLoanTypeDisplay() {
        return loanType != null ? loanType.getMongolianName() : "";
    }

    /**
     * Get priority display text
     */
    public String getPriorityText() {
        if (priority == null) return "Дунд";
        switch (priority) {
            case 1: case 2: return "Өндөр";
            case 3: case 4: case 5: return "Дунд";
            case 6: case 7: case 8: return "Бага";
            case 9: case 10: return "Маш бага";
            default: return "Дунд";
        }
    }

    @Override
    public String toString() {
        return "LoanApplicationRequestDto{" +
                "customerId=" + customerId +
                ", loanProductId=" + loanProductId +
                ", loanType=" + loanType +
                ", requestedAmount=" + requestedAmount +
                ", requestedTermMonths=" + requestedTermMonths +
                ", purpose='" + purpose + '\'' +
                '}';
    }
}