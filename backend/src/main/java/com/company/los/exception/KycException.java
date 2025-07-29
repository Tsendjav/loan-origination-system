package com.company.los.exception;

/**
 * KYC процесстэй холбоотой алдаа
 */
public class KycException extends RuntimeException {
    
    public KycException(String message) {
        super(message);
    }
    
    public KycException(String message, Throwable cause) {
        super(message, cause);
    }
}