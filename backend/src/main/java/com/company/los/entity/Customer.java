package com.company.los.entity;

import com.company.los.entity.BaseEntity;
import com.company.los.enums.CustomerStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Харилцагчийн Entity
 * Customer Entity
 */
@Entity
@Table(name = "customers", indexes = {
        @Index(name = "idx_customer_register_number", columnList = "register_number", unique = true),
        @Index(name = "idx_customer_phone", columnList = "phone"),
        @Index(name = "idx_customer_email", columnList = "email"),
        @Index(name = "idx_customer_type", columnList = "customer_type"),
        @Index(name = "idx_customer_province", columnList = "province")
})
@SQLDelete(sql = "UPDATE customers SET is_deleted = true WHERE id = ?")
@Where(clause = "is_deleted = false")
public class Customer extends BaseEntity {

    // Enum definitions
    public enum CustomerType {
        INDIVIDUAL("INDIVIDUAL", "Хувь хүн"),
        BUSINESS("BUSINESS", "Хуулийн этгээд");

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
    }

    public enum KycStatus {
        PENDING("PENDING", "Хүлээгдэж байгаа"),
        IN_PROGRESS("IN_PROGRESS", "Боловсруулж байгаа"),
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
    }

    @Column(name = "customer_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Харилцагчийн төрөл заавал сонгох ёстой")
    private CustomerType customerType;

    @Column(name = "register_number", unique = true, nullable = false, length = 20)
    @NotBlank(message = "Регистрийн дугаар заавал бөглөх ёстой")
    @Size(min = 8, max = 20, message = "Регистрийн дугаар 8-20 тэмдэгт байх ёстой")
    private String registerNumber;

    // Хувь хүний мэдээлэл
    @Column(name = "first_name", length = 100)
    @Size(max = 100, message = "Нэр 100 тэмдэгтээс ихгүй байх ёстой")
    private String firstName;

    @Column(name = "last_name", length = 100)
    @Size(max = 100, message = "Овог 100 тэмдэгтээс ихгүй байх ёстой")
    private String lastName;

    @Column(name = "middle_name", length = 100)
    @Size(max = 100, message = "Дунд нэр 100 тэмдэгтээс ихгүй байх ёстой")
    private String middleName;

    @Column(name = "date_of_birth")
    @Past(message = "Төрсөн огноо өнгөрсөн огноо байх ёстой")
    private LocalDate dateOfBirth;

    @Column(name = "gender", length = 10)
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(name = "marital_status", length = 20)
    private String maritalStatus; // 'SINGLE', 'MARRIED', 'DIVORCED', 'WIDOWED'

    // Хуулийн этгээдийн мэдээлэл
    @Column(name = "company_name", length = 200)
    @Size(max = 200, message = "Компанийн нэр 200 тэмдэгтээс ихгүй байх ёстой")
    private String companyName;

    @Column(name = "business_type", length = 100)
    @Size(max = 100, message = "Бизнесийн төрөл 100 тэмдэгтээс ихгүй байх ёстой")
    private String businessType;

    @Column(name = "establishment_date")
    private LocalDate establishmentDate;

    @Column(name = "tax_number", length = 50)
    @Size(max = 50, message = "ТТД 50 тэмдэгтээс ихгүй байх ёстой")
    private String taxNumber;

    // Холбоо барих мэдээлэл
    @Column(name = "phone", nullable = false, length = 20)
    @NotBlank(message = "Утасны дугаар заавал бөглөх ёстой")
    @Pattern(regexp = "^[+]?[0-9]{8,15}$", message = "Утасны дугаарын формат буруу")
    private String phone;

    @Column(name = "email", length = 255)
    @Email(message = "И-мэйлийн формат буруу")
    @Size(max = 255, message = "И-мэйл 255 тэмдэгтээс ихгүй байх ёстой")
    private String email;

    // Хаяг
    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "city", length = 100)
    @Size(max = 100, message = "Хот 100 тэмдэгтээс ихгүй байх ёстой")
    private String city;

    @Column(name = "state", length = 100)
    @Size(max = 100, message = "Муж/Аймаг 100 тэмдэгтээс ихгүй байх ёстой")
    private String state;

    @Column(name = "province", length = 100)
    @Size(max = 100, message = "Аймаг 100 тэмдэгтээс ихгүй байх ёстой")
    private String province;

    @Column(name = "zip_code", length = 20)
    @Size(max = 20, message = "Шуудангийн код 20 тэмдэгтээс ихгүй байх ёстой")
    private String zipCode;

    @Column(name = "postal_code", length = 20)
    @Size(max = 20, message = "Шуудангийн код 20 тэмдэгтээс ихгүй байх ёстой")
    private String postalCode;

    @Column(name = "country", length = 100)
    @Size(max = 100, message = "Улс 100 тэмдэгтээс ихгүй байх ёстой")
    private String country = "Mongolia";

    // Ажлын мэдээлэл
    @Column(name = "employer_name", length = 200)
    @Size(max = 200, message = "Ажлын байрны нэр 200 тэмдэгтээс ихгүй байх ёстой")
    private String employerName;

    @Column(name = "job_title", length = 100)
    @Size(max = 100, message = "Албан тушаал 100 тэмдэгтээс ихгүй байх ёстой")
    private String jobTitle;

    @Column(name = "work_phone", length = 20)
    @Pattern(regexp = "^[+]?[0-9]{8,15}$", message = "Ажlын утасны дугаарын формат буруу")
    private String workPhone;

    @Column(name = "work_address", columnDefinition = "TEXT")
    private String workAddress;

    @Column(name = "monthly_income", precision = 15, scale = 2)
    @DecimalMin(value = "0.0", message = "Орлого сөрөг байж болохгүй")
    private BigDecimal monthlyIncome;

    @Column(name = "employment_start_date")
    private LocalDate employmentStartDate;

    @Column(name = "work_experience_years")
    private Integer workExperienceYears;

    // Банкны мэдээлэл
    @Column(name = "bank_name", length = 100)
    @Size(max = 100, message = "Банкны нэр 100 тэмдэгтээс ихгүй байх ёстой")
    private String bankName;

    @Column(name = "account_number", length = 50)
    @Size(max = 50, message = "Дансны дугаар 50 тэмдэгтээс ихгүй байх ёстой")
    private String accountNumber;

    // Business related fields
    @Column(name = "business_registration_number", length = 50)
    @Size(max = 50, message = "Бизнес регистрийн дугаар 50 тэмдэгтээс ихгүй байх ёстой")
    private String businessRegistrationNumber;

    @Column(name = "annual_revenue", precision = 15, scale = 2)
    @DecimalMin(value = "0.0", message = "Жилийн орлого сөрөг байж болохгүй")
    private BigDecimal annualRevenue;

    // KYC статус
    @Column(name = "kyc_status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "KYC статус заавал байх ёстой")
    private KycStatus kycStatus = KycStatus.PENDING;

    @Column(name = "kyc_completed_at")
    private LocalDateTime kycCompletedAt;

    @Column(name = "kyc_verified_by", length = 100)
    @Size(max = 100, message = "KYC хянасан хүн 100 тэмдэгтээс ихгүй байх ёстой")
    private String kycVerifiedBy;

    @Column(name = "risk_rating", length = 20)
    private String riskRating = "LOW"; // 'LOW', 'MEDIUM', 'HIGH'

    // Менежментийн мэдээлэл
    @Column(name = "assigned_to", length = 100)
    @Size(max = 100, message = "Хариуцагч 100 тэмдэгтээс ихгүй байх ёстой")
    private String assignedTo;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // Active status for compatibility
    @Column(name = "is_active")
    private Boolean isActive = true;

    // Status field for compatibility with tests
    @Column(name = "status", length = 20)
    @Enumerated(EnumType.STRING)
    private CustomerStatus status = CustomerStatus.ACTIVE;

    // Preferred language field for compatibility
    @Column(name = "preferred_language", length = 10)
    private String preferredLanguage = "mn";

    // Registration date field for compatibility
    @Column(name = "registration_date")
    private LocalDateTime registrationDate;

    // Last updated field for compatibility
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    // Зээлийн хүсэлтүүд
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LoanApplication> loanApplications = new ArrayList<>();

    // Баримт бичгүүд
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Document> documents = new ArrayList<>();

    // Constructors
    public Customer() {
        super();
        this.registrationDate = LocalDateTime.now();
        this.lastUpdated = LocalDateTime.now();
    }

    public Customer(CustomerType customerType, String registerNumber, String phone) {
        this();
        this.customerType = customerType;
        this.registerNumber = registerNumber;
        this.phone = phone;
    }

    // Business methods
    public String getFullName() {
        if (CustomerType.INDIVIDUAL.equals(customerType)) {
            StringBuilder name = new StringBuilder();
            if (lastName != null && !lastName.trim().isEmpty()) {
                name.append(lastName.trim());
            }
            if (firstName != null && !firstName.trim().isEmpty()) {
                if (name.length() > 0) name.append(" ");
                name.append(firstName.trim());
            }
            if (middleName != null && !middleName.trim().isEmpty()) {
                if (name.length() > 0) name.append(" ");
                name.append(middleName.trim());
            }
            return name.toString();
        } else {
            return companyName != null ? companyName : "";
        }
    }

    public String getDisplayName() {
        String name = getFullName().trim();
        return !name.isEmpty() ? name : registerNumber;
    }

    public boolean isIndividual() {
        return CustomerType.INDIVIDUAL.equals(customerType);
    }

    public boolean isBusiness() {
        return CustomerType.BUSINESS.equals(customerType);
    }

    public boolean isKycCompleted() {
        return KycStatus.COMPLETED.equals(kycStatus);
    }

    // Compatibility method for DTO
    public Boolean getIsKycCompleted() {
        return isKycCompleted();
    }

    public void completeKyc() {
        this.kycStatus = KycStatus.COMPLETED;
        this.kycCompletedAt = LocalDateTime.now();
    }

    public int getAge() {
        if (dateOfBirth == null) return 0;
        return LocalDate.now().getYear() - dateOfBirth.getYear();
    }

    public String getFormattedAddress() {
        StringBuilder addressBuilder = new StringBuilder();
        if (address != null && !address.trim().isEmpty()) {
            addressBuilder.append(address.trim());
        }
        if (city != null && !city.trim().isEmpty()) {
            if (addressBuilder.length() > 0) addressBuilder.append(", ");
            addressBuilder.append(city.trim());
        }
        if (state != null && !state.trim().isEmpty()) {
            if (addressBuilder.length() > 0) addressBuilder.append(", ");
            addressBuilder.append(state.trim());
        }
        if (province != null && !province.trim().isEmpty()) {
            if (addressBuilder.length() > 0) addressBuilder.append(", ");
            addressBuilder.append(province.trim());
        }
        if (zipCode != null && !zipCode.trim().isEmpty()) {
            if (addressBuilder.length() > 0) addressBuilder.append(" ");
            addressBuilder.append(zipCode.trim());
        }
        return addressBuilder.toString();
    }

    // Compatibility methods for test classes
    public String getSocialSecurityNumber() {
        return this.registerNumber;
    }

    public void setSocialSecurityNumber(String socialSecurityNumber) {
        this.registerNumber = socialSecurityNumber;
    }

    public CustomerStatus getStatus() {
        return this.status;
    }

    public void setStatus(CustomerStatus status) {
        this.status = status;
        this.isActive = (status == CustomerStatus.ACTIVE);
    }

    public LocalDateTime getRegistrationDate() {
        return this.registrationDate != null ? this.registrationDate : this.getCreatedAt();
    }

    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
        if (this.getCreatedAt() == null) {
            this.setCreatedAt(registrationDate);
        }
    }

    public LocalDateTime getLastUpdated() {
        return this.lastUpdated != null ? this.lastUpdated : this.getUpdatedAt();
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
        this.setUpdatedAt(lastUpdated);
    }

    public String getPreferredLanguage() {
        return this.preferredLanguage;
    }

    public void setPreferredLanguage(String preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }

    // Getters and Setters
    public CustomerType getCustomerType() { return customerType; }
    public void setCustomerType(CustomerType customerType) { this.customerType = customerType; }

    public String getRegisterNumber() { return registerNumber; }
    public void setRegisterNumber(String registerNumber) { this.registerNumber = registerNumber; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getMiddleName() { return middleName; }
    public void setMiddleName(String middleName) { this.middleName = middleName; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    // Compatibility method for DTO
    public LocalDate getBirthDate() { return getDateOfBirth(); }
    public void setBirthDate(LocalDate birthDate) { this.dateOfBirth = birthDate; }

    public Gender getGender() { return gender; }
    public void setGender(Gender gender) { this.gender = gender; }

    public String getMaritalStatus() { return maritalStatus; }
    public void setMaritalStatus(String maritalStatus) { this.maritalStatus = maritalStatus; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getBusinessType() { return businessType; }
    public void setBusinessType(String businessType) { this.businessType = businessType; }

    public LocalDate getEstablishmentDate() { return establishmentDate; }
    public void setEstablishmentDate(LocalDate establishmentDate) { this.establishmentDate = establishmentDate; }

    public String getTaxNumber() { return taxNumber; }
    public void setTaxNumber(String taxNumber) { this.taxNumber = taxNumber; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }

    public String getZipCode() { return zipCode; }
    public void setZipCode(String zipCode) { this.zipCode = zipCode; }

    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getEmployerName() { return employerName; }
    public void setEmployerName(String employerName) { this.employerName = employerName; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public String getWorkPhone() { return workPhone; }
    public void setWorkPhone(String workPhone) { this.workPhone = workPhone; }

    public String getWorkAddress() { return workAddress; }
    public void setWorkAddress(String workAddress) { this.workAddress = workAddress; }

    public BigDecimal getMonthlyIncome() { return monthlyIncome; }
    public void setMonthlyIncome(BigDecimal monthlyIncome) { this.monthlyIncome = monthlyIncome; }

    public LocalDate getEmploymentStartDate() { return employmentStartDate; }
    public void setEmploymentStartDate(LocalDate employmentStartDate) { this.employmentStartDate = employmentStartDate; }

    public Integer getWorkExperienceYears() { return workExperienceYears; }
    public void setWorkExperienceYears(Integer workExperienceYears) { this.workExperienceYears = workExperienceYears; }

    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getBusinessRegistrationNumber() { return businessRegistrationNumber; }
    public void setBusinessRegistrationNumber(String businessRegistrationNumber) { this.businessRegistrationNumber = businessRegistrationNumber; }

    public BigDecimal getAnnualRevenue() { return annualRevenue; }
    public void setAnnualRevenue(BigDecimal annualRevenue) { this.annualRevenue = annualRevenue; }

    public KycStatus getKycStatus() { return kycStatus; }
    public void setKycStatus(KycStatus kycStatus) { this.kycStatus = kycStatus; }

    public LocalDateTime getKycCompletedAt() { return kycCompletedAt; }
    public void setKycCompletedAt(LocalDateTime kycCompletedAt) { this.kycCompletedAt = kycCompletedAt; }

    public String getKycVerifiedBy() { return kycVerifiedBy; }
    public void setKycVerifiedBy(String kycVerifiedBy) { this.kycVerifiedBy = kycVerifiedBy; }

    public String getRiskRating() { return riskRating; }
    public void setRiskRating(String riskRating) { this.riskRating = riskRating; }

    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public List<LoanApplication> getLoanApplications() { return loanApplications; }
    public void setLoanApplications(List<LoanApplication> loanApplications) { this.loanApplications = loanApplications; }

    public List<Document> getDocuments() { return documents; }
    public void setDocuments(List<Document> documents) { this.documents = documents; }

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