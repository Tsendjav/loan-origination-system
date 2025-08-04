package com.company.los.dto;

import com.company.los.entity.Role;

import java.util.Set;
import java.util.UUID;

/**
 * Authentication Response DTO
 * Login хариуд буцаах мэдээлэл
 * ⭐ ЗАСВАРЛАСАН - Manual Setters/Getters нэмэгдсэн ⭐
 * * @author LOS Development Team
 * @version 2.2 - Manual Implementation
 * @since 2025-08-01
 */
public class AuthResponseDto {

    /**
     * JWT access token
     */
    private String token;

    /**
     * Refresh token
     */
    private String refreshToken;

    /**
     * Token төрөл (Bearer)
     */
    private String tokenType = "Bearer";

    /**
     * Token-ийн хүчинтэй хугацаа (секунд)
     */
    private Long expiresIn;

    /**
     * Хэрэглэгчийн ID (UUID)
     */
    private UUID userId;

    /**
     * Хэрэглэгчийн нэр
     */
    private String username;

    /**
     * Хэрэглэгчийн имэйл
     */
    private String email;

    /**
     * Хэрэглэгчийн дүрүүд
     */
    private Set<Role> roles; // Set<Role>-ийг Set<String>-ээр сольсон

    /**
     * Нэвтрэлт амжилттай эсэх
     */
    private boolean success = true;

    /**
     * Хариуны мессеж
     */
    private String message;

    /**
     * Хэрэглэгчийн овог нэр
     */
    private String fullName;

    /**
     * Хэрэглэгчийн зураг
     */
    private String profileImage;

    /**
     * Анхны нэвтрэлт эсэх
     */
    private boolean firstLogin = false;

    /**
     * Нууц үг солих шаардлагатай эсэх
     */
    private boolean passwordChangeRequired = false;

    // ==================== CONSTRUCTORS ====================

    public AuthResponseDto() {
    }

    public AuthResponseDto(String token, String refreshToken, String tokenType, Long expiresIn, 
                          UUID userId, String username, String email, Set<Role> roles, 
                          boolean success, String message, String fullName, String profileImage, 
                          boolean firstLogin, boolean passwordChangeRequired) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.roles = roles;
        this.success = success;
        this.message = message;
        this.fullName = fullName;
        this.profileImage = profileImage;
        this.firstLogin = firstLogin;
        this.passwordChangeRequired = passwordChangeRequired;
    }

    // ==================== MANUAL GETTERS ====================

    public String getToken() { return token; }
    public String getRefreshToken() { return refreshToken; }
    public String getTokenType() { return tokenType; }
    public Long getExpiresIn() { return expiresIn; }
    public UUID getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public Set<Role> getRoles() { return roles; }
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public String getFullName() { return fullName; }
    public String getProfileImage() { return profileImage; }
    public boolean isFirstLogin() { return firstLogin; }
    public boolean isPasswordChangeRequired() { return passwordChangeRequired; }

    // ==================== MANUAL SETTERS ====================

    public void setToken(String token) { this.token = token; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }
    public void setExpiresIn(Long expiresIn) { this.expiresIn = expiresIn; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public void setUsername(String username) { this.username = username; }
    public void setEmail(String email) { this.email = email; }
    public void setRoles(Set<Role> roles) { this.roles = roles; }
    public void setSuccess(boolean success) { this.success = success; }
    public void setMessage(String message) { this.message = message; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setProfileImage(String profileImage) { this.profileImage = profileImage; }
    public void setFirstLogin(boolean firstLogin) { this.firstLogin = firstLogin; }
    public void setPasswordChangeRequired(boolean passwordChangeRequired) { this.passwordChangeRequired = passwordChangeRequired; }

    // ==================== STATIC FACTORY METHODS ====================

    /**
     * Амжилттай хариу үүсгэх - Builder pattern-ийг орлуулах static method
     */
    public static AuthResponseDto success(String token, String refreshToken, Long expiresIn, 
                                        UUID userId, String username, String email, Set<Role> roles) {
        AuthResponseDto response = new AuthResponseDto();
        response.setToken(token);
        response.setRefreshToken(refreshToken);
        response.setTokenType("Bearer");
        response.setExpiresIn(expiresIn);
        response.setUserId(userId);
        response.setUsername(username);
        response.setEmail(email);
        response.setRoles(roles);
        response.setSuccess(true);
        response.setMessage("Login successful");
        return response;
    }

    /**
     * Амжилттай хариу үүсгэх - бүрэн мэдээлэлтэй
     */
    public static AuthResponseDto successWithDetails(String token, String refreshToken, Long expiresIn, 
                                                   UUID userId, String username, String email, 
                                                   Set<Role> roles, String fullName, String message) {
        AuthResponseDto response = new AuthResponseDto();
        response.setToken(token);
        response.setRefreshToken(refreshToken);
        response.setTokenType("Bearer");
        response.setExpiresIn(expiresIn);
        response.setUserId(userId);
        response.setUsername(username);
        response.setEmail(email);
        response.setRoles(roles);
        response.setSuccess(true);
        response.setFullName(fullName);
        response.setMessage(message);
        return response;
    }

    /**
     * Амжилтгүй хариу үүсгэх
     */
    public static AuthResponseDto failure(String message) {
        AuthResponseDto response = new AuthResponseDto();
        response.setSuccess(false);
        response.setMessage(message);
        response.setTokenType("Bearer");
        return response;
    }

    // ==================== BUILDER-STYLE METHODS ====================

    /**
     * Builder-style methods - method chaining
     */
    public AuthResponseDto withToken(String token) {
        this.setToken(token);
        return this;
    }

    public AuthResponseDto withRefreshToken(String refreshToken) {
        this.setRefreshToken(refreshToken);
        return this;
    }

    public AuthResponseDto withUserId(UUID userId) {
        this.setUserId(userId);
        return this;
    }

    public AuthResponseDto withUsername(String username) {
        this.setUsername(username);
        return this;
    }

    public AuthResponseDto withEmail(String email) {
        this.setEmail(email);
        return this;
    }

    public AuthResponseDto withRoles(Set<Role> roles) {
        this.setRoles(roles);
        return this;
    }

    public AuthResponseDto withFullName(String fullName) {
        this.setFullName(fullName);
        return this;
    }

    public AuthResponseDto withMessage(String message) {
        this.setMessage(message);
        return this;
    }

    public AuthResponseDto withExpiresIn(Long expiresIn) {
        this.setExpiresIn(expiresIn);
        return this;
    }

    public AuthResponseDto withFirstLogin(boolean firstLogin) {
        this.setFirstLogin(firstLogin);
        return this;
    }

    public AuthResponseDto withPasswordChangeRequired(boolean passwordChangeRequired) {
        this.setPasswordChangeRequired(passwordChangeRequired);
        return this;
    }

    // ==================== VALIDATION METHODS ====================

    /**
     * Validation методууд
     */
    public boolean hasValidToken() {
        return token != null && !token.trim().isEmpty();
    }

    public boolean hasValidUser() {
        return username != null && !username.trim().isEmpty();
    }

    public boolean hasValidEmail() {
        return email != null && email.contains("@");
    }

    public boolean hasRoles() {
        return roles != null && !roles.isEmpty();
    }

    // ==================== OVERRIDE METHODS ====================

    /**
     * toString method - нууц мэдээлэл харуулахгүй
     */
    @Override
    public String toString() {
        return "AuthResponseDto{" +
                "success=" + success +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", fullName='" + fullName + '\'' +
                ", tokenType='" + tokenType + '\'' +
                ", expiresIn=" + expiresIn +
                ", userId=" + userId +
                ", rolesCount=" + (roles != null ? roles.size() : 0) +
                ", message='" + message + '\'' +
                ", hasToken=" + (token != null && !token.isEmpty()) +
                ", hasRefreshToken=" + (refreshToken != null && !refreshToken.isEmpty()) +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        AuthResponseDto that = (AuthResponseDto) obj;
        
        if (success != that.success) return false;
        if (firstLogin != that.firstLogin) return false;
        if (passwordChangeRequired != that.passwordChangeRequired) return false;
        if (token != null ? !token.equals(that.token) : that.token != null) return false;
        if (refreshToken != null ? !refreshToken.equals(that.refreshToken) : that.refreshToken != null) return false;
        if (tokenType != null ? !tokenType.equals(that.tokenType) : that.tokenType != null) return false;
        if (expiresIn != null ? !expiresIn.equals(that.expiresIn) : that.expiresIn != null) return false;
        if (userId != null ? !userId.equals(that.userId) : that.userId != null) return false;
        if (username != null ? !username.equals(that.username) : that.username != null) return false;
        if (email != null ? !email.equals(that.email) : that.email != null) return false;
        if (roles != null ? !roles.equals(that.roles) : that.roles != null) return false;
        if (message != null ? !message.equals(that.message) : that.message != null) return false;
        if (fullName != null ? !fullName.equals(that.fullName) : that.fullName != null) return false;
        return profileImage != null ? profileImage.equals(that.profileImage) : that.profileImage == null;
    }

    @Override
    public int hashCode() {
        int result = token != null ? token.hashCode() : 0;
        result = 31 * result + (refreshToken != null ? refreshToken.hashCode() : 0);
        result = 31 * result + (tokenType != null ? tokenType.hashCode() : 0);
        result = 31 * result + (expiresIn != null ? expiresIn.hashCode() : 0);
        result = 31 * result + (userId != null ? userId.hashCode() : 0);
        result = 31 * result + (username != null ? username.hashCode() : 0);
        result = 31 * result + (email != null ? email.hashCode() : 0);
        result = 31 * result + (roles != null ? roles.hashCode() : 0);
        result = 31 * result + (success ? 1 : 0);
        result = 31 * result + (message != null ? message.hashCode() : 0);
        result = 31 * result + (fullName != null ? fullName.hashCode() : 0);
        result = 31 * result + (profileImage != null ? profileImage.hashCode() : 0);
        result = 31 * result + (firstLogin ? 1 : 0);
        result = 31 * result + (passwordChangeRequired ? 1 : 0);
        return result;
    }
}