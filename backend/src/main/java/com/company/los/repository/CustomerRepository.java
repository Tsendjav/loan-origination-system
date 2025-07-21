package com.company.los.repository;

import com.company.los.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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

    // Суурь хайлтууд
    /**
     * Регистрийн дугаараар харилцагч хайх
     */
    Optional<Customer> findByRegisterNumber(String registerNumber);

    /**
     * Утасны дугаараар харилцагч хайх
     */
    Optional<Customer> findByPhone(String phone);

    /**
     * И-мэйлээр харилцагч хайх
     */
    Optional<Customer> findByEmail(String email);

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

    // Төрлөөр хайх
    /**
     * Харилцагчийн төрлөөр хайх
     */
    Page<Customer> findByCustomerType(Customer.CustomerType customerType, Pageable pageable);

    /**
     * KYC статусаар хайх
     */
    Page<Customer> findByKycStatus(Customer.KycStatus kycStatus, Pageable pageable);

    // Нэрээр хайх
    /**
     * Овог нэрээр хайх (хувь хүнд)
     */
    @Query("SELECT c FROM Customer c WHERE c.customerType = 'INDIVIDUAL' AND " +
           "(LOWER(c.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Customer> findIndividualsByName(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Компанийн нэрээр хайх (хуулийн этгээдэд)
     */
    @Query("SELECT c FROM Customer c WHERE c.customerType = 'BUSINESS' AND " +
           "LOWER(c.companyName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Customer> findBusinessesByName(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Ерөнхий хайлт
    /**
     * Ерөнхий хайлт - нэр, регистр, утас, и-мэйлээр
     */
    @Query("SELECT c FROM Customer c WHERE " +
           "LOWER(c.registerNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.phone) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(COALESCE(c.email, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "(c.customerType = 'INDIVIDUAL' AND (" +
           "LOWER(COALESCE(c.firstName, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(COALESCE(c.lastName, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%')))) OR " +
           "(c.customerType = 'BUSINESS' AND " +
           "LOWER(COALESCE(c.companyName, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Customer> findBySearchTerm(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Хурдан хайлт
    /**
     * Хурдан хайлт - ID, регистр, утасаар
     */
    @Query("SELECT c FROM Customer c WHERE " +
           "CAST(c.id AS string) LIKE CONCAT('%', :quickSearch, '%') OR " +
           "c.registerNumber LIKE CONCAT('%', :quickSearch, '%') OR " +
           "c.phone LIKE CONCAT('%', :quickSearch, '%')")
    List<Customer> quickSearch(@Param("quickSearch") String quickSearch);

    // Огноогоор хайх
    /**
     * Тодорхой хугацаанд бүртгүүлсэн харилцагчид
     */
    @Query("SELECT c FROM Customer c WHERE c.createdAt BETWEEN :startDate AND :endDate")
    Page<Customer> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                        @Param("endDate") LocalDateTime endDate, 
                                        Pageable pageable);

    /**
     * Өнөөдөр бүртгүүлсэн харилцагчид
     */
    @Query("SELECT c FROM Customer c WHERE DATE(c.createdAt) = CURRENT_DATE")
    List<Customer> findTodayRegistered();

    // Зээлийн хүсэлттэй холбоотой
    /**
     * Зээлийн хүсэлттэй харилцагчид
     */
    @Query("SELECT DISTINCT c FROM Customer c JOIN c.loanApplications la WHERE la.isDeleted = false")
    Page<Customer> findCustomersWithLoanApplications(Pageable pageable);

    /**
     * Зээлийн хүсэлтгүй харилцагчид
     */
    @Query("SELECT c FROM Customer c WHERE c.id NOT IN " +
           "(SELECT DISTINCT la.customer.id FROM LoanApplication la WHERE la.isDeleted = false)")
    Page<Customer> findCustomersWithoutLoanApplications(Pageable pageable);

    /**
     * Идэвхтэй зээлтэй харилцагчид
     */
    @Query("SELECT DISTINCT c FROM Customer c JOIN c.loanApplications la " +
           "WHERE la.status = 'DISBURSED' AND la.isDeleted = false")
    Page<Customer> findCustomersWithActiveLoans(Pageable pageable);

    // KYC холбоотой
    /**
     * KYC дуусаагүй харилцагчид
     */
    @Query("SELECT c FROM Customer c WHERE c.kycStatus != 'COMPLETED'")
    Page<Customer> findIncompleteKyc(Pageable pageable);

    /**
     * KYC дууссан харилцагчид
     */
    @Query("SELECT c FROM Customer c WHERE c.kycStatus = 'COMPLETED'")
    Page<Customer> findCompletedKyc(Pageable pageable);

    // Статистик
    /**
     * Харилцагчийн төрлөөр тоолох
     */
    @Query("SELECT c.customerType, COUNT(c) FROM Customer c GROUP BY c.customerType")
    List<Object[]> countByCustomerType();

    /**
     * KYC статусаар тоолох
     */
    @Query("SELECT c.kycStatus, COUNT(c) FROM Customer c GROUP BY c.kycStatus")
    List<Object[]> countByKycStatus();

    /**
     * Сарын харилцагч тоо
     */
    @Query("SELECT DATE_FORMAT(c.createdAt, '%Y-%m'), COUNT(c) FROM Customer c " +
           "WHERE c.createdAt >= :startDate " +
           "GROUP BY DATE_FORMAT(c.createdAt, '%Y-%m') " +
           "ORDER BY DATE_FORMAT(c.createdAt, '%Y-%m')")
    List<Object[]> getMonthlyCustomerStats(@Param("startDate") LocalDateTime startDate);

    // Дупликат шалгах
    /**
     * Төстэй харилцагч хайх (нэр, төрсөн өдөр)
     */
    @Query("SELECT c FROM Customer c WHERE c.customerType = 'INDIVIDUAL' AND " +
           "c.firstName = :firstName AND c.lastName = :lastName AND " +
           "c.birthDate = :birthDate AND c.id != :excludeId")
    List<Customer> findSimilarCustomers(@Param("firstName") String firstName,
                                      @Param("lastName") String lastName,
                                      @Param("birthDate") java.time.LocalDate birthDate,
                                      @Param("excludeId") UUID excludeId);

    // Бизнес дүрүүд
    /**
     * ТОП харилцагчид (зээлийн дүнгээр)
     */
    @Query("SELECT c, SUM(la.approvedAmount) as totalLoanAmount FROM Customer c " +
           "JOIN c.loanApplications la " +
           "WHERE la.status = 'DISBURSED' AND la.isDeleted = false " +
           "GROUP BY c " +
           "ORDER BY totalLoanAmount DESC")
    Page<Object[]> findTopCustomersByLoanAmount(Pageable pageable);

    /**
     * Шинэ харилцагчид (сүүлийн 30 хоногт)
     */
    @Query("SELECT c FROM Customer c WHERE c.createdAt >= :thirtyDaysAgo ORDER BY c.createdAt DESC")
    List<Customer> findRecentCustomers(@Param("thirtyDaysAgo") LocalDateTime thirtyDaysAgo);

    /**
     * Идэвхгүй харилцагчид (90 хоногоос илүү зээлийн хүсэлт өгөөгүй)
     */
    @Query("SELECT c FROM Customer c WHERE c.id NOT IN (" +
           "SELECT la.customer.id FROM LoanApplication la " +
           "WHERE la.createdAt >= :ninetyDaysAgo AND la.isDeleted = false" +
           ") AND c.createdAt < :ninetyDaysAgo")
    Page<Customer> findInactiveCustomers(@Param("ninetyDaysAgo") LocalDateTime ninetyDaysAgo, Pageable pageable);

    // Дэвшилтэт хайлт
    /**
     * Филтертэй дэвшилтэт хайлт
     */
    @Query("SELECT c FROM Customer c WHERE " +
           "(:customerType IS NULL OR c.customerType = :customerType) AND " +
           "(:kycStatus IS NULL OR c.kycStatus = :kycStatus) AND " +
           "(:city IS NULL OR LOWER(c.city) = LOWER(:city)) AND " +
           "(:province IS NULL OR LOWER(c.province) = LOWER(:province)) AND " +
           "(:minIncome IS NULL OR c.monthlyIncome >= :minIncome) AND " +
           "(:maxIncome IS NULL OR c.monthlyIncome <= :maxIncome) AND " +
           "(:startDate IS NULL OR c.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR c.createdAt <= :endDate)")
    Page<Customer> findByAdvancedFilters(
            @Param("customerType") Customer.CustomerType customerType,
            @Param("kycStatus") Customer.KycStatus kycStatus,
            @Param("city") String city,
            @Param("province") String province,
            @Param("minIncome") java.math.BigDecimal minIncome,
            @Param("maxIncome") java.math.BigDecimal maxIncome,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    // Bulk операциуд
    /**
     * Олон харилцагчийн KYC статус өөрчлөх
     */
    @Query("UPDATE Customer c SET c.kycStatus = :newStatus WHERE c.id IN :customerIds")
    int updateKycStatusForCustomers(@Param("customerIds") List<UUID> customerIds, 
                                  @Param("newStatus") Customer.KycStatus newStatus);

    /**
     * Хотоор харилцагчийн тоо
     */
    @Query("SELECT c.city, COUNT(c) FROM Customer c WHERE c.city IS NOT NULL " +
           "GROUP BY c.city ORDER BY COUNT(c) DESC")
    List<Object[]> getCustomerCountByCity();

    /**
     * Аймгаар харилцагчийн тоо
     */
    @Query("SELECT c.province, COUNT(c) FROM Customer c WHERE c.province IS NOT NULL " +
           "GROUP BY c.province ORDER BY COUNT(c) DESC")
    List<Object[]> getCustomerCountByProvince();
}