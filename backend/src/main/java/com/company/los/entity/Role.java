package com.company.los.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Дүрийн Entity
 * Role Entity for authorization
 * 
 * @author LOS Development Team
 * @version 3.0 - Complete Role Entity with Hierarchy and Business Logic
 * @since 2025-07-28
 */
@Entity
@Table(name = "roles", indexes = {
        @Index(name = "idx_role_name", columnList = "name", unique = true),
        @Index(name = "idx_role_code", columnList = "code"),
        @Index(name = "idx_role_type", columnList = "type"),
        @Index(name = "idx_role_status", columnList = "status"),
        @Index(name = "idx_role_priority", columnList = "priority"),
        @Index(name = "idx_role_active", columnList = "is_active")
})
@SQLDelete(sql = "UPDATE roles SET is_deleted = true WHERE id = ?")
@Where(clause = "is_deleted = false")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "name", unique = true, nullable = false, length = 100)
    @NotBlank(message = "Дүрийн нэр заавал бөглөх ёстой")
    @Size(min = 2, max = 100, message = "Дүрийн нэр 2-100 тэмдэгт байх ёстой")
    private String name;

    @Column(name = "description", length = 500)
    @Size(max = 500, message = "Дүрийн тайлбар 500 тэмдэгтээс ихгүй байх ёстой")
    private String description;

    @Column(name = "code", length = 50)
    @Size(max = 50, message = "Дүрийн код 50 тэмдэгтээс ихгүй байх ёстой")
    private String code;

    // Role status enum
    public enum RoleStatus {
        ACTIVE("ACTIVE", "Идэвхтэй"),
        INACTIVE("INACTIVE", "Идэвхгүй"),
        SUSPENDED("SUSPENDED", "Түр хориглосон"),
        DEPRECATED("DEPRECATED", "Хуучирсан");

        private final String code;
        private final String mongolianName;

        RoleStatus(String code, String mongolianName) {
            this.code = code;
            this.mongolianName = mongolianName;
        }

        public String getCode() { return code; }
        public String getMongolianName() { return mongolianName; }
    }

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Дүрийн статус заавал байх ёстой")
    private RoleStatus status = RoleStatus.ACTIVE;

    // Role type enum
    public enum RoleType {
        SYSTEM("SYSTEM", "Системийн"),
        BUSINESS("BUSINESS", "Бизнесийн"),
        FUNCTIONAL("FUNCTIONAL", "Үүргийн"),
        TEMPORARY("TEMPORARY", "Түр зуурын");

        private final String code;
        private final String mongolianName;

        RoleType(String code, String mongolianName) {
            this.code = code;
            this.mongolianName = mongolianName;
        }

        public String getCode() { return code; }
        public String getMongolianName() { return mongolianName; }
    }

    @Column(name = "type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Дүрийн төрөл заавал байх ёстой")
    private RoleType type = RoleType.BUSINESS;

    // Priority for role hierarchy (1-100, higher number = higher priority)
    @Column(name = "priority")
    @Min(value = 1, message = "Эрэмбэ 1-ээс бага байж болохгүй")
    @Max(value = 100, message = "Эрэмбэ 100-аас их байж болохгүй")
    private Integer priority = 50;

    // System role flag (derived from type)
    @Transient
    public boolean isSystemRole() {
        return RoleType.SYSTEM.equals(this.type);
    }

    // Default role flag - for commonly assigned roles
    @Column(name = "is_default")
    private Boolean isDefault = false;

    // Display name for UI
    @Column(name = "display_name", length = 150)
    private String displayName;

    // Role hierarchy
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_role_id", foreignKey = @ForeignKey(name = "fk_role_parent"))
    private Role parentRole;

    @OneToMany(mappedBy = "parentRole", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Role> childRoles = new ArrayList<>();

    // Many-to-many relationship with Users
    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    private List<User> users = new ArrayList<>();

    // Many-to-many relationship with Permissions
    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private List<Permission> permissions = new ArrayList<>();

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

    // Constructors
    public Role() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Role(String name, String description) {
        this();
        this.name = name;
        this.description = description;
        this.displayName = name;
    }

    public Role(String name, String description, RoleType type) {
        this(name, description);
        this.type = type;
    }

    // Business methods
    public String getDisplayName() {
        if (displayName != null && !displayName.trim().isEmpty()) {
            return displayName;
        }
        if (description != null && !description.trim().isEmpty()) {
            return name + " (" + description + ")";
        }
        return name;
    }

    public boolean isActive() {
        return RoleStatus.ACTIVE.equals(status) && Boolean.TRUE.equals(isActive);
    }

    public boolean isBusinessRole() {
        return RoleType.BUSINESS.equals(type);
    }

    public boolean hasPermission(String permissionName) {
        return permissions.stream()
                .anyMatch(permission -> permission.getName().equals(permissionName));
    }

    public boolean hasAnyPermission(String... permissionNames) {
        for (String permissionName : permissionNames) {
            if (hasPermission(permissionName)) {
                return true;
            }
        }
        return false;
    }

    public void addPermission(Permission permission) {
        if (permission != null && !permissions.contains(permission)) {
            permissions.add(permission);
            permission.getRoles().add(this);
            this.updatedAt = LocalDateTime.now();
        }
    }

    public void removePermission(Permission permission) {
        if (permission != null && permissions.contains(permission)) {
            permissions.remove(permission);
            permission.getRoles().remove(this);
            this.updatedAt = LocalDateTime.now();
        }
    }

    public void addUser(User user) {
        if (user != null && !users.contains(user)) {
            users.add(user);
            user.getRoles().add(this);
            this.updatedAt = LocalDateTime.now();
        }
    }

    public void removeUser(User user) {
        if (user != null && users.contains(user)) {
            users.remove(user);
            user.getRoles().remove(this);
            this.updatedAt = LocalDateTime.now();
        }
    }

    public void addChildRole(Role childRole) {
        if (childRole != null && !childRoles.contains(childRole)) {
            childRoles.add(childRole);
            childRole.setParentRole(this);
            this.updatedAt = LocalDateTime.now();
        }
    }

    public void removeChildRole(Role childRole) {
        if (childRole != null && childRoles.contains(childRole)) {
            childRoles.remove(childRole);
            childRole.setParentRole(null);
            this.updatedAt = LocalDateTime.now();
        }
    }

    public boolean hasChildRoles() {
        return childRoles != null && !childRoles.isEmpty();
    }

    public boolean hasParentRole() {
        return parentRole != null;
    }

    public boolean hasUsers() {
        return users != null && !users.isEmpty();
    }

    public boolean hasPermissions() {
        return permissions != null && !permissions.isEmpty();
    }

    public int getUserCount() {
        return users != null ? users.size() : 0;
    }

    public int getPermissionCount() {
        return permissions != null ? permissions.size() : 0;
    }

    public int getChildRoleCount() {
        return childRoles != null ? childRoles.size() : 0;
    }

    public String getStatusDisplay() {
        return status != null ? status.getMongolianName() : "Тодорхойгүй";
    }

    public String getTypeDisplay() {
        return type != null ? type.getMongolianName() : "Тодорхойгүй";
    }

    public String getHierarchyLevel() {
        if (parentRole == null) {
            return "Эх дүр";
        } else if (hasChildRoles()) {
            return "Завсрын дүр";
        } else {
            return "Дэд дүр";
        }
    }

    public void enable() {
        this.isActive = true;
        this.status = RoleStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    public void disable() {
        this.isActive = false;
        this.status = RoleStatus.INACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    public void suspend() {
        this.status = RoleStatus.SUSPENDED;
        this.updatedAt = LocalDateTime.now();
    }

    public void deprecate() {
        this.status = RoleStatus.DEPRECATED;
        this.isActive = false;
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
        this.status = RoleStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { 
        this.name = name;
        if (this.displayName == null || this.displayName.isEmpty()) {
            this.displayName = name;
        }
        this.updatedAt = LocalDateTime.now();
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { 
        this.description = description;
        this.updatedAt = LocalDateTime.now();
    }

    public String getCode() { return code; }
    public void setCode(String code) { 
        this.code = code;
        this.updatedAt = LocalDateTime.now();
    }

    public RoleStatus getStatus() { return status; }
    public void setStatus(RoleStatus status) { 
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public RoleType getType() { return type; }
    public void setType(RoleType type) { 
        this.type = type;
        this.updatedAt = LocalDateTime.now();
    }

    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { 
        this.priority = priority;
        this.updatedAt = LocalDateTime.now();
    }

    public Boolean getIsDefault() { return isDefault; }
    public void setIsDefault(Boolean isDefault) { 
        this.isDefault = isDefault;
        this.updatedAt = LocalDateTime.now();
    }

    public void setDisplayName(String displayName) { 
        this.displayName = displayName;
        this.updatedAt = LocalDateTime.now();
    }

    public Role getParentRole() { return parentRole; }
    public void setParentRole(Role parentRole) { 
        this.parentRole = parentRole;
        this.updatedAt = LocalDateTime.now();
    }

    public List<Role> getChildRoles() { return childRoles; }
    public void setChildRoles(List<Role> childRoles) { 
        this.childRoles = childRoles;
        this.updatedAt = LocalDateTime.now();
    }

    public List<User> getUsers() { return users; }
    public void setUsers(List<User> users) { 
        this.users = users;
        this.updatedAt = LocalDateTime.now();
    }

    public List<Permission> getPermissions() { return permissions; }
    public void setPermissions(List<Permission> permissions) { 
        this.permissions = permissions;
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
        if (this.displayName == null && this.name != null) {
            this.displayName = this.name;
        }
    }

    // toString
    @Override
    public String toString() {
        return "Role{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", code='" + code + '\'' +
                ", status=" + status +
                ", type=" + type +
                ", priority=" + priority +
                ", userCount=" + getUserCount() +
                ", permissionCount=" + getPermissionCount() +
                ", isActive=" + isActive +
                ", isDeleted=" + isDeleted +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Role)) return false;
        Role role = (Role) o;
        return id != null && id.equals(role.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}