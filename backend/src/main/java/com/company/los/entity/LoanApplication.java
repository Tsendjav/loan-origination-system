package com.company.los.entity;

import com.company.los.common.BaseEntity;
import com.company.los.enums.LoanStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Зээлийн хүсэлтийн Entity
 * Loan Application Entity
 */
@Entity
@Table(name = "loan_applications", indexes = {
        @Index(name = "idx_loan_application_number", columnList = "application_number", unique = true),
        @Index(name = "idx_loan_status", columnList = "status"),
        @Index(name = "idx_loan_customer", columnList = "customer_id"),
        @Index(name = "idx_loan_type", columnList = "loan_type"),
        @Index(name = "idx_loan_submitted_date", columnList = "submitted_date")
})
@SQLDelete(sql = "UPDATE loan_applications SET is_deleted = true WHERE id = ?")
@Where(clause = "is_deleted = false")
public class LoanApplication extends BaseEntity {

    @Column(name = "application_number", unique = true, nullable = false, length = 50)
    @NotBlank(message = "Хүсэлтийн дугаар заавал байх ёстой")
    private String applicationNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false, foreignKey = @ForeignKey(name = "fk_loan_customer"))
    @NotNull(message = "Харилцагч заавал байх ёстой")
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(name = "loan_type", nullable = false, length = 30)
    @NotNull(message = "Зээлийн төрөл заавал сонгох ёстой")
    private LoanType loanType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @NotNull(message = "Статус заавал байх ёстой")
    private LoanStatus status = LoanStatus.DRAFT;

    // Хүсэх зээлийн мэдээлэл
    @Column(name = "requested_amount", nullable = false, precision = 15, scale = 2)
    @NotNull(message = "Хүсэх дүн заавал бөглөх ёстой")
    @DecimalMin(value = "100000.0", message = "Хамгийн бага зээлийн хэмжээ 100,000₮")
    @DecimalMax(value = "100000000.0", message = "Хамгийн их зээлийн хэмжээ 100,000,000₮")
    private BigDecimal requestedAmount;

    @Column(name = "requested_term_months", nullable = false)
    @NotNull(message = "Хүсэх хугацаа заавал бөглөх ёстой")
    @Min(value = 3, message = "Хамгийн бага хугацаа 3 сар")
    @Max(value = 300, message = "Хамгийн их хугацаа 300 сар")
    private Integer requestedTermMonths;

    @Column(name = "purpose", length = 500)
    @Size(max = 500, message = "Зорилго 500 тэмдэгтээс ихгүй байх ёстой")
    private String purpose;

    @Column(name = "declared_income", precision = 15, scale = 2)
    @DecimalMin(value = "0.0", message = "Орлого сөрөг байж болохгүй")
    private BigDecimal declaredIncome;

    // Зөвшөөрсөн зээлийн мэдээлэл
    @Column(name = "approved_amount", precision = 15, scale = 2)
    @DecimalMin(value = "0.0", message = "Зөвшөөрсөн дүн сөрөг байж болохгүй")
    private BigDecimal approvedAmount;

    @Column(name = "approved_term_months")
    @Min(value = 1, message = "Зөвшөөрсөн хугацаа 1 сараас бага байж болохгүй")
    private Integer approvedTermMonths;

    @Column(name = "approved_rate", precision = 5, scale = 2)
    @DecimalMin(value = "0.0", message = "Хүү сөрөг байж болохгүй")
    @DecimalMax(value = "50.0", message = "Хүү 50%-аас их байж болохгүй")
    private BigDecimal approvedRate;

    @Column(name = "monthly_payment", precision = 15, scale = 2)
    @DecimalMin(value = "0.0", message = "Сарын төлбөр сөрөг байж болохгүй")
    private BigDecimal monthlyPayment;

    // Огнонууд
    @Column(name = "submitted_date")
    private LocalDateTime submittedDate;

    @Column(name = "approved_date")
    private LocalDateTime approvedDate;

    @Column(name = "rejected_date")
    private LocalDateTime rejectedDate;

    @Column(name = "disbursed_date")
    private LocalDateTime disbursedDate;

    @Column(name = "expected_disbursement_date")
    private LocalDate expectedDisbursementDate;

    // Шийдвэрийн мэдээлэл
    @Column(name = "decision_reason", length = 1000)
    @Size(max = 1000, message = "Шийдвэрийн үндэслэл 1000 тэмдэгтээс ихгүй байх ёстой")
    private String decisionReason;

    @Column(name = "risk_score", precision = 5, scale = 2)
    @DecimalMin(value = "0.0", message = "Эрсдэлийн оноо сөрөг байж болохгүй")
    @DecimalMax(value = "100.0", message = "Эрсдэлийн оноо 100-аас их байж болохгүй")
    private BigDecimal riskScore;

    @Column(name = "credit_score", precision = 5, scale = 2)
    @DecimalMin(value = "0.0", message = "Зээлийн оноо сөрөг байж болохгүй")
    @DecimalMax(value = "850.0", message = "Зээлийн оноо 850-аас их байж болохгүй")
    private BigDecimal creditScore;

    // Workflow мэдээлэл
    @Column(name = "current_step", length = 100)
    private String currentStep;

    @Column(name = "assigned_to", length = 100)
    private String assignedTo;

    @Column(name = "priority")
    private Integer priority = 3; // 1=High, 2=Medium, 3=Low

    // Баримт бичгүүд
    @OneToMany(mappedBy = "loanApplication", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Document> documents = new ArrayList<>();

    // Зээлийн төрлүүд
    public enum LoanType {
        CONSUMER("Хэрэглээний зээл", 500000, 50000000, 6, 60),
        BUSINESS("Бизнесийн зээл", 1000000, 100000000, 12, 120),
        MORTGAGE("Орон сууцны зээл", 5000000, 200000000, 60, 300),
        AUTO("Автомашины зээл", 2000000, 50000000, 12, 84),
        EDUCATION("Боловсролын зээл", 500000, 10000000, 12, 120),
        AGRICULTURAL("Хөдөө аж ахуйн зээл", 1000000, 50000000, 12, 60);

        private final String mongolianName;
        private final long minAmount;
        private final long maxAmount;
        private final int minTermMonths;
        private final int maxTermMonths;

        LoanType(String mongolianName, long minAmount, long maxAmount, int minTermMonths, int maxTermMonths) {
            this.mongolianName = mongolianName;
            this.minAmount = minAmount;
            this.maxAmount = maxAmount;
            this.minTermMonths = minTermMonths;
            this.maxTermMonths = maxTermMonths;
        }

        public String getMongolianName() { return mongolianName; }
        public long getMinAmount() { return minAmount; }
        public long getMaxAmount() { return maxAmount; }
        public int getMinTermMonths() { return minTermMonths; }
        public int getMaxTermMonths() { return maxTermMonths; }
    }

    // Constructors
    public LoanApplication() {
        super();
    }

    public LoanApplication(Customer customer, LoanType loanType, BigDecimal requestedAmount, Integer requestedTermMonths) {
        this();
        this.customer = customer;
        this.loanType = loanType;
        this.requestedAmount = requestedAmount;
        this.requestedTermMonths = requestedTermMonths;
        this.applicationNumber = generateApplicationNumber();
    }

    // Business methods
    public void submit() {
        if (this.status == LoanStatus.DRAFT) {
            this.status = LoanStatus.SUBMITTED;
            this.submittedDate = LocalDateTime.now();
        }
    }

    public void approve(BigDecimal amount, Integer termMonths, BigDecimal rate, String reason) {
        this.status = LoanStatus.APPROVED;
        this.approvedAmount = amount;
        this.approvedTermMonths = termMonths;
        this.approvedRate = rate;
        this.decisionReason = reason;
        this.approvedDate = LocalDateTime.now();
        
        // Сарын төлбөр тооцоолох
        this.monthlyPayment = calculateMonthlyPayment(amount, termMonths, rate);
    }

    public void reject(String reason) {
        this.status = LoanStatus.REJECTED;
        this.decisionReason = reason;
        this.rejectedDate = LocalDateTime.now();
    }

    public void disburse() {
        if (this.status == LoanStatus.APPROVED) {
            this.status = LoanStatus.DISBURSED;
            this.disbursedDate = LocalDateTime.now();
        }
    }

    public boolean canBeEdited() {
        return this.status == LoanStatus.DRAFT || this.status == LoanStatus.PENDING_INFO;
    }

    public boolean isFinalStatus() {
        return this.status.isFinalStatus();
    }

    public boolean isActive() {
        return this.status.isActiveStatus();
    }

    private String generateApplicationNumber() {
        // LA + YYYYMMDD + sequential number
        String dateStr = LocalDate.now().toString().replace("-", "");
        return "LA" + dateStr + String.format("%04d", System.currentTimeMillis() % 10000);
    }

    private BigDecimal calculateMonthlyPayment(BigDecimal principal, Integer termMonths, BigDecimal annualRate) {
        if (principal == null || termMonths == null || annualRate == null) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(1200), 10, BigDecimal.ROUND_HALF_UP);
        BigDecimal factor = BigDecimal.ONE.add(monthlyRate).pow(termMonths);
        
        return principal.multiply(monthlyRate).multiply(factor)
                .divide(factor.subtract(BigDecimal.ONE), 2, BigDecimal.ROUND_HALF_UP);
    }

    public String getPriorityText() {
        switch (priority) {
            case 1: return "Өндөр";
            case 2: return "Дунд";
            case 3: return "Бага";
            default: return "Тодорхойгүй";
        }
    }

    // Getters and Setters
    public String getApplicationNumber() { return applicationNumber; }
    public void setApplicationNumber(String applicationNumber) { this.applicationNumber = applicationNumber; }
    
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
    
    public LoanType getLoanType() { return loanType; }
    public void setLoanType(LoanType loanType) { this.loanType = loanType; }
    
    public LoanStatus getStatus() { return status; }
    public void setStatus(LoanStatus status) { this.status = status; }
    
    public BigDecimal getRequestedAmount() { return requestedAmount; }
    public void setRequestedAmount(BigDecimal requestedAmount) { this.requestedAmount = requestedAmount; }
    
    public Integer getRequestedTermMonths() { return requestedTermMonths; }
    public void setRequestedTermMonths(Integer requestedTermMonths) { this.requestedTermMonths = requestedTermMonths; }
    
    public List<Document> getDocuments() { return documents; }
    public void setDocuments(List<Document> documents) { this.documents = documents; }

    // toString
    @Override
    public String toString() {
        return "LoanApplication{" +
                "id=" + getId() +
                ", applicationNumber='" + applicationNumber + '\'' +
                ", loanType=" + loanType +
                ", status=" + status +
                ", requestedAmount=" + requestedAmount +
                ", requestedTermMonths=" + requestedTermMonths +
                '}';
    }
}