package com.tradingplatform.infrastructure.persistence;

import com.tradingplatform.application.dto.UserResponse;
import com.tradingplatform.domain.model.*;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserEntity toEntity(User domain) {
        if (domain == null) {
            return null;
        }

        UserEntity entity = new UserEntity();
        entity.setId(domain.getId().value());
        entity.setKeycloakId(domain.getKeycloakId());
        entity.setUsername(domain.getName());
        entity.setEmail(domain.getEmail().value());
        entity.setPhone(domain.getPhone() != null ? domain.getPhone().value() : null);
        entity.setTradingTimeZone(domain.getTradingTimeZone());

        return entity;
    }

    public User toDomain(UserEntity entity) {
        if (entity == null) {
            return null;
        }

        return User.reconstitute(
                UserID.of(entity.getId()),
                entity.getKeycloakId(),
                entity.getUsername(),
                null, // Password is not stored in our DB
                Email.of(entity.getEmail()),
                Username.of(entity.getUsername()),
                entity.getPhone() != null ? Phone.of(entity.getPhone()) : null,
                entity.getTradingTimeZone()
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
                .tradingTimeZone(domain.getTradingTimeZone())
                .build();
    }
}
