package domain.model;

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
    public User create(final String name,
                       final Password password,
                       final Email email,
                       final Phone phone,
                       final List<TradingTimeZone> tradingTimeZones) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be null or blank");
        }
        if (tradingTimeZones == null || tradingTimeZones.isEmpty()) {
            throw new IllegalArgumentException("At least one trading time zone is required");
        }
        return new User(UserID.of(UUID.randomUUID()), name, password, email, phone, tradingTimeZones);
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
