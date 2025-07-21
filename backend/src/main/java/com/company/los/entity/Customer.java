package com.los.entity;

import com.los.common.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Харилцагчийн Entity
 * Customer Entity
 */
@Entity
@Table(name = "customers", indexes = {
        @Index(name = "idx_customer_register_number", columnList = "register_number", unique = true),
        @Index(name = "idx_customer_phone", columnList = "phone"),
        @Index(name = "idx_customer_email", columnList = "email"),
        @Index(name = "idx_customer_type", columnList = "customer_type")
})
@SQLDelete(sql = "UPDATE customers SET is_deleted = true WHERE id = ?")
@Where(clause = "is_deleted = false")
public class Customer extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "customer_type", nullable = false, length = 20)
    private CustomerType customerType;

    // Хувь хүний мэдээлэл
    @Column(name = "first_name", length = 100)
    @Size(max = 100, message = "Нэр 100 тэмдэгтээс ихгүй байх ёстой")
    private String firstName;

    @Column(name = "last_name", length = 100)
    @Size(max = 100, message = "Овог 100 тэмдэгтээс ихгүй байх ёстой")
    private String lastName;

    @Column(name = "register_number", unique = true, nullable = false, length = 20)
    @NotBlank(message = "Регистрийн дугаар заавал бөглөх ёстой")
    @Size(min = 8, max = 20, message = "Регистрийн дугаар 8-20 тэмдэгт байх ёстой")
    private String registerNumber;

    @Column(name = "birth_date")
    @Past(message = "Төрсөн огноо өнгөрсөн огноо байх ёстой")
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 10)
    private Gender gender;

    // Холбоо барих мэдээлэл
    @Column(name = "phone", nullable = false, length = 20)
    @NotBlank(message = "Утасны дугаар заавал бөглөх ёстой")
    @Pattern(regexp = "^[+]?[0-9]{8,15}$", message = "Утасны дугаарын формат буруу")
    private String phone;

    @Column(name = "email", length = 100)
    @Email(message = "И-мэйлийн формат буруу")
    @Size(max = 100, message = "И-мэйл 100 тэмдэгтээс ихгүй байх ёстой")
    private String email;

    // Хаяг
    @Column(name = "address", length = 500)
    @Size(max = 500, message = "Хаяг 500 тэмдэгтээс ихгүй байх ёстой")
    private String address;

    @Column(name = "city", length = 100)
    @Size(max = 100, message = "Хот 100 тэмдэгтээс ихгүй байх ёстой")
    private String city;

    @Column(name = "province", length = 100)
    @Size(max = 100, message = "Аймаг 100 тэмдэгтээс ихгүй байх ёстой")
    private String province;

    @Column(name = "postal_code", length = 10)
    @Size(max = 10, message = "Шуудангийн код 10 тэмдэгтээс ихгүй байх ёстой")
    private String postalCode;

    // Ажлын мэдээлэл
    @Column(name = "employer_name", length = 200)
    @Size(max = 200, message = "Ажлын байрны нэр 200 тэмдэгтээс ихгүй байх ёстой")
    private String employerName;

    @Column(name = "job_title", length = 100)
    @Size(max = 100, message = "Албан тушаал 100 тэмдэгтээс ихгүй байх ёстой")
    private String jobTitle;

    @Column(name = "work_experience_years")
    @Min(value = 0, message = "Ажлын туршлага сөрөг байж болохгүй")
    @Max(value = 50, message = "Ажлын туршлага 50 жилээс их байж болохгүй")
    private Integer workExperienceYears;

    @Column(name = "monthly_income", precision = 15, scale = 2)
    @DecimalMin(value = "0.0", message = "Орлого сөрөг байж болохгүй")
    private BigDecimal monthlyIncome;

    // Хуулийн этгээдийн мэдээлэл
    @Column(name = "company_name", length = 200)
    @Size(max = 200, message = "Компанийн нэр 200 тэмдэгтээс ихгүй байх ёстой")
    private String companyName;

    @Column(name = "business_registration_number", length = 20)
    @Size(max = 20, message = "ХЭ-ийн регистр 20 тэмдэгтээс ихгүй байх ёстой")
    private String businessRegistrationNumber;

    @Column(name = "tax_number", length = 20)
    @Size(max = 20, message = "ТТД 20 тэмдэгтээс ихгүй байх ёстой")
    private String taxNumber;

    @Column(name = "business_type", length = 100)
    @Size(max = 100, message = "Бизнесийн төрөл 100 тэмдэгтээс ихгүй байх ёстой")
    private String businessType;

    @Column(name = "annual_revenue", precision = 18, scale = 2)
    @DecimalMin(value = "0.0", message = "Жилийн орлого сөрөг байж болохгүй")
    private BigDecimal annualRevenue;

    // KYC статус
    @Enumerated(EnumType.STRING)
    @Column(name = "kyc_status", nullable = false, length = 20)
    private KycStatus kycStatus = KycStatus.PENDING;

    @Column(name = "kyc_completed_at")
    private LocalDate kycCompletedAt;

    // Зээлийн хүсэлтүүд
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LoanApplication> loanApplications = new ArrayList<>();

    // Баримт бичгүүд
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Document> documents = new ArrayList<>();

    // Enums
    public enum CustomerType {
        INDIVIDUAL("Хувь хүн"),
        BUSINESS("Хуулийн этгээд");

        private final String mongolianName;

        CustomerType(String mongolianName) {
            this.mongolianName = mongolianName;
        }

        public String getMongolianName() {
            return mongolianName;
        }
    }

    public enum Gender {
        MALE("Эрэгтэй"),
        FEMALE("Эмэгтэй"),
        OTHER("Бусад");

        private final String mongolianName;

        Gender(String mongolianName) {
            this.mongolianName = mongolianName;
        }

        public String getMongolianName() {
            return mongolianName;
        }
    }

    public enum KycStatus {
        PENDING("Хүлээгдэж байгаа"),
        IN_PROGRESS("Явцад байгаа"),
        COMPLETED("Дууссан"),
        FAILED("Амжилтгүй");

        private final String mongolianName;

        KycStatus(String mongolianName) {
            this.mongolianName = mongolianName;
        }

        public String getMongolianName() {
            return mongolianName;
        }
    }

    // Constructors
    public Customer() {
        super();
    }

    public Customer(CustomerType customerType, String registerNumber, String phone) {
        this();
        this.customerType = customerType;
        this.registerNumber = registerNumber;
        this.phone = phone;
    }

    // Business methods
    public String getFullName() {
        if (customerType == CustomerType.INDIVIDUAL) {
            return (lastName != null ? lastName : "") + " " + (firstName != null ? firstName : "");
        } else {
            return companyName != null ? companyName : "";
        }
    }

    public String getDisplayName() {
        String name = getFullName().trim();
        return !name.isEmpty() ? name : registerNumber;
    }

    public boolean isIndividual() {
        return customerType == CustomerType.INDIVIDUAL;
    }

    public boolean isBusiness() {
        return customerType == CustomerType.BUSINESS;
    }

    public boolean isKycCompleted() {
        return kycStatus == KycStatus.COMPLETED;
    }

    public void completeKyc() {
        this.kycStatus = KycStatus.COMPLETED;
        this.kycCompletedAt = LocalDate.now();
    }

    public int getAge() {
        if (birthDate == null) return 0;
        return LocalDate.now().getYear() - birthDate.getYear();
    }

    // Getters and Setters
    // [Энд бүх getter/setter методуудыг оруулах - товчруулсан]
    
    public CustomerType getCustomerType() { return customerType; }
    public void setCustomerType(CustomerType customerType) { this.customerType = customerType; }
    
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    
    public String getRegisterNumber() { return registerNumber; }
    public void setRegisterNumber(String registerNumber) { this.registerNumber = registerNumber; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public KycStatus getKycStatus() { return kycStatus; }
    public void setKycStatus(KycStatus kycStatus) { this.kycStatus = kycStatus; }
    
    public List<LoanApplication> getLoanApplications() { return loanApplications; }
    public void setLoanApplications(List<LoanApplication> loanApplications) { this.loanApplications = loanApplications; }

    // toString
    @Override
    public String toString() {
        return "Customer{" +
                "id=" + getId() +
                ", customerType=" + customerType +
                ", registerNumber='" + registerNumber + '\'' +
                ", fullName='" + getFullName() + '\'' +
                ", phone='" + phone + '\'' +
                ", kycStatus=" + kycStatus +
                '}';
    }
}