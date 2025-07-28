package com.company.los.service;

import com.company.los.dto.CustomerDto;
import com.company.los.entity.Customer;
import com.company.los.enums.CustomerStatus;
import com.company.los.enums.KYCStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Харилцагчийн Service Interface
 * Customer Service Interface
 */
public interface CustomerService {

    // CRUD операциуд
    /**
     * Шинэ харилцагч үүсгэх
     */
    CustomerDto createCustomer(CustomerDto customerDto);

    /**
     * Харилцагчийн мэдээлэл авах
     */
    CustomerDto getCustomerById(UUID id);

    /**
     * Харилцагчийн мэдээлэл шинэчлэх
     */
    CustomerDto updateCustomer(UUID id, CustomerDto customerDto);

    /**
     * Харилцагч устгах (soft delete)
     */
    void deleteCustomer(UUID id);

    /**
     * Устгасан харилцагч сэргээх
     */
    CustomerDto restoreCustomer(UUID id);

    // Хайлт операциуд
    /**
     * Бүх харилцагчдын жагсаалт
     */
    Page<CustomerDto> getAllCustomers(Pageable pageable);

    /**
     * Регистрийн дугаараар хайх
     */
    CustomerDto getCustomerByRegisterNumber(String registerNumber);

    /**
     * Утасны дугаараар хайх
     */
    CustomerDto getCustomerByPhone(String phone);

    /**
     * И-мэйлээр хайх
     */
    CustomerDto getCustomerByEmail(String email);

    /**
     * И-мэйлээр харилцагч хайх (Optional буцаах)
     */
    Optional<CustomerDto> findByEmail(String email);

    /**
     * Ерөнхий хайлт
     */
    Page<CustomerDto> searchCustomers(String searchTerm, Pageable pageable);

    /**
     * Хурдан хайлт
     */
    List<CustomerDto> quickSearchCustomers(String quickSearch);

    /**
     * Харилцагчийн төрлөөр хайх
     */
    Page<CustomerDto> getCustomersByType(Customer.CustomerType customerType, Pageable pageable);

    /**
     * KYC статусаар хайх
     */
    Page<CustomerDto> getCustomersByKycStatus(Customer.KycStatus kycStatus, Pageable pageable);

    // Дэвшилтэт хайлт
    /**
     * Филтертэй дэвшилтэт хайлт
     */
    Page<CustomerDto> searchCustomersWithFilters(
            Customer.CustomerType customerType,
            Customer.KycStatus kycStatus,
            String city,
            String province,
            BigDecimal minIncome,
            BigDecimal maxIncome,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    );

    // KYC удирдлага
    /**
     * KYC процесс эхлүүлэх
     */
    CustomerDto startKycProcess(UUID customerId);

    /**
     * KYC дуусгах
     */
    CustomerDto completeKyc(UUID customerId, String completedBy);

    /**
     * KYC дахин хийх
     */
    CustomerDto retryKyc(UUID customerId, String reason);

    /**
     * KYC дуусаагүй харилцагчид
     */
    Page<CustomerDto> getIncompleteKycCustomers(Pageable pageable);

    /**
     * KYC статус өөрчлөх
     */
    CustomerDto updateKYCStatus(UUID customerId, KYCStatus newStatus);

    // Дупликат шалгалт
    /**
     * Дупликат харилцагч шалгах
     */
    List<CustomerDto> findDuplicateCustomers(CustomerDto customerDto);

    /**
     * Төстэй харилцагч хайх
     */
    List<CustomerDto> findSimilarCustomers(String firstName, String lastName, java.time.LocalDate birthDate, UUID excludeId);

    // Validation
    /**
     * Регистрийн дугаар байгаа эсэхийг шалгах
     */
    boolean existsByRegisterNumber(String registerNumber);

    /**
     * Утасны дугаар байгаа эсэхийг шалгах
     */
    boolean existsByPhone(String phone);

    /**
     * И-мэйл байгаа эсэхийг шалгах
     */
    boolean existsByEmail(String email);

    /**
     * И-мэйл боломжтой эсэхийг шалгах (хуучин нэр)
     */
    boolean isEmailAvailable(String email);

    /**
     * И-мэйл давхардаагүй эсэхийг шалгах (шинэ нэр)
     */
    boolean isEmailUnique(String email);

    /**
     * Харилцагчийн мэдээлэл хүчинтэй эсэхийг шалгах
     */
    boolean validateCustomerData(CustomerDto customerDto);

    // Status management
    /**
     * Харилцагчийн статус өөрчлөх
     */
    CustomerDto updateCustomerStatus(UUID customerId, CustomerStatus newStatus);

    // Зээлийн хүсэлттэй холбоотой
    /**
     * Зээлийн хүсэлттэй харилцагчид
     */
    Page<CustomerDto> getCustomersWithLoanApplications(Pageable pageable);

    /**
     * Идэвхтэй зээлтэй харилцагчид
     */
    Page<CustomerDto> getCustomersWithActiveLoans(Pageable pageable);

    /**
     * Зээлийн хүсэлтгүй харилцагчид
     */
    Page<CustomerDto> getCustomersWithoutLoanApplications(Pageable pageable);

    /**
     * Харилцагчийн зээлийн түүх
     */
    Map<String, Object> getCustomerLoanHistory(UUID customerId);

    // Статистик
    /**
     * Харилцагчийн статистик
     */
    Map<String, Object> getCustomerStatistics();

    /**
     * Харилцагчийн төрлөөр статистик
     */
    Map<Customer.CustomerType, Long> getCustomerCountByType();

    /**
     * KYC статусаар статистик
     */
    Map<Customer.KycStatus, Long> getCustomerCountByKycStatus();

    /**
     * Сарын харилцагчийн статистик
     */
    List<Map<String, Object>> getMonthlyCustomerStats(int months);

    /**
     * Хотоор харилцагчийн тоо
     */
    Map<String, Long> getCustomerCountByCity();

    /**
     * Аймгаар харилцагчийн тоо
     */
    Map<String, Long> getCustomerCountByProvince();

    // Dashboard
    /**
     * Өнөөдрийн харилцагчийн статистик
     */
    Map<String, Object> getTodayCustomerStats();

    /**
     * ТОП харилцагчид (зээлийн дүнгээр)
     */
    Page<CustomerDto> getTopCustomersByLoanAmount(Pageable pageable);

    /**
     * Шинэ харилцагчид (сүүлийн 30 хоногт)
     */
    List<CustomerDto> getRecentCustomers();

    /**
     * Идэвхгүй харилцагчид
     */
    Page<CustomerDto> getInactiveCustomers(Pageable pageable);

    // Bulk операциуд
    /**
     * Олон харилцагчийн KYC статус өөрчлөх
     */
    int updateKycStatusForCustomers(List<UUID> customerIds, Customer.KycStatus newStatus);

    /**
     * Олон харилцагч үүсгэх (import)
     */
    List<CustomerDto> createCustomersBulk(List<CustomerDto> customers);

    /**
     * Харилцагчийн мэдээлэл export хийх
     */
    byte[] exportCustomersToExcel(List<UUID> customerIds);

    // Profile management
    /**
     * Харилцагчийн профайл шинэчлэх
     */
    CustomerDto updateCustomerProfile(UUID id, CustomerDto profileDto);

    /**
     * Харилцагчийн нууцлалын тохиргоо
     */
    CustomerDto updatePrivacySettings(UUID id, Map<String, Boolean> privacySettings);

    // Audit & History
    /**
     * Харилцагчийн өөрчлөлтийн түүх
     */
    List<Map<String, Object>> getCustomerAuditHistory(UUID customerId);

    /**
     * Харилцагчийн үйл ажиллагааны лого
     */
    List<Map<String, Object>> getCustomerActivityLog(UUID customerId, int days);

    // Business rules
    /**
     * Харилцагч зээл авч болох эсэхийг шалгах
     */
    boolean canCustomerApplyForLoan(UUID customerId);

    /**
     * Харилцагчийн зээлийн хязгаар тооцоолох
     */
    BigDecimal calculateLoanLimit(UUID customerId);

    /**
     * Харилцагчийн эрсдэлийн категори тодорхойлох
     */
    String determineRiskCategory(UUID customerId);

    // Notification
    /**
     * Харилцагчид мэдэгдэл илгээх
     */
    boolean sendNotificationToCustomer(UUID customerId, String subject, String message);

    /**
     * KYC сануулга илгээх
     */
    boolean sendKycReminder(UUID customerId);

    // Cleanup operations
    /**
     * Идэвхгүй харилцагчдыг цэвэрлэх
     */
    int cleanupInactiveCustomers(int inactiveDays);

    /**
     * Дутуу мэдээлэлтэй харилцагчид
     */
    List<CustomerDto> getCustomersWithIncompleteInfo();

    /**
     * Өгөгдлийн бүрэн бус байдлыг шалгах
     */
    Map<String, Object> validateDataIntegrity();
}