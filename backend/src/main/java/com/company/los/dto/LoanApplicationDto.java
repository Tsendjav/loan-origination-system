package com.company.los.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.company.los.entity.LoanApplication;
import jakarta.validation.constraints.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
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

    @NotNull(message = "Харилцагч заавал байх ёстой")
    private UUID customerId;

    private UUID loanProductId;

    @NotBlank(message = "Хүсэлтийн дугаар заавал байх ёстой")
    @Size(max = 50, message = "Хүсэлтийн дугаар 50 тэмдэгтээс ихгүй байх ёстой")
    private String applicationNumber;

    @NotNull(message = "Зээлийн төрөл заавал сонгох ёстой")
    private LoanApplication.LoanType loanType;

    // Хүсэлтийн мэдээлэл
    @NotNull(message = "Хүсэх дүн заавал бөглөх ёстой")
    @DecimalMin(value = "1000.0", message = "Хүсэх дүн 1,000-аас их байх ёстой")
    private BigDecimal requestedAmount;

    @NotNull(message = "Хүсэх хугацаа заавал бөглөх ёстой")
    @Min(value = 1, message = "Хүсэх хугацаа 1 сараас их байх ёстой")
    @Max(value = 360, message = "Хүсэх хугацаа 360 сараас бага байх ёстой")
    private Integer requestedTermMonths;

    @Size(max = 1000, message = "Зорилго 1000 тэмдэгтээс ихгүй байх ёстой")
    private String purpose;

    // Зөвшөөрөгдсөн мэдээлэл
    @DecimalMin(value = "0.0", message = "Зөвшөөрсөн дүн сөрөг байж болохгүй")
    private BigDecimal approvedAmount;

    @Min(value = 1, message = "Зөвшөөрсөн хугацаа 1 сараас их байх ёстой")
    private Integer approvedTermMonths;

    @DecimalMin(value = "0.0", message = "Хүү сөрөг байж болохгүй")
    @DecimalMax(value = "1.0", message = "Хүү 100%-аас их байж болохгүй")
    private BigDecimal approvedRate;

    @DecimalMin(value = "0.0", message = "Сарын төлбөр сөрөг байж болохгүй")
    private BigDecimal monthlyPayment;

    // Санхүүгийн мэдээлэл
    @DecimalMin(value = "0.0", message = "Мэдүүлсэн орлого сөрөг байж болохгүй")
    private BigDecimal declaredIncome;

    @DecimalMin(value = "0.0", message = "Өр орлогын харьцаа сөрөг байж болохгүй")
    private BigDecimal debtToIncomeRatio;

    @Min(value = 300, message = "Зээлийн оноо 300-аас бага байж болохгүй")
    @Max(value = 850, message = "Зээлийн оноо 850-аас их байж болохгүй")
    private Integer creditScore;

    // Статус болон ажлын урсгал
    @NotNull(message = "Статус заавал байх ёстой")
    private LoanApplication.ApplicationStatus status;

    @Size(max = 100, message = "Одоогийн алхам 100 тэмдэгтээс ихгүй байх ёстой")
    private String currentStep;

    @Size(max = 100, message = "Хариуцагч 100 тэмдэгтээс ихгүй байх ёстой")
    private String assignedTo;

    @Min(value = 1, message = "Чухал байдал 1-ээс бага байж болохгүй")
    @Max(value = 5, message = "Чухал байдал 5-аас их байж болохгүй")
    private Integer priority;

    // Шийдвэрийн мэдээлэл
    @Size(max = 2000, message = "Шийдвэрийн үндэслэл 2000 тэмдэгтээс ихгүй байх ёстой")
    private String decisionReason;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime decisionDate;

    @Size(max = 100, message = "Зөвшөөрсөн хүн 100 тэмдэгтээс ихгүй байх ёстой")
    private String approvedBy;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime approvedDate;

    @Size(max = 100, message = "Татгалзсан хүн 100 тэмдэгтээс ихгүй байх ёстой")
    private String rejectedBy;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime rejectedDate;

    // Олголт
    @DecimalMin(value = "0.0", message = "Олгосон дүн сөрөг байж болохгүй")
    private BigDecimal disbursedAmount;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime disbursedDate;

    @Size(max = 100, message = "Олгосон хүн 100 тэмдэгтээс ихгүй байх ёстой")
    private String disbursedBy;

    // Эрсдэлийн үнэлгээ
    @DecimalMin(value = "0.0", message = "Эрсдэлийн оноо сөрөг байж болохгүй")
    @DecimalMax(value = "100.0", message = "Эрсдэлийн оноо 100-аас их байж болохгүй")
    private BigDecimal riskScore;

    @Size(max = 2000, message = "Эрсдэлийн хүчин зүйл 2000 тэмдэгтээс ихгүй байх ёстой")
    private String riskFactors;

    // Чухал огноонууд
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime submittedDate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dueDate;

    // Metadata
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    private String createdBy;
    private String updatedBy;

    // Related objects (summary)
    private CustomerDto customer;
    private LoanProductDto loanProduct;

    // Computed fields (read-only)
    private String statusDisplay;
    private String loanTypeDisplay;
    private String priorityText;
    private Boolean isSubmitted;
    private Boolean isApproved;
    private Boolean isRejected;
    private Boolean canBeEdited;
    private String formattedRequestedAmount;
    private String formattedApprovedAmount;
    private String formattedMonthlyPayment;
    private Integer processingDays;
    private String riskLevel;

    // Constructors
    public LoanApplicationDto() {
        this.status = LoanApplication.ApplicationStatus.DRAFT;
        this.priority = 3;
    }

    public LoanApplicationDto(UUID customerId, LoanApplication.LoanType loanType, 
                             BigDecimal requestedAmount, Integer requestedTermMonths) {
        this();
        this.customerId = customerId;
        this.loanType = loanType;
        this.requestedAmount = requestedAmount;
        this.requestedTermMonths = requestedTermMonths;
    }

    // Static factory methods
    public static LoanApplicationDto fromEntity(LoanApplication loanApplication) {
        if (loanApplication == null) {
            return null;
        }
        
        LoanApplicationDto dto = new LoanApplicationDto();
        
        // Convert String ID to UUID safely
        try {
            if (loanApplication.getId() != null) {
                dto.setId(
                    (UUID) (loanApplication.getId() instanceof UUID ?
                        loanApplication.getId() :
                        UUID.fromString(loanApplication.getId().toString())
                    )
                );
            }
        } catch (Exception e) {
            // If conversion fails, keep null
        }
        
        dto.setApplicationNumber(loanApplication.getApplicationNumber());
        dto.setLoanType(loanApplication.getLoanType());
        dto.setRequestedAmount(loanApplication.getRequestedAmount());
        dto.setRequestedTermMonths(loanApplication.getRequestedTermMonths());
        dto.setPurpose(loanApplication.getPurpose());
        dto.setApprovedAmount(loanApplication.getApprovedAmount());
        dto.setApprovedTermMonths(loanApplication.getApprovedTermMonths());
        dto.setApprovedRate(loanApplication.getApprovedRate());
        dto.setMonthlyPayment(loanApplication.getMonthlyPayment());
        dto.setDeclaredIncome(loanApplication.getDeclaredIncome());
        dto.setDebtToIncomeRatio(loanApplication.getDebtToIncomeRatio());
        dto.setCreditScore(loanApplication.getCreditScore());
        dto.setStatus(loanApplication.getStatus());
        dto.setCurrentStep(loanApplication.getCurrentStep());
        dto.setAssignedTo(loanApplication.getAssignedTo());
        dto.setPriority(loanApplication.getPriority());
        dto.setDecisionReason(loanApplication.getDecisionReason());
        dto.setDecisionDate(loanApplication.getDecisionDate());
        dto.setApprovedBy(loanApplication.getApprovedBy());
        dto.setApprovedDate(loanApplication.getApprovedDate());
        dto.setRejectedBy(loanApplication.getRejectedBy());
        dto.setRejectedDate(loanApplication.getRejectedDate());
        dto.setDisbursedAmount(loanApplication.getDisbursedAmount());
        dto.setDisbursedDate(loanApplication.getDisbursedDate());
        dto.setDisbursedBy(loanApplication.getDisbursedBy());
        dto.setRiskScore(loanApplication.getRiskScore());
        dto.setRiskFactors(loanApplication.getRiskFactors());
        dto.setSubmittedDate(loanApplication.getSubmittedDate());
        dto.setDueDate(loanApplication.getDueDate());
        dto.setCreatedAt(loanApplication.getCreatedAt());
        dto.setUpdatedAt(loanApplication.getUpdatedAt());
        dto.setCreatedBy(loanApplication.getCreatedBy());
        dto.setUpdatedBy(loanApplication.getUpdatedBy());
        
        // Set related object IDs with safe conversion
        if (loanApplication.getCustomer() != null && loanApplication.getCustomer().getId() != null) {
            try {
                Object customerIdObj = loanApplication.getCustomer().getId();
                UUID customerIdConverted = (UUID) (customerIdObj instanceof UUID ?
                    customerIdObj :
                    UUID.fromString(customerIdObj.toString())
                );
                dto.setCustomerId(customerIdConverted);
            } catch (Exception e) {
                // If conversion fails, keep null or log more specific error
                logger.warn("Error converting customerId to UUID: {}", loanApplication.getCustomer().getId(), e);
                dto.setCustomerId(null);
            }
        }
        
        if (loanApplication.getLoanProduct() != null && loanApplication.getLoanProduct().getId() != null) {
            try {
                Object loanProductIdObj = loanApplication.getLoanProduct().getId();
                UUID loanProductIdConverted = (UUID) (loanProductIdObj instanceof UUID ?
                    loanProductIdObj :
                    UUID.fromString(loanProductIdObj.toString())
                );
                dto.setLoanProductId(loanProductIdConverted);
            } catch (Exception e) {
                // If conversion fails, keep null or log more specific error
                logger.warn("Error converting loanProductId to UUID: {}", loanApplication.getLoanProduct().getId(), e);
                dto.setLoanProductId(null);
            }
        }

        // Set computed fields
        dto.setStatusDisplay(loanApplication.getStatusDisplay());
        dto.setLoanTypeDisplay(loanApplication.getLoanTypeDisplay());
        dto.setPriorityText(dto.calculatePriorityText());
        dto.setIsSubmitted(loanApplication.isSubmitted());
        dto.setIsApproved(loanApplication.isApproved());
        dto.setIsRejected(loanApplication.isRejected());
        dto.setCanBeEdited(loanApplication.canBeEdited());
        dto.setFormattedRequestedAmount(dto.formatAmount(dto.getRequestedAmount()));
        dto.setFormattedApprovedAmount(dto.formatAmount(dto.getApprovedAmount()));
        dto.setFormattedMonthlyPayment(dto.formatAmount(dto.getMonthlyPayment()));
        dto.setProcessingDays(dto.calculateProcessingDays());
        dto.setRiskLevel(dto.calculateRiskLevel());

        return dto;
    }

    public static LoanApplicationDto createSummary(LoanApplication loanApplication) {
        if (loanApplication == null) {
            return null;
        }
        
        LoanApplicationDto dto = new LoanApplicationDto();
        
        // Convert String ID to UUID safely
        try {
            if (loanApplication.getId() != null) {
                dto.setId(loanApplication.getId() instanceof UUID ? 
                    (UUID) loanApplication.getId() : 
                    UUID.fromString(loanApplication.getId().toString()));
            }
        } catch (Exception e) {
            // If conversion fails, keep null
        }
        
        dto.setApplicationNumber(loanApplication.getApplicationNumber());
        dto.setLoanType(loanApplication.getLoanType());
        dto.setRequestedAmount(loanApplication.getRequestedAmount());
        dto.setStatus(loanApplication.getStatus());
        dto.setSubmittedDate(loanApplication.getSubmittedDate());
        
        if (loanApplication.getCustomer() != null && loanApplication.getCustomer().getId() != null) {
            try {
                Object customerIdObj = loanApplication.getCustomer().getId();
                dto.setCustomerId(customerIdObj instanceof UUID ? 
                    (UUID) customerIdObj : 
                    UUID.fromString(customerIdObj.toString()));
            } catch (Exception e) {
                // Skip if conversion fails
            }
        }
        
        // Set computed fields
        dto.setStatusDisplay(loanApplication.getStatusDisplay());
        dto.setLoanTypeDisplay(loanApplication.getLoanTypeDisplay());
        dto.setFormattedRequestedAmount(dto.formatAmount(dto.getRequestedAmount()));
        
        return dto;
    }

    public LoanApplication toEntity() {
        LoanApplication loanApplication = new LoanApplication();
        
        // Convert UUID to String safely for entity
        try {
            if (this.id != null) {
                loanApplication.setId(this.id);
            }
        } catch (Exception e) {
            // Skip if conversion fails
        }
        
        loanApplication.setApplicationNumber(this.applicationNumber);
        loanApplication.setLoanType(this.loanType);
        loanApplication.setRequestedAmount(this.requestedAmount);
        loanApplication.setRequestedTermMonths(this.requestedTermMonths);
        loanApplication.setPurpose(this.purpose);
        loanApplication.setApprovedAmount(this.approvedAmount);
        loanApplication.setApprovedTermMonths(this.approvedTermMonths);
        loanApplication.setApprovedRate(this.approvedRate);
        loanApplication.setMonthlyPayment(this.monthlyPayment);
        loanApplication.setDeclaredIncome(this.declaredIncome);
        loanApplication.setDebtToIncomeRatio(this.debtToIncomeRatio);
        loanApplication.setCreditScore(this.creditScore);
        loanApplication.setStatus(this.status);
        loanApplication.setCurrentStep(this.currentStep);
        loanApplication.setAssignedTo(this.assignedTo);
        loanApplication.setPriority(this.priority);
        loanApplication.setDecisionReason(this.decisionReason);
        loanApplication.setDecisionDate(this.decisionDate);
        loanApplication.setApprovedBy(this.approvedBy);
        loanApplication.setApprovedDate(this.approvedDate);
        loanApplication.setRejectedBy(this.rejectedBy);
        loanApplication.setRejectedDate(this.rejectedDate);
        loanApplication.setDisbursedAmount(this.disbursedAmount);
        loanApplication.setDisbursedDate(this.disbursedDate);
        loanApplication.setDisbursedBy(this.disbursedBy);
        loanApplication.setRiskScore(this.riskScore);
        loanApplication.setRiskFactors(this.riskFactors);
        loanApplication.setSubmittedDate(this.submittedDate);
        loanApplication.setDueDate(this.dueDate);
        loanApplication.setCreatedAt(this.createdAt);
        loanApplication.setUpdatedAt(this.updatedAt);
        loanApplication.setCreatedBy(this.createdBy);
        loanApplication.setUpdatedBy(this.updatedBy);
        
        return loanApplication;
    }

    // Helper methods
    private String formatAmount(BigDecimal amount) {
        return amount != null ? String.format("%,.0f₮", amount) : "";
    }

    private String calculatePriorityText() {
        if (priority == null) return "Дунд";
        switch (priority) {
            case 1: case 2: return "Өндөр";
            case 3: return "Дунд";
            case 4: case 5: return "Бага";
            default: return "Дунд";
        }
    }

    private Integer calculateProcessingDays() {
        if (submittedDate == null) return null;
        LocalDateTime endDate = decisionDate != null ? decisionDate : LocalDateTime.now();
        return (int) java.time.Duration.between(submittedDate, endDate).toDays();
    }

    private String calculateRiskLevel() {
        if (riskScore == null) return "Тодорхойгүй";
        if (riskScore.compareTo(BigDecimal.valueOf(70)) >= 0) return "Өндөр";
        if (riskScore.compareTo(BigDecimal.valueOf(30)) >= 0) return "Дунд";
        return "Бага";
    }

    // Validation methods
    public boolean isValidForSubmission() {
        return customerId != null &&
               loanType != null &&
               requestedAmount != null && requestedAmount.compareTo(BigDecimal.ZERO) > 0 &&
               requestedTermMonths != null && requestedTermMonths > 0;
    }

    // Business logic methods
    public String getStatusBadgeClass() {
        if (status == null) return "badge-secondary";
        switch (status) {
            case DRAFT: return "badge-secondary";
            case SUBMITTED: return "badge-primary";
            case PENDING: return "badge-warning";
            case UNDER_REVIEW: return "badge-info";
            case APPROVED: return "badge-success";
            case REJECTED: return "badge-danger";
            case CANCELLED: return "badge-dark";
            case DISBURSED: return "badge-success";
            default: return "badge-secondary";
        }
    }

    public String getPriorityBadgeClass() {
        if (priority == null) return "badge-secondary";
        switch (priority) {
            case 1: case 2: return "badge-danger";
            case 3: return "badge-warning";
            case 4: case 5: return "badge-info";
            default: return "badge-secondary";
        }
    }

    public boolean isOverdue() {
        return dueDate != null && dueDate.isBefore(LocalDateTime.now()) && 
               (status == LoanApplication.ApplicationStatus.SUBMITTED ||
                status == LoanApplication.ApplicationStatus.PENDING ||
                status == LoanApplication.ApplicationStatus.UNDER_REVIEW);
    }

    public BigDecimal getLoanToValueRatio() {
        // This would be calculated based on collateral value if available
        return null;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }

    public UUID getLoanProductId() { return loanProductId; }
    public void setLoanProductId(UUID loanProductId) { this.loanProductId = loanProductId; }

    public String getApplicationNumber() { return applicationNumber; }
    public void setApplicationNumber(String applicationNumber) { this.applicationNumber = applicationNumber; }

    public LoanApplication.LoanType getLoanType() { return loanType; }
    public void setLoanType(LoanApplication.LoanType loanType) { this.loanType = loanType; }

    public BigDecimal getRequestedAmount() { return requestedAmount; }
    public void setRequestedAmount(BigDecimal requestedAmount) { this.requestedAmount = requestedAmount; }

    public Integer getRequestedTermMonths() { return requestedTermMonths; }
    public void setRequestedTermMonths(Integer requestedTermMonths) { this.requestedTermMonths = requestedTermMonths; }

    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }

    public BigDecimal getApprovedAmount() { return approvedAmount; }
    public void setApprovedAmount(BigDecimal approvedAmount) { this.approvedAmount = approvedAmount; }

    public Integer getApprovedTermMonths() { return approvedTermMonths; }
    public void setApprovedTermMonths(Integer approvedTermMonths) { this.approvedTermMonths = approvedTermMonths; }

    public BigDecimal getApprovedRate() { return approvedRate; }
    public void setApprovedRate(BigDecimal approvedRate) { this.approvedRate = approvedRate; }

    public BigDecimal getMonthlyPayment() { return monthlyPayment; }
    public void setMonthlyPayment(BigDecimal monthlyPayment) { this.monthlyPayment = monthlyPayment; }

    public BigDecimal getDeclaredIncome() { return declaredIncome; }
    public void setDeclaredIncome(BigDecimal declaredIncome) { this.declaredIncome = declaredIncome; }

    public BigDecimal getDebtToIncomeRatio() { return debtToIncomeRatio; }
    public void setDebtToIncomeRatio(BigDecimal debtToIncomeRatio) { this.debtToIncomeRatio = debtToIncomeRatio; }

    public Integer getCreditScore() { return creditScore; }
    public void setCreditScore(Integer creditScore) { this.creditScore = creditScore; }

    public LoanApplication.ApplicationStatus getStatus() { return status; }
    public void setStatus(LoanApplication.ApplicationStatus status) { this.status = status; }

    public String getCurrentStep() { return currentStep; }
    public void setCurrentStep(String currentStep) { this.currentStep = currentStep; }

    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }

    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }

    public String getDecisionReason() { return decisionReason; }
    public void setDecisionReason(String decisionReason) { this.decisionReason = decisionReason; }

    public LocalDateTime getDecisionDate() { return decisionDate; }
    public void setDecisionDate(LocalDateTime decisionDate) { this.decisionDate = decisionDate; }

    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }

    public LocalDateTime getApprovedDate() { return approvedDate; }
    public void setApprovedDate(LocalDateTime approvedDate) { this.approvedDate = approvedDate; }

    public String getRejectedBy() { return rejectedBy; }
    public void setRejectedBy(String rejectedBy) { this.rejectedBy = rejectedBy; }

    public LocalDateTime getRejectedDate() { return rejectedDate; }
    public void setRejectedDate(LocalDateTime rejectedDate) { this.rejectedDate = rejectedDate; }

    public BigDecimal getDisbursedAmount() { return disbursedAmount; }
    public void setDisbursedAmount(BigDecimal disbursedAmount) { this.disbursedAmount = disbursedAmount; }

    public LocalDateTime getDisbursedDate() { return disbursedDate; }
    public void setDisbursedDate(LocalDateTime disbursedDate) { this.disbursedDate = disbursedDate; }

    public String getDisbursedBy() { return disbursedBy; }
    public void setDisbursedBy(String disbursedBy) { this.disbursedBy = disbursedBy; }

    public BigDecimal getRiskScore() { return riskScore; }
    public void setRiskScore(BigDecimal riskScore) { this.riskScore = riskScore; }

    public String getRiskFactors() { return riskFactors; }
    public void setRiskFactors(String riskFactors) { this.riskFactors = riskFactors; }

    public LocalDateTime getSubmittedDate() { return submittedDate; }
    public void setSubmittedDate(LocalDateTime submittedDate) { this.submittedDate = submittedDate; }

    public LocalDateTime getDueDate() { return dueDate; }
    public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    public CustomerDto getCustomer() { return customer; }
    public void setCustomer(CustomerDto customer) { this.customer = customer; }

    public LoanProductDto getLoanProduct() { return loanProduct; }
    public void setLoanProduct(LoanProductDto loanProduct) { this.loanProduct = loanProduct; }

    // Computed field getters and setters
    public String getStatusDisplay() { return statusDisplay; }
    public void setStatusDisplay(String statusDisplay) { this.statusDisplay = statusDisplay; }

    public String getLoanTypeDisplay() { return loanTypeDisplay; }
    public void setLoanTypeDisplay(String loanTypeDisplay) { this.loanTypeDisplay = loanTypeDisplay; }

    public String getPriorityText() { return priorityText; }
    public void setPriorityText(String priorityText) { this.priorityText = priorityText; }

    public Boolean getIsSubmitted() { return isSubmitted; }
    public void setIsSubmitted(Boolean isSubmitted) { this.isSubmitted = isSubmitted; }

    public Boolean getIsApproved() { return isApproved; }
    public void setIsApproved(Boolean isApproved) { this.isApproved = isApproved; }

    public Boolean getIsRejected() { return isRejected; }
    public void setIsRejected(Boolean isRejected) { this.isRejected = isRejected; }

    public Boolean getCanBeEdited() { return canBeEdited; }
    public void setCanBeEdited(Boolean canBeEdited) { this.canBeEdited = canBeEdited; }

    public String getFormattedRequestedAmount() { return formattedRequestedAmount; }
    public void setFormattedRequestedAmount(String formattedRequestedAmount) { this.formattedRequestedAmount = formattedRequestedAmount; }

    public String getFormattedApprovedAmount() { return formattedApprovedAmount; }
    public void setFormattedApprovedAmount(String formattedApprovedAmount) { this.formattedApprovedAmount = formattedApprovedAmount; }

    public String getFormattedMonthlyPayment() { return formattedMonthlyPayment; }
    public void setFormattedMonthlyPayment(String formattedMonthlyPayment) { this.formattedMonthlyPayment = formattedMonthlyPayment; }

    public Integer getProcessingDays() { return processingDays; }
    public void setProcessingDays(Integer processingDays) { this.processingDays = processingDays; }

    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

    @Override
    public String toString() {
        return "LoanApplicationDto{" +
                "id=" + id +
                ", applicationNumber='" + applicationNumber + '\'' +
                ", loanType=" + loanType +
                ", requestedAmount=" + requestedAmount +
                ", status=" + status +
                ", customerId=" + customerId +
                '}';
    }
}

/**
 * LoanProductDto placeholder - энэ нь тусдаа файлд байх ёстой
 */
class LoanProductDto {
    // Placeholder implementation
}