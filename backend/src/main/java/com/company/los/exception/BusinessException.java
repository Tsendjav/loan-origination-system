package com.company.los.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown for business logic violations
 * 
 * @author LOS Development Team
 * @version 1.0
 * @since 2025-01-01
 */
@ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    
    private final String errorCode;
    private final Object[] args;

    /**
     * Constructor with simple message
     * 
     * @param message the error message
     */
    public BusinessException(String message) {
        super(message);
        this.errorCode = null;
        this.args = null;
    }

    /**
     * Constructor with error code and message
     * 
     * @param errorCode the error code
     * @param message the error message
     */
    public BusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.args = null;
    }

    /**
     * Constructor with error code, message and arguments
     * 
     * @param errorCode the error code
     * @param message the error message
     * @param args the message arguments
     */
    public BusinessException(String errorCode, String message, Object... args) {
        super(message);
        this.errorCode = errorCode;
        this.args = args;
    }

    /**
     * Constructor with message and cause
     * 
     * @param message the error message
     * @param cause the cause of the exception
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = null;
        this.args = null;
    }

    /**
     * Constructor with error code, message and cause
     * 
     * @param errorCode the error code
     * @param message the error message
     * @param cause the cause of the exception
     */
    public BusinessException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.args = null;
    }

    // Getters
    public String getErrorCode() {
        return errorCode;
    }

    public Object[] getArgs() {
        return args;
    }

    // Common business exceptions for LOS

    /**
     * Customer related business exceptions
     */
    public static class Customer {
        
        public static BusinessException alreadyExists(String email) {
            return new BusinessException("CUSTOMER_ALREADY_EXISTS", 
                "Энэ и-мэйл хаягтай харилцагч аль хэдийн байна: " + email);
        }
        
        public static BusinessException invalidAge(int age) {
            return new BusinessException("CUSTOMER_INVALID_AGE", 
                "Харилцагчийн нас хангалтгүй: " + age + ". 18-аас дээш байх ёстой.");
        }
        
        public static BusinessException inactiveStatus() {
            return new BusinessException("CUSTOMER_INACTIVE", 
                "Идэвхгүй харилцагчтай ажиллах боломжгүй");
        }
        
        public static BusinessException kycNotCompleted() {
            return new BusinessException("CUSTOMER_KYC_INCOMPLETE", 
                "Харилцагчийн KYC (танин мэдэх) процесс дууссангүй");
        }
    }

    /**
     * Loan application related business exceptions
     */
    public static class LoanApplication {
        
        public static BusinessException invalidAmount(double amount, double minAmount, double maxAmount) {
            return new BusinessException("LOAN_INVALID_AMOUNT", 
                String.format("Зээлийн дүн буруу: %.0f. %.0f-%.0f хооронд байх ёстой", 
                    amount, minAmount, maxAmount));
        }
        
        public static BusinessException invalidStatus(String currentStatus, String targetStatus) {
            return new BusinessException("LOAN_INVALID_STATUS_TRANSITION", 
                String.format("Статус шилжилт боломжгүй: %s -> %s", currentStatus, targetStatus));
        }
        
        public static BusinessException alreadyApproved() {
            return new BusinessException("LOAN_ALREADY_APPROVED", 
                "Зээлийн хүсэлт аль хэдийн зөвшөөрөгдсөн байна");
        }
        
        public static BusinessException alreadyRejected() {
            return new BusinessException("LOAN_ALREADY_REJECTED", 
                "Зээлийн хүсэлт аль хэдийн татгалзагдсан байна");
        }
        
        public static BusinessException cannotModifyAfterSubmission() {
            return new BusinessException("LOAN_CANNOT_MODIFY", 
                "Илгээгдсэн хүсэлтийг өөрчлөх боломжгүй");
        }
        
        public static BusinessException insufficientCreditScore(int currentScore, int requiredScore) {
            return new BusinessException("LOAN_INSUFFICIENT_CREDIT_SCORE", 
                String.format("Зээлийн оноо хангалтгүй: %d. %d-аас дээш байх ёстой", 
                    currentScore, requiredScore));
        }
        
        public static BusinessException insufficientIncome(double currentIncome, double requiredIncome) {
            return new BusinessException("LOAN_INSUFFICIENT_INCOME", 
                String.format("Орлого хангалтгүй: %.0f. %.0f-аас дээш байх ёстой", 
                    currentIncome, requiredIncome));
        }
    }

    /**
     * Document related business exceptions
     */
    public static class Document {
        
        public static BusinessException unsupportedFileType(String fileType) {
            return new BusinessException("DOCUMENT_UNSUPPORTED_TYPE", 
                "Дэмжигдээгүй файлын төрөл: " + fileType);
        }
        
        public static BusinessException fileTooLarge(long fileSize, long maxSize) {
            return new BusinessException("DOCUMENT_FILE_TOO_LARGE", 
                String.format("Файлын хэмжээ хэтэрсэн: %d bytes. Максимум %d bytes", 
                    fileSize, maxSize));
        }
        
        public static BusinessException requiredDocumentMissing(String documentType) {
            return new BusinessException("DOCUMENT_REQUIRED_MISSING", 
                "Шаардлагатай баримт бичиг дутуу: " + documentType);
        }
        
        public static BusinessException cannotDeleteUsedDocument() {
            return new BusinessException("DOCUMENT_CANNOT_DELETE", 
                "Ашиглагдаж буй баримт бичигийг устгах боломжгүй");
        }
    }

    /**
     * Authentication and authorization related business exceptions
     */
    public static class Auth {
        
        public static BusinessException accountLocked() {
            return new BusinessException("AUTH_ACCOUNT_LOCKED", 
                "Дансны эрх түгжигдсэн байна");
        }
        
        public static BusinessException passwordExpired() {
            return new BusinessException("AUTH_PASSWORD_EXPIRED", 
                "Нууц үгийн хугацаа дууссан байна");
        }
        
        public static BusinessException tooManyFailedAttempts() {
            return new BusinessException("AUTH_TOO_MANY_ATTEMPTS", 
                "Олон удаа буруу оролдлого хийсэн байна");
        }
        
        public static BusinessException insufficientPermissions(String requiredPermission) {
            return new BusinessException("AUTH_INSUFFICIENT_PERMISSIONS", 
                "Хангалтгүй эрх: " + requiredPermission + " шаардлагатай");
        }
    }

    /**
     * System related business exceptions
     */
    public static class System {
        
        public static BusinessException maintenanceMode() {
            return new BusinessException("SYSTEM_MAINTENANCE", 
                "Систем засвар үйлчилгээний горимд байна");
        }
        
        public static BusinessException serviceUnavailable(String serviceName) {
            return new BusinessException("SYSTEM_SERVICE_UNAVAILABLE", 
                "Үйлчилгээ хүртэх боломжгүй: " + serviceName);
        }
        
        public static BusinessException configurationError(String configKey) {
            return new BusinessException("SYSTEM_CONFIG_ERROR", 
                "Системийн тохиргооны алдаа: " + configKey);
        }
    }
}