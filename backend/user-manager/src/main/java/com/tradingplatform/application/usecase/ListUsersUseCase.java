package com.tradingplatform.application.usecase;

import com.tradingplatform.domain.model.User;
import com.tradingplatform.domain.model.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListUsersUseCase {

    private final UserRepository userRepository;

    public List<User> execute() {
        return userRepository.findAll();
    }
}
