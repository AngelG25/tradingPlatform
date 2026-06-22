package com.tradingplatform.domain.model.repository;

import com.tradingplatform.domain.model.User;
import com.tradingplatform.domain.model.UserID;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

    String createUser(User user);

    User save(User user);

    Optional<User> findById(UserID id);

    List<User> findAll();

    boolean existsById(UserID id);

    void deleteById(UserID id);
}
