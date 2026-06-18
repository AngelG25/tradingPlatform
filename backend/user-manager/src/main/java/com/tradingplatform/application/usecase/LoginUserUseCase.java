package com.tradingplatform.application.usecase;

import com.tradingplatform.application.dto.LoginRequest;
import com.tradingplatform.domain.model.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class LoginUserUseCase {

    private final UserRepository userRepository;

    public Map<String, Object> execute(LoginRequest request) {
        return userRepository.login(request.getUsername(), request.getPassword());
    }
}
