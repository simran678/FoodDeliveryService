package org.services.fooddeliveryservice.exception;

import org.springframework.http.HttpStatus;

public class InvalidRequestException extends ApplicationException {
    public InvalidRequestException(String message, String errorCode) {
        super(HttpStatus.BAD_REQUEST, message, errorCode);
    }
}
