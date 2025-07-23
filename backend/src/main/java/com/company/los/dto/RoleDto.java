package com.company.los.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.company.los.entity.Role;
import com.company.los.entity.Permission;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Дүрийн DTO
 * Role Data Transfer Object
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoleDto {

    private String id;

    @NotBlank(message = "Дүрийн нэр заавал байх ёстой")
    @Size(max = 100, message = "Дүрийн нэр 100 тэмдэгтээс ихгүй байх ёстой")
    private String name;

    @NotBlank(message = "Харуулах нэр заавал байх ёстой")
    @Size(max = 100, message = "Харуулах нэр 100 тэмдэгтээс ихгүй байх ёстой")
    private String displayName;

    @Size(max = 100, message = "Монгол харуулах нэр 100 тэмдэгтээс ихгүй байх ёстой")
    private String displayNameMn;

    @Size(max = 500, message = "Тайлбар 500 тэмдэгтээс ихгүй байх ёстой")
    private String description;

    private Boolean isSystemRole = false;
    private Boolean isDefault = false;

    @Min(value = 1, message = "Дүрийн түвшин 1-ээс бага байж болохгүй")
    private Integer levelOrder = 1;

    private Set<PermissionDto> permissions;
    private Set<String> permissionNames;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    private String createdBy;
    private String updatedBy;

    private Boolean isDeleted = false;
    private Boolean isActive = true;

    // Computed fields
    private String localizedDisplayName;
    private String roleTypeDisplay;
    private Integer permissionCount;
    private Integer userCount;
    private Boolean canBeDeleted;
    private Boolean isEditable;

    // Constructors
    public RoleDto() {
    }

    public RoleDto(String name, String displayName) {
        this.name = name;
        this.displayName = displayName;
    }

    public RoleDto(String name, String displayName, String description) {
        this(name, displayName);
        this.description = description;
    }

    // Static factory methods
    public static RoleDto fromEntity(Role role) {
        if (role == null) {
            return null;
        }

        RoleDto dto = new RoleDto();
        dto.setId(role.getId());
        dto.setName(role.getName());
        dto.setDisplayName(role.getDisplayName());
        dto.setDisplayNameMn(role.getDisplayNameMn());
        dto.setDescription(role.getDescription());
        dto.setIsSystemRole(role.getIsSystemRole());
        dto.setIsDefault(role.getIsDefault());
        dto.setLevelOrder(role.getLevelOrder());

        // Permissions
        if (role.getPermissions() != null) {
            dto.setPermissions(role.getPermissions().stream()
                    .map(PermissionDto::fromEntity)
                    .collect(Collectors.toSet()));
            
            dto.setPermissionNames(role.getPermissions().stream()
                    .map(Permission::getName)
                    .collect(Collectors.toSet()));
        }

        // Audit fields
        dto.setCreatedAt(role.getCreatedAt());
        dto.setUpdatedAt(role.getUpdatedAt());
        dto.setCreatedBy(role.getCreatedBy());
        dto.setUpdatedBy(role.getUpdatedBy());
        dto.setIsDeleted(role.getIsDeleted());
        dto.setIsActive(role.getIsActive());

        // Computed fields
        dto.setLocalizedDisplayName(role.getLocalizedDisplayName());
        dto.setRoleTypeDisplay(role.getRoleTypeDisplay());
        dto.setPermissionCount(role.getPermissionCount());
        dto.setUserCount(role.getUserCount());
        dto.setCanBeDeleted(role.canBeDeleted());
        dto.setIsEditable(role.isEditable());

        return dto;
    }

    public Role toEntity() {
        Role role = new Role();
        
        if (this.id != null) {
            role.setId(this.id);
        }
        
        role.setName(this.name);
        role.setDisplayName(this.displayName);
        role.setDisplayNameMn(this.displayNameMn);
        role.setDescription(this.description);
        
        if (this.isSystemRole != null) {
            role.setIsSystemRole(this.isSystemRole);
        }
        if (this.isDefault != null) {
            role.setIsDefault(this.isDefault);
        }
        if (this.levelOrder != null) {
            role.setLevelOrder(this.levelOrder);
        }

        // Permissions conversion
        if (this.permissions != null) {
            role.setPermissions(this.permissions.stream()
                    .map(PermissionDto::toEntity)
                    .collect(Collectors.toList()));
        }

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

    // Business methods
    public boolean hasPermission(String permissionName) {
        if (permissionNames != null) {
            return permissionNames.contains(permissionName);
        }
        if (permissions != null) {
            return permissions.stream()
                    .anyMatch(p -> permissionName.equals(p.getName()));
        }
        return false;
    }

    public boolean isSystemRole() {
        return Boolean.TRUE.equals(isSystemRole);
    }

    public boolean isDefaultRole() {
        return Boolean.TRUE.equals(isDefault);
    }

    public String getComputedDisplayName() {
        if (displayNameMn != null && !displayNameMn.trim().isEmpty()) {
            return displayNameMn;
        }
        return displayName != null ? displayName : name;
    }

    public String getPermissionNamesAsString() {
        if (permissions != null && !permissions.isEmpty()) {
            return permissions.stream()
                    .map(PermissionDto::getName)
                    .collect(Collectors.joining(", "));
        }
        if (permissionNames != null && !permissionNames.isEmpty()) {
            return String.join(", ", permissionNames);
        }
        return "Эрхгүй";
    }

    // Validation methods
    public boolean isValidForCreation() {
        return name != null && !name.trim().isEmpty() &&
               displayName != null && !displayName.trim().isEmpty();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getDisplayNameMn() { return displayNameMn; }
    public void setDisplayNameMn(String displayNameMn) { this.displayNameMn = displayNameMn; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Boolean getIsSystemRole() { return isSystemRole; }
    public void setIsSystemRole(Boolean isSystemRole) { this.isSystemRole = isSystemRole; }

    public Boolean getIsDefault() { return isDefault; }
    public void setIsDefault(Boolean isDefault) { this.isDefault = isDefault; }

    public Integer getLevelOrder() { return levelOrder; }
    public void setLevelOrder(Integer levelOrder) { this.levelOrder = levelOrder; }

    public Set<PermissionDto> getPermissions() { return permissions; }
    public void setPermissions(Set<PermissionDto> permissions) { this.permissions = permissions; }

    public Set<String> getPermissionNames() { return permissionNames; }
    public void setPermissionNames(Set<String> permissionNames) { this.permissionNames = permissionNames; }

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
    public String getLocalizedDisplayName() { return localizedDisplayName; }
    public void setLocalizedDisplayName(String localizedDisplayName) { this.localizedDisplayName = localizedDisplayName; }

    public String getRoleTypeDisplay() { return roleTypeDisplay; }
    public void setRoleTypeDisplay(String roleTypeDisplay) { this.roleTypeDisplay = roleTypeDisplay; }

    public Integer getPermissionCount() { return permissionCount; }
    public void setPermissionCount(Integer permissionCount) { this.permissionCount = permissionCount; }

    public Integer getUserCount() { return userCount; }
    public void setUserCount(Integer userCount) { this.userCount = userCount; }

    public Boolean getCanBeDeleted() { return canBeDeleted; }
    public void setCanBeDeleted(Boolean canBeDeleted) { this.canBeDeleted = canBeDeleted; }

    public Boolean getIsEditable() { return isEditable; }
    public void setIsEditable(Boolean isEditable) { this.isEditable = isEditable; }

    @Override
    public String toString() {
        return "RoleDto{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", displayName='" + displayName + '\'' +
                ", description='" + description + '\'' +
                ", isSystemRole=" + isSystemRole +
                ", isDefault=" + isDefault +
                ", levelOrder=" + levelOrder +
                ", permissionCount=" + permissionCount +
                ", userCount=" + userCount +
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