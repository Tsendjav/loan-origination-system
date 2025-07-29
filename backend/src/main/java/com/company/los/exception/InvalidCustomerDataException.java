package com.company.los.exception;

/**
 * Харилцагчийн мэдээлэл буруу байх үед гарах exception
 */
public class InvalidCustomerDataException extends RuntimeException {
    
    public InvalidCustomerDataException(String message) {
        super(message);
    }
    
    public InvalidCustomerDataException(String message, Throwable cause) {
        super(message, cause);
    }
}