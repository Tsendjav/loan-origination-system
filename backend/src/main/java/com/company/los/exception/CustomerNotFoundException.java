package com.company.los.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * CustomerNotFoundException нь хэрэглэгч олдсонгүй гэсэн алдааг илэрхийлнэ.
 * Энэ exception үүсэх үед HTTP 404 Not Found статусыг буцаана.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class CustomerNotFoundException extends RuntimeException {

    public CustomerNotFoundException(String message) {
        super(message);
    }

    public CustomerNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}