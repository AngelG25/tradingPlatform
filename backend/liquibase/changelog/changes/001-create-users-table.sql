--liquibase formatted sql

--changeset trading-platform:001-create-users-table
CREATE TABLE users (
                       id UUID PRIMARY KEY,
                       username VARCHAR(255) NOT NULL UNIQUE,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       keycloak_id UUID UNIQUE,
                       phone VARCHAR(50),
                       timezone VARCHAR(50)
);