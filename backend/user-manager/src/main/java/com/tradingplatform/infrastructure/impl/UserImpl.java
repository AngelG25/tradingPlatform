package com.tradingplatform.infrastructure.impl;

import com.tradingplatform.domain.model.User;
import com.tradingplatform.domain.model.UserID;
import com.tradingplatform.domain.model.repository.UserRepository;
import com.tradingplatform.infrastructure.persistence.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Slf4j
public class UserImpl implements UserRepository {

    private final UserJpaRepository jpaRepository;
    private final KeycloakAdapter keycloakAdapter;
    private final UserMapper userMapper;

    @Override
    public String createUser(User user) {
        String keycloakId = keycloakAdapter.createUser(user);
        try {
            User userWithKeycloakId = User.reconstitute(
                    user.getId(),
                    UUID.fromString(keycloakId),
                    user.getName(),
                    user.getPassword(),
                    user.getEmail(),
                    user.getUsername(),
                    user.getPhone(),
                    user.getTradingTimeZone()
            );
            jpaRepository.save(userMapper.toEntity(userWithKeycloakId));
            return user.getId().value().toString();
        } catch (Exception e) {
            log.error("Failed to save user in local database after Keycloak creation. Rolling back Keycloak creation for Keycloak ID: {}", keycloakId, e);
            try {
                keycloakAdapter.deleteUser(keycloakId);
            } catch (Exception ex) {
                log.error("Failed to delete user from Keycloak during rollback for Keycloak ID: {}", keycloakId, ex);
            }
            throw e;
        }
    }

    @Override
    public User save(User user) {
        UserEntity entity = userMapper.toEntity(user);
        return userMapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<User> findById(UserID id) {
        return jpaRepository.findById(id.value())
                .map(userMapper::toDomain);
    }

    @Override
    public List<User> findAll() {
        return jpaRepository.findAll()
                .stream()
                .map(userMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsById(UserID id) {
        return jpaRepository.existsById(id.value());
    }

    @Override
    public void deleteById(UserID id) {
        Optional<UserEntity> entity = jpaRepository.findById(id.value());
        entity.ifPresent(userEntity -> {
            if (userEntity.getKeycloakId() != null) {
                keycloakAdapter.deleteUser(userEntity.getKeycloakId().toString());
            }
            jpaRepository.deleteById(userEntity.getId());
        });
    }
}
