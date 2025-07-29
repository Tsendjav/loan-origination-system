package com.company.los.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Хэрэглэгчийн Entity
 * User Entity - implements Spring Security UserDetails
 * 
 * @author LOS Development Team
 * @version 3.0 - Complete Entity with AuthService compatibility
 * @since 2025-07-28
 */
@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_user_username", columnList = "username", unique = true),
        @Index(name = "idx_user_email", columnList = "email", unique = true),
        @Index(name = "idx_user_employee_id", columnList = "employee_id")
})
@SQLDelete(sql = "UPDATE users SET is_deleted = true WHERE id = ?")
@Where(clause = "is_deleted = false")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "username", unique = true, nullable = false, length = 100)
    @NotBlank(message = "Хэрэглэгчийн нэр заавал бөглөх ёстой")
    @Size(min = 3, max = 100, message = "Хэрэглэгчийн нэр 3-100 тэмдэгт байх ёстой")
    private String username;

    @Column(name = "email", unique = true, nullable = false, length = 255)
    @NotBlank(message = "И-мэйл заавал бөглөх ёстой")
    @Email(message = "И-мэйлийн формат буруу")
    @Size(max = 255, message = "И-мэйл 255 тэмдэгтээс ихгүй байх ёстой")
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    @NotBlank(message = "Нууц үг заавал байх ёстой")
    @Size(max = 255, message = "Нууц үг 255 тэмдэгтээс ихгүй байх ёстой")
    private String password;

    // Хувийн мэдээлэл
    @Column(name = "first_name", nullable = false, length = 100)
    @NotBlank(message = "Нэр заавал бөглөх ёстой")
    @Size(max = 100, message = "Нэр 100 тэмдэгтээс ихгүй байх ёстой")
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    @NotBlank(message = "Овог заавал бөглөх ёстой")
    @Size(max = 100, message = "Овог 100 тэмдэгтээс ихгүй байх ёстой")
    private String lastName;

    @Column(name = "phone", length = 20)
    @Pattern(regexp = "^[+]?[0-9]{8,15}$", message = "Утасны дугаарын формат буруу")
    private String phone;

    // Ажилчны мэдээлэл
    @Column(name = "employee_id", length = 50)
    @Size(max = 50, message = "Ажилчны дугаар 50 тэмдэгтээс ихгүй байх ёстой")
    private String employeeId;

    @Column(name = "department", length = 100)
    @Size(max = 100, message = "Алба 100 тэмдэгтээс ихгүй байх ёстой")
    private String department;

    @Column(name = "position", length = 100)
    @Size(max = 100, message = "Албан тушаал 100 тэмдэгтээс ихгүй байх ёстой")
    private String position;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id", foreignKey = @ForeignKey(name = "fk_user_manager"))
    private User manager;

    // Account properties (Spring Security UserDetails fields)
    @Column(name = "is_email_verified", nullable = false)
    private Boolean isEmailVerified = false;

    @Column(name = "is_locked", nullable = false)
    private Boolean isLocked = false;

    @Column(name = "failed_login_attempts")
    @Min(value = 0, message = "Амжилтгүй нэвтрэх оролдлого сөрөг байж болохгүй")
    private Integer failedLoginAttempts = 0;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "password_expires_at")
    private LocalDateTime passwordExpiresAt;

    // Харилцагчийн харьцах холбоос
    @OneToMany(mappedBy = "manager", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<User> subordinates = new ArrayList<>();

    // Эрхүүд - JPA relationship дээр List ашиглах нь илүү тохиромжтой
    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private List<Role> roles = new ArrayList<>();

    // Audit fields
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    // Additional fields that might be added later
    @Column(name = "language", length = 10)
    private String language = "mn";

    @Column(name = "timezone", length = 50)
    private String timezone = "Asia/Ulaanbaatar";

    @Column(name = "profile_picture_url", length = 500)
    private String profilePictureUrl;

    @Column(name = "two_factor_enabled")
    private Boolean twoFactorEnabled = false;

    @Column(name = "two_factor_secret")
    private String twoFactorSecret;

    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    // Status enum as String for H2 compatibility
    public enum UserStatus {
        ACTIVE("ACTIVE", "Идэвхтэй"),
        INACTIVE("INACTIVE", "Идэвхгүй"),
        SUSPENDED("SUSPENDED", "Түр хориглосон"),
        LOCKED("LOCKED", "Түгжээсэн"),
        EXPIRED("EXPIRED", "Хугацаа дууссан"),
        PENDING_APPROVAL("PENDING_APPROVAL", "Зөвшөөрөл хүлээгдэж байна"),
        PENDING_ACTIVATION("PENDING_ACTIVATION", "Идэвхжүүлэлт хүлээгдэж байна");

        private final String code;
        private final String mongolianName;

        UserStatus(String code, String mongolianName) {
            this.code = code;
            this.mongolianName = mongolianName;
        }

        public String getCode() { return code; }
        public String getMongolianName() { return mongolianName; }
    }

    @Column(name = "status", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Хэрэглэгчийн статус заавал байх ёстой")
    private UserStatus status = UserStatus.PENDING_ACTIVATION;

    // Constructors
    public User() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public User(String username, String email, String password, String firstName, String lastName) {
        this();
        this.username = username;
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    // Spring Security UserDetails implementation
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        
        // Add role-based authorities
        if (roles != null) {
            for (Role role : roles) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
                
                // Add permission-based authorities (if role has permissions)
                if (role.getPermissions() != null) {
                    for (Permission permission : role.getPermissions()) {
                        authorities.add(new SimpleGrantedAuthority(permission.getName()));
                    }
                }
            }
        }
        
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return status != UserStatus.EXPIRED;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !isLocked && (lockedUntil == null || lockedUntil.isBefore(LocalDateTime.now()));
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return passwordExpiresAt == null || passwordExpiresAt.isAfter(LocalDateTime.now());
    }

    @Override
    public boolean isEnabled() {
        return isActive && status == UserStatus.ACTIVE;
    }

    // Business methods
    public String getFullName() {
        return lastName + " " + firstName;
    }

    public String getDisplayName() {
        String fullName = getFullName().trim();
        return !fullName.isEmpty() ? fullName : username;
    }

    public boolean isActive() {
        return UserStatus.ACTIVE.equals(status) && !isLocked;
    }

    public boolean canLogin() {
        return isActive() && isEmailVerified;
    }

    public void lockAccount() {
        this.isLocked = true;
        this.status = UserStatus.LOCKED;
        this.updatedAt = LocalDateTime.now();
    }

    public void lockAccount(LocalDateTime until) {
        this.isLocked = true;
        this.status = UserStatus.LOCKED;
        this.lockedUntil = until;
        this.updatedAt = LocalDateTime.now();
    }

    public void unlockAccount() {
        this.isLocked = false;
        this.status = UserStatus.ACTIVE;
        this.failedLoginAttempts = 0;
        this.lockedUntil = null;
        this.updatedAt = LocalDateTime.now();
    }

    public void incrementFailedLoginAttempts() {
        this.failedLoginAttempts++;
        if (this.failedLoginAttempts >= 5) {
            lockAccount();
        }
        this.updatedAt = LocalDateTime.now();
    }

    public void resetFailedLoginAttempts() {
        this.failedLoginAttempts = 0;
        this.updatedAt = LocalDateTime.now();
    }

    public void recordSuccessfulLogin() {
        this.lastLoginAt = LocalDateTime.now();
        resetFailedLoginAttempts();
        this.updatedAt = LocalDateTime.now();
    }

    public void recordFailedLoginAttempt() {
        incrementFailedLoginAttempts();
    }

    public boolean isPasswordExpired() {
        return passwordExpiresAt != null && passwordExpiresAt.isBefore(LocalDateTime.now());
    }

    public void setPasswordExpiry(int days) {
        this.passwordExpiresAt = LocalDateTime.now().plusDays(days);
        this.updatedAt = LocalDateTime.now();
    }

    public void changePassword(String newEncodedPassword) {
        this.password = newEncodedPassword;
        this.passwordChangedAt = LocalDateTime.now();
        this.passwordExpiresAt = LocalDateTime.now().plusDays(90); // 90 days default
        this.updatedAt = LocalDateTime.now();
    }

    public boolean hasRole(String roleName) {
        return roles.stream().anyMatch(role -> role.getName().equals(roleName));
    }

    public boolean hasAnyRole(String... roleNames) {
        for (String roleName : roleNames) {
            if (hasRole(roleName)) {
                return true;
            }
        }
        return false;
    }

    public void addRole(Role role) {
        if (role != null && !roles.contains(role)) {
            roles.add(role);
            this.updatedAt = LocalDateTime.now();
        }
    }

    public void removeRole(Role role) {
        if (role != null && roles.contains(role)) {
            roles.remove(role);
            this.updatedAt = LocalDateTime.now();
        }
    }

    public String getStatusDisplay() {
        return status != null ? status.getMongolianName() : "Тодорхойгүй";
    }

    public void enable() {
        this.isActive = true;
        this.status = UserStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    public void disable() {
        this.isActive = false;
        this.status = UserStatus.INACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    public void suspend() {
        this.status = UserStatus.SUSPENDED;
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsDeleted() {
        this.isDeleted = true;
        this.isActive = false;
        this.updatedAt = LocalDateTime.now();
    }

    public void restore() {
        this.isDeleted = false;
        this.isActive = true;
        this.status = UserStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isAccountLocked() {
        return isLocked || (lockedUntil != null && lockedUntil.isAfter(LocalDateTime.now()));
    }

    // ==================== STANDARD GETTERS AND SETTERS ====================

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public void setUsername(String username) { 
        this.username = username;
        this.updatedAt = LocalDateTime.now();
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { 
        this.email = email;
        this.updatedAt = LocalDateTime.now();
    }

    public void setPassword(String password) { 
        this.password = password;
        this.updatedAt = LocalDateTime.now();
    }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { 
        this.firstName = firstName;
        this.updatedAt = LocalDateTime.now();
    }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { 
        this.lastName = lastName;
        this.updatedAt = LocalDateTime.now();
    }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { 
        this.phone = phone;
        this.updatedAt = LocalDateTime.now();
    }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { 
        this.employeeId = employeeId;
        this.updatedAt = LocalDateTime.now();
    }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { 
        this.department = department;
        this.updatedAt = LocalDateTime.now();
    }

    public String getPosition() { return position; }
    public void setPosition(String position) { 
        this.position = position;
        this.updatedAt = LocalDateTime.now();
    }

    public User getManager() { return manager; }
    public void setManager(User manager) { 
        this.manager = manager;
        this.updatedAt = LocalDateTime.now();
    }

    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { 
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public Boolean getIsEmailVerified() { return isEmailVerified; }
    public void setIsEmailVerified(Boolean isEmailVerified) { 
        this.isEmailVerified = isEmailVerified;
        this.updatedAt = LocalDateTime.now();
    }

    public Boolean getIsLocked() { return isLocked; }
    public void setIsLocked(Boolean isLocked) { 
        this.isLocked = isLocked;
        this.updatedAt = LocalDateTime.now();
    }

    public Integer getFailedLoginAttempts() { return failedLoginAttempts; }
    public void setFailedLoginAttempts(Integer failedLoginAttempts) { 
        this.failedLoginAttempts = failedLoginAttempts;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(LocalDateTime lastLoginAt) { 
        this.lastLoginAt = lastLoginAt;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getPasswordExpiresAt() { return passwordExpiresAt; }
    public void setPasswordExpiresAt(LocalDateTime passwordExpiresAt) { 
        this.passwordExpiresAt = passwordExpiresAt;
        this.updatedAt = LocalDateTime.now();
    }

    public List<User> getSubordinates() { return subordinates; }
    public void setSubordinates(List<User> subordinates) { 
        this.subordinates = subordinates;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * JPA relationship дээр List<Role> буцаах метод
     */
    public List<Role> getRoles() { 
        return roles; 
    }

    /**
     * List<Role> тохируулах метод - JPA relationship
     */
    public void setRoles(List<Role> roles) { 
        this.roles = roles != null ? roles : new ArrayList<>();
        this.updatedAt = LocalDateTime.now();
    }

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
    public void setIsActive(Boolean isActive) { 
        this.isActive = isActive;
        this.updatedAt = LocalDateTime.now();
    }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { 
        this.language = language;
        this.updatedAt = LocalDateTime.now();
    }

    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { 
        this.timezone = timezone;
        this.updatedAt = LocalDateTime.now();
    }

    public String getProfilePictureUrl() { return profilePictureUrl; }
    public void setProfilePictureUrl(String profilePictureUrl) { 
        this.profilePictureUrl = profilePictureUrl;
        this.updatedAt = LocalDateTime.now();
    }

    public Boolean getTwoFactorEnabled() { return twoFactorEnabled; }
    public void setTwoFactorEnabled(Boolean twoFactorEnabled) { 
        this.twoFactorEnabled = twoFactorEnabled;
        this.updatedAt = LocalDateTime.now();
    }

    public String getTwoFactorSecret() { return twoFactorSecret; }
    public void setTwoFactorSecret(String twoFactorSecret) { 
        this.twoFactorSecret = twoFactorSecret;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getPasswordChangedAt() { return passwordChangedAt; }
    public void setPasswordChangedAt(LocalDateTime passwordChangedAt) { 
        this.passwordChangedAt = passwordChangedAt;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getLockedUntil() { return lockedUntil; }
    public void setLockedUntil(LocalDateTime lockedUntil) { 
        this.lockedUntil = lockedUntil;
        this.updatedAt = LocalDateTime.now();
    }

    // Spring Security compatible getters
    public Boolean getAccountNonExpired() {
        return isAccountNonExpired();
    }

    public void setAccountNonExpired(Boolean accountNonExpired) {
        // This is derived from status, so we update status instead
        if (!accountNonExpired) {
            this.status = UserStatus.EXPIRED;
        }
        this.updatedAt = LocalDateTime.now();
    }

    public Boolean getAccountNonLocked() {
        return isAccountNonLocked();
    }

    public void setAccountNonLocked(Boolean accountNonLocked) {
        this.isLocked = !accountNonLocked;
        this.updatedAt = LocalDateTime.now();
    }

    public Boolean getCredentialsNonExpired() {
        return isCredentialsNonExpired();
    }

    public void setCredentialsNonExpired(Boolean credentialsNonExpired) {
        if (!credentialsNonExpired) {
            this.passwordExpiresAt = LocalDateTime.now().minusDays(1); // Make it expired
        } else {
            this.passwordExpiresAt = LocalDateTime.now().plusDays(90); // Extend for 90 days
        }
        this.updatedAt = LocalDateTime.now();
    }

    public Boolean getEnabled() {
        return isEnabled();
    }

    public void setEnabled(Boolean enabled) {
        this.isActive = enabled;
        if (enabled) {
            this.status = UserStatus.ACTIVE;
        } else {
            this.status = UserStatus.INACTIVE;
        }
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        this.updatedAt = now;
    }

    // toString
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", fullName='" + getFullName() + '\'' +
                ", status=" + status +
                ", department='" + department + '\'' +
                ", position='" + position + '\'' +
                ", isActive=" + isActive +
                ", isDeleted=" + isDeleted +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return id != null && id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}