package com.company.los.service.impl;

import org.springframework.beans.factory.annotation.Value;

import com.company.los.dto.AuthResponseDto;
import com.company.los.dto.LoginRequestDto;
import com.company.los.dto.UserDto;
import com.company.los.entity.User;
import com.company.los.entity.Role;
import com.company.los.repository.UserRepository;
import com.company.los.security.JwtUtil;
import com.company.los.service.AuthService;
import com.company.los.util.LogUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * ⭐ ENHANCED Authentication Service Implementation ⭐
 * JWT token ашиглан хэрэглэгчийн баталгаажуулалт
 *
 * @author LOS Development Team
 * @version 3.4 - БҮРЭН ЗАСВАРЛАСАН - Компиляци алдаа арилгагдсан
 * @since 2025-08-04
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    // Token blacklist for invalidating JWTs upon logout.
    // In a production environment, this should be a persistent, distributed cache (e.g., Redis).
    private final Set<String> invalidatedTokens = ConcurrentHashMap.newKeySet();

    /**
     * Кодод hardcode хийсэн демо хэрэглэгчдийг зөвшөөрөх эсэх.
     * Production/docker дээр `false` — нэвтрэлт зөвхөн DB хэрэглэгчээр явагдана.
     */
    @Value("${app.auth.demo-users-enabled:true}")
    private boolean demoUsersEnabled;

    // ⭐ TEST USERS - Development mode (Production-д хасах) ⭐
    private static final Map<String, TestUser> TEST_USERS = new HashMap<>();
    
    static {
        TEST_USERS.put("admin", new TestUser("admin", "admin123", "SUPER_ADMIN", "Системийн админ", "admin@los.mn"));
        TEST_USERS.put("manager", new TestUser("manager", "manager123", "MANAGER", "Салбарын менежер", "manager@los.mn"));
        TEST_USERS.put("loan_officer", new TestUser("loan_officer", "loan123", "LOAN_OFFICER", "Зээлийн мэргэжилтэн", "loan@los.mn"));
        TEST_USERS.put("reviewer", new TestUser("reviewer", "admin123", "DOCUMENT_REVIEWER", "Баримт хянагч", "reviewer@los.mn"));
        TEST_USERS.put("customer_service", new TestUser("customer_service", "admin123", "CUSTOMER_SERVICE", "Харилцагчийн үйлчилгээ", "service@los.mn"));
    }

    /**
     * Хэрэглэгч нэвтрэх (login) үйлдэл
     */
    @Override
    public AuthResponseDto login(LoginRequestDto loginRequest) {
        log.info("🔐 Login attempt for user: {}", LogUtil.maskSensitiveData(loginRequest.getUsername()));
        
        try {
            String username = loginRequest.getUsername();
            String password = loginRequest.getPassword();
            
            if (username == null || password == null || username.trim().isEmpty() || password.trim().isEmpty()) {
                log.error("❌ Username эсвэл password хоосон байна");
                return createFailureResponse("Хэрэглэгчийн нэр болон нууц үг оруулна уу");
            }

            username = username.trim().toLowerCase();

            // 1️⃣ TEST USERS шалгах (зөвхөн demo-users-enabled=true үед)
            TestUser testUser = demoUsersEnabled ? TEST_USERS.get(username) : null;
            if (testUser != null && testUser.password.equals(password)) {
                log.info("✅ Test хэрэглэгчээр амжилттай нэвтэрлээ: {}", username);
                return createTestUserResponse(testUser);
            }

            // 2️⃣ DATABASE USERS шалгах
            Optional<User> userOptional = userRepository.findByUsername(username); 
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                
                // Check if user is active
                if (!user.getIsActive()) {
                    log.warn("⚠️ Хэрэглэгч идэвхгүй байна: {}", username);
                    return createFailureResponse("Хэрэглэгчийн эрх хаагдсан байна");
                }
                
                // Password шалгах
                if (passwordEncoder.matches(password, user.getPasswordHash())) { 
                    
                    log.info("✅ Database хэрэглэгчээр амжилттай нэвтэрлээ: {}", username);
                    
                    // Last login шинэчлэх
                    user.setLastLoginAt(LocalDateTime.now());
                    userRepository.save(user);
                    
                    return createDatabaseUserResponse(user);
                }
            }

            // Хэрэглэгчийн нэр, нууц үг буруу
            log.error("❌ Нэвтрэх амжилтгүй: {}", username);
            return createFailureResponse("Хэрэглэгчийн нэр эсвэл нууц үг буруу байна");

        } catch (BadCredentialsException e) {
            log.error("Нэвтрэлт амжилтгүй. Буруу хэрэглэгчийн нэр эсвэл нууц үг: {}", e.getMessage());
            return createFailureResponse("Хэрэглэгчийн нэр эсвэл нууц үг буруу байна.");
        } catch (Exception e) {
            log.error("❌ Нэвтрэх үед алдаа гарлаа: {}", e.getMessage(), e);
            return createFailureResponse("Системийн алдаа гарлаа. Дахин оролдоно уу.");
        }
    }

    /**
     * Хэрэглэгчийг баталгаажуулах (хуучин authenticateUser функц)
     */
    @Override
    public Map<String, Object> authenticateUser(String username, String password) {
        log.info("🔐 Authentication хүсэлт (хуучин функц): {}", username);
        
        LoginRequestDto loginRequest = new LoginRequestDto();
        loginRequest.setUsername(username);
        loginRequest.setPassword(password);
        
        AuthResponseDto authResponse = login(loginRequest);
        
        Map<String, Object> result = new HashMap<>();
        if (authResponse.isSuccess()) {
            result.put("success", true);
            result.put("token", authResponse.getToken());
            result.put("user", Map.of(
                "id", authResponse.getUserId() != null ? authResponse.getUserId().toString() : generateTestUserId(username).toString(),
                "username", authResponse.getUsername(),
                "role", getRoleString(authResponse.getRoles()), 
                "name", authResponse.getFullName() != null ? authResponse.getFullName() : authResponse.getUsername(),
                "email", authResponse.getEmail() != null ? authResponse.getEmail() : username + "@los.mn"
            ));
            result.put("message", authResponse.getMessage());
        } else {
            result.put("success", false);
            result.put("message", authResponse.getMessage());
        }
        return result;
    }

    /**
     * Хэрэглэгчийг 2FA-тай нэвтрүүлэх (одоогоор хэрэгжүүлээгүй)
     */
    @Override
    public Map<String, Object> authenticateUserWith2FA(String username, String password, String twoFactorCode) {
        // TODO: 2FA implementation
        return authenticateUser(username, password); // Одоогоор 2FA-гүйгээр нэвтрүүлж байна
    }

    /**
     * JWT access token үүсгэх
     */
    @Override
    public String generateJwtToken(UserDetails userDetails) {
        if (jwtUtil != null && userDetails != null) {
            return jwtUtil.generateAccessToken(userDetails);
        }
        return "LOS_TOKEN_" + userDetails.getUsername() + "_" + System.currentTimeMillis();
    }

    /**
     * JWT token баталгаажуулах
     */
    @Override
    public boolean validateJwtToken(String token) {
        // Check if the token is in the blacklist first
        if (invalidatedTokens.contains(token)) {
            log.warn("⚠️ Blacklisted token detected: {}", LogUtil.maskSensitiveData(token));
            return false;
        }
        if (jwtUtil != null) {
            return jwtUtil.isTokenValid(token);
        }
        return token != null && token.startsWith("LOS_TOKEN_");
    }

    /**
     * JWT token-оос хэрэглэгчийн нэр авах
     */
    @Override
    public String getUsernameFromJwtToken(String token) { 
        if (jwtUtil != null) {
            try {
                return jwtUtil.extractUsername(token);
            } catch (Exception e) {
                log.warn("⚠️ JWT username олох алдаа: {}", e.getMessage());
            }
        }
        
        if (token != null && token.startsWith("LOS_TOKEN_")) {
            String[] parts = token.split("_");
            if (parts.length >= 3) {
                return parts[2];
            }
        }
        
        return null;
    }

    /**
     * JWT token-оос хэрэглэгчийн дэлгэрэнгүй мэдээлэл авах
     */
    @Override
    public UserDetails getUserDetailsFromJwtToken(String token) {
        String username = getUsernameFromJwtToken(token);
        if (username != null) {
            return userRepository.findByUsername(username).orElse(null);
        }
        return null;
    }

    /**
     * Token сэргээх (хуучин token-оос шинэ token үүсгэх)
     */
    @Override
    public String refreshJwtToken(String token) {
        String username = getUsernameFromJwtToken(token); 
        if (username != null) {
            if (jwtUtil != null) {
                // UserDetails-ийг ашиглан шинэ токен үүсгэнэ
                UserDetails userDetails = userRepository.findByUsername(username).orElse(null);
                if (userDetails != null) {
                    return jwtUtil.generateAccessToken(userDetails);
                }
            }
            return "LOS_TOKEN_" + username + "_" + System.currentTimeMillis();
        }
        return null;
    }

    /**
     * Refresh token ашиглан шинэ access token авах
     */
    @Override
    public AuthResponseDto refreshToken(String refreshToken) {
        log.info("🔄 Refresh token хүсэлт");
        
        try {
            if (jwtUtil != null && !jwtUtil.isRefreshTokenValid(refreshToken)) {
                return createFailureResponse("Refresh token хүчингүй байна");
            }

            String username = getUsernameFromJwtToken(refreshToken);
            if (username == null) {
                return createFailureResponse("Token-аас хэрэглэгчийн нэр олдсонгүй");
            }

            // Test user эсэхийг шалгах
            TestUser testUser = TEST_USERS.get(username);
            if (testUser != null) {
                return createTestUserResponse(testUser);
            }

            // Database user шалгах
            Optional<User> userOptional = userRepository.findByUsername(username);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                return createDatabaseUserResponse(user);
            }

            return createFailureResponse("Хэрэглэгч олдсонгүй");
            
        } catch (Exception e) {
            log.error("❌ Token сэргээх алдаа: {}", e.getMessage());
            return createFailureResponse("Token сэргээхэд алдаа гарлаа");
        }
    }

    /**
     * Одоогийн хэрэглэгчийг token-оор авах
     */
    @Override
    public Optional<User> getCurrentUser(String token) {
        if (!validateJwtToken(token)) {
            return Optional.empty();
        }
        
        String username = getUsernameFromJwtToken(token);
        if (username == null) {
            return Optional.empty();
        }
        
        // Database-аас хайх
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isPresent()) {
            return userOptional;
        }
        
        // Test user шалгах
        TestUser testUser = TEST_USERS.get(username);
        if (testUser != null) {
            return Optional.of(createUserFromTestUser(testUser));
        }
        
        return Optional.empty();
    }

    /**
     * Хэрэглэгч гарах
     */
    @Override
    public boolean logoutUser(String token) {
        log.info("🚪 Logout хүсэлт");
        
        try {
            // Invalidate the token to prevent its reuse
            if (invalidateToken(token)) {
                String username = getUsernameFromJwtToken(token);
                if (username != null) {
                    log.info("✅ Хэрэглэгч амжилттай гарлаа: {}", username);
                    return true;
                }
            }
        } catch (Exception e) {
            log.error("❌ Logout алдаа: {}", e.getMessage());
        }
        
        return false;
    }

    /**
     * ⭐ ЗАСВАРЛАСАН: Бүх төхөөрөмжөөс гарах - forceLogoutUser болгосон ⭐
     */
    @Override 
    public boolean forceLogoutUser(UUID userId) {
        log.info("🚪 Force logout user from all devices: {}", userId);
        // TODO: Implement actual force logout logic (e.g., invalidate all sessions/tokens for this user)
        return true; 
    }

    /**
     * Token хүчингүй болгох (blacklist-д нэмэх)
     */
    @Override 
    public boolean invalidateToken(String token) { 
        if (token == null || token.trim().isEmpty()) {
            return false;
        }
        invalidatedTokens.add(token);
        log.info("🚫 Token invalidated and added to blacklist: {}", LogUtil.maskSensitiveData(token));
        return true; 
    }

    /**
     * Нууц үг солих
     */
    @Override
    public boolean changePassword(UUID userId, String currentPassword, String newPassword) {
        try {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Хэрэглэгч олдсонгүй"));

            // Current password шалгах
            if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) { 
                throw new RuntimeException("Одоогийн нууц үг буруу байна");
            }

            // New password encode хийж хадгалах
            user.setPasswordHash(passwordEncoder.encode(newPassword)); 
            user.setPasswordChangedAt(LocalDateTime.now());
            userRepository.save(user);
            
            log.info("✅ Нууц үг амжилттай солигдлоо: {}", user.getUsername());
            return true;
            
        } catch (Exception e) {
            log.error("❌ Нууц үг солих алдаа: {}", e.getMessage());
            return false;
        }
    }

    // ==================== HELPER METHODS ====================

    /**
     * Туршилтын хэрэглэгчийн хариу үүсгэх
     */
    private AuthResponseDto createTestUserResponse(TestUser testUser) {
        // TestUser-ээс UserDetails үүсгэх
        UserDetails userDetails = createUserFromTestUser(testUser);
        String accessToken = jwtUtil.generateAccessToken(userDetails); 
        String refreshToken = jwtUtil.generateRefreshToken(userDetails); 
        
        AuthResponseDto response = new AuthResponseDto();
        response.setSuccess(true);
        response.setToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setTokenType("Bearer");
        response.setExpiresIn(jwtUtil.getAccessTokenExpiration()); 
        response.setUserId(generateTestUserId(testUser.username));
        response.setUsername(testUser.username);
        response.setEmail(testUser.email);
        response.setFullName(testUser.fullName);
        response.setRoles(createTestUserRoles(testUser.role)); 
        response.setMessage("Амжилттай нэвтэрлээ");
        
        return response;
    }

    /**
     * Өгөгдлийн сангаас авсан хэрэглэгчийн хариу үүсгэх
     */
    private AuthResponseDto createDatabaseUserResponse(User user) {
        String accessToken = jwtUtil.generateAccessToken(user); 
        String refreshToken = jwtUtil.generateRefreshToken(user); 
        
        AuthResponseDto response = new AuthResponseDto();
        response.setSuccess(true);
        response.setToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setTokenType("Bearer");
        response.setExpiresIn(jwtUtil.getAccessTokenExpiration()); 
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setFullName(user.getFirstName() + " " + user.getLastName());
        // User entity-ийн roles талбарыг Set<Role> болгож өөрчилсөн тул шууд дамжуулна
        response.setRoles(new HashSet<>(user.getRoles())); 
        response.setMessage("Амжилттай нэвтэрлээ");
        
        return response;
    }

    /**
     * Spring Security-ээр баталгаажсан хэрэглэгчийн хариу үүсгэх
     */
    private AuthResponseDto createSpringSecurityResponse(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String accessToken = jwtUtil.generateAccessToken(userDetails); 
        String refreshToken = jwtUtil.generateRefreshToken(userDetails); 
        
        AuthResponseDto response = new AuthResponseDto();
        response.setSuccess(true);
        response.setToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setTokenType("Bearer");
        response.setExpiresIn(jwtUtil.getAccessTokenExpiration());
        response.setUsername(userDetails.getUsername());
        response.setEmail(userDetails.getUsername() + "@los.mn"); 
        response.setFullName(userDetails.getUsername()); 
        response.setMessage("Амжилттай нэвтэрлээ");
        
        return response;
    }

    /**
     * Амжилтгүй хариу үүсгэх
     */
    private AuthResponseDto createFailureResponse(String message) {
        AuthResponseDto response = new AuthResponseDto();
        response.setSuccess(false);
        response.setMessage(message);
        return response;
    }

    /**
     * Туршилтын хэрэглэгчийн дүрүүдийг үүсгэх
     */
    private Set<Role> createTestUserRoles(String roleName) {
        Role role = new Role();
        role.setName(roleName);
        // Бусад шаардлагатай талбаруудыг тохируулж болно
        return Set.of(role); 
    }

    /**
     * Хэрэглэгчийн дүрүүдийг String-ээр авах
     * AuthResponseDto-д Set<Role> байгаа тул Set<Role> хүлээн авахаар өөрчилсөн
     */
    private String getRoleString(Set<Role> roles) { 
        if (roles == null || roles.isEmpty()) {
            return "USER";
        }
        // Эхний дүрийн нэрийг буцаана
        return roles.iterator().next().getName(); 
    }

    /**
     * Туршилтын хэрэглэгчийн ID үүсгэх
     */
    private UUID generateTestUserId(String username) {
        // Username-д тулгуурлан тогтмол UUID үүсгэх
        switch (username.toLowerCase()) {
            case "admin":
                return UUID.fromString("33333333-3333-3333-3333-333333333301");
            case "manager":
                return UUID.fromString("33333333-3333-3333-3333-333333333302");
            case "loan_officer":
                return UUID.fromString("33333333-3333-3333-3333-333333333303");
            case "reviewer":
                return UUID.fromString("33333333-3333-3333-3333-333333333304");
            case "customer_service":
                return UUID.fromString("33333333-3333-3333-3333-333333333305");
            default:
                return UUID.randomUUID();
        }
    }

    /**
     * Туршилтын хэрэглэгчийн мэдээллээс User объект үүсгэх
     */
    private User createUserFromTestUser(TestUser testUser) {
        User user = new User();
        user.setId(generateTestUserId(testUser.username));
        user.setUsername(testUser.username);
        user.setEmail(testUser.email);
        user.setFirstName(testUser.fullName.split(" ")[0]);
        user.setLastName(testUser.fullName.contains(" ") ? testUser.fullName.split(" ", 2)[1] : "");
        user.setIsActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setLastLoginAt(LocalDateTime.now());
        user.setPasswordHash(passwordEncoder.encode(testUser.password)); 
        Role testRole = new Role();
        testRole.setName(testUser.role);
        user.setRoles(Collections.singletonList(testRole)); 
        return user;
    }

    // ==================== TEST USER CLASS ====================
    
    private static class TestUser {
        final String username;
        final String password;
        final String role;
        final String fullName;
        final String email;
        
        TestUser(String username, String password, String role, String fullName, String email) {
            this.username = username;
            this.password = password;
            this.role = role;
            this.fullName = fullName;
            this.email = email;
        }
    }

    // ==================== STUB IMPLEMENTATIONS ====================
    // AuthService interface-ийн бусад методуудын default implementation
    
    @Override public boolean logoutUserFromAllDevices(UUID userId) { return true; }
    // invalidateToken method is implemented above
    @Override public boolean requestPasswordReset(String email) { return true; }
    @Override public boolean resetPassword(String resetToken, String newPassword) { return true; }
    @Override public boolean validatePasswordResetToken(String token) { return true; }
    @Override public String generateTemporaryPassword() { return "temp123"; }
    @Override public UserDto registerUser(UserDto userDto) { return userDto; }
    @Override public boolean activateUser(String activationToken) { return true; }
    @Override public boolean resendActivationCode(String email) { return true; }
    @Override public Map<String, Object> enableTwoFactorAuthentication(UUID userId) { return new HashMap<>(); }
    @Override public boolean disableTwoFactorAuthentication(UUID userId, String currentPassword) { return true; }
    @Override public boolean validateTwoFactorCode(UUID userId, String code) { return true; }
    @Override public String[] generateBackupCodes(UUID userId) { return new String[]{}; }
    @Override public boolean useTwoFactorBackupCode(UUID userId, String backupCode) { return true; }
    @Override public byte[] generateQRCode(UUID userId) { return new byte[]{}; }
    @Override public Map<String, Object> getActiveSessions(UUID userId) { return new HashMap<>(); }
    @Override public boolean terminateSession(UUID userId, String sessionId) { return true; }
    @Override public boolean terminateAllSessions(UUID userId) { return true; }
    @Override public boolean updateSessionActivity(String token) { return true; }
    @Override public boolean lockAccount(UUID userId, String reason, LocalDateTime until) { return true; }
    @Override public boolean unlockAccount(UUID userId) { return true; }
    @Override public boolean suspendAccount(UUID userId, String reason) { return true; }
    @Override public boolean activateAccount(UUID userId) { return true; }
    @Override public void recordFailedLoginAttempt(String username, String ipAddress) {}
    @Override public void recordSuccessfulLogin(String username, String ipAddress) {}
    @Override public boolean isRateLimited(String username, String ipAddress) { return false; }
    @Override public boolean detectSuspiciousLoginActivity(String username, String ipAddress) { return false; }
    @Override public Map<String, Object> validatePasswordStrength(String password) { return new HashMap<>(); }
    @Override public boolean isIpAddressAllowed(String ipAddress) { return true; }
    @Override public boolean isDeviceTrusted(UUID userId, String deviceFingerprint) { return true; }
    @Override public boolean registerTrustedDevice(UUID userId, String deviceFingerprint, String deviceName) { return true; }
    @Override public boolean sendEmailVerification(UUID userId) { return true; }
    @Override public boolean verifyEmail(String verificationToken) { return true; }
    @Override public boolean sendPhoneVerification(UUID userId) { return true; }
    @Override public boolean verifyPhone(UUID userId, String verificationCode) { return true; }
    @Override public boolean sendSecurityAlert(UUID userId, String alertType, String details) { return true; }
    @Override public boolean sendPasswordChangeNotification(UUID userId) { return true; }
    @Override public boolean sendNewDeviceLoginNotification(UUID userId, String deviceInfo) { return true; }
    @Override public boolean sendSuspiciousActivityNotification(UUID userId, String activityDetails) { return true; }
    @Override public Map<String, Object> getLoginHistory(UUID userId, int days) { return new HashMap<>(); }
    @Override public Map<String, Object> getSecurityLog(UUID userId, int days) { return new HashMap<>(); }
    @Override public Map<String, Object> getFailedLoginAttempts(String username, int hours) { return new HashMap<>(); }
    @Override public Map<String, Object> getActiveTokens(UUID userId) { return new HashMap<>(); }
    @Override public Map<String, Object> getTokenInfo(String token) { return new HashMap<>(); }
    @Override public String extendTokenExpiry(String token) { return token; }
    @Override public int cleanupExpiredTokens() { return 0; }
    @Override public String generateApiKey(UUID userId, String description, LocalDateTime expiresAt) { return "api-key"; }
    @Override public boolean validateApiKey(String apiKey) { return true; }
    @Override public boolean revokeApiKey(String apiKey) { return true; }
    @Override public Map<String, Object> getUserApiKeys(UUID userId) { return new HashMap<>(); }
    @Override public int getActiveUserCount() { return 1; } 
    @Override public Map<String, Object> getOnlineUsers() { return new HashMap<>(); }
    @Override public Map<String, Object> getSecurityStatistics() { return new HashMap<>(); }
    @Override public Map<String, Object> generateLoginAuditReport(LocalDateTime startDate, LocalDateTime endDate) { return new HashMap<>(); }
    @Override public Map<String, Object> generateSecurityActivityReport(UUID userId, LocalDateTime startDate, LocalDateTime endDate) { return new HashMap<>(); }
    @Override public Map<String, Object> analyzeLoginPatterns(UUID userId) { return new HashMap<>(); }
    @Override public Map<String, Object> getSecurityPolicy() { return new HashMap<>(); }
    @Override public boolean updateSecurityPolicy(Map<String, Object> policy) { return true; }
    @Override public Map<String, Object> getJwtConfiguration() { return new HashMap<>(); }
    @Override public Map<String, Object> checkAuthServiceHealth() { return new HashMap<>(); }
    @Override public Map<String, Object> performSecurityHealthCheck() { return new HashMap<>(); }
}