package com.tradingplatform.application.usecase;

import com.tradingplatform.domain.model.User;
import com.tradingplatform.domain.model.Email;
import com.tradingplatform.domain.model.Password;
import com.tradingplatform.application.dto.RegisterRequest;
import com.tradingplatform.domain.model.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class RegisterUserUseCase {

    private final UserRepository userRepository;

    public RegisterUserUseCase(final UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Mono<String> execute(RegisterRequest request) {
        return Mono.fromCallable(() -> buildUser(request)).flatMap(userRepository::createUser).doOnSuccess(v -> log.info("User registered successfully: {}", request.getEmail()));
    }

    private User buildUser(RegisterRequest request) {
        return User.create(request.getUsername(), Password.of(request.getPassword()), Email.of(request.getEmail()));
    }
}
