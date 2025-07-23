package com.company.los.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Зээлийн хүсэлтийн Entity
 * Loan Application Entity
 */
@Entity
@Table(name = "loan_applications", indexes = {
        @Index(name = "idx_loan_application_customer", columnList = "customer_id"),
        @Index(name = "idx_loan_application_number", columnList = "application_number", unique = true),
        @Index(name = "idx_loan_application_status", columnList = "status"),
        @Index(name = "idx_loan_application_created", columnList = "created_at")
})
@SQLDelete(sql = "UPDATE loan_applications SET is_deleted = true WHERE id = ?")
@Where(clause = "is_deleted = false")
public class LoanApplication extends BaseEntity {

    // Enum definitions
    public enum LoanType {
        PERSONAL("PERSONAL", "Хувийн зээл"),
        BUSINESS("BUSINESS", "Бизнесийн зээл"),
        MORTGAGE("MORTGAGE", "Орон сууцны зээл"),
        CAR_LOAN("CAR_LOAN", "Автомашины зээл"),
        CONSUMER("CONSUMER", "Хэрэглээний зээл"),
        EDUCATION("EDUCATION", "Боловсролын зээл"),
        MEDICAL("MEDICAL", "Эмнэлгийн зээл");

        private final String code;
        private final String mongolianName;

        LoanType(String code, String mongolianName) {
            this.code = code;
            this.mongolianName = mongolianName;
        }

        public String getCode() { return code; }
        public String getMongolianName() { return mongolianName; }
    }

    public enum ApplicationStatus {
        DRAFT("DRAFT", "Ноорог"),
        SUBMITTED("SUBMITTED", "Илгээсэн"),
        PENDING("PENDING", "Хүлээгдэж байгаа"),
        UNDER_REVIEW("UNDER_REVIEW", "Хянаж байгаа"),
        APPROVED("APPROVED", "Зөвшөөрсөн"),
        REJECTED("REJECTED", "Татгалзсан"),
        CANCELLED("CANCELLED", "Цуцалсан"),
        DISBURSED("DISBURSED", "Олгосон");

        private final String code;
        private final String mongolianName;

        ApplicationStatus(String code, String mongolianName) {
            this.code = code;
            this.mongolianName = mongolianName;
        }

        public String getCode() { return code; }
        public String getMongolianName() { return mongolianName; }

        public boolean isActiveStatus() {
            return this == SUBMITTED || this == PENDING || this == UNDER_REVIEW;
        }
    }

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false, foreignKey = @ForeignKey(name = "fk_loan_application_customer"))
    @NotNull(message = "Харилцагч заавал байх ёстой")
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "loan_product_id", nullable = false, foreignKey = @ForeignKey(name = "fk_loan_application_product"))
    @NotNull(message = "Зээлийн бүтээгдэхүүн заавал сонгох ёстой")
    private LoanProduct loanProduct;

    @Column(name = "application_number", unique = true, nullable = false, length = 50)
    @NotBlank(message = "Хүсэлтийн дугаар заавал байх ёстой")
    @Size(max = 50, message = "Хүсэлтийн дугаар 50 тэмдэгтээс ихгүй байх ёстой")
    private String applicationNumber;

    @Column(name = "loan_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Зээлийн төрөл заавал сонгох ёстой")
    private LoanType loanType;

    // Хүсэлтийн мэдээлэл
    @Column(name = "requested_amount", nullable = false, precision = 15, scale = 2)
    @NotNull(message = "Хүсэх дүн заавал бөглөх ёстой")
    @DecimalMin(value = "1000.0", message = "Хүсэх дүн 1,000-аас их байх ёстой")
    private BigDecimal requestedAmount;

    @Column(name = "requested_term_months", nullable = false)
    @NotNull(message = "Хүсэх хугацаа заавал бөглөх ёстой")
    @Min(value = 1, message = "Хүсэх хугацаа 1 сараас их байх ёстой")
    @Max(value = 360, message = "Хүсэх хугацаа 360 сараас бага байх ёстой")
    private Integer requestedTermMonths;

    @Column(name = "purpose", columnDefinition = "TEXT")
    private String purpose;

    // Зөвшөөрөгдсөн мэдээлэл
    @Column(name = "approved_amount", precision = 15, scale = 2)
    @DecimalMin(value = "0.0", message = "Зөвшөөрсөн дүн сөрөг байж болохгүй")
    private BigDecimal approvedAmount;

    @Column(name = "approved_term_months")
    @Min(value = 1, message = "Зөвшөөрсөн хугацаа 1 сараас их байх ёстой")
    private Integer approvedTermMonths;

    @Column(name = "approved_rate", precision = 5, scale = 4)
    @DecimalMin(value = "0.0", message = "Хүү сөрөг байж болохгүй")
    @DecimalMax(value = "1.0", message = "Хүү 100%-аас их байж болохгүй")
    private BigDecimal approvedRate;

    @Column(name = "monthly_payment", precision = 15, scale = 2)
    @DecimalMin(value = "0.0", message = "Сарын төлбөр сөрөг байж болохгүй")
    private BigDecimal monthlyPayment;

    // Санхүүгийн мэдээлэл
    @Column(name = "declared_income", precision = 15, scale = 2)
    @DecimalMin(value = "0.0", message = "Мэдүүлсэн орлого сөрөг байж болохгүй")
    private BigDecimal declaredIncome;

    @Column(name = "debt_to_income_ratio", precision = 5, scale = 4)
    @DecimalMin(value = "0.0", message = "Өр орлогын харьцаа сөрөг байж болохгүй")
    private BigDecimal debtToIncomeRatio;

    @Column(name = "credit_score")
    @Min(value = 300, message = "Зээлийн оноо 300-аас бага байж болохгүй")
    @Max(value = 850, message = "Зээлийн оноо 850-аас их байж болохгүй")
    private Integer creditScore;

    // Статус болон ажлын урсгал
    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Статус заавал байх ёстой")
    private ApplicationStatus status = ApplicationStatus.DRAFT;

    @Column(name = "current_step", length = 100)
    @Size(max = 100, message = "Одоогийн алхам 100 тэмдэгтээс ихгүй байх ёстой")
    private String currentStep;

    @Column(name = "assigned_to", length = 100)
    @Size(max = 100, message = "Хариуцагч 100 тэмдэгтээс ихгүй байх ёстой")
    private String assignedTo;

    @Column(name = "priority")
    @Min(value = 1, message = "Чухал байдал 1-ээс бага байж болохгүй")
    @Max(value = 5, message = "Чухал байдал 5-аас их байж болохгүй")
    private Integer priority = 3;

    // Шийдвэрийн мэдээлэл
    @Column(name = "decision_reason", columnDefinition = "TEXT")
    private String decisionReason;

    @Column(name = "decision_date")
    private LocalDateTime decisionDate;

    @Column(name = "approved_by", length = 100)
    @Size(max = 100, message = "Зөвшөөрсөн хүн 100 тэмдэгтээс ихгүй байх ёстой")
    private String approvedBy;

    @Column(name = "approved_date")
    private LocalDateTime approvedDate;

    @Column(name = "rejected_by", length = 100)
    @Size(max = 100, message = "Татгалзсан хүн 100 тэмдэгтээс ихгүй байх ёстой")
    private String rejectedBy;

    @Column(name = "rejected_date")
    private LocalDateTime rejectedDate;

    // Олголт
    @Column(name = "disbursed_amount", precision = 15, scale = 2)
    @DecimalMin(value = "0.0", message = "Олгосон дүн сөрөг байж болохгүй")
    private BigDecimal disbursedAmount;

    @Column(name = "disbursed_date")
    private LocalDateTime disbursedDate;

    @Column(name = "disbursed_by", length = 100)
    @Size(max = 100, message = "Олгосон хүн 100 тэмдэгтээс ихгүй байх ёстой")
    private String disbursedBy;

    // Эрсдэлийн үнэлгээ
    @Column(name = "risk_score", precision = 5, scale = 2)
    @DecimalMin(value = "0.0", message = "Эрсдэлийн оноо сөрөг байж болохгүй")
    @DecimalMax(value = "100.0", message = "Эрсдэлийн оноо 100-аас их байж болохгүй")
    private BigDecimal riskScore;

    @Column(name = "risk_factors", columnDefinition = "TEXT")
    private String riskFactors;

    // Чухал огноонууд
    @Column(name = "submitted_date")
    private LocalDateTime submittedDate;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    // Баримт бичгүүд
    @OneToMany(mappedBy = "loanApplication", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Document> documents = new ArrayList<>();

    // Constructors
    public LoanApplication() {
        super();
    }

    public LoanApplication(Customer customer, LoanProduct loanProduct, LoanType loanType,
                          BigDecimal requestedAmount, Integer requestedTermMonths) {
        this();
        this.customer = customer;
        this.loanProduct = loanProduct;
        this.loanType = loanType;
        this.requestedAmount = requestedAmount;
        this.requestedTermMonths = requestedTermMonths;
    }

    // Business methods
    public void submit() {
        this.status = ApplicationStatus.SUBMITTED;
        this.submittedDate = LocalDateTime.now();
    }

    public void approve(String approvedBy, BigDecimal approvedAmount, Integer approvedTermMonths, BigDecimal approvedRate) {
        this.status = ApplicationStatus.APPROVED;
        this.approvedBy = approvedBy;
        this.approvedDate = LocalDateTime.now();
        this.decisionDate = LocalDateTime.now();
        this.approvedAmount = approvedAmount;
        this.approvedTermMonths = approvedTermMonths;
        this.approvedRate = approvedRate;
    }

    public void reject(String rejectedBy, String reason) {
        this.status = ApplicationStatus.REJECTED;
        this.rejectedBy = rejectedBy;
        this.rejectedDate = LocalDateTime.now();
        this.decisionDate = LocalDateTime.now();
        this.decisionReason = reason;
    }

    public void disburse(String disbursedBy, BigDecimal disbursedAmount) {
        this.status = ApplicationStatus.DISBURSED;
        this.disbursedBy = disbursedBy;
        this.disbursedDate = LocalDateTime.now();
        this.disbursedAmount = disbursedAmount;
    }

    public boolean isSubmitted() {
        return !ApplicationStatus.DRAFT.equals(status);
    }

    public boolean isApproved() {
        return ApplicationStatus.APPROVED.equals(status);
    }

    public boolean isRejected() {
        return ApplicationStatus.REJECTED.equals(status);
    }

    public boolean canBeEdited() {
        return ApplicationStatus.DRAFT.equals(status) || ApplicationStatus.SUBMITTED.equals(status);
    }

    public String getStatusDisplay() {
        return status != null ? status.getMongolianName() : "Тодорхойгүй";
    }

    public String getLoanTypeDisplay() {
        return loanType != null ? loanType.getMongolianName() : "Тодорхойгүй";
    }

    // Getters and Setters
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }

    public LoanProduct getLoanProduct() { return loanProduct; }
    public void setLoanProduct(LoanProduct loanProduct) { this.loanProduct = loanProduct; }

    public String getApplicationNumber() { return applicationNumber; }
    public void setApplicationNumber(String applicationNumber) { this.applicationNumber = applicationNumber; }

    public LoanType getLoanType() { return loanType; }
    public void setLoanType(LoanType loanType) { this.loanType = loanType; }

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

    public ApplicationStatus getStatus() { return status; }
    public void setStatus(ApplicationStatus status) { this.status = status; }

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

    public List<Document> getDocuments() { return documents; }
    public void setDocuments(List<Document> documents) { this.documents = documents; }

    // toString
    @Override
    public String toString() {
        return "LoanApplication{" +
                "id=" + getId() +
                ", applicationNumber='" + applicationNumber + '\'' +
                ", loanType=" + loanType +
                ", requestedAmount=" + requestedAmount +
                ", status=" + status +
                ", customer=" + (customer != null ? customer.getDisplayName() : "null") +
                '}';
    }
}