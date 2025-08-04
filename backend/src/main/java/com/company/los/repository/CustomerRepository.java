package com.company.los.repository;

import com.company.los.entity.Customer;
import com.company.los.enums.CustomerStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Харилцагчийн Repository
 * Customer Repository Interface
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    // ==================== BASIC FINDERS ====================
    
    /**
     * Регистрийн дугаараар хайх
     */
    Optional<Customer> findByRegisterNumber(String registerNumber);

    /**
     * Утасны дугаараар хайх
     */
    Optional<Customer> findByPhone(String phone);

    /**
     * И-мэйлээр хайх
     */
    Optional<Customer> findByEmail(String email);

    /**
     * Username эсвэл email-ээр хайх
     */
    @Query("SELECT c FROM Customer c WHERE c.email = :identifier OR c.registerNumber = :identifier")
    Optional<Customer> findByUsernameOrEmail(@Param("identifier") String identifier);

    // ==================== EXISTENCE CHECKS ====================
    
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
     * И-мэйл давтагдаагүй эсэх (ID-г оруулахгүйгээр)
     */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Customer c WHERE c.email = :email AND c.id != :excludeId")
    boolean existsByEmailAndIdNot(@Param("email") String email, @Param("excludeId") UUID excludeId);

    /**
     * Утасны дугаар давтагдаагүй эсэх (ID-г оруулахгүйгээр)
     */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Customer c WHERE c.phone = :phone AND c.id != :excludeId")
    boolean existsByPhoneAndIdNot(@Param("phone") String phone, @Param("excludeId") UUID excludeId);

    // ==================== SEARCH OPERATIONS ====================
    
    /**
     * Text хайлт
     */
    @Query("SELECT c FROM Customer c WHERE " +
           "LOWER(c.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "c.phone LIKE CONCAT('%', :searchTerm, '%') OR " +
           "c.registerNumber LIKE CONCAT('%', :searchTerm, '%') OR " +
           "LOWER(c.companyName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Customer> findBySearchTerm(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * FIXED: Search method for test compatibility - multiple parameters with Page version
     */
    @Query("SELECT c FROM Customer c WHERE " +
           "LOWER(c.firstName) LIKE LOWER(CONCAT('%', :firstName, '%')) OR " +
           "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :lastName, '%')) OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :email, '%'))")
    Page<Customer> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
        @Param("firstName") String firstName, 
        @Param("lastName") String lastName, 
        @Param("email") String email,
        Pageable pageable);

    /**
     * FIXED: Search method for test compatibility - multiple parameters with List version (for current tests)
     */
    @Query("SELECT c FROM Customer c WHERE " +
           "LOWER(c.firstName) LIKE LOWER(CONCAT('%', :firstName, '%')) OR " +
           "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :lastName, '%')) OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :email, '%'))")
    List<Customer> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
        @Param("firstName") String firstName, 
        @Param("lastName") String lastName, 
        @Param("email") String email);

    /**
     * Харилцагчийн төрлөөр хайх
     */
    List<Customer> findByCustomerType(Customer.CustomerType customerType);

    /**
     * KYC статусаар хайх
     */
    List<Customer> findByKycStatus(Customer.KycStatus kycStatus);

    /**
     * Статусаар хайх
     */
    List<Customer> findByStatus(CustomerStatus status);

    /**
     * Идэвхтэй харилцагчид
     */
    List<Customer> findByIsActiveTrue();

    /**
     * Идэвхгүй харилцагчид
     */
    List<Customer> findByIsActiveFalse();

    // ==================== STATISTICS ====================
    
    /**
     * Статусаар тоолох
     */
    long countByStatus(CustomerStatus status);

    /**
     * ⭐ ЗАСВАРЛАСАН: isActive field-аар тоолох (test-д шаардлагатай) ⭐
     */
    long countByIsActive(boolean isActive);

    /**
     * Харилцагчийн төрлөөр тоолох
     */
    long countByCustomerType(Customer.CustomerType customerType);

    /**
     * KYC статусаар тоолох
     */
    long countByKycStatus(Customer.KycStatus kycStatus);

    /**
     * ADDED: Employment status-аар тоолох (тестэд шаардлагатай)
     */
    long countByEmploymentStatus(String employmentStatus);

    /**
     * ADDED: Credit score-оор тоолох (тестэд шаардлагатай)
     */
    long countByCreditScoreGreaterThanEqual(Integer creditScore);

    /**
     * Хотоор тоолох
     */
    @Query("SELECT c.city, COUNT(c) FROM Customer c WHERE c.city IS NOT NULL GROUP BY c.city")
    List<Object[]> countByCity();

    /**
     * Аймгаар тоолох
     */
    @Query("SELECT c.province, COUNT(c) FROM Customer c WHERE c.province IS NOT NULL GROUP BY c.province")
    List<Object[]> countByProvince();

    /**
     * Огнооны дараах харилцагчдийг тоолох
     */
    long countByRegistrationDateAfter(LocalDateTime date);

    // ==================== DATE BASED QUERIES ====================
    
    /**
     * Тодорхой өдрийн дараа бүртгүүлсэн харилцагчид
     */
    List<Customer> findByCreatedAtAfter(LocalDateTime date);

    /**
     * Тодорхой хугацааны дотор бүртгүүлсэн харилцагчид
     */
    @Query("SELECT c FROM Customer c WHERE c.createdAt BETWEEN :startDate AND :endDate")
    List<Customer> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                         @Param("endDate") LocalDateTime endDate);

    /**
     * Шинэ харилцагчид
     */
    @Query("SELECT c FROM Customer c WHERE c.createdAt > :since ORDER BY c.createdAt DESC")
    List<Customer> findNewCustomers(@Param("since") LocalDateTime since);

    /**
     * Удаан хугацаанд идэвхгүй харилцагчид
     */
    @Query("SELECT c FROM Customer c WHERE c.isActive = false AND c.updatedAt < :before")
    List<Customer> findOldInactiveCustomers(@Param("before") LocalDateTime before);

    // ==================== COMPLEX QUERIES ====================
    
    /**
     * Зээлийн хүсэлттэй харилцагчид
     */
    @Query("SELECT DISTINCT c FROM Customer c JOIN c.loanApplications la WHERE la.id IS NOT NULL")
    Page<Customer> findCustomersWithLoanApplications(Pageable pageable);

    /**
     * Идэвхтэй зээлтэй харилцагчид
     */
    @Query("SELECT DISTINCT c FROM Customer c JOIN c.loanApplications la WHERE la.status = 'APPROVED'")
    Page<Customer> findCustomersWithActiveLoanApplications(Pageable pageable);

    /**
     * Зээлийн хүсэлтгүй харилцагчид
     */
    @Query("SELECT c FROM Customer c WHERE c.id NOT IN (SELECT DISTINCT la.customer.id FROM LoanApplication la)")
    Page<Customer> findCustomersWithoutLoanApplications(Pageable pageable);

    /**
     * Дутуу мэдээлэлтэй харилцагчид
     */
    @Query("SELECT c FROM Customer c WHERE " +
           "(c.customerType = 'INDIVIDUAL' AND (c.firstName IS NULL OR c.lastName IS NULL OR c.birthDate IS NULL)) OR " +
           "(c.customerType = 'BUSINESS' AND (c.companyName IS NULL OR c.businessRegistrationNumber IS NULL)) OR " +
           "c.email IS NULL OR c.phone IS NULL OR c.address IS NULL")
    List<Customer> findCustomersWithIncompleteData();

    // ==================== MONTHLY STATISTICS ====================
    
    /**
     * Сарын статистик
     */
    @Query("SELECT " +
           "FUNCTION('YEAR', c.createdAt) as year, " +
           "FUNCTION('MONTH', c.createdAt) as month, " +
           "COUNT(c) as count " +
           "FROM Customer c " +
           "WHERE c.createdAt >= :startDate " +
           "GROUP BY FUNCTION('YEAR', c.createdAt), FUNCTION('MONTH', c.createdAt) " +
           "ORDER BY year, month")
    List<Object[]> getMonthlyCustomerStats(@Param("startDate") LocalDateTime startDate);

    /**
     * Өнөөдрийн статистик
     */
    @Query(value = "SELECT " +
                   "COUNT(CASE WHEN DATE(created_at) = CURRENT_DATE THEN 1 END) as newToday, " +
                   "COUNT(CASE WHEN monthly_income > 1000000 THEN 1 END) as highIncome, " +
                   "COUNT(CASE WHEN id IN (SELECT DISTINCT customer_id FROM loan_applications) THEN 1 END) as withLoans, " +
                   "COUNT(CASE WHEN (first_name IS NULL OR last_name IS NULL OR email IS NULL) THEN 1 END) as withoutDocuments " +
                   "FROM customers", 
           nativeQuery = true)
    Object[] getTodayCustomerStats();

    // ==================== ADVANCED SEARCH ====================
    
    /**
     * Нарийвчилсан хайлт
     */
    @Query("SELECT c FROM Customer c WHERE " +
           "(:customerType IS NULL OR c.customerType = :customerType) AND " +
           "(:kycStatus IS NULL OR c.kycStatus = :kycStatus) AND " +
           "(:city IS NULL OR LOWER(c.city) = LOWER(:city)) AND " +
           "(:province IS NULL OR LOWER(c.province) = LOWER(:province)) AND " +
           "(:isActive IS NULL OR c.isActive = :isActive)")
    Page<Customer> findWithFilters(@Param("customerType") Customer.CustomerType customerType,
                                  @Param("kycStatus") Customer.KycStatus kycStatus,
                                  @Param("city") String city,
                                  @Param("province") String province,
                                  @Param("isActive") Boolean isActive,
                                  Pageable pageable);

    /**
     * Орлогын хязгаараар хайх
     */
    @Query("SELECT c FROM Customer c WHERE " +
           "c.monthlyIncome BETWEEN :minIncome AND :maxIncome " +
           "ORDER BY c.monthlyIncome DESC")
    List<Customer> findByIncomeRange(@Param("minIncome") java.math.BigDecimal minIncome,
                                    @Param("maxIncome") java.math.BigDecimal maxIncome);

    /**
     * KYC дууссангүй харилцагчид
     */
    @Query("SELECT c FROM Customer c WHERE c.kycStatus != 'COMPLETED' ORDER BY c.createdAt")
    List<Customer> findIncompleteKycCustomers();

    /**
     * Дублицат мэдээлэлтэй харилцагчид
     */
    @Query("SELECT c FROM Customer c WHERE " +
           "c.registerNumber = :registerNumber OR " +
           "c.email = :email OR " +
           "c.phone = :phone")
    List<Customer> findPotentialDuplicates(@Param("registerNumber") String registerNumber,
                                          @Param("email") String email,
                                          @Param("phone") String phone);

    // ==================== BULK OPERATIONS ====================
    
    /**
     * Олон харилцагчийн статус шинэчлэх
     */
    @Modifying
    @Query("UPDATE Customer c SET c.status = :status, c.updatedAt = CURRENT_TIMESTAMP WHERE c.id IN :customerIds")
    int updateStatusForCustomers(@Param("customerIds") List<UUID> customerIds, 
                                @Param("status") CustomerStatus status);

    /**
     * Олон харилцагчийн KYC статус шинэчлэх
     */
    @Modifying
    @Query("UPDATE Customer c SET c.kycStatus = :kycStatus, c.updatedAt = CURRENT_TIMESTAMP WHERE c.id IN :customerIds")
    int updateKycStatusForCustomers(@Param("customerIds") List<UUID> customerIds, 
                                   @Param("kycStatus") Customer.KycStatus kycStatus);

    // ==================== CUSTOM NATIVE QUERIES ====================
    
    /**
     * Харилцагчийн KYC progression report
     */
    @Query(value = "SELECT " +
                   "kyc_status, " +
                   "COUNT(*) as count, " +
                   "AVG(DATEDIFF(COALESCE(kyc_completed_at, NOW()), created_at)) as avg_days " +
                   "FROM customers " +
                   "GROUP BY kyc_status", 
           nativeQuery = true)
    List<Object[]> getKycProgressionReport();

    /**
     * Топ хотууд харилцагчийн тоогоор
     */
    @Query(value = "SELECT city, COUNT(*) as customer_count " +
                   "FROM customers " +
                   "WHERE city IS NOT NULL " +
                   "GROUP BY city " +
                   "ORDER BY customer_count DESC " +
                   "LIMIT 10", 
           nativeQuery = true)
    List<Object[]> getTopCitiesByCustomerCount();

    /**
     * Орлогын статистик
     */
    @Query(value = "SELECT " +
                   "customer_type, " +
                   "COUNT(*) as count, " +
                   "AVG(monthly_income) as avg_income, " +
                   "MIN(monthly_income) as min_income, " +
                   "MAX(monthly_income) as max_income " +
                   "FROM customers " +
                   "WHERE monthly_income IS NOT NULL " +
                   "GROUP BY customer_type", 
           nativeQuery = true)
    List<Object[]> getIncomeStatistics();
}