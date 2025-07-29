package com.company.los.exception;

/**
 * Регистрийн дугаар давхардсан үед гарах exception
 */
public class DuplicateRegisterNumberException extends RuntimeException {
    
    public DuplicateRegisterNumberException(String message) {
        super(message);
    }
    
    public DuplicateRegisterNumberException(String message, Throwable cause) {
        super(message, cause);
    }
}