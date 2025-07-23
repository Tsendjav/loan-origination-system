package com.company.los.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Дүрийн Entity
 * Role Entity
 */
@Entity
@Table(name = "roles", indexes = {
        @Index(name = "idx_role_name", columnList = "name", unique = true)
})
@SQLDelete(sql = "UPDATE roles SET is_deleted = true WHERE id = ?")
@Where(clause = "is_deleted = false")
public class Role {

    // Enum definitions
    public enum RoleType {
        SYSTEM("SYSTEM", "Системийн дүр"),
        BUSINESS("BUSINESS", "Бизнесийн дүр"),
        CUSTOM("CUSTOM", "Тусгай дүр");

        private final String code;
        private final String mongolianName;

        RoleType(String code, String mongolianName) {
            this.code = code;
            this.mongolianName = mongolianName;
        }

        public String getCode() { return code; }
        public String getMongolianName() { return mongolianName; }
    }

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "name", unique = true, nullable = false, length = 100)
    @NotBlank(message = "Дүрийн нэр заавал байх ёстой")
    @Size(max = 100, message = "Дүрийн нэр 100 тэмдэгтээс ихгүй байх ёстой")
    private String name;

    @Column(name = "display_name", nullable = false, length = 100)
    @NotBlank(message = "Харуулах нэр заавал байх ёстой")
    @Size(max = 100, message = "Харуулах нэр 100 тэмдэгтээс ихгүй байх ёстой")
    private String displayName;

    @Column(name = "display_name_mn", length = 100)
    @Size(max = 100, message = "Монгол харуулах нэр 100 тэмдэгтээс ихгүй байх ёстой")
    private String displayNameMn;

    @Column(name = "description", length = 500)
    @Size(max = 500, message = "Тайлбар 500 тэмдэгтээс ихгүй байх ёстой")
    private String description;

    // Дүрийн шинж чанарууд
    @Column(name = "is_system_role", nullable = false)
    private Boolean isSystemRole = false;

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;

    @Column(name = "level_order")
    @Min(value = 1, message = "Дүрийн түвшин 1-ээс бага байж болохгүй")
    private Integer levelOrder = 1;

    // Эрхүүд
    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private List<Permission> permissions = new ArrayList<>();

    // Хэрэглэгчид
    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    private List<User> users = new ArrayList<>();

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
        this.id = java.util.UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Role(String name, String displayName) {
        this();
        this.name = name;
        this.displayName = displayName;
    }

    public Role(String name, String displayName, String description) {
        this(name, displayName);
        this.description = description;
    }

    // Business methods
    public boolean canAssignTo(User user) {
        // System roles can only be assigned by system administrators
        if (isSystemRole && !user.hasAnyRole("ROLE_SYSTEM_ADMIN")) {
            return false;
        }
        return true;
    }

    public boolean hasPermission(String permissionName) {
        return permissions.stream()
                .anyMatch(permission -> permission.getName().equals(permissionName));
    }

    public void addPermission(Permission permission) {
        if (permission != null && !permissions.contains(permission)) {
            permissions.add(permission);
            this.updatedAt = LocalDateTime.now();
        }
    }

    public void removePermission(Permission permission) {
        if (permission != null && permissions.contains(permission)) {
            permissions.remove(permission);
            this.updatedAt = LocalDateTime.now();
        }
    }

    public void addUser(User user) {
        if (user != null && !users.contains(user)) {
            users.add(user);
            this.updatedAt = LocalDateTime.now();
        }
    }

    public void removeUser(User user) {
        if (user != null && users.contains(user)) {
            users.remove(user);
            this.updatedAt = LocalDateTime.now();
        }
    }

    public String getDisplayNameOrDefault() {
        return displayNameMn != null && !displayNameMn.trim().isEmpty() ? displayNameMn : displayName;
    }

    public String getLocalizedDisplayName() {
        return getDisplayNameOrDefault();
    }

    public RoleType getRoleType() {
        if (isSystemRole) {
            return RoleType.SYSTEM;
        } else if (name != null && (name.contains("CUSTOM") || name.contains("SPECIAL"))) {
            return RoleType.CUSTOM;
        }
        return RoleType.BUSINESS;
    }

    public String getRoleTypeDisplay() {
        return getRoleType().getMongolianName();
    }

    public boolean canBeDeleted() {
        return !isSystemRole && users.isEmpty();
    }

    public boolean isEditable() {
        return !isSystemRole;
    }

    public int getPermissionCount() {
        return permissions != null ? permissions.size() : 0;
    }

    public int getUserCount() {
        return users != null ? users.size() : 0;
    }

    public void markAsDeleted() {
        this.isDeleted = true;
        this.isActive = false;
        this.updatedAt = LocalDateTime.now();
    }

    public void restore() {
        this.isDeleted = false;
        this.isActive = true;
        this.updatedAt = LocalDateTime.now();
    }

    public void enable() {
        this.isActive = true;
        this.updatedAt = LocalDateTime.now();
    }

    public void disable() {
        this.isActive = false;
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { 
        this.name = name;
        this.updatedAt = LocalDateTime.now();
    }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { 
        this.displayName = displayName;
        this.updatedAt = LocalDateTime.now();
    }

    public String getDisplayNameMn() { return displayNameMn; }
    public void setDisplayNameMn(String displayNameMn) { 
        this.displayNameMn = displayNameMn;
        this.updatedAt = LocalDateTime.now();
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { 
        this.description = description;
        this.updatedAt = LocalDateTime.now();
    }

    public Boolean getIsSystemRole() { return isSystemRole; }
    public void setIsSystemRole(Boolean isSystemRole) { 
        this.isSystemRole = isSystemRole;
        this.updatedAt = LocalDateTime.now();
    }

    public Boolean getIsDefault() { return isDefault; }
    public void setIsDefault(Boolean isDefault) { 
        this.isDefault = isDefault;
        this.updatedAt = LocalDateTime.now();
    }

    public Integer getLevelOrder() { return levelOrder; }
    public void setLevelOrder(Integer levelOrder) { 
        this.levelOrder = levelOrder;
        this.updatedAt = LocalDateTime.now();
    }

    public List<Permission> getPermissions() { return permissions; }
    public void setPermissions(List<Permission> permissions) { 
        this.permissions = permissions != null ? permissions : new ArrayList<>();
        this.updatedAt = LocalDateTime.now();
    }

    public List<User> getUsers() { return users; }
    public void setUsers(List<User> users) { 
        this.users = users != null ? users : new ArrayList<>();
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
        if (this.id == null) {
            this.id = java.util.UUID.randomUUID().toString();
        }
        LocalDateTime now = LocalDateTime.now();
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        this.updatedAt = now;
    }

    // toString
    @Override
    public String toString() {
        return "Role{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", displayName='" + displayName + '\'' +
                ", description='" + description + '\'' +
                ", isSystemRole=" + isSystemRole +
                ", isDefault=" + isDefault +
                ", levelOrder=" + levelOrder +
                ", permissionCount=" + getPermissionCount() +
                ", userCount=" + getUserCount() +
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