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
 * Эрхийн Entity (Permission for RBAC)
 * Permission Entity for Role-Based Access Control
 * 
 * @author LOS Development Team
 * @version 3.0 - Complete Permission Entity with Business Logic
 * @since 2025-07-28
 */
@Entity
@Table(name = "permissions", indexes = {
        @Index(name = "idx_permission_name", columnList = "name", unique = true),
        @Index(name = "idx_permission_resource", columnList = "resource"),
        @Index(name = "idx_permission_action", columnList = "action"),
        @Index(name = "idx_permission_category", columnList = "category"),
        @Index(name = "idx_permission_resource_action", columnList = "resource,action")
})
@SQLDelete(sql = "UPDATE permissions SET is_deleted = true WHERE id = ?")
@Where(clause = "is_deleted = false")
public class Permission {

    // Action enum
    public enum Action {
        CREATE("CREATE", "Үүсгэх"),
        read("READ", "Харах"),
        UPDATE("UPDATE", "Засварлах"),
        DELETE("DELETE", "Устгах"),
        APPROVE("APPROVE", "Зөвшөөрөх"),
        REJECT("REJECT", "Татгалзах"),
        ASSIGN("ASSIGN", "Хуваарилах"),
        EXPORT("EXPORT", "Экспорт"),
        IMPORT("IMPORT", "Импорт"),
        SEARCH("SEARCH", "Хайх"),
        PRINT("PRINT", "Хэвлэх"),
        DOWNLOAD("DOWNLOAD", "Татах"),
        UPLOAD("UPLOAD", "Илгээх"),
        PROCESS("PROCESS", "Боловсруулах"),
        REVIEW("REVIEW", "Шалгах"),
        AUDIT("AUDIT", "Аудит");

        private final String code;
        private final String mongolianName;

        Action(String code, String mongolianName) {
            this.code = code;
            this.mongolianName = mongolianName;
        }

        public String getCode() { return code; }
        public String getMongolianName() { return mongolianName; }
    }

    // Permission category enum
    public enum PermissionCategory {
        SYSTEM("SYSTEM", "Системийн"),
        USER_MANAGEMENT("USER_MANAGEMENT", "Хэрэглэгчийн удирдлага"),
        ROLE_MANAGEMENT("ROLE_MANAGEMENT", "Дүрийн удирдлага"),
        LOAN_MANAGEMENT("LOAN_MANAGEMENT", "Зээлийн удирдлага"),
        CUSTOMER_MANAGEMENT("CUSTOMER_MANAGEMENT", "Харилцагчийн удирдлага"),
        REPORT("REPORT", "Тайлан"),
        AUDIT("AUDIT", "Аудит"),
        CONFIGURATION("CONFIGURATION", "Тохиргоо"),
        DOCUMENT_MANAGEMENT("DOCUMENT_MANAGEMENT", "Баримтын удирдлага"),
        SYSTEM_ADMINISTRATION("SYSTEM_ADMINISTRATION", "Системийн удирдлага"),
        FINANCIAL_OPERATIONS("FINANCIAL_OPERATIONS", "Санхүүгийн үйлдэл"),
        COMPLIANCE("COMPLIANCE", "Хуулийн нийцэл");

        private final String code;
        private final String mongolianName;

        PermissionCategory(String code, String mongolianName) {
            this.code = code;
            this.mongolianName = mongolianName;
        }

        public String getCode() { return code; }
        public String getMongolianName() { return mongolianName; }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "name", unique = true, nullable = false, length = 100)
    @NotBlank(message = "Эрхийн нэр заавал бөглөх ёстой")
    @Size(min = 3, max = 100, message = "Эрхийн нэр 3-100 тэмдэгт байх ёстой")
    private String name;

    @Column(name = "display_name", nullable = false, length = 100)
    @NotBlank(message = "Харагдах нэр заавал бөглөх ёстой")
    @Size(max = 100, message = "Харагдах нэр 100 тэмдэгтээс ихгүй байх ёстой")
    private String displayName;

    @Column(name = "display_name_mn", length = 100)
    @Size(max = 100, message = "Монгол нэр 100 тэмдэгтээс ихгүй байх ёстой")
    private String displayNameMn;

    @Column(name = "description", length = 500)
    @Size(max = 500, message = "Тайлбар 500 тэмдэгтээс ихгүй байх ёстой")
    private String description;

    @Column(name = "resource", nullable = false, length = 50)
    @NotBlank(message = "Ресурс заавал тодорхойлох ёстой")
    @Size(max = 50, message = "Ресурс 50 тэмдэгтээс ихгүй байх ёстой")
    private String resource;

    @Column(name = "action", nullable = false, length = 20)
    @NotBlank(message = "Үйлдэл заавал тодорхойлох ёстой")
    @Size(max = 20, message = "Үйлдэл 20 тэмдэгтээс ихгүй байх ёстой")
    private String action;

    @Column(name = "category", nullable = false, length = 50)
    @NotBlank(message = "Категори заавал тодорхойлох ёстой")
    @Size(max = 50, message = "Категори 50 тэмдэгтээс ихгүй байх ёстой")
    private String category;

    @Column(name = "scope", length = 20)
    @Size(max = 20, message = "Хамрах хүрээ 20 тэмдэгтээс ихгүй байх ёстой")
    private String scope;

    @Column(name = "is_system_permission", nullable = false)
    private Boolean isSystemPermission = false;

    @Column(name = "priority", nullable = false)
    @Min(value = 1, message = "Тэргүүлэх эрэмбэ 1-ээс бага байж болохгүй")
    @Max(value = 10, message = "Тэргүүлэх эрэмбэ 10-аас их байж болохгүй")
    private Integer priority = 5;

    @ManyToMany(mappedBy = "permissions", fetch = FetchType.LAZY)
    private List<Role> roles = new ArrayList<>();

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
    public Permission() {
        this.id = UUID.randomUUID();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Permission(String name, String displayName, String resource, String action, String category) {
        this();
        this.name = name;
        this.displayName = displayName;
        this.resource = resource;
        this.action = action;
        this.category = category;
    }

    public Permission(String name, String displayName, String displayNameMn, String resource, String action, String category) {
        this(name, displayName, resource, action, category);
        this.displayNameMn = displayNameMn;
    }

    // Business methods
    public String getFullName() {
        return resource.toUpperCase() + "_" + action.toUpperCase();
    }

    public String getLocalizedDisplayName() {
        return displayNameMn != null && !displayNameMn.trim().isEmpty() ? displayNameMn : displayName;
    }

    public String getActionDisplay() {
        Action actionEnum = getActionEnum();
        return actionEnum != null ? actionEnum.getMongolianName() : action;
    }

    public boolean isAssignedToRole(String roleName) {
        return roles.stream().anyMatch(role -> role.getName().equals(roleName));
    }

    public int getAssignedRoleCount() {
        return roles != null ? roles.size() : 0;
    }

    public int getAssignedUserCount() {
        return roles != null ? roles.stream().mapToInt(role -> role.getUserCount()).sum() : 0;
    }

    public boolean isHighPriority() {
        return priority != null && priority >= 8;
    }

    public boolean isMediumPriority() {
        return priority != null && priority >= 4 && priority < 8;
    }

    public boolean isLowPriority() {
        return priority != null && priority < 4;
    }

    public boolean canBeDeleted() {
        return !isSystemPermission && (roles == null || roles.isEmpty());
    }

    public Action getActionEnum() {
        if (action != null) {
            try {
                return Action.valueOf(action.toUpperCase());
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * Fixed isResourceAction method
     */
    public boolean isResourceAction(String resource, String action) {
        return this.resource != null && this.resource.equals(resource) && 
               this.action != null && this.action.equals(action);
    }

    public void addRole(Role role) {
        if (role != null && !roles.contains(role)) {
            roles.add(role);
            if (!role.getPermissions().contains(this)) {
                role.getPermissions().add(this);
            }
            this.updatedAt = LocalDateTime.now();
        }
    }

    public void removeRole(Role role) {
        if (role != null && roles.contains(role)) {
            roles.remove(role);
            role.getPermissions().remove(this);
            this.updatedAt = LocalDateTime.now();
        }
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

    // Builder Pattern
    public static class Builder {
        private Permission permission = new Permission();

        public Builder name(String name) {
            permission.name = name;
            return this;
        }

        public Builder displayName(String displayName) {
            permission.displayName = displayName;
            return this;
        }

        public Builder displayNameMn(String displayNameMn) {
            permission.displayNameMn = displayNameMn;
            return this;
        }

        public Builder description(String description) {
            permission.description = description;
            return this;
        }

        public Builder resource(String resource) {
            permission.resource = resource;
            return this;
        }

        public Builder action(String action) {
            permission.action = action;
            return this;
        }

        public Builder category(String category) {
            permission.category = category;
            return this;
        }

        public Builder scope(String scope) {
            permission.scope = scope;
            return this;
        }

        public Builder isSystemPermission(boolean isSystemPermission) {
            permission.isSystemPermission = isSystemPermission;
            return this;
        }

        public Builder priority(Integer priority) {
            permission.priority = priority;
            return this;
        }

        public Builder createdBy(String createdBy) {
            permission.createdBy = createdBy;
            return this;
        }

        public Builder updatedBy(String updatedBy) {
            permission.updatedBy = updatedBy;
            return this;
        }

        public Builder isActive(boolean isActive) {
            permission.isActive = isActive;
            return this;
        }

        public Permission build() {
            if (permission.name == null || permission.name.isEmpty()) {
                throw new IllegalStateException("Permission name is required");
            }
            if (permission.displayName == null || permission.displayName.isEmpty()) {
                throw new IllegalStateException("Display name is required");
            }
            if (permission.resource == null || permission.resource.isEmpty()) {
                throw new IllegalStateException("Resource is required");
            }
            if (permission.action == null || permission.action.isEmpty()) {
                throw new IllegalStateException("Action is required");
            }
            if (permission.category == null || permission.category.isEmpty()) {
                throw new IllegalStateException("Category is required");
            }
            if (permission.priority == null) {
                permission.priority = 5;
            }
            return permission;
        }
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getDisplayNameMn() { return displayNameMn; }
    public void setDisplayNameMn(String displayNameMn) { this.displayNameMn = displayNameMn; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getResource() { return resource; }
    public void setResource(String resource) { this.resource = resource; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getScope() { return scope; }
    public void setScope(String scope) { this.scope = scope; }

    public Boolean getIsSystemPermission() { return isSystemPermission; }
    public void setIsSystemPermission(Boolean isSystemPermission) { this.isSystemPermission = isSystemPermission; }

    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }

    public List<Role> getRoles() { return roles; }
    public void setRoles(List<Role> roles) { this.roles = roles; }

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

    @Override
    public String toString() {
        return "Permission{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", displayName='" + displayName + '\'' +
                ", resource='" + resource + '\'' +
                ", action='" + action + '\'' +
                ", category='" + category + '\'' +
                ", priority=" + priority +
                ", isSystemPermission=" + isSystemPermission +
                ", isActive=" + isActive +
                ", isDeleted=" + isDeleted +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Permission)) return false;
        Permission permission = (Permission) o;
        return id != null && id.equals(permission.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}