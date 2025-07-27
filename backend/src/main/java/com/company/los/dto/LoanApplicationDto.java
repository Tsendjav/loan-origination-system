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

    // Constructors
    public LoanApplicationDto() {
        this.status = LoanApplication.ApplicationStatus.DRAFT;
        this.requiresCollateral = false;
        this.requiresGuarantor = false;
        this.priority = 1;
        this.isActive = true;
    }

    public LoanApplicationDto(String applicationNumber, UUID customerId, UUID loanProductId,
                             BigDecimal requestedAmount, Integer requestedTermMonths) {
        this();
        this.applicationNumber = applicationNumber;
        this.customerId = customerId;
        this.loanProductId = loanProductId;
        this.requestedAmount = requestedAmount;
        this.requestedTermMonths = requestedTermMonths;
    }

    // Static factory methods
    public static LoanApplicationDto fromEntity(LoanApplication loanApplication) {
        if (loanApplication == null) {
            return null;
        }

        LoanApplicationDto dto = new LoanApplicationDto();

        // ID is already UUID in BaseEntity, no conversion needed
        dto.setId(loanApplication.getId());
        dto.setApplicationNumber(loanApplication.getApplicationNumber());
        dto.setLoanType(loanApplication.getLoanType());
        dto.setRequestedAmount(loanApplication.getRequestedAmount());
        dto.setRequestedTermMonths(loanApplication.getRequestedTermMonths());
        dto.setInterestRate(loanApplication.getInterestRate());
        dto.setMonthlyPayment(loanApplication.getMonthlyPayment());
        dto.setTotalPayment(loanApplication.getTotalPayment());
        dto.setPurpose(loanApplication.getPurpose());
        dto.setDescription(loanApplication.getDescription());
        dto.setDeclaredIncome(loanApplication.getDeclaredIncome());
        dto.setAssignedTo(loanApplication.getAssignedTo());
        dto.setStatus(loanApplication.getStatus());
        dto.setSubmittedAt(loanApplication.getSubmittedAt());
        dto.setReviewedAt(loanApplication.getReviewedAt());
        dto.setApprovedAt(loanApplication.getApprovedAt());
        dto.setRejectedAt(loanApplication.getRejectedAt());
        dto.setDisbursedAt(loanApplication.getDisbursedAt());
        dto.setReviewedBy(loanApplication.getReviewedBy());
        dto.setApprovedBy(loanApplication.getApprovedBy());
        dto.setRejectedBy(loanApplication.getRejectedBy());
        dto.setRejectionReason(loanApplication.getRejectionReason());
        dto.setReviewerNotes(loanApplication.getReviewerNotes());
        dto.setDecisionReason(loanApplication.getDecisionReason());
        dto.setCreditScore(loanApplication.getCreditScore());
        dto.setDebtToIncomeRatio(loanApplication.getDebtToIncomeRatio());
        dto.setRequiresCollateral(loanApplication.getRequiresCollateral());
        dto.setRequiresGuarantor(loanApplication.getRequiresGuarantor());
        dto.setExpectedDisbursementDate(loanApplication.getExpectedDisbursementDate());
        dto.setProcessingFee(loanApplication.getProcessingFee());
        dto.setOtherCharges(loanApplication.getOtherCharges());
        dto.setPriority(loanApplication.getPriority());
        dto.setContractTerms(loanApplication.getContractTerms());
        dto.setSpecialConditions(loanApplication.getSpecialConditions());
        dto.setIsActive(loanApplication.getIsActive());
        dto.setCreatedAt(loanApplication.getCreatedAt());
        dto.setUpdatedAt(loanApplication.getUpdatedAt());

        // Audit fields are String in both DTO and BaseEntity
        dto.setCreatedBy(loanApplication.getCreatedBy());
        dto.setUpdatedBy(loanApplication.getUpdatedBy());

        // Safe customer ID extraction - Customer entity has UUID id
        if (loanApplication.getCustomer() != null && loanApplication.getCustomer().getId() != null) {
            dto.setCustomerId(loanApplication.getCustomer().getId());
            dto.setCustomerName(loanApplication.getCustomer().getDisplayName());
        }

        // Safe loan product ID extraction - LoanProduct entity has UUID id
        if (loanApplication.getLoanProduct() != null && loanApplication.getLoanProduct().getId() != null) {
            dto.setLoanProductId(loanApplication.getLoanProduct().getId());
            dto.setLoanProductName(loanApplication.getLoanProduct().getName());
        }

        // Set computed fields
        dto.setStatusDisplay(dto.calculateStatusDisplay());
        dto.setLoanTypeDisplay(dto.calculateLoanTypeDisplay());
        dto.setFormattedRequestedAmount(dto.formatAmount(dto.getRequestedAmount()));
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

    public static LoanApplicationDto createSummary(LoanApplication loanApplication) {
        if (loanApplication == null) {
            return null;
        }

        LoanApplicationDto dto = new LoanApplicationDto();

        // ID is already UUID in BaseEntity, no conversion needed
        dto.setId(loanApplication.getId());
        dto.setApplicationNumber(loanApplication.getApplicationNumber());
        dto.setLoanType(loanApplication.getLoanType());
        dto.setRequestedAmount(loanApplication.getRequestedAmount());
        dto.setRequestedTermMonths(loanApplication.getRequestedTermMonths());
        dto.setStatus(loanApplication.getStatus());
        dto.setSubmittedAt(loanApplication.getSubmittedAt());

        // Safe customer ID extraction - Customer entity has UUID id
        if (loanApplication.getCustomer() != null && loanApplication.getCustomer().getId() != null) {
            dto.setCustomerId(loanApplication.getCustomer().getId());
            dto.setCustomerName(loanApplication.getCustomer().getDisplayName());
        }

        // Safe loan product ID extraction - LoanProduct entity has UUID id
        if (loanApplication.getLoanProduct() != null && loanApplication.getLoanProduct().getId() != null) {
            dto.setLoanProductId(loanApplication.getLoanProduct().getId());
            dto.setLoanProductName(loanApplication.getLoanProduct().getName());
        }

        // Set basic computed fields
        dto.setStatusDisplay(dto.calculateStatusDisplay());
        dto.setLoanTypeDisplay(dto.calculateLoanTypeDisplay());
        dto.setFormattedRequestedAmount(dto.formatAmount(dto.getRequestedAmount()));

        return dto;
    }

    public LoanApplication toEntity() {
        LoanApplication loanApplication = new LoanApplication();

        // ID is UUID in both DTO and entity, no conversion needed
        loanApplication.setId(this.id);
        loanApplication.setApplicationNumber(this.applicationNumber);
        loanApplication.setLoanType(this.loanType);
        loanApplication.setRequestedAmount(this.requestedAmount);
        loanApplication.setRequestedTermMonths(this.requestedTermMonths);
        loanApplication.setInterestRate(this.interestRate);
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
        loanApplication.setCreatedAt(this.createdAt);
        loanApplication.setUpdatedAt(this.updatedAt);

        // Audit fields are String in both DTO and BaseEntity
        loanApplication.setCreatedBy(this.createdBy);
        loanApplication.setUpdatedBy(this.updatedBy);

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

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getApplicationNumber() { return applicationNumber; }
    public void setApplicationNumber(String applicationNumber) { this.applicationNumber = applicationNumber; }

    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public UUID getLoanProductId() { return loanProductId; }
    public void setLoanProductId(UUID loanProductId) { this.loanProductId = loanProductId; }

    public String getLoanProductName() { return loanProductName; }
    public void setLoanProductName(String loanProductName) { this.loanProductName = loanProductName; }

    public LoanApplication.LoanType getLoanType() { return loanType; }
    public void setLoanType(LoanApplication.LoanType loanType) { this.loanType = loanType; }

    public BigDecimal getRequestedAmount() { return requestedAmount; }
    public void setRequestedAmount(BigDecimal requestedAmount) { this.requestedAmount = requestedAmount; }

    public Integer getRequestedTermMonths() { return requestedTermMonths; }
    public void setRequestedTermMonths(Integer requestedTermMonths) { this.requestedTermMonths = requestedTermMonths; }

    public BigDecimal getInterestRate() { return interestRate; }
    public void setInterestRate(BigDecimal interestRate) { this.interestRate = interestRate; }

    public BigDecimal getMonthlyPayment() { return monthlyPayment; }
    public void setMonthlyPayment(BigDecimal monthlyPayment) { this.monthlyPayment = monthlyPayment; }

    public BigDecimal getTotalPayment() { return totalPayment; }
    public void setTotalPayment(BigDecimal totalPayment) { this.totalPayment = totalPayment; }

    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getDeclaredIncome() { return declaredIncome; }
    public void setDeclaredIncome(BigDecimal declaredIncome) { this.declaredIncome = declaredIncome; }

    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }

    public LoanApplication.ApplicationStatus getStatus() { return status; }
    public void setStatus(LoanApplication.ApplicationStatus status) { this.status = status; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }

    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }

    public LocalDateTime getRejectedAt() { return rejectedAt; }
    public void setRejectedAt(LocalDateTime rejectedAt) { this.rejectedAt = rejectedAt; }

    public LocalDateTime getDisbursedAt() { return disbursedAt; }
    public void setDisbursedAt(LocalDateTime disbursedAt) { this.disbursedAt = disbursedAt; }

    public String getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(String reviewedBy) { this.reviewedBy = reviewedBy; }

    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }

    public String getRejectedBy() { return rejectedBy; }
    public void setRejectedBy(String rejectedBy) { this.rejectedBy = rejectedBy; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public String getReviewerNotes() { return reviewerNotes; }
    public void setReviewerNotes(String reviewerNotes) { this.reviewerNotes = reviewerNotes; }

    public String getDecisionReason() { return decisionReason; }
    public void setDecisionReason(String decisionReason) { this.decisionReason = decisionReason; }

    public Integer getCreditScore() { return creditScore; }
    public void setCreditScore(Integer creditScore) { this.creditScore = creditScore; }

    public BigDecimal getDebtToIncomeRatio() { return debtToIncomeRatio; }
    public void setDebtToIncomeRatio(BigDecimal debtToIncomeRatio) { this.debtToIncomeRatio = debtToIncomeRatio; }

    public Boolean getRequiresCollateral() { return requiresCollateral; }
    public void setRequiresCollateral(Boolean requiresCollateral) { this.requiresCollateral = requiresCollateral; }

    public Boolean getRequiresGuarantor() { return requiresGuarantor; }
    public void setRequiresGuarantor(Boolean requiresGuarantor) { this.requiresGuarantor = requiresGuarantor; }

    public LocalDate getExpectedDisbursementDate() { return expectedDisbursementDate; }
    public void setExpectedDisbursementDate(LocalDate expectedDisbursementDate) { this.expectedDisbursementDate = expectedDisbursementDate; }

    public BigDecimal getProcessingFee() { return processingFee; }
    public void setProcessingFee(BigDecimal processingFee) { this.processingFee = processingFee; }

    public BigDecimal getOtherCharges() { return otherCharges; }
    public void setOtherCharges(BigDecimal otherCharges) { this.otherCharges = otherCharges; }

    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }

    public String getContractTerms() { return contractTerms; }
    public void setContractTerms(String contractTerms) { this.contractTerms = contractTerms; }

    public String getSpecialConditions() { return specialConditions; }
    public void setSpecialConditions(String specialConditions) { this.specialConditions = specialConditions; }

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

    public String getStatusDisplay() { return statusDisplay; }
    public void setStatusDisplay(String statusDisplay) { this.statusDisplay = statusDisplay; }

    public String getLoanTypeDisplay() { return loanTypeDisplay; }
    public void setLoanTypeDisplay(String loanTypeDisplay) { this.loanTypeDisplay = loanTypeDisplay; }

    public String getFormattedRequestedAmount() { return formattedRequestedAmount; }
    public void setFormattedRequestedAmount(String formattedRequestedAmount) { this.formattedRequestedAmount = formattedRequestedAmount; }

    public String getFormattedMonthlyPayment() { return formattedMonthlyPayment; }
    public void setFormattedMonthlyPayment(String formattedMonthlyPayment) { this.formattedMonthlyPayment = formattedMonthlyPayment; }

    public String getFormattedTotalPayment() { return formattedTotalPayment; }
    public void setFormattedTotalPayment(String formattedTotalPayment) { this.formattedTotalPayment = formattedTotalPayment; }

    public String getFormattedInterestRate() { return formattedInterestRate; }
    public void setFormattedInterestRate(String formattedInterestRate) { this.formattedInterestRate = formattedInterestRate; }

    public String getTermText() { return termText; }
    public void setTermText(String termText) { this.termText = termText; }

    public Integer getDaysSinceSubmitted() { return daysSinceSubmitted; }
    public void setDaysSinceSubmitted(Integer daysSinceSubmitted) { this.daysSinceSubmitted = daysSinceSubmitted; }

    public Integer getDaysSinceReviewed() { return daysSinceReviewed; }
    public void setDaysSinceReviewed(Integer daysSinceReviewed) { this.daysSinceReviewed = daysSinceReviewed; }

    public Boolean getIsOverdue() { return isOverdue; }
    public void setIsOverdue(Boolean isOverdue) { this.isOverdue = isOverdue; }

    public Boolean getCanBeEdited() { return canBeEdited; }
    public void setCanBeEdited(Boolean canBeEdited) { this.canBeEdited = canBeEdited; }

    public Boolean getCanBeApproved() { return canBeApproved; }
    public void setCanBeApproved(Boolean canBeApproved) { this.canBeApproved = canBeApproved; }

    public Boolean getCanBeRejected() { return canBeRejected; }
    public void setCanBeRejected(Boolean canBeRejected) { this.canBeRejected = canBeRejected; }

    public String getUrgencyLevel() { return urgencyLevel; }
    public void setUrgencyLevel(String urgencyLevel) { this.urgencyLevel = urgencyLevel; }

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