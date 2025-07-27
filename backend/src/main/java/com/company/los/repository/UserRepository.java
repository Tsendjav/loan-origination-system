package com.company.los.repository;

import com.company.los.entity.Role;
import com.company.los.entity.User;
import com.company.los.entity.Permission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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
     * Хэлтэсээр хайх
     */
    Page<User> findByDepartment(String department, Pageable pageable);

    /**
     * Албан тушаалаар хайх
     */
    Page<User> findByPosition(String position, Pageable pageable);

    /**
     * Менежерээр хайх
     */
    List<User> findByManager(User manager);

    /**
     * Менежерийн ID-гаар хайх (UUID төрөл ашиглана)
     */
    @Query("SELECT u FROM User u WHERE u.manager.id = :managerId")
    List<User> findByManagerId(@Param("managerId") UUID managerId);

    /**
     * Статусаар хайх
     */
    Page<User> findByStatus(User.UserStatus status, Pageable pageable);

    // Account status
    /**
     * Идэвхтэй хэрэглэгчид
     */
    @Query("SELECT u FROM User u WHERE u.isActive = true AND u.isDeleted = false")
    Page<User> findActiveUsers(Pageable pageable);

    /**
     * Түгжээтэй хэрэглэгчид
     */
    @Query("SELECT u FROM User u WHERE u.isLocked = true")
    Page<User> findLockedUsers(Pageable pageable);

    /**
     * И-мэйл баталгаажуулсан хэрэглэгчид
     */
    @Query("SELECT u FROM User u WHERE u.isEmailVerified = true")
    Page<User> findEmailVerifiedUsers(Pageable pageable);

    /**
     * И-мэйл баталгаажуулаагүй хэрэглэгчид
     */
    @Query("SELECT u FROM User u WHERE u.isEmailVerified = false")
    Page<User> findEmailUnverifiedUsers(Pageable pageable);

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

    /**
     * Тодорхой эрх бүхий хэрэглэгчид
     */
    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r JOIN r.permissions p WHERE p.name = :permissionName")
    List<User> findUsersWithPermission(@Param("permissionName") String permissionName);

    // Hierarchy
    /**
     * Менежерүүд (доор нь хүн ажилладаг)
     */
    @Query("SELECT DISTINCT u FROM User u WHERE SIZE(u.subordinates) > 0")
    Page<User> findManagers(Pageable pageable);

    /**
     * Менежерийн доор нь ажилладаг хүмүүс (UUID төрөл ашиглана)
     */
    @Query("SELECT u FROM User u WHERE u.manager.id = :managerId")
    List<User> findSubordinates(@Param("managerId") UUID managerId);

    /**
     * Менежергүй хэрэглэгчид
     */
    @Query("SELECT u FROM User u WHERE u.manager IS NULL")
    Page<User> findUsersWithoutManager(Pageable pageable);

    // Нэрээр хайх
    /**
     * Нэр овогоор хайх
     */
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<User> findByName(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Бүтэн нэрээр хайх
     */
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :fullName, '%'))")
    Page<User> findByFullName(@Param("fullName") String fullName, Pageable pageable);

    /**
     * Ерөнхий хайлт
     */
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(COALESCE(u.employeeId, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(COALESCE(u.phone, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(COALESCE(u.department, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(COALESCE(u.position, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<User> findBySearchTerm(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Аюулгүй байдлын хяналт
    /**
     * Нууц үг хуучирсан хэрэглэгчид
     */
    @Query("SELECT u FROM User u WHERE u.passwordExpiresAt < CURRENT_TIMESTAMP")
    List<User> findUsersWithExpiredPasswords();

    /**
     * Удахгүй нууц үг хуучирах хэрэглэгчид
     */
    @Query("SELECT u FROM User u WHERE u.passwordExpiresAt BETWEEN CURRENT_TIMESTAMP AND :futureDate")
    List<User> findUsersWithExpiringSoonPasswords(@Param("futureDate") LocalDateTime futureDate);

    /**
     * Нууц үг хуучирсан хэрэглэгчид (cutoff date-аар)
     */
    @Query("SELECT u FROM User u WHERE u.passwordChangedAt < :cutoffDate OR u.passwordChangedAt IS NULL")
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

    /**
     * Онлайн байгаа хэрэглэгчид (сүүлийн 30 минутад нэвтэрсэн)
     */
    @Query("SELECT u FROM User u WHERE u.lastLoginAt >= :recentTime")
    List<User> findOnlineUsers(@Param("recentTime") LocalDateTime recentTime);

    // Two-Factor Authentication
    /**
     * 2FA идэвхжүүлсэн хэрэглэгчид
     */
    @Query("SELECT u FROM User u WHERE u.twoFactorEnabled = true")
    Page<User> findUsersWithTwoFactorEnabled(Pageable pageable);

    /**
     * 2FA идэвхжүүлээгүй хэрэглэгчид
     */
    @Query("SELECT u FROM User u WHERE u.twoFactorEnabled = false OR u.twoFactorEnabled IS NULL")
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
    @Query("SELECT u FROM User u WHERE FUNCTION('DATE', u.createdAt) = CURRENT_DATE")
    List<User> findTodayRegistered();

    /**
     * Энэ сард бүртгүүлсэн хэрэглэгчид
     */
    @Query("SELECT u FROM User u WHERE " +
           "FUNCTION('YEAR', u.createdAt) = FUNCTION('YEAR', CURRENT_DATE) AND " +
           "FUNCTION('MONTH', u.createdAt) = FUNCTION('MONTH', CURRENT_DATE)")
    List<User> findThisMonthRegistered();

    /**
     * Шинэ хэрэглэгчид (сүүлийн 7 хоногт)
     */
    @Query("SELECT u FROM User u WHERE u.createdAt >= :oneWeekAgo ORDER BY u.createdAt DESC")
    List<User> findNewUsers(@Param("oneWeekAgo") LocalDateTime oneWeekAgo);

    // Статистик
    /**
     * Хэлтэсээр тоолох
     */
    @Query("SELECT u.department, COUNT(u) FROM User u WHERE u.department IS NOT NULL " +
           "GROUP BY u.department ORDER BY COUNT(u) DESC")
    List<Object[]> countByDepartment();

    /**
     * Албан тушаалаар тоолох
     */
    @Query("SELECT u.position, COUNT(u) FROM User u WHERE u.position IS NOT NULL " +
           "GROUP BY u.position ORDER BY COUNT(u) DESC")
    List<Object[]> countByPosition();

    /**
     * Дүрээр тоолох
     */
    @Query("SELECT r.name, COUNT(u) FROM User u JOIN u.roles r " +
           "GROUP BY r.name ORDER BY COUNT(u) DESC")
    List<Object[]> countByRole();

    /**
     * Статусаар тоолох
     */
    @Query("SELECT u.status, COUNT(u) FROM User u " +
           "GROUP BY u.status ORDER BY COUNT(u) DESC")
    List<Object[]> countByStatus();

    /**
     * Сарын хэрэглэгчийн статистик
     */
    @Query("SELECT FUNCTION('FORMATDATETIME', u.createdAt, 'yyyy-MM'), COUNT(u) FROM User u " +
           "WHERE u.createdAt >= :startDate " +
           "GROUP BY FUNCTION('FORMATDATETIME', u.createdAt, 'yyyy-MM') " +
           "ORDER BY FUNCTION('FORMATDATETIME', u.createdAt, 'yyyy-MM')")
    List<Object[]> getMonthlyUserStats(@Param("startDate") LocalDateTime startDate);

    /**
     * Нэвтрэх үйл ажиллагааны статистик
     */
    @Query("SELECT FUNCTION('FORMATDATETIME', u.lastLoginAt, 'yyyy-MM-dd'), COUNT(u) FROM User u " +
           "WHERE u.lastLoginAt BETWEEN :startDate AND :endDate " +
           "GROUP BY FUNCTION('FORMATDATETIME', u.lastLoginAt, 'yyyy-MM-dd') " +
           "ORDER BY FUNCTION('FORMATDATETIME', u.lastLoginAt, 'yyyy-MM-dd')")
    List<Object[]> getLoginStats(@Param("startDate") LocalDateTime startDate,
                               @Param("endDate") LocalDateTime endDate);

    /**
     * Хэрэглэгчийн идэвхжилтийн статистик
     */
    @Query("SELECT " +
           "COUNT(CASE WHEN u.lastLoginAt >= :oneWeekAgo THEN 1 END) as weeklyActive, " +
           "COUNT(CASE WHEN u.lastLoginAt >= :oneMonthAgo THEN 1 END) as monthlyActive, " +
           "COUNT(CASE WHEN u.lastLoginAt IS NULL THEN 1 END) as neverLoggedIn, " +
           "COUNT(u) as totalUsers " +
           "FROM User u")
    Object[] getUserActivityStats(@Param("oneWeekAgo") LocalDateTime oneWeekAgo,
                                @Param("oneMonthAgo") LocalDateTime oneMonthAgo);

    // Дэвшилтэт хайлт
    /**
     * Дэвшилтэт филтертэй хайлт
     */
    @Query("SELECT u FROM User u WHERE " +
           "(:status IS NULL OR u.status = :status) AND " +
           "(:department IS NULL OR LOWER(u.department) = LOWER(:department)) AND " +
           "(:position IS NULL OR LOWER(u.position) = LOWER(:position)) AND " +
           "(:managerId IS NULL OR u.manager.id = :managerId) AND " +
           "(:enabled IS NULL OR u.isActive = :enabled) AND " +
           "(:twoFactorEnabled IS NULL OR u.twoFactorEnabled = :twoFactorEnabled) AND " +
           "(:hasRoles IS NULL OR (:hasRoles = TRUE AND SIZE(u.roles) > 0) OR (:hasRoles = FALSE AND SIZE(u.roles) = 0)) AND " +
           "(:hasSubordinates IS NULL OR (:hasSubordinates = TRUE AND SIZE(u.subordinates) > 0) OR (:hasSubordinates = FALSE AND SIZE(u.subordinates) = 0)) AND " +
           "(:startDate IS NULL OR u.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR u.createdAt <= :endDate)")
    Page<User> findByAdvancedFilters(
            @Param("status") User.UserStatus status,
            @Param("department") String department,
            @Param("position") String position,
            @Param("managerId") UUID managerId,
            @Param("enabled") Boolean enabled,
            @Param("twoFactorEnabled") Boolean twoFactorEnabled,
            @Param("hasRoles") Boolean hasRoles,
            @Param("hasSubordinates") Boolean hasSubordinates,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    // Bulk операциуд
    /**
     * Олон хэрэглэгчийн статус өөрчлөх
     */
    @Modifying
    @Query("UPDATE User u SET u.status = :newStatus, u.isActive = :enabled, u.updatedBy = :updatedBy, u.updatedAt = CURRENT_TIMESTAMP WHERE u.id IN :userIds")
    int updateStatusForUsers(@Param("userIds") List<UUID> userIds,
                           @Param("newStatus") User.UserStatus newStatus,
                           @Param("enabled") Boolean enabled,
                           @Param("updatedBy") String updatedBy);

    /**
     * Амжилтгүй нэвтрэх тоогоор reset хийх
     */
    @Modifying
    @Query("UPDATE User u SET u.failedLoginAttempts = 0, u.updatedAt = CURRENT_TIMESTAMP WHERE u.id IN :userIds")
    int resetFailedAttempts(@Param("userIds") List<UUID> userIds);

    /**
     * Хэрэглэгчдийн нууц үг сэргээх шаардлагатай гэж тэмдэглэх
     */
    @Modifying
    @Query("UPDATE User u SET u.passwordExpiresAt = CURRENT_TIMESTAMP, u.updatedAt = CURRENT_TIMESTAMP WHERE u.id IN :userIds")
    int markPasswordExpired(@Param("userIds") List<UUID> userIds);

    /**
     * Олон хэрэглэгчийг түгжих
     */
    @Modifying
    @Query("UPDATE User u SET u.isLocked = true, u.status = 'LOCKED', u.updatedBy = :updatedBy, u.updatedAt = CURRENT_TIMESTAMP WHERE u.id IN :userIds")
    int lockUsers(@Param("userIds") List<UUID> userIds, @Param("updatedBy") String updatedBy);

    /**
     * Олон хэрэглэгчийг түгжээг тайлах
     */
    @Modifying
    @Query("UPDATE User u SET u.isLocked = false, u.status = 'ACTIVE', u.failedLoginAttempts = 0, u.updatedBy = :updatedBy, u.updatedAt = CURRENT_TIMESTAMP WHERE u.id IN :userIds")
    int unlockUsers(@Param("userIds") List<UUID> userIds, @Param("updatedBy") String updatedBy);

    /**
     * Хэлтэс өөрчлөх
     */
    @Modifying
    @Query("UPDATE User u SET u.department = :newDepartment, u.updatedBy = :updatedBy, u.updatedAt = CURRENT_TIMESTAMP WHERE u.id IN :userIds")
    int updateDepartmentForUsers(@Param("userIds") List<UUID> userIds,
                               @Param("newDepartment") String newDepartment,
                               @Param("updatedBy") String updatedBy);

    // Performance хяналт
    /**
     * Менежерийн шулуун доор нь ажилладаг хүмүүс
     */
    @Query("SELECT u FROM User u WHERE u.manager.id = :managerId " +
           "ORDER BY u.position, u.lastName, u.firstName")
    List<User> findDirectReports(@Param("managerId") UUID managerId);

    // Security audit
    /**
     * Эрсдэлтэй хэрэглэгчид
     */
    @Query("SELECT DISTINCT u FROM User u " +
           "JOIN u.roles r " +
           "JOIN r.permissions p " +
           "WHERE p.action IN ('DELETE', 'APPROVE', 'AUDIT') AND " +
           "p.resource IN ('SYSTEM', 'USER', 'ROLE')")
    List<User> findHighRiskUsers();

    /**
     * Олон эрхтэй хэрэглэгчид
     */
    @Query("SELECT u, COUNT(DISTINCT p) as permissionCount FROM User u " +
           "JOIN u.roles r " +
           "JOIN r.permissions p " +
           "GROUP BY u " +
           "HAVING COUNT(DISTINCT p) > :permissionThreshold " +
           "ORDER BY permissionCount DESC")
    List<Object[]> findUsersWithTooManyPermissions(@Param("permissionThreshold") int permissionThreshold);

    // Data quality
    /**
     * Дутуу мэдээлэлтэй хэрэглэгчид
     */
    @Query("SELECT u FROM User u WHERE " +
           "u.firstName IS NULL OR u.firstName = '' OR " +
           "u.lastName IS NULL OR u.lastName = '' OR " +
           "u.email IS NULL OR u.email = '' OR " +
           "u.department IS NULL OR u.department = ''")
    List<User> findUsersWithIncompleteProfiles();

    /**
     * Ажлын мэдээлэл дутуу хэрэглэгчид
     */
    @Query("SELECT u FROM User u WHERE " +
           "u.employeeId IS NULL OR u.position IS NULL OR u.department IS NULL")
    List<User> findUsersWithIncompleteWorkInfo();

    /**
     * Хэрэглэгчийн нэр давхцаж байгаа эсэхийг шалгах (ID-гаар хасах)
     */
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE " +
            "LOWER(u.username) = LOWER(:username) AND u.id != :excludeId")
    boolean existsByUsernameIgnoreCaseAndIdNot(@Param("username") String username, @Param("excludeId") UUID excludeId);

    /**
     * И-мэйл давхцаж байгаа эсэхийг шалгах (ID-гаар хасах)
     */
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE " +
            "LOWER(u.email) = LOWER(:email) AND u.id != :excludeId")
    boolean existsByEmailIgnoreCaseAndIdNot(@Param("email") String email, @Param("excludeId") UUID excludeId);

    // Permission and authorization methods
    /**
     * Resource дээр эрхтэй хэрэглэгчид
     */
    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r JOIN r.permissions p " +
           "WHERE p.resource = :resource AND p.action = :action")
    List<User> findUsersWithResourcePermission(@Param("resource") String resource,
                                             @Param("action") String action);

    /**
     * Хэрэглэгч тодорхой ресурс болон үйлдэлд эрх эзэмшдэг эсэхийг шалгах
     */
    @Query("SELECT COUNT(u) > 0 FROM User u " +
           "JOIN u.roles r " +
           "JOIN r.permissions p " +
           "WHERE u.id = :userId " +
           "AND p.resource = :resource " +
           "AND p.action = :action " +
           "AND u.isActive = true " +
           "AND u.isLocked = false")
    boolean userHasResourcePermission(@Param("userId") UUID userId,
                                     @Param("resource") String resource,
                                     @Param("action") String action);

    /**
     * Хэрэглэгчийн бүх эрх
     */
    @Query("SELECT DISTINCT p FROM Permission p " +
           "JOIN p.roles r " +
           "JOIN r.users u " +
           "WHERE u.id = :userId")
    List<Permission> findUserPermissions(@Param("userId") UUID userId);

    // Dashboard статистик
    /**
     * Өнөөдрийн хэрэглэгчийн статистик
     */
    @Query("SELECT new map(" +
           "COUNT(CASE WHEN FUNCTION('DATE', u.createdAt) = CURRENT_DATE THEN 1 END) as todayRegistered, " +
           "COUNT(CASE WHEN u.isActive = true AND u.isLocked = false THEN 1 END) as activeUsers, " +
           "COUNT(CASE WHEN u.isLocked = true THEN 1 END) as lockedUsers, " +
           "COUNT(CASE WHEN FUNCTION('DATE', u.lastLoginAt) = CURRENT_DATE THEN 1 END) as todayLoggedIn, " +
           "COUNT(CASE WHEN u.isEmailVerified = false THEN 1 END) as unverifiedEmails, " +
           "COUNT(CASE WHEN u.failedLoginAttempts >= 3 THEN 1 END) as withFailedAttempts) " +
           "FROM User u")
    Map<String, Object> getTodayUserStats();

    /**
     * Хэрэглэгчийн үндсэн статистик
     */
    @Query("SELECT " +
           "COUNT(u) as totalUsers, " +
           "COUNT(CASE WHEN u.isActive = true THEN 1 END) as activeUsers, " +
           "COUNT(CASE WHEN SIZE(u.roles) > 0 THEN 1 END) as usersWithRoles, " +
           "COUNT(CASE WHEN SIZE(u.subordinates) > 0 THEN 1 END) as managers, " +
           "COUNT(DISTINCT u.department) as departments " +
           "FROM User u")
    Object[] getUserOverviewStats();

    // Cleanup functions
    /**
     * Урт хугацаанд идэвхгүй хэрэглэгчид
     */
    @Query("SELECT u FROM User u WHERE " +
           "u.lastLoginAt < :sixMonthsAgo AND u.isActive = true")
    List<User> findInactiveForCleanup(@Param("sixMonthsAgo") LocalDateTime sixMonthsAgo);

    /**
     * Хэзээ ч нэвтрээгүй хэрэглэгчид
     */
    @Query("SELECT u FROM User u WHERE u.lastLoginAt IS NULL AND u.createdAt < :oneMonthAgo")
    List<User> findNeverLoggedIn(@Param("oneMonthAgo") LocalDateTime oneMonthAgo);

    /**
     * И-мэйл баталгаажуулаагүй урт хугацаатай хэрэглэгчид
     */
    @Query("SELECT u FROM User u WHERE u.isEmailVerified = false AND u.createdAt < :oneWeekAgo")
    List<User> findUnverifiedEmailsOld(@Param("oneWeekAgo") LocalDateTime oneWeekAgo);

    // Organization structure
    /**
     * Хэлтэсийн бүх хэрэглэгч
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.department) = LOWER(:department) " +
           "ORDER BY u.position, u.lastName, u.firstName")
    List<User> findByDepartmentOrdered(@Param("department") String department);

    /**
     * Зохион байгуулалтын бүтэц
     */
    @Query("SELECT u FROM User u WHERE u.manager IS NULL ORDER BY u.department, u.position")
    List<User> findTopLevelManagers();

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
           "r.name LIKE '%ADMIN%'")
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

    /**
     * Менежер эрхтэй хэрэглэгчид
     */
    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE " +
           "r.name LIKE '%MANAGER%' OR SIZE(u.subordinates) > 0")
    List<User> findManagerUsers();
}