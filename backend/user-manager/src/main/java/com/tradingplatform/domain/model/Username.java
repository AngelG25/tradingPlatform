package com.tradingplatform.domain.model;

public record Username(String value) {

    public static Username of(String value) {
        return new Username(value);
    }
}
