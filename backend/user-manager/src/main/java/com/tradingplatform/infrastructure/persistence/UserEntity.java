package com.tradingplatform.infrastructure.persistence;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;

import java.util.List;
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

    @ManyToMany
    @JoinTable(
        name = "user_time_zones_map",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "time_zone_id")
    )
    private List<TradingTimeZoneEntity> tradingTimeZones;
}
