package com.company.los.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * RoleDto - UserDto алдаа засах зориулалтаар
 * Файл байршил: backend/src/main/java/com/company/los/dto/RoleDto.java
 */
@Data
@NoArgsConstructor 
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoleDto {
    
    private Long id;
    
    private String name;
    
    private String description;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private Boolean isActive;
    
    // Constructors
    public RoleDto(String name, String description) {
        this.name = name;
        this.description = description;
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
    }
    
    // Utility методууд
    public boolean isAdmin() {
        return "ADMIN".equals(this.name);
    }
    
    public boolean isLoanOfficer() {
        return "LOAN_OFFICER".equals(this.name);
    }
    
    public boolean isManager() {
        return "MANAGER".equals(this.name);
    }
    
    public boolean isCustomerService() {
        return "CUSTOMER_SERVICE".equals(this.name);
    }
    
    public boolean isViewer() {
        return "VIEWER".equals(this.name);
    }
    
    @Override
    public String toString() {
        return "RoleDto{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}