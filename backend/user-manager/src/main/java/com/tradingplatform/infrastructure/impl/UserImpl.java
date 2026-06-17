package com.tradingplatform.infrastructure.impl;

import com.tradingplatform.domain.model.TradingTimeZone;
import com.tradingplatform.domain.model.User;
import com.tradingplatform.domain.model.UserID;
import com.tradingplatform.domain.model.repository.UserRepository;
import com.tradingplatform.infrastructure.persistence.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UserImpl implements UserRepository {

    private final UserJpaRepository jpaRepository;
    private final TradingTimeZoneJpaRepository timeZoneRepository;
    private final KeycloakAdapter keycloakAdapter;
    private final UserMapper userMapper;

    @Override
    public String createUser(User user) {
        String keycloakId = keycloakAdapter.createUser(user);
        User userWithKeycloakId = User.reconstitute(
                user.getId(),
                UUID.fromString(keycloakId),
                user.getName(),
                user.getPassword(),
                user.getEmail(),
                user.getPhone(),
                user.getTradingTimeZones()
        );
        jpaRepository.save(userMapper.toEntity(userWithKeycloakId, mapToEntities(user.getTradingTimeZones())));
        return keycloakId;
    }

    @Override
    public User save(User user) {
        UserEntity entity = userMapper.toEntity(user, mapToEntities(user.getTradingTimeZones()));
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

    private List<TradingTimeZoneEntity> mapToEntities(List<TradingTimeZone> timeZones) {
        return timeZones.stream()
                .map(tz -> timeZoneRepository.findByName(tz.name())
                        .orElseThrow(() -> new RuntimeException("Timezone not found: " + tz.name())))
                .collect(java.util.stream.Collectors.toList());
    }
}
