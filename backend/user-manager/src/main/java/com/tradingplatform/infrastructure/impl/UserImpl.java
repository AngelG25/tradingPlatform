package com.tradingplatform.infrastructure.impl;

import com.tradingplatform.domain.model.Email;
import com.tradingplatform.domain.model.User;
import com.tradingplatform.domain.model.UserID;
import com.tradingplatform.domain.model.Password;
import com.tradingplatform.infrastructure.persistence.r2dbc.UserEntity;
import com.tradingplatform.infrastructure.persistence.r2dbc.UserR2dbcRepository;
import com.tradingplatform.domain.model.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class UserImpl implements UserRepository {

    private final UserR2dbcRepository r2dbcRepository;
    private final KeycloakAdapter keycloakAdapter;

    @Override
    public Mono<String> createUser(User user) {
        return keycloakAdapter.createUser(user);
    }

    @Override
    public Mono<User> save(User user) {
        return r2dbcRepository.save(toEntity(user))
                .map(this::toDomain);
    }

    @Override
    public Mono<User> findById(UserID id) {
        return r2dbcRepository.findById(id.value())
                .map(this::toDomain);
    }

    @Override
    public Mono<List<User>> findAll() {
        return r2dbcRepository.findAll()
                .map(this::toDomain)
                .collectList();
    }

    @Override
    public Mono<Boolean> existsById(UserID id) {
        return r2dbcRepository.existsById(id.value());
    }

    @Override
    public Mono<Void> deleteById(UserID id) {
        return r2dbcRepository.deleteById(id.value());
    }

    private UserEntity toEntity(User user) {
        return new UserEntity(user.getId().value(), user.getName(), user.getEmail().value());
    }

    private User toDomain(UserEntity entity) {
        return User.reconstitute(
                UserID.of(entity.getId()),
                entity.getUsername(),
                Password.unsafeReconstitute(null),
                new Email(entity.getEmail()),
                null,
                List.of()
        );
    }
}
