package com.tradingplatform.infrastructure.impl;

import com.tradingplatform.domain.model.Email;
import com.tradingplatform.domain.model.User;
import com.tradingplatform.domain.model.UserID;
import com.tradingplatform.domain.model.Password;
import com.tradingplatform.infrastructure.persistence.UserEntity;
import com.tradingplatform.infrastructure.persistence.UserJpaRepository;
import com.tradingplatform.domain.model.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserImpl implements UserRepository {

    private final UserJpaRepository jpaRepository;
    private final KeycloakAdapter keycloakAdapter;

    @Override
    public String createUser(User user) {
        return keycloakAdapter.createUser(user);
    }

    @Override
    public User save(User user) {
        return toDomain(jpaRepository.save(toEntity(user)));
    }

    @Override
    public Optional<User> findById(UserID id) {
        return jpaRepository.findById(id.value())
                .map(this::toDomain);
    }

    @Override
    public List<User> findAll() {
        return jpaRepository.findAll()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public boolean existsById(UserID id) {
        return jpaRepository.existsById(id.value());
    }

    @Override
    public void deleteById(UserID id) {
        jpaRepository.deleteById(id.value());
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
