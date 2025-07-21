package com.company.los.service;

import com.company.los.dto.UserDto; // Package name засварласан
import com.company.los.entity.Role;
import com.company.los.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Хэрэглэгчийн Service Interface
 * User Service Interface - extends Spring Security UserDetailsService
 */
public interface UserService extends UserDetailsService {

    // CRUD операциуд
    /**
     * Шинэ хэрэглэгч үүсгэх
     */
    UserDto createUser(UserDto userDto);

    /**
     * Хэрэглэгчийн мэдээлэл авах
     */
    UserDto getUserById(UUID id);

    /**
     * Хэрэглэгч шинэчлэх
     */
    UserDto updateUser(UUID id, UserDto userDto);

    /**
     * Хэрэглэгч устгах (soft delete)
     */
    void deleteUser(UUID id);

    /**
     * Устгасан хэрэглэгч сэргээх
     */
    UserDto restoreUser(UUID id);

    // Authentication operations
    /**
     * Хэрэглэгчийн нэрээр хайх
     */
    UserDto getUserByUsername(String username);

    /**
     * И-мэйлээр хайх
     */
    UserDto getUserByEmail(String email);

    /**
     * Ажилтны дугаараар хайх
     */
    UserDto getUserByEmployeeId(String employeeId);

    /**
     * Утасны дугаараар хайх
     */
    UserDto getUserByPhone(String phone);

    // Хайлт операциуд
    /**
     * Бүх хэрэглэгчдийн жагсаалт
     */
    Page<UserDto> getAllUsers(Pageable pageable);

    /**
     * Статусаар хайх
     */
    Page<UserDto> getUsersByStatus(User.UserStatus status, Pageable pageable);

    /**
     * Хэлтэсээр хайх
     */
    Page<UserDto> getUsersByDepartment(String department, Pageable pageable);

    /**
     * Албан тушаалаар хайх
     */
    Page<UserDto> getUsersByPosition(String position, Pageable pageable);

    /**
     * Нэрээр хайх
     */
    Page<UserDto> searchUsersByName(String searchTerm, Pageable pageable);

    /**
     * Ерөнхий хайлт
     */
    Page<UserDto> searchUsers(String searchTerm, Pageable pageable);

    // Дэвшилтэт хайлт
    /**
     * Филтертэй дэвшилтэт хайлт
     */
    Page<UserDto> searchUsersWithFilters(
            User.UserStatus status,
            String department,
            String position,
            Boolean enabled,
            Boolean twoFactorEnabled,
            Boolean hasRoles,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    );

    // Role management
    /**
     * Хэрэглэгчид дүр олгох
     */
    UserDto assignRoleToUser(UUID userId, UUID roleId);

    /**
     * Хэрэглэгчээс дүр хасах
     */
    UserDto removeRoleFromUser(UUID userId, UUID roleId);

    /**
     * Хэрэглэгчийн дүрүүд
     */
    List<Role> getUserRoles(UUID userId);

    /**
     * Дүртэй хэрэглэгчид
     */
    Page<UserDto> getUsersByRole(UUID roleId, Pageable pageable);

    /**
     * Дүрийн нэрээр хэрэглэгч хайх
     */
    Page<UserDto> getUsersByRoleName(String roleName, Pageable pageable);

    /**
     * Олон дүртэй хэрэглэгчид
     */
    List<UserDto> getUsersWithMultipleRoles();

    /**
     * Дүргүй хэрэглэгчид
     */
    List<UserDto> getUsersWithoutRoles();

    // Account management
    /**
     * Идэвхтэй хэрэглэгчид
     */
    Page<UserDto> getActiveUsers(Pageable pageable);

    /**
     * Түгжээтэй хэрэглэгчид
     */
    Page<UserDto> getLockedUsers(Pageable pageable);

    /**
     * Хэрэглэгч идэвхжүүлэх
     */
    UserDto enableUser(UUID id);

    /**
     * Хэрэглэгч идэвхгүй болгох
     */
    UserDto disableUser(UUID id);

    /**
     * Хэрэглэгч түгжих
     */
    UserDto lockUser(UUID id, LocalDateTime until, String reason);

    /**
     * Хэрэглэгч түгжээ тайлах
     */
    UserDto unlockUser(UUID id);

    /**
     * Хэрэглэгч түр зогсоох
     */
    UserDto suspendUser(UUID id, String reason);

    // Password management
    /**
     * Нууц үг өөрчлөх
     */
    UserDto changePassword(UUID id, String currentPassword, String newPassword);

    /**
     * Нууц үг сэргээх
     */
    UserDto resetPassword(UUID id, String newPassword);

    /**
     * Нууц үг хуучирсан хэрэглэгчид
     */
    List<UserDto> getUsersWithExpiredPasswords();

    /**
     * Нууц үг сэргээх шаардлагатай гэж тэмдэглэх
     */
    UserDto markPasswordExpired(UUID id);

    /**
     * Нууц үгийн үүсгэх
     */
    String generateTemporaryPassword();

    // Login management
    /**
     * Амжилттай нэвтрэх бүртгэх
     */
    void recordSuccessfulLogin(String username);

    /**
     * Амжилтгүй нэвтрэх бүртгэх
     */
    void recordFailedLoginAttempt(String username);

    /**
     * Сүүлийн нэвтрэх огноогоор хайх
     */
    Page<UserDto> getUsersByLastLogin(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * Урт хугацаанд нэвтрээгүй хэрэглэгчид
     */
    List<UserDto> getInactiveUsers(int days);

    /**
     * Олон удаа буруу нууц үг оруулсан хэрэглэгчид
     */
    List<UserDto> getUsersWithFailedAttempts(int threshold);

    // Two-Factor Authentication
    /**
     * 2FA идэвхжүүлэх
     */
    UserDto enableTwoFactor(UUID id);

    /**
     * 2FA идэвхгүй болгох
     */
    UserDto disableTwoFactor(UUID id);

    /**
     * 2FA secret үүсгэх
     */
    String generateTwoFactorSecret(UUID id);

    /**
     * 2FA идэвхжүүлсэн хэрэглэгчид
     */
    Page<UserDto> getUsersWithTwoFactorEnabled(Pageable pageable);

    /**
     * 2FA идэвхжүүлээгүй хэрэглэгчид
     */
    Page<UserDto> getUsersWithoutTwoFactor(Pageable pageable);

    // Profile management
    /**
     * Хэрэглэгчийн профайл шинэчлэх
     */
    UserDto updateUserProfile(UUID id, UserDto profileDto);

    /**
     * Хэрэглэгчийн тохиргоо шинэчлэх
     */
    UserDto updateUserPreferences(UUID id, String language, String timezone);

    /**
     * Профайл зураг upload хийх
     */
    UserDto uploadProfilePicture(UUID id, byte[] imageData, String contentType);

    // Validation
    /**
     * Хэрэглэгчийн нэр байгаа эсэхийг шалгах
     */
    boolean existsByUsername(String username);

    /**
     * И-мэйл байгаа эсэхийг шалгах
     */
    boolean existsByEmail(String email);

    /**
     * Хэрэглэгчийн мэдээлэл хүчинтэй эсэхийг шалгах
     */
    boolean validateUserData(UserDto userDto);

    /**
     * Хэрэглэгч устгах боломжтой эсэхийг шалгах
     */
    boolean canDeleteUser(UUID id);

    /**
     * Нууц үгийн бат байдал шалгах
     */
    boolean isPasswordStrong(String password);

    // Permissions
    /**
     * Хэрэглэгчийн эрхүүд
     */
    Set<String> getUserPermissions(UUID id);

    /**
     * Хэрэглэгч эрхтэй эсэхийг шалгах
     */
    boolean hasPermission(UUID id, String permissionName);

    /**
     * Хэрэглэгч ресурс дээр эрхтэй эсэхийг шалгах
     */
    boolean hasResourcePermission(UUID id, String resource, String action);

    /**
     * Эрхтэй хэрэглэгчид
     */
    List<UserDto> getUsersWithPermission(String permissionName);

    // Статистик
    /**
     * Хэрэглэгчийн статистик
     */
    Map<String, Object> getUserStatistics();

    /**
     * Статусаар статистик
     */
    Map<User.UserStatus, Long> getUserCountByStatus();

    /**
     * Хэлтэсээр статистик
     */
    Map<String, Long> getUserCountByDepartment();

    /**
     * Дүрээр статистик
     */
    Map<String, Long> getUserCountByRole();

    /**
     * Сарын хэрэглэгчийн статистик
     */
    List<Map<String, Object>> getMonthlyUserStats(int months);

    /**
     * Нэвтрэх үйл ажиллагааны статистик
     */
    List<Map<String, Object>> getLoginStats(LocalDateTime startDate, LocalDateTime endDate);

    // Dashboard
    /**
     * Өнөөдрийн хэрэглэгчийн статистик
     */
    Map<String, Object> getTodayUserStats();

    /**
     * Хэрэглэгчийн идэвхжилт
     */
    Map<String, Object> getUserActivityStats();

    /**
     * Хамгийн идэвхтэй хэрэглэгчид
     */
    Page<UserDto> getMostActiveUsers(Pageable pageable);

    /**
     * Хамгийн идэвхгүй хэрэглэгчид
     */
    Page<UserDto> getLeastActiveUsers(Pageable pageable);

    // Bulk операциуд
    /**
     * Олон хэрэглэгчийн статус өөрчлөх
     */
    int updateStatusForUsers(List<UUID> userIds, User.UserStatus newStatus, Boolean enabled);

    /**
     * Амжилтгүй нэвтрэх тоог reset хийх
     */
    int resetFailedAttemptsForUsers(List<UUID> userIds);

    /**
     * Нууц үг сэргээх шаардлагатай гэж тэмдэглэх
     */
    int markPasswordExpiredForUsers(List<UUID> userIds);

    /**
     * Олон хэрэглэгч үүсгэх (import)
     */
    List<UserDto> createUsersBulk(List<UserDto> users);

    /**
     * Хэрэглэгчийн мэдээлэл export хийх
     */
    byte[] exportUsersToExcel(List<UUID> userIds);

    // Admin functions
    /**
     * Админ эрхтэй хэрэглэгчид
     */
    List<UserDto> getAdminUsers();

    /**
     * Системийн хэрэглэгчид
     */
    Page<UserDto> getSystemUsers(Pageable pageable);

    /**
     * Энгийн хэрэглэгчид
     */
    Page<UserDto> getRegularUsers(Pageable pageable);

    // Cleanup operations
    /**
     * Идэвхгүй хэрэглэгчдыг цэвэрлэх
     */
    int cleanupInactiveUsers(int inactiveDays);

    /**
     * Хэзээ ч нэвтрээгүй хэрэглэгчид
     */
    List<UserDto> getUsersNeverLoggedIn(int days);

    // Notification
    /**
     * Хэрэглэгчид мэдэгдэл илгээх
     */
    boolean sendNotificationToUser(UUID userId, String subject, String message);

    /**
     * Нууц үг сэргээх мэдэгдэл
     */
    boolean sendPasswordResetNotification(UUID userId);

    /**
     * Акаунт түгжигдсэн мэдэгдэл
     */
    boolean sendAccountLockedNotification(UUID userId);

    // Audit & History
    /**
     * Хэрэглэгчийн өөрчлөлтийн түүх
     */
    List<Map<String, Object>> getUserAuditHistory(UUID id);

    /**
     * Хэрэглэгчийн үйл ажиллагааны лого
     */
    List<Map<String, Object>> getUserActivityLog(UUID id, int days);

    /**
     * Нэвтрэх түүх
     */
    List<Map<String, Object>> getUserLoginHistory(UUID id, int days);

    // Data integrity
    /**
     * Өгөгдлийн бүрэн бус байдлыг шалгах
     */
    Map<String, Object> validateDataIntegrity();

    /**
     * Дүр эрх consistency шалгах
     */
    List<Map<String, Object>> validateRolePermissionConsistency();
}