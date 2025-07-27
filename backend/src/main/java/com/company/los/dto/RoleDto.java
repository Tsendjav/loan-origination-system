package com.company.los.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.company.los.entity.Role;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Дүрийн DTO
 * Role Data Transfer Object
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoleDto {

    private UUID id;

    @NotBlank(message = "Дүрийн нэр заавал бөглөх ёстой")
    @Size(min = 2, max = 100, message = "Дүрийн нэр 2-100 тэмдэгт байх ёстой")
    private String name;

    @Size(max = 500, message = "Дүрийн тайлбар 500 тэмдэгтээс ихгүй байх ёстой")
    private String description;

    @Size(max = 50, message = "Дүрийн код 50 тэмдэгтээс ихгүй байх ёстой")
    private String code;

    @NotNull(message = "Дүрийн статус заавал байх ёстой")
    private Role.RoleStatus status;

    @NotNull(message = "Дүрийн төрөл заавал байх ёстой")
    private Role.RoleType type;

    // Display name for UI
    @Size(max = 150, message = "Харагдах нэр 150 тэмдэгтээс ихгүй байх ёстой")
    private String displayName;

    // Default role flag
    private Boolean isDefault;

    // Permission IDs associated with this role
    private Set<UUID> permissionIds;

    // Permission names for display
    private Set<String> permissionNames;

    // User count
    private Integer userCount;

    // Hierarchy fields
    private UUID parentRoleId;
    private String parentRoleName;
    private List<RoleDto> childRoles;

    // Priority for role hierarchy (1-100, higher = more important)
    @Min(value = 1, message = "Эрэмбэ 1-ээс бага байж болохгүй")
    @Max(value = 100, message = "Эрэмбэ 100-аас их байж болохгүй")
    private Integer priority;

    // Audit fields
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    private String createdBy;
    private String updatedBy;

    private Boolean isDeleted;
    private Boolean isActive;

    // Computed fields
    private Boolean hasPermissions;
    private Boolean hasUsers;
    private Boolean isSystemRole;

    // Constructors
    public RoleDto() {
    }

    public RoleDto(String name, String description) {
        this.name = name;
        this.description = description;
        this.displayName = name;
        this.status = Role.RoleStatus.ACTIVE;
        this.type = Role.RoleType.BUSINESS;
        this.priority = 50;
        this.isDeleted = false;
        this.isActive = true;
        this.isDefault = false;
    }

    // Static factory methods
    public static RoleDto fromEntity(Role role) {
        if (role == null) {
            return null;
        }

        RoleDto dto = new RoleDto();
        dto.setId(role.getId());
        dto.setName(role.getName());
        dto.setDescription(role.getDescription());
        dto.setCode(role.getCode());
        dto.setStatus(role.getStatus());
        dto.setType(role.getType());
        dto.setPriority(role.getPriority());
        dto.setDisplayName(role.getDisplayName());
        dto.setIsDefault(role.getIsDefault());

        // Permission handling
        if (role.getPermissions() != null) {
            dto.setPermissionIds(role.getPermissions().stream()
                    .map(permission -> permission.getId())
                    .collect(Collectors.toSet()));
            dto.setPermissionNames(role.getPermissions().stream()
                    .map(permission -> permission.getName())
                    .collect(Collectors.toSet()));
        }

        // User count
        if (role.getUsers() != null) {
            dto.setUserCount(role.getUsers().size());
        }

        // Parent role
        if (role.getParentRole() != null) {
            dto.setParentRoleId(role.getParentRole().getId());
            dto.setParentRoleName(role.getParentRole().getName());
        }

        // Child roles
        if (role.getChildRoles() != null) {
            dto.setChildRoles(role.getChildRoles().stream()
                    .map(RoleDto::fromEntity)
                    .collect(Collectors.toList()));
        }

        // Audit fields
        dto.setCreatedAt(role.getCreatedAt());
        dto.setUpdatedAt(role.getUpdatedAt());
        dto.setCreatedBy(role.getCreatedBy());
        dto.setUpdatedBy(role.getUpdatedBy());
        dto.setIsDeleted(role.getIsDeleted());
        dto.setIsActive(role.getIsActive());

        // Set computed fields
        dto.setHasPermissions(role.getPermissions() != null && !role.getPermissions().isEmpty());
        dto.setHasUsers(role.getUsers() != null && !role.getUsers().isEmpty());
        dto.setIsSystemRole(role.isSystemRole());

        return dto;
    }

    public Role toEntity() {
        Role role = new Role();
        
        if (this.id != null) {
            role.setId(this.id);
        }
        
        role.setName(this.name);
        role.setDescription(this.description);
        role.setCode(this.code);
        role.setStatus(this.status != null ? this.status : Role.RoleStatus.ACTIVE);
        role.setType(this.type != null ? this.type : Role.RoleType.BUSINESS);
        role.setPriority(this.priority != null ? this.priority : 50);
        role.setDisplayName(this.displayName);
        role.setIsDefault(this.isDefault != null ? this.isDefault : false);

        // Audit fields
        role.setCreatedAt(this.createdAt);
        role.setUpdatedAt(this.updatedAt);
        role.setCreatedBy(this.createdBy);
        role.setUpdatedBy(this.updatedBy);

        if (this.isDeleted != null) {
            role.setIsDeleted(this.isDeleted);
        }
        if (this.isActive != null) {
            role.setIsActive(this.isActive);
        }

        return role;
    }

    // Business logic methods
    public boolean isValidRole() {
        return name != null && !name.trim().isEmpty() &&
               status != null &&
               type != null;
    }

    public boolean isSystemRole() {
        return Role.RoleType.SYSTEM.equals(type);
    }

    public boolean isBusinessRole() {
        return Role.RoleType.BUSINESS.equals(type);
    }

    public boolean isActiveRole() {
        return Role.RoleStatus.ACTIVE.equals(status) && Boolean.TRUE.equals(isActive);
    }

    public boolean isDefaultRole() {
        return Boolean.TRUE.equals(isDefault);
    }

    public String getStatusDisplay() {
        if (status == null) {
            return "Тодорхойгүй";
        }
        return status.getMongolianName();
    }

    public String getTypeDisplay() {
        if (type == null) {
            return "Тодорхойгүй";
        }
        return type.getMongolianName();
    }

    public String getPermissionNamesAsString() {
        if (permissionNames == null || permissionNames.isEmpty()) {
            return "Эрхгүй";
        }
        return String.join(", ", permissionNames);
    }

    public int getPermissionCount() {
        return permissionIds != null ? permissionIds.size() : 0;
    }

    public int getUserCountSafe() {
        return userCount != null ? userCount : 0;
    }

    public String getHierarchyLevel() {
        if (parentRoleId == null) {
            return "Эх дүр";
        } else if (childRoles != null && !childRoles.isEmpty()) {
            return "Завсрын дүр";
        } else {
            return "Дэд дүр";
        }
    }

    public boolean hasChildRoles() {
        return childRoles != null && !childRoles.isEmpty();
    }

    public boolean hasParentRole() {
        return parentRoleId != null;
    }

    public String getPriorityDisplay() {
        if (priority == null) {
            return "Тодорхойгүй";
        }
        if (priority >= 80) {
            return "Өндөр (" + priority + ")";
        } else if (priority >= 50) {
            return "Дунд (" + priority + ")";
        } else {
            return "Доод (" + priority + ")";
        }
    }

    public String getRiskLevel() {
        if (isSystemRole()) {
            return "Өндөр эрсдэл";
        }
        if (getPermissionCount() > 10) {
            return "Дунд эрсдэл";
        }
        return "Бага эрсдэл";
    }

    // Validation methods
    public String getValidationSummary() {
        StringBuilder sb = new StringBuilder();
        
        if (name == null || name.trim().isEmpty()) {
            sb.append("• Дүрийн нэр хоосон\n");
        }
        
        if (status == null) {
            sb.append("• Статус сонгоогүй\n");
        }
        
        if (type == null) {
            sb.append("• Төрөл сонгоогүй\n");
        }
        
        if (priority != null && (priority < 1 || priority > 100)) {
            sb.append("• Эрэмбэ буруу (1-100)\n");
        }
        
        return sb.toString();
    }

    public boolean hasValidationErrors() {
        return !isValidRole();
    }

    // Security assessment
    public String getSecurityRiskAssessment() {
        StringBuilder risks = new StringBuilder();
        
        if (isSystemRole()) {
            risks.append("• Системийн дүр\n");
        }
        
        if (getPermissionCount() > 10) {
            risks.append("• Олон эрхтэй дүр\n");
        }
        
        if (getUserCountSafe() > 50) {
            risks.append("• Олон хэрэглэгчтэй дүр\n");
        }
        
        if (hasChildRoles()) {
            risks.append("• Дэд дүртэй\n");
        }
        
        if (priority != null && priority >= 80) {
            risks.append("• Өндөр эрэмбэтэй дүр\n");
        }
        
        return risks.toString();
    }

    // Display helpers
    public String getEffectiveDisplayName() {
        if (displayName != null && !displayName.trim().isEmpty()) {
            return displayName;
        }
        return name;
    }

    public String getFullDescription() {
        StringBuilder desc = new StringBuilder();
        desc.append(getEffectiveDisplayName());
        
        if (description != null && !description.trim().isEmpty()) {
            desc.append(" - ").append(description);
        }
        
        desc.append(" (").append(getTypeDisplay()).append(")");
        
        return desc.toString();
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public Role.RoleStatus getStatus() { return status; }
    public void setStatus(Role.RoleStatus status) { this.status = status; }

    public Role.RoleType getType() { return type; }
    public void setType(Role.RoleType type) { this.type = type; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public Boolean getIsDefault() { return isDefault; }
    public void setIsDefault(Boolean isDefault) { this.isDefault = isDefault; }

    public Set<UUID> getPermissionIds() { return permissionIds; }
    public void setPermissionIds(Set<UUID> permissionIds) { this.permissionIds = permissionIds; }

    public Set<String> getPermissionNames() { return permissionNames; }
    public void setPermissionNames(Set<String> permissionNames) { this.permissionNames = permissionNames; }

    public Integer getUserCount() { return userCount; }
    public void setUserCount(Integer userCount) { this.userCount = userCount; }

    public UUID getParentRoleId() { return parentRoleId; }
    public void setParentRoleId(UUID parentRoleId) { this.parentRoleId = parentRoleId; }

    public String getParentRoleName() { return parentRoleName; }
    public void setParentRoleName(String parentRoleName) { this.parentRoleName = parentRoleName; }

    public List<RoleDto> getChildRoles() { return childRoles; }
    public void setChildRoles(List<RoleDto> childRoles) { this.childRoles = childRoles; }

    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }

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
    public Boolean getHasPermissions() { return hasPermissions; }
    public void setHasPermissions(Boolean hasPermissions) { this.hasPermissions = hasPermissions; }

    public Boolean getHasUsers() { return hasUsers; }
    public void setHasUsers(Boolean hasUsers) { this.hasUsers = hasUsers; }

    public Boolean getIsSystemRole() { return isSystemRole; }
    public void setIsSystemRole(Boolean isSystemRole) { this.isSystemRole = isSystemRole; }

    @Override
    public String toString() {
        return "RoleDto{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", code='" + code + '\'' +
                ", status=" + status +
                ", type=" + type +
                ", priority=" + priority +
                ", displayName='" + displayName + '\'' +
                ", isDefault=" + isDefault +
                ", userCount=" + userCount +
                ", permissionCount=" + getPermissionCount() +
                ", isActive=" + isActive +
                ", isDeleted=" + isDeleted +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RoleDto)) return false;
        RoleDto roleDto = (RoleDto) o;
        return id != null && id.equals(roleDto.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}