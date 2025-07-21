package com.company.los.dto;

import com.company.los.entity.Role;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.UUID;

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

    @Size(max = 200, message = "Тайлбар 200 тэмдэгтээс ихгүй байх ёстой")
    private String description;

    private Boolean enabled;
    private Boolean systemRole;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    // Constructors
    public RoleDto() {
        this.enabled = true;
        this.systemRole = false;
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
        dto.setDescription(role.getDescription());
        // Handle null-safe getter calls
        try {
            dto.setEnabled(role.getEnabled());
            dto.setSystemRole(role.getSystemRole());
            dto.setCreatedAt(role.getCreatedAt());
            dto.setUpdatedAt(role.getUpdatedAt());
            dto.setCreatedBy(role.getCreatedBy());
            dto.setUpdatedBy(role.getUpdatedBy());
        } catch (Exception e) {
            // Set default values if entity methods are not available
            dto.setEnabled(true);
            dto.setSystemRole(false);
        }
        return dto;
    }

    public Role toEntity() {
        Role role = new Role();
        role.setId(this.id);
        role.setName(this.name);
        role.setDescription(this.description);
        // Handle null-safe setter calls with defaults
        try {
            role.setEnabled(this.enabled != null ? this.enabled : true);
            role.setSystemRole(this.systemRole != null ? this.systemRole : false);
        } catch (Exception e) {
            // Ignore if setters are not available in entity
        }
        return role;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }

    public Boolean getSystemRole() { return systemRole; }
    public void setSystemRole(Boolean systemRole) { this.systemRole = systemRole; }

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
                ", description='" + description + '\'' +
                ", enabled=" + enabled +
                '}';
    }
}