package com.tradingplatform.domain.model;

import com.tradingplatform.domain.exceptions.InvalidPasswordException;

public record Password(String value) {

    public Password {
        if (value != null && (value.length() < 8
                || !value.matches(".*[A-Z].*")
                || !value.matches(".*[0-9].*")
                || !value.matches(".*[!@#$%^&*].*"))) {
            throw new InvalidPasswordException("Password does not meet security requirements");
        }
    }

    public static Password of(String value) {
        return new Password(value);
    }

    public static Password unsafeReconstitute(String value) {
        return new Password(value);
    }
}
