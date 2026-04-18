package com.tradingplatform.application;

import com.tradingplatform.domain.User;
import com.tradingplatform.domain.Email;
import com.tradingplatform.domain.Password;
import com.tradingplatform.domain.port.KeycloakPort;
import com.tradingplatform.infrastructure.dtos.RegisterRequest;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class RegisterUserUseCase {

    private final KeycloakPort keycloakPort;

    public RegisterUserUseCase(final KeycloakPort keycloakPort) {
        this.keycloakPort = keycloakPort;
    }

    public Mono<String> execute(RegisterRequest request) {
        return Mono.fromCallable(() -> buildUser(request)).flatMap(keycloakPort::createUser).doOnSuccess(v -> log.info("User registered successfully: {}", request.getEmail()));
    }

    private User buildUser(RegisterRequest request) {
        return User.create(request.getUsername(), Password.of(request.getPassword()), Email.of(request.getEmail()));
    }
}
