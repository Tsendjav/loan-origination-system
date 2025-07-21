package com.company.los.entity;

import com.company.los.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.util.HashSet;
import java.util.Set;

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
public class Permission extends BaseEntity {

    @Column(name = "name", unique = true, nullable = false, length = 100)
    @NotBlank(message = "Эрхийн нэр заавал бөглөх ёстой")
    @Size(min = 3, max = 100, message = "Эрхийн нэр 3-100 тэмдэгт байх ёстой")
    @Pattern(regexp = "^[A-Z_]+$", message = "Эрхийн нэр том үсэг болон доор зураас ашиглана уу")
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

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 20)
    @NotNull(message = "Үйлдэл заавал сонгох ёстой")
    private Action action; // CREATE, READ, UPDATE, DELETE, APPROVE, etc.

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 30)
    @NotNull(message = "Категори заавал сонгох ёстой")
    private Category category;

    @Column(name = "is_system_permission", nullable = false)
    private Boolean isSystemPermission = false;

    @Column(name = "priority", nullable = false)
    @Min(value = 1, message = "Тэргүүлэх эрэмбэ 1-ээс бага байж болохгүй")
    @Max(value = 10, message = "Тэргүүлэх эрэмбэ 10-аас их байж болохгүй")
    private Integer priority = 5;

    @Column(name = "scope", length = 20)
    private String scope; // OWN, BRANCH, ALL - хамрах хүрээ

    // Дүрүүд
    @ManyToMany(mappedBy = "permissions", fetch = FetchType.LAZY)
    private Set<Role> roles = new HashSet<>();

    // Үйлдлүүд
    public enum Action {
        CREATE("Үүсгэх", "Create new records"),
        READ("Унших", "View and read data"),
        UPDATE("Засах", "Modify existing records"),
        DELETE("Устгах", "Remove records"),
        APPROVE("Зөвшөөрөх", "Approve applications or requests"),
        REJECT("Татгалзах", "Reject applications or requests"),
        ASSIGN("Хувьлах", "Assign tasks or responsibilities"),
        EXPORT("Экспорт", "Export data to files"),
        IMPORT("Импорт", "Import data from files"),
        SEARCH("Хайх", "Search through data"),
        PRINT("Хэвлэх", "Print documents or reports"),
        DOWNLOAD("Татах", "Download files or documents"),
        UPLOAD("Илгээх", "Upload files or documents"),
        PROCESS("Боловсруулах", "Process applications or documents"),
        REVIEW("Хянах", "Review and validate data"),
        AUDIT("Аудит", "Audit and monitor activities");

        private final String mongolianName;
        private final String description;

        Action(String mongolianName, String description) {
            this.mongolianName = mongolianName;
            this.description = description;
        }

        public String getMongolianName() { return mongolianName; }
        public String getDescription() { return description; }
    }

    // Категориуд
    public enum Category {
        CUSTOMER_MANAGEMENT("Харилцагчийн удирдлага"),
        LOAN_PROCESSING("Зээлийн боловсруулалт"),
        DOCUMENT_MANAGEMENT("Баримт бичгийн удирдлага"),
        USER_MANAGEMENT("Хэрэглэгчийн удирдлага"),
        ROLE_MANAGEMENT("Дүрийн удирдлага"),
        REPORTING("Тайлагналт"),
        SYSTEM_ADMINISTRATION("Системийн удирдлага"),
        FINANCIAL_OPERATIONS("Санхүүгийн үйл ажиллагаа"),
        COMPLIANCE("Хяналт зөвшөөрөл"),
        AUDIT("Аудит");

        private final String mongolianName;

        Category(String mongolianName) {
            this.mongolianName = mongolianName;
        }

        public String getMongolianName() { return mongolianName; }
    }

    // Constructors
    public Permission() {
        super();
    }

    public Permission(String name, String displayName, String resource, Action action, Category category) {
        this();
        this.name = name;
        this.displayName = displayName;
        this.resource = resource;
        this.action = action;
        this.category = category;
    }

    public Permission(String name, String displayName, String displayNameMn, String resource, Action action, Category category) {
        this(name, displayName, resource, action, category);
        this.displayNameMn = displayNameMn;
    }

    // Business methods
    public String getFullName() {
        return resource.toUpperCase() + "_" + action.name();
    }

    public String getLocalizedDisplayName() {
        return displayNameMn != null && !displayNameMn.trim().isEmpty() ? displayNameMn : displayName;
    }

    public boolean isAssignedToRole(String roleName) {
        return roles.stream()
                .anyMatch(role -> role.getName().equals(roleName));
    }

    public int getAssignedRoleCount() {
        return roles.size();
    }

    public int getAssignedUserCount() {
        return roles.stream()
                .mapToInt(role -> role.getUsers().size())
                .sum();
    }

    public boolean isHighPriority() {
        return priority >= 8;
    }

    public boolean isMediumPriority() {
        return priority >= 4 && priority < 8;
    }

    public boolean isLowPriority() {
        return priority < 4;
    }

    public void makeSystemPermission() {
        this.isSystemPermission = true;
    }

    public boolean canBeDeleted() {
        return !isSystemPermission && roles.isEmpty();
    }

    public void addToRole(Role role) {
        this.roles.add(role);
        role.getPermissions().add(this);
    }

    public void removeFromRole(Role role) {
        this.roles.remove(role);
        role.getPermissions().remove(this);
    }

    // Static factory methods for common permissions
    public static Permission createCustomerRead() {
        return new Permission("CUSTOMER_READ", "View Customers", "Харилцагч харах", 
                            "customer", Action.READ, Category.CUSTOMER_MANAGEMENT);
    }

    public static Permission createCustomerCreate() {
        return new Permission("CUSTOMER_CREATE", "Create Customer", "Харилцагч үүсгэх", 
                            "customer", Action.CREATE, Category.CUSTOMER_MANAGEMENT);
    }

    public static Permission createLoanApprove() {
        return new Permission("LOAN_APPROVE", "Approve Loans", "Зээл зөвшөөрөх", 
                            "loan_application", Action.APPROVE, Category.LOAN_PROCESSING);
    }

    public static Permission createUserManage() {
        return new Permission("USER_MANAGE", "Manage Users", "Хэрэглэгч удирдах", 
                            "user", Action.UPDATE, Category.USER_MANAGEMENT);
    }

    public static Permission createSystemAdmin() {
        Permission permission = new Permission("SYSTEM_ADMIN", "System Administration", "Системийн удирдлага", 
                                             "system", Action.UPDATE, Category.SYSTEM_ADMINISTRATION);
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

        public Builder action(Action action) {
            permission.action = action;
            return this;
        }

        public Builder category(Category category) {
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
    
    public Action getAction() { return action; }
    public void setAction(Action action) { this.action = action; }
    
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    
    public Boolean getIsSystemPermission() { return isSystemPermission; }
    public void setIsSystemPermission(Boolean isSystemPermission) { this.isSystemPermission = isSystemPermission; }
    
    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }
    
    public Set<Role> getRoles() { return roles; }
    public void setRoles(Set<Role> roles) { this.roles = roles; }

    // toString
    @Override
    public String toString() {
        return "Permission{" +
                "id=" + getId() +
                ", name='" + name + '\'' +
                ", resource='" + resource + '\'' +
                ", action=" + action +
                ", category=" + category +
                ", priority=" + priority +
                '}';
    }
}