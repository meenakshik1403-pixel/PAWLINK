-- =====================================================================
-- PAWLINK DATABASE SEED DATA
-- Team Role: Person 1 (P1 - Auth & Core Architecture Lead)
-- Branch: feature/P1-auth-core
-- Note: All sample passwords are 'password123' hashed with BCrypt.
-- Hash: $2a$10$zPfq9J9dMrCMLI8E1qc9dOenskj7Nx4OGdPqhkEoEn.fdV3mvld0K
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1. [P1] SEED USERS (At least 2 Vets, 2 Sitters, 1 Admin, 1 Owner)
-- ---------------------------------------------------------------------
INSERT INTO users (name, email, phone, password_hash, role)
VALUES
    -- Veterinarians (Desktop Swing Clients)
    ('Dr. Sreekumar', 'vet.sreekumar@pawlink.com', '+919876543101', '$2a$10$zPfq9J9dMrCMLI8E1qc9dOenskj7Nx4OGdPqhkEoEn.fdV3mvld0K', 'VET'),
    ('Dr. Michael Phillip', 'vet.michael@pawlink.com', '+919876543102', '$2a$10$zPfq9J9dMrCMLI8E1qc9dOenskj7Nx4OGdPqhkEoEn.fdV3mvld0K', 'VET'),

    -- Pet Sitters
    ('Emily Johnson', 'sitter.emily@pawlink.com', '+919876543201', '$2a$10$zPfq9J9dMrCMLI8E1qc9dOenskj7Nx4OGdPqhkEoEn.fdV3mvld0K', 'SITTER'),
    ('Pramod S P', 'sitter.pramod@pawlink.com', '+919876543202', '$2a$10$zPfq9J9dMrCMLI8E1qc9dOenskj7Nx4OGdPqhkEoEn.fdV3mvld0K', 'SITTER'),

    -- System Administrator account is managed via .env / DatabaseSeeder

    -- Pet Owners
    ('John Vargese', 'owner.john@pawlink.com', '+919876543301', '$2a$10$zPfq9J9dMrCMLI8E1qc9dOenskj7Nx4OGdPqhkEoEn.fdV3mvld0K', 'OWNER'),
    ('Janaki Nair', 'owner.janaki@pawlink.com', '+919876543302', '$2a$10$zPfq9J9dMrCMLI8E1qc9dOenskj7Nx4OGdPqhkEoEn.fdV3mvld0K', 'OWNER')
ON CONFLICT (email) DO NOTHING;

-- ---------------------------------------------------------------------
-- 2. [P6] SEED SITTER PROFILES
-- Emily: Verified sitter (₹250/hr), Pramod: Pending approval (₹200/hr)
-- ---------------------------------------------------------------------
INSERT INTO sitters (user_id, hourly_rate, verified)
SELECT id, 250.00, TRUE FROM users WHERE email = 'sitter.emily@pawlink.com'
ON CONFLICT (user_id) DO NOTHING;

INSERT INTO sitters (user_id, hourly_rate, verified)
SELECT id, 200.00, FALSE FROM users WHERE email = 'sitter.pramod@pawlink.com'
ON CONFLICT (user_id) DO NOTHING;

-- ---------------------------------------------------------------------
-- 2b. SEED VET PROFILES (Dr. Sreekumar & Dr. Michael verified)
-- ---------------------------------------------------------------------
INSERT INTO vets (user_id, verified)
SELECT id, TRUE FROM users WHERE role = 'VET'
ON CONFLICT (user_id) DO NOTHING;

-- ---------------------------------------------------------------------
-- 3. [P2] SEED SAMPLE PETS FOR OWNER JOHN VARGESE
-- ---------------------------------------------------------------------
INSERT INTO pets (owner_id, species, name, dob)
SELECT id, 'Dog', 'Bruno', '2021-03-15' FROM users WHERE email = 'owner.john@pawlink.com'
  AND NOT EXISTS (SELECT 1 FROM pets p WHERE p.owner_id = users.id AND LOWER(p.name) = 'bruno');

INSERT INTO pets (owner_id, species, name, dob)
SELECT id, 'Cat', 'Jimmy', '2022-07-20' FROM users WHERE email = 'owner.john@pawlink.com'
  AND NOT EXISTS (SELECT 1 FROM pets p WHERE p.owner_id = users.id AND LOWER(p.name) = 'jimmy');

INSERT INTO pets (owner_id, species, name, dob)
SELECT id, 'Bird', 'Scooby', '2023-01-10' FROM users WHERE email = 'owner.janaki@pawlink.com'
  AND NOT EXISTS (SELECT 1 FROM pets p WHERE p.owner_id = users.id AND LOWER(p.name) = 'scooby');

-- ---------------------------------------------------------------------
-- 4. [P5] SEED SAMPLE REMINDERS (Idempotent: Never insert duplicates)
-- ---------------------------------------------------------------------
INSERT INTO reminders (pet_id, type, due_date, sent)
SELECT id, 'VACCINATION', CURRENT_DATE, FALSE FROM pets
WHERE name = 'Bruno'
  AND NOT EXISTS (
      SELECT 1 FROM reminders r
      WHERE r.pet_id = pets.id AND r.type = 'VACCINATION' AND r.due_date = CURRENT_DATE
  )
LIMIT 1;

INSERT INTO reminders (pet_id, type, due_date, sent)
SELECT id, 'CHECKUP', CURRENT_DATE + INTERVAL '7 days', FALSE FROM pets
WHERE name = 'Jimmy'
  AND NOT EXISTS (
      SELECT 1 FROM reminders r
      WHERE r.pet_id = pets.id AND r.type = 'CHECKUP' AND r.due_date = CURRENT_DATE + INTERVAL '7 days'
  )
LIMIT 1;
