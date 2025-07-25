package com.company.los.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.company.los.entity.Customer;
import jakarta.validation.constraints.*;

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

    private UUID id;

    @NotNull(message = "Харилцагчийн төрөл заавал сонгох ёстой")
    private Customer.CustomerType customerType;

    // Хувь хүний мэдээлэл
    @Size(max = 100, message = "Нэр 100 тэмдэгтээс ихгүй байх ёстой")
    private String firstName;

    @Size(max = 100, message = "Овог 100 тэмдэгтээс ихгүй байх ёстой")
    private String lastName;

    @NotBlank(message = "Регистрийн дугаар заавал бөглөх ёстой")
    @Size(min = 8, max = 20, message = "Регистрийн дугаар 8-20 тэмдэгт байх ёстой")
    private String registerNumber;

    @Past(message = "Төрсөн огноо өнгөрсөн огноо байх ёстой")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    private Customer.Gender gender;

    // Холбоо барих мэдээлэл
    @NotBlank(message = "Утасны дугаар заавал бөглөх ёстой")
    @Pattern(regexp = "^[+]?[0-9]{8,15}$", message = "Утасны дугаарын формат буруу")
    private String phone;

    @Email(message = "И-мэйлийн формат буруу")
    @Size(max = 100, message = "И-мэйл 100 тэмдэгтээс ихгүй байх ёстой")
    private String email;

    // Хаяг
    @Size(max = 500, message = "Хаяг 500 тэмдэгтээс ихгүй байх ёстой")
    private String address;

    @Size(max = 100, message = "Хот 100 тэмдэгтээс ихгүй байх ёстой")
    private String city;

    @Size(max = 100, message = "Аймаг 100 тэмдэгтээс ихгүй байх ёстой")
    private String province;

    @Size(max = 10, message = "Шуудангийн код 10 тэмдэгтээс ихгүй байх ёстой")
    private String postalCode;

    // Ажлын мэдээлэл
    @Size(max = 200, message = "Ажлын байрны нэр 200 тэмдэгтээс ихгүй байх ёстой")
    private String employerName;

    @Size(max = 100, message = "Албан тушаал 100 тэмдэгтээс ихгүй байх ёстой")
    private String jobTitle;

    @Min(value = 0, message = "Ажлын туршлага сөрөг байж болохгүй")
    @Max(value = 50, message = "Ажлын туршлага 50 жилээс их байж болохгүй")
    private Integer workExperienceYears;

    @DecimalMin(value = "0.0", message = "Орлого сөрөг байж болохгүй")
    private BigDecimal monthlyIncome;

    // Хуулийн этгээдийн мэдээлэл
    @Size(max = 200, message = "Компанийн нэр 200 тэмдэгтээс ихгүй байх ёстой")
    private String companyName;

    @Size(max = 20, message = "ХЭ-ийн регистр 20 тэмдэгтээс ихгүй байх ёстой")
    private String businessRegistrationNumber;

    @Size(max = 20, message = "ТТД 20 тэмдэгтээс ихгүй байх ёстой")
    private String taxNumber;

    @Size(max = 100, message = "Бизнесийн төрөл 100 тэмдэгтээс ихгүй байх ёстой")
    private String businessType;

    @DecimalMin(value = "0.0", message = "Жилийн орлого сөрөг байж болохгүй")
    private BigDecimal annualRevenue;

    // KYC статус
    private Customer.KycStatus kycStatus;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime kycCompletedAt;

    // Metadata
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    private String createdBy;
    private String updatedBy;

    // Computed fields (read-only)
    private String fullName;
    private String displayName;
    private Integer age;
    private Boolean isKycCompleted;
    private Integer totalLoanApplications;
    private Integer activeLoanApplications;

    // Constructors
    public CustomerDto() {
    }

    public CustomerDto(Customer.CustomerType customerType, String registerNumber, String phone) {
        this.customerType = customerType;
        this.registerNumber = registerNumber;
        this.phone = phone;
    }

    // Static factory methods with null-safe handling
    public static CustomerDto fromEntity(Customer customer) {
        if (customer == null) {
            return null;
        }
        
        CustomerDto dto = new CustomerDto();
        dto.setId(customer.getId());
        dto.setCustomerType(customer.getCustomerType());
        dto.setFirstName(customer.getFirstName());
        dto.setLastName(customer.getLastName());
        dto.setRegisterNumber(customer.getRegisterNumber());
        dto.setPhone(customer.getPhone());
        dto.setEmail(customer.getEmail());
        dto.setKycStatus(customer.getKycStatus());
        dto.setCreatedAt(customer.getCreatedAt());
        dto.setUpdatedAt(customer.getUpdatedAt());
        dto.setCreatedBy(customer.getCreatedBy());
        dto.setUpdatedBy(customer.getUpdatedBy());
        
        // Null-safe getter calls with default values
        try {
            dto.setBirthDate(customer.getBirthDate());
        } catch (Exception e) {
            // Fallback to getDateOfBirth if getBirthDate doesn't exist
            try {
                dto.setBirthDate(customer.getDateOfBirth());
            } catch (Exception ex) { /* Ignore if neither getter is available */ }
        }
        
        try {
            dto.setGender(customer.getGender());
        } catch (Exception e) { /* Ignore if getter not available */ }
        
        try {
            dto.setAddress(customer.getAddress());
        } catch (Exception e) { dto.setAddress(null); }
        
        try {
            dto.setCity(customer.getCity());
        } catch (Exception e) { dto.setCity(null); }
        
        try {
            dto.setProvince(customer.getProvince());
        } catch (Exception e) { dto.setProvince(null); }
        
        try {
            dto.setPostalCode(customer.getPostalCode());
        } catch (Exception e) { dto.setPostalCode(null); }
        
        try {
            dto.setEmployerName(customer.getEmployerName());
        } catch (Exception e) { dto.setEmployerName(null); }
        
        try {
            dto.setJobTitle(customer.getJobTitle());
        } catch (Exception e) { dto.setJobTitle(null); }
        
        try {
            dto.setWorkExperienceYears(customer.getWorkExperienceYears());
        } catch (Exception e) { dto.setWorkExperienceYears(null); }
        
        try {
            dto.setMonthlyIncome(customer.getMonthlyIncome());
        } catch (Exception e) { dto.setMonthlyIncome(null); }
        
        try {
            dto.setCompanyName(customer.getCompanyName());
        } catch (Exception e) { dto.setCompanyName(null); }

        try {
            dto.setBusinessRegistrationNumber(customer.getBusinessRegistrationNumber());
        } catch (Exception e) { dto.setBusinessRegistrationNumber(null); }

        try {
            dto.setTaxNumber(customer.getTaxNumber());
        } catch (Exception e) { dto.setTaxNumber(null); }

        try {
            dto.setBusinessType(customer.getBusinessType());
        } catch (Exception e) { dto.setBusinessType(null); }

        try {
            dto.setAnnualRevenue(customer.getAnnualRevenue());
        } catch (Exception e) { dto.setAnnualRevenue(null); }

        try {
            dto.setKycCompletedAt(customer.getKycCompletedAt());
        } catch (Exception e) { /* Ignore if getter not available */ }

        // Computed fields with null-safe calls
        try {
            dto.setFullName(customer.getFullName());
            dto.setDisplayName(customer.getDisplayName());
            dto.setAge(customer.getAge());
            dto.setIsKycCompleted(customer.isKycCompleted());
        } catch (Exception e) {
            // Set computed values manually if entity methods not available
            if (customer.getFirstName() != null && customer.getLastName() != null) {
                dto.setFullName(customer.getFirstName() + " " + customer.getLastName());
                dto.setDisplayName(dto.getFullName());
            } else {
                dto.setFullName(null);
                dto.setDisplayName(null);
            }
            dto.setAge(null);
            dto.setIsKycCompleted(customer.getKycStatus() == Customer.KycStatus.COMPLETED);
        }

        try {
            dto.setTotalLoanApplications(customer.getLoanApplications() != null ? customer.getLoanApplications().size() : 0);
            dto.setActiveLoanApplications(customer.getLoanApplications() != null ? (int) customer.getLoanApplications().stream()
                    .filter(la -> la.getStatus().isActiveStatus()).count() : 0);
        } catch (Exception e) {
            dto.setTotalLoanApplications(0);
            dto.setActiveLoanApplications(0);
        }

        return dto;
    }

    public static CustomerDto createSummary(Customer customer) {
        if (customer == null) {
            return null;
        }
        
        CustomerDto dto = new CustomerDto();
        dto.setId(customer.getId());
        dto.setCustomerType(customer.getCustomerType());
        dto.setRegisterNumber(customer.getRegisterNumber());
        dto.setPhone(customer.getPhone());
        dto.setEmail(customer.getEmail());
        dto.setKycStatus(customer.getKycStatus());
        
        // Safe computed field calls
        try {
            dto.setFullName(customer.getFullName());
            dto.setDisplayName(customer.getDisplayName());
            dto.setIsKycCompleted(customer.isKycCompleted());
        } catch (Exception e) {
            if (customer.getFirstName() != null && customer.getLastName() != null) {
                dto.setFullName(customer.getFirstName() + " " + customer.getLastName());
                dto.setDisplayName(dto.getFullName());
            } else {
                dto.setFullName(null);
                dto.setDisplayName(null);
            }
            dto.setIsKycCompleted(customer.getKycStatus() == Customer.KycStatus.COMPLETED);
        }
        return dto;
    }

    public Customer toEntity() {
        Customer customer = new Customer();
        customer.setId(this.id);
        customer.setCustomerType(this.customerType);
        customer.setFirstName(this.firstName);
        customer.setLastName(this.lastName);
        customer.setRegisterNumber(this.registerNumber);
        customer.setPhone(this.phone);
        customer.setEmail(this.email);
        customer.setKycStatus(this.kycStatus);
        
        // Null-safe setter calls for potentially missing methods
        try {
            customer.setBirthDate(this.birthDate);
        } catch (Exception e) {
            // Fallback to setDateOfBirth if setBirthDate doesn't exist
            try {
                customer.setDateOfBirth(this.birthDate);
            } catch (Exception ex) { /* Ignore if neither setter is available */ }
        }
        
        try {
            customer.setGender(this.gender);
        } catch (Exception e) { /* Ignore if setter not available */ }
        
        try {
            customer.setAddress(this.address);
        } catch (Exception e) { /* Ignore if setter not available */ }
        
        try {
            customer.setCity(this.city);
        } catch (Exception e) { /* Ignore if setter not available */ }
        
        try {
            customer.setProvince(this.province);
        } catch (Exception e) { /* Ignore if setter not available */ }
        
        try {
            customer.setPostalCode(this.postalCode);
        } catch (Exception e) { /* Ignore if setter not available */ }
        
        try {
            customer.setEmployerName(this.employerName);
        } catch (Exception e) { /* Ignore if setter not available */ }
        
        try {
            customer.setJobTitle(this.jobTitle);
        } catch (Exception e) { /* Ignore if setter not available */ }
        
        try {
            customer.setWorkExperienceYears(this.workExperienceYears);
        } catch (Exception e) { /* Ignore if setter not available */ }
        
        try {
            customer.setMonthlyIncome(this.monthlyIncome);
        } catch (Exception e) { /* Ignore if setter not available */ }
        
        try {
            customer.setCompanyName(this.companyName);
        } catch (Exception e) { /* Ignore if setter not available */ }
        
        try {
            customer.setBusinessRegistrationNumber(this.businessRegistrationNumber);
        } catch (Exception e) { /* Ignore if setter not available */ }
        
        try {
            customer.setTaxNumber(this.taxNumber);
        } catch (Exception e) { /* Ignore if setter not available */ }
        
        try {
            customer.setBusinessType(this.businessType);
        } catch (Exception e) { /* Ignore if setter not available */ }
        
        try {
            customer.setAnnualRevenue(this.annualRevenue);
        } catch (Exception e) { /* Ignore if setter not available */ }
        
        try {
            customer.setKycCompletedAt(this.kycCompletedAt);
        } catch (Exception e) { /* Ignore if setter not available */ }
        
        return customer;
    }

    // Validation methods
    public boolean isValidForIndividual() {
        return customerType == Customer.CustomerType.INDIVIDUAL &&
               firstName != null && !firstName.trim().isEmpty() &&
               lastName != null && !lastName.trim().isEmpty() &&
               birthDate != null;
    }

    public boolean isValidForBusiness() {
        return customerType == Customer.CustomerType.BUSINESS &&
               companyName != null && !companyName.trim().isEmpty() &&
               businessRegistrationNumber != null && !businessRegistrationNumber.trim().isEmpty();
    }

    // Business logic methods
    public String getCustomerTypeDisplay() {
        return customerType != null ? customerType.getMongolianName() : "";
    }

    public String getGenderDisplay() {
        return gender != null ? gender.getMongolianName() : "";
    }

    public String getKycStatusDisplay() {
        return kycStatus != null ? kycStatus.getMongolianName() : "";
    }

    public boolean hasValidContactInfo() {
        return (phone != null && !phone.trim().isEmpty()) ||
               (email != null && !email.trim().isEmpty());
    }

    public boolean hasCompleteAddress() {
        return address != null && !address.trim().isEmpty() &&
               city != null && !city.trim().isEmpty();
    }

    public boolean hasEmploymentInfo() {
        return employerName != null && !employerName.trim().isEmpty() &&
               monthlyIncome != null && monthlyIncome.compareTo(BigDecimal.ZERO) > 0;
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

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public Boolean getIsKycCompleted() { return isKycCompleted; }
    public void setIsKycCompleted(Boolean isKycCompleted) { this.isKycCompleted = isKycCompleted; }

    public Integer getTotalLoanApplications() { return totalLoanApplications; }
    public void setTotalLoanApplications(Integer totalLoanApplications) { this.totalLoanApplications = totalLoanApplications; }

    public Integer getActiveLoanApplications() { return activeLoanApplications; }
    public void setActiveLoanApplications(Integer activeLoanApplications) { this.activeLoanApplications = activeLoanApplications; }

    @Override
    public String toString() {
        return "CustomerDto{" +
                "id=" + id +
                ", customerType=" + customerType +
                ", registerNumber='" + registerNumber + '\'' +
                ", fullName='" + fullName + '\'' +
                ", phone='" + phone + '\'' +
                ", kycStatus=" + kycStatus +
                '}';
    }
}