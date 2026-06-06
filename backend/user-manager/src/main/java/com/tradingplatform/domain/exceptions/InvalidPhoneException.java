package com.tradingplatform.domain.exceptions;

public class InvalidPhoneException extends RuntimeException {

    public InvalidPhoneException(String message) {
        super(message);
    }
}
