package com.company.los.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * Login хүсэлтийн DTO - ЭЦСИЙН САЙЖРУУЛСАН ХУВИЛБАР
 * ⭐ MANUAL SETTERS/GETTERS НЭМЭГДСЭН ⭐
 * ⭐ CHARACTER ENCODING АЛДАА ШИЙДЭГДСЭН ⭐
 * ⭐ VALIDATION САЙЖРУУЛСАН ⭐
 * Хэрэглэгчийн нэвтрэх мэдээллийг хүлээн авах
 * 
 * @author LOS Development Team
 * @version 2.4 - Final Implementation
 * @since 2025-08-01
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginRequestDto implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Хэрэглэгчийн нэр эсвэл имэйл хаяг
     */
    @NotBlank(message = "Хэрэглэгчийн нэр заавал бөглөх ёстой")
    @Size(min = 3, max = 50, message = "Хэрэглэгчийн нэр 3-50 тэмдэгт байх ёстой")
    @Pattern(
        regexp = "^[a-zA-Z0-9._@-]+$", 
        message = "Хэрэглэгчийн нэр зөвхөн үсэг, тоо, цэг, дэд зураас, @ тэмдэг агуулах боломжтой"
    )
    private String username;

    /**
     * Нууц үг
     */
    @NotBlank(message = "Нууц үг заавал бөглөх ёстой")
    @Size(min = 6, max = 100, message = "Нууц үг 6-100 тэмдэгт байх ёстой")
    private String password;

    /**
     * Намайг санах сонголт
     */
    private boolean rememberMe = false;

    /**
     * Device мэдээлэл (optional)
     */
    private String deviceInfo;

    /**
     * User Agent мэдээлэл (optional)
     */
    private String userAgent;

    /**
     * IP хаяг (optional)
     */
    private String ipAddress;

    /**
     * Login attempt хугацаа (optional)
     */
    private Long timestamp;

    /**
     * Client версийн мэдээлэл (optional)
     */
    private String clientVersion;

    /**
     * Timezone мэдээлэл (optional)
     */
    private String timezone;

    /**
     * Platform мэдээлэл (optional)
     */
    private String platform;

    // ==================== CONSTRUCTORS ====================

    /**
     * Default constructor for deserialization
     */
    public LoginRequestDto() {
    }

    /**
     * Constructor with basic fields
     */
    public LoginRequestDto(String username, String password) {
        this.username = username != null ? username.trim() : null;
        this.password = password;
        this.rememberMe = false;
        this.timestamp = System.currentTimeMillis();
        this.platform = "WEB";
    }

    /**
     * Constructor with remember me option
     */
    public LoginRequestDto(String username, String password, boolean rememberMe) {
        this.username = username != null ? username.trim() : null;
        this.password = password;
        this.rememberMe = rememberMe;
        this.timestamp = System.currentTimeMillis();
        this.platform = "WEB";
    }

    /**
     * Full constructor
     */
    public LoginRequestDto(String username, String password, boolean rememberMe, String deviceInfo, 
                          String userAgent, String ipAddress, Long timestamp, String clientVersion, 
                          String timezone, String platform) {
        this.username = username != null ? username.trim() : null;
        this.password = password;
        this.rememberMe = rememberMe;
        this.deviceInfo = deviceInfo != null ? deviceInfo.trim() : null;
        this.userAgent = userAgent != null ? userAgent.trim() : null;
        this.ipAddress = ipAddress != null ? ipAddress.trim() : null;
        this.timestamp = timestamp != null ? timestamp : System.currentTimeMillis();
        this.clientVersion = clientVersion != null ? clientVersion.trim() : null;
        this.timezone = timezone != null ? timezone.trim() : null;
        this.platform = platform != null ? platform.trim() : "WEB";
    }

    // ==================== MANUAL GETTERS ====================

    public String getUsername() { 
        return username; 
    }
    
    public String getPassword() { 
        return password; 
    }
    
    public boolean isRememberMe() { 
        return rememberMe; 
    }
    
    public String getDeviceInfo() { 
        return deviceInfo; 
    }
    
    public String getUserAgent() { 
        return userAgent; 
    }
    
    public String getIpAddress() { 
        return ipAddress; 
    }
    
    public Long getTimestamp() { 
        return timestamp; 
    }
    
    public String getClientVersion() { 
        return clientVersion; 
    }
    
    public String getTimezone() { 
        return timezone; 
    }
    
    public String getPlatform() { 
        return platform; 
    }

    // ==================== MANUAL SETTERS WITH TRIMMING ====================

    public void setUsername(String username) { 
        this.username = username != null ? username.trim() : null; 
    }
    
    public void setPassword(String password) { 
        this.password = password; 
    }
    
    public void setRememberMe(boolean rememberMe) { 
        this.rememberMe = rememberMe; 
    }
    
    public void setDeviceInfo(String deviceInfo) { 
        this.deviceInfo = deviceInfo != null ? deviceInfo.trim() : null; 
    }
    
    public void setUserAgent(String userAgent) { 
        this.userAgent = userAgent != null ? userAgent.trim() : null; 
    }
    
    public void setIpAddress(String ipAddress) { 
        this.ipAddress = ipAddress != null ? ipAddress.trim() : null; 
    }
    
    public void setTimestamp(Long timestamp) { 
        this.timestamp = timestamp; 
    }
    
    public void setClientVersion(String clientVersion) { 
        this.clientVersion = clientVersion != null ? clientVersion.trim() : null; 
    }
    
    public void setTimezone(String timezone) { 
        this.timezone = timezone != null ? timezone.trim() : null; 
    }
    
    public void setPlatform(String platform) { 
        this.platform = platform != null ? platform.trim() : null; 
    }

    // ==================== BUSINESS LOGIC METHODS ====================

    /**
     * Хэрэглэгчийн нэр эсвэл имэйл эсэхийг шалгах
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
     */
    public void clearPassword() {
        this.password = null;
    }

    /**
     * Manual validation - клиент болон серверт хоёуланд ашиглах боломжтой
     */
    @JsonIgnore
    public String validateManual() {
        // Username validation
        if (this.username == null || this.username.trim().isEmpty()) {
            return "Хэрэглэгчийн нэр заавал оруулна уу";
        }
        
        String trimmedUsername = this.username.trim();
        if (trimmedUsername.length() < 3) {
            return "Хэрэглэгчийн нэр хамгийн багадаа 3 тэмдэгт байх ёстой";
        }
        if (trimmedUsername.length() > 50) {
            return "Хэрэглэгчийн нэр 50 тэмдэгтээс их байж болохгүй";
        }
        if (!trimmedUsername.matches("^[a-zA-Z0-9._@-]+$")) {
            return "Хэрэглэгчийн нэр зөвхөн үсэг, тоо, цэг, дэд зураас, @ тэмдэг агуулах боломжтой";
        }
        
        // Password validation
        if (this.password == null || this.password.trim().isEmpty()) {
            return "Нууц үг заавал оруулна уу";
        }
        if (this.password.length() < 6) {
            return "Нууц үг хамгийн багадаа 6 тэмдэгт байх ёстой";
        }
        if (this.password.length() > 100) {
            return "Нууц үг 100 тэмдэгтээс их байж болохгүй";
        }
        
        return null; // No error
    }

    /**
     * Хүсэлт валид эсэхийг шалгах
     */
    @JsonIgnore
    public boolean isValid() {
        return validateManual() == null;
    }

    /**
     * Validation алдааны мессеж авах (AuthController-тэй нийцүүлэх)
     */
    @JsonIgnore
    public String getValidationError() {
        return validateManual();
    }

    /**
     * Username normalize хийх
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
     * Timestamp үүсгэх
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
            this.platform = Platform.WEB;
        }
    }

    /**
     * Input sanitization
     */
    public void sanitizeInputs() {
        if (username != null) {
            username = username.trim();
        }
        if (deviceInfo != null) {
            deviceInfo = deviceInfo.trim();
        }
        if (userAgent != null) {
            userAgent = userAgent.trim();
        }
        if (clientVersion != null) {
            clientVersion = clientVersion.trim();
        }
        if (timezone != null) {
            timezone = timezone.trim();
        }
        if (platform != null) {
            platform = platform.trim();
        }
    }

    /**
     * Audit мэдээлэл бэлтгэх
     */
    @JsonIgnore
    public String getAuditInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("user=").append(username != null ? username : "null");
        if (ipAddress != null) sb.append(", ip=").append(ipAddress);
        if (deviceInfo != null) sb.append(", device=").append(deviceInfo);
        if (platform != null) sb.append(", platform=").append(platform);
        if (timestamp != null) sb.append(", timestamp=").append(timestamp);
        return sb.toString();
    }

    // ==================== STATIC FACTORY METHODS ====================

    /**
     * Static factory method - энгийн хүсэлт
     */
    public static LoginRequestDto of(String username, String password) {
        return new LoginRequestDto(username, password);
    }

    /**
     * Static factory method - remember me тэй
     */
    public static LoginRequestDto withRememberMe(String username, String password, boolean rememberMe) {
        return new LoginRequestDto(username, password, rememberMe);
    }

    /**
     * Static factory method - бүрэн мэдээлэлтэй
     */
    public static LoginRequestDto withFullInfo(String username, String password, boolean rememberMe, 
                                             String deviceInfo, String ipAddress, String platform) {
        LoginRequestDto request = new LoginRequestDto(username, password, rememberMe);
        request.setDeviceInfo(deviceInfo);
        request.setIpAddress(ipAddress);
        request.setPlatform(platform);
        return request;
    }

    /**
     * Mobile app-аас ирэх хүсэлт
     */
    public static LoginRequestDto forMobile(String username, String password, String platform, 
                                          String clientVersion, String deviceInfo) {
        LoginRequestDto request = new LoginRequestDto(username, password, true);
        request.setPlatform(platform);
        request.setClientVersion(clientVersion);
        request.setDeviceInfo(deviceInfo);
        return request;
    }

    // ==================== BUILDER-STYLE METHODS ====================

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

    // ==================== OVERRIDE METHODS ====================

    /**
     * toString method (нууц үг харуулахгүй)
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
                ", isValid=" + isValid() +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        LoginRequestDto that = (LoginRequestDto) obj;
        return rememberMe == that.rememberMe &&
               username != null ? username.equals(that.username) : that.username == null;
    }

    @Override
    public int hashCode() {
        int result = username != null ? username.hashCode() : 0;
        result = 31 * result + (rememberMe ? 1 : 0);
        return result;
    }

    // ==================== NESTED CLASSES ====================

    /**
     * Platform constants
     */
    public static final class Platform {
        public static final String WEB = "WEB";
        public static final String MOBILE_ANDROID = "MOBILE_ANDROID";
        public static final String MOBILE_IOS = "MOBILE_IOS";
        public static final String DESKTOP = "DESKTOP";
        public static final String API = "API";
        
        private Platform() {
            // Utility class
        }
    }

    /**
     * Validation helper - сайжруулсан хувилбар
     */
    public static final class Validator {
        
        /**
         * Username format шалгах
         */
        public static boolean isValidUsername(String username) {
            if (username == null || username.trim().isEmpty()) return false;
            String trimmed = username.trim();
            if (trimmed.length() < 3 || trimmed.length() > 50) return false;
            return trimmed.matches("^[a-zA-Z0-9._@-]+$");
        }
        
        /**
         * Password format шалгах
         */
        public static boolean isValidPassword(String password) {
            if (password == null) return false;
            return password.length() >= 6 && password.length() <= 100;
        }
        
        /**
         * Email format шалгах
         */
        public static boolean isValidEmail(String email) {
            if (email == null || email.trim().isEmpty()) return false;
            return email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
        }
        
        /**
         * Login request бүрэн шалгах
         */
        public static String validateLoginRequest(LoginRequestDto request) {
            if (request == null) {
                return "Login мэдээлэл байхгүй байна";
            }
            return request.validateManual();
        }
        
        private Validator() {
            // Utility class
        }
    }
}