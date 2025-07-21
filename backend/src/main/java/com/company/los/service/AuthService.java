package com.los.service;

import com.los.dto.UserDto;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Authentication Service Interface
 * Нэвтрэх баталгаажуулалтын Service Interface
 */
public interface AuthService {

    // Authentication
    /**
     * Хэрэглэгч нэвтрэх
     */
    Map<String, Object> authenticateUser(String username, String password);

    /**
     * Хэрэглэгч нэвтрэх (2FA-тай)
     */
    Map<String, Object> authenticateUserWith2FA(String username, String password, String twoFactorCode);

    /**
     * JWT token үүсгэх
     */
    String generateJwtToken(UserDetails userDetails);

    /**
     * JWT token баталгаажуулах
     */
    boolean validateJwtToken(String token);

    /**
     * JWT token-оос хэрэглэгчийн нэр авах
     */
    String getUsernameFromJwtToken(String token);

    /**
     * JWT token-оос хэрэглэгчийн мэдээлэл авах
     */
    UserDetails getUserDetailsFromJwtToken(String token);

    /**
     * Token сэргээх
     */
    String refreshJwtToken(String token);

    // Logout
    /**
     * Хэрэглэгч гарах
     */
    boolean logoutUser(String token);

    /**
     * Бүх төхөөрөмжөөс гарах
     */
    boolean logoutUserFromAllDevices(UUID userId);

    /**
     * Token хүчингүй болгох
     */
    boolean invalidateToken(String token);

    // Password operations
    /**
     * Нууц үг солих
     */
    boolean changePassword(UUID userId, String currentPassword, String newPassword);

    /**
     * Нууц үг сэргээх хүсэлт илгээх
     */
    boolean requestPasswordReset(String email);

    /**
     * Нууц үг сэргээх
     */
    boolean resetPassword(String resetToken, String newPassword);

    /**
     * Нууц үг сэргээх token баталгаажуулах
     */
    boolean validatePasswordResetToken(String token);

    /**
     * Түр зуурын нууц үг үүсгэх
     */
    String generateTemporaryPassword();

    // Registration
    /**
     * Шинэ хэрэглэгч бүртгэх
     */
    UserDto registerUser(UserDto userDto);

    /**
     * Хэрэглэгч идэвхжүүлэх
     */
    boolean activateUser(String activationToken);

    /**
     * Идэвхжүүлэх код дахин илгээх
     */
    boolean resendActivationCode(String email);

    // Two-Factor Authentication
    /**
     * 2FA идэвхжүүлэх
     */
    Map<String, Object> enableTwoFactorAuthentication(UUID userId);

    /**
     * 2FA идэвхгүй болгох
     */
    boolean disableTwoFactorAuthentication(UUID userId, String currentPassword);

    /**
     * 2FA код баталгаажуулах
     */
    boolean validateTwoFactorCode(UUID userId, String code);

    /**
     * 2FA backup кодууд үүсгэх
     */
    String[] generateBackupCodes(UUID userId);

    /**
     * 2FA backup код ашиглах
     */
    boolean useTwoFactorBackupCode(UUID userId, String backupCode);

    /**
     * QR код үүсгэх 2FA-д
     */
    byte[] generateQRCode(UUID userId);

    // Session management
    /**
     * Идэвхтэй session-ууд
     */
    Map<String, Object> getActiveSessions(UUID userId);

    /**
     * Session устгах
     */
    boolean terminateSession(UUID userId, String sessionId);

    /**
     * Бүх session устгах
     */
    boolean terminateAllSessions(UUID userId);

    /**
     * Session мэдээлэл шинэчлэх
     */
    boolean updateSessionActivity(String token);

    // Account security
    /**
     * Акаунт түгжих
     */
    boolean lockAccount(UUID userId, String reason, LocalDateTime until);

    /**
     * Акаунт түгжээ тайлах
     */
    boolean unlockAccount(UUID userId);

    /**
     * Акаунт түр зогсоох
     */
    boolean suspendAccount(UUID userId, String reason);

    /**
     * Акаунт идэвхжүүлэх
     */
    boolean activateAccount(UUID userId);

    // Login attempts
    /**
     * Амжилтгүй нэвтрэх оролдлого бүртгэх
     */
    void recordFailedLoginAttempt(String username, String ipAddress);

    /**
     * Амжилттай нэвтрэх бүртгэх
     */
    void recordSuccessfulLogin(String username, String ipAddress);

    /**
     * Login rate limiting шалгах
     */
    boolean isRateLimited(String username, String ipAddress);

    /**
     * Сонирхолтой нэвтрэх үйлдэл илрүүлэх
     */
    boolean detectSuspiciousLoginActivity(String username, String ipAddress);

    // Security validation
    /**
     * Нууц үгийн бат байдал шалгах
     */
    Map<String, Object> validatePasswordStrength(String password);

    /**
     * IP хаяг зөвшөөрөгдсөн эсэхийг шалгах
     */
    boolean isIpAddressAllowed(String ipAddress);

    /**
     * Төхөөрөмж зөвшөөрөгдсөн эсэхийг шалгах
     */
    boolean isDeviceTrusted(UUID userId, String deviceFingerprint);

    /**
     * Шинэ төхөөрөмж бүртгэх
     */
    boolean registerTrustedDevice(UUID userId, String deviceFingerprint, String deviceName);

    // User verification
    /**
     * И-мэйл баталгаажуулалт илгээх
     */
    boolean sendEmailVerification(UUID userId);

    /**
     * И-мэйл баталгаажуулах
     */
    boolean verifyEmail(String verificationToken);

    /**
     * Утасны дугаар баталгаажуулалт илгээх
     */
    boolean sendPhoneVerification(UUID userId);

    /**
     * Утасны дугаар баталгаажуулах
     */
    boolean verifyPhone(UUID userId, String verificationCode);

    // Security notifications
    /**
     * Аюулгүй байдлын мэдэгдэл илгээх
     */
    boolean sendSecurityAlert(UUID userId, String alertType, String details);

    /**
     * Нууц үг өөрчлөгдсөн мэдэгдэл
     */
    boolean sendPasswordChangeNotification(UUID userId);

    /**
     * Шинэ төхөөрөмжөөс нэвтрэсэн мэдэгдэл
     */
    boolean sendNewDeviceLoginNotification(UUID userId, String deviceInfo);

    /**
     * Сонирхолтой үйлдлийн мэдэгдэл
     */
    boolean sendSuspiciousActivityNotification(UUID userId, String activityDetails);

    // Authentication logs
    /**
     * Нэвтрэх түүх авах
     */
    Map<String, Object> getLoginHistory(UUID userId, int days);

    /**
     * Аюулгүй байдлын лог авах
     */
    Map<String, Object> getSecurityLog(UUID userId, int days);

    /**
     * Амжилтгүй нэвтрэх оролдлогууд
     */
    Map<String, Object> getFailedLoginAttempts(String username, int hours);

    // Token management
    /**
     * Идэвхтэй token-ууд
     */
    Map<String, Object> getActiveTokens(UUID userId);

    /**
     * Token-ийн мэдээлэл авах
     */
    Map<String, Object> getTokenInfo(String token);

    /**
     * Token-ийн хугацаа сунгах
     */
    String extendTokenExpiry(String token);

    /**
     * Хуучин token-ууд цэвэрлэх
     */
    int cleanupExpiredTokens();

    // API Key management
    /**
     * API түлхүүр үүсгэх
     */
    String generateApiKey(UUID userId, String description, LocalDateTime expiresAt);

    /**
     * API түлхүүр баталгаажуулах
     */
    boolean validateApiKey(String apiKey);

    /**
     * API түлхүүр устгах
     */
    boolean revokeApiKey(String apiKey);

    /**
     * Хэрэглэгчийн API түлхүүрүүд
     */
    Map<String, Object> getUserApiKeys(UUID userId);

    // Administrative functions
    /**
     * Хэрэглэгч албадан гаргах
     */
    boolean forceLogoutUser(UUID userId);

    /**
     * Системд нэвтэрсэн хэрэглэгчдийн тоо
     */
    int getActiveUserCount();

    /**
     * Онлайн хэрэглэгчдийн жагсаалт
     */
    Map<String, Object> getOnlineUsers();

    /**
     * Аюулгүй байдлын статистик
     */
    Map<String, Object> getSecurityStatistics();

    // Compliance & Audit
    /**
     * Нэвтрэх аудитын тайлан
     */
    Map<String, Object> generateLoginAuditReport(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Аюулгүй байдлын үйл ажиллагааны тайлан
     */
    Map<String, Object> generateSecurityActivityReport(UUID userId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Нэвтрэх pattern шинжилгээ
     */
    Map<String, Object> analyzeLoginPatterns(UUID userId);

    // Configuration
    /**
     * Аюулгүй байдлын бодлого авах
     */
    Map<String, Object> getSecurityPolicy();

    /**
     * Аюулгүй байдлын бодлого шинэчлэх
     */
    boolean updateSecurityPolicy(Map<String, Object> policy);

    /**
     * JWT тохиргоо авах
     */
    Map<String, Object> getJwtConfiguration();

    // Health check
    /**
     * Authentication сервисийн эрүүл мэнд шалгах
     */
    Map<String, Object> checkAuthServiceHealth();

    /**
     * Аюулгүй байдлын шалгалт
     */
    Map<String, Object> performSecurityHealthCheck();
}