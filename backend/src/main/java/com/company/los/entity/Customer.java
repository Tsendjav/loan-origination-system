package com.company.los.entity;

import com.company.los.enums.CustomerStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.Arrays;

/**
 * Харилцагчийн Entity
 * Customer Entity
 * ⭐ ЗАСВАРЛАСАН - preferredLanguage талбар нэмэгдсэн ⭐
 * 
 * @version 1.3 - preferredLanguage field added
 */
@Entity
@Table(name = "customers", indexes = {
        @Index(name = "idx_customers_register_number", columnList = "register_number", unique = true),
        @Index(name = "idx_customers_email", columnList = "email", unique = true),
        @Index(name = "idx_customers_phone", columnList = "phone"),
        @Index(name = "idx_customers_customer_type", columnList = "customer_type"),
        @Index(name = "idx_customers_kyc_status", columnList = "kyc_status"),
        @Index(name = "idx_customers_status", columnList = "status"),
        @Index(name = "idx_customers_is_active", columnList = "is_active")
})
@SQLDelete(sql = "UPDATE customers SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
public class Customer extends BaseEntity {

    public enum CustomerType {
        INDIVIDUAL("INDIVIDUAL", "Хувь хүн"),
        BUSINESS("BUSINESS", "Байгууллага");

        private final String code;
        private final String mongolianName;

        CustomerType(String code, String mongolianName) {
            this.code = code;
            this.mongolianName = mongolianName;
        }

        public String getCode() { return code; }
        public String getMongolianName() { return mongolianName; }
    }

    public enum Gender {
        MALE("MALE", "Эрэгтэй"),
        FEMALE("FEMALE", "Эмэгтэй"),
        OTHER("OTHER", "Бусад");

        private final String code;
        private final String mongolianName;

        Gender(String code, String mongolianName) {
            this.code = code;
            this.mongolianName = mongolianName;
        }

        public String getCode() { return code; }
        public String getMongolianName() { return mongolianName; }

        public static Gender fromCode(String code) {
            return Arrays.stream(Gender.values())
                    .filter(gender -> gender.getCode().equalsIgnoreCase(code) || (code.length() == 1 && gender.getCode().startsWith(code)))
                    .findFirst()
                    .orElse(null);
        }
    }

    public enum KycStatus {
        PENDING("PENDING", "Хүлээгдэж байна"),
        IN_PROGRESS("IN_PROGRESS", "Шалгагдаж байна"),
        COMPLETED("COMPLETED", "Дууссан"),
        REJECTED("REJECTED", "Татгалзсан"),
        FAILED("FAILED", "Амжилтгүй");

        private final String code;
        private final String mongolianName;

        KycStatus(String code, String mongolianName) {
            this.code = code;
            this.mongolianName = mongolianName;
        }

        public String getCode() { return code; }
        public String getMongolianName() { return mongolianName; }

        public boolean isCompleted() { return this == COMPLETED; }
        public boolean isPending() { return this == PENDING; }
        public boolean isInProgress() { return this == IN_PROGRESS; }
        public boolean isRejected() { return this == REJECTED; }
        public boolean isFailed() { return this == FAILED; }
        public boolean requiresAction() { return this != COMPLETED; }

        public String getBadgeClass() {
            switch (this) {
                case PENDING: return "badge-warning";
                case IN_PROGRESS: return "badge-info";
                case COMPLETED: return "badge-success";
                case REJECTED:
                case FAILED: return "badge-danger";
                default: return "badge-secondary";
            }
        }

        @Override
        public String toString() { return mongolianName; }
    }

    @Column(name = "customer_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Харилцагчийн төрөл заавал сонгох ёстой")
    private CustomerType customerType;

    @Column(name = "first_name", length = 100)
    @Size(max = 100, message = "Нэр 100 тэмдэгтээс ихгүй байх ёстой")
    private String firstName;

    @Column(name = "last_name", length = 100)
    @Size(max = 100, message = "Овог 100 тэмдэгтээс ихгүй байх ёстой")
    private String lastName;

    @Column(name = "register_number", unique = true, nullable = false, length = 20)
    @NotBlank(message = "Регистрийн дугаар заавал байх ёстой")
    @Size(max = 20, message = "Регистрийн дугаар 20 тэмдэгтээс ихгүй байх ёстой")
    private String registerNumber;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "gender", length = 10)
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(name = "social_security_number", length = 20)
    @Size(max = 20, message = "Нийгмийн даатгалын дугаар 20 тэмдэгтээс ихгүй байх ёстой")
    private String socialSecurityNumber;

    @Column(name = "nationality", length = 50)
    @Size(max = 50, message = "Иргэншил 50 тэмдэгтээс ихгүй байх ёстой")
    private String nationality = "Mongolian";

    @Column(name = "phone", nullable = false, length = 20)
    @NotBlank(message = "Утасны дугаар заавал байх ёстой")
    @Size(max = 20, message = "Утасны дугаар 20 тэмдэгтээс ихгүй байх ёстой")
    private String phone;

    @Column(name = "email", unique = true, nullable = false, length = 100)
    @NotBlank(message = "И-мэйл хаяг заавал байх ёстой")
    @Email(message = "И-мэйл хаяг буруу байна")
    @Size(max = 100, message = "И-мэйл хаяг 100 тэмдэгтээс ихгүй байх ёстой")
    private String email;

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

    @Column(name = "employer_name", length = 200)
    @Size(max = 200, message = "Ажил олгогчийн нэр 200 тэмдэгтээс ихгүй байх ёстой")
    private String employerName;

    @Column(name = "job_title", length = 100)
    @Size(max = 100, message = "Албан тушаал 100 тэмдэгтээс ихгүй байх ёстой")
    private String jobTitle;

    @Column(name = "employment_status", length = 50)
    @Size(max = 50, message = "Ажлын статус 50 тэмдэгтээс ихгүй байх ёстой")
    private String employmentStatus;

    @Column(name = "work_experience_years")
    @Min(value = 0, message = "Ажлын туршлага сөрөг байж болохгүй")
    @Max(value = 50, message = "Ажлын туршлага 50 жилээс их байж болохгүй")
    private Integer workExperienceYears;

    @Column(name = "monthly_income", precision = 15, scale = 2)
    @DecimalMin(value = "0.0", message = "Сарын орлого сөрөг байж болохгүй")
    private BigDecimal monthlyIncome;

    // ⭐ НЭМЭГДСЭН ТАЛБАР: Дуртай хэл ⭐
    @Column(name = "preferred_language", length = 10)
    @Size(max = 10, message = "Дуртай хэл 10 тэмдэгтээс ихгүй байх ёстой")
    private String preferredLanguage = "mn"; // Default: Mongolian

    @Column(name = "company_name", length = 200)
    @Size(max = 200, message = "Компанийн нэр 200 тэмдэгтээс ихгүй байх ёстой")
    private String companyName;

    @Column(name = "business_registration_number", length = 20)
    @Size(max = 20, message = "Бизнес регистрийн дугаар 20 тэмдэгтээс ихгүй байх ёстой")
    private String businessRegistrationNumber;

    @Column(name = "tax_number", length = 20)
    @Size(max = 20, message = "Татварын дугаар 20 тэмдэгтээс ихгүй байх ёстой")
    private String taxNumber;

    @Column(name = "business_type", length = 100)
    @Size(max = 100, message = "Бизнесийн төрөл 100 тэмдэгтээс ихгүй байх ёстой")
    private String businessType;

    @Column(name = "annual_revenue", precision = 15, scale = 2)
    @DecimalMin(value = "0.0", message = "Жилийн орлого сөрөг байж болохгүй")
    private BigDecimal annualRevenue;

    @Column(name = "credit_score")
    @Min(value = 300, message = "Зээлийн оноо 300-аас бага байж болохгүй")
    @Max(value = 850, message = "Зээлийн оноо 850-аас их байж болохгүй")
    private Integer creditScore;

    @Column(name = "kyc_status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "KYC статус заавал байх ёстой")
    private KycStatus kycStatus = KycStatus.PENDING;

    @Column(name = "kyc_completed_at")
    private LocalDateTime kycCompletedAt;

    @Column(name = "kyc_verified_by", length = 100)
    @Size(max = 100, message = "KYC хянасан хүн 100 тэмдэгтээс ихгүй байх ёстой")
    private String kycVerifiedBy;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "status", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Статус заавал байх ёстой")
    private CustomerStatus status = CustomerStatus.PENDING_VERIFICATION;

    @Column(name = "registration_date")
    private LocalDateTime registrationDate;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LoanApplication> loanApplications = new ArrayList<>();

    // Constructors
    public Customer() {
        super();
        this.customerType = CustomerType.INDIVIDUAL;
        this.kycStatus = KycStatus.PENDING;
        this.isActive = true;
        this.status = CustomerStatus.PENDING_VERIFICATION;
        this.registrationDate = LocalDateTime.now();
        this.preferredLanguage = "mn"; // Default language
    }

    public Customer(CustomerType customerType, String firstName, String lastName,
                   String registerNumber, String phone, String email) {
        this();
        this.customerType = customerType;
        this.firstName = firstName;
        this.lastName = lastName;
        this.registerNumber = registerNumber;
        this.phone = phone;
        this.email = email;
    }

    // Business methods
    public void completeKyc(String verifiedBy) {
        this.kycStatus = KycStatus.COMPLETED;
        this.kycCompletedAt = LocalDateTime.now();
        this.kycVerifiedBy = verifiedBy;
        this.setUpdatedAt(LocalDateTime.now());
    }

    public void rejectKyc(String verifiedBy) {
        this.kycStatus = KycStatus.REJECTED;
        this.kycVerifiedBy = verifiedBy;
        this.setUpdatedAt(LocalDateTime.now());
    }

    public void activate() {
        this.isActive = true;
        this.status = CustomerStatus.ACTIVE;
        this.setUpdatedAt(LocalDateTime.now());
    }

    public void deactivate() {
        this.isActive = false;
        this.status = CustomerStatus.INACTIVE;
        this.setUpdatedAt(LocalDateTime.now());
    }

    public boolean canApplyForLoan() {
        return isActive && kycStatus == KycStatus.COMPLETED;
    }

    public String getDisplayName() {
        if (customerType == CustomerType.BUSINESS && companyName != null) {
            return companyName;
        }
        StringBuilder name = new StringBuilder();
        if (firstName != null) name.append(firstName);
        if (lastName != null) {
            if (name.length() > 0) name.append(" ");
            name.append(lastName);
        }
        return name.length() > 0 ? name.toString() : registerNumber;
    }

    public String getFullName() {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        }
        return getDisplayName();
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

    public boolean isKycPending() {
        return kycStatus == KycStatus.PENDING;
    }

    public Integer getAge() {
        if (birthDate == null) return null;
        return LocalDate.now().getYear() - birthDate.getYear();
    }

    public String getGenderDisplay() {
        return gender != null ? gender.getMongolianName() : "";
    }

    public String getKycStatusDisplay() {
        return kycStatus != null ? kycStatus.getMongolianName() : "Тодорхойгүй";
    }

    public String getCustomerTypeDisplay() {
        return customerType != null ? customerType.getMongolianName() : "Тодорхойгүй";
    }

    public boolean hasActiveLoan() {
        return loanApplications != null && loanApplications.stream()
                .anyMatch(app -> app.getStatus() == LoanApplication.ApplicationStatus.APPROVED ||
                                app.getStatus() == LoanApplication.ApplicationStatus.DISBURSED);
    }

    public int getActiveLoanCount() {
        if (loanApplications == null) return 0;
        return (int) loanApplications.stream()
                .filter(app -> app.getStatus() == LoanApplication.ApplicationStatus.APPROVED ||
                              app.getStatus() == LoanApplication.ApplicationStatus.DISBURSED)
                .count();
    }

    // Getters and Setters
    public CustomerType getCustomerType() { return customerType; }
    public void setCustomerType(CustomerType customerType) { this.customerType = customerType; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getRegisterNumber() { return registerNumber; }
    public void setRegisterNumber(String registerNumber) { this.registerNumber = registerNumber; }

    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }

    public Gender getGender() { return gender; }
    public void setGender(Gender gender) { this.gender = gender; }

    // Шинээр нэмэгдсэн setGender метод нь String утгыг Gender enum-д хөрвүүлнэ
    public void setGender(String genderString) {
        this.gender = Gender.fromCode(genderString);
    }

    public String getSocialSecurityNumber() { return socialSecurityNumber; }
    public void setSocialSecurityNumber(String socialSecurityNumber) { this.socialSecurityNumber = socialSecurityNumber; }

    public String getNationality() { return nationality; }
    public void setNationality(String nationality) { this.nationality = nationality; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }

    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }

    public String getEmployerName() { return employerName; }
    public void setEmployerName(String employerName) { this.employerName = employerName; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public String getEmploymentStatus() { return employmentStatus; }
    public void setEmploymentStatus(String employmentStatus) { this.employmentStatus = employmentStatus; }

    public Integer getWorkExperienceYears() { return workExperienceYears; }
    public void setWorkExperienceYears(Integer workExperienceYears) { this.workExperienceYears = workExperienceYears; }

    public BigDecimal getMonthlyIncome() { return monthlyIncome; }
    public void setMonthlyIncome(BigDecimal monthlyIncome) { this.monthlyIncome = monthlyIncome; }

    // ⭐ НЭМЭГДСЭН: preferredLanguage getter/setter ⭐
    public String getPreferredLanguage() { return preferredLanguage; }
    public void setPreferredLanguage(String preferredLanguage) { this.preferredLanguage = preferredLanguage; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getBusinessRegistrationNumber() { return businessRegistrationNumber; }
    public void setBusinessRegistrationNumber(String businessRegistrationNumber) { this.businessRegistrationNumber = businessRegistrationNumber; }

    public String getTaxNumber() { return taxNumber; }
    public void setTaxNumber(String taxNumber) { this.taxNumber = taxNumber; }

    public String getBusinessType() { return businessType; }
    public void setBusinessType(String businessType) { this.businessType = businessType; }

    public BigDecimal getAnnualRevenue() { return annualRevenue; }
    public void setAnnualRevenue(BigDecimal annualRevenue) { this.annualRevenue = annualRevenue; }

    public Integer getCreditScore() { return creditScore; }
    public void setCreditScore(Integer creditScore) { this.creditScore = creditScore; }

    public KycStatus getKycStatus() { return kycStatus; }
    public void setKycStatus(KycStatus kycStatus) { this.kycStatus = kycStatus; }

    public LocalDateTime getKycCompletedAt() { return kycCompletedAt; }
    public void setKycCompletedAt(LocalDateTime kycCompletedAt) { this.kycCompletedAt = kycCompletedAt; }

    public String getKycVerifiedBy() { return kycVerifiedBy; }
    public void setKycVerifiedBy(String kycVerifiedBy) { this.kycVerifiedBy = kycVerifiedBy; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public CustomerStatus getStatus() { return status; }
    public void setStatus(CustomerStatus status) { this.status = status; }

    public LocalDateTime getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(LocalDateTime registrationDate) { this.registrationDate = registrationDate; }

    public List<LoanApplication> getLoanApplications() { return loanApplications; }
    public void setLoanApplications(List<LoanApplication> loanApplications) {
        this.loanApplications = loanApplications != null ? loanApplications : new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Customer customer = (Customer) o;
        return Objects.equals(getId(), customer.getId()) &&
               Objects.equals(registerNumber, customer.registerNumber) &&
               Objects.equals(email, customer.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), registerNumber, email);
    }

    @Override
    public String toString() {
        return "Customer{" +
               "id=" + getId() +
               ", customerType=" + customerType +
               ", firstName='" + firstName + '\'' +
               ", lastName='" + lastName + '\'' +
               ", registerNumber='" + registerNumber + '\'' +
               ", phone='" + phone + '\'' +
               ", email='" + email + '\'' +
               ", kycStatus=" + kycStatus +
               ", status=" + status +
               ", preferredLanguage='" + preferredLanguage + '\'' +
               '}';
    }
}