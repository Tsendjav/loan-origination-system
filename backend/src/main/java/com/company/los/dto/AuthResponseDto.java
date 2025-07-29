package com.company.los.dto;

import com.company.los.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

/**
 * Authentication Response DTO
 * Login хариуд буцаах мэдээлэл
 * 
 * @author LOS Development Team
 * @version 2.1 - Builder Pattern Removed for Compatibility
 * @since 2025-07-28
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
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
    private Set<Role> roles;

    /**
     * Нэвтрэх амжилттай эсэх
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
}