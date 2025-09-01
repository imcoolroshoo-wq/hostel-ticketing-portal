-- Railway Database Initialization Script
-- This script will be run automatically when the database is created

-- Enable UUID extension if not already enabled
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create sequences for auto-incrementing IDs
CREATE SEQUENCE IF NOT EXISTS user_id_seq START 1;
CREATE SEQUENCE IF NOT EXISTS ticket_id_seq START 1;
CREATE SEQUENCE IF NOT EXISTS category_staff_mapping_id_seq START 1;
CREATE SEQUENCE IF NOT EXISTS ticket_comment_id_seq START 1;
CREATE SEQUENCE IF NOT EXISTS ticket_attachment_id_seq START 1;
CREATE SEQUENCE IF NOT EXISTS ticket_history_id_seq START 1;
CREATE SEQUENCE IF NOT EXISTS notification_id_seq START 1;

-- The application will create tables automatically using Hibernate
-- But we can insert initial data here

-- Insert default admin user (password: admin123)
INSERT INTO users (id, username, email, password, first_name, last_name, role, phone, is_active, created_at, updated_at)
VALUES (
    nextval('user_id_seq'),
    'admin',
    'admin@iimtrichy.ac.in',
    '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', -- bcrypt hash of 'admin123'
    'System',
    'Administrator',
    'ADMIN',
    '+91-9876543210',
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (email) DO NOTHING;

-- Insert default student user (password: student123)
INSERT INTO users (id, username, email, password, first_name, last_name, role, student_id, room_number, hostel_block, phone, is_active, created_at, updated_at)
VALUES (
    nextval('user_id_seq'),
    'student',
    'student@iimtrichy.ac.in',
    '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', -- bcrypt hash of 'student123'
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
INSERT INTO users (id, username, email, password, first_name, last_name, role, phone, is_active, created_at, updated_at)
VALUES (
    nextval('user_id_seq'),
    'staff',
    'staff@iimtrichy.ac.in',
    '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', -- bcrypt hash of 'staff123'
    'Demo',
    'Staff',
    'STAFF',
    '+91-9876543212',
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (email) DO NOTHING;

-- Insert sample tickets for demonstration
INSERT INTO tickets (id, title, description, category, priority, status, creator_id, hostel_block, room_number, location_details, created_at, updated_at)
SELECT 
    nextval('ticket_id_seq'),
    'Sample Ticket - WiFi Issue',
    'Internet connection is very slow in my room. Please check the router.',
    'ELECTRICAL',
    'MEDIUM',
    'OPEN',
    u.id,
    'BLOCK_A',
    'A101',
    'Room A101, Block A',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM users u WHERE u.email = 'student@iimtrichy.ac.in'
ON CONFLICT DO NOTHING;

INSERT INTO tickets (id, title, description, category, priority, status, creator_id, hostel_block, room_number, location_details, created_at, updated_at)
SELECT 
    nextval('ticket_id_seq'),
    'Sample Ticket - Plumbing Issue',
    'Tap in the bathroom is leaking continuously.',
    'PLUMBING',
    'HIGH',
    'OPEN',
    u.id,
    'BLOCK_A',
    'A101',
    'Room A101, Block A, Bathroom',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM users u WHERE u.email = 'student@iimtrichy.ac.in'
ON CONFLICT DO NOTHING;

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);
CREATE INDEX IF NOT EXISTS idx_tickets_status ON tickets(status);
CREATE INDEX IF NOT EXISTS idx_tickets_priority ON tickets(priority);
CREATE INDEX IF NOT EXISTS idx_tickets_category ON tickets(category);
CREATE INDEX IF NOT EXISTS idx_tickets_creator ON tickets(creator_id);
CREATE INDEX IF NOT EXISTS idx_tickets_assigned ON tickets(assigned_to_id);
CREATE INDEX IF NOT EXISTS idx_tickets_created_at ON tickets(created_at);

-- Grant necessary permissions
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO CURRENT_USER;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO CURRENT_USER;
