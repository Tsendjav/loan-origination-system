package com.company.los.repository;

import com.company.los.entity.Permission;
import com.company.los.entity.Role;
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
 * Эрхийн Repository
 * Permission Repository Interface for RBAC
 */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, String> {

    // Суурь хайлтууд
    /**
     * Нэрээр хайх
     */
    Optional<Permission> findByName(String name);

    /**
     * Харагдах нэрээр хайх
     */
    Optional<Permission> findByDisplayName(String displayName);

    /**
     * Нэр байгаа эсэхийг шалгах
     */
    boolean existsByName(String name);

    /**
     * Ресурс болон үйлдлээр хайх
     */
    Optional<Permission> findByResourceAndAction(String resource, String action);

    // Ресурсээр хайх
    /**
     * Ресурсээр хайх
     */
    Page<Permission> findByResource(String resource, Pageable pageable);

    /**
     * Олон ресурсээр хайх
     */
    @Query("SELECT p FROM Permission p WHERE p.resource IN :resources")
    List<Permission> findByResourceIn(@Param("resources") List<String> resources);

    /**
     * Ресурсийн бүх эрхүүд
     */
    @Query("SELECT p FROM Permission p WHERE p.resource = :resource ORDER BY p.action, p.priority DESC")
    List<Permission> findAllByResourceOrdered(@Param("resource") String resource);

    // Үйлдлээр хайх
    /**
     * Үйлдлээр хайх
     */
    Page<Permission> findByAction(String action, Pageable pageable);

    /**
     * Олон үйлдлээр хайх
     */
    @Query("SELECT p FROM Permission p WHERE p.action IN :actions")
    List<Permission> findByActionIn(@Param("actions") List<String> actions);

    // Категориар хайх
    /**
     * Категориар хайх
     */
    Page<Permission> findByCategory(String category, Pageable pageable);

    /**
     * Олон категориар хайх
     */
    @Query("SELECT p FROM Permission p WHERE p.category IN :categories")
    List<Permission> findByCategoryIn(@Param("categories") List<String> categories);

    // Системийн эрх
    /**
     * Системийн эрхүүд
     */
    @Query("SELECT p FROM Permission p WHERE p.isSystemPermission = true")
    List<Permission> findSystemPermissions();

    /**
     * Системийн бус эрхүүд
     */
    @Query("SELECT p FROM Permission p WHERE p.isSystemPermission = false")
    Page<Permission> findNonSystemPermissions(Pageable pageable);

    // Тэргүүлэх эрэмбэ
    /**
     * Тэргүүлэх эрэмбээр хайх
     */
    Page<Permission> findByPriority(Integer priority, Pageable pageable);

    /**
     * Өндөр тэргүүлэх эрэмбэтэй эрхүүд
     */
    @Query("SELECT p FROM Permission p WHERE p.priority >= :highPriorityThreshold ORDER BY p.priority DESC")
    List<Permission> findHighPriorityPermissions(@Param("highPriorityThreshold") Integer highPriorityThreshold);

    /**
     * Тэргүүлэх эрэмбэ дээр нь дараалсан эрхүүд
     */
    @Query("SELECT p FROM Permission p ORDER BY p.priority DESC, p.category, p.resource, p.action")
    List<Permission> findAllOrderedByPriority();

    // Дүрүүдтэй холбоотой
    /**
     * Дүртэй эрхүүд
     */
    @Query("SELECT p FROM Permission p WHERE SIZE(p.roles) > 0")
    Page<Permission> findPermissionsWithRoles(Pageable pageable);

    /**
     * Дүргүй эрхүүд
     */
    @Query("SELECT p FROM Permission p WHERE SIZE(p.roles) = 0")
    List<Permission> findPermissionsWithoutRoles();

    /**
     * Тодорхой дүрийн эрхүүд
     */
    @Query("SELECT p FROM Permission p JOIN p.roles r WHERE r = :role")
    List<Permission> findPermissionsByRole(@Param("role") Role role);

    /**
     * Дүрийн нэрээр эрх хайх
     */
    @Query("SELECT p FROM Permission p JOIN p.roles r WHERE r.name = :roleName")
    List<Permission> findPermissionsByRoleName(@Param("roleName") String roleName);

    /**
     * Олон дүртэй эрхүүд
     */
    @Query("SELECT p FROM Permission p WHERE SIZE(p.roles) > 1")
    List<Permission> findPermissionsWithMultipleRoles();

    // Нэрээр хайх
    /**
     * Харагдах нэрээр хайх (орчуулга дэмжих)
     */
    @Query("SELECT p FROM Permission p WHERE " +
           "LOWER(p.displayName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(COALESCE(p.displayNameMn, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Permission> findByDisplayNameContaining(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Ерөнхий хайлт
     */
    @Query("SELECT p FROM Permission p WHERE " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.displayName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(COALESCE(p.displayNameMn, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.resource) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(COALESCE(p.description, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Permission> findBySearchTerm(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Хамрах хүрээ (Scope)
    /**
     * Scope-оор хайх
     */
    Page<Permission> findByScope(String scope, Pageable pageable);

    /**
     * Scope-тэй эрхүүд
     */
    @Query("SELECT p FROM Permission p WHERE p.scope IS NOT NULL")
    List<Permission> findPermissionsWithScope();

    /**
     * Scope-гүй эрхүүд
     */
    @Query("SELECT p FROM Permission p WHERE p.scope IS NULL")
    List<Permission> findPermissionsWithoutScope();

    // Статистик
    /**
     * Ресурсээр тоолох
     */
    @Query("SELECT p.resource, COUNT(p) FROM Permission p GROUP BY p.resource ORDER BY COUNT(p) DESC")
    List<Object[]> countByResource();

    /**
     * Үйлдлээр тоолох
     */
    @Query("SELECT p.action, COUNT(p) FROM Permission p GROUP BY p.action ORDER BY COUNT(p) DESC")
    List<Object[]> countByAction();

    /**
     * Категориар тоолох
     */
    @Query("SELECT p.category, COUNT(p) FROM Permission p GROUP BY p.category ORDER BY COUNT(p) DESC")
    List<Object[]> countByCategory();

    /**
     * Дүрүүдийн тоогоор статистик
     */
    @Query("SELECT p.name, p.displayName, SIZE(p.roles) as roleCount FROM Permission p " +
           "ORDER BY SIZE(p.roles) DESC")
    List<Object[]> getPermissionRoleStats();

    /**
     * Тэргүүлэх эрэмбээр статистик
     */
    @Query("SELECT p.priority, COUNT(p) FROM Permission p GROUP BY p.priority ORDER BY p.priority DESC")
    List<Object[]> countByPriority();

    // CRUD үйлдлүүдийн тусгай функцууд
    /**
     * CRUD эрхүүд
     */
    @Query("SELECT p FROM Permission p WHERE p.action IN ('CREATE', 'READ', 'UPDATE', 'DELETE')")
    List<Permission> findCrudPermissions();

    /**
     * Зөвшөөрөлийн эрхүүд
     */
    @Query("SELECT p FROM Permission p WHERE p.action IN ('APPROVE', 'REJECT')")
    List<Permission> findApprovalPermissions();

    /**
     * Файлын эрхүүд
     */
    @Query("SELECT p FROM Permission p WHERE p.action IN ('UPLOAD', 'DOWNLOAD', 'EXPORT', 'IMPORT')")
    List<Permission> findFilePermissions();

    /**
     * Удирдлагын эрхүүд
     */
    @Query("SELECT p FROM Permission p WHERE p.action IN ('ASSIGN', 'REVIEW', 'AUDIT')")
    List<Permission> findManagementPermissions();

    // Тодорхой ресурсийн эрхүүд
    /**
     * Харилцагчийн эрхүүд
     */
    @Query("SELECT p FROM Permission p WHERE p.resource = 'customer'")
    List<Permission> findCustomerPermissions();

    /**
     * Зээлийн эрхүүд
     */
    @Query("SELECT p FROM Permission p WHERE p.resource = 'loan_application'")
    List<Permission> findLoanPermissions();

    /**
     * Баримт бичгийн эрхүүд
     */
    @Query("SELECT p FROM Permission p WHERE p.resource = 'document'")
    List<Permission> findDocumentPermissions();

    /**
     * Хэрэглэгчийн эрхүүд
     */
    @Query("SELECT p FROM Permission p WHERE p.resource = 'user'")
    List<Permission> findUserPermissions();

    /**
     * Дүрийн эрхүүд
     */
    @Query("SELECT p FROM Permission p WHERE p.resource = 'role'")
    List<Permission> findRolePermissions();

    // Дэвшилтэт хайлт
    /**
     * Дэвшилтэт филтертэй хайлт
     */
    @Query("SELECT p FROM Permission p WHERE " +
           "(:resource IS NULL OR LOWER(p.resource) = LOWER(:resource)) AND " +
           "(:action IS NULL OR LOWER(p.action) = LOWER(:action)) AND " +
           "(:category IS NULL OR LOWER(p.category) = LOWER(:category)) AND " +
           "(:isSystemPermission IS NULL OR p.isSystemPermission = :isSystemPermission) AND " +
           "(:minPriority IS NULL OR p.priority >= :minPriority) AND " +
           "(:maxPriority IS NULL OR p.priority <= :maxPriority) AND " +
           "(:hasRoles IS NULL OR (SIZE(p.roles) > 0) = :hasRoles) AND " +
           "(:scope IS NULL OR p.scope = :scope)")
    Page<Permission> findByAdvancedFilters(
            @Param("resource") String resource,
            @Param("action") String action,
            @Param("category") String category,
            @Param("isSystemPermission") Boolean isSystemPermission,
            @Param("minPriority") Integer minPriority,
            @Param("maxPriority") Integer maxPriority,
            @Param("hasRoles") Boolean hasRoles,
            @Param("scope") String scope,
            Pageable pageable);

    // Дүр удирдлага
    /**
     * Эрхийг дүрт нэмэх
     */
    @Modifying
    @Query("INSERT INTO role_permissions (role_id, permission_id, granted_by, granted_at) VALUES (:roleId, :permissionId, :grantedBy, CURRENT_TIMESTAMP)")
    void addPermissionToRole(@Param("roleId") String roleId, 
                           @Param("permissionId") String permissionId,
                           @Param("grantedBy") String grantedBy);

    /**
     * Эрхийг дүрээс хасах
     */
    @Modifying
    @Query("DELETE FROM role_permissions WHERE role_id = :roleId AND permission_id = :permissionId")
    void removePermissionFromRole(@Param("roleId") String roleId, @Param("permissionId") String permissionId);

    /**
     * Эрхийг бүх дүрээс хасах
     */
    @Modifying
    @Query("DELETE FROM role_permissions WHERE permission_id = :permissionId")
    void removePermissionFromAllRoles(@Param("permissionId") String permissionId);

    // Validation
    /**
     * Хэрэглэгч эрхтэй эсэхийг шалгах
     */
    @Query("SELECT CASE WHEN COUNT(rp) > 0 THEN true ELSE false END " +
           "FROM User u JOIN u.roles r JOIN r.permissions rp " +
           "WHERE u.id = :userId AND rp.id = :permissionId")
    Boolean userHasPermission(@Param("userId") String userId, @Param("permissionId") String permissionId);

    /**
     * Хэрэглэгчийн ресурс дээрх эрх шалгах
     */
    @Query("SELECT CASE WHEN COUNT(rp) > 0 THEN true ELSE false END " +
           "FROM User u JOIN u.roles r JOIN r.permissions rp " +
           "WHERE u.id = :userId AND rp.resource = :resource AND rp.action = :action")
    Boolean userHasResourcePermission(@Param("userId") String userId,
                                    @Param("resource") String resource,
                                    @Param("action") String action);

    /**
     * Дүр эрхтэй эсэхийг шалгах
     */
    @Query("SELECT CASE WHEN COUNT(rp) > 0 THEN true ELSE false END " +
           "FROM Role r JOIN r.permissions rp " +
           "WHERE r.id = :roleId AND rp.id = :permissionId")
    Boolean roleHasPermission(@Param("roleId") String roleId, @Param("permissionId") String permissionId);

    // Хэрэглэгчийн эрхүүд
    /**
     * Хэрэглэгчийн бүх эрх
     */
    @Query("SELECT DISTINCT p FROM Permission p JOIN p.roles r JOIN r.users u WHERE u.id = :userId")
    List<Permission> findUserPermissions(@Param("userId") String userId);

    /**
     * Хэрэглэгчийн ресурсийн эрхүүд
     */
    @Query("SELECT DISTINCT p FROM Permission p JOIN p.roles r JOIN r.users u " +
           "WHERE u.id = :userId AND p.resource = :resource")
    List<Permission> findUserPermissionsByResource(@Param("userId") String userId, @Param("resource") String resource);

    /**
     * Хэрэглэгчийн категорийн эрхүүд
     */
    @Query("SELECT DISTINCT p FROM Permission p JOIN p.roles r JOIN r.users u " +
           "WHERE u.id = :userId AND p.category = :category")
    List<Permission> findUserPermissionsByCategory(@Param("userId") String userId, @Param("category") String category);

    /**
     * Хэрэглэгчийн үйлдлийн эрхүүд
     */
    @Query("SELECT DISTINCT p FROM Permission p JOIN p.roles r JOIN r.users u " +
           "WHERE u.id = :userId AND p.action = :action")
    List<Permission> findUserPermissionsByAction(@Param("userId") String userId, @Param("action") String action);

    // Cleanup
    /**
     * Ашиглагдаагүй эрхүүд
     */
    @Query("SELECT p FROM Permission p WHERE SIZE(p.roles) = 0 AND p.isSystemPermission = false")
    List<Permission> findUnusedPermissions();

    /**
     * Дүрддэггүй системийн эрхүүд
     */
    @Query("SELECT p FROM Permission p WHERE SIZE(p.roles) = 0 AND p.isSystemPermission = true")
    List<Permission> findUnassignedSystemPermissions();

    // Бизнес логик
    /**
     * Админ эрхүүд
     */
    @Query("SELECT p FROM Permission p WHERE " +
           "p.resource = 'system' OR " +
           "p.category = 'SYSTEM_ADMINISTRATION' OR " +
           "p.priority >= 8")
    List<Permission> findAdminPermissions();

    /**
     * Зээлийн эрхүүд
     */
    @Query("SELECT p FROM Permission p WHERE " +
           "p.resource IN ('loan_application', 'customer') OR " +
           "p.category IN ('LOAN_PROCESSING', 'CUSTOMER_MANAGEMENT')")
    List<Permission> findLoanRelatedPermissions();

    /**
     * Тайлангийн эрхүүд
     */
    @Query("SELECT p FROM Permission p WHERE " +
           "p.resource = 'report' OR " +
           "p.category = 'REPORTING' OR " +
           "p.action IN ('EXPORT', 'PRINT')")
    List<Permission> findReportPermissions();

    /**
     * Баримт бичгийн эрхүүд
     */
    @Query("SELECT p FROM Permission p WHERE " +
           "p.resource = 'document' OR " +
           "p.category = 'DOCUMENT_MANAGEMENT'")
    List<Permission> findDocumentManagementPermissions();

    // Dashboard
    /**
     * Эрхийн dashboard статистик
     */
    @Query("SELECT " +
           "COUNT(p) as totalPermissions, " +
           "COUNT(CASE WHEN p.isSystemPermission = true THEN 1 END) as systemPermissions, " +
           "COUNT(CASE WHEN SIZE(p.roles) = 0 THEN 1 END) as unassignedPermissions, " +
           "COUNT(DISTINCT p.resource) as uniqueResources, " +
           "COUNT(DISTINCT p.category) as uniqueCategories, " +
           "COUNT(DISTINCT p.action) as uniqueActions " +
           "FROM Permission p")
    Object[] getPermissionDashboardStats();

    /**
     * Эрхийн matrix (ресурс x үйлдэл)
     */
    @Query("SELECT p.resource, p.action, COUNT(p) FROM Permission p " +
           "GROUP BY p.resource, p.action " +
           "ORDER BY p.resource, p.action")
    List<Object[]> getPermissionMatrix();

    // Data integrity
    /**
     * Өгөгдөл алдаатай эрхүүд
     */
    @Query("SELECT p FROM Permission p WHERE " +
           "p.name IS NULL OR p.displayName IS NULL OR " +
           "p.resource IS NULL OR p.action IS NULL OR p.category IS NULL")
    List<Permission> findPermissionsWithDataIssues();

    /**
     * Давхардсан эрхүүд шалгах
     */
    @Query("SELECT p1.name, p2.name FROM Permission p1, Permission p2 " +
           "WHERE p1.resource = p2.resource AND p1.action = p2.action AND p1.id != p2.id")
    List<Object[]> findDuplicatePermissions();

    // Автомат эрх үүсгэх
    /**
     * Ресурсийн суурь CRUD эрхүүд байгаа эсэхийг шалгах
     */
    @Query("SELECT DISTINCT p.resource FROM Permission p WHERE " +
           "p.action IN ('CREATE', 'READ', 'UPDATE', 'DELETE') " +
           "GROUP BY p.resource " +
           "HAVING COUNT(DISTINCT p.action) = 4")
    List<String> findResourcesWithCompleteCrud();

    /**
     * Дутуу CRUD эрхтэй ресурсууд
     */
    @Query("SELECT p.resource, COUNT(DISTINCT p.action) as actionCount FROM Permission p " +
           "WHERE p.action IN ('CREATE', 'READ', 'UPDATE', 'DELETE') " +
           "GROUP BY p.resource " +
           "HAVING COUNT(DISTINCT p.action) < 4")
    List<Object[]> findResourcesWithIncompleteCrud();

    /**
     * Ресурсээр эрхийн тоо
     */
    @Query("SELECT p.resource, COUNT(p) as permissionCount FROM Permission p " +
           "GROUP BY p.resource ORDER BY permissionCount DESC")
    List<Object[]> getPermissionCountByResource();

    // Template methods
    /**
     * Ресурсийн стандарт эрхүүд үүсгэх
     */
    @Query("SELECT DISTINCT p.resource FROM Permission p")
    List<String> findAllResources();

    /**
     * Үйлдлийн стандарт эрхүүд үүсгэх
     */
    @Query("SELECT DISTINCT p.action FROM Permission p")
    List<String> findAllActions();

    /**
     * Категорийн стандарт эрхүүд үүсгэх
     */
    @Query("SELECT DISTINCT p.category FROM Permission p")
    List<String> findAllCategories();

    // Security audit
    /**
     * Өндөр эрсдэлтэй эрхүүд
     */
    @Query("SELECT p FROM Permission p WHERE " +
           "p.action IN ('DELETE', 'APPROVE', 'AUDIT') OR " +
           "p.priority >= 8 OR " +
           "p.resource = 'system'")
    List<Permission> findHighRiskPermissions();

    /**
     * Олон хэрэглэгчид байгаа эрхүүд
     */
    @Query("SELECT p, COUNT(DISTINCT u) as userCount FROM Permission p " +
           "JOIN p.roles r JOIN r.users u " +
           "GROUP BY p " +
           "HAVING COUNT(DISTINCT u) > :userThreshold " +
           "ORDER BY userCount DESC")
    List<Object[]> findWidelyUsedPermissions(@Param("userThreshold") int userThreshold);
}