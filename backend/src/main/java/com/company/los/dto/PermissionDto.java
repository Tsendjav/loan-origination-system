package com.company.los.dto;

import com.company.los.entity.Role; // Added import
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.company.los.entity.Permission;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Эрхийн DTO
 * Permission Data Transfer Object
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
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

    @NotBlank(message = "Үйлдэл заавал тодорхойлох ёстой")
    @Size(max = 20, message = "Үйлдэл 20 тэмдэгтээс ихгүй байх ёстой")
    private String action;

    @NotBlank(message = "Категори заавал тодорхойлох ёстой")
    @Size(max = 50, message = "Категори 50 тэмдэгтээс ихгүй байх ёстой")
    private String category;

    @Size(max = 20, message = "Хамрах хүрээ 20 тэмдэгтээс ихгүй байх ёстой")
    private String scope;

    private Boolean isSystemPermission = false;

    @Min(value = 1, message = "Тэргүүлэх эрэмбэ 1-ээс бага байж болохгүй")
    @Max(value = 10, message = "Тэргүүлэх эрэмбэ 10-аас их байж болохгүй")
    private Integer priority = 5;

    // Role information
    private Set<String> roleNames;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    private String createdBy;
    private String updatedBy;

    private Boolean isDeleted = false;
    private Boolean isActive = true;

    // Computed fields
    private String fullName;
    private String localizedDisplayName;
    private String actionDisplay;
    private Integer assignedRoleCount;
    private Integer assignedUserCount;
    private Boolean isHighPriority;
    private Boolean isMediumPriority;
    private Boolean isLowPriority;
    private Boolean canBeDeleted;

    // Constructors
    public PermissionDto() {
    }

    public PermissionDto(String name, String displayName, String resource, String action, String category) {
        this.name = name;
        this.displayName = displayName;
        this.resource = resource;
        this.action = action;
        this.category = category;
    }

    public PermissionDto(String name, String displayName, String displayNameMn, String resource, String action, String category) {
        this(name, displayName, resource, action, category);
        this.displayNameMn = displayNameMn;
    }

    // Static factory methods
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
        dto.setScope(permission.getScope());
        dto.setIsSystemPermission(permission.getIsSystemPermission());
        dto.setPriority(permission.getPriority());

        // Role information
        if (permission.getRoles() != null) {
            dto.setRoleNames(permission.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toSet()));
        }

        // Audit fields
        dto.setCreatedAt(permission.getCreatedAt());
        dto.setUpdatedAt(permission.getUpdatedAt());
        dto.setCreatedBy(permission.getCreatedBy());
        dto.setUpdatedBy(permission.getUpdatedBy());
        dto.setIsDeleted(permission.getIsDeleted());
        dto.setIsActive(permission.getIsActive());

        // Computed fields
        dto.setFullName(permission.getFullName());
        dto.setLocalizedDisplayName(permission.getLocalizedDisplayName());
        dto.setActionDisplay(permission.getActionDisplay());
        dto.setAssignedRoleCount(permission.getAssignedRoleCount());
        dto.setAssignedUserCount(permission.getAssignedUserCount());
        dto.setIsHighPriority(permission.isHighPriority());
        dto.setIsMediumPriority(permission.isMediumPriority());
        dto.setIsLowPriority(permission.isLowPriority());
        dto.setCanBeDeleted(permission.canBeDeleted());

        return dto;
    }

    public Permission toEntity() {
        Permission permission = new Permission();
        
        permission.setId(this.id != null ? this.id : UUID.randomUUID());
        permission.setName(this.name);
        permission.setDisplayName(this.displayName);
        permission.setDisplayNameMn(this.displayNameMn);
        permission.setDescription(this.description);
        permission.setResource(this.resource);
        permission.setAction(this.action);
        permission.setCategory(this.category);
        permission.setScope(this.scope);
        permission.setIsSystemPermission(this.isSystemPermission != null ? this.isSystemPermission : false);
        permission.setPriority(this.priority != null ? this.priority : 5);

        // Audit fields
        permission.setCreatedAt(this.createdAt != null ? this.createdAt : LocalDateTime.now());
        permission.setUpdatedAt(this.updatedAt != null ? this.updatedAt : LocalDateTime.now());
        permission.setCreatedBy(this.createdBy);
        permission.setUpdatedBy(this.updatedBy);
        permission.setIsDeleted(this.isDeleted != null ? this.isDeleted : false);
        permission.setIsActive(this.isActive != null ? this.isActive : true);

        return permission;
    }

    // Business methods
    public boolean isSystemPermission() {
        return Boolean.TRUE.equals(isSystemPermission);
    }

    public boolean isAssignedToRole(String roleName) {
        return roleNames != null && roleNames.contains(roleName);
    }

    public String getComputedDisplayName() {
        if (displayNameMn != null && !displayNameMn.trim().isEmpty()) {
            return displayNameMn;
        }
        return displayName != null ? displayName : name;
    }

    public String getRoleNamesAsString() {
        if (roleNames != null && !roleNames.isEmpty()) {
            return String.join(", ", roleNames);
        }
        return "Дүргүй";
    }

    public String getPriorityText() {
        if (priority == null) return "Дундаж";
        if (priority >= 8) return "Өндөр";
        if (priority >= 4) return "Дундаж";
        return "Бага";
    }

    public String getScopeText() {
        if (scope == null) return "";
        switch (scope.toUpperCase()) {
            case "OWN": return "Өөрийн";
            case "BRANCH": return "Салбарын";
            case "ALL": return "Бүгдийн";
            default: return scope;
        }
    }

    public String getCategoryText() {
        if (category == null) return "";
        switch (category.toUpperCase()) {
            case "CUSTOMER_MANAGEMENT": return "Харилцагч удирдлага";
            case "LOAN_MANAGEMENT": return "Зээлийн боловсруулалт";
            case "DOCUMENT_MANAGEMENT": return "Баримт удирдлага";
            case "USER_MANAGEMENT": return "Хэрэглэгч удирдлага";
            case "ROLE_MANAGEMENT": return "Дүр удирдлага";
            case "REPORT": return "Тайлан";
            case "SYSTEM_ADMINISTRATION": return "Системийн удирдлага";
            case "FINANCIAL_OPERATIONS": return "Санхүүгийн үйл ажиллагаа";
            case "COMPLIANCE": return "Дүрэм баримтлалт";
            case "AUDIT": return "Аудит";
            default: return category;
        }
    }

    // Validation methods
    public boolean isValidForCreation() {
        return name != null && !name.trim().isEmpty() &&
               displayName != null && !displayName.trim().isEmpty() &&
               resource != null && !resource.trim().isEmpty() &&
               action != null && !action.trim().isEmpty() &&
               category != null && !category.trim().isEmpty();
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

    public Set<String> getRoleNames() { return roleNames; }
    public void setRoleNames(Set<String> roleNames) { this.roleNames = roleNames; }

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

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getLocalizedDisplayName() { return localizedDisplayName; }
    public void setLocalizedDisplayName(String localizedDisplayName) { this.localizedDisplayName = localizedDisplayName; }

    public String getActionDisplay() { return actionDisplay; }
    public void setActionDisplay(String actionDisplay) { this.actionDisplay = actionDisplay; }

    public Integer getAssignedRoleCount() { return assignedRoleCount; }
    public void setAssignedRoleCount(Integer assignedRoleCount) { this.assignedRoleCount = assignedRoleCount; }

    public Integer getAssignedUserCount() { return assignedUserCount; }
    public void setAssignedUserCount(Integer assignedUserCount) { this.assignedUserCount = assignedUserCount; }

    public Boolean getIsHighPriority() { return isHighPriority; }
    public void setIsHighPriority(Boolean isHighPriority) { this.isHighPriority = isHighPriority; }

    public Boolean getIsMediumPriority() { return isMediumPriority; }
    public void setIsMediumPriority(Boolean isMediumPriority) { this.isMediumPriority = isMediumPriority; }

    public Boolean getIsLowPriority() { return isLowPriority; }
    public void setIsLowPriority(Boolean isLowPriority) { this.isLowPriority = isLowPriority; }

    public Boolean getCanBeDeleted() { return canBeDeleted; }
    public void setCanBeDeleted(Boolean canBeDeleted) { this.canBeDeleted = canBeDeleted; }

    @Override
    public String toString() {
        return "PermissionDto{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", displayName='" + displayName + '\'' +
                ", resource='" + resource + '\'' +
                ", action='" + action + '\'' +
                ", category='" + category + '\'' +
                ", priority=" + priority +
                ", scope='" + scope + '\'' +
                ", isSystemPermission=" + isSystemPermission +
                ", assignedRoleCount=" + assignedRoleCount +
                ", assignedUserCount=" + assignedUserCount +
                ", isActive=" + isActive +
                ", isDeleted=" + isDeleted +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PermissionDto)) return false;
        PermissionDto that = (PermissionDto) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}