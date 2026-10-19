-- =====================================================================
-- PAWLINK DATABASE SCHEMA (PostgreSQL)
-- Team Role: Person 1 (P1 - Auth & Core Architecture Lead)
-- Branch: feature/P1-auth-core
-- Note: Only P1 edits db/schema.sql
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1. [P1] USERS TABLE (Core Authentication & Role Management)
-- Roles: OWNER, VET, SITTER, ADMIN
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    phone VARCHAR(20),
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('OWNER', 'VET', 'SITTER', 'ADMIN')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);

-- ---------------------------------------------------------------------
-- 2. [P2] PETS TABLE (Pet Management Vertical)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS pets (
    id SERIAL PRIMARY KEY,
    owner_id INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    species VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    dob DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_pets_owner ON pets(owner_id);

-- ---------------------------------------------------------------------
-- 3. [P3] MEDICAL RECORDS & VACCINATIONS (Medical Vertical)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS medical_records (
    id SERIAL PRIMARY KEY,
    pet_id INT NOT NULL REFERENCES pets(id) ON DELETE CASCADE,
    vet_id INT NOT NULL REFERENCES users(id),
    record_date DATE NOT NULL DEFAULT CURRENT_DATE,
    diagnosis TEXT NOT NULL,
    prescription TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS vaccinations (
    id SERIAL PRIMARY KEY,
    pet_id INT NOT NULL REFERENCES pets(id) ON DELETE CASCADE,
    vaccine_name VARCHAR(100) NOT NULL,
    given_date DATE NOT NULL DEFAULT CURRENT_DATE,
    next_due_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_medical_pet ON medical_records(pet_id);
CREATE INDEX IF NOT EXISTS idx_vaccinations_pet ON vaccinations(pet_id);

-- ---------------------------------------------------------------------
-- 4. [P4] APPOINTMENTS TABLE (Vet Scheduling Vertical)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS appointments (
    id SERIAL PRIMARY KEY,
    vet_id INT NOT NULL REFERENCES users(id),
    pet_id INT NOT NULL REFERENCES pets(id) ON DELETE CASCADE,
    owner_id INT NOT NULL REFERENCES users(id),
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'CONFIRMED', 'COMPLETED', 'CANCELLED')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_appointments_vet_time ON appointments(vet_id, start_time, end_time);
CREATE INDEX IF NOT EXISTS idx_appointments_owner ON appointments(owner_id);

-- ---------------------------------------------------------------------
-- 5. [P5] REMINDERS TABLE (Alerts & Observer Engine)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS reminders (
    id SERIAL PRIMARY KEY,
    pet_id INT NOT NULL REFERENCES pets(id) ON DELETE CASCADE,
    type VARCHAR(50) NOT NULL, -- 'VACCINATION', 'MEDICATION', 'CHECKUP'
    due_date DATE NOT NULL,
    sent BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_reminders_due_sent ON reminders(due_date, sent);

-- ---------------------------------------------------------------------
-- 6. [P6] SITTERS, SITTER BOOKINGS & CHECK-INS (Sitter Vertical)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sitters (
    user_id INT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    hourly_rate NUMERIC(10,2) NOT NULL,
    verified BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS sitter_bookings (
    id SERIAL PRIMARY KEY,
    pet_id INT NOT NULL REFERENCES pets(id) ON DELETE CASCADE,
    sitter_id INT NOT NULL REFERENCES users(id),
    owner_id INT NOT NULL REFERENCES users(id),
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS checkins (
    id SERIAL PRIMARY KEY,
    booking_id INT NOT NULL REFERENCES sitter_bookings(id) ON DELETE CASCADE,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    note TEXT NOT NULL
);

-- ---------------------------------------------------------------------
-- 7. [P7] PAYMENTS TABLE (Payment Processing & Escrow Simulation)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS payments (
    id SERIAL PRIMARY KEY,
    booking_id INT NOT NULL REFERENCES sitter_bookings(id) ON DELETE CASCADE,
    amount NUMERIC(10,2) NOT NULL,
    method VARCHAR(20) NOT NULL CHECK (method IN ('UPI', 'CARD', 'WALLET')),
    status VARCHAR(20) NOT NULL DEFAULT 'HELD' CHECK (status IN ('HELD', 'RELEASED', 'REFUNDED', 'FAILED')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
