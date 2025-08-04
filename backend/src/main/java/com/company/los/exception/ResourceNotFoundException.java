package com.company.los.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a requested resource is not found
 * 
 * @author LOS Development Team
 * @version 1.0
 * @since 2025-01-01
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String resourceName;
    private final String fieldName;
    private final Object fieldValue;

    /**
     * Constructor with simple message
     * 
     * @param message the error message
     */
    public ResourceNotFoundException(String message) {
        super(message);
        this.resourceName = null;
        this.fieldName = null;
        this.fieldValue = null;
    }

    /**
     * Constructor with resource details
     * 
     * @param resourceName the name of the resource
     * @param fieldName the field name that was searched
     * @param fieldValue the field value that was searched
     */
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s олдсонгүй: %s = '%s'", resourceName, fieldName, fieldValue));
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    /**
     * Constructor with message and cause
     * 
     * @param message the error message
     * @param cause the cause of the exception
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
        this.resourceName = null;
        this.fieldName = null;
        this.fieldValue = null;
    }

    /**
     * Constructor with resource details and cause
     * 
     * @param resourceName the name of the resource
     * @param fieldName the field name that was searched
     * @param fieldValue the field value that was searched
     * @param cause the cause of the exception
     */
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue, Throwable cause) {
        super(String.format("%s олдсонгүй: %s = '%s'", resourceName, fieldName, fieldValue), cause);
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    // Getters
    public String getResourceName() {
        return resourceName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Object getFieldValue() {
        return fieldValue;
    }

    /**
     * Helper method to create ResourceNotFoundException for Customer not found by ID
     */
    public static ResourceNotFoundException customerNotFound(Object customerId) {
        return new ResourceNotFoundException("Харилцагч", "id", customerId);
    }

    /**
     * Helper method to create ResourceNotFoundException for LoanApplication not found by ID
     */
    public static ResourceNotFoundException loanApplicationNotFound(Object applicationId) {
        return new ResourceNotFoundException("Зээлийн хүсэлт", "id", applicationId);
    }

    /**
     * Helper method to create ResourceNotFoundException for Document not found by ID
     */
    public static ResourceNotFoundException documentNotFound(Object documentId) {
        return new ResourceNotFoundException("Баримт бичиг", "id", documentId);
    }

    /**
     * Helper method to create ResourceNotFoundException for LoanProduct not found by ID
     */
    public static ResourceNotFoundException loanProductNotFound(Object productId) {
        return new ResourceNotFoundException("Зээлийн бүтээгдэхүүн", "id", productId);
    }

    /**
     * Helper method to create ResourceNotFoundException for User not found by ID
     */
    public static ResourceNotFoundException userNotFound(Object userId) {
        return new ResourceNotFoundException("Хэрэглэгч", "id", userId);
    }

    /**
     * Helper method to create ResourceNotFoundException for User not found by username
     */
    public static ResourceNotFoundException userNotFoundByUsername(String username) {
        return new ResourceNotFoundException("Хэрэглэгч", "username", username);
    }

    /**
     * Helper method to create ResourceNotFoundException for Customer not found by email
     */
    public static ResourceNotFoundException customerNotFoundByEmail(String email) {
        return new ResourceNotFoundException("Харилцагч", "email", email);
    }
}