package com.tradingplatform.application.usecase;

import com.tradingplatform.application.dto.UpdateUserRequest;
import com.tradingplatform.domain.model.*;
import com.tradingplatform.domain.model.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateUserUseCase {

    private final UserRepository userRepository;

    @Transactional
    public Optional<User> execute(UUID id, UpdateUserRequest request) {
        return userRepository.findById(UserID.of(id))
                .map(user -> {
                    Username username = request.getUsername() != null ?
                            Username.of(request.getUsername()) :
                            null;
                    Phone phone = request.getPhone() != null ? Phone.of(request.getPhone()) : null;
                    User updatedUser = user.update(username, phone, request.getTradingTimeZone());
                    return userRepository.save(updatedUser);
                });
    }
}
