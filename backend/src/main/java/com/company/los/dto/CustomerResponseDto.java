package com.company.los.dto;

import com.company.los.enums.CustomerStatus;
import com.company.los.enums.CustomerType;
import com.company.los.enums.KYCStatus;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * CustomerResponseDto нь хэрэглэгчийн мэдээллийг буцаахад хэрэглэгдэх өгөгдлийг агуулна.
 */
@Data
public class CustomerResponseDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private LocalDate dateOfBirth;
    private String socialSecurityNumber;
    private CustomerType customerType;
    private CustomerStatus status;
    private KYCStatus kycStatus;
    private String preferredLanguage;
    private LocalDateTime registrationDate;
    private LocalDateTime lastUpdated;
}
