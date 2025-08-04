package com.company.los.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.company.los.entity.LoanApplication;
import com.company.los.entity.Customer;
import com.company.los.entity.LoanProduct;
import jakarta.validation.constraints.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Зээлийн хүсэлтийн DTO
 * Loan Application Data Transfer Object
 * ⭐ MANUAL SETTERS/GETTERS НЭМЭГДСЭН - ЗАСВАРЛАСАН ⭐
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoanApplicationDto {

    private static final Logger logger = LoggerFactory.getLogger(LoanApplicationDto.class);

    private UUID id;

    @NotBlank(message = "Хүсэлтийн дугаар заавал байх ёстой")
    @Size(max = 50, message = "Хүсэлтийн дугаар 50 тэмдэгтээс ихгүй байх ёстой")
    private String applicationNumber;

    private UUID customerId;
    private String customerName;

    private UUID loanProductId;
    private String loanProductName;

    @NotNull(message = "Зээлийн төрөл заавал сонгох ёстой")
    private LoanApplication.LoanType loanType;

    @NotNull(message = "Зээлийн дүн заавал байх ёстой")
    @DecimalMin(value = "1000.0", message = "Зээлийн дүн 1,000-аас их байх ёстой")
    private BigDecimal requestedAmount;

    @NotNull(message = "Зээлийн хугацаа заавал байх ёстой")
    @Min(value = 1, message = "Зээлийн хугацаа 1 сараас их байх ёстой")
    private Integer requestedTermMonths;

    @DecimalMin(value = "0.0", message = "Хүү сөрөг байж болохгүй")
    @DecimalMax(value = "1.0", message = "Хүү 100%-аас их байж болохгүй")
    private BigDecimal interestRate;

    @DecimalMin(value = "0.0", message = "Батлагдсан дүн сөрөг байж болохгүй")
    private BigDecimal approvedAmount;

    @Min(value = 1, message = "Батлагдсан хугацаа 1 сараас их байх ёстой")
    private Integer approvedTermMonths;

    @DecimalMin(value = "0.0", message = "Сарын төлбөр сөрөг байж болохгүй")
    private BigDecimal monthlyPayment;

    @DecimalMin(value = "0.0", message = "Нийт төлбөр сөрөг байж болохгүй")
    private BigDecimal totalPayment;

    @Size(max = 2000, message = "Хүсэлтийн зорилго 2000 тэмдэгтээс ихгүй байх ёстой")
    private String purpose;

    @Size(max = 1000, message = "Тайлбар 1000 тэмдэгтээс ихгүй байх ёстой")
    private String description;

    @DecimalMin(value = "0.0", message = "Орлого сөрөг байж болохгүй")
    private BigDecimal declaredIncome;

    @Size(max = 100, message = "Хүлээлгэх хүн 100 тэмдэгтээс ихгүй байх ёстой")
    private String assignedTo;

    @NotNull(message = "Хүсэлтийн статус заавал байх ёстой")
    private LoanApplication.ApplicationStatus status;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime submittedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime reviewedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime approvedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime rejectedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime disbursedAt;

    @Size(max = 100, message = "Хянагч 100 тэмдэгтээс ихгүй байх ёстой")
    private String reviewedBy;

    @Size(max = 100, message = "Батлагч 100 тэмдэгтээс ихгүй байх ёстой")
    private String approvedBy;

    @Size(max = 100, message = "Татгалзсан хүн 100 тэмдэгтээс ихгүй байх ёстой")
    private String rejectedBy;

    @Size(max = 1000, message = "Татгалзсан шалтгаан 1000 тэмдэгтээс ихгүй байх ёстой")
    private String rejectionReason;

    @Size(max = 1000, message = "Хянагчийн тэмдэглэл 1000 тэмдэгтээс ихгүй байх ёстой")
    private String reviewerNotes;

    @Size(max = 1000, message = "Статусын тэмдэглэл 1000 тэмдэгтээс ихгүй байх ёстой")
    private String statusNote;

    @Size(max = 1000, message = "Шийдвэрийн шалтгаан 1000 тэмдэгтээс ихгүй байх ёстой")
    private String decisionReason;

    @Min(value = 300, message = "Кредит скор 300-аас бага байж болохгүй")
    @Max(value = 850, message = "Кредит скор 850-аас их байж болохгүй")
    private Integer creditScore;

    @DecimalMin(value = "0.0", message = "Өрийн харьцаа сөрөг байж болохгүй")
    @DecimalMax(value = "1.0", message = "Өрийн харьцаа 100%-аас их байж болохгүй")
    private BigDecimal debtToIncomeRatio;

    private Boolean requiresCollateral = false;
    private Boolean requiresGuarantor = false;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expectedDisbursementDate;

    @DecimalMin(value = "0.0", message = "Боловсруулалтын шимтгэл сөрөг байж болохгүй")
    private BigDecimal processingFee;

    @DecimalMin(value = "0.0", message = "Бусад зардал сөрөг байж болохгүй")
    private BigDecimal otherCharges;

    private Integer priority = 1;

    @Size(max = 500, message = "Гэрээний нөхцөл 500 тэмдэгтээс ихгүй байх ёстой")
    private String contractTerms;

    @Size(max = 500, message = "Тусгай нөхцөл 500 тэмдэгтээс ихгүй байх ёстой")
    private String specialConditions;

    private Boolean isActive = true;

    // Assessment fields
    private String assessmentResult;
    private Integer assessmentScore;
    private String assessmentNotes;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime assessedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    private String createdBy;
    private String updatedBy;

    // Computed fields
    private String statusDisplay;
    private String loanTypeDisplay;
    private String formattedRequestedAmount;
    private String formattedApprovedAmount;
    private String formattedMonthlyPayment;
    private String formattedTotalPayment;
    private String formattedInterestRate;
    private String termText;
    private Integer daysSinceSubmitted;
    private Integer daysSinceReviewed;
    private Boolean isOverdue;
    private Boolean canBeEdited;
    private Boolean canBeApproved;
    private Boolean canBeRejected;
    private String urgencyLevel;

    // ==================== CONSTRUCTORS ====================

    public LoanApplicationDto() {
    }

    public LoanApplicationDto(UUID id, String applicationNumber, UUID customerId, String customerName,
                             UUID loanProductId, String loanProductName, LoanApplication.LoanType loanType,
                             BigDecimal requestedAmount, Integer requestedTermMonths, BigDecimal interestRate,
                             BigDecimal approvedAmount, Integer approvedTermMonths, BigDecimal monthlyPayment,
                             BigDecimal totalPayment, String purpose, String description, BigDecimal declaredIncome,
                             String assignedTo, LoanApplication.ApplicationStatus status, LocalDateTime submittedAt,
                             LocalDateTime reviewedAt, LocalDateTime approvedAt, LocalDateTime rejectedAt,
                             LocalDateTime disbursedAt, String reviewedBy, String approvedBy, String rejectedBy,
                             String rejectionReason, String reviewerNotes, String statusNote, String decisionReason,
                             Integer creditScore, BigDecimal debtToIncomeRatio, Boolean requiresCollateral,
                             Boolean requiresGuarantor, LocalDate expectedDisbursementDate, BigDecimal processingFee,
                             BigDecimal otherCharges, Integer priority, String contractTerms, String specialConditions,
                             Boolean isActive, String assessmentResult, Integer assessmentScore, String assessmentNotes,
                             LocalDateTime assessedAt, LocalDateTime createdAt, LocalDateTime updatedAt,
                             String createdBy, String updatedBy) {
        this.id = id;
        this.applicationNumber = applicationNumber;
        this.customerId = customerId;
        this.customerName = customerName;
        this.loanProductId = loanProductId;
        this.loanProductName = loanProductName;
        this.loanType = loanType;
        this.requestedAmount = requestedAmount;
        this.requestedTermMonths = requestedTermMonths;
        this.interestRate = interestRate;
        this.approvedAmount = approvedAmount;
        this.approvedTermMonths = approvedTermMonths;
        this.monthlyPayment = monthlyPayment;
        this.totalPayment = totalPayment;
        this.purpose = purpose;
        this.description = description;
        this.declaredIncome = declaredIncome;
        this.assignedTo = assignedTo;
        this.status = status;
        this.submittedAt = submittedAt;
        this.reviewedAt = reviewedAt;
        this.approvedAt = approvedAt;
        this.rejectedAt = rejectedAt;
        this.disbursedAt = disbursedAt;
        this.reviewedBy = reviewedBy;
        this.approvedBy = approvedBy;
        this.rejectedBy = rejectedBy;
        this.rejectionReason = rejectionReason;
        this.reviewerNotes = reviewerNotes;
        this.statusNote = statusNote;
        this.decisionReason = decisionReason;
        this.creditScore = creditScore;
        this.debtToIncomeRatio = debtToIncomeRatio;
        this.requiresCollateral = requiresCollateral;
        this.requiresGuarantor = requiresGuarantor;
        this.expectedDisbursementDate = expectedDisbursementDate;
        this.processingFee = processingFee;
        this.otherCharges = otherCharges;
        this.priority = priority;
        this.contractTerms = contractTerms;
        this.specialConditions = specialConditions;
        this.isActive = isActive;
        this.assessmentResult = assessmentResult;
        this.assessmentScore = assessmentScore;
        this.assessmentNotes = assessmentNotes;
        this.assessedAt = assessedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
    }

    // ==================== MANUAL GETTERS ====================

    public UUID getId() { return id; }
    public String getApplicationNumber() { return applicationNumber; }
    public UUID getCustomerId() { return customerId; }
    public String getCustomerName() { return customerName; }
    public UUID getLoanProductId() { return loanProductId; }
    public String getLoanProductName() { return loanProductName; }
    public LoanApplication.LoanType getLoanType() { return loanType; }
    public BigDecimal getRequestedAmount() { return requestedAmount; }
    public Integer getRequestedTermMonths() { return requestedTermMonths; }
    public BigDecimal getInterestRate() { return interestRate; }
    public BigDecimal getApprovedAmount() { return approvedAmount; }
    public Integer getApprovedTermMonths() { return approvedTermMonths; }
    public BigDecimal getMonthlyPayment() { return monthlyPayment; }
    public BigDecimal getTotalPayment() { return totalPayment; }
    public String getPurpose() { return purpose; }
    public String getDescription() { return description; }
    public BigDecimal getDeclaredIncome() { return declaredIncome; }
    public String getAssignedTo() { return assignedTo; }
    public LoanApplication.ApplicationStatus getStatus() { return status; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public LocalDateTime getApprovedAt() { return approvedAt; }
    public LocalDateTime getRejectedAt() { return rejectedAt; }
    public LocalDateTime getDisbursedAt() { return disbursedAt; }
    public String getReviewedBy() { return reviewedBy; }
    public String getApprovedBy() { return approvedBy; }
    public String getRejectedBy() { return rejectedBy; }
    public String getRejectionReason() { return rejectionReason; }
    public String getReviewerNotes() { return reviewerNotes; }
    public String getStatusNote() { return statusNote; }
    public String getDecisionReason() { return decisionReason; }
    public Integer getCreditScore() { return creditScore; }
    public BigDecimal getDebtToIncomeRatio() { return debtToIncomeRatio; }
    public Boolean getRequiresCollateral() { return requiresCollateral; }
    public Boolean getRequiresGuarantor() { return requiresGuarantor; }
    public LocalDate getExpectedDisbursementDate() { return expectedDisbursementDate; }
    public BigDecimal getProcessingFee() { return processingFee; }
    public BigDecimal getOtherCharges() { return otherCharges; }
    public Integer getPriority() { return priority; }
    public String getContractTerms() { return contractTerms; }
    public String getSpecialConditions() { return specialConditions; }
    public Boolean getIsActive() { return isActive; }
    public String getAssessmentResult() { return assessmentResult; }
    public Integer getAssessmentScore() { return assessmentScore; }
    public String getAssessmentNotes() { return assessmentNotes; }
    public LocalDateTime getAssessedAt() { return assessedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public String getCreatedBy() { return createdBy; }
    public String getUpdatedBy() { return updatedBy; }
    public String getStatusDisplay() { return statusDisplay; }
    public String getLoanTypeDisplay() { return loanTypeDisplay; }
    public String getFormattedRequestedAmount() { return formattedRequestedAmount; }
    public String getFormattedApprovedAmount() { return formattedApprovedAmount; }
    public String getFormattedMonthlyPayment() { return formattedMonthlyPayment; }
    public String getFormattedTotalPayment() { return formattedTotalPayment; }
    public String getFormattedInterestRate() { return formattedInterestRate; }
    public String getTermText() { return termText; }
    public Integer getDaysSinceSubmitted() { return daysSinceSubmitted; }
    public Integer getDaysSinceReviewed() { return daysSinceReviewed; }
    public Boolean getIsOverdue() { return isOverdue; }
    public Boolean getCanBeEdited() { return canBeEdited; }
    public Boolean getCanBeApproved() { return canBeApproved; }
    public Boolean getCanBeRejected() { return canBeRejected; }
    public String getUrgencyLevel() { return urgencyLevel; }

    // ==================== MANUAL SETTERS ====================

    public void setId(UUID id) { this.id = id; }
    public void setApplicationNumber(String applicationNumber) { this.applicationNumber = applicationNumber; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public void setLoanProductId(UUID loanProductId) { this.loanProductId = loanProductId; }
    public void setLoanProductName(String loanProductName) { this.loanProductName = loanProductName; }
    public void setLoanType(LoanApplication.LoanType loanType) { this.loanType = loanType; }
    public void setRequestedAmount(BigDecimal requestedAmount) { this.requestedAmount = requestedAmount; }
    public void setRequestedTermMonths(Integer requestedTermMonths) { this.requestedTermMonths = requestedTermMonths; }
    public void setInterestRate(BigDecimal interestRate) { this.interestRate = interestRate; }
    public void setApprovedAmount(BigDecimal approvedAmount) { this.approvedAmount = approvedAmount; }
    public void setApprovedTermMonths(Integer approvedTermMonths) { this.approvedTermMonths = approvedTermMonths; }
    public void setMonthlyPayment(BigDecimal monthlyPayment) { this.monthlyPayment = monthlyPayment; }
    public void setTotalPayment(BigDecimal totalPayment) { this.totalPayment = totalPayment; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
    public void setDescription(String description) { this.description = description; }
    public void setDeclaredIncome(BigDecimal declaredIncome) { this.declaredIncome = declaredIncome; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }
    public void setStatus(LoanApplication.ApplicationStatus status) { this.status = status; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }
    public void setRejectedAt(LocalDateTime rejectedAt) { this.rejectedAt = rejectedAt; }
    public void setDisbursedAt(LocalDateTime disbursedAt) { this.disbursedAt = disbursedAt; }
    public void setReviewedBy(String reviewedBy) { this.reviewedBy = reviewedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }
    public void setRejectedBy(String rejectedBy) { this.rejectedBy = rejectedBy; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    public void setReviewerNotes(String reviewerNotes) { this.reviewerNotes = reviewerNotes; }
    public void setStatusNote(String statusNote) { this.statusNote = statusNote; }
    public void setDecisionReason(String decisionReason) { this.decisionReason = decisionReason; }
    public void setCreditScore(Integer creditScore) { this.creditScore = creditScore; }
    public void setDebtToIncomeRatio(BigDecimal debtToIncomeRatio) { this.debtToIncomeRatio = debtToIncomeRatio; }
    public void setRequiresCollateral(Boolean requiresCollateral) { this.requiresCollateral = requiresCollateral; }
    public void setRequiresGuarantor(Boolean requiresGuarantor) { this.requiresGuarantor = requiresGuarantor; }
    public void setExpectedDisbursementDate(LocalDate expectedDisbursementDate) { this.expectedDisbursementDate = expectedDisbursementDate; }
    public void setProcessingFee(BigDecimal processingFee) { this.processingFee = processingFee; }
    public void setOtherCharges(BigDecimal otherCharges) { this.otherCharges = otherCharges; }
    public void setPriority(Integer priority) { this.priority = priority; }
    public void setContractTerms(String contractTerms) { this.contractTerms = contractTerms; }
    public void setSpecialConditions(String specialConditions) { this.specialConditions = specialConditions; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public void setAssessmentResult(String assessmentResult) { this.assessmentResult = assessmentResult; }
    public void setAssessmentScore(Integer assessmentScore) { this.assessmentScore = assessmentScore; }
    public void setAssessmentNotes(String assessmentNotes) { this.assessmentNotes = assessmentNotes; }
    public void setAssessedAt(LocalDateTime assessedAt) { this.assessedAt = assessedAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
    public void setStatusDisplay(String statusDisplay) { this.statusDisplay = statusDisplay; }
    public void setLoanTypeDisplay(String loanTypeDisplay) { this.loanTypeDisplay = loanTypeDisplay; }
    public void setFormattedRequestedAmount(String formattedRequestedAmount) { this.formattedRequestedAmount = formattedRequestedAmount; }
    public void setFormattedApprovedAmount(String formattedApprovedAmount) { this.formattedApprovedAmount = formattedApprovedAmount; }
    public void setFormattedMonthlyPayment(String formattedMonthlyPayment) { this.formattedMonthlyPayment = formattedMonthlyPayment; }
    public void setFormattedTotalPayment(String formattedTotalPayment) { this.formattedTotalPayment = formattedTotalPayment; }
    public void setFormattedInterestRate(String formattedInterestRate) { this.formattedInterestRate = formattedInterestRate; }
    public void setTermText(String termText) { this.termText = termText; }
    public void setDaysSinceSubmitted(Integer daysSinceSubmitted) { this.daysSinceSubmitted = daysSinceSubmitted; }
    public void setDaysSinceReviewed(Integer daysSinceReviewed) { this.daysSinceReviewed = daysSinceReviewed; }
    public void setIsOverdue(Boolean isOverdue) { this.isOverdue = isOverdue; }
    public void setCanBeEdited(Boolean canBeEdited) { this.canBeEdited = canBeEdited; }
    public void setCanBeApproved(Boolean canBeApproved) { this.canBeApproved = canBeApproved; }
    public void setCanBeRejected(Boolean canBeRejected) { this.canBeRejected = canBeRejected; }
    public void setUrgencyLevel(String urgencyLevel) { this.urgencyLevel = urgencyLevel; }

    // ==================== MANUAL BUILDER PATTERN ====================

    public static LoanApplicationDtoBuilder builder() {
        return new LoanApplicationDtoBuilder();
    }

    public static class LoanApplicationDtoBuilder {
        private LoanApplicationDto dto = new LoanApplicationDto();

        public LoanApplicationDtoBuilder id(UUID id) { dto.id = id; return this; }
        public LoanApplicationDtoBuilder applicationNumber(String applicationNumber) { dto.applicationNumber = applicationNumber; return this; }
        public LoanApplicationDtoBuilder customerId(UUID customerId) { dto.customerId = customerId; return this; }
        public LoanApplicationDtoBuilder customerName(String customerName) { dto.customerName = customerName; return this; }
        public LoanApplicationDtoBuilder loanProductId(UUID loanProductId) { dto.loanProductId = loanProductId; return this; }
        public LoanApplicationDtoBuilder loanProductName(String loanProductName) { dto.loanProductName = loanProductName; return this; }
        public LoanApplicationDtoBuilder loanType(LoanApplication.LoanType loanType) { dto.loanType = loanType; return this; }
        public LoanApplicationDtoBuilder requestedAmount(BigDecimal requestedAmount) { dto.requestedAmount = requestedAmount; return this; }
        public LoanApplicationDtoBuilder requestedTermMonths(Integer requestedTermMonths) { dto.requestedTermMonths = requestedTermMonths; return this; }
        public LoanApplicationDtoBuilder interestRate(BigDecimal interestRate) { dto.interestRate = interestRate; return this; }
        public LoanApplicationDtoBuilder approvedAmount(BigDecimal approvedAmount) { dto.approvedAmount = approvedAmount; return this; }
        public LoanApplicationDtoBuilder approvedTermMonths(Integer approvedTermMonths) { dto.approvedTermMonths = approvedTermMonths; return this; }
        public LoanApplicationDtoBuilder monthlyPayment(BigDecimal monthlyPayment) { dto.monthlyPayment = monthlyPayment; return this; }
        public LoanApplicationDtoBuilder totalPayment(BigDecimal totalPayment) { dto.totalPayment = totalPayment; return this; }
        public LoanApplicationDtoBuilder purpose(String purpose) { dto.purpose = purpose; return this; }
        public LoanApplicationDtoBuilder description(String description) { dto.description = description; return this; }
        public LoanApplicationDtoBuilder declaredIncome(BigDecimal declaredIncome) { dto.declaredIncome = declaredIncome; return this; }
        public LoanApplicationDtoBuilder assignedTo(String assignedTo) { dto.assignedTo = assignedTo; return this; }
        public LoanApplicationDtoBuilder status(LoanApplication.ApplicationStatus status) { dto.status = status; return this; }
        public LoanApplicationDtoBuilder submittedAt(LocalDateTime submittedAt) { dto.submittedAt = submittedAt; return this; }
        public LoanApplicationDtoBuilder reviewedAt(LocalDateTime reviewedAt) { dto.reviewedAt = reviewedAt; return this; }
        public LoanApplicationDtoBuilder approvedAt(LocalDateTime approvedAt) { dto.approvedAt = approvedAt; return this; }
        public LoanApplicationDtoBuilder rejectedAt(LocalDateTime rejectedAt) { dto.rejectedAt = rejectedAt; return this; }
        public LoanApplicationDtoBuilder disbursedAt(LocalDateTime disbursedAt) { dto.disbursedAt = disbursedAt; return this; }
        public LoanApplicationDtoBuilder reviewedBy(String reviewedBy) { dto.reviewedBy = reviewedBy; return this; }
        public LoanApplicationDtoBuilder approvedBy(String approvedBy) { dto.approvedBy = approvedBy; return this; }
        public LoanApplicationDtoBuilder rejectedBy(String rejectedBy) { dto.rejectedBy = rejectedBy; return this; }
        public LoanApplicationDtoBuilder rejectionReason(String rejectionReason) { dto.rejectionReason = rejectionReason; return this; }
        public LoanApplicationDtoBuilder reviewerNotes(String reviewerNotes) { dto.reviewerNotes = reviewerNotes; return this; }
        public LoanApplicationDtoBuilder statusNote(String statusNote) { dto.statusNote = statusNote; return this; }
        public LoanApplicationDtoBuilder decisionReason(String decisionReason) { dto.decisionReason = decisionReason; return this; }
        public LoanApplicationDtoBuilder creditScore(Integer creditScore) { dto.creditScore = creditScore; return this; }
        public LoanApplicationDtoBuilder debtToIncomeRatio(BigDecimal debtToIncomeRatio) { dto.debtToIncomeRatio = debtToIncomeRatio; return this; }
        public LoanApplicationDtoBuilder requiresCollateral(Boolean requiresCollateral) { dto.requiresCollateral = requiresCollateral; return this; }
        public LoanApplicationDtoBuilder requiresGuarantor(Boolean requiresGuarantor) { dto.requiresGuarantor = requiresGuarantor; return this; }
        public LoanApplicationDtoBuilder expectedDisbursementDate(LocalDate expectedDisbursementDate) { dto.expectedDisbursementDate = expectedDisbursementDate; return this; }
        public LoanApplicationDtoBuilder processingFee(BigDecimal processingFee) { dto.processingFee = processingFee; return this; }
        public LoanApplicationDtoBuilder otherCharges(BigDecimal otherCharges) { dto.otherCharges = otherCharges; return this; }
        public LoanApplicationDtoBuilder priority(Integer priority) { dto.priority = priority; return this; }
        public LoanApplicationDtoBuilder contractTerms(String contractTerms) { dto.contractTerms = contractTerms; return this; }
        public LoanApplicationDtoBuilder specialConditions(String specialConditions) { dto.specialConditions = specialConditions; return this; }
        public LoanApplicationDtoBuilder isActive(Boolean isActive) { dto.isActive = isActive; return this; }
        public LoanApplicationDtoBuilder assessmentResult(String assessmentResult) { dto.assessmentResult = assessmentResult; return this; }
        public LoanApplicationDtoBuilder assessmentScore(Integer assessmentScore) { dto.assessmentScore = assessmentScore; return this; }
        public LoanApplicationDtoBuilder assessmentNotes(String assessmentNotes) { dto.assessmentNotes = assessmentNotes; return this; }
        public LoanApplicationDtoBuilder assessedAt(LocalDateTime assessedAt) { dto.assessedAt = assessedAt; return this; }
        public LoanApplicationDtoBuilder createdAt(LocalDateTime createdAt) { dto.createdAt = createdAt; return this; }
        public LoanApplicationDtoBuilder updatedAt(LocalDateTime updatedAt) { dto.updatedAt = updatedAt; return this; }
        public LoanApplicationDtoBuilder createdBy(String createdBy) { dto.createdBy = createdBy; return this; }
        public LoanApplicationDtoBuilder updatedBy(String updatedBy) { dto.updatedBy = updatedBy; return this; }

        public LoanApplicationDto build() { return dto; }
    }

    // Static factory methods
    public static LoanApplicationDto fromEntity(LoanApplication loanApplication) {
        if (loanApplication == null) {
            return null;
        }

        LoanApplicationDto dto = LoanApplicationDto.builder()
                .id(loanApplication.getId())
                .applicationNumber(loanApplication.getApplicationNumber())
                .loanType(loanApplication.getLoanType())
                .requestedAmount(loanApplication.getRequestedAmount())
                .requestedTermMonths(loanApplication.getRequestedTermMonths())
                .interestRate(loanApplication.getInterestRate())
                .approvedAmount(loanApplication.getApprovedAmount())
                .approvedTermMonths(loanApplication.getApprovedTermMonths())
                .monthlyPayment(loanApplication.getMonthlyPayment())
                .totalPayment(loanApplication.getTotalPayment())
                .purpose(loanApplication.getPurpose())
                .description(loanApplication.getDescription())
                .declaredIncome(loanApplication.getDeclaredIncome())
                .assignedTo(loanApplication.getAssignedTo())
                .status(loanApplication.getStatus())
                .submittedAt(loanApplication.getSubmittedAt())
                .reviewedAt(loanApplication.getReviewedAt())
                .approvedAt(loanApplication.getApprovedAt())
                .rejectedAt(loanApplication.getRejectedAt())
                .disbursedAt(loanApplication.getDisbursedAt())
                .reviewedBy(loanApplication.getReviewedBy())
                .approvedBy(loanApplication.getApprovedBy())
                .rejectedBy(loanApplication.getRejectedBy())
                .rejectionReason(loanApplication.getRejectionReason())
                .reviewerNotes(loanApplication.getReviewerNotes())
                .statusNote(loanApplication.getStatusNote())
                .decisionReason(loanApplication.getDecisionReason())
                .creditScore(loanApplication.getCreditScore())
                .debtToIncomeRatio(loanApplication.getDebtToIncomeRatio())
                .requiresCollateral(loanApplication.getRequiresCollateral())
                .requiresGuarantor(loanApplication.getRequiresGuarantor())
                .expectedDisbursementDate(loanApplication.getExpectedDisbursementDate())
                .processingFee(loanApplication.getProcessingFee())
                .otherCharges(loanApplication.getOtherCharges())
                .priority(loanApplication.getPriority())
                .contractTerms(loanApplication.getContractTerms())
                .specialConditions(loanApplication.getSpecialConditions())
                .isActive(loanApplication.getIsActive())
                .assessmentResult(loanApplication.getAssessmentResult())
                .assessmentScore(loanApplication.getAssessmentScore())
                .assessmentNotes(loanApplication.getAssessmentNotes())
                .assessedAt(loanApplication.getAssessedAt())
                .createdAt(loanApplication.getCreatedAt())
                .updatedAt(loanApplication.getUpdatedAt())
                .createdBy(loanApplication.getCreatedBy())
                .updatedBy(loanApplication.getUpdatedBy())
                .build();

        // Set customer info
        if (loanApplication.getCustomer() != null) {
            dto.setCustomerId(loanApplication.getCustomer().getId());
            dto.setCustomerName(loanApplication.getCustomer().getDisplayName());
        }

        // Set loan product info
        if (loanApplication.getLoanProduct() != null) {
            dto.setLoanProductId(loanApplication.getLoanProduct().getId());
            dto.setLoanProductName(loanApplication.getLoanProduct().getName());
        }

        // Set computed fields
        dto.setStatusDisplay(dto.calculateStatusDisplay());
        dto.setLoanTypeDisplay(dto.calculateLoanTypeDisplay());
        dto.setFormattedRequestedAmount(dto.formatAmount(dto.getRequestedAmount()));
        dto.setFormattedApprovedAmount(dto.formatAmount(dto.getApprovedAmount()));
        dto.setFormattedMonthlyPayment(dto.formatAmount(dto.getMonthlyPayment()));
        dto.setFormattedTotalPayment(dto.formatAmount(dto.getTotalPayment()));
        dto.setFormattedInterestRate(dto.formatRate(dto.getInterestRate()));
        dto.setTermText(dto.calculateTermText());
        dto.setDaysSinceSubmitted(dto.calculateDaysSinceSubmitted());
        dto.setDaysSinceReviewed(dto.calculateDaysSinceReviewed());
        dto.setIsOverdue(dto.calculateIsOverdue());
        dto.setCanBeEdited(dto.calculateCanBeEdited());
        dto.setCanBeApproved(dto.calculateCanBeApproved());
        dto.setCanBeRejected(dto.calculateCanBeRejected());
        dto.setUrgencyLevel(dto.calculateUrgencyLevel());

        return dto;
    }

    public LoanApplication toEntity() {
        LoanApplication loanApplication = new LoanApplication();

        loanApplication.setId(this.id);
        loanApplication.setApplicationNumber(this.applicationNumber);
        loanApplication.setLoanType(this.loanType);
        loanApplication.setRequestedAmount(this.requestedAmount);
        loanApplication.setRequestedTermMonths(this.requestedTermMonths);
        loanApplication.setInterestRate(this.interestRate);
        loanApplication.setApprovedAmount(this.approvedAmount);
        loanApplication.setApprovedTermMonths(this.approvedTermMonths);
        loanApplication.setMonthlyPayment(this.monthlyPayment);
        loanApplication.setTotalPayment(this.totalPayment);
        loanApplication.setPurpose(this.purpose);
        loanApplication.setDescription(this.description);
        loanApplication.setDeclaredIncome(this.declaredIncome);
        loanApplication.setAssignedTo(this.assignedTo);
        loanApplication.setStatus(this.status);
        loanApplication.setSubmittedAt(this.submittedAt);
        loanApplication.setReviewedAt(this.reviewedAt);
        loanApplication.setApprovedAt(this.approvedAt);
        loanApplication.setRejectedAt(this.rejectedAt);
        loanApplication.setDisbursedAt(this.disbursedAt);
        loanApplication.setReviewedBy(this.reviewedBy);
        loanApplication.setApprovedBy(this.approvedBy);
        loanApplication.setRejectedBy(this.rejectedBy);
        loanApplication.setRejectionReason(this.rejectionReason);
        loanApplication.setReviewerNotes(this.reviewerNotes);
        loanApplication.setStatusNote(this.statusNote);
        loanApplication.setDecisionReason(this.decisionReason);
        loanApplication.setCreditScore(this.creditScore);
        loanApplication.setDebtToIncomeRatio(this.debtToIncomeRatio);
        loanApplication.setRequiresCollateral(this.requiresCollateral);
        loanApplication.setRequiresGuarantor(this.requiresGuarantor);
        loanApplication.setExpectedDisbursementDate(this.expectedDisbursementDate);
        loanApplication.setProcessingFee(this.processingFee);
        loanApplication.setOtherCharges(this.otherCharges);
        loanApplication.setPriority(this.priority);
        loanApplication.setContractTerms(this.contractTerms);
        loanApplication.setSpecialConditions(this.specialConditions);
        loanApplication.setIsActive(this.isActive);
        loanApplication.setAssessmentResult(this.assessmentResult);
        loanApplication.setAssessmentScore(this.assessmentScore);
        loanApplication.setAssessmentNotes(this.assessmentNotes);
        loanApplication.setAssessedAt(this.assessedAt);
        loanApplication.setCreatedAt(this.createdAt);
        loanApplication.setUpdatedAt(this.updatedAt);
        loanApplication.setCreatedBy(this.createdBy);
        loanApplication.setUpdatedBy(this.updatedBy);

        // NOTE: customer and loanProduct are not set here as they are typically handled by a service layer
        // that fetches the full entity from the database. This DTO to entity conversion is for saving/updating
        // the core application data.

        return loanApplication;
    }

    // Helper methods for computed fields
    private String calculateStatusDisplay() {
        if (status == null) return "Тодорхойгүй";
        switch (status) {
            case DRAFT: return "Ноорог";
            case SUBMITTED: return "Илгээсэн";
            case PENDING: return "Хүлээгдэж байгаа";
            case UNDER_REVIEW: return "Хянаж байна";
            case PENDING_DOCUMENTS: return "Баримт хүлээж байна";
            case APPROVED: return "Батлагдсан";
            case REJECTED: return "Татгалзсан";
            case DISBURSED: return "Олгосон";
            case CANCELLED: return "Цуцалсан";
            default: return status.toString();
        }
    }

    private String calculateLoanTypeDisplay() {
        if (loanType == null) return "Тодорхойгүй";
        return loanType.getMongolianName();
    }

    private String formatAmount(BigDecimal amount) {
        return amount != null ? String.format("%,.0f₮", amount) : "";
    }

    private String formatRate(BigDecimal rate) {
        return rate != null ? String.format("%.2f%%", rate.multiply(BigDecimal.valueOf(100))) : "";
    }

    private String calculateTermText() {
        return requestedTermMonths != null ? requestedTermMonths + " сар" : "";
    }

    private Integer calculateDaysSinceSubmitted() {
        if (submittedAt == null) return null;
        return (int) java.time.Duration.between(submittedAt, LocalDateTime.now()).toDays();
    }

    private Integer calculateDaysSinceReviewed() {
        if (reviewedAt == null) return null;
        return (int) java.time.Duration.between(reviewedAt, LocalDateTime.now()).toDays();
    }

    private Boolean calculateIsOverdue() {
        // Define overdue logic based on business rules
        if (status == LoanApplication.ApplicationStatus.UNDER_REVIEW && submittedAt != null) {
            return java.time.Duration.between(submittedAt, LocalDateTime.now()).toDays() > 7; // 7 days threshold
        }
        return false;
    }

    private Boolean calculateCanBeEdited() {
        return status == LoanApplication.ApplicationStatus.DRAFT ||
               status == LoanApplication.ApplicationStatus.PENDING_DOCUMENTS;
    }

    private Boolean calculateCanBeApproved() {
        return status == LoanApplication.ApplicationStatus.UNDER_REVIEW;
    }

    private Boolean calculateCanBeRejected() {
        return status == LoanApplication.ApplicationStatus.UNDER_REVIEW ||
               status == LoanApplication.ApplicationStatus.PENDING_DOCUMENTS;
    }

    private String calculateUrgencyLevel() {
        if (priority != null && priority >= 5) return "Маш яаралтай";
        if (priority != null && priority >= 3) return "Яаралтай";
        if (calculateIsOverdue() != null && calculateIsOverdue()) return "Хугацаа хэтэрсэн";
        return "Энгийн";
    }

    // Business logic methods
    public boolean isValidApplication() {
        return customerId != null &&
               loanProductId != null &&
               requestedAmount != null && requestedAmount.compareTo(BigDecimal.ZERO) > 0 &&
               requestedTermMonths != null && requestedTermMonths > 0 &&
               applicationNumber != null && !applicationNumber.trim().isEmpty();
    }

    public boolean canSubmit() {
        return status == LoanApplication.ApplicationStatus.DRAFT && isValidApplication();
    }

    public boolean isInProgress() {
        return status == LoanApplication.ApplicationStatus.SUBMITTED ||
               status == LoanApplication.ApplicationStatus.UNDER_REVIEW ||
               status == LoanApplication.ApplicationStatus.PENDING_DOCUMENTS;
    }

    public boolean isCompleted() {
        return status == LoanApplication.ApplicationStatus.APPROVED ||
               status == LoanApplication.ApplicationStatus.REJECTED ||
               status == LoanApplication.ApplicationStatus.DISBURSED ||
               status == LoanApplication.ApplicationStatus.CANCELLED;
    }

    public String getStatusBadgeClass() {
        if (status == null) return "badge-secondary";
        switch (status) {
            case DRAFT: return "badge-secondary";
            case SUBMITTED: return "badge-info";
            case PENDING: return "badge-warning";
            case UNDER_REVIEW: return "badge-warning";
            case PENDING_DOCUMENTS: return "badge-warning";
            case APPROVED: return "badge-success";
            case REJECTED: return "badge-danger";
            case DISBURSED: return "badge-primary";
            case CANCELLED: return "badge-dark";
            default: return "badge-secondary";
        }
    }

    public String getPriorityBadgeClass() {
        if (priority == null) return "badge-secondary";
        if (priority >= 5) return "badge-danger";
        if (priority >= 3) return "badge-warning";
        return "badge-info";
    }

    @Override
    public String toString() {
        return "LoanApplicationDto{" +
                "id=" + id +
                ", applicationNumber='" + applicationNumber + '\'' +
                ", customerId=" + customerId +
                ", loanProductId=" + loanProductId +
                ", requestedAmount=" + requestedAmount +
                ", status=" + status +
                '}';
    }
}