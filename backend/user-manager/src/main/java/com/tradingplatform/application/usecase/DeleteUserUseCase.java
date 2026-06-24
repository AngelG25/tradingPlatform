package com.tradingplatform.application.usecase;

import com.tradingplatform.domain.model.UserID;
import com.tradingplatform.domain.model.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeleteUserUseCase {

    private final UserRepository userRepository;

    @Transactional
    public void execute(UUID id) {
        userRepository.deleteById(UserID.of(id));
    }
}
