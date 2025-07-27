package com.company.los.repository;

import com.company.los.entity.LoanProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime; // LocalDateTime-ийг импортлох
import java.util.List;
import java.util.Map; // Map-ийг импортлох
import java.util.Optional;
import java.util.UUID;

/**
 * Зээлийн бүтээгдэхүүний Repository
 * Loan Product Repository Interface
 */
@Repository
public interface LoanProductRepository extends JpaRepository<LoanProduct, UUID> {

    // Суурь хайлтууд
    /**
     * Нэрээр хайх
     */
    Optional<LoanProduct> findByName(String name);

    /**
     * Нэр байгаа эсэхийг шалгах
     */
    boolean existsByName(String name);

    /**
     * Идэвхтэй бүтээгдэхүүнүүд
     */
    @Query("SELECT lp FROM LoanProduct lp WHERE lp.isActive = true ORDER BY lp.name")
    List<LoanProduct> findActiveLoanProducts();

    /**
     * Идэвхгүй бүтээгдэхүүнүүд
     */
    @Query("SELECT lp FROM LoanProduct lp WHERE lp.isActive = false")
    List<LoanProduct> findInactiveLoanProducts();

    // Дүнгийн хязгаараар хайх
    /**
     * Дүнгийн хязгаар дотор бүтээгдэхүүн хайх
     */
    @Query("SELECT lp FROM LoanProduct lp WHERE " +
           "lp.minAmount <= :amount AND lp.maxAmount >= :amount AND lp.isActive = true")
    List<LoanProduct> findByAmountRange(@Param("amount") BigDecimal amount);

    /**
     * Хамгийн бага дүнгийн хязгаараар хайх
     */
    @Query("SELECT lp FROM LoanProduct lp WHERE lp.minAmount BETWEEN :minAmount AND :maxAmount")
    Page<LoanProduct> findByMinAmountBetween(@Param("minAmount") BigDecimal minAmount,
                                           @Param("maxAmount") BigDecimal maxAmount,
                                           Pageable pageable);

    /**
     * Хамгийн их дүнгийн хязгаараар хайх
     */
    @Query("SELECT lp FROM LoanProduct lp WHERE lp.maxAmount BETWEEN :minAmount AND :maxAmount")
    Page<LoanProduct> findByMaxAmountBetween(@Param("minAmount") BigDecimal minAmount,
                                           @Param("maxAmount") BigDecimal maxAmount,
                                           Pageable pageable);

    /**
     * Том дүнтэй бүтээгдэхүүнүүд
     */
    @Query("SELECT lp FROM LoanProduct lp WHERE lp.maxAmount >= :largeAmountThreshold " +
           "ORDER BY lp.maxAmount DESC")
    Page<LoanProduct> findHighValueProducts(@Param("largeAmountThreshold") BigDecimal largeAmountThreshold,
                                          Pageable pageable);

    // Хугацааны хязгаараар хайх
    /**
     * Хугацааны хязгаар дотор бүтээгдэхүүн хайх
     */
    @Query("SELECT lp FROM LoanProduct lp WHERE " +
           "lp.minTermMonths <= :termMonths AND lp.maxTermMonths >= :termMonths AND lp.isActive = true")
    List<LoanProduct> findByTermRange(@Param("termMonths") Integer termMonths);

    /**
     * Богино хугацаатай бүтээгдэхүүнүүд
     */
    @Query("SELECT lp FROM LoanProduct lp WHERE lp.maxTermMonths <= :shortTermThreshold " +
           "ORDER BY lp.maxTermMonths ASC")
    List<LoanProduct> findShortTermProducts(@Param("shortTermThreshold") Integer shortTermThreshold);

    /**
     * Урт хугацаатай бүтээгдэхүүнүүд
     */
    @Query("SELECT lp FROM LoanProduct lp WHERE lp.minTermMonths >= :longTermThreshold " +
           "ORDER BY lp.minTermMonths DESC")
    List<LoanProduct> findLongTermProducts(@Param("longTermThreshold") Integer longTermThreshold);

    // Хүүгийн хувьаар хайх
    /**
     * Хүүгийн хязгаараар хайх
     */
    @Query("SELECT lp FROM LoanProduct lp WHERE " +
           "lp.baseRate BETWEEN :minRate AND :maxRate")
    Page<LoanProduct> findByBaseRateBetween(@Param("minRate") BigDecimal minRate,
                                          @Param("maxRate") BigDecimal maxRate,
                                          Pageable pageable);

    /**
     * Бага хүүтэй бүтээгдэхүүнүүд
     */
    @Query("SELECT lp FROM LoanProduct lp WHERE lp.baseRate <= :lowRateThreshold " +
           "ORDER BY lp.baseRate ASC")
    List<LoanProduct> findLowRateProducts(@Param("lowRateThreshold") BigDecimal lowRateThreshold);

    /**
     * Өндөр хүүтэй бүтээгдэхүүнүүд
     */
    @Query("SELECT lp FROM LoanProduct lp WHERE lp.baseRate >= :highRateThreshold " +
           "ORDER BY lp.baseRate DESC")
    List<LoanProduct> findHighRateProducts(@Param("highRateThreshold") BigDecimal highRateThreshold);

    // Нэрээр хайх
    /**
     * Нэрээр хайх (хэсэгчилсэн)
     */
    @Query("SELECT lp FROM LoanProduct lp WHERE LOWER(lp.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<LoanProduct> findByNameContaining(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Тайлбараар хайх
     */
    @Query("SELECT lp FROM LoanProduct lp WHERE " +
           "LOWER(COALESCE(lp.description, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<LoanProduct> findByDescriptionContaining(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Ерөнхий хайлт
     */
    @Query("SELECT lp FROM LoanProduct lp WHERE " +
           "LOWER(lp.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(COALESCE(lp.description, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<LoanProduct> findBySearchTerm(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Зээлийн хүсэлттэй холбоотой
    /**
     * Зээлийн хүсэлттэй бүтээгдэхүүнүүд
     */
    @Query("SELECT lp FROM LoanProduct lp WHERE SIZE(lp.loanApplications) > 0")
    Page<LoanProduct> findProductsWithApplications(Pageable pageable);

    /**
     * Зээлийн хүсэлтгүй бүтээгдэхүүнүүд
     */
    @Query("SELECT lp FROM LoanProduct lp WHERE SIZE(lp.loanApplications) = 0")
    List<LoanProduct> findProductsWithoutApplications();

    /**
     * Хамгийн их ашиглагддаг бүтээгдэхүүнүүд
     */
    @Query("SELECT lp FROM LoanProduct lp WHERE SIZE(lp.loanApplications) > 0 " +
           "ORDER BY SIZE(lp.loanApplications) DESC")
    Page<LoanProduct> findMostPopularProducts(Pageable pageable);

    /**
     * Зээлийн хүсэлтийн тоогоор хайх
     */
    @Query("SELECT lp FROM LoanProduct lp WHERE SIZE(lp.loanApplications) >= :minApplications")
    List<LoanProduct> findProductsWithMinimumApplications(@Param("minApplications") int minApplications);

    // Дэвшилтэт хайлт
    /**
     * Дэвшилтэт филтертэй хайлт
     */
    @Query("SELECT lp FROM LoanProduct lp WHERE " +
           "(:isActive IS NULL OR lp.isActive = :isActive) AND " +
           "(:minAmount IS NULL OR lp.minAmount >= :minAmount) AND " +
           "(:maxAmount IS NULL OR lp.maxAmount <= :maxAmount) AND " +
           "(:minTerm IS NULL OR lp.minTermMonths >= :minTerm) AND " +
           "(:maxTerm IS NULL OR lp.maxTermMonths <= :maxTerm) AND " +
           "(:minRate IS NULL OR lp.baseRate >= :minRate) AND " +
           "(:maxRate IS NULL OR lp.baseRate <= :maxRate) AND " +
           "(:hasApplications IS NULL OR (:hasApplications = TRUE AND SIZE(lp.loanApplications) > 0) OR (:hasApplications = FALSE AND SIZE(lp.loanApplications) = 0))")
    Page<LoanProduct> findByAdvancedFilters(
            @Param("isActive") Boolean isActive,
            @Param("minAmount") BigDecimal minAmount,
            @Param("maxAmount") BigDecimal maxAmount,
            @Param("minTerm") Integer minTerm,
            @Param("maxTerm") Integer maxTerm,
            @Param("minRate") BigDecimal minRate,
            @Param("maxRate") BigDecimal maxRate,
            @Param("hasApplications") Boolean hasApplications,
            Pageable pageable);

    // Статистик
    /**
     * Бүтээгдэхүүний үндсэн статистик
     */
    @Query("SELECT " +
           "COUNT(lp) as totalProducts, " +
           "COUNT(CASE WHEN lp.isActive = true THEN 1 END) as activeProducts, " +
           "COUNT(CASE WHEN SIZE(lp.loanApplications) > 0 THEN 1 END) as usedProducts, " +
           "AVG(lp.baseRate) as avgBaseRate, " +
           "AVG(lp.maxAmount) as avgMaxAmount " +
           "FROM LoanProduct lp")
    Object[] getProductStats();

    /**
     * Зээлийн хүсэлтийн тоогоор статистик
     */
    @Query("SELECT lp.name, SIZE(lp.loanApplications) as applicationCount FROM LoanProduct lp " +
           "ORDER BY SIZE(lp.loanApplications) DESC")
    List<Object[]> getApplicationCountByProduct();

    /**
     * Хүүгийн статистик
     */
    @Query("SELECT " +
           "MIN(lp.baseRate) as minRate, " +
           "MAX(lp.baseRate) as maxRate, " +
           "AVG(lp.baseRate) as avgRate " +
           "FROM LoanProduct lp WHERE lp.isActive = true")
    Object[] getRateStats();

    /**
     * Дүнгийн статистик
     */
    @Query("SELECT " +
           "MIN(lp.minAmount) as minMinAmount, " +
           "MAX(lp.maxAmount) as maxMaxAmount, " +
           "AVG(lp.minAmount) as avgMinAmount, " +
           "AVG(lp.maxAmount) as avgMaxAmount " +
           "FROM LoanProduct lp WHERE lp.isActive = true")
    Object[] getAmountStats();

    /**
     * Хугацааны статистик
     */
    @Query("SELECT " +
           "MIN(lp.minTermMonths) as minMinTerm, " +
           "MAX(lp.maxTermMonths) as maxMaxTerm, " +
           "AVG(lp.minTermMonths) as avgMinTerm, " +
           "AVG(lp.maxTermMonths) as avgMaxTerm " +
           "FROM LoanProduct lp WHERE lp.isActive = true")
    Object[] getTermStats();

    // Business logic
    /**
     * Харилцагчид тохирох бүтээгдэхүүнүүд
     */
    @Query("SELECT lp FROM LoanProduct lp WHERE " +
           "lp.minAmount <= :requestedAmount AND lp.maxAmount >= :requestedAmount AND " +
           "lp.minTermMonths <= :requestedTerm AND lp.maxTermMonths >= :requestedTerm AND " +
           "lp.isActive = true " +
           "ORDER BY lp.baseRate ASC")
    List<LoanProduct> findSuitableProducts(@Param("requestedAmount") BigDecimal requestedAmount,
                                         @Param("requestedTerm") Integer requestedTerm);

    /**
     * Зэрэглэлийн дагуу бүтээгдэхүүн олох
     */
    @Query("SELECT lp FROM LoanProduct lp WHERE lp.isActive = true " +
           "ORDER BY lp.baseRate ASC, lp.maxAmount DESC")
    List<LoanProduct> findProductsRankedByRate();

    /**
     * Эхлэгч зээлийн бүтээгдэхүүнүүд
     */
    @Query("SELECT lp FROM LoanProduct lp WHERE " +
           "lp.minAmount <= :beginnerMaxAmount AND lp.baseRate <= :beginnerMaxRate AND " +
           "lp.isActive = true " +
           "ORDER BY lp.minAmount ASC")
    List<LoanProduct> findBeginnerFriendlyProducts(@Param("beginnerMaxAmount") BigDecimal beginnerMaxAmount,
                                                 @Param("beginnerMaxRate") BigDecimal beginnerMaxRate);

    // Bulk операциуд
    /**
     * Олон бүтээгдэхүүний идэвхжүүлэх/идэвхгүй болгох
     */
    @Modifying
    @Query("UPDATE LoanProduct lp SET lp.isActive = :isActive, lp.updatedBy = :updatedBy " +
           "WHERE lp.id IN :productIds")
    int updateActiveStatus(@Param("productIds") List<UUID> productIds,
                         @Param("isActive") Boolean isActive,
                         @Param("updatedBy") String updatedBy);

    /**
     * Хүү өөрчлөх
     */
    @Modifying
    @Query("UPDATE LoanProduct lp SET lp.baseRate = :newRate, lp.updatedBy = :updatedBy " +
           "WHERE lp.id IN :productIds")
    int updateBaseRate(@Param("productIds") List<UUID> productIds,
                     @Param("newRate") BigDecimal newRate,
                     @Param("updatedBy") String updatedBy);

    /**
     * Дүнгийн хязгаар өөрчлөх
     */
    @Modifying
    @Query("UPDATE LoanProduct lp SET lp.minAmount = :minAmount, lp.maxAmount = :maxAmount, " +
           "lp.updatedBy = :updatedBy WHERE lp.id = :productId")
    int updateAmountLimits(@Param("productId") UUID productId,
                         @Param("minAmount") BigDecimal minAmount,
                         @Param("maxAmount") BigDecimal maxAmount,
                         @Param("updatedBy") String updatedBy);

    /**
     * Хугацааны хязгаар өөрчлөх
     */
    @Modifying
    @Query("UPDATE LoanProduct lp SET lp.minTermMonths = :minTerm, lp.maxTermMonths = :maxTerm, " +
           "lp.updatedBy = :updatedBy WHERE lp.id = :productId")
    int updateTermLimits(@Param("productId") UUID productId,
                       @Param("minTerm") Integer minTerm,
                       @Param("maxTerm") Integer maxTerm,
                       @Param("updatedBy") String updatedBy);

    // Validation
    /**
     * Нэр давхцаж байгаа эсэхийг шалгах
     */
    @Query("SELECT COUNT(lp) > 0 FROM LoanProduct lp WHERE " +
           "LOWER(lp.name) = LOWER(:name) AND lp.id != :excludeId")
    boolean existsByNameIgnoreCaseAndIdNot(@Param("name") String name, @Param("excludeId") UUID excludeId);

    /**
     * Хязгаарын зөв эсэхийг шалгах
     */
    @Query("SELECT COUNT(lp) FROM LoanProduct lp WHERE " +
           "lp.minAmount > lp.maxAmount OR lp.minTermMonths > lp.maxTermMonths")
    int countProductsWithInvalidLimits();

    // Dashboard статистик
    /**
     * Dashboard-ийн статистик
     */
    @Query("SELECT new map(" +
           "COUNT(lp) as totalProducts, " +
           "COUNT(CASE WHEN lp.isActive = true THEN 1 END) as activeProducts, " +
           "COUNT(CASE WHEN SIZE(lp.loanApplications) > 0 THEN 1 END) as usedProducts, " +
           "COUNT(CASE WHEN SIZE(lp.loanApplications) = 0 THEN 1 END) as unusedProducts, " +
           "SUM(SIZE(lp.loanApplications)) as totalApplications) " + // Added a space before FROM
           "FROM LoanProduct lp")
    Object[] getDashboardStats();

    /**
     * Өнөөдрийн статистик
     */
    @Query("SELECT new map(" +
           "COUNT(CASE WHEN lp.createdAt >= :startOfDay AND lp.createdAt < :endOfDay THEN 1 END) as createdToday, " +
           "COUNT(CASE WHEN lp.updatedAt >= :startOfDay AND lp.updatedAt < :endOfDay THEN 1 END) as updatedToday) " +
           "FROM LoanProduct lp")
    Map<String, Long> getTodayStats(@Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);


    // Cleanup
    /**
     * Ашиглагдаагүй бүтээгдэхүүнүүд
     */
    @Query("SELECT lp FROM LoanProduct lp WHERE SIZE(lp.loanApplications) = 0 AND lp.isActive = false")
    List<LoanProduct> findUnusedInactiveProducts();

    /**
     * Устгах боломжтой бүтээгдэхүүнүүд
     */
    @Query("SELECT lp FROM LoanProduct lp WHERE SIZE(lp.loanApplications) = 0 AND " +
           "lp.isActive = false AND lp.createdAt < :oldDate")
    List<LoanProduct> findDeletableProducts(@Param("oldDate") java.time.LocalDateTime oldDate);

    // Data quality
    /**
     * Тайлбаргүй бүтээгдэхүүнүүд
     */
    @Query("SELECT lp FROM LoanProduct lp WHERE lp.description IS NULL OR lp.description = ''")
    List<LoanProduct> findProductsWithoutDescription();

    /**
     * Алдаатай хязгаартай бүтээгдэхүүнүүд
     */
    @Query("SELECT lp FROM LoanProduct lp WHERE " +
           "lp.minAmount > lp.maxAmount OR lp.minTermMonths > lp.maxTermMonths OR " +
           "lp.baseRate <= 0 OR lp.baseRate > 1")
    List<LoanProduct> findProductsWithInvalidData();

    // Performance monitoring
    /**
     * Сүүлийн өөрчлөлт хийсэн бүтээгдэхүүнүүд
     */
    @Query("SELECT lp FROM LoanProduct lp ORDER BY lp.updatedAt DESC")
    Page<LoanProduct> findRecentlyModified(Pageable pageable);

    /**
     * Шинээр үүсгэсэн бүтээгдэхүүнүүд
     */
    @Query("SELECT lp FROM LoanProduct lp WHERE lp.createdAt >= :startDate ORDER BY lp.createdAt DESC")
    List<LoanProduct> findRecentlyCreated(@Param("startDate") java.time.LocalDateTime startDate);

    // Market analysis
    /**
     * Өрсөлдөхүйц хүүтэй бүтээгдэхүүнүүд
     */
    @Query("SELECT lp FROM LoanProduct lp WHERE " +
           "lp.baseRate <= :competitiveRate AND lp.isActive = true " +
           "ORDER BY lp.baseRate ASC")
    List<LoanProduct> findCompetitiveRateProducts(@Param("competitiveRate") BigDecimal competitiveRate);

    /**
     * Өндөр дүнтэй premium бүтээгдэхүүнүүд
     */
    @Query("SELECT lp FROM LoanProduct lp WHERE " +
           "lp.minAmount >= :premiumThreshold AND lp.isActive = true " +
           "ORDER BY lp.maxAmount DESC")
    List<LoanProduct> findPremiumProducts(@Param("premiumThreshold") BigDecimal premiumThreshold);
}
