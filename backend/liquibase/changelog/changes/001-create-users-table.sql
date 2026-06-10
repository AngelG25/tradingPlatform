--liquibase formatted sql

--changeset trading-platform:001-create-users-table
--comment: Tabla users inicial. Refleja el estado de user-manager/src/main/resources/db/migration/V1__create_users_table.sql.
CREATE TABLE users (
    id UUID PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE
);

--rollback DROP TABLE users;
