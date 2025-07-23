package com.company.los.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Системийн тохиргооны entity
 * System Configuration Entity
 */
@Entity
@Table(name = "system_configs",
       indexes = {
           @Index(name = "idx_config_key", columnList = "config_key", unique = true),
           @Index(name = "idx_category", columnList = "category"),
           @Index(name = "idx_active", columnList = "is_active"),
           @Index(name = "idx_category_active", columnList = "category, is_active"),
           @Index(name = "idx_environment", columnList = "environment")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemConfig {

    /**
     * Үндсэн түлхүүр
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private String id;

    /**
     * Тохиргооны түлхүүр үг (давтагдахгүй)
     */
    @NotBlank(message = "Config key cannot be blank")
    @Size(max = 100, message = "Config key must be less than 100 characters")
    @Column(name = "config_key", nullable = false, unique = true, length = 100)
    private String configKey;

    /**
     * Тохиргооны утга
     */
    @NotBlank(message = "Config value cannot be blank")
    @Size(max = 1000, message = "Config value must be less than 1000 characters")
    @Column(name = "config_value", nullable = false, length = 1000)
    private String configValue;

    /**
     * Утгын төрөл (STRING, INTEGER, DECIMAL, BOOLEAN, JSON)
     */
    @NotBlank(message = "Value type cannot be blank")
    @Size(max = 20, message = "Value type must be less than 20 characters")
    @Column(name = "value_type", nullable = false, length = 20)
    private String valueType = "STRING";

    /**
     * Тохиргооны категори
     */
    @Size(max = 50, message = "Category must be less than 50 characters")
    @Column(name = "category", length = 50)
    private String category;

    /**
     * Тохиргооны тайлбар
     */
    @Size(max = 500, message = "Description must be less than 500 characters")
    @Column(name = "description", length = 500)
    private String description;

    /**
     * Идэвхтэй эсэх
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /**
     * Runtime-д өөрчлөх боломжтой эсэх
     */
    @Column(name = "is_runtime_editable", nullable = false)
    private Boolean isRuntimeEditable = true;

    /**
     * Environment (ALL, DEVELOPMENT, STAGING, PRODUCTION)
     */
    @Size(max = 20, message = "Environment must be less than 20 characters")
    @Column(name = "environment", length = 20)
    private String environment = "ALL";

    /**
     * Эрэмбийн дугаар
     */
    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    /**
     * Анхдагч утга
     */
    @Size(max = 1000, message = "Default value must be less than 1000 characters")
    @Column(name = "default_value", length = 1000)
    private String defaultValue;

    /**
     * Validation pattern (regex)
     */
    @Size(max = 200, message = "Validation pattern must be less than 200 characters")
    @Column(name = "validation_pattern", length = 200)
    private String validationPattern;

    /**
     * Үүсгэсэн огноо
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Үүсгэгч
     */
    @Size(max = 100, message = "Created by must be less than 100 characters")
    @Column(name = "created_by", length = 100, updatable = false)
    private String createdBy = "SYSTEM";

    /**
     * Өөрчилсөн огноо
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Өөрчлөгч
     */
    @Size(max = 100, message = "Updated by must be less than 100 characters")
    @Column(name = "updated_by", length = 100)
    private String updatedBy = "SYSTEM";

    /**
     * Version for optimistic locking
     */
    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    // Business methods

    /**
     * Boolean утга авах
     */
    public Boolean getBooleanValue() {
        if (configValue == null) return null;
        return switch (configValue.toLowerCase()) {
            case "true", "1", "yes", "on" -> true;
            case "false", "0", "no", "off" -> false;
            default -> null;
        };
    }

    /**
     * Integer утга авах
     */
    public Integer getIntegerValue() {
        try {
            return Integer.parseInt(configValue);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Long утга авах
     */
    public Long getLongValue() {
        try {
            return Long.parseLong(configValue);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Double утга авах
     */
    public Double getDoubleValue() {
        try {
            return Double.parseDouble(configValue);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * BigDecimal утга авах
     */
    public java.math.BigDecimal getDecimalValue() {
        try {
            return new java.math.BigDecimal(configValue);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * JSON объект авах
     */
    public String getJsonValue() {
        return "JSON".equals(valueType) ? configValue : null;
    }

    /**
     * Утга validation хийх
     */
    public boolean isValidValue() {
        if (configValue == null || configValue.trim().isEmpty()) {
            return false;
        }

        return switch (valueType) {
            case "INTEGER" -> getIntegerValue() != null;
            case "DECIMAL" -> getDecimalValue() != null;
            case "BOOLEAN" -> getBooleanValue() != null;
            case "JSON" -> isValidJson();
            default -> true; // STRING and others
        };
    }

    /**
     * JSON format шалгах
     */
    private boolean isValidJson() {
        if (configValue == null) return false;
        try {
            // Simple JSON validation - starts and ends with { } or [ ]
            String trimmed = configValue.trim();
            return (trimmed.startsWith("{") && trimmed.endsWith("}")) ||
                   (trimmed.startsWith("[") && trimmed.endsWith("]"));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Pattern validation
     */
    public boolean matchesValidationPattern() {
        if (validationPattern == null || validationPattern.trim().isEmpty()) {
            return true;
        }
        if (configValue == null) {
            return false;
        }
        try {
            return configValue.matches(validationPattern);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Анхдагч утга эсэхийг шалгах
     */
    public boolean isDefaultValue() {
        if (defaultValue == null) return false;
        return defaultValue.equals(configValue);
    }

    /**
     * Тохиргоо өөрчлөгдсөн эсэхийг шалгах
     */
    public boolean isModified() {
        return updatedAt != null && !updatedAt.equals(createdAt);
    }

    @Override
    public String toString() {
        return "SystemConfig{" +
                "configKey='" + configKey + '\'' +
                ", configValue='" + configValue + '\'' +
                ", valueType='" + valueType + '\'' +
                ", category='" + category + '\'' +
                ", isActive=" + isActive +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SystemConfig that)) return false;
        return configKey != null && configKey.equals(that.configKey);
    }

    @Override
    public int hashCode() {
        return configKey != null ? configKey.hashCode() : 0;
    }
}