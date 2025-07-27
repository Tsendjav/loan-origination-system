package com.company.los.repository;

import com.company.los.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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
public interface CustomerRepository extends JpaRepository<Customer, UUID> {

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

    /**
     * Залуу харилцагчид (18-30 нас)
     */
    @Query("SELECT c FROM Customer c WHERE " +
           "YEAR(CURRENT_DATE) - YEAR(c.dateOfBirth) BETWEEN 18 AND 30")
    Page<Customer> findYoungCustomers(Pageable pageable);

    /**
     * Дунд насны харилцагчид (31-50 нас)
     */
    @Query("SELECT c FROM Customer c WHERE " +
           "YEAR(CURRENT_DATE) - YEAR(c.dateOfBirth) BETWEEN 31 AND 50")
    Page<Customer> findMiddleAgedCustomers(Pageable pageable);

    /**
     * Ахмад харилцагчид (50+ нас)
     */
    @Query("SELECT c FROM Customer c WHERE " +
           "YEAR(CURRENT_DATE) - YEAR(c.dateOfBirth) > 50")
    Page<Customer> findSeniorCustomers(Pageable pageable);

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

    /**
     * Дундаж орлоготой харилцагчид
     */
    @Query("SELECT c FROM Customer c WHERE " +
           "c.monthlyIncome BETWEEN :lowerBound AND :upperBound " +
           "ORDER BY c.monthlyIncome DESC")
    Page<Customer> findMiddleIncomeCustomers(@Param("lowerBound") BigDecimal lowerBound,
                                           @Param("upperBound") BigDecimal upperBound,
                                           Pageable pageable);

    /**
     * Бага орлоготой харилцагчид
     */
    @Query("SELECT c FROM Customer c WHERE c.monthlyIncome <= :lowIncomeThreshold " +
           "ORDER BY c.monthlyIncome ASC")
    Page<Customer> findLowIncomeCustomers(@Param("lowIncomeThreshold") BigDecimal lowIncomeThreshold,
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
     * Олон зээлийн хүсэлттэй харилцагчид
     */
    @Query("SELECT c FROM Customer c WHERE SIZE(c.loanApplications) > 1 " +
           "ORDER BY SIZE(c.loanApplications) DESC")
    Page<Customer> findCustomersWithMultipleLoanApplications(Pageable pageable);

    /**
     * Зээлийн хүсэлтийн тоогоор хайх
     */
    @Query("SELECT c FROM Customer c WHERE SIZE(c.loanApplications) >= :minApplications")
    List<Customer> findCustomersWithMinimumApplications(@Param("minApplications") int minApplications);

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

    /**
     * Цуцлагдсан зээлийн хүсэлттэй харилцагчид
     */
    @Query("SELECT DISTINCT c FROM Customer c JOIN c.loanApplications la " +
           "WHERE la.status = 'REJECTED'")
    Page<Customer> findCustomersWithRejectedLoans(Pageable pageable);

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

    /**
     * Баримтын тоогоор хайх
     */
    @Query("SELECT c FROM Customer c WHERE SIZE(c.documents) >= :minDocuments")
    List<Customer> findCustomersWithMinimumDocuments(@Param("minDocuments") int minDocuments);

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
     * H2 database-д FORMATDATETIME функц нь TIMESTAMP-ийг DATE болгоход ашиглагддаг.
     */
    @Query("SELECT c FROM Customer c WHERE FORMATDATETIME(c.createdAt, 'yyyy-MM-dd') = FORMATDATETIME(CURRENT_TIMESTAMP(), 'yyyy-MM-dd')")
    List<Customer> findTodayRegistered();

    /**
     * Энэ сард бүртгүүлсэн харилцагчид
     * H2 database-д YEAR болон MONTH функцуудыг ашиглаж байна.
     */
    @Query("SELECT c FROM Customer c WHERE " +
           "YEAR(c.createdAt) = YEAR(CURRENT_TIMESTAMP()) AND " +
           "MONTH(c.createdAt) = MONTH(CURRENT_TIMESTAMP())")
    List<Customer> findThisMonthRegistered();

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

    // Дэвшилтэт хайлт
    /**
     * Дэвшилтэт филтертэй хайлт
     */
    @Query("SELECT c FROM Customer c WHERE " +
           "(:firstName IS NULL OR LOWER(c.firstName) LIKE LOWER(CONCAT('%', :firstName, '%'))) AND " +
           "(:lastName IS NULL OR LOWER(c.lastName) LIKE LOWER(CONCAT('%', :lastName, '%'))) AND " +
           "(:email IS NULL OR LOWER(c.email) = LOWER(:email)) AND " +
           "(:phone IS NULL OR c.phone = :phone) AND " +
           "(:city IS NULL OR LOWER(c.city) = LOWER(:city)) AND " +
           "(:province IS NULL OR LOWER(c.province) = LOWER(:province)) AND " +
           "(:minAge IS NULL OR YEAR(CURRENT_DATE) - YEAR(c.dateOfBirth) >= :minAge) AND " +
           "(:maxAge IS NULL OR YEAR(CURRENT_DATE) - YEAR(c.dateOfBirth) <= :maxAge) AND " +
           "(:minIncome IS NULL OR c.monthlyIncome >= :minIncome) AND " +
           "(:maxIncome IS NULL OR c.monthlyIncome <= :maxIncome) AND " +
           "(:hasLoanApplications IS NULL OR " +
           "(:hasLoanApplications = true AND SIZE(c.loanApplications) > 0 OR " +
           ":hasLoanApplications = false AND SIZE(c.loanApplications) = 0)) AND " +
           "(:hasDocuments IS NULL OR " +
           "(:hasDocuments = true AND SIZE(c.documents) > 0 OR " +
           ":hasDocuments = false AND SIZE(c.documents) = 0))")
    Page<Customer> findByAdvancedFilters(
            @Param("firstName") String firstName,
            @Param("lastName") String lastName,
            @Param("email") String email,
            @Param("phone") String phone,
            @Param("city") String city,
            @Param("province") String province,
            @Param("minAge") Integer minAge,
            @Param("maxAge") Integer maxAge,
            @Param("minIncome") BigDecimal minIncome,
            @Param("maxIncome") BigDecimal maxIncome,
            @Param("hasLoanApplications") Boolean hasLoanApplications,
            @Param("hasDocuments") Boolean hasDocuments,
            Pageable pageable);

    // Статистик
    /**
     * Харилцагчийн үндсэн статистик
     */
    @Query("SELECT " +
           "COUNT(c) as totalCustomers, " +
           "COUNT(CASE WHEN SIZE(c.loanApplications) > 0 THEN 1 END) as customersWithLoans, " +
           "COUNT(CASE WHEN SIZE(c.documents) > 0 THEN 1 END) as customersWithDocuments, " +
           "AVG(c.monthlyIncome) as avgIncome, " +
           "AVG(YEAR(CURRENT_DATE()) - YEAR(c.dateOfBirth)) as avgAge " +
           "FROM Customer c")
    Object[] getCustomerStats();

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
     * Насны бүлгээр тоолох
     */
    @Query("SELECT " +
           "CASE " +
           "WHEN YEAR(CURRENT_DATE()) - YEAR(c.dateOfBirth) < 25 THEN 'Under 25' " +
           "WHEN YEAR(CURRENT_DATE()) - YEAR(c.dateOfBirth) BETWEEN 25 AND 35 THEN '25-35' " +
           "WHEN YEAR(CURRENT_DATE()) - YEAR(c.dateOfBirth) BETWEEN 36 AND 50 THEN '36-50' " +
           "WHEN YEAR(CURRENT_DATE()) - YEAR(c.dateOfBirth) BETWEEN 51 AND 65 THEN '51-65' " +
           "ELSE 'Over 65' END as ageGroup, " +
           "COUNT(c) " +
           "FROM Customer c " +
           "GROUP BY " +
           "CASE " +
           "WHEN YEAR(CURRENT_DATE()) - YEAR(c.dateOfBirth) < 25 THEN 'Under 25' " +
           "WHEN YEAR(CURRENT_DATE()) - YEAR(c.dateOfBirth) BETWEEN 25 AND 35 THEN '25-35' " +
           "WHEN YEAR(CURRENT_DATE()) - YEAR(c.dateOfBirth) BETWEEN 36 AND 50 THEN '36-50' " +
           "WHEN YEAR(CURRENT_DATE()) - YEAR(c.dateOfBirth) BETWEEN 51 AND 65 THEN '51-65' " +
           "ELSE 'Over 65' END")
    List<Object[]> countByAgeGroup();

    /**
     * Орлогын бүлгээр тоолох
     */
    @Query("SELECT " +
           "CASE " +
           "WHEN c.monthlyIncome < 500000 THEN 'Under 500K' " +
           "WHEN c.monthlyIncome BETWEEN 500000 AND 1000000 THEN '500K-1M' " +
           "WHEN c.monthlyIncome BETWEEN 1000001 AND 2000000 THEN '1M-2M' " +
           "WHEN c.monthlyIncome BETWEEN 2000001 AND 5000000 THEN '2M-5M' " +
           "ELSE 'Over 5M' END as incomeGroup, " +
           "COUNT(c) " +
           "FROM Customer c WHERE c.monthlyIncome IS NOT NULL " +
           "GROUP BY " +
           "CASE " +
           "WHEN c.monthlyIncome < 500000 THEN 'Under 500K' " +
           "WHEN c.monthlyIncome BETWEEN 500000 AND 1000000 THEN '500K-1M' " +
           "WHEN c.monthlyIncome BETWEEN 1000001 AND 2000000 THEN '1M-2M' " +
           "WHEN c.monthlyIncome BETWEEN 2000001 AND 5000000 THEN '2M-5M' " +
           "ELSE 'Over 5M' END")
    List<Object[]> countByIncomeGroup();

    /**
     * Сарын харилцагчийн статистик
     * H2 database-д TO_CHAR функц нь TIMESTAMP-ийг форматлахад ашиглагддаг.
     */
    @Query("SELECT TO_CHAR(c.createdAt, 'YYYY-MM'), COUNT(c) FROM Customer c " +
           "WHERE c.createdAt >= :startDate " +
           "GROUP BY TO_CHAR(c.createdAt, 'YYYY-MM') " +
           "ORDER BY TO_CHAR(c.createdAt, 'YYYY-MM')")
    List<Object[]> getMonthlyCustomerStats(@Param("startDate") LocalDateTime startDate);

    // Bulk операциуд
    /**
     * Олон харилцагчийн хот өөрчлөх
     */
    @Modifying
    @Query("UPDATE Customer c SET c.city = :newCity, c.updatedBy = :updatedBy WHERE c.id IN :customerIds")
    int updateCityForCustomers(@Param("customerIds") List<UUID> customerIds,
                             @Param("newCity") String newCity,
                             @Param("updatedBy") String updatedBy);

    /**
     * Олон харилцагчийн аймаг өөрчлөх
     */
    @Modifying
    @Query("UPDATE Customer c SET c.province = :newProvince, c.updatedBy = :updatedBy WHERE c.id IN :customerIds")
    int updateProvinceForCustomers(@Param("customerIds") List<UUID> customerIds,
                                 @Param("newProvince") String newProvince,
                                 @Param("updatedBy") String updatedBy);

    // Validation
    /**
     * Регистрийн дугаар давхцаж байгаа эсэхийг шалгах
     */
    @Query("SELECT COUNT(c) > 0 FROM Customer c WHERE " +
           "c.registerNumber = :registerNumber AND c.id != :excludeId")
    boolean existsByRegisterNumberAndIdNot(@Param("registerNumber") String registerNumber,
                                             @Param("excludeId") UUID excludeId);

    /**
     * И-мэйл давхцаж байгаа эсэхийг шалгах
     */
    @Query("SELECT COUNT(c) > 0 FROM Customer c WHERE " +
           "LOWER(c.email) = LOWER(:email) AND c.id != :excludeId")
    boolean existsByEmailIgnoreCaseAndIdNot(@Param("email") String email, @Param("excludeId") UUID excludeId);

    // Business logic
    /**
     * VIP харилцагчид (өндөр орлого болон олон зээл)
     */
    @Query("SELECT c FROM Customer c WHERE " +
           "c.monthlyIncome >= :vipIncomeThreshold AND " +
           "SIZE(c.loanApplications) >= :vipLoanThreshold " +
           "ORDER BY c.monthlyIncome DESC")
    List<Customer> findVipCustomers(@Param("vipIncomeThreshold") BigDecimal vipIncomeThreshold,
                                  @Param("vipLoanThreshold") int vipLoanThreshold);

    /**
     * Боломжит харилцагчид (зээлгүй, сайн орлого)
     */
    @Query("SELECT c FROM Customer c WHERE " +
           "SIZE(c.loanApplications) = 0 AND " +
           "c.monthlyIncome >= :goodIncomeThreshold " +
           "ORDER BY c.monthlyIncome DESC")
    List<Customer> findProspectiveCustomers(@Param("goodIncomeThreshold") BigDecimal goodIncomeThreshold);

    /**
     * Тогтмол харилцагчид (олон зээл авсан)
     */
    @Query("SELECT c FROM Customer c WHERE SIZE(c.loanApplications) > :loyalCustomerThreshold " +
           "ORDER BY SIZE(c.loanApplications) DESC")
    List<Customer> findLoyalCustomers(@Param("loyalCustomerThreshold") int loyalCustomerThreshold);

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
     * Хаяггүй харилцагчид
     */
    @Query("SELECT c FROM Customer c WHERE c.address IS NULL OR c.address = ''")
    List<Customer> findCustomersWithoutAddress();

    /**
     * Орлогогүй харилцагчид
     */
    @Query("SELECT c FROM Customer c WHERE c.monthlyIncome IS NULL")
    List<Customer> findCustomersWithoutIncome();

    // Dashboard статистик
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

    // Performance monitoring
    /**
     * Хамгийн идэвхтэй харилцагчид (зээлийн хүсэлтээр)
     */
    @Query("SELECT c FROM Customer c WHERE SIZE(c.loanApplications) > 0 " +
           "ORDER BY SIZE(c.loanApplications) DESC")
    Page<Customer> findMostActiveLoanCustomers(Pageable pageable);

    /**
     * Сүүлийн өөрчлөлт хийсэн харилцагчид
     */
    @Query("SELECT c FROM Customer c ORDER BY c.updatedAt DESC")
    Page<Customer> findRecentlyModified(Pageable pageable);

    // Cleanup
    /**
     * Зээлгүй, баримтгүй харилцагчид
     */
    @Query("SELECT c FROM Customer c WHERE " +
           "SIZE(c.loanApplications) = 0 AND SIZE(c.documents) = 0")
    List<Customer> findEmptyCustomers();

    /**
     * Хуучин харилцагчид (урт хугацаанд үйл ажиллагаагүй)
     */
    @Query("SELECT c FROM Customer c WHERE " +
           "c.createdAt < :oldDate AND SIZE(c.loanApplications) = 0")
    List<Customer> findOldInactiveCustomers(@Param("oldDate") LocalDateTime oldDate);
}