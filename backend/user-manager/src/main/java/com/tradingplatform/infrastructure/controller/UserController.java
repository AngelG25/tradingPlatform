package com.tradingplatform.infrastructure.controller;

import com.tradingplatform.application.usecase.DeleteUserUseCase;
import com.tradingplatform.application.usecase.GetUserUseCase;
import com.tradingplatform.application.usecase.ListUsersUseCase;
import com.tradingplatform.application.usecase.RegisterUserUseCase;
import com.tradingplatform.application.dto.RegisterRequest;
import com.tradingplatform.application.dto.UserResponse;
import com.tradingplatform.infrastructure.persistence.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final RegisterUserUseCase registerUserUseCase;
    private final GetUserUseCase getUserUseCase;
    private final ListUsersUseCase listUsersUseCase;
    private final DeleteUserUseCase deleteUserUseCase;
    private final UserMapper userMapper;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public String register(@RequestBody RegisterRequest request) {
        return registerUserUseCase.execute(request);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable UUID id) {
        return getUserUseCase.execute(id)
                .map(userMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public List<UserResponse> listUsers() {
        return listUsersUseCase.execute().stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable UUID id) {
        deleteUserUseCase.execute(id);
    }
}