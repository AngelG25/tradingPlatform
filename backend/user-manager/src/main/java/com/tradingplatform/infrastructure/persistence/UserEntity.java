package com.tradingplatform.infrastructure.persistence;

import com.tradingplatform.domain.model.TradingTimeZone;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {

    @Id
    private UUID id;

    private UUID keycloakId;

    private String username;

    private String email;

    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "timezone")
    private TradingTimeZone tradingTimeZone;
}
