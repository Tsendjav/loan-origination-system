package com.company.los.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.company.los.entity.Role;
import com.company.los.entity.User;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Хэрэглэгчийн DTO
 * User Data Transfer Object
 * ⭐ ЗАСВАРЛАСАН - setPassword method нэмэгдсэн ⭐
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDto {

    private UUID id;

    @NotBlank(message = "Хэрэглэгчийн нэр заавал бөглөх ёстой")
    @Size(min = 3, max = 100, message = "Хэрэглэгчийн нэр 3-100 тэмдэгт байх ёстой")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Хэрэглэгчийн нэрэнд зөвхөн үсэг, тоо, цэг, доор зураас ашиглана уу")
    private String username;

    @JsonIgnore // Never serialize password
    @Size(min = 8, max = 100, message = "Нууц үг 8-100 тэмдэгт байх ёстой")
    private String password;

    @NotBlank(message = "И-мэйл заавал бөглөх ёстой")
    @Email(message = "И-мэйлийн формат буруу")
    @Size(max = 255, message = "И-мэйл 255 тэмдэгтээс ихгүй байх ёстой")
    private String email;

    @NotBlank(message = "Нэр заавал бөглөх ёстой")
    @Size(max = 100, message = "Нэр 100 тэмдэгтээс ихгүй байх ёстой")
    private String firstName;

    @NotBlank(message = "Овог заавал бөглөх ёстой")
    @Size(max = 100, message = "Овог 100 тэмдэгтээс ихгүй байх ёстой")
    private String lastName;

    @Pattern(regexp = "^[+]?[0-9]{8,15}$", message = "Утасны дугаарын формат буруу")
    @Size(max = 20, message = "Утасны дугаар 20 тэмдэгтээс ихгүй байх ёстой")
    private String phone;

    @Size(max = 50, message = "Ажилтны дугаар 50 тэмдэгтээс ихгүй байх ёстой")
    private String employeeId;

    @Size(max = 100, message = "Албан тушаал 100 тэмдэгтээс ихгүй байх ёстой")
    private String position;

    @Size(max = 100, message = "Хэлтэс 100 тэмдэгтээс ихгүй байх ёстой")
    private String department;

    @NotNull(message = "Хэрэглэгчийн статус заавал байх ёстой")
    private User.UserStatus status;

    // Spring Security UserDetails compatible fields
    private Boolean accountNonExpired;
    private Boolean accountNonLocked;
    private Boolean credentialsNonExpired;
    private Boolean enabled;

    // Account management fields
    private Boolean isEmailVerified;
    private Boolean isLocked;
    private Integer failedLoginAttempts;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastLoginAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime passwordChangedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lockedUntil;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime passwordExpiresAt;

    // Two-Factor Authentication
    private Boolean twoFactorEnabled;
    @JsonIgnore // Secret never expose in DTO
    private String twoFactorSecret;

    // Profile мэдээлэл
    @Size(max = 500, message = "Профайл зургийн URL 500 тэмдэгтээс ихгүй байх ёстой")
    private String profilePictureUrl;

    @Size(max = 10, message = "Хэл 10 тэмдэгтээс ихгүй байх ёстой")
    private String language;

    @Size(max = 50, message = "Цагийн бүс 50 тэмдэгтээс ихгүй байх ёстой")
    private String timezone;

    // Manager relationship
    private UUID managerId;
    private String managerName;

    // Roles
    private Set<RoleDto> roles;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    private String createdBy;
    private String updatedBy;

    private Boolean isDeleted;
    private Boolean isActive;

    // Computed fields
    private String fullName;
    private String displayName;
    private Boolean hasMultipleRoles;
    private Boolean isAdminUser;
    private Integer daysSinceLastLogin;
    private String lastLoginText; // for display, e.g., "5 days ago", "just now"

    // Constructors
    public UserDto() {
    }

    public UserDto(String username, String email, String firstName, String lastName) {
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.status = User.UserStatus.PENDING_ACTIVATION; // Default status
        this.accountNonExpired = true;
        this.accountNonLocked = true;
        this.credentialsNonExpired = true;
        this.enabled = false; // By default, not enabled until activated
        this.isEmailVerified = false;
        this.isLocked = false;
        this.failedLoginAttempts = 0;
        this.twoFactorEnabled = false;
        this.language = "mn";
        this.timezone = "Asia/Ulaanbaatar";
        this.isDeleted = false;
        this.isActive = true;
    }

    // Static factory methods
    public static UserDto fromEntity(User user) {
        if (user == null) {
            return null;
        }

        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        // Password is @JsonIgnore, so don't set it for security
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setPhone(user.getPhone());
        dto.setEmployeeId(user.getEmployeeId());
        dto.setPosition(user.getPosition());
        dto.setDepartment(user.getDepartment());
        dto.setStatus(user.getStatus());

        // Spring Security UserDetails fields
        dto.setAccountNonExpired(user.isAccountNonExpired());
        dto.setAccountNonLocked(user.isAccountNonLocked());
        dto.setCredentialsNonExpired(user.isCredentialsNonExpired());
        dto.setEnabled(user.isEnabled());

        // Account management
        dto.setIsEmailVerified(user.getIsEmailVerified());
        dto.setIsLocked(user.getIsLocked());
        dto.setFailedLoginAttempts(user.getFailedLoginAttempts());
        dto.setLastLoginAt(user.getLastLoginAt());
        dto.setPasswordChangedAt(user.getPasswordChangedAt());
        dto.setLockedUntil(user.getLockedUntil());
        dto.setPasswordExpiresAt(user.getPasswordExpiresAt());

        // Two-Factor Authentication
        dto.setTwoFactorEnabled(user.getTwoFactorEnabled());
        // twoFactorSecret intentionally not set for security

        // Profile
        dto.setProfilePictureUrl(user.getProfilePictureUrl());
        dto.setLanguage(user.getLanguage());
        dto.setTimezone(user.getTimezone());

        // Manager
        if (user.getManager() != null) {
            dto.setManagerId(user.getManager().getId());
            dto.setManagerName(user.getManager().getFullName());
        }

        // Roles
        if (user.getRoles() != null) {
            dto.setRoles(user.getRoles().stream()
                    .map(RoleDto::fromEntity)
                    .collect(Collectors.toSet()));
        }

        // Audit fields
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        dto.setCreatedBy(user.getCreatedBy());
        dto.setUpdatedBy(user.getUpdatedBy());
        dto.setIsDeleted(user.getIsDeleted());
        dto.setIsActive(user.getIsActive());

        // Set computed fields
        dto.setFullName(user.getFullName());
        dto.setDisplayName(user.getDisplayName());
        dto.setHasMultipleRoles(user.getRoles() != null && user.getRoles().size() > 1);
        dto.setIsAdminUser(user.hasAnyRole("ROLE_SYSTEM_ADMIN", "ROLE_BUSINESS_ADMIN"));

        // Calculate daysSinceLastLogin and lastLoginText
        if (user.getLastLoginAt() != null) {
            Duration duration = Duration.between(user.getLastLoginAt(), LocalDateTime.now());
            long days = duration.toDays();
            dto.setDaysSinceLastLogin((int) days);
            if (days == 0) {
                dto.setLastLoginText("Өнөөдөр");
            } else if (days == 1) {
                dto.setLastLoginText("Өчигдөр");
            } else {
                dto.setLastLoginText(days + " хоногийн өмнө");
            }
        } else {
            dto.setDaysSinceLastLogin(null);
            dto.setLastLoginText("Хэзээ ч нэвтрээгүй");
        }

        return dto;
    }

    // Method to convert from CreateUserRequestDto to UserDto
    public static UserDto fromCreateRequest(CreateUserRequestDto createRequest) {
        if (createRequest == null) {
            return null;
        }
        UserDto dto = new UserDto();
        dto.setUsername(createRequest.getUsername());
        dto.setEmail(createRequest.getEmail());
        dto.setFirstName(createRequest.getFirstName());
        dto.setLastName(createRequest.getLastName());
        dto.setPhone(createRequest.getPhone());
        dto.setEmployeeId(createRequest.getEmployeeId());
        dto.setPosition(createRequest.getPosition());
        dto.setDepartment(createRequest.getDepartment());
        dto.setPassword(createRequest.getPassword()); // Include password for validation/processing
        dto.setStatus(createRequest.getStatus());
        dto.setEnabled(createRequest.getActivateImmediately()); // Assuming activateImmediately maps to enabled
        dto.setLanguage(createRequest.getLanguage());
        dto.setTimezone(createRequest.getTimezone());
        dto.setProfilePictureUrl(createRequest.getProfilePictureUrl());
        dto.setManagerId(createRequest.getManagerId()); // Set managerId

        // Default values for new user
        dto.setAccountNonExpired(true);
        dto.setAccountNonLocked(true);
        dto.setCredentialsNonExpired(!Boolean.TRUE.equals(createRequest.getRequirePasswordChange()));
        dto.setIsEmailVerified(!Boolean.TRUE.equals(createRequest.getRequireEmailVerification()));
        dto.setIsLocked(false);
        dto.setFailedLoginAttempts(0);
        dto.setTwoFactorEnabled(false); // Default to false for new users
        dto.setIsDeleted(false);
        dto.setIsActive(Boolean.TRUE.equals(createRequest.getActivateImmediately()));

        return dto;
    }


    public User toEntity() {
        User user = new User();
        
        if (this.id != null) {
            user.setId(this.id);
        }
        
        user.setUsername(this.username);
        
        if (this.password != null) {
            user.setPasswordHash(this.password); // ⭐ ЗАСВАРЛАСАН: setPassword -> setPasswordHash ⭐
        }
        
        user.setEmail(this.email);
        user.setFirstName(this.firstName);
        user.setLastName(this.lastName);
        user.setPhone(this.phone);
        user.setEmployeeId(this.employeeId);
        user.setPosition(this.position);
        user.setDepartment(this.department);
        user.setStatus(this.status != null ? this.status : User.UserStatus.PENDING_ACTIVATION);

        // Account management
        if (this.isEmailVerified != null) {
            user.setIsEmailVerified(this.isEmailVerified);
        }
        if (this.isLocked != null) {
            user.setIsLocked(this.isLocked);
        }
        if (this.failedLoginAttempts != null) {
            user.setFailedLoginAttempts(this.failedLoginAttempts);
        }

        user.setLastLoginAt(this.lastLoginAt);
        user.setPasswordChangedAt(this.passwordChangedAt);
        user.setLockedUntil(this.lockedUntil);
        user.setPasswordExpiresAt(this.passwordExpiresAt);

        // Two-Factor Authentication
        if (this.twoFactorEnabled != null) {
            user.setTwoFactorEnabled(this.twoFactorEnabled);
        }
        if (this.twoFactorSecret != null) {
            user.setTwoFactorSecret(this.twoFactorSecret);
        }

        // Profile
        user.setProfilePictureUrl(this.profilePictureUrl);
        user.setLanguage(this.language != null ? this.language : "mn");
        user.setTimezone(this.timezone != null ? this.timezone : "Asia/Ulaanbaatar");

        // Manager - Handle in service layer instead of here
        // if (this.managerId != null) {
        //     user.setManager(new User(this.managerId));
        // }

        // Roles conversion
        if (this.roles != null) {
            user.setRoles(this.roles.stream()
                    .map(RoleDto::toEntity)
                    .collect(Collectors.toList()));
        }

        // Audit fields
        user.setCreatedAt(this.createdAt);
        user.setUpdatedAt(this.updatedAt);
        user.setCreatedBy(this.createdBy);
        user.setUpdatedBy(this.updatedBy);

        if (this.isDeleted != null) {
            user.setIsDeleted(this.isDeleted);
        }
        if (this.isActive != null) {
            user.setIsActive(this.isActive);
        }

        return user;
    }

    // ========== НЭМЭГДСЭН ДУТУ МЕТОДУУД ==========

    /**
     * Хэрэглэгчийн мэдээлэл бүрэн эсэхийг шалгах
     */
    public boolean isValidForRegistration() {
        return username != null && !username.trim().isEmpty() &&
               email != null && !email.trim().isEmpty() &&
               firstName != null && !firstName.trim().isEmpty() &&
               lastName != null && !lastName.trim().isEmpty() &&
               isValidEmail(email) &&
               isValidUsername(username);
    }

    /**
     * И-мэйл хаягийн формат зөв эсэхийг шалгах
     */
    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email.matches(emailRegex);
    }

    /**
     * Хэрэглэгчийн нэрийн формат зөв эсэхийг шалгах
     */
    private boolean isValidUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        String usernameRegex = "^[a-zA-Z0-9._-]{3,100}$";
        return username.matches(usernameRegex);
    }

    /**
     * Хэрэглэгчийн бүрэн нэрийг авах
     */
    public String getComputedFullName() {
        if (firstName == null && lastName == null) {
            return username;
        }
        
        StringBuilder fullName = new StringBuilder();
        if (lastName != null && !lastName.trim().isEmpty()) {
            fullName.append(lastName.trim());
        }
        if (firstName != null && !firstName.trim().isEmpty()) {
            if (fullName.length() > 0) {
                fullName.append(" ");
            }
            fullName.append(firstName.trim());
        }
        
        return fullName.length() > 0 ? fullName.toString() : username;
    }

    /**
     * Хэрэглэгчийн харагдах нэрийг авах
     */
    public String getComputedDisplayName() {
        String fullName = getComputedFullName();
        return fullName != null && !fullName.equals(username) ? fullName : username;
    }

    /**
     * Дүр эзэмшдэг эсэхийг шалгах
     */
    public boolean hasRole(String roleName) {
        if (roles == null || roleName == null) {
            return false;
        }
        return roles.stream()
                .anyMatch(role -> roleName.equals(role.getName()));
    }

    /**
     * Аль нэг дүр эзэмшдэг эсэхийг шалгах
     */
    public boolean hasAnyRole(String... roleNames) {
        if (roles == null || roleNames == null) {
            return false;
        }
        for (String roleName : roleNames) {
            if (hasRole(roleName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Админ эрхтэй эсэхийг шалгах
     */
    public boolean isAdmin() {
        return hasAnyRole("ROLE_SYSTEM_ADMIN", "ROLE_BUSINESS_ADMIN");
    }

    /**
     * Дүрүүдийн нэрийг текст болгож буцаах
     */
    public String getRoleNamesAsString() {
        if (roles == null || roles.isEmpty()) {
            return "Дүргүй";
        }
        return roles.stream()
                .map(RoleDto::getName)
                .collect(Collectors.joining(", "));
    }

    /**
     * Утасны дугаарыг форматлаж буцаах
     */
    public String getFormattedPhone() {
        if (phone == null || phone.trim().isEmpty()) {
            return "";
        }
        String cleanPhone = phone.replaceAll("[^0-9+]", "");
        if (cleanPhone.startsWith("+976")) {
            return cleanPhone.substring(0, 4) + " " + cleanPhone.substring(4, 8) + " " + cleanPhone.substring(8);
        } else if (cleanPhone.length() == 8) {
            return cleanPhone.substring(0, 4) + " " + cleanPhone.substring(4);
        }
        return cleanPhone;
    }

    /**
     * Нэвтрэх статусын текст
     */
    public String getAccountStatusText() {
        if (!Boolean.TRUE.equals(enabled)) {
            return "Идэвхгүй";
        }
        if (!Boolean.TRUE.equals(accountNonLocked)) {
            return "Түгжээтэй";
        }
        if (!Boolean.TRUE.equals(credentialsNonExpired)) {
            return "Нууц үг хуучирсан";
        }
        if (status == User.UserStatus.PENDING_ACTIVATION) {
            return "Идэвхжүүлэх хүлээгдэж байна";
        }
        if (status == User.UserStatus.SUSPENDED) {
            return "Түдгэлзүүлсэн";
        }
        return "Идэвхтэй";
    }

    // ========== GETTERS AND SETTERS ==========

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    /**
     * ⭐ ЗАСВАРЛАСАН: setPassword method нэмэгдсэн ⭐
     */
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public User.UserStatus getStatus() { return status; }
    public void setStatus(User.UserStatus status) { this.status = status; }

    public Boolean getAccountNonExpired() { return accountNonExpired; }
    public void setAccountNonExpired(Boolean accountNonExpired) { this.accountNonExpired = accountNonExpired; }

    public Boolean getAccountNonLocked() { return accountNonLocked; }
    public void setAccountNonLocked(Boolean accountNonLocked) { this.accountNonLocked = accountNonLocked; }

    public Boolean getCredentialsNonExpired() { return credentialsNonExpired; }
    public void setCredentialsNonExpired(Boolean credentialsNonExpired) { this.credentialsNonExpired = credentialsNonExpired; }

    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }

    public Boolean getIsEmailVerified() { return isEmailVerified; }
    public void setIsEmailVerified(Boolean isEmailVerified) { this.isEmailVerified = isEmailVerified; }

    public Boolean getIsLocked() { return isLocked; }
    public void setIsLocked(Boolean isLocked) { this.isLocked = isLocked; }

    public Integer getFailedLoginAttempts() { return failedLoginAttempts; }
    public void setFailedLoginAttempts(Integer failedLoginAttempts) { this.failedLoginAttempts = failedLoginAttempts; }

    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(LocalDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; }

    public LocalDateTime getPasswordChangedAt() { return passwordChangedAt; }
    public void setPasswordChangedAt(LocalDateTime passwordChangedAt) { this.passwordChangedAt = passwordChangedAt; }

    public LocalDateTime getLockedUntil() { return lockedUntil; }
    public void setLockedUntil(LocalDateTime lockedUntil) { this.lockedUntil = lockedUntil; }

    public LocalDateTime getPasswordExpiresAt() { return passwordExpiresAt; }
    public void setPasswordExpiresAt(LocalDateTime passwordExpiresAt) { this.passwordExpiresAt = passwordExpiresAt; }

    public Boolean getTwoFactorEnabled() { return twoFactorEnabled; }
    public void setTwoFactorEnabled(Boolean twoFactorEnabled) { this.twoFactorEnabled = twoFactorEnabled; }

    public String getTwoFactorSecret() { return twoFactorSecret; }
    public void setTwoFactorSecret(String twoFactorSecret) { this.twoFactorSecret = twoFactorSecret; }

    public String getProfilePictureUrl() { return profilePictureUrl; }
    public void setProfilePictureUrl(String profilePictureUrl) { this.profilePictureUrl = profilePictureUrl; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }

    public UUID getManagerId() { return managerId; }
    public void setManagerId(UUID managerId) { this.managerId = managerId; }

    public String getManagerName() { return managerName; }
    public void setManagerName(String managerName) { this.managerName = managerName; }

    public Set<RoleDto> getRoles() { return roles; }
    public void setRoles(Set<RoleDto> roles) { this.roles = roles; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    public Boolean getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Boolean isDeleted) { this.isDeleted = isDeleted; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    // Computed fields getters and setters
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public Boolean getHasMultipleRoles() { return hasMultipleRoles; }
    public void setHasMultipleRoles(Boolean hasMultipleRoles) { this.hasMultipleRoles = hasMultipleRoles; }

    public Boolean getIsAdminUser() { return isAdminUser; }
    public void setIsAdminUser(Boolean isAdminUser) { this.isAdminUser = isAdminUser; }

    public Integer getDaysSinceLastLogin() { return daysSinceLastLogin; }
    public void setDaysSinceLastLogin(Integer daysSinceLastLogin) { this.daysSinceLastLogin = daysSinceLastLogin; }

    public String getLastLoginText() { return lastLoginText; }
    public void setLastLoginText(String lastLoginText) { this.lastLoginText = lastLoginText; }

    @Override
    public String toString() {
        return "UserDto{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", phone='" + phone + '\'' +
                ", employeeId='" + employeeId + '\'' +
                ", position='" + position + '\'' +
                ", department='" + department + '\'' +
                ", status=" + status +
                ", enabled=" + enabled +
                ", isEmailVerified=" + isEmailVerified +
                ", isLocked=" + isLocked +
                ", failedLoginAttempts=" + failedLoginAttempts +
                ", lastLoginAt=" + lastLoginAt +
                ", twoFactorEnabled=" + twoFactorEnabled +
                ", language='" + language + '\'' +
                ", timezone='" + timezone + '\'' +
                ", roles=" + (roles != null ? roles.stream().map(RoleDto::getName).collect(Collectors.joining(", ")) : "[]") +
                ", isActive=" + isActive +
                ", isDeleted=" + isDeleted +
                '}';
    }
}