package com.company.los.config;

import com.company.los.entity.Customer;
import com.company.los.enums.CustomerStatus;
import com.company.los.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Development/демо өгөгдөл үүсгэгч.
 *
 * ⭐ ТАЙЛБАР: schema.sql болон data.sql нь entity-үүдийн баганатай таарахгүй
 * (жишээ нь date_of_birth vs birth_date, дутуу баганууд, UUID/VARCHAR зөрүү)
 * байсан тул H2 дээр өгөгдөл эсвэл алдаа үүсгэдэг байв. Одоо хүснэгтийг
 * Hibernate (ddl-auto) үүсгэж, эхний өгөгдлийг entity-ээр дамжуулан энд үүсгэнэ.
 *
 * Зөвхөн харилцагч хоосон үед ажиллана (дахин ажиллуулахад давхардахгүй).
 * Тест профайл дээр ажиллахгүй.
 */
@Component
@Profile("!test")
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final CustomerRepository customerRepository;

    public DataInitializer(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public void run(String... args) {
        try {
            if (customerRepository.count() > 0) {
                log.info("ℹ️ Демо өгөгдөл аль хэдийн байна ({} харилцагч)", customerRepository.count());
                return;
            }

            Customer bat = individual("Бат", "Болд", "УБ12345678", "bat.bold@example.mn", "99999999",
                    LocalDate.of(1985, 5, 15), Customer.Gender.MALE, "Монгол банк", "Ахлах мэргэжилтэн",
                    8, new BigDecimal("1200000.00"));

            Customer tsetseg = individual("Цэцэг", "Сүх", "УБ87654321", "tsetseg.suh@example.mn", "88888888",
                    LocalDate.of(1990, 8, 20), Customer.Gender.FEMALE, "Хаан банк", "Салбарын менежер",
                    10, new BigDecimal("2000000.00"));

            Customer enhbat = individual("Энхбат", "Төмөр", "УБ11223344", "enhbat.tumor@example.mn", "77777777",
                    LocalDate.of(1988, 12, 10), Customer.Gender.MALE, "Тээвэр ХХК", "Жолооч",
                    5, new BigDecimal("800000.00"));

            Customer ariun = business("Ариун ХХК", "КР11111111", "info@ariun.mn", "66666666",
                    "Барилгын материал", "TT12345678", "BR11111111", new BigDecimal("120000000.00"));

            Customer mandal = business("Мандал ХХК", "КР22222222", "contact@mandal.mn", "55555555",
                    "Худалдаа үйлчилгээ", "TT87654321", "BR22222222", new BigDecimal("80000000.00"));

            customerRepository.save(bat);
            customerRepository.save(tsetseg);
            customerRepository.save(enhbat);
            customerRepository.save(ariun);
            customerRepository.save(mandal);

            log.info("✅ Демо өгөгдөл үүслээ: {} харилцагч", customerRepository.count());
        } catch (Exception e) {
            log.warn("⚠️ Демо өгөгдөл үүсгэхэд алдаа: {}", e.getMessage());
        }
    }

    private Customer individual(String firstName, String lastName, String registerNumber, String email,
                                String phone, LocalDate birthDate, Customer.Gender gender,
                                String employerName, String jobTitle, Integer workExperienceYears,
                                BigDecimal monthlyIncome) {
        Customer c = new Customer();
        c.setCustomerType(Customer.CustomerType.INDIVIDUAL);
        c.setFirstName(firstName);
        c.setLastName(lastName);
        c.setRegisterNumber(registerNumber);
        c.setBirthDate(birthDate);
        c.setGender(gender);
        c.setEmail(email);
        c.setPhone(phone);
        c.setAddress("Улаанбаатар хот");
        c.setCity("Улаанбаатар");
        c.setProvince("Улаанбаатар");
        c.setPostalCode("14200");
        c.setEmployerName(employerName);
        c.setJobTitle(jobTitle);
        c.setWorkExperienceYears(workExperienceYears);
        c.setMonthlyIncome(monthlyIncome);
        return finalizeCustomer(c);
    }

    private Customer business(String companyName, String registerNumber, String email, String phone,
                              String businessType, String taxNumber, String businessRegistrationNumber,
                              BigDecimal annualRevenue) {
        Customer c = new Customer();
        c.setCustomerType(Customer.CustomerType.BUSINESS);
        c.setCompanyName(companyName);
        c.setFirstName(companyName);
        c.setLastName(companyName);
        c.setRegisterNumber(registerNumber);
        c.setEmail(email);
        c.setPhone(phone);
        c.setBusinessType(businessType);
        c.setTaxNumber(taxNumber);
        c.setBusinessRegistrationNumber(businessRegistrationNumber);
        c.setAnnualRevenue(annualRevenue);
        c.setAddress("Улаанбаатар хот");
        c.setCity("Улаанбаатар");
        c.setProvince("Улаанбаатар");
        c.setPostalCode("14200");
        return finalizeCustomer(c);
    }

    private Customer finalizeCustomer(Customer c) {
        c.setKycStatus(Customer.KycStatus.COMPLETED);
        c.setIsActive(true);
        c.setStatus(CustomerStatus.ACTIVE);
        c.setRegistrationDate(LocalDateTime.now());
        c.setPreferredLanguage("mn");
        return c;
    }
}
