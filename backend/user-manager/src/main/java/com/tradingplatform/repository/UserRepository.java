package com.tradingplatform.repository;

import com.tradingplatform.domain.User;
import com.tradingplatform.domain.UserID;
import reactor.core.publisher.Mono;

import java.util.List;

public interface UserRepository {

    Mono<User> save(User user);

    Mono<User> findById(UserID id);

    Mono<List<User>> findAll();

    Mono<Boolean> existsById(UserID id);

    Mono<Void> deleteById(UserID id);
}
