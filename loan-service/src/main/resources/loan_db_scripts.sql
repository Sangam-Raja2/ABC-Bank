-- ============================================================
-- Create Database
-- ============================================================
CREATE DATABASE loandb;

-- Connect to Database
\c loandb;

-- ============================================================
-- Create User
-- ============================================================
CREATE USER sa WITH PASSWORD 'password';

-- Grant Database Privileges
GRANT ALL PRIVILEGES ON DATABASE loandb TO sa;

-- Grant Schema Privileges
GRANT ALL ON SCHEMA public TO sa;
ALTER SCHEMA public OWNER TO sa;

-- ============================================================
-- Loan Table
-- ============================================================
CREATE TABLE loan (
    loan_account_number      VARCHAR(50) PRIMARY KEY,
    owner_username           VARCHAR(100) NOT NULL,
    loan_amount              NUMERIC(19,2) NOT NULL,
    tenure_in_months         INTEGER NOT NULL,
    annual_interest_rate     NUMERIC(5,2) NOT NULL,
    loan_purpose             VARCHAR(255),
    status                   VARCHAR(30) NOT NULL,
    applied_date             TIMESTAMP,
    reviewed_by_name         VARCHAR(100),
    reviewers_role           VARCHAR(50),
    reviewed_date            TIMESTAMP,
    reviewal_remarks         TEXT,
    approved_by_name         VARCHAR(100),
    approvers_role           VARCHAR(50),
    approved_date            TIMESTAMP,
    approval_remarks         TEXT,
    rejected_by_name         VARCHAR(100),
    rejectors_role           VARCHAR(50),
    rejected_date            TIMESTAMP,
    rejected_remarks         TEXT,
    updated_date             TIMESTAMP,
    disbursed_by_name        VARCHAR(100),
    disbursed_by_role        VARCHAR(50),
    disbursed_date           TIMESTAMP
);

-- ============================================================
-- Loan Audit Logs Table
-- ============================================================
CREATE TABLE loan_audit_logs (
    id                   BIGSERIAL PRIMARY KEY,
    loan_account_number  VARCHAR(20) NOT NULL,
    action               VARCHAR(100) NOT NULL,
    performed_by         VARCHAR(100),
    role                 VARCHAR(50),
    action_time          TIMESTAMP NOT NULL,
    remarks              TEXT,
    CONSTRAINT fk_loan_audit_loan
        FOREIGN KEY (loan_account_number)
        REFERENCES loan(loan_account_number)
        ON DELETE CASCADE
);

-- ============================================================
-- Indexes
-- ============================================================

CREATE INDEX idx_loan_owner
ON loan(owner_username);

CREATE INDEX idx_loan_status
ON loan(status);

CREATE INDEX idx_loan_applied_date
ON loan(applied_date);

CREATE INDEX idx_loan_updated_date
ON loan(updated_date);

CREATE INDEX idx_audit_loan_number
ON loan_audit_logs(loan_account_number);

CREATE INDEX idx_audit_action_time
ON loan_audit_logs(action_time);

CREATE INDEX idx_audit_performed_by
ON loan_audit_logs(performed_by);

-- ============================================================
-- Table Permissions
-- ============================================================

GRANT SELECT, INSERT, UPDATE, DELETE
ON TABLE loan TO sa;

GRANT SELECT, INSERT, UPDATE, DELETE
ON TABLE loan_audit_logs TO sa;

GRANT USAGE, SELECT
ON SEQUENCE loan_audit_logs_id_seq TO sa;

ALTER TABLE loan OWNER TO sa;
ALTER TABLE loan_audit_logs OWNER TO sa;
ALTER SEQUENCE loan_audit_logs_id_seq OWNER TO sa;