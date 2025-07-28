package com.company.los.dto;

import com.company.los.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

/**
 * Authentication Response DTO
 * Login хариуд буцаах мэдээлэл
 */
@Data
@Builder
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
    @Builder.Default
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
    @Builder.Default
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
    @Builder.Default
    private boolean firstLogin = false;

    /**
     * Нууц үг солих шаардлагатай эсэх
     */
    @Builder.Default
    private boolean passwordChangeRequired = false;

    // Getter методууд (Lombok @Data автоматаар үүсгэдэг ч explicit хийе)
    public boolean isSuccess() {
        return success;
    }

    public String getToken() {
        return token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public String getMessage() {
        return message;
    }

    /**
     * Амжилттай хариу үүсгэх
     */
    public static AuthResponseDto success(String token, String refreshToken, Long expiresIn, 
                                        UUID userId, String username, String email, Set<Role> roles) {
        return AuthResponseDto.builder()
                .success(true)
                .token(token)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .userId(userId)
                .username(username)
                .email(email)
                .roles(roles)
                .message("Login successful")
                .build();
    }

    /**
     * Амжилтгүй хариу үүсгэх
     */
    public static AuthResponseDto failure(String message) {
        return AuthResponseDto.builder()
                .success(false)
                .message(message)
                .build();
    }
}