package com.tradingplatform.domain;

import java.util.UUID;

public record UserID(UUID value) {

    public static UserID of (final UUID value) {
        return new UserID(value);
    }
}
