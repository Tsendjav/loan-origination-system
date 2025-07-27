package com.company.los.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Суурь Entity класс - бүх entity-д нийтлэг талбарууд
 * Base Entity Class - common fields for all entities
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "VARCHAR(36)")
    private UUID id;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @CreatedBy
    @Column(name = "created_by", length = 100)
    private String createdBy;

    @LastModifiedBy
    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    // Constructors
    public BaseEntity() {
        LocalDateTime now = LocalDateTime.now();
        this.id = UUID.randomUUID(); // Автомат UUID үүсгэх
        this.createdAt = now;
        this.updatedAt = now;
    }

    // Lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Business methods
    /**
     * Soft delete - бодит устгалт биш
     */
    public void markAsDeleted() {
        this.isDeleted = true;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Restore - сэргээх
     */
    public void restore() {
        this.isDeleted = false;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Устгагдсан эсэхийг шалгах
     */
    public boolean isDeleted() {
        return this.isDeleted != null && this.isDeleted;
    }

    /**
     * Entity шинэ үүссэн эсэхийг шалгах
     */
    public boolean isNew() {
        return id == null;
    }

    /**
     * Audit мэдээлэл тохируулах
     */
    public void setAuditInfo(String username) {
        if (isNew()) {
            this.createdBy = username;
        }
        this.updatedBy = username;
    }

    /**
     * Идэвхтэй эсэхийг шалгах (устгагдаагүй)
     */
    public boolean isActiveEntity() {
        return !isDeleted();
    }

    // Getters and Setters
    public UUID getId() { 
        return id; 
    }
    
    public void setId(UUID id) { 
        this.id = id; 
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

    public Boolean getIsDeleted() { 
        return isDeleted; 
    }
    
    public void setIsDeleted(Boolean isDeleted) { 
        this.isDeleted = isDeleted; 
    }

    // equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseEntity that = (BaseEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    // toString
    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "id=" + id +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", isDeleted=" + isDeleted +
                '}';
    }
}