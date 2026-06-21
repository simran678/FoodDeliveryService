package org.services.fooddeliveryservice.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedResourceAccessException extends ApplicationException {
    public UnauthorizedResourceAccessException(String message) {
        super(HttpStatus.FORBIDDEN, message, "FORBIDDEN");
    }
}
