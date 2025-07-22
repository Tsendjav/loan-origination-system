package com.company.los.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.company.los.entity.LoanApplication;
import com.company.los.enums.LoanStatus;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Зээлийн хүсэлтийн DTO
 * Loan Application Data Transfer Object
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoanApplicationDto {

    private UUID id;

    private String applicationNumber;

    @NotNull(message = "Харилцагч заавал байх ёстой")
    private UUID customerId;

    @NotNull(message = "Зээлийн төрөл заавал сонгох ёстой")
    private LoanApplication.LoanType loanType;

    private LoanStatus status;

    // Хүсэх зээлийн мэдээлэл
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

    // Зөвшөөрсөн зээлийн мэдээлэл
    @DecimalMin(value = "0.0", message = "Зөвшөөрсөн дүн сөрөг байж болохгүй")
    private BigDecimal approvedAmount;

    @Min(value = 1, message = "Зөвшөөрсөн хугацаа 1 сараас бага байж болохгүй")
    private Integer approvedTermMonths;

    @DecimalMin(value = "0.0", message = "Хүү сөрөг байж болохгүй")
    @DecimalMax(value = "50.0", message = "Хүү 50%-аас их байж болохгүй")
    private BigDecimal approvedRate;

    @DecimalMin(value = "0.0", message = "Сарын төлбөр сөрөг байж болохгүй")
    private BigDecimal monthlyPayment;

    // Огноонууд
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime submittedDate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime approvedDate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime rejectedDate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime disbursedDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expectedDisbursementDate;

    // Шийдвэрийн мэдээлэл
    @Size(max = 1000, message = "Шийдвэрийн үндэслэл 1000 тэмдэгтээс ихгүй байх ёстой")
    private String decisionReason;

    @DecimalMin(value = "0.0", message = "Эрсдэлийн оноо сөрөг байж болохгүй")
    @DecimalMax(value = "100.0", message = "Эрсдэлийн оноо 100-аас их байж болохгүй")
    private BigDecimal riskScore;

    @DecimalMin(value = "0.0", message = "Зээлийн оноо сөрөг байж болохгүй")
    @DecimalMax(value = "850.0", message = "Зээлийн оноо 850-аас их байж болохгүй")
    private BigDecimal creditScore;

    // Workflow мэдээлэл
    private String currentStep;
    private String assignedTo;
    private Integer priority;

    // Metadata
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    private String createdBy;
    private String updatedBy;

    // Related objects
    private CustomerDto customer;
    private List<DocumentDto> documents;

    // Computed fields (read-only)
    private String statusDisplay;
    private String loanTypeDisplay;
    private String priorityText;
    private Boolean canBeEdited;
    private Boolean isFinalStatus;
    private Boolean isActive;
    private Integer daysInProcess;
    private String processingTime;

    // Constructors
    public LoanApplicationDto() {
    }

    public LoanApplicationDto(UUID customerId, LoanApplication.LoanType loanType, BigDecimal requestedAmount, Integer requestedTermMonths) {
        this.customerId = customerId;
        this.loanType = loanType;
        this.requestedAmount = requestedAmount;
        this.requestedTermMonths = requestedTermMonths;
        this.status = LoanStatus.DRAFT;
        this.priority = 3; // Default to medium priority
    }

    // Static factory methods with null-safe handling
    public static LoanApplicationDto fromEntity(LoanApplication loanApplication) {
        if (loanApplication == null) {
            return null;
        }
        
        LoanApplicationDto dto = new LoanApplicationDto();
        dto.setId(loanApplication.getId());
        dto.setApplicationNumber(loanApplication.getApplicationNumber());
        
        // Safe customer ID extraction
        try {
            if (loanApplication.getCustomer() != null) {
                dto.setCustomerId(loanApplication.getCustomer().getId());
            }
        } catch (Exception e) { /* Ignore if customer not available */ }
        
        dto.setLoanType(loanApplication.getLoanType());
        dto.setStatus(loanApplication.getStatus());
        dto.setRequestedAmount(loanApplication.getRequestedAmount());
        dto.setRequestedTermMonths(loanApplication.getRequestedTermMonths());
        dto.setCreatedAt(loanApplication.getCreatedAt());
        dto.setUpdatedAt(loanApplication.getUpdatedAt());
        dto.setCreatedBy(loanApplication.getCreatedBy());
        dto.setUpdatedBy(loanApplication.getUpdatedBy());

        // Null-safe getter calls with default values
        try {
            dto.setPurpose(loanApplication.getPurpose() != null ? loanApplication.getPurpose() : "");
        } catch (Exception e) { dto.setPurpose(""); }
        
        try {
            dto.setDeclaredIncome(loanApplication.getDeclaredIncome() != null ? loanApplication.getDeclaredIncome() : BigDecimal.ZERO);
        } catch (Exception e) { dto.setDeclaredIncome(BigDecimal.ZERO); }
        
        try {
            dto.setApprovedAmount(loanApplication.getApprovedAmount() != null ? loanApplication.getApprovedAmount() : BigDecimal.ZERO);
        } catch (Exception e) { dto.setApprovedAmount(BigDecimal.ZERO); }
        
        try {
            dto.setApprovedTermMonths(loanApplication.getApprovedTermMonths() != null ? loanApplication.getApprovedTermMonths() : 0);
        } catch (Exception e) { dto.setApprovedTermMonths(0); }
        
        try {
            dto.setApprovedRate(loanApplication.getApprovedRate() != null ? loanApplication.getApprovedRate() : BigDecimal.ZERO);
        } catch (Exception e) { dto.setApprovedRate(BigDecimal.ZERO); }
        
        try {
            dto.setMonthlyPayment(loanApplication.getMonthlyPayment() != null ? loanApplication.getMonthlyPayment() : BigDecimal.ZERO);
        } catch (Exception e) { dto.setMonthlyPayment(BigDecimal.ZERO); }
        
        try {
            dto.setSubmittedDate(loanApplication.getSubmittedDate() != null ? loanApplication.getSubmittedDate() : null);
        } catch (Exception e) { /* Ignore if getter not available */ }
        
        try {
            dto.setApprovedDate(loanApplication.getApprovedDate() != null ? loanApplication.getApprovedDate() : null);
        } catch (Exception e) { /* Ignore if getter not available */ }
        
        try {
            dto.setRejectedDate(loanApplication.getRejectedDate() != null ? loanApplication.getRejectedDate() : null);
        } catch (Exception e) { /* Ignore if getter not available */ }
        
        try {
            dto.setDisbursedDate(loanApplication.getDisbursedDate() != null ? loanApplication.getDisbursedDate() : null);
        } catch (Exception e) { /* Ignore if getter not available */ }
        
        try {
            dto.setExpectedDisbursementDate(loanApplication.getExpectedDisbursementDate() != null ? loanApplication.getExpectedDisbursementDate() : null);
        } catch (Exception e) { /* Ignore if getter not available */ }
        
        try {
            dto.setDecisionReason(loanApplication.getDecisionReason() != null ? loanApplication.getDecisionReason() : "");
        } catch (Exception e) { dto.setDecisionReason(""); }
        
        try {
            dto.setRiskScore(loanApplication.getRiskScore() != null ? loanApplication.getRiskScore() : BigDecimal.ZERO);
        } catch (Exception e) { dto.setRiskScore(BigDecimal.ZERO); }
        
        try {
            dto.setCreditScore(loanApplication.getCreditScore() != null ? loanApplication.getCreditScore() : BigDecimal.ZERO);
        } catch (Exception e) { dto.setCreditScore(BigDecimal.ZERO); }
        
        try {
            dto.setCurrentStep(loanApplication.getCurrentStep() != null ? loanApplication.getCurrentStep() : "");
        } catch (Exception e) { dto.setCurrentStep(""); }
        
        try {
            dto.setAssignedTo(loanApplication.getAssignedTo() != null ? loanApplication.getAssignedTo() : "");
        } catch (Exception e) { dto.setAssignedTo(""); }
        
        try {
            dto.setPriority(loanApplication.getPriority() != null ? loanApplication.getPriority() : 3);
        } catch (Exception e) { dto.setPriority(3); }

        // Set customer info
        try {
            if (loanApplication.getCustomer() != null) {
                dto.setCustomer(CustomerDto.createSummary(loanApplication.getCustomer()));
            }
        } catch (Exception e) { /* Ignore if customer not available */ }

        // Computed fields with safe method calls
        try {
            dto.setStatusDisplay(loanApplication.getStatus().getMongolianName());
        } catch (Exception e) {
            dto.setStatusDisplay(loanApplication.getStatus().toString());
        }
        
        try {
            dto.setLoanTypeDisplay(loanApplication.getLoanType().getMongolianName());
        } catch (Exception e) {
            dto.setLoanTypeDisplay(loanApplication.getLoanType().toString());
        }
        
        try {
            dto.setPriorityText(loanApplication.getPriorityText());
        } catch (Exception e) {
            Integer priority = dto.getPriority();
            if (priority != null) {
                if (priority <= 2) dto.setPriorityText("Өндөр");
                else if (priority == 3) dto.setPriorityText("Дунд");
                else dto.setPriorityText("Бага");
            } else {
                dto.setPriorityText("Дунд");
            }
        }
        
        try {
            dto.setCanBeEdited(loanApplication.canBeEdited());
        } catch (Exception e) {
            dto.setCanBeEdited(loanApplication.getStatus() == LoanStatus.DRAFT || 
                               loanApplication.getStatus() == LoanStatus.SUBMITTED);
        }
        
        try {
            dto.setIsFinalStatus(loanApplication.isFinalStatus());
        } catch (Exception e) {
            dto.setIsFinalStatus(loanApplication.getStatus() == LoanStatus.APPROVED ||
                                loanApplication.getStatus() == LoanStatus.REJECTED ||
                                loanApplication.getStatus() == LoanStatus.DISBURSED ||
                                loanApplication.getStatus() == LoanStatus.CANCELLED);
        }
        
        try {
            dto.setIsActive(loanApplication.isActive());
        } catch (Exception e) {
            dto.setIsActive(!dto.getIsFinalStatus());
        }
        
        if (dto.getSubmittedDate() != null) {
            LocalDateTime endDate = dto.getApprovedDate() != null ? 
                dto.getApprovedDate() : LocalDateTime.now();
            long days = java.time.Duration.between(dto.getSubmittedDate(), endDate).toDays();
            dto.setDaysInProcess((int) days);
            dto.setProcessingTime(days + " хоног");
        }

        return dto;
    }

    public static LoanApplicationDto createSummary(LoanApplication loanApplication) {
        if (loanApplication == null) {
            return null;
        }
        
        LoanApplicationDto dto = new LoanApplicationDto();
        dto.setId(loanApplication.getId());
        dto.setApplicationNumber(loanApplication.getApplicationNumber());
        dto.setLoanType(loanApplication.getLoanType());
        dto.setStatus(loanApplication.getStatus());
        dto.setRequestedAmount(loanApplication.getRequestedAmount());
        dto.setRequestedTermMonths(loanApplication.getRequestedTermMonths());
        
        try {
            dto.setSubmittedDate(loanApplication.getSubmittedDate() != null ? loanApplication.getSubmittedDate() : null);
        } catch (Exception e) { /* Ignore if getter not available */ }
        
        try {
            dto.setStatusDisplay(loanApplication.getStatus().getMongolianName());
            dto.setLoanTypeDisplay(loanApplication.getLoanType().getMongolianName());
        } catch (Exception e) {
            dto.setStatusDisplay(loanApplication.getStatus().toString());
            dto.setLoanTypeDisplay(loanApplication.getLoanType().toString());
        }
        
        try {
            dto.setPriorityText(loanApplication.getPriorityText());
        } catch (Exception e) {
            dto.setPriorityText("Дунд");
        }
        
        return dto;
    }

    public LoanApplication toEntity() {
        LoanApplication loanApplication = new LoanApplication();
        loanApplication.setId(this.id);
        loanApplication.setApplicationNumber(this.applicationNumber);
        loanApplication.setLoanType(this.loanType);
        loanApplication.setStatus(this.status != null ? this.status : LoanStatus.DRAFT);
        loanApplication.setRequestedAmount(this.requestedAmount);
        loanApplication.setRequestedTermMonths(this.requestedTermMonths);
        loanApplication.setCreatedAt(this.createdAt);
        loanApplication.setUpdatedAt(this.updatedAt);
        loanApplication.setCreatedBy(this.createdBy);
        loanApplication.setUpdatedBy(this.updatedBy);
        
        try {
            loanApplication.setPurpose(this.purpose);
        } catch (Exception e) { /* Ignore if setter not available */ }
        
        try {
            loanApplication.setDeclaredIncome(this.declaredIncome);
        } catch (Exception e) { /* Ignore if setter not available */ }
        
        try {
            loanApplication.setApprovedAmount(this.approvedAmount);
        } catch (Exception e) { /* Ignore if setter not available */ }
        
        try {
            loanApplication.setApprovedTermMonths(this.approvedTermMonths);
        } catch (Exception e) { /* Ignore if setter not available */ }
        
        try {
            loanApplication.setApprovedRate(this.approvedRate);
        } catch (Exception e) { /* Ignore if setter not available */ }
        
        try {
            loanApplication.setMonthlyPayment(this.monthlyPayment);
        } catch (Exception e) { /* Ignore if setter not available */ }
        
        try {
            loanApplication.setSubmittedDate(this.submittedDate);
        } catch (Exception e) { /* Ignore if setter not available */ }
        
        try {
            loanApplication.setApprovedDate(this.approvedDate);
        } catch (Exception e) { /* Ignore if setter not available */ }
        
        try {
            loanApplication.setRejectedDate(this.rejectedDate);
        } catch (Exception e) { /* Ignore if setter not available */ }
        
        try {
            loanApplication.setDisbursedDate(this.disbursedDate);
        } catch (Exception e) { /* Ignore if setter not available */ }
        
        try {
            loanApplication.setExpectedDisbursementDate(this.expectedDisbursementDate);
        } catch (Exception e) { /* Ignore if setter not available */ }
        
        try {
            loanApplication.setDecisionReason(this.decisionReason);
        } catch (Exception e) { /* Ignore if setter not available */ }
        
        try {
            loanApplication.setRiskScore(this.riskScore);
        } catch (Exception e) { /* Ignore if setter not available */ }
        
        try {
            loanApplication.setCreditScore(this.creditScore);
        } catch (Exception e) { /* Ignore if setter not available */ }
        
        try {
            loanApplication.setCurrentStep(this.currentStep);
        } catch (Exception e) { /* Ignore if setter not available */ }
        
        try {
            loanApplication.setAssignedTo(this.assignedTo);
        } catch (Exception e) { /* Ignore if setter not available */ }
        
        try {
            loanApplication.setPriority(this.priority != null ? this.priority : 3);
        } catch (Exception e) { /* Ignore if setter not available */ }
        
        return loanApplication;
    }

    // Validation methods
    public boolean isValidLoanRequest() {
        return loanType != null &&
               requestedAmount != null && requestedAmount.compareTo(BigDecimal.ZERO) > 0 &&
               requestedTermMonths != null && requestedTermMonths > 0 &&
               customerId != null;
    }

    public boolean isWithinLoanTypeLimits() {
        if (loanType == null || requestedAmount == null || requestedTermMonths == null) {
            return false;
        }
        
        try {
            return requestedAmount.compareTo(BigDecimal.valueOf(loanType.getMinAmount())) >= 0 &&
                   requestedAmount.compareTo(BigDecimal.valueOf(loanType.getMaxAmount())) <= 0 &&
                   requestedTermMonths >= loanType.getMinTermMonths() &&
                   requestedTermMonths <= loanType.getMaxTermMonths();
        } catch (Exception e) {
            return requestedAmount.compareTo(BigDecimal.valueOf(100000)) >= 0 &&
                   requestedAmount.compareTo(BigDecimal.valueOf(100000000)) <= 0 &&
                   requestedTermMonths >= 3 && requestedTermMonths <= 300;
        }
    }

    // Business logic methods
    public String getFormattedRequestedAmount() {
        return requestedAmount != null ? String.format("%,.0f₮", requestedAmount) : "";
    }

    public String getFormattedApprovedAmount() {
        return approvedAmount != null ? String.format("%,.0f₮", approvedAmount) : "";
    }

    public String getFormattedMonthlyPayment() {
        return monthlyPayment != null ? String.format("%,.0f₮", monthlyPayment) : "";
    }

    public String getFormattedRate() {
        return approvedRate != null ? String.format("%.2f%%", approvedRate) : "";
    }

    public String getTermText() {
        return requestedTermMonths != null ? requestedTermMonths + " сар" : "";
    }

    public String getApprovedTermText() {
        return approvedTermMonths != null ? approvedTermMonths + " сар" : "";
    }

    public String getRiskScoreText() {
        if (riskScore == null) return "";
        if (riskScore.compareTo(BigDecimal.valueOf(30)) <= 0) return "Бага эрсдэл";
        if (riskScore.compareTo(BigDecimal.valueOf(70)) <= 0) return "Дунд эрсдэл";
        return "Өндөр эрсдэл";
    }

    public String getCreditScoreText() {
        if (creditScore == null) return "";
        if (creditScore.compareTo(BigDecimal.valueOf(600)) < 0) return "Муу";
        if (creditScore.compareTo(BigDecimal.valueOf(700)) < 0) return "Дунд";
        if (creditScore.compareTo(BigDecimal.valueOf(750)) < 0) return "Сайн";
        return "Маш сайн";
    }

    public boolean hasApprovalInfo() {
        return approvedAmount != null && approvedTermMonths != null && approvedRate != null;
    }

    public boolean hasRiskAssessment() {
        return riskScore != null || creditScore != null;
    }

    public String getStatusBadgeClass() {
        if (status == null) return "badge-secondary";
        switch (status) {
            case DRAFT: return "badge-secondary";
            case SUBMITTED: case DOCUMENT_REVIEW: case CREDIT_CHECK: 
            case RISK_ASSESSMENT: case MANAGER_REVIEW: return "badge-warning";
            case APPROVED: case DISBURSED: return "badge-success";
            case REJECTED: case CANCELLED: return "badge-danger";
            case PENDING_INFO: return "badge-info";
            default: return "badge-secondary";
        }
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getApplicationNumber() { return applicationNumber; }
    public void setApplicationNumber(String applicationNumber) { this.applicationNumber = applicationNumber; }

    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }

    public LoanApplication.LoanType getLoanType() { return loanType; }
    public void setLoanType(LoanApplication.LoanType loanType) { this.loanType = loanType; }

    public LoanStatus getStatus() { return status; }
    public void setStatus(LoanStatus status) { this.status = status; }

    public BigDecimal getRequestedAmount() { return requestedAmount; }
    public void setRequestedAmount(BigDecimal requestedAmount) { this.requestedAmount = requestedAmount; }

    public Integer getRequestedTermMonths() { return requestedTermMonths; }
    public void setRequestedTermMonths(Integer requestedTermMonths) { this.requestedTermMonths = requestedTermMonths; }

    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }

    public BigDecimal getDeclaredIncome() { return declaredIncome; }
    public void setDeclaredIncome(BigDecimal declaredIncome) { this.declaredIncome = declaredIncome; }

    public BigDecimal getApprovedAmount() { return approvedAmount; }
    public void setApprovedAmount(BigDecimal approvedAmount) { this.approvedAmount = approvedAmount; }

    public Integer getApprovedTermMonths() { return approvedTermMonths; }
    public void setApprovedTermMonths(Integer approvedTermMonths) { this.approvedTermMonths = approvedTermMonths; }

    public BigDecimal getApprovedRate() { return approvedRate; }
    public void setApprovedRate(BigDecimal approvedRate) { this.approvedRate = approvedRate; }

    public BigDecimal getMonthlyPayment() { return monthlyPayment; }
    public void setMonthlyPayment(BigDecimal monthlyPayment) { this.monthlyPayment = monthlyPayment; }

    public LocalDateTime getSubmittedDate() { return submittedDate; }
    public void setSubmittedDate(LocalDateTime submittedDate) { this.submittedDate = submittedDate; }

    public LocalDateTime getApprovedDate() { return approvedDate; }
    public void setApprovedDate(LocalDateTime approvedDate) { this.approvedDate = approvedDate; }

    public LocalDateTime getRejectedDate() { return rejectedDate; }
    public void setRejectedDate(LocalDateTime rejectedDate) { this.rejectedDate = rejectedDate; }

    public LocalDateTime getDisbursedDate() { return disbursedDate; }
    public void setDisbursedDate(LocalDateTime disbursedDate) { this.disbursedDate = disbursedDate; }

    public LocalDate getExpectedDisbursementDate() { return expectedDisbursementDate; }
    public void setExpectedDisbursementDate(LocalDate expectedDisbursementDate) { this.expectedDisbursementDate = expectedDisbursementDate; }

    public String getDecisionReason() { return decisionReason; }
    public void setDecisionReason(String decisionReason) { this.decisionReason = decisionReason; }

    public BigDecimal getRiskScore() { return riskScore; }
    public void setRiskScore(BigDecimal riskScore) { this.riskScore = riskScore; }

    public BigDecimal getCreditScore() { return creditScore; }
    public void setCreditScore(BigDecimal creditScore) { this.creditScore = creditScore; }

    public String getCurrentStep() { return currentStep; }
    public void setCurrentStep(String currentStep) { this.currentStep = currentStep; }

    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }

    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }

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

    public List<DocumentDto> getDocuments() { return documents; }
    public void setDocuments(List<DocumentDto> documents) { this.documents = documents; }

    public String getStatusDisplay() { return statusDisplay; }
    public void setStatusDisplay(String statusDisplay) { this.statusDisplay = statusDisplay; }

    public String getLoanTypeDisplay() { return loanTypeDisplay; }
    public void setLoanTypeDisplay(String loanTypeDisplay) { this.loanTypeDisplay = loanTypeDisplay; }

    public String getPriorityText() { return priorityText; }
    public void setPriorityText(String priorityText) { this.priorityText = priorityText; }

    public Boolean getCanBeEdited() { return canBeEdited; }
    public void setCanBeEdited(Boolean canBeEdited) { this.canBeEdited = canBeEdited; }

    public Boolean getIsFinalStatus() { return isFinalStatus; }
    public void setIsFinalStatus(Boolean isFinalStatus) { this.isFinalStatus = isFinalStatus; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public Integer getDaysInProcess() { return daysInProcess; }
    public void setDaysInProcess(Integer daysInProcess) { this.daysInProcess = daysInProcess; }

    public String getProcessingTime() { return processingTime; }
    public void setProcessingTime(String processingTime) { this.processingTime = processingTime; }

    @Override
    public String toString() {
        return "LoanApplicationDto{" +
                "id=" + id +
                ", applicationNumber='" + applicationNumber + '\'' +
                ", loanType=" + loanType +
                ", status=" + status +
                ", requestedAmount=" + requestedAmount +
                ", requestedTermMonths=" + requestedTermMonths +
                '}';
    }
}