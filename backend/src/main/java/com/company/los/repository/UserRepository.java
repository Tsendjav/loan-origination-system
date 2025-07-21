package com.company.los.repository;

import com.company.los.entity.Role;
import com.company.los.entity.User;
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
 * Хэрэглэгчийн Repository
 * User Repository Interface for Spring Security
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    // Spring Security үүрэг
    /**
     * Хэрэглэгчийн нэрээр хайх (Spring Security)
     */
    Optional<User> findByUsername(String username);

    /**
     * И-мэйлээр хайх
     */
    Optional<User> findByEmail(String email);

    /**
     * Хэрэглэгчийн нэр байгаа эсэхийг шалгах
     */
    boolean existsByUsername(String username);

    /**
     * И-мэйл байгаа эсэхийг шалгах
     */
    boolean existsByEmail(String email);

    // Суурь хайлтууд
    /**
     * Ажилтны дугаараар хайх
     */
    Optional<User> findByEmployeeId(String employeeId);

    /**
     * Утасны дугаараар хайх
     */
    Optional<User> findByPhone(String phone);

    /**
     * Статусаар хайх
     */
    Page<User> findByStatus(User.UserStatus status, Pageable pageable);

    /**
     * Хэлтэсээр хайх
     */
    Page<User> findByDepartment(String department, Pageable pageable);

    /**
     * Албан тушаалаар хайх
     */
    Page<User> findByPosition(String position, Pageable pageable);

    // Дүрээр хайх
    /**
     * Дүр бүхий хэрэглэгчид
     */
    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE r = :role")
    Page<User> findByRole(@Param("role") Role role, Pageable pageable);

    /**
     * Дүрийн нэрээр хайх
     */
    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    Page<User> findByRoleName(@Param("roleName") String roleName, Pageable pageable);

    /**
     * Олон дүртэй хэрэглэгчид
     */
    @Query("SELECT u FROM User u WHERE SIZE(u.roles) > 1")
    List<User> findUsersWithMultipleRoles();

    /**
     * Дүргүй хэрэглэгчид
     */
    @Query("SELECT u FROM User u WHERE SIZE(u.roles) = 0")
    List<User> findUsersWithoutRoles();

    // Нэрээр хайх
    /**
     * Нэр овогоор хайх
     */
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<User> findByName(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Ерөнхий хайлт
     */
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(COALESCE(u.employeeId, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(COALESCE(u.phone, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<User> findBySearchTerm(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Аюулгүй байдлын хяналт
    /**
     * Идэвхтэй хэрэглэгчид
     */
    @Query("SELECT u FROM User u WHERE u.enabled = true AND u.status = 'ACTIVE'")
    Page<User> findActiveUsers(Pageable pageable);

    /**
     * Түгжээтэй хэрэглэгчид
     */
    @Query("SELECT u FROM User u WHERE u.accountNonLocked = false OR u.lockedUntil > CURRENT_TIMESTAMP")
    Page<User> findLockedUsers(Pageable pageable);

    /**
     * Нууц үг хуучирсан хэрэглэгчид
     */
    @Query("SELECT u FROM User u WHERE u.passwordChangedAt < :cutoffDate")
    List<User> findUsersWithExpiredPasswords(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Сүүлийн нэвтрэх огноогоор хайх
     */
    @Query("SELECT u FROM User u WHERE u.lastLoginAt BETWEEN :startDate AND :endDate")
    Page<User> findByLastLoginBetween(@Param("startDate") LocalDateTime startDate,
                                    @Param("endDate") LocalDateTime endDate,
                                    Pageable pageable);

    /**
     * Урт хугацаанд нэвтрээгүй хэрэглэгчид
     */
    @Query("SELECT u FROM User u WHERE u.lastLoginAt < :cutoffDate OR u.lastLoginAt IS NULL")
    List<User> findInactiveUsers(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Олон удаа буруу нууц үг оруулсан хэрэглэгчид
     */
    @Query("SELECT u FROM User u WHERE u.failedLoginAttempts >= :threshold")
    List<User> findUsersWithFailedAttempts(@Param("threshold") Integer threshold);

    // 2FA холбоотой
    /**
     * 2FA идэвхжүүлсэн хэрэглэгчид
     */
    @Query("SELECT u FROM User u WHERE u.twoFactorEnabled = true")
    Page<User> findUsersWithTwoFactorEnabled(Pageable pageable);

    /**
     * 2FA идэвхжүүлээгүй хэрэглэгчид
     */
    @Query("SELECT u FROM User u WHERE u.twoFactorEnabled = false")
    Page<User> findUsersWithoutTwoFactor(Pageable pageable);

    // Огноогоор хайх
    /**
     * Тодорхой хугацаанд бүртгүүлсэн хэрэглэгчид
     */
    @Query("SELECT u FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate")
    Page<User> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate,
                                    @Param("endDate") LocalDateTime endDate,
                                    Pageable pageable);

    /**
     * Өнөөдөр бүртгүүлсэн хэрэглэгчид
     */
    @Query("SELECT u FROM User u WHERE DATE(u.createdAt) = CURRENT_DATE")
    List<User> findTodayRegistered();

    /**
     * Энэ сард бүртгүүлсэн хэрэглэгчид
     */
    @Query("SELECT u FROM User u WHERE " +
           "YEAR(u.createdAt) = YEAR(CURRENT_DATE) AND " +
           "MONTH(u.createdAt) = MONTH(CURRENT_DATE)")
    List<User> findThisMonthRegistered();

    // Статистик
    /**
     * Статусаар тоолох
     */
    @Query("SELECT u.status, COUNT(u) FROM User u GROUP BY u.status")
    List<Object[]> countByStatus();

    /**
     * Хэлтэсээр тоолох
     */
    @Query("SELECT u.department, COUNT(u) FROM User u WHERE u.department IS NOT NULL " +
           "GROUP BY u.department ORDER BY COUNT(u) DESC")
    List<Object[]> countByDepartment();

    /**
     * Дүрээр тоолох
     */
    @Query("SELECT r.name, COUNT(u) FROM User u JOIN u.roles r " +
           "GROUP BY r.name ORDER BY COUNT(u) DESC")
    List<Object[]> countByRole();

    /**
     * Сарын хэрэглэгчийн статистик
     */
    @Query("SELECT DATE_FORMAT(u.createdAt, '%Y-%m'), COUNT(u) FROM User u " +
           "WHERE u.createdAt >= :startDate " +
           "GROUP BY DATE_FORMAT(u.createdAt, '%Y-%m') " +
           "ORDER BY DATE_FORMAT(u.createdAt, '%Y-%m')")
    List<Object[]> getMonthlyUserStats(@Param("startDate") LocalDateTime startDate);

    /**
     * Нэвтрэх үйл ажиллагааны статистик
     */
    @Query("SELECT DATE_FORMAT(u.lastLoginAt, '%Y-%m-%d'), COUNT(u) FROM User u " +
           "WHERE u.lastLoginAt BETWEEN :startDate AND :endDate " +
           "GROUP BY DATE_FORMAT(u.lastLoginAt, '%Y-%m-%d') " +
           "ORDER BY DATE_FORMAT(u.lastLoginAt, '%Y-%m-%d')")
    List<Object[]> getLoginStats(@Param("startDate") LocalDateTime startDate,
                               @Param("endDate") LocalDateTime endDate);

    // Дэвшилтэт хайлт
    /**
     * Дэвшилтэт филтертэй хайлт
     */
    @Query("SELECT u FROM User u WHERE " +
           "(:status IS NULL OR u.status = :status) AND " +
           "(:department IS NULL OR LOWER(u.department) = LOWER(:department)) AND " +
           "(:position IS NULL OR LOWER(u.position) = LOWER(:position)) AND " +
           "(:enabled IS NULL OR u.enabled = :enabled) AND " +
           "(:twoFactorEnabled IS NULL OR u.twoFactorEnabled = :twoFactorEnabled) AND " +
           "(:hasRoles IS NULL OR (SIZE(u.roles) > 0) = :hasRoles) AND " +
           "(:startDate IS NULL OR u.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR u.createdAt <= :endDate)")
    Page<User> findByAdvancedFilters(
            @Param("status") User.UserStatus status,
            @Param("department") String department,
            @Param("position") String position,
            @Param("enabled") Boolean enabled,
            @Param("twoFactorEnabled") Boolean twoFactorEnabled,
            @Param("hasRoles") Boolean hasRoles,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    // Bulk операциуд
    /**
     * Олон хэрэглэгчийн статус өөрчлөх
     */
    @Modifying
    @Query("UPDATE User u SET u.status = :newStatus, u.enabled = :enabled WHERE u.id IN :userIds")
    int updateStatusForUsers(@Param("userIds") List<UUID> userIds,
                           @Param("newStatus") User.UserStatus newStatus,
                           @Param("enabled") Boolean enabled);

    /**
     * Амжилтгүй нэвтрэх тоогоор reset хийх
     */
    @Modifying
    @Query("UPDATE User u SET u.failedLoginAttempts = 0, u.lockedUntil = NULL WHERE u.id IN :userIds")
    int resetFailedAttempts(@Param("userIds") List<UUID> userIds);

    /**
     * Нууц үг сэргээх шаардлагатай гэж тэмдэглэх
     */
    @Modifying
    @Query("UPDATE User u SET u.credentialsNonExpired = false WHERE u.id IN :userIds")
    int markPasswordExpired(@Param("userIds") List<UUID> userIds);

    // Performance хяналт
    /**
     * Хамгийн идэвхтэй хэрэглэгчид (сүүлийн нэвтрэхээр)
     */
    @Query("SELECT u FROM User u WHERE u.lastLoginAt IS NOT NULL " +
           "ORDER BY u.lastLoginAt DESC")
    Page<User> findMostActiveUsers(Pageable pageable);

    /**
     * Хамгийн идэвхгүй хэрэглэгчид
     */
    @Query("SELECT u FROM User u ORDER BY COALESCE(u.lastLoginAt, u.createdAt) ASC")
    Page<User> findLeastActiveUsers(Pageable pageable);

    // Админ функц
    /**
     * Админ эрхтэй хэрэглэгчид
     */
    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE " +
           "r.name IN ('ROLE_SYSTEM_ADMIN', 'ROLE_BUSINESS_ADMIN')")
    List<User> findAdminUsers();

    /**
     * Системийн хэрэглэгчид (дүр бүхий)
     */
    @Query("SELECT u FROM User u WHERE SIZE(u.roles) > 0")
    Page<User> findSystemUsers(Pageable pageable);

    /**
     * Энгийн хэрэглэгчид (дүргүй)
     */
    @Query("SELECT u FROM User u WHERE SIZE(u.roles) = 0")
    Page<User> findRegularUsers(Pageable pageable);

    // Эрхийн шалгалт
    /**
     * Тодорхой эрх бүхий хэрэглэгчид
     */
    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r JOIN r.permissions p " +
           "WHERE p.name = :permissionName")
    List<User> findUsersWithPermission(@Param("permissionName") String permissionName);

    /**
     * Resource дээр эрхтэй хэрэглэгчид
     */
    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r JOIN r.permissions p " +
           "WHERE p.resource = :resource AND p.action = :action")
    List<User> findUsersWithResourcePermission(@Param("resource") String resource,
                                             @Param("action") String action);

    // Dashboard статистик
    /**
     * Өнөөдрийн хэрэглэгчийн статистик
     */
    @Query("SELECT " +
           "COUNT(CASE WHEN DATE(u.createdAt) = CURRENT_DATE THEN 1 END) as todayRegistered, " +
           "COUNT(CASE WHEN u.enabled = true AND u.status = 'ACTIVE' THEN 1 END) as activeUsers, " +
           "COUNT(CASE WHEN u.accountNonLocked = false OR u.lockedUntil > CURRENT_TIMESTAMP THEN 1 END) as lockedUsers, " +
           "COUNT(CASE WHEN DATE(u.lastLoginAt) = CURRENT_DATE THEN 1 END) as todayLoggedIn " +
           "FROM User u")
    Object[] getTodayUserStats();

    /**
     * Хэрэглэгчийн идэвхжилт
     */
    @Query("SELECT " +
           "COUNT(CASE WHEN u.lastLoginAt >= :oneWeekAgo THEN 1 END) as weeklyActive, " +
           "COUNT(CASE WHEN u.lastLoginAt >= :oneMonthAgo THEN 1 END) as monthlyActive, " +
           "COUNT(u) as totalUsers " +
           "FROM User u")
    Object[] getUserActivityStats(@Param("oneWeekAgo") LocalDateTime oneWeekAgo,
                                @Param("oneMonthAgo") LocalDateTime oneMonthAgo);

    // Cleanup functions
    /**
     * Урт хугацаанд идэвхгүй хэрэглэгчид
     */
    @Query("SELECT u FROM User u WHERE " +
           "u.lastLoginAt < :sixMonthsAgo AND u.status != 'SUSPENDED'")
    List<User> findInactiveForCleanup(@Param("sixMonthsAgo") LocalDateTime sixMonthsAgo);

    /**
     * Хэзээ ч нэвтрээгүй хэрэглэгчид
     */
    @Query("SELECT u FROM User u WHERE u.lastLoginAt IS NULL AND u.createdAt < :oneMonthAgo")
    List<User> findNeverLoggedIn(@Param("oneMonthAgo") LocalDateTime oneMonthAgo);
}