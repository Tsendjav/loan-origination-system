package com.company.los.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * DuplicateEmailException нь имэйл хаяг давхардсан алдааг илэрхийлнэ.
 * Энэ exception үүсэх үед HTTP 409 Conflict статусыг буцаана.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException(String message) {
        super(message);
    }

    public DuplicateEmailException(String message, Throwable cause) {
        super(message, cause);
    }
}