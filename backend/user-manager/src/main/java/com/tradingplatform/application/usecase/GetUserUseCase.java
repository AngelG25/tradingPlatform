package com.tradingplatform.application.usecase;

import com.tradingplatform.domain.model.User;
import com.tradingplatform.domain.model.UserID;
import com.tradingplatform.domain.model.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetUserUseCase {

    private final UserRepository userRepository;

    public Optional<User> execute(UUID id) {
        return userRepository.findById(UserID.of(id));
    }
}
