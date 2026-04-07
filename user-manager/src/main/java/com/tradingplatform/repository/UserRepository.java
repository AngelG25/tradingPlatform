package com.tradingplatform.repository;

import com.tradingplatform.domain.User;
import com.tradingplatform.domain.UserID;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

    User save(User user);

    Optional<User> findById(UserID id);

    List<User> findAll();

    boolean existsById(UserID id);

    void deleteById(UserID id);
}
