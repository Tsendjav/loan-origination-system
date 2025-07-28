package com.company.los.repository;

import com.company.los.entity.Customer;
import com.company.los.enums.CustomerStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Харилцагчийн Repository
 * Customer Repository Interface
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID>, JpaSpecificationExecutor<Customer> {

    // Суурь хайлтууд
    /**
     * Регистрийн дугаараар хайх
     */
    Optional<Customer> findByRegisterNumber(String registerNumber);

    /**
     * И-мэйлээр хайх
     */
    Optional<Customer> findByEmail(String email);

    /**
     * Утасны дугаараар хайх
     */
    Optional<Customer> findByPhone(String phone);

    /**
     * Регистрийн дугаар байгаа эсэхийг шалгах
     */
    boolean existsByRegisterNumber(String registerNumber);

    /**
     * И-мэйл байгаа эсэхийг шалгах
     */
    boolean existsByEmail(String email);

    /**
     * Утасны дугаар байгаа эсэхийг шалгах
     */
    boolean existsByPhone(String phone);

    /**
     * И-мэйл давхцаж байгаа эсэхийг шалгах (ID-г оруулаагүй)
     */
    @Query("SELECT COUNT(c) > 0 FROM Customer c WHERE " +
           "LOWER(c.email) = LOWER(:email) AND c.id != :excludeId")
    boolean existsByEmailAndIdNot(@Param("email") String email, @Param("excludeId") UUID excludeId);

    /**
     * Утасны дугаар давхцаж байгаа эсэхийг шалгах (ID-г оруулаагүй)
     */
    @Query("SELECT COUNT(c) > 0 FROM Customer c WHERE " +
           "c.phone = :phone AND c.id != :excludeId")
    boolean existsByPhoneAndIdNot(@Param("phone") String phone, @Param("excludeId") UUID excludeId);

    // Нэрээр хайх
    /**
     * Овогоор хайх
     */
    Page<Customer> findByLastName(String lastName, Pageable pageable);

    /**
     * Нэрээр хайх
     */
    Page<Customer> findByFirstName(String firstName, Pageable pageable);

    /**
     * Нэр овогоор хайх
     */
    @Query("SELECT c FROM Customer c WHERE " +
           "LOWER(c.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Customer> findByName(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Бүтэн нэрээр хайх
     */
    @Query("SELECT c FROM Customer c WHERE " +
           "LOWER(CONCAT(c.firstName, ' ', c.lastName)) LIKE LOWER(CONCAT('%', :fullName, '%'))")
    Page<Customer> findByFullName(@Param("fullName") String fullName, Pageable pageable);

    // Төрсөн огноогоор хайх
    /**
     * Төрсөн огноогоор хайх
     */
    Page<Customer> findByDateOfBirth(LocalDate dateOfBirth, Pageable pageable);

    /**
     * Төрсөн огноогоор хайх (хязгаар)
     */
    @Query("SELECT c FROM Customer c WHERE c.dateOfBirth BETWEEN :startDate AND :endDate")
    Page<Customer> findByDateOfBirthBetween(@Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate,
                                          Pageable pageable);

    /**
     * Насны бүлгээр хайх
     */
    @Query("SELECT c FROM Customer c WHERE " +
           "YEAR(CURRENT_DATE) - YEAR(c.dateOfBirth) BETWEEN :minAge AND :maxAge")
    Page<Customer> findByAgeBetween(@Param("minAge") Integer minAge,
                                  @Param("maxAge") Integer maxAge,
                                  Pageable pageable);

    // Хаягаар хайх
    /**
     * Хотоор хайх
     */
    Page<Customer> findByCity(String city, Pageable pageable);

    /**
     * Аймгаар хайх
     */
    Page<Customer> findByProvince(String province, Pageable pageable);

    /**
     * Хаягаар хайх (хэсэгчилсэн)
     */
    @Query("SELECT c FROM Customer c WHERE " +
           "LOWER(COALESCE(c.address, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Customer> findByAddressContaining(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Орлогоор хайх
    /**
     * Орлогын хязгаараар хайх
     */
    @Query("SELECT c FROM Customer c WHERE c.monthlyIncome BETWEEN :minIncome AND :maxIncome")
    Page<Customer> findByIncomeRange(@Param("minIncome") BigDecimal minIncome,
                                   @Param("maxIncome") BigDecimal maxIncome,
                                   Pageable pageable);

    /**
     * Өндөр орлоготой харилцагчид
     */
    @Query("SELECT c FROM Customer c WHERE c.monthlyIncome >= :highIncomeThreshold " +
           "ORDER BY c.monthlyIncome DESC")
    Page<Customer> findHighIncomeCustomers(@Param("highIncomeThreshold") BigDecimal highIncomeThreshold,
                                         Pageable pageable);

    // Зээлийн хүсэлттэй холбоотой
    /**
     * Зээлийн хүсэлттэй харилцагчид
     */
    @Query("SELECT c FROM Customer c WHERE SIZE(c.loanApplications) > 0")
    Page<Customer> findCustomersWithLoanApplications(Pageable pageable);

    /**
     * Зээлийн хүсэлтгүй харилцагчид
     */
    @Query("SELECT c FROM Customer c WHERE SIZE(c.loanApplications) = 0")
    Page<Customer> findCustomersWithoutLoanApplications(Pageable pageable);

    /**
     * Идэвхтэй зээлийн хүсэлттэй харилцагчид
     */
    @Query("SELECT DISTINCT c FROM Customer c JOIN c.loanApplications la " +
           "WHERE la.status IN ('PENDING', 'UNDER_REVIEW', 'APPROVED')")
    Page<Customer> findCustomersWithActiveLoanApplications(Pageable pageable);

    /**
     * Батлагдсан зээлтэй харилцагчид
     */
    @Query("SELECT DISTINCT c FROM Customer c JOIN c.loanApplications la " +
           "WHERE la.status = 'APPROVED'")
    Page<Customer> findCustomersWithApprovedLoans(Pageable pageable);

    // Баримттай холбоотой
    /**
     * Баримттай харилцагчид
     */
    @Query("SELECT c FROM Customer c WHERE SIZE(c.documents) > 0")
    Page<Customer> findCustomersWithDocuments(Pageable pageable);

    /**
     * Баримтгүй харилцагчид
     */
    @Query("SELECT c FROM Customer c WHERE SIZE(c.documents) = 0")
    Page<Customer> findCustomersWithoutDocuments(Pageable pageable);

    // Огноогоор хайх
    /**
     * Бүртгүүлсэн огноогоор хайх
     */
    @Query("SELECT c FROM Customer c WHERE c.createdAt BETWEEN :startDate AND :endDate")
    Page<Customer> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate,
                                        Pageable pageable);

    /**
     * Өнөөдөр бүртгүүлсэн харилцагчид
     */
    @Query("SELECT c FROM Customer c WHERE FORMATDATETIME(c.createdAt, 'yyyy-MM-dd') = FORMATDATETIME(CURRENT_TIMESTAMP(), 'yyyy-MM-dd')")
    List<Customer> findTodayRegistered();

    /**
     * Шинэ харилцагчид (сүүлийн 30 хоногт)
     */
    @Query("SELECT c FROM Customer c WHERE c.createdAt >= :oneMonthAgo ORDER BY c.createdAt DESC")
    List<Customer> findNewCustomers(@Param("oneMonthAgo") LocalDateTime oneMonthAgo);

    // Ерөнхий хайлт
    /**
     * Ерөнхий хайлт
     */
    @Query("SELECT c FROM Customer c WHERE " +
           "LOWER(c.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(COALESCE(c.email, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(COALESCE(c.phone, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(COALESCE(c.registerNumber, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(COALESCE(c.address, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Customer> findBySearchTerm(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Статистик
    /**
     * Хотоор тоолох
     */
    @Query("SELECT c.city, COUNT(c) FROM Customer c WHERE c.city IS NOT NULL " +
           "GROUP BY c.city ORDER BY COUNT(c) DESC")
    List<Object[]> countByCity();

    /**
     * Аймгаар тоолох
     */
    @Query("SELECT c.province, COUNT(c) FROM Customer c WHERE c.province IS NOT NULL " +
           "GROUP BY c.province ORDER BY COUNT(c) DESC")
    List<Object[]> countByProvince();

    /**
     * Сарын харилцагчийн статистик
     */
    @Query("SELECT TO_CHAR(c.createdAt, 'YYYY-MM'), COUNT(c) FROM Customer c " +
           "WHERE c.createdAt >= :startDate " +
           "GROUP BY TO_CHAR(c.createdAt, 'YYYY-MM') " +
           "ORDER BY TO_CHAR(c.createdAt, 'YYYY-MM')")
    List<Object[]> getMonthlyCustomerStats(@Param("startDate") LocalDateTime startDate);

    /**
     * Өнөөдрийн харилцагчийн статистик
     */
    @Query("SELECT " +
           "COUNT(CASE WHEN FORMATDATETIME(c.createdAt, 'yyyy-MM-dd') = FORMATDATETIME(CURRENT_TIMESTAMP(), 'yyyy-MM-dd') THEN 1 END) as newToday, " +
           "COUNT(CASE WHEN c.monthlyIncome >= 1000000 THEN 1 END) as highIncome, " +
           "COUNT(CASE WHEN SIZE(c.loanApplications) > 0 THEN 1 END) as withLoans, " +
           "COUNT(CASE WHEN SIZE(c.documents) = 0 THEN 1 END) as withoutDocuments " +
           "FROM Customer c")
    Object[] getTodayCustomerStats();

    // Статус шалгалт
    /**
     * Статусаар тоолох
     */
    long countByStatus(CustomerStatus status);

    /**
     * Огнооноос хойш бүртгүүлсэн харилцагчдыг тоолох
     */
    long countByRegistrationDateAfter(LocalDateTime date);

    // Data quality
    /**
     * Дутуу мэдээлэлтэй харилцагчид
     */
    @Query("SELECT c FROM Customer c WHERE " +
           "c.email IS NULL OR c.email = '' OR " +
           "c.phone IS NULL OR c.phone = '' OR " +
           "c.address IS NULL OR c.address = '' OR " +
           "c.monthlyIncome IS NULL")
    List<Customer> findCustomersWithIncompleteData();

    /**
     * Хуучин идэвхгүй харилцагчид
     */
    @Query("SELECT c FROM Customer c WHERE " +
           "c.createdAt < :oldDate AND SIZE(c.loanApplications) = 0")
    List<Customer> findOldInactiveCustomers(@Param("oldDate") LocalDateTime oldDate);

    // Cleanup
    /**
     * Зээлгүй, баримтгүй харилцагчид
     */
    @Query("SELECT c FROM Customer c WHERE " +
           "SIZE(c.loanApplications) = 0 AND SIZE(c.documents) = 0")
    List<Customer> findEmptyCustomers();
}