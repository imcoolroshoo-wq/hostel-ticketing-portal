-- Spring Boot Data Initialization for Render
-- This file will be executed after Hibernate creates the schema

-- Insert default admin user (password: admin123)
INSERT INTO users (id, username, email, password_hash, first_name, last_name, role, phone, is_active, created_at, updated_at)
VALUES (
    gen_random_uuid(),
    'admin',
    'admin@iimtrichy.ac.in',
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
    'System',
    'Administrator',
    'ADMIN',
    '+91-9876543210',
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (email) DO NOTHING;

-- Insert default student user (password: student123)
INSERT INTO users (id, username, email, password_hash, first_name, last_name, role, student_id, room_number, hostel_block, phone, is_active, created_at, updated_at)
VALUES (
    gen_random_uuid(),
    'student',
    'student@iimtrichy.ac.in',
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
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
INSERT INTO users (id, username, email, password_hash, first_name, last_name, role, phone, is_active, created_at, updated_at)
VALUES (
    gen_random_uuid(),
    'staff',
    'staff@iimtrichy.ac.in',
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
    'Demo',
    'Staff',
    'STAFF',
    '+91-9876543212',
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (email) DO NOTHING;
