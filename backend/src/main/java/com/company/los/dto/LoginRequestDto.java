package com.company.los.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * Login хүсэлтийн DTO
 * Хэрэглэгчийн нэвтрэх мэдээллийг хүлээн авах
 * 
 * @author LOS Development Team
 * @version 2.1 - Builder Pattern Removed for Compatibility
 * @since 2025-07-28
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginRequestDto implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Хэрэглэгчийн нэр эсвэл имэйл хаяг
     * Username эсвэл email зөвхөн тэмдэгт, тоо, дэд зураас, цэг ашиглах боломжтой
     */
    @NotBlank(message = "Хэрэглэгчийн нэр эсвэл имэйл заавал бөглөх ёстой")
    @Size(min = 3, max = 50, message = "Хэрэглэгчийн нэр 3-50 тэмдэгтийн урттай байх ёстой")
    @Pattern(
        regexp = "^[a-zA-Z0-9._@-]+$", 
        message = "Хэрэглэгчийн нэр зөвхөн үсэг, тоо, цэг, дэд зураас, @ тэмдэг агуулах боломжтой"
    )
    private String username;

    /**
     * Нууц үг
     * Хамгийн багадаа 6 тэмдэгт, дээд тал нь 100 тэмдэгт
     */
    @NotBlank(message = "Нууц үг заавал бөглөх ёстой")
    @Size(min = 6, max = 100, message = "Нууц үг 6-100 тэмдэгтийн урттай байх ёстой")
    private String password;

    /**
     * Намайг санах сонголт
     * Удаан хугацаанд нэвтэрсэн байдалд үлдэх
     */
    private boolean rememberMe = false;

    /**
     * Device мэдээлэл (optional)
     * Аюулгүй байдлын log-д ашиглах
     */
    private String deviceInfo;

    /**
     * User Agent мэдээлэл (optional)
     * Browser болон OS мэдээлэл
     */
    private String userAgent;

    /**
     * IP хаяг (optional)
     * Автоматаар гарч ирэх, гар аргаар оруулах шаардлагагүй
     */
    private String ipAddress;

    /**
     * Login attempt хугацаа (optional)
     * Frontend-ээс timestamp илгээж болно
     */
    private Long timestamp;

    /**
     * Client версийн мэдээлэл (optional)
     * Mobile app эсвэл web client верси
     */
    private String clientVersion;

    /**
     * Timezone мэдээлэл (optional)
     * Хэрэглэгчийн цагийн бүс
     */
    private String timezone;

    /**
     * Platform мэдээлэл (optional)
     * WEB, MOBILE_ANDROID, MOBILE_IOS
     */
    private String platform;

    /**
     * Хэрэглэгчийн нэр эсвэл имэйл эсэхийг шалгах
     * @ тэмдэг агуулж байвал имэйл гэж үзнэ
     */
    @JsonIgnore
    public boolean isEmail() {
        return username != null && username.contains("@") && username.indexOf("@") > 0;
    }

    /**
     * Username эсэхийг шалгах
     */
    @JsonIgnore
    public boolean isUsername() {
        return !isEmail();
    }

    /**
     * Нууц үгийг цэвэрлэх (security-ын төлөө)
     * Санах ой дээрээс нууц үгийг устгах
     */
    public void clearPassword() {
        this.password = null;
    }

    /**
     * Хүсэлт валид эсэхийг шалгах
     * Manual validation хийх
     */
    @JsonIgnore
    public boolean isValid() {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        if (password == null || password.trim().isEmpty()) {
            return false;
        }
        if (username.length() < 3 || username.length() > 50) {
            return false;
        }
        if (password.length() < 6 || password.length() > 100) {
            return false;
        }
        // Username pattern шалгах
        return username.matches("^[a-zA-Z0-9._@-]+$");
    }

    /**
     * Timestamp үүсгэх (хэрэв байхгүй бол)
     */
    public void ensureTimestamp() {
        if (this.timestamp == null) {
            this.timestamp = System.currentTimeMillis();
        }
    }

    /**
     * Default platform тохируулах
     */
    public void ensurePlatform() {
        if (this.platform == null || this.platform.trim().isEmpty()) {
            this.platform = "WEB";
        }
    }

    /**
     * Normalize username (trim + lowercase for email)
     */
    public void normalizeUsername() {
        if (username != null) {
            username = username.trim();
            if (isEmail()) {
                username = username.toLowerCase();
            }
        }
    }

    /**
     * Audit мэдээлэл бэлтгэх
     */
    @JsonIgnore
    public String getAuditInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("user=").append(username);
        if (ipAddress != null) sb.append(", ip=").append(ipAddress);
        if (deviceInfo != null) sb.append(", device=").append(deviceInfo);
        if (platform != null) sb.append(", platform=").append(platform);
        return sb.toString();
    }

    // ==================== Static Factory Methods ====================

    /**
     * Static factory method - энгийн хүсэлт
     */
    public static LoginRequestDto of(String username, String password) {
        LoginRequestDto request = new LoginRequestDto();
        request.setUsername(username);
        request.setPassword(password);
        request.setRememberMe(false);
        request.setTimestamp(System.currentTimeMillis());
        request.setPlatform("WEB");
        return request;
    }

    /**
     * Static factory method - remember me тэй
     */
    public static LoginRequestDto withRememberMe(String username, String password, boolean rememberMe) {
        LoginRequestDto request = new LoginRequestDto();
        request.setUsername(username);
        request.setPassword(password);
        request.setRememberMe(rememberMe);
        request.setTimestamp(System.currentTimeMillis());
        request.setPlatform("WEB");
        return request;
    }

    /**
     * Static factory method - бүрэн мэдээлэлтэй
     */
    public static LoginRequestDto withFullInfo(String username, String password, boolean rememberMe, 
                                             String deviceInfo, String ipAddress, String platform) {
        LoginRequestDto request = new LoginRequestDto();
        request.setUsername(username);
        request.setPassword(password);
        request.setRememberMe(rememberMe);
        request.setDeviceInfo(deviceInfo);
        request.setIpAddress(ipAddress);
        request.setPlatform(platform);
        request.setTimestamp(System.currentTimeMillis());
        return request;
    }

    /**
     * Mobile app-аас ирэх хүсэлт
     */
    public static LoginRequestDto forMobile(String username, String password, String platform, 
                                          String clientVersion, String deviceInfo) {
        LoginRequestDto request = new LoginRequestDto();
        request.setUsername(username);
        request.setPassword(password);
        request.setRememberMe(true); // Mobile-д ихэвчлэн remember me байдаг
        request.setPlatform(platform);
        request.setClientVersion(clientVersion);
        request.setDeviceInfo(deviceInfo);
        request.setTimestamp(System.currentTimeMillis());
        return request;
    }

    /**
     * Builder-style method chaining
     */
    public LoginRequestDto withUsername(String username) {
        this.setUsername(username);
        return this;
    }

    public LoginRequestDto withPassword(String password) {
        this.setPassword(password);
        return this;
    }

    public LoginRequestDto withRememberMe(boolean rememberMe) {
        this.setRememberMe(rememberMe);
        return this;
    }

    public LoginRequestDto withDeviceInfo(String deviceInfo) {
        this.setDeviceInfo(deviceInfo);
        return this;
    }

    public LoginRequestDto withUserAgent(String userAgent) {
        this.setUserAgent(userAgent);
        return this;
    }

    public LoginRequestDto withIpAddress(String ipAddress) {
        this.setIpAddress(ipAddress);
        return this;
    }

    public LoginRequestDto withTimestamp(Long timestamp) {
        this.setTimestamp(timestamp);
        return this;
    }

    public LoginRequestDto withClientVersion(String clientVersion) {
        this.setClientVersion(clientVersion);
        return this;
    }

    public LoginRequestDto withTimezone(String timezone) {
        this.setTimezone(timezone);
        return this;
    }

    public LoginRequestDto withPlatform(String platform) {
        this.setPlatform(platform);
        return this;
    }

    // ==================== toString Override ====================

    /**
     * ToString method (нууц үг харуулахгүй)
     * Security-ын зорилгоор password-ийг log-д бичихгүй
     */
    @Override
    public String toString() {
        return "LoginRequestDto{" +
                "username='" + username + '\'' +
                ", rememberMe=" + rememberMe +
                ", deviceInfo='" + deviceInfo + '\'' +
                ", userAgent='" + userAgent + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                ", timestamp=" + timestamp +
                ", clientVersion='" + clientVersion + '\'' +
                ", timezone='" + timezone + '\'' +
                ", platform='" + platform + '\'' +
                ", passwordProvided=" + (password != null && !password.isEmpty()) +
                '}';
    }

    // ==================== Equals & HashCode ====================

    /**
     * Equals method (нууц үг ашиглахгүй)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        LoginRequestDto that = (LoginRequestDto) obj;
        return rememberMe == that.rememberMe &&
               username != null ? username.equals(that.username) : that.username == null;
    }

    /**
     * HashCode method (нууц үг ашиглахгүй)
     */
    @Override
    public int hashCode() {
        int result = username != null ? username.hashCode() : 0;
        result = 31 * result + (rememberMe ? 1 : 0);
        return result;
    }

    // ==================== Nested Classes ====================

    /**
     * Platform constants
     */
    public static final class Platform {
        public static final String WEB = "WEB";
        public static final String MOBILE_ANDROID = "MOBILE_ANDROID";
        public static final String MOBILE_IOS = "MOBILE_IOS";
        public static final String DESKTOP = "DESKTOP";
        
        private Platform() {
            // Utility class
        }
    }

    /**
     * Validation helper
     */
    public static final class Validator {
        
        /**
         * Username format шалгах
         */
        public static boolean isValidUsername(String username) {
            if (username == null || username.trim().isEmpty()) return false;
            if (username.length() < 3 || username.length() > 50) return false;
            return username.matches("^[a-zA-Z0-9._@-]+$");
        }
        
        /**
         * Password format шалгах
         */
        public static boolean isValidPassword(String password) {
            if (password == null || password.trim().isEmpty()) return false;
            return password.length() >= 6 && password.length() <= 100;
        }
        
        /**
         * Email format шалгах
         */
        public static boolean isValidEmail(String email) {
            if (email == null || email.trim().isEmpty()) return false;
            return email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
        }
        
        private Validator() {
            // Utility class
        }
    }
}