package com.tradingplatform.application.dto;

import com.tradingplatform.domain.model.TradingTimeZone;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class UserResponse {
    private UUID id;
    private UUID keycloakId;
    private String username;
    private String email;
    private String phone;
    private List<TradingTimeZone> tradingTimeZones;
}
