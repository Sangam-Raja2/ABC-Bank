-- Create Database
CREATE DATABASE bankingdb;

-- Connect to the database
\c bankingdb;

-- Create User
CREATE USER sa WITH PASSWORD 'password';

-- Grant Privileges
GRANT ALL PRIVILEGES ON DATABASE userdb TO sa;

-- Allow schema access
GRANT ALL ON SCHEMA public TO sa;

ALTER SCHEMA public OWNER TO sa;

CREATE TABLE accounts (
    id BIGSERIAL PRIMARY KEY,
    account_number VARCHAR(20) NOT NULL UNIQUE,
    owner_username VARCHAR(255) NOT NULL,
    account_holder_name VARCHAR(255) NOT NULL,
    account_type VARCHAR(255) NOT NULL,
    balance NUMERIC(19,2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE TABLE transactions (
    id BIGSERIAL PRIMARY KEY,
    account_number VARCHAR(20) NOT NULL,
    type VARCHAR(50) NOT NULL,
    amount NUMERIC(19,2) NOT NULL,
    balance_after NUMERIC(19,2) NOT NULL,
    performed_by VARCHAR(255),
    timestamp TIMESTAMP NOT NULL
);