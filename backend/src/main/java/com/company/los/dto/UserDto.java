package com.company.los.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.company.los.entity.Role;
import com.company.los.entity.User;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Хэрэглэгчийн DTO
 * User Data Transfer Object
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDto {

    private UUID id;

    @NotBlank(message = "Хэрэглэгчийн нэр заавал бөглөх ёстой")
    @Size(min = 3, max = 50, message = "Хэрэглэгчийн нэр 3-50 тэмдэгт байх ёстой")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Хэрэглэгчийн нэрэнд зөвхөн үсэг, тоо, цэг, доор зураас ашиглана уу")
    private String username;

    @JsonIgnore // Never serialize password
    @NotBlank(message = "Нууц үг заавал бөглөх ёстой")
    @Size(min = 8, max = 100, message = "Нууц үг 8-100 тэмдэгт байх ёстой")
    private String password;

    @NotBlank(message = "И-мэйл заавал бөглөх ёстой")
    @Email(message = "И-мэйлийн формат буруу")
    @Size(max = 100, message = "И-мэйл 100 тэмдэгтээс ихгүй байх ёстой")
    private String email;

    @NotBlank(message = "Нэр заавал бөглөх ёстой")
    @Size(max = 100, message = "Нэр 100 тэмдэгтээс ихгүй байх ёстой")
    private String firstName;

    @NotBlank(message = "Овог заавал бөглөх ёстой")
    @Size(max = 100, message = "Овог 100 тэмдэгтээс ихгүй байх ёстой")
    private String lastName;

    @Pattern(regexp = "^[+]?[0-9]{8,15}$", message = "Утасны дугаарын формат буруу")
    private String phone;

    @Size(max = 20, message = "Ажилтны дугаар 20 тэмдэгтээс ихгүй байх ёстой")
    private String employeeId;

    @Size(max = 100, message = "Албан тушаал 100 тэмдэгтээс ихгүй байх ёстой")
    private String position;

    @Size(max = 100, message = "Хэлтэс 100 тэмдэгтээс ихгүй байх ёстой")
    private String department;

    private User.UserStatus status;

    // Security мэдээлэл
    private Boolean accountNonExpired;
    private Boolean accountNonLocked;
    private Boolean credentialsNonExpired;
    private Boolean enabled;

    private Integer failedLoginAttempts;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastLoginAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime passwordChangedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lockedUntil;

    // Two-Factor Authentication
    private Boolean twoFactorEnabled;
    
    @JsonIgnore // Never serialize secret
    private String twoFactorSecret;

    // Profile мэдээлэл
    private String profilePictureUrl;
    private String language;
    private String timezone;

    // Audit fields
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    private String createdBy;
    private String updatedBy;

    // Roles - simplified for DTO
    private Set<String> roleNames;
    private List<RoleDto> roles;

    // Computed fields (read-only)
    private String fullName;
    private String displayName;
    private String statusDisplay;
    private Boolean isActive;
    private Boolean isLocked;
    private Boolean isPasswordExpired;
    private Boolean hasMultipleRoles;
    private Boolean isAdminUser;
    private Integer daysSinceLastLogin;
    private String lastLoginText;

    // Constructors
    public UserDto() {
        this.status = User.UserStatus.ACTIVE;
        this.accountNonExpired = true;
        this.accountNonLocked = true;
        this.credentialsNonExpired = true;
        this.enabled = true;
        this.failedLoginAttempts = 0;
        this.twoFactorEnabled = false;
        this.language = "mn";
        this.timezone = "Asia/Ulaanbaatar";
    }

    public UserDto(String username, String password, String email, String firstName, String lastName) {
        this();
        this.username = username;
        this.password = password;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    // Static factory methods
    public static UserDto fromEntity(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        // Note: password is never set in DTO for security
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setPhone(user.getPhone());
        dto.setEmployeeId(user.getEmployeeId());
        dto.setPosition(user.getPosition());
        dto.setDepartment(user.getDepartment());
        dto.setStatus(user.getStatus());
        dto.setAccountNonExpired(user.getAccountNonExpired());
        dto.setAccountNonLocked(user.getAccountNonLocked());
        dto.setCredentialsNonExpired(user.getCredentialsNonExpired());
        dto.setEnabled(user.getEnabled());
        dto.setFailedLoginAttempts(user.getFailedLoginAttempts());
        dto.setLastLoginAt(user.getLastLoginAt());
        dto.setPasswordChangedAt(user.getPasswordChangedAt());
        dto.setLockedUntil(user.getLockedUntil());
        dto.setTwoFactorEnabled(user.getTwoFactorEnabled());
        dto.setProfilePictureUrl(user.getProfilePictureUrl());
        dto.setLanguage(user.getLanguage());
        dto.setTimezone(user.getTimezone());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        dto.setCreatedBy(user.getCreatedBy());
        dto.setUpdatedBy(user.getUpdatedBy());

        // Set role information
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            dto.setRoleNames(user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toSet()));
            
            dto.setRoles(user.getRoles().stream()
                    .map(RoleDto::fromEntity)
                    .collect(Collectors.toList()));
        }

        // Computed fields
        dto.setFullName(user.getFullName());
        dto.setDisplayName(user.getDisplayName());
        dto.setStatusDisplay(user.getStatus().getMongolianName());
        dto.setIsActive(user.isEnabled() && user.getStatus() == User.UserStatus.ACTIVE);
        dto.setIsLocked(user.isAccountLocked());
        dto.setIsPasswordExpired(user.isPasswordExpired());
        dto.setHasMultipleRoles(user.getRoles().size() > 1);
        dto.setIsAdminUser(user.hasAnyRole("ROLE_SYSTEM_ADMIN", "ROLE_BUSINESS_ADMIN"));
        
        // Calculate days since last login
        if (user.getLastLoginAt() != null) {
            long days = java.time.Duration.between(user.getLastLoginAt(), LocalDateTime.now()).toDays();
            dto.setDaysSinceLastLogin((int) days);
            dto.setLastLoginText(days == 0 ? "Өнөөдөр" : days + " хоногийн өмнө");
        } else {
            dto.setLastLoginText("Хэзээ ч нэвтрээгүй");
        }

        return dto;
    }

    public static UserDto createSummary(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setDisplayName(user.getDisplayName());
        dto.setStatus(user.getStatus());
        dto.setEnabled(user.getEnabled());
        dto.setStatusDisplay(user.getStatus().getMongolianName());
        dto.setIsActive(user.isEnabled() && user.getStatus() == User.UserStatus.ACTIVE);
        
        if (user.getRoles() != null) {
            dto.setRoleNames(user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toSet()));
        }
        
        return dto;
    }

    public static UserDto createProfile(User user) {
        UserDto dto = fromEntity(user);
        // Remove sensitive information for profile view
        dto.setFailedLoginAttempts(null);
        dto.setAccountNonExpired(null);
        dto.setAccountNonLocked(null);
        dto.setCredentialsNonExpired(null);
        dto.setLockedUntil(null);
        return dto;
    }

    public User toEntity() {
        User user = new User();
        user.setId(this.id);
        user.setUsername(this.username);
        // Password should be handled separately with encoding
        if (this.password != null) {
            user.setPassword(this.password); // Will be encoded in service
        }
        user.setEmail(this.email);
        user.setFirstName(this.firstName);
        user.setLastName(this.lastName);
        user.setPhone(this.phone);
        user.setEmployeeId(this.employeeId);
        user.setPosition(this.position);
        user.setDepartment(this.department);
        user.setStatus(this.status != null ? this.status : User.UserStatus.ACTIVE);
        user.setAccountNonExpired(this.accountNonExpired != null ? this.accountNonExpired : true);
        user.setAccountNonLocked(this.accountNonLocked != null ? this.accountNonLocked : true);
        user.setCredentialsNonExpired(this.credentialsNonExpired != null ? this.credentialsNonExpired : true);
        user.setEnabled(this.enabled != null ? this.enabled : true);
        user.setFailedLoginAttempts(this.failedLoginAttempts != null ? this.failedLoginAttempts : 0);
        user.setLastLoginAt(this.lastLoginAt);
        user.setPasswordChangedAt(this.passwordChangedAt);
        user.setLockedUntil(this.lockedUntil);
        user.setTwoFactorEnabled(this.twoFactorEnabled != null ? this.twoFactorEnabled : false);
        user.setTwoFactorSecret(this.twoFactorSecret);
        user.setProfilePictureUrl(this.profilePictureUrl);
        user.setLanguage(this.language != null ? this.language : "mn");
        user.setTimezone(this.timezone != null ? this.timezone : "Asia/Ulaanbaatar");
        return user;
    }

    // Validation methods
    public boolean isValidForRegistration() {
        return username != null && !username.trim().isEmpty() &&
               password != null && password.length() >= 8 &&
               email != null && email.contains("@") &&
               firstName != null && !firstName.trim().isEmpty() &&
               lastName != null && !lastName.trim().isEmpty();
    }

    public boolean hasValidContactInfo() {
        return (email != null && email.contains("@")) ||
               (phone != null && !phone.trim().isEmpty());
    }

    // Business logic methods
    public boolean canLogin() {
        return enabled && status == User.UserStatus.ACTIVE && 
               accountNonLocked && !isLocked;
    }

    public boolean needsPasswordReset() {
        return isPasswordExpired || status == User.UserStatus.PENDING_ACTIVATION;
    }

    public boolean canBeDeactivated() {
        return status != User.UserStatus.SUSPENDED &&
               !isAdminUser; // Don't allow deactivating admin users
    }

    public boolean canBeDeleted() {
        return status != User.UserStatus.ACTIVE &&
               !isAdminUser && // Don't allow deleting admin users
               daysSinceLastLogin != null && daysSinceLastLogin > 90; // Inactive for 90+ days
    }

    public String getStatusBadgeClass() {
        if (status == null) return "badge-secondary";
        switch (status) {
            case ACTIVE: return enabled ? "badge-success" : "badge-warning";
            case INACTIVE: return "badge-secondary";
            case LOCKED: case SUSPENDED: return "badge-danger";
            case PENDING_ACTIVATION: return "badge-info";
            default: return "badge-secondary";
        }
    }

    public String getSecurityStatusText() {
        if (!enabled) return "Идэвхгүй";
        if (isLocked) return "Түгжээтэй";
        if (status == User.UserStatus.SUSPENDED) return "Түр зогсоосон";
        if (isPasswordExpired) return "Нууц үг хуучирсан";
        if (failedLoginAttempts != null && failedLoginAttempts > 0) {
            return failedLoginAttempts + " удаа буруу оролдлого";
        }
        return "Хэвийн";
    }

    public String getRoleNamesText() {
        if (roleNames == null || roleNames.isEmpty()) {
            return "Дүр олгоогүй";
        }
        return String.join(", ", roleNames);
    }

    public boolean hasRole(String roleName) {
        return roleNames != null && roleNames.contains(roleName);
    }

    public boolean hasAnyRole(String... roleNames) {
        if (this.roleNames == null) return false;
        for (String role : roleNames) {
            if (this.roleNames.contains(role)) {
                return true;
            }
        }
        return false;
    }

    // Profile management
    public void updateProfile(String firstName, String lastName, String phone, String profilePictureUrl) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.profilePictureUrl = profilePictureUrl;
    }

    public void updatePreferences(String language, String timezone) {
        this.language = language;
        this.timezone = timezone;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
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

    public Integer getFailedLoginAttempts() { return failedLoginAttempts; }
    public void setFailedLoginAttempts(Integer failedLoginAttempts) { this.failedLoginAttempts = failedLoginAttempts; }

    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(LocalDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; }

    public LocalDateTime getPasswordChangedAt() { return passwordChangedAt; }
    public void setPasswordChangedAt(LocalDateTime passwordChangedAt) { this.passwordChangedAt = passwordChangedAt; }

    public LocalDateTime getLockedUntil() { return lockedUntil; }
    public void setLockedUntil(LocalDateTime lockedUntil) { this.lockedUntil = lockedUntil; }

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

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    public Set<String> getRoleNames() { return roleNames; }
    public void setRoleNames(Set<String> roleNames) { this.roleNames = roleNames; }

    public List<RoleDto> getRoles() { return roles; }
    public void setRoles(List<RoleDto> roles) { this.roles = roles; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getStatusDisplay() { return statusDisplay; }
    public void setStatusDisplay(String statusDisplay) { this.statusDisplay = statusDisplay; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public Boolean getIsLocked() { return isLocked; }
    public void setIsLocked(Boolean isLocked) { this.isLocked = isLocked; }

    public Boolean getIsPasswordExpired() { return isPasswordExpired; }
    public void setIsPasswordExpired(Boolean isPasswordExpired) { this.isPasswordExpired = isPasswordExpired; }

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
                ", fullName='" + fullName + '\'' +
                ", status=" + status +
                ", enabled=" + enabled +
                '}';
    }
}