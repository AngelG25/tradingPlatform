package com.tradingplatform.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface TradingTimeZoneJpaRepository extends JpaRepository<TradingTimeZoneEntity, UUID> {
    Optional<TradingTimeZoneEntity> findByName(String name);
}
