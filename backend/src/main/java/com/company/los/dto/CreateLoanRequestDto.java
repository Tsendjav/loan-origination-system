package com.company.los.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.company.los.entity.LoanApplication;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Зээлийн хүсэлт үүсгэх DTO
 * Create Loan Request Data Transfer Object
 * ⭐ ЗАСВАРЛАСАН - GETTER/SETTER МЕТОДУУД НЭМЭГДСЭН ⭐
 *
 * @author LOS Development Team
 * @version 1.4 - All missing getters/setters added
 * @since 2025-08-01
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateLoanRequestDto {

    @NotNull(message = "Харилцагчийн ID заавал байх ёстой")
    private UUID customerId;

    private UUID loanProductId;

    @NotNull(message = "Зээлийн төрөл заавал сонгох ёстой")
    private LoanApplication.LoanType loanType;

    @NotNull(message = "Зээлийн дүн заавал байх ёстой")
    @DecimalMin(value = "1000.0", message = "Зээлийн дүн 1,000-аас их байх ёстой")
    @DecimalMax(value = "100000000.0", message = "Зээлийн дүн 100,000,000-аас бага байх ёстой")
    private BigDecimal requestedAmount;

    @NotNull(message = "Зээлийн хугацаа заавал байх ёстой")
    @Min(value = 1, message = "Зээлийн хугацаа 1 сараас их байх ёстой")
    @Max(value = 360, message = "Зээлийн хугацаа 360 сараас бага байх ёстой")
    private Integer requestedTermMonths;

    @Size(max = 2000, message = "Хүсэлтийн зорилго 2000 тэмдэгтээс ихгүй байх ёстой")
    private String purpose;

    @Size(max = 1000, message = "Тайлбар 1000 тэмдэгтээс ихгүй байх ёстой")
    private String description;

    @DecimalMin(value = "0.0", message = "Орлого сөрөг байж болохгүй")
    @DecimalMax(value = "10000000.0", message = "Орлого 10,000,000-аас бага байх ёстой")
    private BigDecimal declaredIncome;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expectedDisbursementDate;

    @Builder.Default
    private Boolean requiresCollateral = false;

    @Builder.Default
    private Boolean requiresGuarantor = false;

    @Min(value = 1, message = "Эрэмбэ 1-ээс бага байж болохгүй")
    @Max(value = 5, message = "Эрэмбэ 5-аас их байж болохгүй")
    @Builder.Default
    private Integer priority = 1;

    @Size(max = 500, message = "Тусгай нөхцөл 500 тэмдэгтээс ихгүй байх ёстой")
    private String specialConditions;

    @Size(max = 100, message = "Хүлээлгэх хүн 100 тэмдэгтээс ихгүй байх ёстой")
    private String assignedTo;

    @Builder.Default
    private Boolean saveAsDraft = false;

    @DecimalMin(value = "0.0", message = "Хүү сөрөг байж болохгүй")
    @DecimalMax(value = "1.0", message = "Хүү 100%-аас их байж болохгүй")
    private BigDecimal interestRate;

    @DecimalMin(value = "0.0", message = "Боловсруулалтын шимтгэл сөрөг байж болохгүй")
    private BigDecimal processingFee;

    @DecimalMin(value = "0.0", message = "Бусад зардал сөрөг байж болохгүй")
    private BigDecimal otherCharges;

    @Size(max = 1000, message = "Тэмдэглэл 1000 тэмдэгтээс ихгүй байх ёстой")
    private String notes;

    // Employment information (optional)
    @Size(max = 200, message = "Ажлын байрны нэр 200 тэмдэгтээс ихгүй байх ёстой")
    private String employerName;

    @Size(max = 100, message = "Албан тушаал 100 тэмдэгтээс ихгүй байх ёстой")
    private String jobTitle;

    @Min(value = 0, message = "Ажлын туршлага сөрөг байж болохгүй")
    @Max(value = 50, message = "Ажлын туршлага 50 жилээс бага байх ёстой")
    private Integer workExperienceYears;

    // Contact information (optional)
    @Size(max = 15, message = "Утасны дугаар 15 тэмдэгтээс ихгүй байх ёстой")
    @Pattern(regexp = "^[+]?[0-9]{8,15}$", message = "Утасны дугаар буруу форматтай байна")
    private String phoneNumber;

    @Size(max = 100, message = "Имэйл 100 тэмдэгтээс ихгүй байх ёстой")
    @Email(message = "Имэйлийн формат буруу байна")
    private String email;

    @Size(max = 500, message = "Хаяг 500 тэмдэгтээс ихгүй байх ёстой")
    private String address;

    // Financial information (optional)
    @DecimalMin(value = "0.0", message = "Сарын орлого сөрөг байж болохгүй")
    private BigDecimal monthlyIncome;

    @DecimalMin(value = "0.0", message = "Сарын зардал сөрөг байж болохгүй")
    private BigDecimal monthlyExpenses;

    @DecimalMin(value = "0.0", message = "Одоогийн өр сөрөг байж болохгүй")
    private BigDecimal existingDebt;

    @DecimalMin(value = "0.0", message = "Хөрөнгийн дүн сөрөг байж болохгүй")
    private BigDecimal assetValue;

    // Additional fields
    @Size(max = 1000, message = "Нэмэлт мэдээлэл 1000 тэмдэгтээс ихгүй байх ёстой")
    private String additionalInfo;

    @Builder.Default
    private Boolean hasExistingLoan = false;

    @Builder.Default
    private Boolean hasCollateral = false;

    @Builder.Default
    private Boolean hasGuarantor = false;

    // Marketing consent
    @Builder.Default
    private Boolean marketingConsent = false;

    @Builder.Default
    private Boolean termsAccepted = false;

    // Source information
    @Builder.Default
    private String source = "WEB";

    @Size(max = 100, message = "Хүсэлт илгээсэн хүн 100 тэмдэгтээс ихгүй байх ёстой")
    private String submittedBy;

    // ==================== GETTER/SETTER METHODS - НЭМЭГДСЭН ⭐ ====================

    public UUID getCustomerId() {
        return customerId;
    }

    public void setCustomerId(UUID customerId) {
        this.customerId = customerId;
    }

    public UUID getLoanProductId() {
        return loanProductId;
    }

    public void setLoanProductId(UUID loanProductId) {
        this.loanProductId = loanProductId;
    }

    public LoanApplication.LoanType getLoanType() {
        return loanType;
    }

    public void setLoanType(LoanApplication.LoanType loanType) {
        this.loanType = loanType;
    }

    public BigDecimal getRequestedAmount() {
        return requestedAmount;
    }

    public void setRequestedAmount(BigDecimal requestedAmount) {
        this.requestedAmount = requestedAmount;
    }

    public Integer getRequestedTermMonths() {
        return requestedTermMonths;
    }

    public void setRequestedTermMonths(Integer requestedTermMonths) {
        this.requestedTermMonths = requestedTermMonths;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getDeclaredIncome() {
        return declaredIncome;
    }

    public void setDeclaredIncome(BigDecimal declaredIncome) {
        this.declaredIncome = declaredIncome;
    }

    public LocalDate getExpectedDisbursementDate() {
        return expectedDisbursementDate;
    }

    public void setExpectedDisbursementDate(LocalDate expectedDisbursementDate) {
        this.expectedDisbursementDate = expectedDisbursementDate;
    }

    public Boolean getRequiresCollateral() {
        return requiresCollateral;
    }

    public void setRequiresCollateral(Boolean requiresCollateral) {
        this.requiresCollateral = requiresCollateral;
    }

    public Boolean getRequiresGuarantor() {
        return requiresGuarantor;
    }

    public void setRequiresGuarantor(Boolean requiresGuarantor) {
        this.requiresGuarantor = requiresGuarantor;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getSpecialConditions() {
        return specialConditions;
    }

    public void setSpecialConditions(String specialConditions) {
        this.specialConditions = specialConditions;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }

    public Boolean getSaveAsDraft() {
        return saveAsDraft;
    }

    public void setSaveAsDraft(Boolean saveAsDraft) {
        this.saveAsDraft = saveAsDraft;
    }

    public boolean isSaveAsDraft() {
        return Boolean.TRUE.equals(saveAsDraft);
    }

    public BigDecimal getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(BigDecimal interestRate) {
        this.interestRate = interestRate;
    }

    public BigDecimal getProcessingFee() {
        return processingFee;
    }

    public void setProcessingFee(BigDecimal processingFee) {
        this.processingFee = processingFee;
    }

    public BigDecimal getOtherCharges() {
        return otherCharges;
    }

    public void setOtherCharges(BigDecimal otherCharges) {
        this.otherCharges = otherCharges;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getEmployerName() {
        return employerName;
    }

    public void setEmployerName(String employerName) {
        this.employerName = employerName;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public Integer getWorkExperienceYears() {
        return workExperienceYears;
    }

    public void setWorkExperienceYears(Integer workExperienceYears) {
        this.workExperienceYears = workExperienceYears;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public BigDecimal getMonthlyIncome() {
        return monthlyIncome;
    }

    public void setMonthlyIncome(BigDecimal monthlyIncome) {
        this.monthlyIncome = monthlyIncome;
    }

    public BigDecimal getMonthlyExpenses() {
        return monthlyExpenses;
    }

    public void setMonthlyExpenses(BigDecimal monthlyExpenses) {
        this.monthlyExpenses = monthlyExpenses;
    }

    public BigDecimal getExistingDebt() {
        return existingDebt;
    }

    public void setExistingDebt(BigDecimal existingDebt) {
        this.existingDebt = existingDebt;
    }

    public BigDecimal getAssetValue() {
        return assetValue;
    }

    public void setAssetValue(BigDecimal assetValue) {
        this.assetValue = assetValue;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public Boolean getHasExistingLoan() {
        return hasExistingLoan;
    }

    public void setHasExistingLoan(Boolean hasExistingLoan) {
        this.hasExistingLoan = hasExistingLoan;
    }

    public Boolean getHasCollateral() {
        return hasCollateral;
    }

    public void setHasCollateral(Boolean hasCollateral) {
        this.hasCollateral = hasCollateral;
    }

    public Boolean getHasGuarantor() {
        return hasGuarantor;
    }

    public void setHasGuarantor(Boolean hasGuarantor) {
        this.hasGuarantor = hasGuarantor;
    }

    public Boolean getMarketingConsent() {
        return marketingConsent;
    }

    public void setMarketingConsent(Boolean marketingConsent) {
        this.marketingConsent = marketingConsent;
    }

    public Boolean getTermsAccepted() {
        return termsAccepted;
    }

    public void setTermsAccepted(Boolean termsAccepted) {
        this.termsAccepted = termsAccepted;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSubmittedBy() {
        return submittedBy;
    }

    public void setSubmittedBy(String submittedBy) {
        this.submittedBy = submittedBy;
    }

    // ==================== BUSINESS LOGIC METHODS ====================

    /**
     * Validate the loan request
     */
    public boolean isValid() {
        if (customerId == null || loanType == null || requestedAmount == null || requestedTermMonths == null) {
            return false;
        }
        if (requestedAmount.compareTo(BigDecimal.valueOf(1000)) < 0 || requestedAmount.compareTo(BigDecimal.valueOf(100000000)) > 0) {
            return false;
        }
        if (requestedTermMonths < 1 || requestedTermMonths > 360) {
            return false;
        }
        return true;
    }

    /**
     * Calculate debt-to-income ratio
     */
    public BigDecimal calculateDebtToIncomeRatio() {
        if (monthlyIncome == null || monthlyIncome.compareTo(BigDecimal.ZERO) <= 0 || existingDebt == null) {
            return BigDecimal.ZERO;
        }
        return existingDebt.divide(monthlyIncome, 4, RoundingMode.HALF_UP);
    }

    /**
     * Calculate monthly disposable income
     */
    public BigDecimal calculateDisposableIncome() {
        if (monthlyIncome == null) return BigDecimal.ZERO;
        BigDecimal expenses = monthlyExpenses != null ? monthlyExpenses : BigDecimal.ZERO;
        return monthlyIncome.subtract(expenses);
    }

    /**
     * Check if customer has good financial standing
     */
    public boolean hasGoodFinancialStanding() {
        BigDecimal debtRatio = calculateDebtToIncomeRatio();
        return debtRatio.compareTo(BigDecimal.valueOf(0.4)) <= 0; // Less than or equal to 40% debt ratio
    }

    /**
     * Get risk level based on financial data
     */
    public String getRiskLevel() {
        BigDecimal debtRatio = calculateDebtToIncomeRatio();

        if (debtRatio.compareTo(BigDecimal.valueOf(0.2)) <= 0) {
            return "LOW";
        } else if (debtRatio.compareTo(BigDecimal.valueOf(0.4)) <= 0) {
            return "MEDIUM";
        } else {
            return "HIGH";
        }
    }

    /**
     * Check if all required fields are provided
     */
    public boolean hasRequiredFields() {
        return customerId != null &&
               loanType != null &&
               requestedAmount != null &&
               requestedTermMonths != null &&
               (purpose != null && !purpose.trim().isEmpty());
    }

    /**
     * Check if customer has sufficient income
     */
    public boolean hasSufficientIncome(BigDecimal estimatedMonthlyPayment) {
        if (monthlyIncome == null || estimatedMonthlyPayment == null) {
            return false;
        }

        BigDecimal disposableIncome = calculateDisposableIncome();
        // Monthly payment should not exceed 50% of disposable income
        BigDecimal maxPayment = disposableIncome.multiply(BigDecimal.valueOf(0.5));

        return estimatedMonthlyPayment.compareTo(maxPayment) <= 0;
    }

    // ==================== CONVERSION METHODS ====================

    /**
     * Convert to LoanApplicationDto for processing
     * ⭐ ЗАСВАРЛАСАН: LoanApplicationDto дотроо builder() ашиглах ⭐
     */
    public LoanApplicationDto toLoanApplicationDto() {
        // ⭐ STUB implementation - LoanApplicationDto-д builder байгаа эсэхээс хамаарч ⭐
        LoanApplicationDto dto = new LoanApplicationDto();
        dto.setCustomerId(this.customerId);
        dto.setLoanProductId(this.loanProductId);
        dto.setLoanType(this.loanType);
        dto.setRequestedAmount(this.requestedAmount);
        dto.setRequestedTermMonths(this.requestedTermMonths);
        dto.setPurpose(this.purpose);
        dto.setDescription(this.description);
        dto.setDeclaredIncome(this.declaredIncome);
        dto.setExpectedDisbursementDate(this.expectedDisbursementDate);
        dto.setRequiresCollateral(this.requiresCollateral);
        dto.setRequiresGuarantor(this.requiresGuarantor);
        dto.setPriority(this.priority);
        dto.setSpecialConditions(this.specialConditions);
        dto.setAssignedTo(this.assignedTo);
        dto.setProcessingFee(this.processingFee);
        dto.setOtherCharges(this.otherCharges);
        dto.setReviewerNotes(this.notes);
        dto.setStatus(saveAsDraft ? LoanApplication.ApplicationStatus.DRAFT : LoanApplication.ApplicationStatus.PENDING);
        dto.setIsActive(true);
        dto.setCreatedBy(this.submittedBy);

        // Set decision reason based on analysis
        String riskLevel = getRiskLevel();
        if ("HIGH".equals(riskLevel)) {
            dto.setDecisionReason("High debt-to-income ratio detected. Manual review required.");
        } else if (!hasGoodFinancialStanding()) {
            dto.setDecisionReason("Financial standing requires additional verification.");
        }

        return dto;
    }

    // ==================== STATIC FACTORY METHODS ====================

    /**
     * Create a quick loan request with minimal information
     * ⭐ ЗАСВАРЛАСАН: Builder pattern ашиглах ⭐
     */
    public static CreateLoanRequestDto quickLoan(UUID customerId, LoanApplication.LoanType loanType,
                                                BigDecimal amount, Integer termMonths, String purpose) {
        return CreateLoanRequestDto.builder()
                .customerId(customerId)
                .loanType(loanType)
                .requestedAmount(amount)
                .requestedTermMonths(termMonths)
                .purpose(purpose)
                .priority(1)
                .source("WEB")
                .build();
    }

    /**
     * Create a business loan request
     * ⭐ ЗАСВАРЛАСАН: Builder pattern ашиглах ⭐
     */
    public static CreateLoanRequestDto businessLoan(UUID customerId, BigDecimal amount, Integer termMonths,
                                                   String businessPurpose, String employerName) {
        return CreateLoanRequestDto.builder()
                .customerId(customerId)
                .loanType(LoanApplication.LoanType.BUSINESS)
                .requestedAmount(amount)
                .requestedTermMonths(termMonths)
                .purpose(businessPurpose)
                .employerName(employerName)
                .source("BUSINESS_PORTAL")
                .build();
    }

    /**
     * Create a personal loan request
     * ⭐ ЗАСВАРЛАСАН: Builder pattern ашиглах ⭐
     */
    public static CreateLoanRequestDto personalLoan(UUID customerId, BigDecimal amount, Integer termMonths,
                                                   String purpose, BigDecimal monthlyIncome) {
        return CreateLoanRequestDto.builder()
                .customerId(customerId)
                .loanType(LoanApplication.LoanType.PERSONAL)
                .requestedAmount(amount)
                .requestedTermMonths(termMonths)
                .purpose(purpose)
                .monthlyIncome(monthlyIncome)
                .declaredIncome(monthlyIncome.multiply(BigDecimal.valueOf(12))) // Annual income
                .build();
    }

    // ==================== OVERRIDE METHODS ====================

    @Override
    public String toString() {
        return "CreateLoanRequestDto{" +
                "customerId=" + customerId +
                ", loanProductId=" + loanProductId +
                ", loanType=" + loanType +
                ", requestedAmount=" + requestedAmount +
                ", requestedTermMonths=" + requestedTermMonths +
                ", purpose='" + purpose + '\'' +
                ", description='" + description + '\'' +
                ", declaredIncome=" + declaredIncome +
                ", expectedDisbursementDate=" + expectedDisbursementDate +
                ", requiresCollateral=" + requiresCollateral +
                ", requiresGuarantor=" + requiresGuarantor +
                ", priority=" + priority +
                ", specialConditions='" + specialConditions + '\'' +
                ", assignedTo='" + assignedTo + '\'' +
                ", saveAsDraft=" + saveAsDraft +
                ", interestRate=" + interestRate +
                ", processingFee=" + processingFee +
                ", otherCharges=" + otherCharges +
                ", notes='" + notes + '\'' +
                ", employerName='" + employerName + '\'' +
                ", jobTitle='" + jobTitle + '\'' +
                ", workExperienceYears=" + workExperienceYears +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", email='" + email + '\'' +
                ", address='" + address + '\'' +
                ", monthlyIncome=" + monthlyIncome +
                ", monthlyExpenses=" + monthlyExpenses +
                ", existingDebt=" + existingDebt +
                ", assetValue=" + assetValue +
                ", additionalInfo='" + additionalInfo + '\'' +
                ", hasExistingLoan=" + hasExistingLoan +
                ", hasCollateral=" + hasCollateral +
                ", hasGuarantor=" + hasGuarantor +
                ", marketingConsent=" + marketingConsent +
                ", termsAccepted=" + termsAccepted +
                ", source='" + source + '\'' +
                ", submittedBy='" + submittedBy + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CreateLoanRequestDto that = (CreateLoanRequestDto) o;
        return Objects.equals(customerId, that.customerId) &&
               Objects.equals(loanProductId, that.loanProductId) &&
               loanType == that.loanType &&
               Objects.equals(requestedAmount, that.requestedAmount) &&
               Objects.equals(requestedTermMonths, that.requestedTermMonths) &&
               Objects.equals(purpose, that.purpose) &&
               Objects.equals(description, that.description) &&
               Objects.equals(declaredIncome, that.declaredIncome) &&
               Objects.equals(expectedDisbursementDate, that.expectedDisbursementDate) &&
               Objects.equals(requiresCollateral, that.requiresCollateral) &&
               Objects.equals(requiresGuarantor, that.requiresGuarantor) &&
               Objects.equals(priority, that.priority) &&
               Objects.equals(specialConditions, that.specialConditions) &&
               Objects.equals(assignedTo, that.assignedTo) &&
               Objects.equals(saveAsDraft, that.saveAsDraft) &&
               Objects.equals(interestRate, that.interestRate) &&
               Objects.equals(processingFee, that.processingFee) &&
               Objects.equals(otherCharges, that.otherCharges) &&
               Objects.equals(notes, that.notes) &&
               Objects.equals(employerName, that.employerName) &&
               Objects.equals(jobTitle, that.jobTitle) &&
               Objects.equals(workExperienceYears, that.workExperienceYears) &&
               Objects.equals(phoneNumber, that.phoneNumber) &&
               Objects.equals(email, that.email) &&
               Objects.equals(address, that.address) &&
               Objects.equals(monthlyIncome, that.monthlyIncome) &&
               Objects.equals(monthlyExpenses, that.monthlyExpenses) &&
               Objects.equals(existingDebt, that.existingDebt) &&
               Objects.equals(assetValue, that.assetValue) &&
               Objects.equals(additionalInfo, that.additionalInfo) &&
               Objects.equals(hasExistingLoan, that.hasExistingLoan) &&
               Objects.equals(hasCollateral, that.hasCollateral) &&
               Objects.equals(hasGuarantor, that.hasGuarantor) &&
               Objects.equals(marketingConsent, that.marketingConsent) &&
               Objects.equals(termsAccepted, that.termsAccepted) &&
               Objects.equals(source, that.source) &&
               Objects.equals(submittedBy, that.submittedBy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(customerId, loanProductId, loanType, requestedAmount, requestedTermMonths,
                            purpose, description, declaredIncome, expectedDisbursementDate, requiresCollateral,
                            requiresGuarantor, priority, specialConditions, assignedTo, saveAsDraft, interestRate,
                            processingFee, otherCharges, notes, employerName, jobTitle, workExperienceYears,
                            phoneNumber, email, address, monthlyIncome, monthlyExpenses, existingDebt,
                            assetValue, additionalInfo, hasExistingLoan, hasCollateral, hasGuarantor,
                            marketingConsent, termsAccepted, source, submittedBy);
    }
}