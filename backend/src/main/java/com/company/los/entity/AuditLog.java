package com.company.los.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Аудитын лог Entity
 * Audit Log Entity for tracking all database changes
 */
@Entity
@Table(name = "audit_logs", indexes = {
        @Index(name = "idx_audit_table_name", columnList = "table_name"),
        @Index(name = "idx_audit_record_id", columnList = "record_id"),
        @Index(name = "idx_audit_action", columnList = "action"),
        @Index(name = "idx_audit_changed_by", columnList = "changed_by"),
        @Index(name = "idx_audit_changed_at", columnList = "changed_at")
})
public class AuditLog {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "table_name", nullable = false, length = 100)
    @NotBlank(message = "Хүснэгтийн нэр заавал байх ёстой")
    @Size(max = 100, message = "Хүснэгтийн нэр 100 тэмдэгтээс ихгүй байх ёстой")
    private String tableName;

    @Column(name = "record_id", nullable = false, length = 36)
    @NotBlank(message = "Бичлэгийн ID заавал байх ёстой")
    @Size(max = 36, message = "Бичлэгийн ID 36 тэмдэгтээс ихгүй байх ёстой")
    private String recordId;

    @Column(name = "action", nullable = false, length = 20)
    @NotBlank(message = "Үйлдэл заавал байх ёстой")
    @Size(max = 20, message = "Үйлдэл 20 тэмдэгтээс ихгүй байх ёстой")
    private String action; // 'INSERT', 'UPDATE', 'DELETE'

    @Column(name = "old_values", columnDefinition = "JSON")
    private String oldValues;

    @Column(name = "new_values", columnDefinition = "JSON")
    private String newValues;

    @Column(name = "changed_by", length = 100)
    @Size(max = 100, message = "Өөрчлөгч 100 тэмдэгтээс ихгүй байх ёстой")
    private String changedBy;

    @CreationTimestamp
    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    @Column(name = "ip_address", length = 45)
    @Size(max = 45, message = "IP хаяг 45 тэмдэгтээс ихгүй байх ёстой")
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    // Action constants
    public static final String ACTION_INSERT = "INSERT";
    public static final String ACTION_UPDATE = "UPDATE";
    public static final String ACTION_DELETE = "DELETE";

    // Constructors
    public AuditLog() {
        this.id = java.util.UUID.randomUUID().toString();
        this.changedAt = LocalDateTime.now();
    }

    public AuditLog(String tableName, String recordId, String action, String changedBy) {
        this();
        this.tableName = tableName;
        this.recordId = recordId;
        this.action = action;
        this.changedBy = changedBy;
    }

    // Business methods
    public boolean isInsertAction() {
        return ACTION_INSERT.equals(action);
    }

    public boolean isUpdateAction() {
        return ACTION_UPDATE.equals(action);
    }

    public boolean isDeleteAction() {
        return ACTION_DELETE.equals(action);
    }

    public String getActionText() {
        switch (action) {
            case ACTION_INSERT: return "Үүсгэсэн";
            case ACTION_UPDATE: return "Засварласан";
            case ACTION_DELETE: return "Устгасан";
            default: return action;
        }
    }

    public void setValuesAsJson(String oldValues, String newValues) {
        this.oldValues = oldValues;
        this.newValues = newValues;
    }

    public void setClientInfo(String ipAddress, String userAgent) {
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
    }

    // Static factory methods
    public static AuditLog forInsert(String tableName, String recordId, String newValues, String changedBy) {
        AuditLog log = new AuditLog(tableName, recordId, ACTION_INSERT, changedBy);
        log.setNewValues(newValues);
        return log;
    }

    public static AuditLog forUpdate(String tableName, String recordId, String oldValues, String newValues, String changedBy) {
        AuditLog log = new AuditLog(tableName, recordId, ACTION_UPDATE, changedBy);
        log.setOldValues(oldValues);
        log.setNewValues(newValues);
        return log;
    }

    public static AuditLog forDelete(String tableName, String recordId, String oldValues, String changedBy) {
        AuditLog log = new AuditLog(tableName, recordId, ACTION_DELETE, changedBy);
        log.setOldValues(oldValues);
        return log;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTableName() { return tableName; }
    public void setTableName(String tableName) { this.tableName = tableName; }

    public String getRecordId() { return recordId; }
    public void setRecordId(String recordId) { this.recordId = recordId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getOldValues() { return oldValues; }
    public void setOldValues(String oldValues) { this.oldValues = oldValues; }

    public String getNewValues() { return newValues; }
    public void setNewValues(String newValues) { this.newValues = newValues; }

    public String getChangedBy() { return changedBy; }
    public void setChangedBy(String changedBy) { this.changedBy = changedBy; }

    public LocalDateTime getChangedAt() { return changedAt; }
    public void setChangedAt(LocalDateTime changedAt) { this.changedAt = changedAt; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    // equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuditLog auditLog = (AuditLog) o;
        return Objects.equals(id, auditLog.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // toString
    @Override
    public String toString() {
        return "AuditLog{" +
                "id='" + id + '\'' +
                ", tableName='" + tableName + '\'' +
                ", recordId='" + recordId + '\'' +
                ", action='" + action + '\'' +
                ", changedBy='" + changedBy + '\'' +
                ", changedAt=" + changedAt +
                '}';
    }
}