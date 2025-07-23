package com.company.los.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Үйл ажиллагааны лог Entity
 * Activity Log Entity for tracking user activities
 */
@Entity
@Table(name = "activity_logs", indexes = {
        @Index(name = "idx_activity_user_id", columnList = "user_id"),
        @Index(name = "idx_activity_type", columnList = "activity_type"),
        @Index(name = "idx_activity_entity_type", columnList = "entity_type"),
        @Index(name = "idx_activity_entity_id", columnList = "entity_id"),
        @Index(name = "idx_activity_created_at", columnList = "created_at")
})
public class ActivityLog {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_activity_user"))
    private User user;

    @Column(name = "activity_type", nullable = false, length = 100)
    @NotBlank(message = "Үйл ажиллагааны төрөл заавал байх ёстой")
    @Size(max = 100, message = "Үйл ажиллагааны төрөл 100 тэмдэгтээс ихгүй байх ёстой")
    private String activityType;

    @Column(name = "entity_type", length = 100)
    @Size(max = 100, message = "Объектын төрөл 100 тэмдэгтээс ихгүй байх ёстой")
    private String entityType;

    @Column(name = "entity_id", length = 36)
    @Size(max = 36, message = "Объектын ID 36 тэмдэгтээс ихгүй байх ёстой")
    private String entityId;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "details", columnDefinition = "JSON")
    private String details;

    @Column(name = "ip_address", length = 45)
    @Size(max = 45, message = "IP хаяг 45 тэмдэгтээс ихгүй байх ёстой")
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // Activity type constants
    public static final String LOGIN = "LOGIN";
    public static final String LOGOUT = "LOGOUT";
    public static final String LOGIN_FAILED = "LOGIN_FAILED";
    public static final String PASSWORD_CHANGED = "PASSWORD_CHANGED";
    public static final String CUSTOMER_CREATED = "CUSTOMER_CREATED";
    public static final String CUSTOMER_UPDATED = "CUSTOMER_UPDATED";
    public static final String CUSTOMER_DELETED = "CUSTOMER_DELETED";
    public static final String LOAN_APPLICATION_CREATED = "LOAN_APPLICATION_CREATED";
    public static final String LOAN_APPLICATION_SUBMITTED = "LOAN_APPLICATION_SUBMITTED";
    public static final String LOAN_APPLICATION_APPROVED = "LOAN_APPLICATION_APPROVED";
    public static final String LOAN_APPLICATION_REJECTED = "LOAN_APPLICATION_REJECTED";
    public static final String DOCUMENT_UPLOADED = "DOCUMENT_UPLOADED";
    public static final String DOCUMENT_VERIFIED = "DOCUMENT_VERIFIED";
    public static final String DOCUMENT_REJECTED = "DOCUMENT_REJECTED";
    public static final String ROLE_ASSIGNED = "ROLE_ASSIGNED";
    public static final String ROLE_REMOVED = "ROLE_REMOVED";
    public static final String PERMISSION_GRANTED = "PERMISSION_GRANTED";
    public static final String PERMISSION_REVOKED = "PERMISSION_REVOKED";
    public static final String REPORT_GENERATED = "REPORT_GENERATED";
    public static final String EXPORT_DATA = "EXPORT_DATA";
    public static final String IMPORT_DATA = "IMPORT_DATA";
    public static final String SYSTEM_SETTING_CHANGED = "SYSTEM_SETTING_CHANGED";

    // Entity type constants
    public static final String ENTITY_CUSTOMER = "Customer";
    public static final String ENTITY_LOAN_APPLICATION = "LoanApplication";
    public static final String ENTITY_DOCUMENT = "Document";
    public static final String ENTITY_USER = "User";
    public static final String ENTITY_ROLE = "Role";
    public static final String ENTITY_PERMISSION = "Permission";
    public static final String ENTITY_LOAN_PRODUCT = "LoanProduct";
    public static final String ENTITY_SYSTEM_SETTING = "SystemSetting";

    // Constructors
    public ActivityLog() {
        this.id = java.util.UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
    }

    public ActivityLog(User user, String activityType, String description) {
        this();
        this.user = user;
        this.activityType = activityType;
        this.description = description;
    }

    public ActivityLog(User user, String activityType, String entityType, String entityId, String description) {
        this(user, activityType, description);
        this.entityType = entityType;
        this.entityId = entityId;
    }

    // Business methods
    public boolean isLoginActivity() {
        return LOGIN.equals(activityType) || LOGOUT.equals(activityType) || LOGIN_FAILED.equals(activityType);
    }

    public boolean isSecurityActivity() {
        return LOGIN_FAILED.equals(activityType) || 
               PASSWORD_CHANGED.equals(activityType) ||
               ROLE_ASSIGNED.equals(activityType) ||
               ROLE_REMOVED.equals(activityType) ||
               PERMISSION_GRANTED.equals(activityType) ||
               PERMISSION_REVOKED.equals(activityType);
    }

    public boolean isBusinessActivity() {
        return CUSTOMER_CREATED.equals(activityType) ||
               CUSTOMER_UPDATED.equals(activityType) ||
               LOAN_APPLICATION_CREATED.equals(activityType) ||
               LOAN_APPLICATION_SUBMITTED.equals(activityType) ||
               LOAN_APPLICATION_APPROVED.equals(activityType) ||
               LOAN_APPLICATION_REJECTED.equals(activityType) ||
               DOCUMENT_UPLOADED.equals(activityType) ||
               DOCUMENT_VERIFIED.equals(activityType);
    }

    public String getActivityTypeText() {
        switch (activityType) {
            case LOGIN: return "Нэвтэрсэн";
            case LOGOUT: return "Гарсан";
            case LOGIN_FAILED: return "Нэвтрэх амжилтгүй";
            case PASSWORD_CHANGED: return "Нууц үг солисон";
            case CUSTOMER_CREATED: return "Харилцагч үүсгэсэн";
            case CUSTOMER_UPDATED: return "Харилцагч шинэчилсэн";
            case CUSTOMER_DELETED: return "Харилцагч устгасан";
            case LOAN_APPLICATION_CREATED: return "Зээлийн хүсэлт үүсгэсэн";
            case LOAN_APPLICATION_SUBMITTED: return "Зээлийн хүсэлт илгээсэн";
            case LOAN_APPLICATION_APPROVED: return "Зээлийн хүсэлт зөвшөөрсөн";
            case LOAN_APPLICATION_REJECTED: return "Зээлийн хүсэлт татгалзсан";
            case DOCUMENT_UPLOADED: return "Баримт бичиг илгээсэн";
            case DOCUMENT_VERIFIED: return "Баримт бичиг баталгаажуулсан";
            case DOCUMENT_REJECTED: return "Баримт бичиг татгалзсан";
            case ROLE_ASSIGNED: return "Дүр оноосон";
            case ROLE_REMOVED: return "Дүр хассан";
            case PERMISSION_GRANTED: return "Эрх олгосон";
            case PERMISSION_REVOKED: return "Эрх хассан";
            case REPORT_GENERATED: return "Тайлан үүсгэсэн";
            case EXPORT_DATA: return "Өгөгдөл экспорт хийсэн";
            case IMPORT_DATA: return "Өгөгдөл импорт хийсэн";
            case SYSTEM_SETTING_CHANGED: return "Системийн тохиргоо өөрчилсөн";
            default: return activityType;
        }
    }

    public void setClientInfo(String ipAddress, String userAgent) {
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
    }

    public void setDetailsAsJson(String details) {
        this.details = details;
    }

    // Static factory methods
    public static ActivityLog login(User user, String ipAddress, String userAgent) {
        ActivityLog log = new ActivityLog(user, LOGIN, "Хэрэглэгч нэвтэрсэн");
        log.setClientInfo(ipAddress, userAgent);
        return log;
    }

    public static ActivityLog logout(User user) {
        return new ActivityLog(user, LOGOUT, "Хэрэглэгч гарсан");
    }

    public static ActivityLog loginFailed(String username, String ipAddress, String userAgent) {
        ActivityLog log = new ActivityLog(null, LOGIN_FAILED, "Амжилтгүй нэвтрэх оролдлого: " + username);
        log.setClientInfo(ipAddress, userAgent);
        return log;
    }

    public static ActivityLog customerCreated(User user, String customerId) {
        return new ActivityLog(user, CUSTOMER_CREATED, ENTITY_CUSTOMER, customerId, "Шинэ харилцагч үүсгэсэн");
    }

    public static ActivityLog loanApplicationSubmitted(User user, String applicationId) {
        return new ActivityLog(user, LOAN_APPLICATION_SUBMITTED, ENTITY_LOAN_APPLICATION, applicationId, "Зээлийн хүсэлт илгээсэн");
    }

    public static ActivityLog documentUploaded(User user, String documentId, String documentType) {
        return new ActivityLog(user, DOCUMENT_UPLOADED, ENTITY_DOCUMENT, documentId, "Баримт бичиг илгээсэн: " + documentType);
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getActivityType() { return activityType; }
    public void setActivityType(String activityType) { this.activityType = activityType; }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public String getEntityId() { return entityId; }
    public void setEntityId(String entityId) { this.entityId = entityId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ActivityLog that = (ActivityLog) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // toString
    @Override
    public String toString() {
        return "ActivityLog{" +
                "id='" + id + '\'' +
                ", user=" + (user != null ? user.getUsername() : "null") +
                ", activityType='" + activityType + '\'' +
                ", entityType='" + entityType + '\'' +
                ", entityId='" + entityId + '\'' +
                ", description='" + description + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}