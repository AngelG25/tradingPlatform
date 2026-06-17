CREATE TABLE trading_time_zones (
    id UUID PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE user_time_zones_map (
    user_id UUID NOT NULL,
    time_zone_id UUID NOT NULL,
    PRIMARY KEY (user_id, time_zone_id),
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_timezone FOREIGN KEY (time_zone_id) REFERENCES trading_time_zones(id)
);

-- Initial Master Data
INSERT INTO trading_time_zones (id, name) VALUES (gen_random_uuid(), 'ASIA');
INSERT INTO trading_time_zones (id, name) VALUES (gen_random_uuid(), 'EUROPE');
INSERT INTO trading_time_zones (id, name) VALUES (gen_random_uuid(), 'AMERICA');
