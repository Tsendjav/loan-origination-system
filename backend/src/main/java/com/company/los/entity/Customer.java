package com.company.los.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "customers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Customer {
    
    @Id
    @GeneratedValue
    private UUID id;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "customer_type", nullable = false)
    private CustomerType customerType;
    
    // Хувь хүний мэдээлэл
    @Column(name = "first_name")
    private String firstName;
    
    @Column(name = "last_name")
    private String lastName;
    
    @Column(name = "register_number", unique = true)
    private String registerNumber;
    
    @Column(name = "birth_date")
    private LocalDate birthDate;
    
    @Enumerated(EnumType.STRING)
    private Gender gender;
    
    // Байгууллагын мэдээлэл
    @Column(name = "company_name")
    private String companyName;
    
    @Column(name = "company_register", unique = true)
    private String companyRegister;
    
    // Холбоо барих мэдээлэл
    @Column(nullable = false)
    private String phone;
    
    private String email;
    
    // Хаяг
    @Column(name = "address_detail")
    private String addressDetail;
    
    // Нэмэлт мэдээлэл
    @Column(name = "monthly_income")
    private BigDecimal monthlyIncome;
    
    @Enumerated(EnumType.STRING)
    private CustomerStatus status = CustomerStatus.ACTIVE;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "kyc_status")
    private KYCStatus kycStatus = KYCStatus.PENDING;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")  
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

enum CustomerType {
    INDIVIDUAL, BUSINESS
}

enum Gender {
    MALE, FEMALE
}

enum CustomerStatus {
    ACTIVE, INACTIVE, BLOCKED
}

enum KYCStatus {
    PENDING, IN_PROGRESS, APPROVED, REJECTED
}
