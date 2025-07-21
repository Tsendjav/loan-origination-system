package com.los.repository;

import com.los.entity.Permission;
import com.los.entity.Role;
import com.los.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Дүрийн Repository
 * Role Repository Interface for RBAC
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {

    // Суурь хайлтууд
    /**
     * Нэрээр хайх
     */
    Optional<Role> findByName(String name);

    /**
     * Харагдах нэрээр хайх
     */
    Optional<Role> findByDisplayName(String displayName);

    /**
     * Нэр байгаа эсэхийг шалгах
     */
    boolean existsByName(String name);

    /**
     * Харагдах нэр байгаа эсэхийг шалгах
     */
    boolean existsByDisplayName(String displayName);

    // Төрлөөр хайх
    /**
     * Дүрийн төрлөөр хайх
     */
    Page<Role> findByRoleType(Role.RoleType roleType, Pageable pageable);

    /**
     * Түвшингээр хайх
     */
    Page<Role> findByLevel(Integer level, Pageable pageable);

    /**
     * Түвшингийн хязгаараар хайх
     */
    @Query("SELECT r FROM Role r WHERE r.level BETWEEN :minLevel AND :maxLevel")
    Page<Role> findByLevelBetween(@Param("minLevel") Integer minLevel,
                                @Param("maxLevel") Integer maxLevel,
                                Pageable pageable);

    // Системийн дүр
    /**
     * Системийн дүрүүд
     */
    @Query("SELECT r FROM Role r WHERE r.isSystemRole = true")
    List<Role> findSystemRoles();

    /**
     * Системийн бус дүрүүд
     */
    @Query("SELECT r FROM Role r WHERE r.isSystemRole = false")
    Page<Role> findNonSystemRoles(Pageable pageable);

    /**
     * Хувьлах боломжтой дүрүүд
     */
    @Query("SELECT r FROM Role r WHERE r.canBeAssigned = true")
    Page<Role> findAssignableRoles(Pageable pageable);

    // Эрхтэй холбоотой
    /**
     * Тодорхой эрх бүхий дүрүүд
     */
    @Query("SELECT DISTINCT r FROM Role r JOIN r.permissions p WHERE p = :permission")
    List<Role> findRolesWithPermission(@Param("permission") Permission permission);

    /**
     * Эрхийн нэрээр дүр хайх
     */
    @Query("SELECT DISTINCT r FROM Role r JOIN r.permissions p WHERE p.name = :permissionName")
    List<Role> findRolesWithPermissionName(@Param("permissionName") String permissionName);

    /**
     * Олон эрх бүхий дүрүүд
     */
    @Query("SELECT r FROM Role r WHERE SIZE(r.permissions) > :permissionCount")
    List<Role> findRolesWithManyPermissions(@Param("permissionCount") int permissionCount);

    /**
     * Эрхгүй дүрүүд
     */
    @Query("SELECT r FROM Role r WHERE SIZE(r.permissions) = 0")
    List<Role> findRolesWithoutPermissions();

    // Хэрэглэгчтэй холбоотой
    /**
     * Хэрэглэгчтэй дүрүүд
     */
    @Query("SELECT r FROM Role r WHERE SIZE(r.users) > 0")
    Page<Role> findRolesWithUsers(Pageable pageable);

    /**
     * Хэрэглэгчгүй дүрүүд
     */
    @Query("SELECT r FROM Role r WHERE SIZE(r.users) = 0")
    List<Role> findRolesWithoutUsers();

    /**
     * Тодорхой хэрэглэгчийн дүрүүд
     */
    @Query("SELECT r FROM Role r JOIN r.users u WHERE u = :user")
    List<Role> findRolesByUser(@Param("user") User user);

    /**
     * Хэрэглэгчийн ID-гаар дүр хайх
     */
    @Query("SELECT r FROM Role r JOIN r.users u WHERE u.id = :userId")
    List<Role> findRolesByUserId(@Param("userId") UUID userId);

    // Нэрээр хайх
    /**
     * Харагдах нэрээр хайх (орчуулга дэмжих)
     */
    @Query("SELECT r FROM Role r WHERE " +
           "LOWER(r.displayName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(COALESCE(r.displayNameMn, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Role> findByDisplayNameContaining(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Ерөнхий хайлт
     */
    @Query("SELECT r FROM Role r WHERE " +
           "LOWER(r.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(r.displayName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(COALESCE(r.displayNameMn, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(COALESCE(r.description, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Role> findBySearchTerm(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Hierarchy холбоотой
    /**
     * Илүү өндөр түвшний дүрүүд
     */
    @Query("SELECT r FROM Role r WHERE r.level > :level ORDER BY r.level ASC")
    List<Role> findHigherLevelRoles(@Param("level") Integer level);

    /**
     * Илүү доод түвшний дүрүүд
     */
    @Query("SELECT r FROM Role r WHERE r.level < :level ORDER BY r.level DESC")
    List<Role> findLowerLevelRoles(@Param("level") Integer level);

    /**
     * Ижил түвшний дүрүүд
     */
    @Query("SELECT r FROM Role r WHERE r.level = :level AND r.id != :excludeId")
    List<Role> findSameLevelRoles(@Param("level") Integer level, @Param("excludeId") UUID excludeId);

    // Тэргүүлэх эрэмбэ
    /**
     * Тэргүүлэх эрэмбээр эрэмбэлсэн дүрүүд
     */
    @Query("SELECT r FROM Role r ORDER BY r.priority DESC, r.level DESC, r.name ASC")
    List<Role> findAllOrderedByPriority();

    /**
     * Өндөр тэргүүлэх эрэмбэтэй дүрүүд
     */
    @Query("SELECT r FROM Role r WHERE r.priority >= :highPriorityThreshold ORDER BY r.priority DESC")
    List<Role> findHighPriorityRoles(@Param("highPriorityThreshold") Integer highPriorityThreshold);

    // Статистик
    /**
     * Дүрийн төрлөөр тоолох
     */
    @Query("SELECT r.roleType, COUNT(r) FROM Role r GROUP BY r.roleType")
    List<Object[]> countByRoleType();

    /**
     * Түвшингээр тоолох
     */
    @Query("SELECT r.level, COUNT(r) FROM Role r GROUP BY r.level ORDER BY r.level")
    List<Object[]> countByLevel();

    /**
     * Хэрэглэгчдийн тоогоор статистик
     */
    @Query("SELECT r.name, r.displayName, SIZE(r.users) as userCount FROM Role r " +
           "ORDER BY SIZE(r.users) DESC")
    List<Object[]> getRoleUserStats();

    /**
     * Эрхийн тоогоор статистик
     */
    @Query("SELECT r.name, r.displayName, SIZE(r.permissions) as permissionCount FROM Role r " +
           "ORDER BY SIZE(r.permissions) DESC")
    List<Object[]> getRolePermissionStats();

    // Дэвшилтэт хайлт
    /**
     * Дэвшилтэт филтертэй хайлт
     */
    @Query("SELECT r FROM Role r WHERE " +
           "(:roleType IS NULL OR r.roleType = :roleType) AND " +
           "(:minLevel IS NULL OR r.level >= :minLevel) AND " +
           "(:maxLevel IS NULL OR r.level <= :maxLevel) AND " +
           "(:isSystemRole IS NULL OR r.isSystemRole = :isSystemRole) AND " +
           "(:canBeAssigned IS NULL OR r.canBeAssigned = :canBeAssigned) AND " +
           "(:hasUsers IS NULL OR (SIZE(r.users) > 0) = :hasUsers) AND " +
           "(:hasPermissions IS NULL OR (SIZE(r.permissions) > 0) = :hasPermissions) AND " +
           "(:minPriority IS NULL OR r.priority >= :minPriority)")
    Page<Role> findByAdvancedFilters(
            @Param("roleType") Role.RoleType roleType,
            @Param("minLevel") Integer minLevel,
            @Param("maxLevel") Integer maxLevel,
            @Param("isSystemRole") Boolean isSystemRole,
            @Param("canBeAssigned") Boolean canBeAssigned,
            @Param("hasUsers") Boolean hasUsers,
            @Param("hasPermissions") Boolean hasPermissions,
            @Param("minPriority") Integer minPriority,
            Pageable pageable);

    // Эрх удирдлага
    /**
     * Дүрт эрх нэмэх
     */
    @Modifying
    @Query("INSERT INTO role_permissions (role_id, permission_id) VALUES (:roleId, :permissionId)")
    void addPermissionToRole(@Param("roleId") UUID roleId, @Param("permissionId") UUID permissionId);

    /**
     * Дүрээс эрх хасах
     */
    @Modifying
    @Query("DELETE FROM role_permissions WHERE role_id = :roleId AND permission_id = :permissionId")
    void removePermissionFromRole(@Param("roleId") UUID roleId, @Param("permissionId") UUID permissionId);

    /**
     * Дүрийн бүх эрх хасах
     */
    @Modifying
    @Query("DELETE FROM role_permissions WHERE role_id = :roleId")
    void removeAllPermissionsFromRole(@Param("roleId") UUID roleId);

    // Хэрэглэгч удирдлага
    /**
     * Дүрт хэрэглэгч нэмэх
     */
    @Modifying
    @Query("INSERT INTO user_roles (user_id, role_id) VALUES (:userId, :roleId)")
    void addUserToRole(@Param("userId") UUID userId, @Param("roleId") UUID roleId);

    /**
     * Дүрээс хэрэглэгч хасах
     */
    @Modifying
    @Query("DELETE FROM user_roles WHERE user_id = :userId AND role_id = :roleId")
    void removeUserFromRole(@Param("userId") UUID userId, @Param("roleId") UUID roleId);

    /**
     * Дүрийн бүх хэрэглэгч хасах
     */
    @Modifying
    @Query("DELETE FROM user_roles WHERE role_id = :roleId")
    void removeAllUsersFromRole(@Param("roleId") UUID roleId);

    // Validation
    /**
     * Дүр хувьлах боломжтой эсэхийг шалгах
     */
    @Query("SELECT CASE WHEN (r.maxAssignments = -1 OR SIZE(r.users) < r.maxAssignments) " +
           "AND r.canBeAssigned = true THEN true ELSE false END " +
           "FROM Role r WHERE r.id = :roleId")
    Boolean canAssignRole(@Param("roleId") UUID roleId);

    /**
     * Хэрэглэгч дүртэй эсэхийг шалгах
     */
    @Query("SELECT CASE WHEN COUNT(ur) > 0 THEN true ELSE false END " +
           "FROM User u JOIN u.roles ur WHERE u.id = :userId AND ur.id = :roleId")
    Boolean userHasRole(@Param("userId") UUID userId, @Param("roleId") UUID roleId);

    // Cleanup
    /**
     * Ашиглагдаагүй дүрүүд
     */
    @Query("SELECT r FROM Role r WHERE SIZE(r.users) = 0 AND r.isSystemRole = false")
    List<Role> findUnusedRoles();

    /**
     * Эрхгүй дүрүүд (системийн биш)
     */
    @Query("SELECT r FROM Role r WHERE SIZE(r.permissions) = 0 AND r.isSystemRole = false")
    List<Role> findEmptyRoles();

    // Бизнес логик
    /**
     * Админ дүрүүд
     */
    @Query("SELECT r FROM Role r WHERE r.roleType IN ('SYSTEM_ADMIN', 'BUSINESS_ADMIN')")
    List<Role> findAdminRoles();

    /**
     * Зээлийн дүрүүд
     */
    @Query("SELECT r FROM Role r WHERE r.roleType IN ('LOAN_OFFICER', 'UNDERWRITER', 'CREDIT_ANALYST')")
    List<Role> findLoanRoles();

    /**
     * Менежерийн дүрүүд
     */
    @Query("SELECT r FROM Role r WHERE r.roleType IN ('MANAGER', 'BRANCH_MANAGER')")
    List<Role> findManagerRoles();

    // Dashboard
    /**
     * Дүрийн dashboard статистик
     */
    @Query("SELECT " +
           "COUNT(r) as totalRoles, " +
           "COUNT(CASE WHEN r.isSystemRole = true THEN 1 END) as systemRoles, " +
           "COUNT(CASE WHEN r.canBeAssigned = true THEN 1 END) as assignableRoles, " +
           "COUNT(CASE WHEN SIZE(r.users) = 0 THEN 1 END) as emptyRoles " +
           "FROM Role r")
    Object[] getRoleDashboardStats();

    /**
     * Дүрийн өгөгдлийн integrity шалгах
     */
    @Query("SELECT r FROM Role r WHERE " +
           "r.name IS NULL OR r.displayName IS NULL OR r.roleType IS NULL OR r.level IS NULL")
    List<Role> findRolesWithDataIssues();

    // Role hierarchy validation
    /**
     * Дүрийн hierarchy consistency шалгах
     */
    @Query("SELECT r1.name, r1.level, r2.name, r2.level FROM Role r1, Role r2 " +
           "WHERE r1.roleType = r2.roleType AND r1.level = r2.level AND r1.id != r2.id")
    List<Object[]> findLevelConflicts();

    /**
     * Хэрэглэгчийн хамгийн өндөр дүр
     */
    @Query("SELECT r FROM Role r JOIN r.users u WHERE u.id = :userId " +
           "ORDER BY r.level DESC, r.priority DESC LIMIT 1")
    Optional<Role> findHighestRoleForUser(@Param("userId") UUID userId);
}