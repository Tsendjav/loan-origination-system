package com.company.los.repository;

import com.company.los.entity.SystemConfig;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Системийн тохиргооны Repository
 * System Configuration Repository Interface
 */
@Repository
public interface SystemConfigRepository extends JpaRepository<SystemConfig, String> {

    // Суурь хайлтууд
    /**
     * Түлхүүр үгээр хайх
     */
    Optional<SystemConfig> findByConfigKey(String configKey);

    /**
     * Түлхүүр үг байгаа эсэхийг шалгах
     */
    boolean existsByConfigKey(String configKey);

    /**
     * Категориор хайх
     */
    Page<SystemConfig> findByCategory(String category, Pageable pageable);

    /**
     * Бүх категори
     */
    @Query("SELECT DISTINCT sc.category FROM SystemConfig sc WHERE sc.category IS NOT NULL ORDER BY sc.category")
    List<String> findAllCategories();

    // Төрлөөр хайх
    /**
     * Утгын төрлөөр хайх
     */
    Page<SystemConfig> findByValueType(String valueType, Pageable pageable);

    /**
     * Boolean төрлийн тохиргоонууд
     */
    @Query("SELECT sc FROM SystemConfig sc WHERE sc.valueType = 'BOOLEAN'")
    List<SystemConfig> findBooleanConfigs();

    /**
     * Integer төрлийн тохиргоонууд
     */
    @Query("SELECT sc FROM SystemConfig sc WHERE sc.valueType = 'INTEGER'")
    List<SystemConfig> findIntegerConfigs();

    /**
     * String төрлийн тохиргоонууд
     */
    @Query("SELECT sc FROM SystemConfig sc WHERE sc.valueType = 'STRING'")
    List<SystemConfig> findStringConfigs();

    /**
     * Decimal төрлийн тохиргоонууд
     */
    @Query("SELECT sc FROM SystemConfig sc WHERE sc.valueType = 'DECIMAL'")
    List<SystemConfig> findDecimalConfigs();

    // Идэвхжилээр хайх
    /**
     * Идэвхтэй тохиргоонууд
     */
    @Query("SELECT sc FROM SystemConfig sc WHERE sc.isActive = true ORDER BY sc.category, sc.configKey")
    List<SystemConfig> findActiveConfigs();

    /**
     * Идэвхгүй тохиргоонууд
     */
    @Query("SELECT sc FROM SystemConfig sc WHERE sc.isActive = false")
    List<SystemConfig> findInactiveConfigs();

    // Цэгээр өөрчлөх боломжоор хайх
    /**
     * Runtime-д өөрчлөх боломжтой тохиргоонууд
     */
    @Query("SELECT sc FROM SystemConfig sc WHERE sc.isRuntimeEditable = true ORDER BY sc.category, sc.configKey")
    List<SystemConfig> findRuntimeEditableConfigs();

    /**
     * Runtime-д өөрчлөх боломжгүй тохиргоонууд
     */
    @Query("SELECT sc FROM SystemConfig sc WHERE sc.isRuntimeEditable = false")
    List<SystemConfig> findReadOnlyConfigs();

    // Нэрээр хайх
    /**
     * Түлхүүр үгээр хайх (хэсэгчилсэн)
     */
    @Query("SELECT sc FROM SystemConfig sc WHERE LOWER(sc.configKey) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<SystemConfig> findByConfigKeyContaining(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Тайлбараар хайх
     */
    @Query("SELECT sc FROM SystemConfig sc WHERE " +
           "LOWER(COALESCE(sc.description, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<SystemConfig> findByDescriptionContaining(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Утгаар хайх
     */
    @Query("SELECT sc FROM SystemConfig sc WHERE " +
           "LOWER(sc.configValue) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<SystemConfig> findByValueContaining(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Ерөнхий хайлт
     */
    @Query("SELECT sc FROM SystemConfig sc WHERE " +
           "LOWER(sc.configKey) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(sc.configValue) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(COALESCE(sc.description, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(COALESCE(sc.category, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<SystemConfig> findBySearchTerm(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Дэвшилтэт хайлт
    /**
     * Дэвшилтэт филтертэй хайлт
     */
    @Query("SELECT sc FROM SystemConfig sc WHERE " +
           "(:category IS NULL OR sc.category = :category) AND " +
           "(:valueType IS NULL OR sc.valueType = :valueType) AND " +
           "(:isActive IS NULL OR sc.isActive = :isActive) AND " +
           "(:isRuntimeEditable IS NULL OR sc.isRuntimeEditable = :isRuntimeEditable)")
    Page<SystemConfig> findByAdvancedFilters(
            @Param("category") String category,
            @Param("valueType") String valueType,
            @Param("isActive") Boolean isActive,
            @Param("isRuntimeEditable") Boolean isRuntimeEditable,
            Pageable pageable);

    // Business logic methods
    /**
     * Системийн тохиргооны утга авах
     */
    @Query("SELECT sc.configValue FROM SystemConfig sc WHERE sc.configKey = :configKey AND sc.isActive = true")
    Optional<String> findConfigValue(@Param("configKey") String configKey);

    /**
     * Boolean утга авах
     */
    @Query("SELECT CASE WHEN LOWER(sc.configValue) IN ('true', '1', 'yes', 'on') THEN true ELSE false END " +
           "FROM SystemConfig sc WHERE sc.configKey = :configKey AND sc.isActive = true")
    Optional<Boolean> findBooleanValue(@Param("configKey") String configKey);

    /**
     * Integer утга авах
     */
    @Query("SELECT CAST(sc.configValue AS INTEGER) FROM SystemConfig sc " +
           "WHERE sc.configKey = :configKey AND sc.isActive = true AND sc.valueType = 'INTEGER'")
    Optional<Integer> findIntegerValue(@Param("configKey") String configKey);

    /**
     * Decimal утга авах
     */
    @Query("SELECT CAST(sc.configValue AS DECIMAL) FROM SystemConfig sc " +
           "WHERE sc.configKey = :configKey AND sc.isActive = true AND sc.valueType = 'DECIMAL'")
    Optional<java.math.BigDecimal> findDecimalValue(@Param("configKey") String configKey);

    // Common system configurations
    /**
     * Системийн үндсэн тохиргоонууд
     */
    @Query("SELECT sc FROM SystemConfig sc WHERE sc.configKey IN " +
           "('SYSTEM_NAME', 'SYSTEM_VERSION', 'SYSTEM_ENVIRONMENT', 'DEFAULT_LANGUAGE', 'DEFAULT_TIMEZONE') " +
           "AND sc.isActive = true")
    List<SystemConfig> findSystemBasicConfigs();

    /**
     * Зээлийн тохиргоонууд
     */
    @Query("SELECT sc FROM SystemConfig sc WHERE sc.category = 'LOAN' AND sc.isActive = true " +
           "ORDER BY sc.configKey")
    List<SystemConfig> findLoanConfigs();

    /**
     * Нууцлалын тохиргоонууд
     */
    @Query("SELECT sc FROM SystemConfig sc WHERE sc.category = 'SECURITY' AND sc.isActive = true " +
           "ORDER BY sc.configKey")
    List<SystemConfig> findSecurityConfigs();

    /**
     * И-мэйлийн тохиргоонууд
     */
    @Query("SELECT sc FROM SystemConfig sc WHERE sc.category = 'EMAIL' AND sc.isActive = true " +
           "ORDER BY sc.configKey")
    List<SystemConfig> findEmailConfigs();

    /**
     * Файлын тохиргоонууд
     */
    @Query("SELECT sc FROM SystemConfig sc WHERE sc.category = 'FILE' AND sc.isActive = true " +
           "ORDER BY sc.configKey")
    List<SystemConfig> findFileConfigs();

    // Configuration validation
    /**
     * Шаардлагатай тохиргоо дутуу эсэхийг шалгах
     */
    @Query("SELECT requiredKey FROM (" +
           "  SELECT 'SYSTEM_NAME' as requiredKey UNION ALL " +
           "  SELECT 'MAX_LOAN_AMOUNT' UNION ALL " +
           "  SELECT 'MIN_LOAN_AMOUNT' UNION ALL " +
           "  SELECT 'DEFAULT_LOAN_TERM' UNION ALL " +
           "  SELECT 'MAX_FILE_SIZE'" +
           ") req WHERE requiredKey NOT IN (" +
           "  SELECT sc.configKey FROM SystemConfig sc WHERE sc.isActive = true" +
           ")")
    List<String> findMissingRequiredConfigs();

    /**
     * Буруу төрлийн утгатай тохиргоонууд
     */
    @Query("SELECT sc FROM SystemConfig sc WHERE " +
           "(sc.valueType = 'INTEGER' AND sc.configValue NOT REGEXP '^-?[0-9]+$') OR " +
           "(sc.valueType = 'DECIMAL' AND sc.configValue NOT REGEXP '^-?[0-9]+(\\.[0-9]+)?$') OR " +
           "(sc.valueType = 'BOOLEAN' AND LOWER(sc.configValue) NOT IN ('true', 'false', '1', '0', 'yes', 'no', 'on', 'off'))")
    List<SystemConfig> findInvalidValueConfigs();

    // Statistics
    /**
     * Тохиргооны статистик
     */
    @Query("SELECT " +
           "COUNT(sc) as totalConfigs, " +
           "COUNT(CASE WHEN sc.isActive = true THEN 1 END) as activeConfigs, " +
           "COUNT(CASE WHEN sc.isRuntimeEditable = true THEN 1 END) as editableConfigs, " +
           "COUNT(DISTINCT sc.category) as categories, " +
           "COUNT(DISTINCT sc.valueType) as valueTypes " +
           "FROM SystemConfig sc")
    Object[] getConfigStatistics();

    /**
     * Категориор тоолох
     */
    @Query("SELECT sc.category, COUNT(sc) FROM SystemConfig sc " +
           "WHERE sc.category IS NOT NULL " +
           "GROUP BY sc.category ORDER BY COUNT(sc) DESC")
    List<Object[]> countByCategory();

    /**
     * Төрлөөр тоолох
     */
    @Query("SELECT sc.valueType, COUNT(sc) FROM SystemConfig sc " +
           "GROUP BY sc.valueType ORDER BY COUNT(sc) DESC")
    List<Object[]> countByValueType();

    // Bulk operations
    /**
     * Категорийн бүх тохиргоог идэвхжүүлэх/идэвхгүй болгох
     */
    @Modifying
    @Query("UPDATE SystemConfig sc SET sc.isActive = :isActive, sc.updatedBy = :updatedBy " +
           "WHERE sc.category = :category")
    int updateActiveStatusForCategory(@Param("category") String category,
                                    @Param("isActive") Boolean isActive,
                                    @Param("updatedBy") String updatedBy);

    /**
     * Тохиргооны утга өөрчлөх
     */
    @Modifying
    @Query("UPDATE SystemConfig sc SET sc.configValue = :newValue, sc.updatedBy = :updatedBy " +
           "WHERE sc.configKey = :configKey")
    int updateConfigValue(@Param("configKey") String configKey,
                        @Param("newValue") String newValue,
                        @Param("updatedBy") String updatedBy);

    /**
     * Олон тохиргооны тайлбар өөрчлөх
     */
    @Modifying
    @Query("UPDATE SystemConfig sc SET sc.description = :description, sc.updatedBy = :updatedBy " +
           "WHERE sc.id IN :configIds")
    int updateDescriptionForConfigs(@Param("configIds") List<String> configIds,
                                  @Param("description") String description,
                                  @Param("updatedBy") String updatedBy);

    // Default configurations
    /**
     * Анхдагч тохиргоонууд үүсгэх
     */
    @Query("SELECT " +
           "defaultConfig.configKey, " +
           "defaultConfig.configValue, " +
           "defaultConfig.valueType, " +
           "defaultConfig.category, " +
           "defaultConfig.description " +
           "FROM (" +
           "  SELECT 'SYSTEM_NAME' as configKey, 'Loan Origination System' as configValue, 'STRING' as valueType, 'SYSTEM' as category, 'System display name' as description UNION ALL " +
           "  SELECT 'MAX_LOAN_AMOUNT', '100000000', 'DECIMAL', 'LOAN', 'Maximum loan amount allowed' UNION ALL " +
           "  SELECT 'MIN_LOAN_AMOUNT', '100000', 'DECIMAL', 'LOAN', 'Minimum loan amount allowed' UNION ALL " +
           "  SELECT 'DEFAULT_LOAN_TERM', '12', 'INTEGER', 'LOAN', 'Default loan term in months' UNION ALL " +
           "  SELECT 'MAX_FILE_SIZE', '10485760', 'INTEGER', 'FILE', 'Maximum file size in bytes (10MB)' UNION ALL " +
           "  SELECT 'SESSION_TIMEOUT', '30', 'INTEGER', 'SECURITY', 'Session timeout in minutes' UNION ALL " +
           "  SELECT 'ENABLE_EMAIL_NOTIFICATIONS', 'true', 'BOOLEAN', 'EMAIL', 'Enable email notifications' UNION ALL " +
           "  SELECT 'AUTO_APPROVE_SMALL_LOANS', 'false', 'BOOLEAN', 'LOAN', 'Auto approve loans under threshold'" +
           ") defaultConfig " +
           "WHERE defaultConfig.configKey NOT IN (" +
           "  SELECT sc.configKey FROM SystemConfig sc" +
           ")")
    List<Object[]> findDefaultConfigsToCreate();

    // Validation methods
    /**
     * Түлхүүр үг давхцаж байгаа эсэхийг шалгах
     */
    @Query("SELECT COUNT(sc) > 0 FROM SystemConfig sc WHERE " +
           "sc.configKey = :configKey AND sc.id != :excludeId")
    boolean existsByConfigKeyAndIdNot(@Param("configKey") String configKey, @Param("excludeId") String excludeId);

    // Cache support methods
    /**
     * Кэшлэх шаардлагатай тохиргоонууд
     */
    @Query("SELECT sc FROM SystemConfig sc WHERE sc.isActive = true AND " +
           "sc.configKey IN ('SYSTEM_NAME', 'MAX_LOAN_AMOUNT', 'MIN_LOAN_AMOUNT', 'SESSION_TIMEOUT', 'MAX_FILE_SIZE')")
    List<SystemConfig> findCacheableConfigs();

    /**
     * Сүүлд өөрчлөгдсөн огноо
     */
    @Query("SELECT MAX(sc.updatedAt) FROM SystemConfig sc WHERE sc.isActive = true")
    Optional<java.time.LocalDateTime> findLastModificationDate();

    // Environment specific
    /**
     * Environment-ээр хайх
     */
    @Query("SELECT sc FROM SystemConfig sc WHERE " +
           "sc.environment = :environment OR sc.environment = 'ALL' " +
           "ORDER BY sc.category, sc.configKey")
    List<SystemConfig> findByEnvironment(@Param("environment") String environment);

    /**
     * Production тохиргоонууд
     */
    @Query("SELECT sc FROM SystemConfig sc WHERE " +
           "sc.environment IN ('PRODUCTION', 'ALL') AND sc.isActive = true")
    List<SystemConfig> findProductionConfigs();

    /**
     * Development тохиргоонууд
     */
    @Query("SELECT sc FROM SystemConfig sc WHERE " +
           "sc.environment IN ('DEVELOPMENT', 'ALL') AND sc.isActive = true")
    List<SystemConfig> findDevelopmentConfigs();

    // Dashboard configurations
    /**
     * Dashboard-ийн статистик
     */
    @Query("SELECT " +
           "COUNT(sc) as totalConfigs, " +
           "COUNT(CASE WHEN sc.isActive = true THEN 1 END) as activeConfigs, " +
           "COUNT(CASE WHEN DATE(sc.updatedAt) = CURRENT_DATE THEN 1 END) as updatedToday, " +
           "COUNT(CASE WHEN sc.isRuntimeEditable = false THEN 1 END) as readOnlyConfigs, " +
           "COUNT(DISTINCT sc.category) as categories " +
           "FROM SystemConfig sc")
    Object[] getDashboardStats();

    // Data integrity
    /**
     * Тайлбаргүй тохиргоонууд
     */
    @Query("SELECT sc FROM SystemConfig sc WHERE sc.description IS NULL OR sc.description = ''")
    List<SystemConfig> findConfigsWithoutDescription();

    /**
     * Категоригүй тохиргоонууд
     */
    @Query("SELECT sc FROM SystemConfig sc WHERE sc.category IS NULL OR sc.category = ''")
    List<SystemConfig> findConfigsWithoutCategory();

    /**
     * Хоосон утгатай тохиргоонууд
     */
    @Query("SELECT sc FROM SystemConfig sc WHERE sc.configValue IS NULL OR sc.configValue = ''")
    List<SystemConfig> findConfigsWithEmptyValue();
}