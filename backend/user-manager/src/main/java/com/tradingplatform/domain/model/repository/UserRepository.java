package com.tradingplatform.domain.model.repository;

import com.tradingplatform.domain.model.User;
import com.tradingplatform.domain.model.UserID;
import reactor.core.publisher.Mono;

import java.util.List;

public interface UserRepository {

    Mono<String> createUser(User user);

    Mono<User> save(User user);

    Mono<User> findById(UserID id);

    Mono<List<User>> findAll();

    Mono<Boolean> existsById(UserID id);

    Mono<Void> deleteById(UserID id);
}
