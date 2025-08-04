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

    // ==================== CRUD OPERATIONS ====================
    
    /**
     * Шинэ харилцагч үүсгэх
     */
    CustomerDto createCustomer(CustomerDto customerDto);

    /**
     * Харилцагчийг ID-аар авах
     */
    CustomerDto getCustomerById(UUID id);

    /**
     * Харилцагч шинэчлэх
     */
    CustomerDto updateCustomer(UUID id, CustomerDto customerDto);

    /**
     * Харилцагч устгах
     */
    void deleteCustomer(UUID id);

    /**
     * Харилцагч сэргээх
     */
    CustomerDto restoreCustomer(UUID id);

    // ==================== SEARCH OPERATIONS ====================
    
    /**
     * Бүх харилцагчийн жагсаалт авах (pagination-тай)
     */
    Page<CustomerDto> getAllCustomers(Pageable pageable);

    /**
     * Регистрийн дугаараар харилцагч авах
     */
    CustomerDto getCustomerByRegisterNumber(String registerNumber);

    /**
     * Утасны дугаараар харилцагч авах
     */
    CustomerDto getCustomerByPhone(String phone);

    /**
     * И-мэйлээр харилцагч авах (exception буцаана эсвэл олдохгүй бол)
     */
    CustomerDto getCustomerByEmail(String email);

    /**
     * И-мэйлээр харилцагч хайх (Optional буцаана)
     */
    Optional<CustomerDto> findByEmail(String email);

    /**
     * Харилцагч хайх (text search)
     */
    Page<CustomerDto> searchCustomers(String searchTerm, Pageable pageable);

    /**
     * Хурдан хайлт (autocomplete-д ашиглах)
     */
    List<CustomerDto> quickSearchCustomers(String quickSearch);

    /**
     * Харилцагчийн төрлөөр харилцагч авах
     */
    Page<CustomerDto> getCustomersByType(Customer.CustomerType customerType, Pageable pageable);

    /**
     * KYC статусаар харилцагч авах
     */
    Page<CustomerDto> getCustomersByKycStatus(Customer.KycStatus kycStatus, Pageable pageable);

    /**
     * Нарийвчилсан filter-тэй хайлт
     */
    Page<CustomerDto> searchCustomersWithFilters(Customer.CustomerType customerType,
                                               Customer.KycStatus kycStatus,
                                               String city, String province,
                                               BigDecimal minIncome, BigDecimal maxIncome,
                                               LocalDateTime startDate, LocalDateTime endDate,
                                               Pageable pageable);

    // ==================== KYC MANAGEMENT ====================
    
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
     * KYC дууссангүй харилцагчдийн жагсаалт
     */
    Page<CustomerDto> getIncompleteKycCustomers(Pageable pageable);

    /**
     * KYC статус шинэчлэх (external enum parameter)
     */
    CustomerDto updateKYCStatus(UUID customerId, KYCStatus newStatus);

    // ==================== DUPLICATE CHECKING ====================
    
    /**
     * Давхардсан харилцагч хайх
     */
    List<CustomerDto> findDuplicateCustomers(CustomerDto customerDto);

    /**
     * Ижил төстэй харилцагч хайх
     */
    List<CustomerDto> findSimilarCustomers(String firstName, String lastName, 
                                         java.time.LocalDate birthDate, UUID excludeId);

    // ==================== VALIDATION ====================
    
    /**
     * Регистрийн дугаар байгаа эсэх
     */
    boolean existsByRegisterNumber(String registerNumber);

    /**
     * Утасны дугаар байгаа эсэх
     */
    boolean existsByPhone(String phone);

    /**
     * И-мэйл байгаа эсэх
     */
    boolean existsByEmail(String email);

    /**
     * И-мэйл ашиглах боломжтой эсэх
     */
    boolean isEmailAvailable(String email);

    /**
     * И-мэйл давтагдаагүй эсэх (legacy method)
     */
    boolean isEmailUnique(String email);

    /**
     * Харилцагчийн мэдээлэл зөв эсэх
     */
    boolean validateCustomerData(CustomerDto customerDto);

    // ==================== STATUS MANAGEMENT ====================
    
    /**
     * Харилцагчийн статус шинэчлэх
     */
    CustomerDto updateCustomerStatus(UUID customerId, CustomerStatus newStatus);

    // ==================== STATISTICS ====================
    
    /**
     * Харилцагчийн ерөнхий статистик
     */
    Map<String, Object> getCustomerStatistics();

    /**
     * Харилцагчийн тоог төрлөөр ангилах
     */
    Map<Customer.CustomerType, Long> getCustomerCountByType();

    /**
     * Харилцагчийн тоог KYC статусаар ангилах
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

    /**
     * Өнөөдрийн харилцагчийн статистик
     */
    Map<String, Object> getTodayCustomerStats();

    // ⭐ ШИНЭЭР НЭМЭГДСЭН: Харилцагчийн нийт тоо ⭐
    /**
     * Харилцагчийн нийт тоо авах
     */
    long getTotalCustomerCount();

    // ==================== LOAN RELATED ====================
    
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

    /**
     * Зээлийн дүнгээр эрэмбэлсэн харилцагчид
     */
    Page<CustomerDto> getTopCustomersByLoanAmount(Pageable pageable);

    // ==================== CUSTOMER INSIGHTS ====================
    
    /**
     * Саяхны харилцагчид
     */
    List<CustomerDto> getRecentCustomers();

    /**
     * Идэвхгүй харилцагчид
     */
    Page<CustomerDto> getInactiveCustomers(Pageable pageable);

    /**
     * Олон харилцагчийн KYC статус шинэчлэх
     */
    int updateKycStatusForCustomers(List<UUID> customerIds, Customer.KycStatus newStatus);

    // ==================== BULK OPERATIONS ====================
    
    /**
     * Олон харилцагч нэгэн зэрэг үүсгэх
     */
    List<CustomerDto> createCustomersBulk(List<CustomerDto> customers);

    /**
     * Харилцагчдийг Excel-д экспорт хийх
     */
    byte[] exportCustomersToExcel(List<UUID> customerIds);

    // ==================== PROFILE MANAGEMENT ====================
    
    /**
     * Харилцагчийн профайл шинэчлэх
     */
    CustomerDto updateCustomerProfile(UUID id, CustomerDto profileDto);

    /**
     * Нууцлалын тохиргоо шинэчлэх
     */
    CustomerDto updatePrivacySettings(UUID id, Map<String, Boolean> privacySettings);

    // ==================== AUDIT & TRACKING ====================
    
    /**
     * Харилцагчийн өөрчлөлтийн түүх
     */
    List<Map<String, Object>> getCustomerAuditHistory(UUID customerId);

    /**
     * Харилцагчийн үйл ажиллагааны log
     */
    List<Map<String, Object>> getCustomerActivityLog(UUID customerId, int days);

    // ==================== LOAN ELIGIBILITY ====================
    
    /**
     * Зээл авах боломжтой эсэх
     */
    boolean canCustomerApplyForLoan(UUID customerId);

    /**
     * ADDED: Зээлийн чадвар шалгах (тестэд шаардлагатай)
     */
    boolean checkEligibility(UUID customerId);

    /**
     * Зээлийн хязгаар тооцоолох
     */
    BigDecimal calculateLoanLimit(UUID customerId);

    /**
     * Эрсдэлийн ангилал тодорхойлох
     */
    String determineRiskCategory(UUID customerId);

    // ==================== CREDIT SCORE MANAGEMENT ====================
    
    /**
     * ADDED: Зээлийн оноо шинэчлэх (тестэд шаардлагатай)
     */
    CustomerDto updateCreditScore(UUID customerId, int creditScore);

    // ==================== NOTIFICATIONS ====================
    
    /**
     * Харилцагчид мэдэгдэл илгээх
     */
    boolean sendNotificationToCustomer(UUID customerId, String subject, String message);

    /**
     * KYC сануулга илгээх
     */
    boolean sendKycReminder(UUID customerId);

    // ==================== MAINTENANCE ====================
    
    /**
     * Идэвхгүй харилцагчдийг цэвэрлэх
     */
    int cleanupInactiveCustomers(int inactiveDays);

    /**
     * Дутуу мэдээлэлтэй харилцагчид
     */
    List<CustomerDto> getCustomersWithIncompleteInfo();

    /**
     * Өгөгдлийн бүрэн бүтэн байдал шалгах
     */
    Map<String, Object> validateDataIntegrity();
}