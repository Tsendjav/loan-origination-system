package com.company.los.entity;

import com.company.los.common.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.util.HashSet;
import java.util.Set;

/**
 * Дүрийн Entity (Role-Based Access Control)
 * Role Entity for RBAC
 */
@Entity
@Table(name = "roles", indexes = {
        @Index(name = "idx_role_name", columnList = "name", unique = true),
        @Index(name = "idx_role_type", columnList = "role_type"),
        @Index(name = "idx_role_level", columnList = "level")
})
@SQLDelete(sql = "UPDATE roles SET is_deleted = true WHERE id = ?")
@Where(clause = "is_deleted = false")
public class Role extends BaseEntity {

    @Column(name = "name", unique = true, nullable = false, length = 50)
    @NotBlank(message = "Дүрийн нэр заавал бөглөх ёстой")
    @Size(min = 3, max = 50, message = "Дүрийн нэр 3-50 тэмдэгт байх ёстой")
    @Pattern(regexp = "^ROLE_[A-Z_]+$", message = "Дүрийн нэр ROLE_ үгээр эхлэн том үсгээр байх ёстой")
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

    @Enumerated(EnumType.STRING)
    @Column(name = "role_type", nullable = false, length = 30)
    @NotNull(message = "Дүрийн төрөл заавал сонгох ёстой")
    private RoleType roleType;

    @Column(name = "level", nullable = false)
    @NotNull(message = "Дүрийн түвшин заавал байх ёстой")
    @Min(value = 1, message = "Дүрийн түвшин 1-ээс бага байж болохгүй")
    @Max(value = 10, message = "Дүрийн түвшин 10-аас их байж болохгүй")
    private Integer level;

    @Column(name = "is_system_role", nullable = false)
    private Boolean isSystemRole = false;

    @Column(name = "can_be_assigned", nullable = false)
    private Boolean canBeAssigned = true;

    @Column(name = "max_assignments")
    @Min(value = -1, message = "Хамгийн их хувьлалт -1 (хязгааргүй) эсвэл эерэг тоо байх ёстой")
    private Integer maxAssignments = -1; // -1 = unlimited

    @Column(name = "priority", nullable = false)
    @Min(value = 1, message = "Тэргүүлэх эрэмбэ 1-ээс бага байж болохгүй")
    private Integer priority = 5;

    // Эрхүүд
    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id"),
            indexes = {
                    @Index(name = "idx_role_permissions_role", columnList = "role_id"),
                    @Index(name = "idx_role_permissions_permission", columnList = "permission_id")
            }
    )
    private Set<Permission> permissions = new HashSet<>();

    // Хэрэглэгчид
    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    private Set<User> users = new HashSet<>();

    // Дүрийн төрөл
    public enum RoleType {
        SYSTEM_ADMIN("Системийн админ"),
        BUSINESS_ADMIN("Бизнесийн админ"),
        MANAGER("Менежер"),
        LOAN_OFFICER("Зээлийн мэргэжилтэн"),
        UNDERWRITER("Эрсдэлийн үнэлэгч"),
        CUSTOMER_SERVICE("Үйлчлүүлэгчийн үйлчилгээ"),
        AUDITOR("Аудитор"),
        COMPLIANCE_OFFICER("Хяналтын мэргэжилтэн"),
        BRANCH_MANAGER("Салбарын менежер"),
        CREDIT_ANALYST("Зээлийн шинжээч");

        private final String mongolianName;

        RoleType(String mongolianName) {
            this.mongolianName = mongolianName;
        }

        public String getMongolianName() {
            return mongolianName;
        }
    }

    // Constructors
    public Role() {
        super();
    }

    public Role(String name, String displayName, RoleType roleType, Integer level) {
        this();
        this.name = name;
        this.displayName = displayName;
        this.roleType = roleType;
        this.level = level;
    }

    public Role(String name, String displayName, String displayNameMn, RoleType roleType, Integer level) {
        this(name, displayName, roleType, level);
        this.displayNameMn = displayNameMn;
    }

    // Business methods
    public void addPermission(Permission permission) {
        this.permissions.add(permission);
        permission.getRoles().add(this);
    }

    public void removePermission(Permission permission) {
        this.permissions.remove(permission);
        permission.getRoles().remove(this);
    }

    public boolean hasPermission(String permissionName) {
        return permissions.stream()
                .anyMatch(permission -> permission.getName().equals(permissionName));
    }

    public boolean canAssignTo(User user) {
        if (!canBeAssigned) {
            return false;
        }
        
        if (maxAssignments == -1) {
            return true; // Хязгааргүй
        }
        
        return users.size() < maxAssignments;
    }

    public void assignToUser(User user) {
        if (!canAssignTo(user)) {
            throw new IllegalStateException("Энэ дүрийг " + user.getUsername() + "-д өгөх боломжгүй");
        }
        
        users.add(user);
        user.getRoles().add(this);
    }

    public void removeFromUser(User user) {
        if (isSystemRole && users.size() == 1) {
            throw new IllegalStateException("Системийн дүрийг сүүлийн хэрэглэгчээс хасах боломжгүй");
        }
        
        users.remove(user);
        user.getRoles().remove(this);
    }

    public boolean isHigherLevelThan(Role other) {
        return this.level > other.level;
    }

    public boolean isLowerLevelThan(Role other) {
        return this.level < other.level;
    }

    public boolean isSameLevelAs(Role other) {
        return this.level.equals(other.level);
    }

    public String getLocalizedDisplayName() {
        return displayNameMn != null && !displayNameMn.trim().isEmpty() ? displayNameMn : displayName;
    }

    public int getActiveUserCount() {
        return (int) users.stream()
                .filter(User::isEnabled)
                .filter(user -> user.getStatus() == User.UserStatus.ACTIVE)
                .count();
    }

    public boolean isAdminRole() {
        return roleType == RoleType.SYSTEM_ADMIN || roleType == RoleType.BUSINESS_ADMIN;
    }

    public boolean isManagerRole() {
        return roleType == RoleType.MANAGER || roleType == RoleType.BRANCH_MANAGER;
    }

    public boolean isLoanRole() {
        return roleType == RoleType.LOAN_OFFICER || 
               roleType == RoleType.UNDERWRITER || 
               roleType == RoleType.CREDIT_ANALYST;
    }

    public void makeSystemRole() {
        this.isSystemRole = true;
        this.canBeAssigned = false; // Системийн дүрийг автоматаар өгөх боломжгүй
    }

    public void enableAssignment() {
        if (!isSystemRole) {
            this.canBeAssigned = true;
        }
    }

    public void disableAssignment() {
        this.canBeAssigned = false;
    }

    // Static factory methods for common roles
    public static Role createSystemAdmin() {
        Role role = new Role("ROLE_SYSTEM_ADMIN", "System Administrator", "Системийн админ", 
                           RoleType.SYSTEM_ADMIN, 10);
        role.makeSystemRole();
        role.setDescription("Full system access with all permissions");
        return role;
    }

    public static Role createLoanOfficer() {
        return new Role("ROLE_LOAN_OFFICER", "Loan Officer", "Зээлийн мэргэжилтэн", 
                       RoleType.LOAN_OFFICER, 3);
    }

    public static Role createManager() {
        return new Role("ROLE_MANAGER", "Manager", "Менежер", 
                       RoleType.MANAGER, 5);
    }

    public static Role createCustomerService() {
        return new Role("ROLE_CUSTOMER_SERVICE", "Customer Service", "Үйлчлүүлэгчийн үйлчилгээ", 
                       RoleType.CUSTOMER_SERVICE, 2);
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
    
    public RoleType getRoleType() { return roleType; }
    public void setRoleType(RoleType roleType) { this.roleType = roleType; }
    
    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }
    
    public Boolean getIsSystemRole() { return isSystemRole; }
    public void setIsSystemRole(Boolean isSystemRole) { this.isSystemRole = isSystemRole; }
    
    public Set<Permission> getPermissions() { return permissions; }
    public void setPermissions(Set<Permission> permissions) { this.permissions = permissions; }
    
    public Set<User> getUsers() { return users; }
    public void setUsers(Set<User> users) { this.users = users; }

    // toString
    @Override
    public String toString() {
        return "Role{" +
                "id=" + getId() +
                ", name='" + name + '\'' +
                ", displayName='" + displayName + '\'' +
                ", roleType=" + roleType +
                ", level=" + level +
                ", userCount=" + users.size() +
                '}';
    }
}