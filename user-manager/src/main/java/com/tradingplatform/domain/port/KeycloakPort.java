package com.tradingplatform.domain.port;

import com.tradingplatform.domain.User;
import reactor.core.publisher.Mono;

public interface KeycloakPort {
    Mono<String> createUser(User user);
}