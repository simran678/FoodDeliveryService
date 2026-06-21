package org.services.fooddeliveryservice.exception;

import org.springframework.http.HttpStatus;

public class ResourceConflictException extends ApplicationException {
    public ResourceConflictException(String message, String errorCode) {
        super(HttpStatus.CONFLICT, message, errorCode);
    }
}
