package com.company.los.repository;

import com.company.los.entity.Permission;
import com.company.los.entity.Role;
import com.company.los.entity.User;
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

    // Эрэмбээр хайх (priority ашиглах)
    /**
     * Эрэмбээр хайх
     */
    Page<Role> findByPriority(Integer priority, Pageable pageable);

    /**
     * Эрэмбийн хязгаараар хайх
     */
    @Query("SELECT r FROM Role r WHERE r.priority BETWEEN :minPriority AND :maxPriority")
    Page<Role> findByPriorityBetween(@Param("minPriority") Integer minPriority,
                                   @Param("maxPriority") Integer maxPriority,
                                   Pageable pageable);

    /**
     * Өндөр эрэмбийн дүрүүд
     */
    @Query("SELECT r FROM Role r WHERE r.priority >= :highPriorityThreshold ORDER BY r.priority DESC")
    List<Role> findHighPriorityRoles(@Param("highPriorityThreshold") Integer highPriorityThreshold);

    // Системийн дүр
    /**
     * Системийн дүрүүд
     */
    @Query("SELECT r FROM Role r WHERE r.type = 'SYSTEM'")
    List<Role> findSystemRoles();

    /**
     * Системийн бус дүрүүд
     */
    @Query("SELECT r FROM Role r WHERE r.type != 'SYSTEM'")
    Page<Role> findNonSystemRoles(Pageable pageable);

    /**
     * Анхдагч дүрүүд
     */
    @Query("SELECT r FROM Role r WHERE r.isDefault = true")
    List<Role> findDefaultRoles();

    /**
     * Анхдагч бус дүрүүд
     */
    @Query("SELECT r FROM Role r WHERE r.isDefault = false")
    Page<Role> findNonDefaultRoles(Pageable pageable);

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

    /**
     * Тодорхой ресурсийн эрх бүхий дүрүүд
     */
    @Query("SELECT DISTINCT r FROM Role r JOIN r.permissions p WHERE p.resource = :resource")
    List<Role> findRolesWithResourcePermission(@Param("resource") String resource);

    /**
     * Тодорхой үйлдлийн эрх бүхий дүрүүд
     */
    @Query("SELECT DISTINCT r FROM Role r JOIN r.permissions p WHERE p.action = :action")
    List<Role> findRolesWithActionPermission(@Param("action") String action);

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

    /**
     * Олон хэрэглэгчтэй дүрүүд
     */
    @Query("SELECT r FROM Role r WHERE SIZE(r.users) > :userCount")
    List<Role> findRolesWithManyUsers(@Param("userCount") int userCount);

    // Нэрээр хайх
    /**
     * Харагдах нэрээр хайх (орчуулга дэмжих)
     */
    @Query("SELECT r FROM Role r WHERE " +
           "LOWER(r.displayName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Role> findByDisplayNameContaining(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Ерөнхий хайлт
     */
    @Query("SELECT r FROM Role r WHERE " +
           "LOWER(r.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(r.displayName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(COALESCE(r.description, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Role> findBySearchTerm(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Hierarchy холбоотой
    /**
     * Илүү өндөр эрэмбийн дүрүүд
     */
    @Query("SELECT r FROM Role r WHERE r.priority > :priority ORDER BY r.priority ASC")
    List<Role> findHigherPriorityRoles(@Param("priority") Integer priority);

    /**
     * Илүү доод эрэмбийн дүрүүд
     */
    @Query("SELECT r FROM Role r WHERE r.priority < :priority ORDER BY r.priority DESC")
    List<Role> findLowerPriorityRoles(@Param("priority") Integer priority);

    /**
     * Ижил эрэмбийн дүрүүд
     */
    @Query("SELECT r FROM Role r WHERE r.priority = :priority AND r.id != :excludeId")
    List<Role> findSamePriorityRoles(@Param("priority") Integer priority, @Param("excludeId") UUID excludeId);

    /**
     * Эрэмбээр эрэмбэлсэн дүрүүд
     */
    @Query("SELECT r FROM Role r ORDER BY r.priority DESC, r.name ASC")
    List<Role> findAllOrderedByPriority();

    // Статистик
    /**
     * Эрэмбээр тоолох
     */
    @Query("SELECT r.priority, COUNT(r) FROM Role r GROUP BY r.priority ORDER BY r.priority")
    List<Object[]> countByPriority();

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

    /**
     * Системийн болон энгийн дүрийн тоо
     */
    @Query("SELECT " +
           "COUNT(CASE WHEN r.type = 'SYSTEM' THEN 1 END) as systemRoles, " +
           "COUNT(CASE WHEN r.type != 'SYSTEM' THEN 1 END) as regularRoles, " +
           "COUNT(CASE WHEN r.isDefault = true THEN 1 END) as defaultRoles " +
           "FROM Role r")
    Object[] getRoleTypeStats();

    // Дэвшилтэт хайлт
    /**
     * Дэвшилтэт филтертэй хайлт
     */
    @Query("SELECT r FROM Role r WHERE " +
           "(:minPriority IS NULL OR r.priority >= :minPriority) AND " +
           "(:maxPriority IS NULL OR r.priority <= :maxPriority) AND " +
           "(:type IS NULL OR r.type = :type) AND " +
           "(:isDefault IS NULL OR r.isDefault = :isDefault) AND " +
           "(:hasUsers IS NULL OR (:hasUsers = TRUE AND SIZE(r.users) > 0) OR (:hasUsers = FALSE AND SIZE(r.users) = 0)) AND " +
           "(:hasPermissions IS NULL OR (:hasPermissions = TRUE AND SIZE(r.permissions) > 0) OR (:hasPermissions = FALSE AND SIZE(r.permissions) = 0)) AND " +
           "(:minUsers IS NULL OR SIZE(r.users) >= :minUsers) AND " +
           "(:maxUsers IS NULL OR SIZE(r.users) <= :maxUsers)")
    Page<Role> findByAdvancedFilters(
            @Param("minPriority") Integer minPriority,
            @Param("maxPriority") Integer maxPriority,
            @Param("type") Role.RoleType type,
            @Param("isDefault") Boolean isDefault,
            @Param("hasUsers") Boolean hasUsers,
            @Param("hasPermissions") Boolean hasPermissions,
            @Param("minUsers") Integer minUsers,
            @Param("maxUsers") Integer maxUsers,
            Pageable pageable);

    // Эрх удирдлага
    /**
     * Дүрт эрх нэмэх
     */
    @Modifying
    @Query(value = "INSERT INTO role_permissions (role_id, permission_id, granted_by, granted_at) VALUES (:roleId, :permissionId, :grantedBy, CURRENT_TIMESTAMP)", nativeQuery = true)
    void addPermissionToRole(@Param("roleId") UUID roleId, 
                           @Param("permissionId") UUID permissionId,
                           @Param("grantedBy") String grantedBy);

    /**
     * Дүрээс эрх хасах
     */
    @Modifying
    @Query(value = "DELETE FROM role_permissions WHERE role_id = :roleId AND permission_id = :permissionId", nativeQuery = true)
    void removePermissionFromRole(@Param("roleId") UUID roleId, @Param("permissionId") UUID permissionId);

    /**
     * Дүрийн бүх эрх хасах
     */
    @Modifying
    @Query(value = "DELETE FROM role_permissions WHERE role_id = :roleId", nativeQuery = true)
    void removeAllPermissionsFromRole(@Param("roleId") UUID roleId);

    /**
     * Дүрүүдийн эрх солих
     */
    @Modifying
    @Query(value = "UPDATE role_permissions SET permission_id = :newPermissionId WHERE role_id IN :roleIds AND permission_id = :oldPermissionId", nativeQuery = true)
    int replacePermissionInRoles(@Param("roleIds") List<UUID> roleIds,
                               @Param("oldPermissionId") UUID oldPermissionId,
                               @Param("newPermissionId") UUID newPermissionId);

    // Хэрэглэгч удирдлага
    /**
     * Дүрт хэрэглэгч нэмэх
     */
    @Modifying
    @Query(value = "INSERT INTO user_roles (user_id, role_id, assigned_by, assigned_at) VALUES (:userId, :roleId, :assignedBy, CURRENT_TIMESTAMP)", nativeQuery = true)
    void addUserToRole(@Param("userId") UUID userId, 
                     @Param("roleId") UUID roleId,
                     @Param("assignedBy") String assignedBy);

    /**
     * Дүрээс хэрэглэгч хасах
     */
    @Modifying
    @Query(value = "DELETE FROM user_roles WHERE user_id = :userId AND role_id = :roleId", nativeQuery = true)
    void removeUserFromRole(@Param("userId") UUID userId, @Param("roleId") UUID roleId);

    /**
     * Дүрийн бүх хэрэглэгч хасах
     */
    @Modifying
    @Query(value = "DELETE FROM user_roles WHERE role_id = :roleId", nativeQuery = true)
    void removeAllUsersFromRole(@Param("roleId") UUID roleId);

    /**
     * Олон хэрэглэгчид дүр өгөх
     */
    @Modifying
    @Query(value = "INSERT INTO user_roles (user_id, role_id, assigned_by, assigned_at) " +
           "SELECT :userId, r.id, :assignedBy, CURRENT_TIMESTAMP FROM roles r WHERE r.id IN :roleIds", nativeQuery = true)
    void assignRolesToUser(@Param("userId") UUID userId,
                         @Param("roleIds") List<UUID> roleIds,
                         @Param("assignedBy") String assignedBy);

    // Validation
    /**
     * Хэрэглэгч дүртэй эсэхийг шалгах
     */
    @Query("SELECT CASE WHEN COUNT(ur) > 0 THEN true ELSE false END " +
           "FROM User u JOIN u.roles ur WHERE u.id = :userId AND ur.id = :roleId")
    Boolean userHasRole(@Param("userId") UUID userId, @Param("roleId") UUID roleId);

    /**
     * Дүр тодорхой эрхтэй эсэхийг шалгах
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END " +
           "FROM Role r JOIN r.permissions p " +
           "WHERE r.id = :roleId AND p.name = :permissionName")
    Boolean roleHasPermissionByName(@Param("roleId") UUID roleId, @Param("permissionName") String permissionName);

    /**
     * Дүр ресурс дээр эрхтэй эсэхийг шалгах
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END " +
           "FROM Role r JOIN r.permissions p " +
           "WHERE r.id = :roleId AND p.resource = :resource AND p.action = :action")
    Boolean roleHasResourcePermission(@Param("roleId") UUID roleId,
                                    @Param("resource") String resource,
                                    @Param("action") String action);

    // Cleanup
    /**
     * Ашиглагдаагүй дүрүүд
     */
    @Query("SELECT r FROM Role r WHERE SIZE(r.users) = 0 AND r.type != 'SYSTEM'")
    List<Role> findUnusedRoles();

    /**
     * Эрхгүй дүрүүд (системийн биш)
     */
    @Query("SELECT r FROM Role r WHERE SIZE(r.permissions) = 0 AND r.type != 'SYSTEM'")
    List<Role> findEmptyRoles();

    /**
     * Хүчингүй дүрүүд
     */
    @Query("SELECT r FROM Role r WHERE r.isActive = false")
    List<Role> findInactiveRoles();

    // Бизнес логик
    /**
     * Админ дүрүүд
     */
    @Query("SELECT r FROM Role r WHERE r.name LIKE '%ADMIN%' OR r.priority >= 80")
    List<Role> findAdminRoles();

    /**
     * Менежерийн дүрүүд
     */
    @Query("SELECT r FROM Role r WHERE r.name LIKE '%MANAGER%' OR r.priority BETWEEN 50 AND 79")
    List<Role> findManagerRoles();

    /**
     * Ажилтны дүрүүд
     */
    @Query("SELECT r FROM Role r WHERE r.priority <= 49 AND r.type != 'SYSTEM'")
    List<Role> findEmployeeRoles();

    /**
     * Зээлийн дүрүүд
     */
    @Query("SELECT DISTINCT r FROM Role r JOIN r.permissions p " +
           "WHERE p.resource IN ('loan_application', 'customer') AND " +
           "p.action IN ('CREATE', 'UPDATE', 'APPROVE')")
    List<Role> findLoanProcessingRoles();

    // Role hierarchy methods
    /**
     * Хэрэглэгчийн хамгийн өндөр дүр
     */
    @Query("SELECT r FROM Role r JOIN r.users u WHERE u.id = :userId " +
           "ORDER BY r.priority DESC")
    Optional<Role> findHighestRoleForUser(@Param("userId") UUID userId);

    /**
     * Хэрэглэгчийн хамгийн доод дүр
     */
    @Query("SELECT r FROM Role r JOIN r.users u WHERE u.id = :userId " +
           "ORDER BY r.priority ASC")
    Optional<Role> findLowestRoleForUser(@Param("userId") UUID userId);

    /**
     * Дүрийн шаталсан жагсаалт
     */
    @Query("SELECT r FROM Role r WHERE r.isActive = true ORDER BY r.priority DESC, r.name ASC")
    List<Role> findRoleHierarchy();

    // Dashboard
    /**
     * Дүрийн dashboard статистик
     */
    @Query("SELECT " +
           "COUNT(r) as totalRoles, " +
           "COUNT(CASE WHEN r.type = 'SYSTEM' THEN 1 END) as systemRoles, " +
           "COUNT(CASE WHEN r.isDefault = true THEN 1 END) as defaultRoles, " +
           "COUNT(CASE WHEN SIZE(r.users) = 0 THEN 1 END) as emptyRoles, " +
           "COUNT(CASE WHEN SIZE(r.permissions) = 0 THEN 1 END) as permissionlessRoles, " +
           "AVG(SIZE(r.users)) as avgUsersPerRole, " +
           "AVG(SIZE(r.permissions)) as avgPermissionsPerRole " +
           "FROM Role r")
    Object[] getRoleDashboardStats();

    /**
     * Эрэмбийн тархалт
     */
    @Query("SELECT r.priority, COUNT(r), AVG(SIZE(r.users)), AVG(SIZE(r.permissions)) " +
           "FROM Role r GROUP BY r.priority ORDER BY r.priority DESC")
    List<Object[]> getPriorityDistribution();

    // Data integrity
    /**
     * Дүрийн өгөгдлийн integrity шалгах
     */
    @Query("SELECT r FROM Role r WHERE " +
           "r.name IS NULL OR r.displayName IS NULL OR r.priority IS NULL")
    List<Role> findRolesWithDataIssues();

    /**
     * Давхардсан нэртэй дүрүүд
     */
    @Query("SELECT r1.name, r2.name FROM Role r1, Role r2 " +
           "WHERE LOWER(r1.name) = LOWER(r2.name) AND r1.id != r2.id")
    List<Object[]> findDuplicateRoleNames();

    /**
     * Давхардсан харагдах нэртэй дүрүүд
     */
    @Query("SELECT r1.displayName, r2.displayName FROM Role r1, Role r2 " +
           "WHERE LOWER(r1.displayName) = LOWER(r2.displayName) AND r1.id != r2.id")
    List<Object[]> findDuplicateDisplayNames();

    // Role template and copying
    /**
     * Дүр хуулбарлах
     */
    @Query("SELECT p FROM Permission p JOIN p.roles r WHERE r.id = :sourceRoleId")
    List<Permission> findPermissionsForCopying(@Param("sourceRoleId") UUID sourceRoleId);

    /**
     * Шаблон дүрүүд
     */
    @Query("SELECT r FROM Role r WHERE r.name LIKE 'TEMPLATE_%' OR r.description LIKE '%template%'")
    List<Role> findTemplateRoles();

    // Bulk operations
    /**
     * Олон дүрийн нэр өөрчлөх
     */
    @Modifying
    @Query("UPDATE Role r SET r.displayName = CONCAT(r.displayName, ' (Archived)'), r.isActive = false " +
           "WHERE r.id IN :roleIds")
    int archiveRoles(@Param("roleIds") List<UUID> roleIds);

    /**
     * Олон дүрийн эрэмбэ өөрчлөх
     */
    @Modifying
    @Query("UPDATE Role r SET r.priority = :newPriority, r.updatedBy = :updatedBy " +
           "WHERE r.id IN :roleIds")
    int updatePriorityForRoles(@Param("roleIds") List<UUID> roleIds,
                          @Param("newPriority") Integer newPriority,
                          @Param("updatedBy") String updatedBy);

    // Security checks
    /**
     * Эрсдэлтэй дүрүүд
     */
    @Query("SELECT DISTINCT r FROM Role r JOIN r.permissions p " +
           "WHERE p.action IN ('DELETE', 'APPROVE', 'AUDIT') AND " +
           "p.resource IN ('system', 'user', 'role')")
    List<Role> findHighRiskRoles();

    /**
     * Олон эрхтэй дүрүүд
     */
    @Query("SELECT r FROM Role r WHERE SIZE(r.permissions) > :permissionThreshold")
    List<Role> findRolesWithTooManyPermissions(@Param("permissionThreshold") int permissionThreshold);

    // Special queries
    /**
     * Дүрийн эрхийн matrix
     */
    @Query("SELECT r.name, p.resource, p.action FROM Role r " +
           "JOIN r.permissions p " +
           "ORDER BY r.priority DESC, r.name, p.resource, p.action")
    List<Object[]> getRolePermissionMatrix();

    /**
     * Хэрэглэгчийн дүрүүдийн нэгдсэн эрх
     */
    @Query("SELECT DISTINCT p FROM Permission p " +
           "JOIN p.roles r " +
           "JOIN r.users u " +
           "WHERE u.id = :userId")
    List<Permission> findCombinedPermissionsForUser(@Param("userId") UUID userId);
}