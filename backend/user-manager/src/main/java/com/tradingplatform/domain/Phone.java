package com.tradingplatform.domain;

import com.tradingplatform.domain.exceptions.InvalidPhoneException;

import java.util.regex.Pattern;

public record Phone(String value) {

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^\\+?[1-9]\\d{1,14}$"
    );

    public Phone {
            if (value == null || !PHONE_PATTERN.matcher(value).matches()) {
                throw new InvalidPhoneException("Invalid phone number: " + value);
            }
    }

    public static Phone of(String value) {
        return new Phone(value);
    }
}
