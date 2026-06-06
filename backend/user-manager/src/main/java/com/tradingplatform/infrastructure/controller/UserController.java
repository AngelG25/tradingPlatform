package com.tradingplatform.infrastructure.controller;

import com.tradingplatform.application.RegisterUserUseCase;
import com.tradingplatform.infrastructure.dtos.RegisterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final RegisterUserUseCase registerUserUseCase;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<String> register(@RequestBody RegisterRequest request) {
        return registerUserUseCase.execute(request);
    }
}