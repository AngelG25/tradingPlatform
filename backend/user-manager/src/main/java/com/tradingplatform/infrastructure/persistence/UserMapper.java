package com.tradingplatform.infrastructure.persistence;

import com.tradingplatform.application.dto.UserResponse;
import com.tradingplatform.domain.model.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserMapper {

    public UserEntity toEntity(User domain, List<TradingTimeZoneEntity> timeZoneEntities) {
        if (domain == null) {
            return null;
        }

        UserEntity entity = new UserEntity();
        entity.setId(domain.getId().value());
        entity.setKeycloakId(domain.getKeycloakId());
        entity.setUsername(domain.getName());
        entity.setEmail(domain.getEmail().value());
        entity.setPhone(domain.getPhone() != null ? domain.getPhone().value() : null);
        entity.setTradingTimeZones(timeZoneEntities);

        return entity;
    }

    public User toDomain(UserEntity entity) {
        if (entity == null) {
            return null;
        }

        List<TradingTimeZone> domainTimeZones = entity.getTradingTimeZones().stream()
                .map(tz -> TradingTimeZone.valueOf(tz.getName()))
                .collect(Collectors.toList());

        return User.reconstitute(
                UserID.of(entity.getId()),
                entity.getKeycloakId(),
                entity.getUsername(),
                null, // Password is not stored in our DB
                Email.of(entity.getEmail()),
                entity.getPhone() != null ? Phone.of(entity.getPhone()) : null,
                domainTimeZones
        );
    }

    public UserResponse toResponse(User domain) {
        if (domain == null) {
            return null;
        }

        return UserResponse.builder()
                .id(domain.getId().value())
                .keycloakId(domain.getKeycloakId())
                .username(domain.getName())
                .email(domain.getEmail().value())
                .phone(domain.getPhone() != null ? domain.getPhone().value() : null)
                .tradingTimeZones(domain.getTradingTimeZones())
                .build();
    }
}
