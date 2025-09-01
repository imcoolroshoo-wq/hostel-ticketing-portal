-- Spring Boot Data Initialization for Render
-- This file will be executed after Hibernate creates the schema

-- Insert default admin user (password: admin123)
INSERT INTO users (username, email, password_hash, first_name, last_name, role, phone, is_active, created_at, updated_at)
VALUES (
    'admin',
    'admin@iimtrichy.ac.in',
    '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a',
    'System',
    'Administrator',
    'ADMIN',
    '+91-9876543210',
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (email) DO NOTHING;

-- Insert default student user (password: student123)
INSERT INTO users (username, email, password_hash, first_name, last_name, role, student_id, room_number, hostel_block, phone, is_active, created_at, updated_at)
VALUES (
    'student',
    'student@iimtrichy.ac.in',
    '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a',
    'Demo',
    'Student',
    'STUDENT',
    'STU001',
    'A101',
    'BLOCK_A',
    '+91-9876543211',
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (email) DO NOTHING;

-- Insert default staff user (password: staff123)
INSERT INTO users (username, email, password_hash, first_name, last_name, role, phone, is_active, created_at, updated_at)
VALUES (
    'staff',
    'staff@iimtrichy.ac.in',
    '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a',
    'Demo',
    'Staff',
    'STAFF',
    '+91-9876543212',
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (email) DO NOTHING;
