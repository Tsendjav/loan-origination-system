package com.company.los.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Global Exception Handler - ЭЦСИЙН ЗАСВАРЛАСАН ХУВИЛБАР
 * ⭐ CHARACTER ENCODING АЛДАА БҮРЭН ШИЙДЭГДСЭН ⭐
 * ⭐ UTF-8 ДЭМЖЛЭГ НЭМЭГДСЭН ⭐
 * ⭐ LOGGER ЗАСВАРЛАСАН ⭐
 * Системийн бүх алдаануудыг төвлөрсөн байдлаар барих
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ⭐ ЗАСВАР: @Slf4j annotation-ийн оронд шууд Logger ашиглах ⭐
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final String UTF8_CONTENT_TYPE = "application/json;charset=UTF-8";

    /**
     * ⭐ VALIDATION АЛДААНУУД - CHARACTER ENCODING ЗАСВАРЛАГДСАН ⭐
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex, HttpServletRequest request) {
        log.warn("Validation error: {}", ex.getMessage());
        
        Map<String, Object> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            
            // ⭐ CHARACTER ENCODING АЛДАА ШИЙДЭХ ⭐
            errorMessage = fixCharacterEncoding(errorMessage);
            
            // ⭐ ТАЛБАР ТУСГАЙЛСАН МЕССЕЖ ⭐
            errorMessage = getFieldSpecificMessage(fieldName, errorMessage, error.getCode());
            
            errors.put(fieldName, errorMessage);
        });

        // ⭐ LOGIN REQUEST ТУСГАЙЛСАН ERROR RESPONSE ⭐
        String mainMessage = "Өгөгдөл оруулахад алдаа гарлаа";
        if (request.getRequestURI().contains("/auth/login")) {
            mainMessage = "Нэвтрэх мэдээлэл буруу байна";
        }

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Failed")
                .message(mainMessage)
                .path(request.getRequestURI())
                .details(errors)
                .build();

        return ResponseEntity.badRequest()
                .header("Content-Type", UTF8_CONTENT_TYPE)
                .body(errorResponse);
    }

    /**
     * ⭐ CHARACTER ENCODING АЛДАА ЗАСАХ МЕТОД ⭐
     */
    private String fixCharacterEncoding(String message) {
        if (message == null) {
            return null;
        }
        
        // Question mark эсвэл garbled text шалгах
        if (message.contains("???") || message.contains("????????????")) {
            return null; // Default message ашиглуулах
        }
        
        try {
            // UTF-8 encoding шалгах
            byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
            String fixed = new String(bytes, StandardCharsets.UTF_8);
            
            // Хэрэв encoding алдаа байвал default message ашиглах
            if (fixed.contains("�") || fixed.contains("???")) {
                return null;
            }
            
            return fixed;
        } catch (Exception e) {
            log.debug("Character encoding fix failed: {}", e.getMessage());
            return null;
        }
    }

    /**
     * ⭐ ТАЛБАР ТУСГАЙЛСАН МЕССЕЖ АВАХ ⭐
     */
    private String getFieldSpecificMessage(String fieldName, String originalMessage, String errorCode) {
        // Хэрэв originalMessage алдаатай бол default message ашиглах
        if (originalMessage == null || originalMessage.trim().isEmpty() || 
            originalMessage.contains("???") || originalMessage.contains("????????????")) {
            
            return getDefaultFieldMessage(fieldName, errorCode);
        }
        
        // Username талбарын тусгайлсан мессеж
        if ("username".equals(fieldName)) {
            if ("NotBlank".equals(errorCode) || originalMessage.contains("NotBlank")) {
                return "Хэрэглэгчийн нэр заавал оруулна уу";
            } else if ("Size".equals(errorCode) || originalMessage.contains("Size")) {
                return "Хэрэглэгчийн нэр 3-50 тэмдэгт байх ёстой";
            } else if ("Pattern".equals(errorCode) || originalMessage.contains("Pattern")) {
                return "Хэрэглэгчийн нэр зөвхөн үсэг, тоо, цэг, дэд зураас агуулах боломжтой";
            }
        }
        
        // Password талбарын тусгайлсан мессеж
        if ("password".equals(fieldName)) {
            if ("NotBlank".equals(errorCode) || originalMessage.contains("NotBlank")) {
                return "Нууц үг заавал оруулна уу";
            } else if ("Size".equals(errorCode) || originalMessage.contains("Size")) {
                return "Нууц үг 6-100 тэмдэгт байх ёстой";
            }
        }
        
        return originalMessage;
    }

    /**
     * ⭐ DEFAULT ТАЛБАРЫН МЕССЕЖ ⭐
     */
    private String getDefaultFieldMessage(String fieldName, String errorCode) {
        switch (fieldName) {
            case "username":
                if ("NotBlank".equals(errorCode)) {
                    return "Хэрэглэгчийн нэр заавал оруулна уу";
                } else if ("Size".equals(errorCode)) {
                    return "Хэрэглэгчийн нэр 3-50 тэмдэгт байх ёстой";
                } else if ("Pattern".equals(errorCode)) {
                    return "Хэрэглэгчийн нэр зөвхөн үсэг, тоо, цэг, дэд зураас агуулах боломжтой";
                }
                return "Хэрэглэгчийн нэр заавал оруулна уу";
                
            case "password":
                if ("NotBlank".equals(errorCode)) {
                    return "Нууц үг заавал оруулна уу";
                } else if ("Size".equals(errorCode)) {
                    return "Нууц үг 6-100 тэмдэгт байх ёстой";
                }
                return "Нууц үг заавал оруулна уу";
                
            case "email":
                if ("NotBlank".equals(errorCode)) {
                    return "И-мэйл заавал оруулна уу";
                } else if ("Email".equals(errorCode)) {
                    return "И-мэйлийн формат буруу байна";
                }
                return "И-мэйл заавал оруулна уу";
                
            default:
                return fieldName + " талбар заавал бөглөнө үү";
        }
    }

    /**
     * Constraint Violation алдаанууд
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        log.warn("Constraint violation: {}", ex.getMessage());
        
        Map<String, Object> errors = new HashMap<>();
        Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();
        for (ConstraintViolation<?> violation : violations) {
            String fieldName = violation.getPropertyPath().toString();
            String errorMessage = fixCharacterEncoding(violation.getMessage());
            
            if (errorMessage == null) {
                errorMessage = getDefaultFieldMessage(fieldName, "NotBlank");
            }
            
            errors.put(fieldName, errorMessage);
        }

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Constraint Violation")
                .message("Өгөгдлийн шалгалтад алдаа гарлаа")
                .path(request.getRequestURI())
                .details(errors)
                .build();

        return ResponseEntity.badRequest()
                .header("Content-Type", UTF8_CONTENT_TYPE)
                .body(errorResponse);
    }

    /**
     * Authentication алдаанууд
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex, HttpServletRequest request) {
        log.warn("Authentication error: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error("Authentication Failed")
                .message("Нэвтрэх нэр эсвэл нууц үг буруу")
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .header("Content-Type", UTF8_CONTENT_TYPE)
                .body(errorResponse);
    }

    /**
     * Bad Credentials алдаа
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        log.warn("Bad credentials: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error("Invalid Credentials")
                .message("Нэвтрэх мэдээлэл буруу байна")
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .header("Content-Type", UTF8_CONTENT_TYPE)
                .body(errorResponse);
    }

    /**
     * Access Denied алдаанууд
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        log.warn("Access denied: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.FORBIDDEN.value())
                .error("Access Denied")
                .message("Энэ үйлдэл хийхэд танд эрх хүрэхгүй байна")
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .header("Content-Type", UTF8_CONTENT_TYPE)
                .body(errorResponse);
    }

    /**
     * Data Integrity Violation
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex, HttpServletRequest request) {
        log.error("Data integrity violation: {}", ex.getMessage());

        String message = "Өгөгдлийн шалгалтад алдаа гарлаа";
        if (ex.getMessage() != null) {
            if (ex.getMessage().contains("Unique")) {
                message = "Энэ утга аль хэдийн системд бүртгэгдсэн байна";
            } else if (ex.getMessage().contains("foreign key")) {
                message = "Холбоотой өгөгдөл олдсонгүй";
            }
        }

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error("Data Integrity Violation")
                .message(message)
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .header("Content-Type", UTF8_CONTENT_TYPE)
                .body(errorResponse);
    }

    /**
     * Database алдаанууд
     */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponse> handleDataAccessException(DataAccessException ex, HttpServletRequest request) {
        log.error("Data access error: {}", ex.getMessage(), ex);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Database Error")
                .message("Өгөгдлийн сантай холбогдоход алдаа гарлаа")
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .header("Content-Type", UTF8_CONTENT_TYPE)
                .body(errorResponse);
    }

    /**
     * Method Argument Type Mismatch
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        log.warn("Type mismatch error: {}", ex.getMessage());

        String message = String.format("Параметр '%s'-ийн утга буруу байна", ex.getName());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Invalid Parameter")
                .message(message)
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.badRequest()
                .header("Content-Type", UTF8_CONTENT_TYPE)
                .body(errorResponse);
    }

    /**
     * Illegal Argument Exception
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        log.warn("Illegal argument: {}", ex.getMessage());

        String message = ex.getMessage() != null ? ex.getMessage() : "Буруу параметр дамжуулсан байна";
        message = fixCharacterEncoding(message);
        if (message == null) {
            message = "Буруу параметр дамжуулсан байна";
        }

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Invalid Argument")
                .message(message)
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.badRequest()
                .header("Content-Type", UTF8_CONTENT_TYPE)
                .body(errorResponse);
    }

    /**
     * Business Logic Exception (Custom)
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex, HttpServletRequest request) {
        log.warn("Business logic error: {}", ex.getMessage());

        String message = fixCharacterEncoding(ex.getMessage());
        if (message == null) {
            message = "Бизнес логикийн алдаа гарлаа";
        }

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(ex.getStatus().value())
                .error(ex.getErrorCode())
                .message(message)
                .path(request.getRequestURI())
                .details(ex.getDetails())
                .build();

        return ResponseEntity.status(ex.getStatus())
                .header("Content-Type", UTF8_CONTENT_TYPE)
                .body(errorResponse);
    }

    /**
     * Resource Not Found Exception
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        log.warn("Resource not found: {}", ex.getMessage());

        String message = fixCharacterEncoding(ex.getMessage());
        if (message == null) {
            message = "Хайсан мэдээлэл олдсонгүй";
        }

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Resource Not Found")
                .message(message)
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .header("Content-Type", UTF8_CONTENT_TYPE)
                .body(errorResponse);
    }

    /**
     * ⭐ REQUEST BODY MISSING EXCEPTION ⭐
     * Хэрэв request body байхгүй бол
     */
    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMessageNotReadable(
            org.springframework.http.converter.HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.warn("Request body not readable: {}", ex.getMessage());

        String message = "Хүсэлтийн мэдээлэл буруу эсвэл байхгүй байна";
        if (request.getRequestURI().contains("/auth/login")) {
            message = "Нэвтрэх мэдээлэл илгээгдээгүй байна";
        }

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Invalid Request Body")
                .message(message)
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.badRequest()
                .header("Content-Type", UTF8_CONTENT_TYPE)
                .body(errorResponse);
    }

    /**
     * Бусад бүх алдаанууд
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message("Системд алдаа гарлаа. Удахгүй засагдах болно")
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .header("Content-Type", UTF8_CONTENT_TYPE)
                .body(errorResponse);
    }

    /**
     * ⭐ ERROR RESPONSE MODEL - UTF-8 ДЭМЖЛЭГТЭЙ ⭐
     */
    public static class ErrorResponse {
        private LocalDateTime timestamp;
        private int status;
        private String error;
        private String message;
        private String path;
        private Map<String, Object> details;

        // Constructors
        public ErrorResponse() {}

        private ErrorResponse(Builder builder) {
            this.timestamp = builder.timestamp;
            this.status = builder.status;
            this.error = builder.error;
            this.message = builder.message;
            this.path = builder.path;
            this.details = builder.details;
        }

        // Getters and Setters
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

        public int getStatus() { return status; }
        public void setStatus(int status) { this.status = status; }

        public String getError() { return error; }
        public void setError(String error) { this.error = error; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public String getPath() { return path; }
        public void setPath(String path) { this.path = path; }

        public Map<String, Object> getDetails() { return details; }
        public void setDetails(Map<String, Object> details) { this.details = details; }

        // Builder Pattern
        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private LocalDateTime timestamp;
            private int status;
            private String error;
            private String message;
            private String path;
            private Map<String, Object> details;

            public Builder timestamp(LocalDateTime timestamp) {
                this.timestamp = timestamp;
                return this;
            }

            public Builder status(int status) {
                this.status = status;
                return this;
            }

            public Builder error(String error) {
                this.error = error;
                return this;
            }

            public Builder message(String message) {
                this.message = message;
                return this;
            }

            public Builder path(String path) {
                this.path = path;
                return this;
            }

            public Builder details(Map<String, Object> details) {
                this.details = details;
                return this;
            }

            public ErrorResponse build() {
                return new ErrorResponse(this);
            }
        }
    }

    /**
     * Custom Business Exception
     */
    public static class BusinessException extends RuntimeException {
        private final HttpStatus status;
        private final String errorCode;
        private final Map<String, Object> details;

        public BusinessException(String message) {
            this(message, HttpStatus.BAD_REQUEST, "BUSINESS_ERROR", null);
        }

        public BusinessException(String message, HttpStatus status) {
            this(message, status, "BUSINESS_ERROR", null);
        }

        public BusinessException(String message, HttpStatus status, String errorCode, Map<String, Object> details) {
            super(message);
            this.status = status;
            this.errorCode = errorCode;
            this.details = details;
        }

        public HttpStatus getStatus() { return status; }
        public String getErrorCode() { return errorCode; }
        public Map<String, Object> getDetails() { return details; }
    }

    /**
     * Resource Not Found Exception
     */
    public static class ResourceNotFoundException extends RuntimeException {
        public ResourceNotFoundException(String message) {
            super(message);
        }

        public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
            super(String.format("%s олдсонгүй: %s = %s", resourceName, fieldName, fieldValue));
        }
    }
}