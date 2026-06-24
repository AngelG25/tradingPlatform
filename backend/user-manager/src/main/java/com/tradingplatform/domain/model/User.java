package com.tradingplatform.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jmolecules.ddd.annotation.AggregateRoot;

import java.util.UUID;

@AggregateRoot
@Getter
@EqualsAndHashCode(of = "id")
public class User {

    private final UserID id;
    private final UUID keycloakId;
    private final String name;
    private final Password password;
    private final Email email;
    private final Username username;
    private final Phone phone;
    private final TradingTimeZone tradingTimeZone;

    // Package-private constructor for persistence / ORM
    public User(final UserID id,
                final UUID keycloakId,
                final String name,
                final Password password,
                final Email email,
                Username username,
                final Phone phone,
                final TradingTimeZone tradingTimeZone) {
        this.id = id;
        this.keycloakId = keycloakId;
        this.name = name;
        this.password = password;
        this.email = email;
        this.username = username;
        this.phone = phone;
        this.tradingTimeZone = tradingTimeZone;
    }

    // Factory method — preferred way to create a User from the domain perspective
    public static User create(final String name,
                              final Password password,
                              final Email email) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be null or blank");
        }
        return new User(UserID.of(UUID.randomUUID()), null, name, password, email, null, null,
                null);
    }

    public static User reconstitute(final UserID id,
                                    final UUID keycloakId,
                                    final String name,
                                    final Password password,
                                    final Email email,
                                    final Username username,
                                    final Phone phone,
                                    final TradingTimeZone tradingTimeZone) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        return new User(id, keycloakId, name, password, email, username, phone, tradingTimeZone);
    }

    // Update method to return a new User with updated profile information
    public User update(final Username username, final Phone phone,
                       final TradingTimeZone tradingTimeZone) {
        return new User(
                this.id,
                this.keycloakId,
                this.name,
                this.password,
                this.email,
                username != null ? username : this.username,
                phone != null ? phone : this.phone,
                tradingTimeZone != null ? tradingTimeZone : this.tradingTimeZone
        );
    }
}
