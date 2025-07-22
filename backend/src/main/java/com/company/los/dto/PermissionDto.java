package com.company.los.dto;

import com.company.los.entity.Permission;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Permission DTO for data transfer
 */
public class PermissionDto {

    private UUID id;

    @NotBlank(message = "Эрхийн нэр заавал бөглөх ёстой")
    @Size(min = 3, max = 100, message = "Эрхийн нэр 3-100 тэмдэгт байх ёстой")
    private String name;

    @NotBlank(message = "Харагдах нэр заавал бөглөх ёстой")
    @Size(max = 100, message = "Харагдах нэр 100 тэмдэгтээс ихгүй байх ёстой")
    private String displayName;

    @Size(max = 100, message = "Монгол нэр 100 тэмдэгтээс ихгүй байх ёстой")
    private String displayNameMn;

    @Size(max = 500, message = "Тайлбар 500 тэмдэгтээс ихгүй байх ёстой")
    private String description;

    @NotBlank(message = "Ресурс заавал тодорхойлох ёстой")
    @Size(max = 50, message = "Ресурс 50 тэмдэгтээс ихгүй байх ёстой")
    private String resource;

    @NotNull(message = "Үйлдэл заавал сонгох ёстой")
    private Permission.Action action;

    @NotNull(message = "Категори заавал сонгох ёстой")
    private Permission.Category category;

    private Boolean isSystemPermission = false;

    private Integer priority = 5;

    private String scope;

    private Set<String> roleNames;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String createdBy;

    private String updatedBy;

    // Constructors
    public PermissionDto() {}

    public PermissionDto(String name, String displayName, String resource, 
                        Permission.Action action, Permission.Category category) {
        this.name = name;
        this.displayName = displayName;
        this.resource = resource;
        this.action = action;
        this.category = category;
    }

    // Static factory method to convert from Entity
    public static PermissionDto fromEntity(Permission permission) {
        if (permission == null) {
            return null;
        }

        PermissionDto dto = new PermissionDto();
        dto.setId(permission.getId());
        dto.setName(permission.getName());
        dto.setDisplayName(permission.getDisplayName());
        dto.setDisplayNameMn(permission.getDisplayNameMn());
        dto.setDescription(permission.getDescription());
        dto.setResource(permission.getResource());
        dto.setAction(permission.getAction());
        dto.setCategory(permission.getCategory());
        dto.setIsSystemPermission(permission.getIsSystemPermission());
        dto.setPriority(permission.getPriority());
        dto.setScope(permission.getScope());
        dto.setCreatedAt(permission.getCreatedAt());
        dto.setUpdatedAt(permission.getUpdatedAt());
        dto.setCreatedBy(permission.getCreatedBy());
        dto.setUpdatedBy(permission.getUpdatedBy());

        // Convert role names
        if (permission.getRoles() != null) {
            dto.setRoleNames(permission.getRoles().stream()
                    .map(role -> role.getName())
                    .collect(Collectors.toSet()));
        }

        return dto;
    }

    // Convert to Entity
    public Permission toEntity() {
        Permission permission = new Permission();
        permission.setId(this.id);
        permission.setName(this.name);
        permission.setDisplayName(this.displayName);
        permission.setDisplayNameMn(this.displayNameMn);
        permission.setDescription(this.description);
        permission.setResource(this.resource);
        permission.setAction(this.action);
        permission.setCategory(this.category);
        permission.setIsSystemPermission(this.isSystemPermission);
        permission.setPriority(this.priority);
        permission.setScope(this.scope);
        return permission;
    }

    // Business methods
    public String getFullName() {
        return resource != null && action != null ? 
               resource.toUpperCase() + "_" + action.name() : "";
    }

    public String getLocalizedDisplayName() {
        return displayNameMn != null && !displayNameMn.trim().isEmpty() ? 
               displayNameMn : displayName;
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

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayNameMn() {
        return displayNameMn;
    }

    public void setDisplayNameMn(String displayNameMn) {
        this.displayNameMn = displayNameMn;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public Permission.Action getAction() {
        return action;
    }

    public void setAction(Permission.Action action) {
        this.action = action;
    }

    public Permission.Category getCategory() {
        return category;
    }

    public void setCategory(Permission.Category category) {
        this.category = category;
    }

    public Boolean getIsSystemPermission() {
        return isSystemPermission;
    }

    public void setIsSystemPermission(Boolean isSystemPermission) {
        this.isSystemPermission = isSystemPermission;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public Set<String> getRoleNames() {
        return roleNames;
    }

    public void setRoleNames(Set<String> roleNames) {
        this.roleNames = roleNames;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    @Override
    public String toString() {
        return "PermissionDto{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", displayName='" + displayName + '\'' +
                ", resource='" + resource + '\'' +
                ", action=" + action +
                ", category=" + category +
                ", priority=" + priority +
                ", scope='" + scope + '\'' +
                '}';
    }
}