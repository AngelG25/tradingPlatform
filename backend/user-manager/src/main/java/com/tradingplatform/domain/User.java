package com.tradingplatform.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jmolecules.ddd.annotation.AggregateRoot;

import java.util.List;
import java.util.UUID;

@AggregateRoot
@Getter
@EqualsAndHashCode(of = "id")
public class User {

    private final UserID id;
    private final String name;
    private final Password password;
    private final Email email;
    private final Phone phone;
    private final List<TradingTimeZone> tradingTimeZones;

    // Package-private constructor for persistence / ORM
    public User(final UserID id,
                final String name,
                final Password password,
                final Email email,
                final Phone phone,
                final List<TradingTimeZone> tradingTimeZones) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.email = email;
        this.phone = phone;
        this.tradingTimeZones = List.copyOf(tradingTimeZones);
    }

    // Factory method — preferred way to create a User from the domain perspective
    public static User create(final String name,
                              final Password password,
                              final Email email) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be null or blank");
        }
        return new User(UserID.of(UUID.randomUUID()), name, password, email, null, List.of());
    }

    public static User reconstitute(final UserID id,
                                    final String name,
                                    final Password password,
                                    final Email email,
                                    final Phone phone,
                                    final List<TradingTimeZone> tradingTimeZones) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        return new User(id, name, password, email, phone, tradingTimeZones);
    }
}
