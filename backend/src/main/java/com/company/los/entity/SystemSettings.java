package com.company.los.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Системийн тохиргооны Entity
 * System Settings Entity for application configuration
 */
@Entity
@Table(name = "system_settings", indexes = {
        @Index(name = "idx_system_setting_key", columnList = "setting_key", unique = true),
        @Index(name = "idx_system_setting_category", columnList = "category")
})
public class SystemSettings {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "setting_key", unique = true, nullable = false, length = 100)
    @NotBlank(message = "Тохиргооны түлхүүр заавал байх ёстой")
    @Size(max = 100, message = "Тохиргооны түлхүүр 100 тэмдэгтээс ихгүй байх ёстой")
    private String settingKey;

    @Column(name = "setting_value", columnDefinition = "TEXT")
    private String settingValue;

    @Column(name = "data_type", length = 20)
    @Size(max = 20, message = "Өгөгдлийн төрөл 20 тэмдэгтээс ихгүй байх ёстой")
    private String dataType = "STRING"; // 'STRING', 'INTEGER', 'DECIMAL', 'BOOLEAN', 'JSON'

    @Column(name = "category", length = 100)
    @Size(max = 100, message = "Категори 100 тэмдэгтээс ихгүй байх ёстой")
    private String category;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_encrypted", nullable = false)
    private Boolean isEncrypted = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 100)
    @Size(max = 100, message = "Шинэчлэгч 100 тэмдэгтээс ихгүй байх ёстой")
    private String updatedBy;

    // Data type constants
    public static final String TYPE_STRING = "STRING";
    public static final String TYPE_INTEGER = "INTEGER";
    public static final String TYPE_DECIMAL = "DECIMAL";
    public static final String TYPE_BOOLEAN = "BOOLEAN";
    public static final String TYPE_JSON = "JSON";

    // Category constants
    public static final String CATEGORY_GENERAL = "GENERAL";
    public static final String CATEGORY_SECURITY = "SECURITY";
    public static final String CATEGORY_LOAN = "LOAN";
    public static final String CATEGORY_NOTIFICATION = "NOTIFICATION";
    public static final String CATEGORY_INTEGRATION = "INTEGRATION";
    public static final String CATEGORY_REPORTING = "REPORTING";
    public static final String CATEGORY_AUDIT = "AUDIT";

    // Common setting keys
    public static final String KEY_MAX_LOGIN_ATTEMPTS = "MAX_LOGIN_ATTEMPTS";
    public static final String KEY_PASSWORD_EXPIRY_DAYS = "PASSWORD_EXPIRY_DAYS";
    public static final String KEY_SESSION_TIMEOUT_MINUTES = "SESSION_TIMEOUT_MINUTES";
    public static final String KEY_MAX_FILE_SIZE_MB = "MAX_FILE_SIZE_MB";
    public static final String KEY_ALLOWED_FILE_TYPES = "ALLOWED_FILE_TYPES";
    public static final String KEY_EMAIL_NOTIFICATIONS_ENABLED = "EMAIL_NOTIFICATIONS_ENABLED";
    public static final String KEY_SMS_NOTIFICATIONS_ENABLED = "SMS_NOTIFICATIONS_ENABLED";
    public static final String KEY_DEFAULT_CURRENCY = "DEFAULT_CURRENCY";
    public static final String KEY_MIN_LOAN_AMOUNT = "MIN_LOAN_AMOUNT";
    public static final String KEY_MAX_LOAN_AMOUNT = "MAX_LOAN_AMOUNT";
    public static final String KEY_DEFAULT_LOAN_TERM_MONTHS = "DEFAULT_LOAN_TERM_MONTHS";
    public static final String KEY_BASE_INTEREST_RATE = "BASE_INTEREST_RATE";
    public static final String KEY_RISK_ASSESSMENT_ENABLED = "RISK_ASSESSMENT_ENABLED";
    public static final String KEY_AUTO_APPROVAL_THRESHOLD = "AUTO_APPROVAL_THRESHOLD";
    public static final String KEY_BACKUP_RETENTION_DAYS = "BACKUP_RETENTION_DAYS";
    public static final String KEY_AUDIT_LOG_RETENTION_DAYS = "AUDIT_LOG_RETENTION_DAYS";

    // Constructors
    public SystemSettings() {
        this.id = java.util.UUID.randomUUID().toString();
    }

    public SystemSettings(String settingKey, String settingValue, String dataType, String category) {
        this();
        this.settingKey = settingKey;
        this.settingValue = settingValue;
        this.dataType = dataType;
        this.category = category;
    }

    // Business methods
    public String getValueAsString() {
        return settingValue;
    }

    public Integer getValueAsInteger() {
        if (settingValue == null || !TYPE_INTEGER.equals(dataType)) {
            return null;
        }
        try {
            return Integer.valueOf(settingValue);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public BigDecimal getValueAsDecimal() {
        if (settingValue == null || !TYPE_DECIMAL.equals(dataType)) {
            return null;
        }
        try {
            return new BigDecimal(settingValue);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public Boolean getValueAsBoolean() {
        if (settingValue == null || !TYPE_BOOLEAN.equals(dataType)) {
            return null;
        }
        return Boolean.valueOf(settingValue);
    }

    public void setValue(String value) {
        this.settingValue = value;
        this.dataType = TYPE_STRING;
    }

    public void setValue(Integer value) {
        this.settingValue = value != null ? value.toString() : null;
        this.dataType = TYPE_INTEGER;
    }

    public void setValue(BigDecimal value) {
        this.settingValue = value != null ? value.toString() : null;
        this.dataType = TYPE_DECIMAL;
    }

    public void setValue(Boolean value) {
        this.settingValue = value != null ? value.toString() : null;
        this.dataType = TYPE_BOOLEAN;
    }

    public void setJsonValue(String jsonValue) {
        this.settingValue = jsonValue;
        this.dataType = TYPE_JSON;
    }

    public boolean isStringType() {
        return TYPE_STRING.equals(dataType);
    }

    public boolean isIntegerType() {
        return TYPE_INTEGER.equals(dataType);
    }

    public boolean isDecimalType() {
        return TYPE_DECIMAL.equals(dataType);
    }

    public boolean isBooleanType() {
        return TYPE_BOOLEAN.equals(dataType);
    }

    public boolean isJsonType() {
        return TYPE_JSON.equals(dataType);
    }

    public boolean isSecuritySetting() {
        return CATEGORY_SECURITY.equals(category);
    }

    public boolean isLoanSetting() {
        return CATEGORY_LOAN.equals(category);
    }

    public String getCategoryText() {
        switch (category) {
            case CATEGORY_GENERAL: return "Ерөнхий";
            case CATEGORY_SECURITY: return "Аюулгүй байдал";
            case CATEGORY_LOAN: return "Зээл";
            case CATEGORY_NOTIFICATION: return "Мэдэгдэл";
            case CATEGORY_INTEGRATION: return "Интеграци";
            case CATEGORY_REPORTING: return "Тайлагналт";
            case CATEGORY_AUDIT: return "Аудит";
            default: return category;
        }
    }

    public String getDataTypeText() {
        switch (dataType) {
            case TYPE_STRING: return "Текст";
            case TYPE_INTEGER: return "Бүхэл тоо";
            case TYPE_DECIMAL: return "Аравтын бутархай";
            case TYPE_BOOLEAN: return "Логик утга";
            case TYPE_JSON: return "JSON";
            default: return dataType;
        }
    }

    public void encrypt() {
        this.isEncrypted = true;
        // TODO: Implement actual encryption logic
    }

    public void decrypt() {
        // TODO: Implement actual decryption logic
    }

    // Static factory methods for common settings
    public static SystemSettings createMaxLoginAttempts(int attempts) {
        return new SystemSettings(KEY_MAX_LOGIN_ATTEMPTS, String.valueOf(attempts), TYPE_INTEGER, CATEGORY_SECURITY);
    }

    public static SystemSettings createPasswordExpiryDays(int days) {
        return new SystemSettings(KEY_PASSWORD_EXPIRY_DAYS, String.valueOf(days), TYPE_INTEGER, CATEGORY_SECURITY);
    }

    public static SystemSettings createMinLoanAmount(BigDecimal amount) {
        return new SystemSettings(KEY_MIN_LOAN_AMOUNT, amount.toString(), TYPE_DECIMAL, CATEGORY_LOAN);
    }

    public static SystemSettings createMaxLoanAmount(BigDecimal amount) {
        return new SystemSettings(KEY_MAX_LOAN_AMOUNT, amount.toString(), TYPE_DECIMAL, CATEGORY_LOAN);
    }

    public static SystemSettings createEmailNotificationsEnabled(boolean enabled) {
        return new SystemSettings(KEY_EMAIL_NOTIFICATIONS_ENABLED, String.valueOf(enabled), TYPE_BOOLEAN, CATEGORY_NOTIFICATION);
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSettingKey() { return settingKey; }
    public void setSettingKey(String settingKey) { this.settingKey = settingKey; }

    public String getSettingValue() { return settingValue; }
    public void setSettingValue(String settingValue) { this.settingValue = settingValue; }

    public String getDataType() { return dataType; }
    public void setDataType(String dataType) { this.dataType = dataType; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Boolean getIsEncrypted() { return isEncrypted; }
    public void setIsEncrypted(Boolean isEncrypted) { this.isEncrypted = isEncrypted; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    // equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SystemSettings that = (SystemSettings) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // toString
    @Override
    public String toString() {
        return "SystemSettings{" +
                "id='" + id + '\'' +
                ", settingKey='" + settingKey + '\'' +
                ", settingValue='" + (isEncrypted ? "***" : settingValue) + '\'' +
                ", dataType='" + dataType + '\'' +
                ", category='" + category + '\'' +
                ", isEncrypted=" + isEncrypted +
                '}';
    }
}