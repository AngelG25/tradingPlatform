CREATE TABLE user_time_zones (
    user_id UUID NOT NULL,
    time_zone VARCHAR(20) NOT NULL,
    PRIMARY KEY (user_id, time_zone),
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
