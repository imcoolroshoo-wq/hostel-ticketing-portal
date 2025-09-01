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
    '$2a$10$X5wFBtLrL/kCcnhWnorNxu6qdQq0waxlVYhKmrHlLx7VbJKtJi8iK',
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
    '$2a$10$Zj5FoQqgznN3PV0HHr7o5eKkdhYFJSLQ.eNF7vJZO4OhwJINEW7uK',
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
    '$2a$10$4KTnAXIjZjEyJQkJ2LmGv.Ke/t1ueJMN1ZJnWnhAZ8MJI5qEXwvEK',
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

-- Additional admin user (password: admin123)
INSERT INTO users (id, username, email, password_hash, first_name, last_name, role, phone, is_active, created_at, updated_at)
VALUES (
    gen_random_uuid(),
    'admin_test',
    'admin.test@iimtrichy.ac.in',
    '$2a$10$X5wFBtLrL/kCcnhWnorNxu6qdQq0waxlVYhKmrHlLx7VbJKtJi8iK',
    'Test',
    'Administrator',
    'ADMIN',
    '+91-9999999997',
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (email) DO NOTHING;

-- Additional student users for testing (password: student123)
INSERT INTO users (id, username, email, password_hash, first_name, last_name, role, student_id, room_number, hostel_block, phone, is_active, created_at, updated_at)
VALUES (
    gen_random_uuid(),
    'student_test1',
    'student002@iimtrichy.ac.in',
    '$2a$10$Zj5FoQqgznN3PV0HHr7o5eKkdhYFJSLQ.eNF7vJZO4OhwJINEW7uK',
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
) ON CONFLICT (email) DO NOTHING;

INSERT INTO users (id, username, email, password_hash, first_name, last_name, role, student_id, room_number, hostel_block, phone, is_active, created_at, updated_at)
VALUES (
    gen_random_uuid(),
    'student_test2', 
    'student003@iimtrichy.ac.in',
    '$2a$10$Zj5FoQqgznN3PV0HHr7o5eKkdhYFJSLQ.eNF7vJZO4OhwJINEW7uK',
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

-- Additional staff users for testing different verticals (password: staff123)
INSERT INTO users (id, username, email, password_hash, first_name, last_name, role, staff_vertical, phone, is_active, created_at, updated_at)
VALUES (
    gen_random_uuid(),
    'staff_plumbing',
    'plumbing@iimtrichy.ac.in',
    '$2a$10$4KTnAXIjZjEyJQkJ2LmGv.Ke/t1ueJMN1ZJnWnhAZ8MJI5qEXwvEK',
    'Demo',
    'Plumber',
    'STAFF',
    'PLUMBING',
    '+91-9999999994',
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (email) DO NOTHING;

INSERT INTO users (id, username, email, password_hash, first_name, last_name, role, staff_vertical, phone, is_active, created_at, updated_at)
VALUES (
    gen_random_uuid(),
    'staff_hvac',
    'hvac@iimtrichy.ac.in',
    '$2a$10$4KTnAXIjZjEyJQkJ2LmGv.Ke/t1ueJMN1ZJnWnhAZ8MJI5qEXwvEK',
    'Demo',
    'HVAC Tech',
    'STAFF',
    'HVAC',
    '+91-9999999993',
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (email) DO NOTHING;

INSERT INTO users (id, username, email, password_hash, first_name, last_name, role, staff_vertical, phone, is_active, created_at, updated_at)
VALUES (
    gen_random_uuid(),
    'staff_general',
    'general@iimtrichy.ac.in',
    '$2a$10$4KTnAXIjZjEyJQkJ2LmGv.Ke/t1ueJMN1ZJnWnhAZ8MJI5qEXwvEK',
    'Demo',
    'General Staff',
    'STAFF',
    'GENERAL_MAINTENANCE',
    '+91-9999999992',
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (email) DO NOTHING;

-- =====================================================
-- CATEGORY STAFF MAPPINGS FOR AUTOMATIC ASSIGNMENT
-- =====================================================
-- These mappings enable automatic ticket assignment based on category and hostel block
-- Priority levels: 1 = highest priority, higher numbers = lower priority
-- Capacity weight: 1.0 = full capacity, lower values = reduced capacity

-- Demo Electrical Staff Mappings
INSERT INTO category_staff_mappings (id, staff_id, hostel_block, category, priority_level, capacity_weight, expertise_level, is_active, created_at, updated_at)
SELECT 
    gen_random_uuid(),
    u.id,
    'BLOCK_A',
    'ELECTRICAL_ISSUES', 
    1,
    1.0,
    5,
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM users u WHERE u.username = 'demo_electrical'
ON CONFLICT (staff_id, hostel_block, category) DO NOTHING;

-- Demo Electrical Staff - All Blocks (fallback)
INSERT INTO category_staff_mappings (id, staff_id, hostel_block, category, priority_level, capacity_weight, expertise_level, is_active, created_at, updated_at)
SELECT 
    gen_random_uuid(),
    u.id,
    NULL,
    'ELECTRICAL_ISSUES', 
    2,
    0.8,
    5,
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM users u WHERE u.username = 'demo_electrical'
ON CONFLICT (staff_id, hostel_block, category) DO NOTHING;

-- Plumbing Staff Mappings  
INSERT INTO category_staff_mappings (id, staff_id, hostel_block, category, priority_level, capacity_weight, expertise_level, is_active, created_at, updated_at)
SELECT 
    gen_random_uuid(),
    u.id,
    'BLOCK_A',
    'PLUMBING_WATER', 
    1,
    1.0,
    4,
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM users u WHERE u.username = 'staff_plumbing'
ON CONFLICT (staff_id, hostel_block, category) DO NOTHING;

INSERT INTO category_staff_mappings (id, staff_id, hostel_block, category, priority_level, capacity_weight, expertise_level, is_active, created_at, updated_at)
SELECT 
    gen_random_uuid(),
    u.id,
    NULL,
    'PLUMBING_WATER', 
    2,
    0.8,
    4,
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM users u WHERE u.username = 'staff_plumbing'
ON CONFLICT (staff_id, hostel_block, category) DO NOTHING;

-- HVAC Staff Mappings
INSERT INTO category_staff_mappings (id, staff_id, hostel_block, category, priority_level, capacity_weight, expertise_level, is_active, created_at, updated_at)
SELECT 
    gen_random_uuid(),
    u.id,
    'BLOCK_B',
    'HVAC', 
    1,
    1.0,
    4,
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM users u WHERE u.username = 'staff_hvac'
ON CONFLICT (staff_id, hostel_block, category) DO NOTHING;

INSERT INTO category_staff_mappings (id, staff_id, hostel_block, category, priority_level, capacity_weight, expertise_level, is_active, created_at, updated_at)
SELECT 
    gen_random_uuid(),
    u.id,
    NULL,
    'HVAC', 
    2,
    0.8,
    4,
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM users u WHERE u.username = 'staff_hvac'
ON CONFLICT (staff_id, hostel_block, category) DO NOTHING;

-- General Staff Mappings (fallback for all categories)
INSERT INTO category_staff_mappings (id, staff_id, hostel_block, category, priority_level, capacity_weight, expertise_level, is_active, created_at, updated_at)
SELECT 
    gen_random_uuid(),
    u.id,
    NULL,
    'GENERAL', 
    3,
    0.6,
    3,
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM users u WHERE u.username = 'staff_general'
ON CONFLICT (staff_id, hostel_block, category) DO NOTHING;

-- Demo Electrical also handles network issues (cross-training)
INSERT INTO category_staff_mappings (id, staff_id, hostel_block, category, priority_level, capacity_weight, expertise_level, is_active, created_at, updated_at)
SELECT 
    gen_random_uuid(),
    u.id,
    NULL,
    'NETWORK_INTERNET', 
    2,
    0.7,
    3,
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM users u WHERE u.username = 'demo_electrical'
ON CONFLICT (staff_id, hostel_block, category) DO NOTHING;

-- Housekeeping mappings
INSERT INTO category_staff_mappings (id, staff_id, hostel_block, category, priority_level, capacity_weight, expertise_level, is_active, created_at, updated_at)
SELECT 
    gen_random_uuid(),
    u.id,
    'BLOCK_A',
    'HOUSEKEEPING_CLEANLINESS', 
    1,
    1.0,
    4,
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM users u WHERE u.username = 'staff_general'
ON CONFLICT (staff_id, hostel_block, category) DO NOTHING;

INSERT INTO category_staff_mappings (id, staff_id, hostel_block, category, priority_level, capacity_weight, expertise_level, is_active, created_at, updated_at)
SELECT 
    gen_random_uuid(),
    u.id,
    NULL,
    'HOUSEKEEPING_CLEANLINESS', 
    2,
    0.8,
    4,
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM users u WHERE u.username = 'staff_general'
ON CONFLICT (staff_id, hostel_block, category) DO NOTHING;
