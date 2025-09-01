-- Spring Boot Data Initialization for Production
-- This file will be executed after Hibernate creates the schema
-- Note: Tables are dropped and recreated on every restart (ddl-auto: create-drop)

-- Clear any existing data (this happens automatically with create-drop, but being explicit)
-- The ON CONFLICT clauses are kept for safety

-- Insert demo admin user (password: admin123)
INSERT INTO users (id, username, email, password_hash, first_name, last_name, role, phone, is_active, created_at, updated_at)
VALUES (
    gen_random_uuid(),
    'demo_admin',
    'admin@iimtrichy.ac.in',
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
    'Demo',
    'Admin',
    'ADMIN',
    '+91-9999999999',
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (email) DO NOTHING;

-- Insert demo student user (password: student123)
INSERT INTO users (id, username, email, password_hash, first_name, last_name, role, student_id, room_number, hostel_block, phone, is_active, created_at, updated_at)
VALUES (
    gen_random_uuid(),
    'demo_student',
    'student001@iimtrichy.ac.in',
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
    'Demo',
    'Student',
    'STUDENT',
    'STU001',
    'A101',
    'BLOCK_A',
    '+91-9999999998',
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (email) DO NOTHING;

-- Insert demo electrical staff user (password: staff123)
INSERT INTO users (id, username, email, password_hash, first_name, last_name, role, staff_vertical, phone, is_active, created_at, updated_at)
VALUES (
    gen_random_uuid(),
    'demo_electrical',
    'electrical@iimtrichy.ac.in',
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
    'Demo',
    'Electrician',
    'STAFF',
    'ELECTRICAL',
    '+91-9999999998',
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (email) DO NOTHING;

-- Insert additional comprehensive test users for full functionality testing

-- Additional admin user
INSERT INTO users (id, username, email, password_hash, first_name, last_name, role, phone, is_active, created_at, updated_at)
VALUES (
    gen_random_uuid(),
    'admin_test',
    'admin.test@iimtrichy.ac.in',
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
    'Test',
    'Administrator',
    'ADMIN',
    '+91-9999999997',
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (email) DO NOTHING;

-- Additional student users for testing
INSERT INTO users (id, username, email, password_hash, first_name, last_name, role, student_id, room_number, hostel_block, phone, is_active, created_at, updated_at)
VALUES 
(
    gen_random_uuid(),
    'student_test1',
    'student002@iimtrichy.ac.in',
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
    'Test',
    'Student One',
    'STUDENT',
    'STU002',
    'A102',
    'BLOCK_A',
    '+91-9999999996',
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (email) DO NOTHING,
(
    gen_random_uuid(),
    'student_test2', 
    'student003@iimtrichy.ac.in',
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
    'Test',
    'Student Two',
    'STUDENT',
    'STU003',
    'B101',
    'BLOCK_B',
    '+91-9999999995',
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (email) DO NOTHING;

-- Additional staff users for testing different verticals
INSERT INTO users (id, username, email, password_hash, first_name, last_name, role, staff_vertical, phone, is_active, created_at, updated_at)
VALUES
(
    gen_random_uuid(),
    'staff_plumbing',
    'plumbing@iimtrichy.ac.in',
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
    'Demo',
    'Plumber',
    'STAFF',
    'PLUMBING',
    '+91-9999999994',
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (email) DO NOTHING,
(
    gen_random_uuid(),
    'staff_hvac',
    'hvac@iimtrichy.ac.in',
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
    'Demo',
    'HVAC Tech',
    'STAFF',
    'HVAC',
    '+91-9999999993',
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (email) DO NOTHING,
(
    gen_random_uuid(),
    'staff_general',
    'general@iimtrichy.ac.in',
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
    'Demo',
    'General Staff',
    'STAFF',
    'GENERAL',
    '+91-9999999992',
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (email) DO NOTHING;
