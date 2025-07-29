// ==================== CustomerNotFoundException.java ====================
package com.company.los.exception;

/**
 * Харилцагч олдохгүй байх үед гарах exception
 */
public class CustomerNotFoundException extends RuntimeException {
    
    public CustomerNotFoundException(String message) {
        super(message);
    }
    
    public CustomerNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public CustomerNotFoundException(java.util.UUID customerId) {
        super("Customer not found with ID: " + customerId);
    }
}