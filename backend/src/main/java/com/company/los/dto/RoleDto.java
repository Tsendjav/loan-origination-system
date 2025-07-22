package com.company.los.dto;

import com.company.los.entity.Role;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Дүрийн DTO
 * Role Data Transfer Object
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoleDto {

    private UUID id;

    @NotBlank(message = "Дүрийн нэр заавал бөглөх ёстой")
    @Size(max = 50, message = "Дүрийн нэр 50 тэмдэгтээс ихгүй байх ёстой")
    private String name;

    private String displayName;
    private String displayNameMn;

    @Size(max = 500, message = "Тайлбар 500 тэмдэгтээс ихгүй байх ёстой")
    private String description;

    // Entity-тэй тааруулж entity-н талбаруудыг ашиглана
    private Role.RoleType roleType;
    private Integer level;
    private Boolean isSystemRole; // Entity дотор isSystemRole гэж байна
    private Boolean canBeAssigned;
    private Integer maxAssignments;
    private Integer priority;

    private Set<PermissionDto> permissions;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    // Constructors
    public RoleDto() {
        this.isSystemRole = false;
        this.canBeAssigned = true;
        this.maxAssignments = -1;
        this.priority = 5;
    }

    public RoleDto(String name, String description) {
        this();
        this.name = name;
        this.description = description;
    }

    // Static factory method
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
        dto.setRoleType(role.getRoleType());
        dto.setLevel(role.getLevel());
        dto.setIsSystemRole(role.getIsSystemRole()); // Entity-тэй тааруулсан
        dto.setCanBeAssigned(role.getCanBeAssigned());
        dto.setMaxAssignments(role.getMaxAssignments());
        dto.setPriority(role.getPriority());
        dto.setCreatedAt(role.getCreatedAt());
        dto.setUpdatedAt(role.getUpdatedAt());
        dto.setCreatedBy(role.getCreatedBy());
        dto.setUpdatedBy(role.getUpdatedBy());
        
        if (role.getPermissions() != null) {
            dto.setPermissions(role.getPermissions().stream()
                    .map(PermissionDto::fromEntity)
                    .collect(Collectors.toSet()));
        }
        return dto;
    }

    public Role toEntity() {
        Role role = new Role();
        role.setId(this.id);
        role.setName(this.name);
        role.setDisplayName(this.displayName);
        role.setDisplayNameMn(this.displayNameMn);
        role.setDescription(this.description);
        role.setRoleType(this.roleType);
        role.setLevel(this.level);
        role.setIsSystemRole(this.isSystemRole);
        role.setCanBeAssigned(this.canBeAssigned);
        role.setMaxAssignments(this.maxAssignments);
        role.setPriority(this.priority);
        role.setCreatedAt(this.createdAt);
        role.setUpdatedAt(this.updatedAt);
        role.setCreatedBy(this.createdBy);
        role.setUpdatedBy(this.updatedBy);
        
        if (this.permissions != null) {
            role.setPermissions(this.permissions.stream()
                    .map(PermissionDto::toEntity)
                    .collect(Collectors.toSet()));
        }
        return role;
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

    public Role.RoleType getRoleType() { return roleType; }
    public void setRoleType(Role.RoleType roleType) { this.roleType = roleType; }

    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }

    public Boolean getIsSystemRole() { return isSystemRole; }
    public void setIsSystemRole(Boolean isSystemRole) { this.isSystemRole = isSystemRole; }

    public Boolean getCanBeAssigned() { return canBeAssigned; }
    public void setCanBeAssigned(Boolean canBeAssigned) { this.canBeAssigned = canBeAssigned; }

    public Integer getMaxAssignments() { return maxAssignments; }
    public void setMaxAssignments(Integer maxAssignments) { this.maxAssignments = maxAssignments; }

    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }

    public Set<PermissionDto> getPermissions() { return permissions; }
    public void setPermissions(Set<PermissionDto> permissions) { this.permissions = permissions; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    @Override
    public String toString() {
        return "RoleDto{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", displayName='" + displayName + '\'' +
                ", roleType=" + roleType +
                ", level=" + level +
                ", isSystemRole=" + isSystemRole +
                ", permissions=" + (permissions != null ? permissions.stream().map(PermissionDto::getName).collect(Collectors.joining(", ")) : "[]") +
                '}';
    }
}