package com.company.los.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Эрхийн Entity (Permission for RBAC)
 * Permission Entity for Role-Based Access Control
 */
@Entity
@Table(name = "permissions", indexes = {
        @Index(name = "idx_permission_name", columnList = "name", unique = true),
        @Index(name = "idx_permission_resource", columnList = "resource"),
        @Index(name = "idx_permission_action", columnList = "action"),
        @Index(name = "idx_permission_category", columnList = "category")
})
@SQLDelete(sql = "UPDATE permissions SET is_deleted = true WHERE id = ?")
@Where(clause = "is_deleted = false")
public class Permission {

    // Action enum as String for H2 compatibility
    public enum Action {
        CREATE("CREATE", "Үүсгэх"),
        READ("READ", "Харах"),
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

    @Id
    @Column(name = "id", length = 36)
    private String id;

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
    private String resource; // customer, loan_application, document, user, role, report

    @Column(name = "action", nullable = false, length = 20)
    @NotBlank(message = "Үйлдэл заавал тодорхойлох ёстой")
    @Size(max = 20, message = "Үйлдэл 20 тэмдэгтээс ихгүй байх ёстой")
    private String action; // CREATE, READ, UPDATE, DELETE, APPROVE, etc.

    @Column(name = "category", nullable = false, length = 50)
    @NotBlank(message = "Категори заавал тодорхойлох ёстой")
    @Size(max = 50, message = "Категори 50 тэмдэгтээс ихгүй байх ёстой")
    private String category;

    @Column(name = "scope", length = 20)
    @Size(max = 20, message = "Хамрах хүрээ 20 тэмдэгтээс ихгүй байх ёстой")
    private String scope; // OWN, BRANCH, ALL - хамрах хүрээ

    @Column(name = "is_system_permission", nullable = false)
    private Boolean isSystemPermission = false;

    @Column(name = "priority")
    @Min(value = 1, message = "Тэргүүлэх эрэмбэ 1-ээс бага байж болохгүй")
    @Max(value = 10, message = "Тэргүүлэх эрэмбэ 10-аас их байж болохгүй")
    private Integer priority = 5;

    // Дүрүүд
    @ManyToMany(mappedBy = "permissions", fetch = FetchType.LAZY)
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

    // Constructors
    public Permission() {
        this.id = java.util.UUID.randomUUID().toString();
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

    public boolean isAssignedToRole(String roleName) {
        return roles.stream()
                .anyMatch(role -> role.getName().equals(roleName));
    }

    public int getAssignedRoleCount() {
        return roles != null ? roles.size() : 0;
    }

    public int getAssignedUserCount() {
        return roles != null ? roles.stream()
                .mapToInt(role -> role.getUserCount())
                .sum() : 0;
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

    public void makeSystemPermission() {
        this.isSystemPermission = true;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean canBeDeleted() {
        return !isSystemPermission && (roles == null || roles.isEmpty());
    }

    public void addToRole(Role role) {
        if (role != null && (this.roles == null || !this.roles.contains(role))) {
            if (this.roles == null) {
                this.roles = new ArrayList<>();
            }
            this.roles.add(role);
            this.updatedAt = LocalDateTime.now();
        }
    }

    public void removeFromRole(Role role) {
        if (role != null && this.roles != null && this.roles.contains(role)) {
            this.roles.remove(role);
            this.updatedAt = LocalDateTime.now();
        }
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

    public String getActionDisplay() {
        Action actionEnum = getActionEnum();
        return actionEnum != null ? actionEnum.getMongolianName() : action;
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

    // Action/Category constants as inner classes
    public static final class Actions {
        public static final String CREATE = "CREATE";
        public static final String READ = "READ";
        public static final String UPDATE = "UPDATE";
        public static final String DELETE = "DELETE";
        public static final String APPROVE = "APPROVE";
        public static final String REJECT = "REJECT";
        public static final String ASSIGN = "ASSIGN";
        public static final String EXPORT = "EXPORT";
        public static final String IMPORT = "IMPORT";
        public static final String SEARCH = "SEARCH";
        public static final String PRINT = "PRINT";
        public static final String DOWNLOAD = "DOWNLOAD";
        public static final String UPLOAD = "UPLOAD";
        public static final String PROCESS = "PROCESS";
        public static final String REVIEW = "REVIEW";
        public static final String AUDIT = "AUDIT";
    }

    public static final class Categories {
        public static final String CUSTOMER_MANAGEMENT = "CUSTOMER_MANAGEMENT";
        public static final String LOAN_PROCESSING = "LOAN_PROCESSING";
        public static final String DOCUMENT_MANAGEMENT = "DOCUMENT_MANAGEMENT";
        public static final String USER_MANAGEMENT = "USER_MANAGEMENT";
        public static final String ROLE_MANAGEMENT = "ROLE_MANAGEMENT";
        public static final String REPORTING = "REPORTING";
        public static final String SYSTEM_ADMINISTRATION = "SYSTEM_ADMINISTRATION";
        public static final String FINANCIAL_OPERATIONS = "FINANCIAL_OPERATIONS";
        public static final String COMPLIANCE = "COMPLIANCE";
        public static final String AUDIT = "AUDIT";
    }

    public static final class Resources {
        public static final String CUSTOMER = "CUSTOMER";
        public static final String LOAN_APPLICATION = "LOAN_APPLICATION";
        public static final String DOCUMENT = "DOCUMENT";
        public static final String USER = "USER";
        public static final String ROLE = "ROLE";
        public static final String REPORT = "REPORT";
        public static final String SYSTEM = "SYSTEM";
        public static final String LOAN_PRODUCT = "LOAN_PRODUCT";
    }

    public static final class Scopes {
        public static final String OWN = "OWN";
        public static final String BRANCH = "BRANCH";
        public static final String ALL = "ALL";
    }

    // Static factory methods for common permissions
    public static Permission createCustomerRead() {
        return new Permission("CUSTOMER_READ", "View Customers", "Харилцагч харах", 
                            Resources.CUSTOMER, Actions.READ, Categories.CUSTOMER_MANAGEMENT);
    }

    public static Permission createCustomerCreate() {
        return new Permission("CUSTOMER_CREATE", "Create Customer", "Харилцагч үүсгэх", 
                            Resources.CUSTOMER, Actions.CREATE, Categories.CUSTOMER_MANAGEMENT);
    }

    public static Permission createLoanApprove() {
        return new Permission("LOAN_APPROVE", "Approve Loans", "Зээл зөвшөөрөх", 
                            Resources.LOAN_APPLICATION, Actions.APPROVE, Categories.LOAN_PROCESSING);
    }

    public static Permission createUserManage() {
        return new Permission("USER_MANAGE", "Manage Users", "Хэрэглэгч удирдах", 
                            Resources.USER, Actions.UPDATE, Categories.USER_MANAGEMENT);
    }

    public static Permission createSystemAdmin() {
        Permission permission = new Permission("SYSTEM_ADMIN", "System Administration", "Системийн удирдлага", 
                                             Resources.SYSTEM, Actions.UPDATE, Categories.SYSTEM_ADMINISTRATION);
        permission.makeSystemPermission();
        permission.setPriority(10);
        return permission;
    }

    // Permission Builder Pattern
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

        public Builder priority(Integer priority) {
            permission.priority = priority;
            return this;
        }

        public Builder scope(String scope) {
            permission.scope = scope;
            return this;
        }

        public Builder systemPermission() {
            permission.isSystemPermission = true;
            return this;
        }

        public Permission build() {
            return permission;
        }
    }

    public static Builder builder() {
        return new Builder();
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
    
    public String getResource() { return resource; }
    public void setResource(String resource) { 
        this.resource = resource;
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getAction() { return action; }
    public void setAction(String action) { 
        this.action = action;
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { 
        this.category = category;
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getScope() { return scope; }
    public void setScope(String scope) { 
        this.scope = scope;
        this.updatedAt = LocalDateTime.now();
    }
    
    public Boolean getIsSystemPermission() { return isSystemPermission; }
    public void setIsSystemPermission(Boolean isSystemPermission) { 
        this.isSystemPermission = isSystemPermission;
        this.updatedAt = LocalDateTime.now();
    }
    
    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { 
        this.priority = priority;
        this.updatedAt = LocalDateTime.now();
    }
    
    public List<Role> getRoles() { return roles; }
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
        return "Permission{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", resource='" + resource + '\'' +
                ", action='" + action + '\'' +
                ", category='" + category + '\'' +
                ", priority=" + priority +
                ", scope='" + scope + '\'' +
                ", isSystemPermission=" + isSystemPermission +
                ", roleCount=" + getAssignedRoleCount() +
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