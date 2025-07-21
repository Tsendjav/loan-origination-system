package com.los.entity;

import com.los.common.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Хэрэглэгчийн Entity
 * User Entity - Spring Security UserDetails implementation
 */
@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_user_username", columnList = "username", unique = true),
        @Index(name = "idx_user_email", columnList = "email", unique = true),
        @Index(name = "idx_user_employee_id", columnList = "employee_id"),
        @Index(name = "idx_user_status", columnList = "status"),
        @Index(name = "idx_user_department", columnList = "department")
})
@SQLDelete(sql = "UPDATE users SET is_deleted = true WHERE id = ?")
@Where(clause = "is_deleted = false")
public class User extends BaseEntity implements UserDetails {

    @Column(name = "username", unique = true, nullable = false, length = 50)
    @NotBlank(message = "Хэрэглэгчийн нэр заавал бөглөх ёстой")
    @Size(min = 3, max = 50, message = "Хэрэглэгчийн нэр 3-50 тэмдэгт байх ёстой")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Хэрэглэгчийн нэрэнд зөвхөн үсэг, тоо, цэг, доор зураас ашиглана уу")
    private String username;

    @Column(name = "password", nullable = false, length = 100)
    @NotBlank(message = "Нууц үг заавал бөглөх ёстой")
    @Size(min = 8, max = 100, message = "Нууц үг 8-100 тэмдэгт байх ёстой")
    private String password;

    @Column(name = "email", unique = true, nullable = false, length = 100)
    @NotBlank(message = "И-мэйл заавал бөглөх ёстой")
    @Email(message = "И-мэйлийн формат буруу")
    @Size(max = 100, message = "И-мэйл 100 тэмдэгтээс ихгүй байх ёстой")
    private String email;

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

    @Column(name = "employee_id", length = 20)
    @Size(max = 20, message = "Ажилтны дугаар 20 тэмдэгтээс ихгүй байх ёстой")
    private String employeeId;

    @Column(name = "position", length = 100)
    @Size(max = 100, message = "Албан тушаал 100 тэмдэгтээс ихгүй байх ёстой")
    private String position;

    @Column(name = "department", length = 100)
    @Size(max = 100, message = "Хэлтэс 100 тэмдэгтээс ихгүй байх ёстой")
    private String department;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @NotNull(message = "Хэрэглэгчийн статус заавал байх ёстой")
    private UserStatus status = UserStatus.ACTIVE;

    // Security мэдээлэл
    @Column(name = "account_non_expired", nullable = false)
    private Boolean accountNonExpired = true;

    @Column(name = "account_non_locked", nullable = false)
    private Boolean accountNonLocked = true;

    @Column(name = "credentials_non_expired", nullable = false)
    private Boolean credentialsNonExpired = true;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    @Column(name = "failed_login_attempts")
    @Min(value = 0, message = "Амжилтгүй нэвтрэх оролдлого сөрөг байж болохгүй")
    private Integer failedLoginAttempts = 0;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    // Two-Factor Authentication
    @Column(name = "two_factor_enabled", nullable = false)
    private Boolean twoFactorEnabled = false;

    @Column(name = "two_factor_secret", length = 32)
    private String twoFactorSecret;

    // Profile мэдээлэл
    @Column(name = "profile_picture_url", length = 500)
    private String profilePictureUrl;

    @Column(name = "language", length = 10)
    private String language = "mn"; // mn, en

    @Column(name = "timezone", length = 50)
    private String timezone = "Asia/Ulaanbaatar";

    // Дүрүүд
    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"),
            indexes = {
                    @Index(name = "idx_user_roles_user", columnList = "user_id"),
                    @Index(name = "idx_user_roles_role", columnList = "role_id")
            }
    )
    private Set<Role> roles = new HashSet<>();

    // User Status Enum
    public enum UserStatus {
        ACTIVE("Идэвхтэй"),
        INACTIVE("Идэвхгүй"),
        LOCKED("Түгжээтэй"),
        SUSPENDED("Түр зогсоосон"),
        PENDING_ACTIVATION("Идэвхжүүлэх хүлээлттэй");

        private final String mongolianName;

        UserStatus(String mongolianName) {
            this.mongolianName = mongolianName;
        }

        public String getMongolianName() {
            return mongolianName;
        }
    }

    // Constructors
    public User() {
        super();
        this.passwordChangedAt = LocalDateTime.now();
    }

    public User(String username, String password, String email, String firstName, String lastName) {
        this();
        this.username = username;
        this.password = password;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    // Business methods
    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getDisplayName() {
        String fullName = getFullName();
        return fullName.trim().isEmpty() ? username : fullName;
    }

    public void addRole(Role role) {
        this.roles.add(role);
        role.getUsers().add(this);
    }

    public void removeRole(Role role) {
        this.roles.remove(role);
        role.getUsers().remove(this);
    }

    public boolean hasRole(String roleName) {
        return roles.stream().anyMatch(role -> role.getName().equals(roleName));
    }

    public boolean hasAnyRole(String... roleNames) {
        return Arrays.stream(roleNames)
                .anyMatch(this::hasRole);
    }

    public void lockAccount(LocalDateTime until) {
        this.accountNonLocked = false;
        this.lockedUntil = until;
        this.status = UserStatus.LOCKED;
    }

    public void unlockAccount() {
        this.accountNonLocked = true;
        this.lockedUntil = null;
        this.failedLoginAttempts = 0;
        this.status = UserStatus.ACTIVE;
    }

    public void recordFailedLoginAttempt() {
        this.failedLoginAttempts++;
        if (this.failedLoginAttempts >= 5) {
            lockAccount(LocalDateTime.now().plusMinutes(30)); // 30 мин түгжих
        }
    }

    public void recordSuccessfulLogin() {
        this.failedLoginAttempts = 0;
        this.lastLoginAt = LocalDateTime.now();
        this.lockedUntil = null;
    }

    public void changePassword(String newPassword) {
        this.password = newPassword;
        this.passwordChangedAt = LocalDateTime.now();
        this.credentialsNonExpired = true;
    }

    public boolean isAccountLocked() {
        return !accountNonLocked || (lockedUntil != null && lockedUntil.isAfter(LocalDateTime.now()));
    }

    public boolean isPasswordExpired() {
        if (passwordChangedAt == null) return true;
        // 90 хоногийн дараа нууц үг хуучирна
        return passwordChangedAt.plusDays(90).isBefore(LocalDateTime.now());
    }

    public void enable() {
        this.enabled = true;
        this.status = UserStatus.ACTIVE;
    }

    public void disable() {
        this.enabled = false;
        this.status = UserStatus.INACTIVE;
    }

    public void suspend() {
        this.enabled = false;
        this.status = UserStatus.SUSPENDED;
    }

    // Spring Security UserDetails implementation
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(permission -> new SimpleGrantedAuthority(permission.getName()))
                .collect(Collectors.toSet());
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
        return accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked && !isAccountLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired && !isPasswordExpired();
    }

    @Override
    public boolean isEnabled() {
        return enabled && status == UserStatus.ACTIVE;
    }

    // Getters and Setters
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }
    
    public Set<Role> getRoles() { return roles; }
    public void setRoles(Set<Role> roles) { this.roles = roles; }
    
    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(LocalDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; }

    // toString
    @Override
    public String toString() {
        return "User{" +
                "id=" + getId() +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", fullName='" + getFullName() + '\'' +
                ", status=" + status +
                ", enabled=" + enabled +
                '}';
    }
}