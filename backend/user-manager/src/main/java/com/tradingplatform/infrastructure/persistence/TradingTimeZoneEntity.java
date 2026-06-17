package com.tradingplatform.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "trading_time_zones")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TradingTimeZoneEntity {
    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;
}
