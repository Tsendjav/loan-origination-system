package com.company.los.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.company.los.entity.Customer;
import com.company.los.enums.CustomerStatus; // CustomerStatus-г импортлох
import jakarta.validation.constraints.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Харилцагчийн DTO
 * Customer Data Transfer Object
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomerDto {

    private static final Logger logger = LoggerFactory.getLogger(CustomerDto.class);

    private UUID id;

    @NotNull(message = "Харилцагчийн төрөл заавал сонгох ёстой")
    private Customer.CustomerType customerType;

    // Хувь хүний мэдээлэл
    @Size(max = 100, message = "Нэр 100 тэмдэгтээс ихгүй байх ёстой")
    private String firstName;

    @Size(max = 100, message = "Овог 100 тэмдэгтээс ихгүй байх ёстой")
    private String lastName;

    @NotBlank(message = "Регистрийн дугаар заавал байх ёстой")
    @Size(max = 20, message = "Регистрийн дугаар 20 тэмдэгтээс ихгүй байх ёстой")
    private String registerNumber;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    private Customer.Gender gender;

    // Холбоо барих мэдээлэл
    @NotBlank(message = "Утасны дугаар заавал байх ёстой")
    @Size(max = 20, message = "Утасны дугаар 20 тэмдэгтээс ихгүй байх ёстой")
    private String phone;

    @Email(message = "И-мэйл хаяг буруу байна")
    @Size(max = 100, message = "И-мэйл хаяг 100 тэмдэгтээс ихгүй байх ёстой")
    private String email;

    // Хаягийн мэдээлэл
    @Size(max = 500, message = "Хаяг 500 тэмдэгтээс ихгүй байх ёстой")
    private String address;

    @Size(max = 100, message = "Хот 100 тэмдэгтээс ихгүй байх ёстой")
    private String city;

    @Size(max = 100, message = "Аймаг 100 тэмдэгтээс ихгүй байх ёстой")
    private String province;

    @Size(max = 10, message = "Шуудангийн код 10 тэмдэгтээс ихгүй байх ёстой")
    private String postalCode;

    // Ажлын мэдээлэл
    @Size(max = 200, message = "Ажил олгогчийн нэр 200 тэмдэгтээс ихгүй байх ёстой")
    private String employerName;

    @Size(max = 100, message = "Албан тушаал 100 тэмдэгтээс ихгүй байх ёстой")
    private String jobTitle;

    @Min(value = 0, message = "Ажлын туршлага сөрөг байж болохгүй")
    @Max(value = 50, message = "Ажлын туршлага 50 жилээс их байж болохгүй")
    private Integer workExperienceYears;

    @DecimalMin(value = "0.0", message = "Сарын орлого сөрөг байж болохгүй")
    private BigDecimal monthlyIncome;

    // Байгууллагын мэдээлэл
    @Size(max = 200, message = "Компанийн нэр 200 тэмдэгтээс ихгүй байх ёстой")
    private String companyName;

    @Size(max = 20, message = "Бизнес регистрийн дугаар 20 тэмдэгтээс ихгүй байх ёстой")
    private String businessRegistrationNumber;

    @Size(max = 20, message = "Татварын дугаар 20 тэмдэгтээс ихгүй байх ёстой")
    private String taxNumber;

    @Size(max = 100, message = "Бизнесийн төрөл 100 тэмдэгтээс ихгүй байх ёстой")
    private String businessType;

    @DecimalMin(value = "0.0", message = "Жилийн орлого сөрөг байж болохгүй")
    private BigDecimal annualRevenue;

    // KYC мэдээлэл
    @NotNull(message = "KYC статус заавал байх ёстой")
    private Customer.KycStatus kycStatus;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime kycCompletedAt;

    @Size(max = 100, message = "KYC хянасан хүн 100 тэмдэгтээс ихгүй байх ёстой")
    private String kycVerifiedBy;

    // Системийн мэдээлэл
    private Boolean isActive = true;

    // CustomerStatus талбарыг нэмэх
    private CustomerStatus status;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    private String createdBy;
    private String updatedBy;

    // Computed fields
    private String displayName;
    private String customerTypeDisplay;
    private String kycStatusDisplay;
    private Boolean isKycCompleted;
    private Integer age;
    private String genderDisplay;
    private String formattedMonthlyIncome;
    private String formattedAnnualRevenue;

    // Constructors
    public CustomerDto() {
        this.customerType = Customer.CustomerType.INDIVIDUAL;
        this.kycStatus = Customer.KycStatus.PENDING;
        this.isActive = true;
        this.status = CustomerStatus.PENDING_VERIFICATION; // Эхний утгыг тохируулах
    }

    public CustomerDto(Customer.CustomerType customerType, String firstName, String lastName, 
                      String registerNumber, String phone) {
        this();
        this.customerType = customerType;
        this.firstName = firstName;
        this.lastName = lastName;
        this.registerNumber = registerNumber;
        this.phone = phone;
    }

    // Static factory methods
    public static CustomerDto fromEntity(Customer customer) {
        if (customer == null) {
            return null;
        }

        CustomerDto dto = new CustomerDto();

        // ID is already UUID in BaseEntity, no conversion needed
        dto.setId(customer.getId());
        dto.setCustomerType(customer.getCustomerType());
        dto.setFirstName(customer.getFirstName());
        dto.setLastName(customer.getLastName());
        dto.setRegisterNumber(customer.getRegisterNumber());
        dto.setBirthDate(customer.getBirthDate());
        dto.setGender(customer.getGender());
        dto.setPhone(customer.getPhone());
        dto.setEmail(customer.getEmail());
        dto.setAddress(customer.getAddress());
        dto.setCity(customer.getCity());
        dto.setProvince(customer.getProvince());
        dto.setPostalCode(customer.getPostalCode());
        dto.setEmployerName(customer.getEmployerName());
        dto.setJobTitle(customer.getJobTitle());
        dto.setWorkExperienceYears(customer.getWorkExperienceYears());
        dto.setMonthlyIncome(customer.getMonthlyIncome());
        dto.setCompanyName(customer.getCompanyName());
        dto.setBusinessRegistrationNumber(customer.getBusinessRegistrationNumber());
        dto.setTaxNumber(customer.getTaxNumber());
        dto.setBusinessType(customer.getBusinessType());
        dto.setAnnualRevenue(customer.getAnnualRevenue());
        dto.setKycStatus(customer.getKycStatus());
        dto.setKycCompletedAt(customer.getKycCompletedAt());
        
        // Safe KYC verified by extraction - assuming it might be missing in entity
        try {
            dto.setKycVerifiedBy(customer.getKycVerifiedBy()); 
        } catch (Exception e) {
            logger.warn("KycVerifiedBy field not available in Customer entity");
            dto.setKycVerifiedBy(null);
        }
        
        dto.setIsActive(customer.getIsActive());
        dto.setStatus(customer.getStatus()); // Статусыг entity-ээс DTO руу хуулах
        dto.setCreatedAt(customer.getCreatedAt());
        dto.setUpdatedAt(customer.getUpdatedAt());
        
        // Audit fields are String in both DTO and BaseEntity
        dto.setCreatedBy(customer.getCreatedBy());
        dto.setUpdatedBy(customer.getUpdatedBy());

        // Set computed fields
        dto.setDisplayName(dto.calculateDisplayName());
        dto.setCustomerTypeDisplay(dto.calculateCustomerTypeDisplay());
        dto.setKycStatusDisplay(dto.calculateKycStatusDisplay());
        dto.setIsKycCompleted(dto.calculateIsKycCompleted());
        dto.setAge(dto.calculateAge());
        dto.setGenderDisplay(dto.calculateGenderDisplay());
        dto.setFormattedMonthlyIncome(dto.formatAmount(dto.getMonthlyIncome()));
        dto.setFormattedAnnualRevenue(dto.formatAmount(dto.getAnnualRevenue()));

        return dto;
    }

    public static CustomerDto createSummary(Customer customer) {
        if (customer == null) {
            return null;
        }

        CustomerDto dto = new CustomerDto();

        // ID is already UUID in BaseEntity, no conversion needed
        dto.setId(customer.getId());
        dto.setCustomerType(customer.getCustomerType());
        dto.setFirstName(customer.getFirstName());
        dto.setLastName(customer.getLastName());
        dto.setRegisterNumber(customer.getRegisterNumber());
        dto.setPhone(customer.getPhone());
        dto.setKycStatus(customer.getKycStatus());
        dto.setStatus(customer.getStatus()); // Статусыг summary DTO-д нэмэх

        dto.setDisplayName(dto.calculateDisplayName());
        dto.setKycStatusDisplay(dto.calculateKycStatusDisplay());
        dto.setIsKycCompleted(dto.calculateIsKycCompleted());

        return dto;
    }

    public Customer toEntity() {
        Customer customer = new Customer();

        // ID is UUID in both DTO and entity, no conversion needed
        customer.setId(this.id);
        customer.setCustomerType(this.customerType);
        customer.setFirstName(this.firstName);
        customer.setLastName(this.lastName);
        customer.setRegisterNumber(this.registerNumber);
        customer.setBirthDate(this.birthDate);
        customer.setGender(this.gender);
        customer.setPhone(this.phone);
        customer.setEmail(this.email);
        customer.setAddress(this.address);
        customer.setCity(this.city);
        customer.setProvince(this.province);
        customer.setPostalCode(this.postalCode);
        customer.setEmployerName(this.employerName);
        customer.setJobTitle(this.jobTitle);
        customer.setWorkExperienceYears(this.workExperienceYears);
        customer.setMonthlyIncome(this.monthlyIncome);
        customer.setCompanyName(this.companyName);
        customer.setBusinessRegistrationNumber(this.businessRegistrationNumber);
        customer.setTaxNumber(this.taxNumber);
        customer.setBusinessType(this.businessType);
        customer.setAnnualRevenue(this.annualRevenue);
        customer.setKycStatus(this.kycStatus);
        customer.setKycCompletedAt(this.kycCompletedAt);
        
        // Safe KYC verified by setting - assuming it might be missing in entity
        try {
            customer.setKycVerifiedBy(this.kycVerifiedBy);
        } catch (Exception e) {
            logger.warn("KycVerifiedBy field not available in Customer entity");
        }
        
        customer.setIsActive(this.isActive);
        customer.setStatus(this.status); // Статусыг DTO-оос entity руу хуулах
        customer.setCreatedAt(this.createdAt);
        customer.setUpdatedAt(this.updatedAt);
        
        // Audit fields are String in both DTO and BaseEntity
        customer.setCreatedBy(this.createdBy);
        customer.setUpdatedBy(this.updatedBy);

        return customer;
    }

    // Helper methods for computed fields
    private String calculateDisplayName() {
        if (customerType == Customer.CustomerType.BUSINESS && companyName != null) {
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

    private String calculateCustomerTypeDisplay() {
        if (customerType == null) return "Тодорхойгүй";
        switch (customerType) {
            case INDIVIDUAL: return "Хувь хүн";
            case BUSINESS: return "Байгууллага";
            default: return customerType.toString();
        }
    }

    private String calculateKycStatusDisplay() {
        if (kycStatus == null) return "Тодорхойгүй";
        switch (kycStatus) {
            case PENDING: return "Хүлээгдэж байна";
            case IN_PROGRESS: return "Хийгдэж байна";
            case COMPLETED: return "Дууссан";
            case REJECTED: return "Татгалзсан";
            case FAILED: return "Амжилтгүй"; // FAILED-г нэмэх
            default: return kycStatus.toString();
        }
    }

    private Boolean calculateIsKycCompleted() {
        return kycStatus == Customer.KycStatus.COMPLETED;
    }

    private Integer calculateAge() {
        if (birthDate == null) return null;
        return LocalDate.now().getYear() - birthDate.getYear();
    }

    private String calculateGenderDisplay() {
        if (gender == null) return "";
        switch (gender) {
            case MALE: return "Эрэгтэй";
            case FEMALE: return "Эмэгтэй";
            case OTHER: return "Бусад";
            default: return gender.toString();
        }
    }

    private String formatAmount(BigDecimal amount) {
        return amount != null ? String.format("%,.0f₮", amount) : "";
    }

    // Business logic methods
    public boolean isValidForIndividual() {
        return customerType == Customer.CustomerType.INDIVIDUAL &&
               firstName != null && !firstName.trim().isEmpty() &&
               lastName != null && !lastName.trim().isEmpty() &&
               registerNumber != null && !registerNumber.trim().isEmpty() &&
               phone != null && !phone.trim().isEmpty();
    }

    public boolean isValidForBusiness() {
        return customerType == Customer.CustomerType.BUSINESS &&
               companyName != null && !companyName.trim().isEmpty() &&
               businessRegistrationNumber != null && !businessRegistrationNumber.trim().isEmpty() &&
               phone != null && !phone.trim().isEmpty();
    }

    public String getKycStatusBadgeClass() {
        if (kycStatus == null) return "badge-secondary";
        switch (kycStatus) {
            case PENDING: return "badge-warning";
            case IN_PROGRESS: return "badge-info";
            case COMPLETED: return "badge-success";
            case REJECTED: return "badge-danger";
            case FAILED: return "badge-danger"; // FAILED-г нэмэх
            default: return "badge-secondary";
        }
    }

    public String getCustomerTypeBadgeClass() {
        if (customerType == null) return "badge-secondary";
        switch (customerType) {
            case INDIVIDUAL: return "badge-primary";
            case BUSINESS: return "badge-info";
            default: return "badge-secondary";
        }
    }

    public boolean canApplyForLoan() {
        // isActive нь Boolean тул шууд ашиглаж болно
        return Boolean.TRUE.equals(isActive) && Boolean.TRUE.equals(isKycCompleted);
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Customer.CustomerType getCustomerType() { return customerType; }
    public void setCustomerType(Customer.CustomerType customerType) { this.customerType = customerType; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getRegisterNumber() { return registerNumber; }
    public void setRegisterNumber(String registerNumber) { this.registerNumber = registerNumber; }

    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }

    public Customer.Gender getGender() { return gender; }
    public void setGender(Customer.Gender gender) { this.gender = gender; }

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

    public Integer getWorkExperienceYears() { return workExperienceYears; }
    public void setWorkExperienceYears(Integer workExperienceYears) { this.workExperienceYears = workExperienceYears; }

    public BigDecimal getMonthlyIncome() { return monthlyIncome; }
    public void setMonthlyIncome(BigDecimal monthlyIncome) { this.monthlyIncome = monthlyIncome; }

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

    public Customer.KycStatus getKycStatus() { return kycStatus; }
    public void setKycStatus(Customer.KycStatus kycStatus) { this.kycStatus = kycStatus; }

    public LocalDateTime getKycCompletedAt() { return kycCompletedAt; }
    public void setKycCompletedAt(LocalDateTime kycCompletedAt) { this.kycCompletedAt = kycCompletedAt; }

    public String getKycVerifiedBy() { return kycVerifiedBy; }
    public void setKycVerifiedBy(String kycVerifiedBy) { this.kycVerifiedBy = kycVerifiedBy; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    // CustomerStatus-ийн getter/setter-г нэмэх
    public CustomerStatus getStatus() { return status; }
    public void setStatus(CustomerStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getCustomerTypeDisplay() { return customerTypeDisplay; }
    public void setCustomerTypeDisplay(String customerTypeDisplay) { this.customerTypeDisplay = customerTypeDisplay; }

    public String getKycStatusDisplay() { return kycStatusDisplay; }
    public void setKycStatusDisplay(String kycStatusDisplay) { this.kycStatusDisplay = kycStatusDisplay; }

    public Boolean getIsKycCompleted() { return isKycCompleted; }
    public void setIsKycCompleted(Boolean isKycCompleted) { this.isKycCompleted = isKycCompleted; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public String getGenderDisplay() { return genderDisplay; }
    public void setGenderDisplay(String genderDisplay) { this.genderDisplay = genderDisplay; }

    public String getFormattedMonthlyIncome() { return formattedMonthlyIncome; }
    public void setFormattedMonthlyIncome(String formattedMonthlyIncome) { this.formattedMonthlyIncome = formattedMonthlyIncome; }

    public String getFormattedAnnualRevenue() { return formattedAnnualRevenue; }
    public void setFormattedAnnualRevenue(String formattedAnnualRevenue) { this.formattedAnnualRevenue = formattedAnnualRevenue; }

    @Override
    public String toString() {
        return "CustomerDto{" +
                "id=" + id +
                ", customerType=" + customerType +
                ", displayName='" + displayName + '\'' +
                ", registerNumber='" + registerNumber + '\'' +
                ", phone='" + phone + '\'' +
                ", kycStatus=" + kycStatus +
                ", status=" + status + // toString-д статусыг нэмэх
                '}';
    }
}