-- =====================================================
-- IIM Trichy Hostel Ticketing System - Database Reset
-- =====================================================
-- This script deletes all existing data and creates fresh, comprehensive test data
-- based on the current code structure and enums

-- =====================================================
-- 1. DELETE ALL EXISTING DATA (in correct order to handle foreign keys)
-- =====================================================

-- Delete dependent data first
DELETE FROM ticket_attachments;
DELETE FROM ticket_comments;
DELETE FROM ticket_history;
DELETE FROM ticket_escalations;
DELETE FROM category_staff_mappings;
DELETE FROM tickets;
DELETE FROM notifications;
DELETE FROM asset_movements;
DELETE FROM assets;
DELETE FROM building_maintenance_schedule;
DELETE FROM maintenance_schedules;
DELETE FROM hostel_blocks;
DELETE FROM users;

-- Reset sequences if they exist
-- ALTER SEQUENCE IF EXISTS users_id_seq RESTART WITH 1;

-- =====================================================
-- 2. INSERT FRESH USER DATA
-- =====================================================

-- ADMIN USERS (3 admins)
INSERT INTO users (id, username, email, password_hash, first_name, last_name, role, staff_id, staff_vertical, phone, is_active, created_at, updated_at) VALUES
('550e8400-e29b-41d4-a716-446655440000', 'admin_director', 'director@iimtrichy.ac.in', '$2a$10$N.zmdr9k7uOCQb0bgjam.OOzVAWUK94srEWt8j4X3vyuan.2LhqHu', 'Dr. Rajesh', 'Kumar', 'ADMIN', 'ADM001', 'ADMIN_STAFF', '+91-9876543210', true, NOW(), NOW()),
('550e8400-e29b-41d4-a716-446655440001', 'admin_hostel', 'hostel.admin@iimtrichy.ac.in', '$2a$10$N.zmdr9k7uOCQb0bgjam.OOzVAWUK94srEWt8j4X3vyuan.2LhqHu', 'Priya', 'Sharma', 'ADMIN', 'ADM002', 'ADMIN_STAFF', '+91-9876543211', true, NOW(), NOW()),
('550e8400-e29b-41d4-a716-446655440002', 'admin_maintenance', 'maintenance.head@iimtrichy.ac.in', '$2a$10$N.zmdr9k7uOCQb0bgjam.OOzVAWUK94srEWt8j4X3vyuan.2LhqHu', 'Suresh', 'Patel', 'ADMIN', 'ADM003', 'ADMIN_STAFF', '+91-9876543212', true, NOW(), NOW());

-- STAFF USERS (20 staff members across different verticals)
INSERT INTO users (id, username, email, password_hash, first_name, last_name, role, staff_id, staff_vertical, phone, is_active, created_at, updated_at) VALUES
-- Electrical Staff
('550e8400-e29b-41d4-a716-446655440010', 'electrician_ravi', 'ravi.electrical@iimtrichy.ac.in', '$2a$10$N.zmdr9k7uOCQb0bgjam.OOzVAWUK94srEWt8j4X3vyuan.2LhqHu', 'Ravi', 'Krishnan', 'STAFF', 'ELE001', 'ELECTRICAL', '+91-9876543220', true, NOW(), NOW()),
('550e8400-e29b-41d4-a716-446655440011', 'electrician_kumar', 'kumar.electrical@iimtrichy.ac.in', '$2a$10$N.zmdr9k7uOCQb0bgjam.OOzVAWUK94srEWt8j4X3vyuan.2LhqHu', 'Kumar', 'Venkatesh', 'STAFF', 'ELE002', 'ELECTRICAL', '+91-9876543221', true, NOW(), NOW()),

-- Plumbing Staff
('550e8400-e29b-41d4-a716-446655440012', 'plumber_mohan', 'mohan.plumbing@iimtrichy.ac.in', '$2a$10$N.zmdr9k7uOCQb0bgjam.OOzVAWUK94srEWt8j4X3vyuan.2LhqHu', 'Mohan', 'Das', 'STAFF', 'PLB001', 'PLUMBING', '+91-9876543222', true, NOW(), NOW()),
('550e8400-e29b-41d4-a716-446655440013', 'plumber_raja', 'raja.plumbing@iimtrichy.ac.in', '$2a$10$N.zmdr9k7uOCQb0bgjam.OOzVAWUK94srEWt8j4X3vyuan.2LhqHu', 'Raja', 'Murugan', 'STAFF', 'PLB002', 'PLUMBING', '+91-9876543223', true, NOW(), NOW()),

-- HVAC Staff
('550e8400-e29b-41d4-a716-446655440014', 'hvac_arun', 'arun.hvac@iimtrichy.ac.in', '$2a$10$N.zmdr9k7uOCQb0bgjam.OOzVAWUK94srEWt8j4X3vyuan.2LhqHu', 'Arun', 'Selvam', 'STAFF', 'HVC001', 'HVAC', '+91-9876543224', true, NOW(), NOW()),
('550e8400-e29b-41d4-a716-446655440015', 'hvac_ganesh', 'ganesh.hvac@iimtrichy.ac.in', '$2a$10$N.zmdr9k7uOCQb0bgjam.OOzVAWUK94srEWt8j4X3vyuan.2LhqHu', 'Ganesh', 'Kumar', 'STAFF', 'HVC002', 'HVAC', '+91-9876543225', true, NOW(), NOW()),

-- IT Support Staff
('550e8400-e29b-41d4-a716-446655440016', 'it_support_anand', 'anand.it@iimtrichy.ac.in', '$2a$10$N.zmdr9k7uOCQb0bgjam.OOzVAWUK94srEWt8j4X3vyuan.2LhqHu', 'Anand', 'Raj', 'STAFF', 'ITS001', 'IT_SUPPORT', '+91-9876543226', true, NOW(), NOW()),
('550e8400-e29b-41d4-a716-446655440017', 'it_support_deepak', 'deepak.it@iimtrichy.ac.in', '$2a$10$N.zmdr9k7uOCQb0bgjam.OOzVAWUK94srEWt8j4X3vyuan.2LhqHu', 'Deepak', 'Singh', 'STAFF', 'ITS002', 'IT_SUPPORT', '+91-9876543227', true, NOW(), NOW()),

-- Housekeeping Staff
('550e8400-e29b-41d4-a716-446655440018', 'housekeeping_lakshmi', 'lakshmi.housekeeping@iimtrichy.ac.in', '$2a$10$N.zmdr9k7uOCQb0bgjam.OOzVAWUK94srEWt8j4X3vyuan.2LhqHu', 'Lakshmi', 'Devi', 'STAFF', 'HKP001', 'HOUSEKEEPING', '+91-9876543228', true, NOW(), NOW()),
('550e8400-e29b-41d4-a716-446655440019', 'housekeeping_kamala', 'kamala.housekeeping@iimtrichy.ac.in', '$2a$10$N.zmdr9k7uOCQb0bgjam.OOzVAWUK94srEWt8j4X3vyuan.2LhqHu', 'Kamala', 'Bai', 'STAFF', 'HKP002', 'HOUSEKEEPING', '+91-9876543229', true, NOW(), NOW()),

-- Security Staff
('550e8400-e29b-41d4-a716-446655440020', 'security_murugan', 'murugan.security@iimtrichy.ac.in', '$2a$10$N.zmdr9k7uOCQb0bgjam.OOzVAWUK94srEWt8j4X3vyuan.2LhqHu', 'Murugan', 'S', 'STAFF', 'SEC001', 'SECURITY_OFFICER', '+91-9876543230', true, NOW(), NOW()),
('550e8400-e29b-41d4-a716-446655440021', 'security_ramesh', 'ramesh.security@iimtrichy.ac.in', '$2a$10$N.zmdr9k7uOCQb0bgjam.OOzVAWUK94srEWt8j4X3vyuan.2LhqHu', 'Ramesh', 'Babu', 'STAFF', 'SEC002', 'SECURITY_OFFICER', '+91-9876543231', true, NOW(), NOW()),

-- Hostel Wardens
('550e8400-e29b-41d4-a716-446655440022', 'warden_male', 'warden.male@iimtrichy.ac.in', '$2a$10$N.zmdr9k7uOCQb0bgjam.OOzVAWUK94srEWt8j4X3vyuan.2LhqHu', 'Dr. Vikram', 'Reddy', 'STAFF', 'WRD001', 'HOSTEL_WARDEN', '+91-9876543232', true, NOW(), NOW()),
('550e8400-e29b-41d4-a716-446655440023', 'warden_female', 'warden.female@iimtrichy.ac.in', '$2a$10$N.zmdr9k7uOCQb0bgjam.OOzVAWUK94srEWt8j4X3vyuan.2LhqHu', 'Dr. Meera', 'Nair', 'STAFF', 'WRD002', 'HOSTEL_WARDEN', '+91-9876543233', true, NOW(), NOW()),

-- General Maintenance
('550e8400-e29b-41d4-a716-446655440024', 'maintenance_selvam', 'selvam.maintenance@iimtrichy.ac.in', '$2a$10$N.zmdr9k7uOCQb0bgjam.OOzVAWUK94srEWt8j4X3vyuan.2LhqHu', 'Selvam', 'K', 'STAFF', 'MNT001', 'GENERAL_MAINTENANCE', '+91-9876543234', true, NOW(), NOW()),
('550e8400-e29b-41d4-a716-446655440025', 'maintenance_balu', 'balu.maintenance@iimtrichy.ac.in', '$2a$10$N.zmdr9k7uOCQb0bgjam.OOzVAWUK94srEWt8j4X3vyuan.2LhqHu', 'Balu', 'Raman', 'STAFF', 'MNT002', 'GENERAL_MAINTENANCE', '+91-9876543235', true, NOW(), NOW()),

-- Carpentry Staff
('550e8400-e29b-41d4-a716-446655440026', 'carpenter_krishna', 'krishna.carpenter@iimtrichy.ac.in', '$2a$10$N.zmdr9k7uOCQb0bgjam.OOzVAWUK94srEWt8j4X3vyuan.2LhqHu', 'Krishna', 'Moorthy', 'STAFF', 'CRP001', 'CARPENTRY', '+91-9876543236', true, NOW(), NOW()),

-- Network Admin
('550e8400-e29b-41d4-a716-446655440027', 'network_admin', 'network.admin@iimtrichy.ac.in', '$2a$10$N.zmdr9k7uOCQb0bgjam.OOzVAWUK94srEWt8j4X3vyuan.2LhqHu', 'Rajesh', 'Kumar', 'STAFF', 'NET001', 'NETWORK_ADMIN', '+91-9876543237', true, NOW(), NOW()),

-- Block Supervisors
('550e8400-e29b-41d4-a716-446655440028', 'supervisor_male', 'supervisor.male@iimtrichy.ac.in', '$2a$10$N.zmdr9k7uOCQb0bgjam.OOzVAWUK94srEWt8j4X3vyuan.2LhqHu', 'Arjun', 'Prasad', 'STAFF', 'SUP001', 'BLOCK_SUPERVISOR', '+91-9876543238', true, NOW(), NOW()),
('550e8400-e29b-41d4-a716-446655440029', 'supervisor_female', 'supervisor.female@iimtrichy.ac.in', '$2a$10$N.zmdr9k7uOCQb0bgjam.OOzVAWUK94srEWt8j4X3vyuan.2LhqHu', 'Sita', 'Devi', 'STAFF', 'SUP002', 'BLOCK_SUPERVISOR', '+91-9876543239', true, NOW(), NOW());

-- STUDENT USERS (40 students across different blocks)
INSERT INTO users (id, username, email, password_hash, first_name, last_name, role, student_id, room_number, hostel_block, phone, is_active, created_at, updated_at) VALUES
-- Block A Students (Male)
('550e8400-e29b-41d4-a716-446655440100', 'student_aarav', 'aarav.sharma@student.iimtrichy.ac.in', '$2a$10$N.zmdr9k7uOCQb0bgjam.OOzVAWUK94srEWt8j4X3vyuan.2LhqHu', 'Aarav', 'Sharma', 'STUDENT', 'PGP24001', 'A101', 'Block A', '+91-9876543301', true, NOW(), NOW()),
('550e8400-e29b-41d4-a716-446655440101', 'student_arjun', 'arjun.patel@student.iimtrichy.ac.in', '$2a$10$N.zmdr9k7uOCQb0bgjam.OOzVAWUK94srEWt8j4X3vyuan.2LhqHu', 'Arjun', 'Patel', 'STUDENT', 'PGP24002', 'A102', 'Block A', '+91-9876543302', true, NOW(), NOW()),
('550e8400-e29b-41d4-a716-446655440102', 'student_karan', 'karan.singh@student.iimtrichy.ac.in', '$2a$10$N.zmdr9k7uOCQb0bgjam.OOzVAWUK94srEWt8j4X3vyuan.2LhqHu', 'Karan', 'Singh', 'STUDENT', 'PGP24003', 'A103', 'Block A', '+91-9876543303', true, NOW(), NOW()),
('550e8400-e29b-41d4-a716-446655440103', 'student_rohit', 'rohit.kumar@student.iimtrichy.ac.in', '$2a$10$N.zmdr9k7uOCQb0bgjam.OOzVAWUK94srEWt8j4X3vyuan.2LhqHu', 'Rohit', 'Kumar', 'STUDENT', 'PGP24004', 'A104', 'Block A', '+91-9876543304', true, NOW(), NOW()),
('550e8400-e29b-41d4-a716-446655440104', 'student_vikash', 'vikash.gupta@student.iimtrichy.ac.in', '$2a$10$N.zmdr9k7uOCQb0bgjam.OOzVAWUK94srEWt8j4X3vyuan.2LhqHu', 'Vikash', 'Gupta', 'STUDENT', 'PGP24005', 'A105', 'Block A', '+91-9876543305', true, NOW(), NOW()),

-- Block B Students (Male)
('550e8400-e29b-41d4-a716-446655440105', 'student_amit', 'amit.verma@student.iimtrichy.ac.in', '$2a$10$N.zmdr9k7uOCQb0bgjam.OOzVAWUK94srEWt8j4X3vyuan.2LhqHu', 'Amit', 'Verma', 'STUDENT', 'PGP24006', 'B101', 'Block B', '+91-9876543306', true, NOW(), NOW()),
('550e8400-e29b-41d4-a716-446655440106', 'student_deepak', 'deepak.jain@student.iimtrichy.ac.in', '$2a$10$N.zmdr9k7uOCQb0bgjam.OOzVAWUK94srEWt8j4X3vyuan.2LhqHu', 'Deepak', 'Jain', 'STUDENT', 'PGP24007', 'B102', 'Block B', '+91-9876543307', true, NOW(), NOW()),
('550e8400-e29b-41d4-a716-446655440107', 'student_rahul', 'rahul.agarwal@student.iimtrichy.ac.in', '$2a$10$N.zmdr9k7uOCQb0bgjam.OOzVAWUK94srEWt8j4X3vyuan.2LhqHu', 'Rahul', 'Agarwal', 'STUDENT', 'PGP24008', 'B103', 'Block B', '+91-9876543308', true, NOW(), NOW()),
('550e8400-e29b-41d4-a716-446655440108', 'student_sanjay', 'sanjay.mehta@student.iimtrichy.ac.in', '$2a$10$N.zmdr9k7uOCQb0bgjam.OOzVAWUK94srEWt8j4X3vyuan.2LhqHu', 'Sanjay', 'Mehta', 'STUDENT', 'PGP24009', 'B104', 'Block B', '+91-9876543309', true, NOW(), NOW()),
('550e8400-e29b-41d4-a716-446655440109', 'student_manish', 'manish.shah@student.iimtrichy.ac.in', '$2a$10$N.zmdr9k7uOCQb0bgjam.OOzVAWUK94srEWt8j4X3vyuan.2LhqHu', 'Manish', 'Shah', 'STUDENT', 'PGP24010', 'B105', 'Block B', '+91-9876543310', true, NOW(), NOW()),

-- Block C Students (Male)
('550e8400-e29b-41d4-a716-446655440110', 'student_suresh', 'suresh.reddy@student.iimtrichy.ac.in', '$2a$10$N.zmdr9k7uOCQb0bgjam.OOzVAWUK94srEWt8j4X3vyuan.2LhqHu', 'Suresh', 'Reddy', 'STUDENT', 'PGP24011', 'C101', 'Block C', '+91-9876543311', true, NOW(), NOW()),
('550e8400-e29b-41d4-a716-446655440111', 'student_naveen', 'naveen.kumar@student.iimtrichy.ac.in', '$2a$10$N.zmdr9k7uOCQb0bgjam.OOzVAWUK94srEWt8j4X3vyuan.2LhqHu', 'Naveen', 'Kumar', 'STUDENT', 'PGP24012', 'C102', 'Block C', '+91-9876543312', true, NOW(), NOW()),
('550e8400-e29b-41d4-a716-446655440112', 'student_ravi', 'ravi.nair@student.iimtrichy.ac.in', '$2a$10$N.zmdr9k7uOCQb0bgjam.OOzVAWUK94srEWt8j4X3vyuan.2LhqHu', 'Ravi', 'Nair', 'STUDENT', 'PGP24013', 'C103', 'Block C', '+91-9876543313', true, NOW(), NOW()),
('550e8400-e29b-41d4-a716-446655440113', 'student_anil', 'anil.yadav@student.iimtrichy.ac.in', '$2a$10$N.zmdr9k7uOCQb0bgjam.OOzVAWUK94srEWt8j4X3vyuan.2LhqHu', 'Anil', 'Yadav', 'STUDENT', 'PGP24014', 'C104', 'Block C', '+91-9876543314', true, NOW(), NOW()),
('550e8400-e29b-41d4-a716-446655440114', 'student_pradeep', 'pradeep.singh@student.iimtrichy.ac.in', '$2a$10$N.zmdr9k7uOCQb0bgjam.OOzVAWUK94srEWt8j4X3vyuan.2LhqHu', 'Pradeep', 'Singh', 'STUDENT', 'PGP24015', 'C105', 'Block C', '+91-9876543315', true, NOW(), NOW()),

-- Block D Students (Male)
('550e8400-e29b-41d4-a716-446655440115', 'student_ajay', 'ajay.sharma@student.iimtrichy.ac.in', '$2a$10$N.zmdr9k7uOCQb0bgjam.OOzVAWUK94srEWt8j4X3vyuan.2LhqHu', 'Ajay', 'Sharma', 'STUDENT', 'PGP24016', 'D101', 'Block D', '+91-9876543316', true, NOW(), NOW()),
('550e8400-e29b-41d4-a716-446655440116', 'student_vinay', 'vinay.kumar@student.iimtrichy.ac.in', '$2a$10$N.zmdr9k7uOCQb0bgjam.OOzVAWUK94srEWt8j4X3vyuan.2LhqHu', 'Vinay', 'Kumar', 'STUDENT', 'PGP24017', 'D102', 'Block D', '+91-9876543317', true, NOW(), NOW()),
('550e8400-e29b-41d4-a716-446655440117', 'student_sachin', 'sachin.gupta@student.iimtrichy.ac.in', '$2a$10$N.zmdr9k7uOCQb0bgjam.OOzVAWUK94srEWt8j4X3vyuan.2LhqHu', 'Sachin', 'Gupta', 'STUDENT', 'PGP24018', 'D103', 'Block D', '+91-9876543318', true, NOW(), NOW()),
('550e8400-e29b-41d4-a716-446655440118', 'student_manoj', 'manoj.patel@student.iimtrichy.ac.in', '$2a$10$N.zmdr9k7uOCQb0bgjam.OOzVAWUK94srEWt8j4X3vyuan.2LhqHu', 'Manoj', 'Patel', 'STUDENT', 'PGP24019', 'D104', 'Block D', '+91-9876543319', true, NOW(), NOW()),
('550e8400-e29b-41d4-a716-446655440119', 'student_ashok', 'ashok.verma@student.iimtrichy.ac.in', '$2a$10$N.zmdr9k7uOCQb0bgjam.OOzVAWUK94srEWt8j4X3vyuan.2LhqHu', 'Ashok', 'Verma', 'STUDENT', 'PGP24020', 'D105', 'Block D', '+91-9876543320', true, NOW(), NOW()),

-- Block G Students (Female)
('550e8400-e29b-41d4-a716-446655440120', 'student_priya', 'priya.sharma@student.iimtrichy.ac.in', '$2a$10$N.zmdr9k7uOCQb0bgjam.OOzVAWUK94srEWt8j4X3vyuan.2LhqHu', 'Priya', 'Sharma', 'STUDENT', 'PGP24021', 'G101', 'Block G', '+91-9876543321', true, NOW(), NOW()),
('550e8400-e29b-41d4-a716-446655440121', 'student_anita', 'anita.patel@student.iimtrichy.ac.in', '$2a$10$N.zmdr9k7uOCQb0bgjam.OOzVAWUK94srEWt8j4X3vyuan.2LhqHu', 'Anita', 'Patel', 'STUDENT', 'PGP24022', 'G102', 'Block G', '+91-9876543322', true, NOW(), NOW()),
('550e8400-e29b-41d4-a716-446655440122', 'student_kavya', 'kavya.singh@student.iimtrichy.ac.in', '$2a$10$N.zmdr9k7uOCQb0bgjam.OOzVAWUK94srEWt8j4X3vyuan.2LhqHu', 'Kavya', 'Singh', 'STUDENT', 'PGP24023', 'G103', 'Block G', '+91-9876543323', true, NOW(), NOW()),
('550e8400-e29b-41d4-a716-446655440123', 'student_sneha', 'sneha.kumar@student.iimtrichy.ac.in', '$2a$10$N.zmdr9k7uOCQb0bgjam.OOzVAWUK94srEWt8j4X3vyuan.2LhqHu', 'Sneha', 'Kumar', 'STUDENT', 'PGP24024', 'G104', 'Block G', '+91-9876543324', true, NOW(), NOW()),
('550e8400-e29b-41d4-a716-446655440124', 'student_meera', 'meera.gupta@student.iimtrichy.ac.in', '$2a$10$N.zmdr9k7uOCQb0bgjam.OOzVAWUK94srEWt8j4X3vyuan.2LhqHu', 'Meera', 'Gupta', 'STUDENT', 'PGP24025', 'G105', 'Block G', '+91-9876543325', true, NOW(), NOW()),

-- Block H Students (Female)
('550e8400-e29b-41d4-a716-446655440125', 'student_pooja', 'pooja.verma@student.iimtrichy.ac.in', '$2a$10$N.zmdr9k7uOCQb0bgjam.OOzVAWUK94srEWt8j4X3vyuan.2LhqHu', 'Pooja', 'Verma', 'STUDENT', 'PGP24026', 'H101', 'Block H', '+91-9876543326', true, NOW(), NOW()),
('550e8400-e29b-41d4-a716-446655440126', 'student_ritu', 'ritu.jain@student.iimtrichy.ac.in', '$2a$10$N.zmdr9k7uOCQb0bgjam.OOzVAWUK94srEWt8j4X3vyuan.2LhqHu', 'Ritu', 'Jain', 'STUDENT', 'PGP24027', 'H102', 'Block H', '+91-9876543327', true, NOW(), NOW()),
('550e8400-e29b-41d4-a716-446655440127', 'student_sonia', 'sonia.agarwal@student.iimtrichy.ac.in', '$2a$10$N.zmdr9k7uOCQb0bgjam.OOzVAWUK94srEWt8j4X3vyuan.2LhqHu', 'Sonia', 'Agarwal', 'STUDENT', 'PGP24028', 'H103', 'Block H', '+91-9876543328', true, NOW(), NOW()),
('550e8400-e29b-41d4-a716-446655440128', 'student_nisha', 'nisha.mehta@student.iimtrichy.ac.in', '$2a$10$N.zmdr9k7uOCQb0bgjam.OOzVAWUK94srEWt8j4X3vyuan.2LhqHu', 'Nisha', 'Mehta', 'STUDENT', 'PGP24029', 'H104', 'Block H', '+91-9876543329', true, NOW(), NOW()),
('550e8400-e29b-41d4-a716-446655440129', 'student_divya', 'divya.shah@student.iimtrichy.ac.in', '$2a$10$N.zmdr9k7uOCQb0bgjam.OOzVAWUK94srEWt8j4X3vyuan.2LhqHu', 'Divya', 'Shah', 'STUDENT', 'PGP24030', 'H105', 'Block H', '+91-9876543330', true, NOW(), NOW()),

-- Additional Students for other blocks
('550e8400-e29b-41d4-a716-446655440130', 'student_rajesh', 'rajesh.reddy@student.iimtrichy.ac.in', '$2a$10$N.zmdr9k7uOCQb0bgjam.OOzVAWUK94srEWt8j4X3vyuan.2LhqHu', 'Rajesh', 'Reddy', 'STUDENT', 'PGP24031', 'E101', 'Block E', '+91-9876543331', true, NOW(), NOW()),
('550e8400-e29b-41d4-a716-446655440131', 'student_gopal', 'gopal.kumar@student.iimtrichy.ac.in', '$2a$10$N.zmdr9k7uOCQb0bgjam.OOzVAWUK94srEWt8j4X3vyuan.2LhqHu', 'Gopal', 'Kumar', 'STUDENT', 'PGP24032', 'E102', 'Block E', '+91-9876543332', true, NOW(), NOW()),
('550e8400-e29b-41d4-a716-446655440132', 'student_harish', 'harish.nair@student.iimtrichy.ac.in', '$2a$10$N.zmdr9k7uOCQb0bgjam.OOzVAWUK94srEWt8j4X3vyuan.2LhqHu', 'Harish', 'Nair', 'STUDENT', 'PGP24033', 'F101', 'Block F', '+91-9876543333', true, NOW(), NOW()),
('550e8400-e29b-41d4-a716-446655440133', 'student_krishna', 'krishna.yadav@student.iimtrichy.ac.in', '$2a$10$N.zmdr9k7uOCQb0bgjam.OOzVAWUK94srEWt8j4X3vyuan.2LhqHu', 'Krishna', 'Yadav', 'STUDENT', 'PGP24034', 'F102', 'Block F', '+91-9876543334', true, NOW(), NOW()),
('550e8400-e29b-41d4-a716-446655440134', 'student_mohan', 'mohan.singh@student.iimtrichy.ac.in', '$2a$10$N.zmdr9k7uOCQb0bgjam.OOzVAWUK94srEWt8j4X3vyuan.2LhqHu', 'Mohan', 'Singh', 'STUDENT', 'PGP24035', 'E103', 'Block E', '+91-9876543335', true, NOW(), NOW());

-- =====================================================
-- 3. INSERT STAFF-HOSTEL-CATEGORY MAPPINGS
-- =====================================================

-- Electrical Staff Mappings
INSERT INTO category_staff_mappings (id, staff_id, hostel_block, category, priority_level, capacity_weight, expertise_level, is_active, created_at, updated_at) VALUES
('650e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440010', 'Block A', 'ELECTRICAL_ISSUES', 1, 1.0, 5, true, NOW(), NOW()),
('650e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440010', 'Block B', 'ELECTRICAL_ISSUES', 1, 1.0, 5, true, NOW(), NOW()),
('650e8400-e29b-41d4-a716-446655440003', '550e8400-e29b-41d4-a716-446655440011', 'Block C', 'ELECTRICAL_ISSUES', 1, 1.0, 4, true, NOW(), NOW()),
('650e8400-e29b-41d4-a716-446655440004', '550e8400-e29b-41d4-a716-446655440011', 'Block D', 'ELECTRICAL_ISSUES', 1, 1.0, 4, true, NOW(), NOW()),

-- Plumbing Staff Mappings
('650e8400-e29b-41d4-a716-446655440005', '550e8400-e29b-41d4-a716-446655440012', 'Block A', 'PLUMBING_WATER', 1, 1.0, 5, true, NOW(), NOW()),
('650e8400-e29b-41d4-a716-446655440006', '550e8400-e29b-41d4-a716-446655440012', 'Block B', 'PLUMBING_WATER', 1, 1.0, 5, true, NOW(), NOW()),
('650e8400-e29b-41d4-a716-446655440007', '550e8400-e29b-41d4-a716-446655440013', 'Block C', 'PLUMBING_WATER', 1, 1.0, 4, true, NOW(), NOW()),
('650e8400-e29b-41d4-a716-446655440008', '550e8400-e29b-41d4-a716-446655440013', 'Block D', 'PLUMBING_WATER', 1, 1.0, 4, true, NOW(), NOW()),

-- HVAC Staff Mappings
('650e8400-e29b-41d4-a716-446655440009', '550e8400-e29b-41d4-a716-446655440014', 'Block A', 'HVAC', 1, 1.0, 4, true, NOW(), NOW()),
('650e8400-e29b-41d4-a716-446655440010', '550e8400-e29b-41d4-a716-446655440014', 'Block B', 'HVAC', 1, 1.0, 4, true, NOW(), NOW()),
('650e8400-e29b-41d4-a716-446655440011', '550e8400-e29b-41d4-a716-446655440015', 'Block C', 'HVAC', 1, 1.0, 3, true, NOW(), NOW()),
('650e8400-e29b-41d4-a716-446655440012', '550e8400-e29b-41d4-a716-446655440015', 'Block D', 'HVAC', 1, 1.0, 3, true, NOW(), NOW()),

-- IT Support Mappings (Cross-block)
('650e8400-e29b-41d4-a716-446655440013', '550e8400-e29b-41d4-a716-446655440016', NULL, 'NETWORK_INTERNET', 1, 1.0, 5, true, NOW(), NOW()),
('650e8400-e29b-41d4-a716-446655440014', '550e8400-e29b-41d4-a716-446655440016', NULL, 'COMPUTER_HARDWARE', 1, 1.0, 5, true, NOW(), NOW()),
('650e8400-e29b-41d4-a716-446655440015', '550e8400-e29b-41d4-a716-446655440017', NULL, 'AUDIO_VISUAL_EQUIPMENT', 1, 1.0, 4, true, NOW(), NOW()),

-- Housekeeping Mappings
('650e8400-e29b-41d4-a716-446655440016', '550e8400-e29b-41d4-a716-446655440018', 'Block A', 'HOUSEKEEPING_CLEANLINESS', 1, 1.0, 4, true, NOW(), NOW()),
('650e8400-e29b-41d4-a716-446655440017', '550e8400-e29b-41d4-a716-446655440018', 'Block B', 'HOUSEKEEPING_CLEANLINESS', 1, 1.0, 4, true, NOW(), NOW()),
('650e8400-e29b-41d4-a716-446655440018', '550e8400-e29b-41d4-a716-446655440019', 'Block G', 'HOUSEKEEPING_CLEANLINESS', 1, 1.0, 3, true, NOW(), NOW()),
('650e8400-e29b-41d4-a716-446655440019', '550e8400-e29b-41d4-a716-446655440019', 'Block H', 'HOUSEKEEPING_CLEANLINESS', 1, 1.0, 3, true, NOW(), NOW()),

-- Security Mappings (Cross-block)
('650e8400-e29b-41d4-a716-446655440020', '550e8400-e29b-41d4-a716-446655440020', NULL, 'SAFETY_SECURITY', 1, 1.0, 5, true, NOW(), NOW()),
('650e8400-e29b-41d4-a716-446655440021', '550e8400-e29b-41d4-a716-446655440021', NULL, 'SECURITY_SYSTEMS', 1, 1.0, 4, true, NOW(), NOW()),

-- Warden Mappings
('650e8400-e29b-41d4-a716-446655440022', '550e8400-e29b-41d4-a716-446655440022', 'Block A', 'GENERAL', 1, 1.5, 5, true, NOW(), NOW()),
('650e8400-e29b-41d4-a716-446655440023', '550e8400-e29b-41d4-a716-446655440022', 'Block B', 'GENERAL', 1, 1.5, 5, true, NOW(), NOW()),
('650e8400-e29b-41d4-a716-446655440024', '550e8400-e29b-41d4-a716-446655440023', 'Block G', 'GENERAL', 1, 1.5, 5, true, NOW(), NOW()),
('650e8400-e29b-41d4-a716-446655440025', '550e8400-e29b-41d4-a716-446655440023', 'Block H', 'GENERAL', 1, 1.5, 5, true, NOW(), NOW()),

-- General Maintenance Mappings
('650e8400-e29b-41d4-a716-446655440026', '550e8400-e29b-41d4-a716-446655440024', 'Block E', 'GENERAL', 1, 1.0, 3, true, NOW(), NOW()),
('650e8400-e29b-41d4-a716-446655440027', '550e8400-e29b-41d4-a716-446655440024', 'Block F', 'GENERAL', 1, 1.0, 3, true, NOW(), NOW()),
('650e8400-e29b-41d4-a716-446655440028', '550e8400-e29b-41d4-a716-446655440025', 'Block C', 'GENERAL', 1, 1.0, 3, true, NOW(), NOW()),
('650e8400-e29b-41d4-a716-446655440029', '550e8400-e29b-41d4-a716-446655440025', 'Block D', 'GENERAL', 1, 1.0, 3, true, NOW(), NOW()),

-- Carpentry Mappings
('650e8400-e29b-41d4-a716-446655440030', '550e8400-e29b-41d4-a716-446655440026', NULL, 'FURNITURE_FIXTURES', 1, 1.0, 4, true, NOW(), NOW()),
('650e8400-e29b-41d4-a716-446655440031', '550e8400-e29b-41d4-a716-446655440026', NULL, 'STRUCTURAL_CIVIL', 2, 1.0, 3, true, NOW(), NOW()),

-- Network Admin Mappings
('650e8400-e29b-41d4-a716-446655440032', '550e8400-e29b-41d4-a716-446655440027', NULL, 'NETWORK_INTERNET', 2, 1.2, 5, true, NOW(), NOW()),
('650e8400-e29b-41d4-a716-446655440033', '550e8400-e29b-41d4-a716-446655440027', NULL, 'SECURITY_SYSTEMS', 2, 1.2, 4, true, NOW(), NOW()),

-- Block Supervisor Mappings
('650e8400-e29b-41d4-a716-446655440034', '550e8400-e29b-41d4-a716-446655440028', 'Block E', 'GENERAL', 2, 1.3, 4, true, NOW(), NOW()),
('650e8400-e29b-41d4-a716-446655440035', '550e8400-e29b-41d4-a716-446655440028', 'Block F', 'GENERAL', 2, 1.3, 4, true, NOW(), NOW()),
('650e8400-e29b-41d4-a716-446655440036', '550e8400-e29b-41d4-a716-446655440029', 'Block G', 'GENERAL', 2, 1.3, 4, true, NOW(), NOW()),
('650e8400-e29b-41d4-a716-446655440037', '550e8400-e29b-41d4-a716-446655440029', 'Block H', 'GENERAL', 2, 1.3, 4, true, NOW(), NOW());

-- =====================================================
-- 4. INSERT SAMPLE TICKETS
-- =====================================================

-- Sample tickets with different statuses and categories
INSERT INTO tickets (id, ticket_number, title, description, category_enum, priority, status, hostel_block, room_number, location_details, created_by, assigned_to, created_at, updated_at) VALUES
-- Open Tickets
('750e8400-e29b-41d4-a716-446655440001', 'TKT-2025-0001', 'AC not working in room A101', 'The air conditioning unit in room A101 is not cooling properly. It makes noise but no cold air comes out.', 'HVAC', 'HIGH', 'OPEN', 'Block A', 'A101', 'Room A101, near the window', '550e8400-e29b-41d4-a716-446655440100', NULL, NOW() - INTERVAL '2 hours', NOW() - INTERVAL '2 hours'),

('750e8400-e29b-41d4-a716-446655440002', 'TKT-2025-0002', 'WiFi connectivity issues in Block B', 'Internet connection is very slow and frequently disconnects in Block B, Floor 1.', 'NETWORK_INTERNET', 'MEDIUM', 'OPEN', 'Block B', 'B102', 'Block B, Floor 1, Common Area', '550e8400-e29b-41d4-a716-446655440106', NULL, NOW() - INTERVAL '4 hours', NOW() - INTERVAL '4 hours'),

('750e8400-e29b-41d4-a716-446655440003', 'TKT-2025-0003', 'Leaking tap in bathroom', 'The bathroom tap in room G103 is leaking continuously. Water is wasting and making noise.', 'PLUMBING_WATER', 'MEDIUM', 'OPEN', 'Block G', 'G103', 'Room G103, Bathroom', '550e8400-e29b-41d4-a716-446655440122', NULL, NOW() - INTERVAL '6 hours', NOW() - INTERVAL '6 hours'),

-- Assigned Tickets
('750e8400-e29b-41d4-a716-446655440004', 'TKT-2025-0004', 'Broken study table in room C102', 'The study table leg is broken and the table is unstable. Cannot use it for studying.', 'FURNITURE_FIXTURES', 'MEDIUM', 'ASSIGNED', 'Block C', 'C102', 'Room C102, Study Area', '550e8400-e29b-41d4-a716-446655440111', '550e8400-e29b-41d4-a716-446655440026', NOW() - INTERVAL '1 day', NOW() - INTERVAL '8 hours'),

('750e8400-e29b-41d4-a716-446655440005', 'TKT-2025-0005', 'Power outlet not working', 'The power outlet near the bed in room D103 is not working. Cannot charge laptop or phone.', 'ELECTRICAL_ISSUES', 'HIGH', 'ASSIGNED', 'Block D', 'D103', 'Room D103, Near bed', '550e8400-e29b-41d4-a716-446655440117', '550e8400-e29b-41d4-a716-446655440011', NOW() - INTERVAL '12 hours', NOW() - INTERVAL '4 hours'),

-- In Progress Tickets
('750e8400-e29b-41d4-a716-446655440006', 'TKT-2025-0006', 'Housekeeping request for deep cleaning', 'Room H104 needs deep cleaning. There are stains on the carpet and bathroom needs thorough cleaning.', 'HOUSEKEEPING_CLEANLINESS', 'LOW', 'IN_PROGRESS', 'Block H', 'H104', 'Room H104, Entire room', '550e8400-e29b-41d4-a716-446655440128', '550e8400-e29b-41d4-a716-446655440019', NOW() - INTERVAL '2 days', NOW() - INTERVAL '6 hours'),

('750e8400-e29b-41d4-a716-446655440007', 'TKT-2025-0007', 'Security concern - broken lock', 'The main door lock of room B104 is broken. Door does not lock properly, security issue.', 'SAFETY_SECURITY', 'HIGH', 'IN_PROGRESS', 'Block B', 'B104', 'Room B104, Main door', '550e8400-e29b-41d4-a716-446655440108', '550e8400-e29b-41d4-a716-446655440020', NOW() - INTERVAL '8 hours', NOW() - INTERVAL '2 hours'),

-- Resolved Tickets
('750e8400-e29b-41d4-a716-446655440008', 'TKT-2025-0008', 'Bathroom light not working', 'The bathroom light in room A103 was not working. Bulb needed replacement.', 'ELECTRICAL_ISSUES', 'MEDIUM', 'RESOLVED', 'Block A', 'A103', 'Room A103, Bathroom', '550e8400-e29b-41d4-a716-446655440102', '550e8400-e29b-41d4-a716-446655440010', NOW() - INTERVAL '3 days', NOW() - INTERVAL '1 day'),

('750e8400-e29b-41d4-a716-446655440009', 'TKT-2025-0009', 'Slow internet in computer lab', 'Internet speed was very slow in the computer lab. Network configuration was optimized.', 'NETWORK_INTERNET', 'MEDIUM', 'RESOLVED', 'Block E', 'E-Lab', 'Block E, Computer Lab', '550e8400-e29b-41d4-a716-446655440130', '550e8400-e29b-41d4-a716-446655440016', NOW() - INTERVAL '5 days', NOW() - INTERVAL '2 days'),

-- Closed Tickets
('750e8400-e29b-41d4-a716-446655440010', 'TKT-2025-0010', 'Water cooler maintenance', 'Water cooler in Block F common area was not cooling water properly. Filter replaced and serviced.', 'GENERAL', 'LOW', 'CLOSED', 'Block F', 'Common', 'Block F, Common Area', '550e8400-e29b-41d4-a716-446655440132', '550e8400-e29b-41d4-a716-446655440024', NOW() - INTERVAL '7 days', NOW() - INTERVAL '3 days'),

('750e8400-e29b-41d4-a716-446655440011', 'TKT-2025-0011', 'Wardrobe door repair', 'Wardrobe door in room G105 was not closing properly. Hinges were adjusted and door aligned.', 'FURNITURE_FIXTURES', 'LOW', 'CLOSED', 'Block G', 'G105', 'Room G105, Wardrobe', '550e8400-e29b-41d4-a716-446655440124', '550e8400-e29b-41d4-a716-446655440026', NOW() - INTERVAL '10 days', NOW() - INTERVAL '5 days'),

-- Emergency Tickets
('750e8400-e29b-41d4-a716-446655440012', 'TKT-2025-0012', 'EMERGENCY: Water leakage from ceiling', 'Major water leakage from ceiling in room C104. Water is dripping on bed and study table.', 'PLUMBING_WATER', 'EMERGENCY', 'ASSIGNED', 'Block C', 'C104', 'Room C104, Ceiling above bed', '550e8400-e29b-41d4-a716-446655440113', '550e8400-e29b-41d4-a716-446655440013', NOW() - INTERVAL '30 minutes', NOW() - INTERVAL '15 minutes'),

('750e8400-e29b-41d4-a716-446655440013', 'TKT-2025-0013', 'EMERGENCY: Power outage in entire Block D', 'Complete power outage in Block D. No electricity in any room or common areas.', 'ELECTRICAL_ISSUES', 'EMERGENCY', 'IN_PROGRESS', 'Block D', 'All Rooms', 'Block D, Entire building', '550e8400-e29b-41d4-a716-446655440115', '550e8400-e29b-41d4-a716-446655440011', NOW() - INTERVAL '45 minutes', NOW() - INTERVAL '30 minutes');

-- =====================================================
-- 5. INSERT SAMPLE TICKET COMMENTS
-- =====================================================

INSERT INTO ticket_comments (id, ticket_id, user_id, comment, is_internal, created_at) VALUES
-- Comments for ticket TKT-2025-0004 (Broken study table)
('850e8400-e29b-41d4-a716-446655440001', '750e8400-e29b-41d4-a716-446655440004', '550e8400-e29b-41d4-a716-446655440111', 'The table leg is completely broken. I cannot use it for studying. Please fix it urgently.', false, NOW() - INTERVAL '8 hours'),
('850e8400-e29b-41d4-a716-446655440002', '750e8400-e29b-41d4-a716-446655440004', '550e8400-e29b-41d4-a716-446655440026', 'I have inspected the table. The leg can be repaired. Will bring tools tomorrow morning.', false, NOW() - INTERVAL '6 hours'),

-- Comments for ticket TKT-2025-0005 (Power outlet)
('850e8400-e29b-41d4-a716-446655440003', '750e8400-e29b-41d4-a716-446655440005', '550e8400-e29b-41d4-a716-446655440011', 'Checked the outlet. Seems like internal wiring issue. Will need to open the wall socket.', true, NOW() - INTERVAL '3 hours'),

-- Comments for emergency ticket TKT-2025-0012 (Water leakage)
('850e8400-e29b-41d4-a716-446655440004', '750e8400-e29b-41d4-a716-446655440012', '550e8400-e29b-41d4-a716-446655440013', 'Water supply to upper floor has been shut off temporarily. Investigating the source of leak.', false, NOW() - INTERVAL '10 minutes');

-- =====================================================
-- 6. INSERT SAMPLE NOTIFICATIONS
-- =====================================================

INSERT INTO notifications (id, user_id, title, message, type, is_read, related_ticket_id, created_at) VALUES
-- Notifications for students
('950e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440111', 'Ticket Assigned', 'Your ticket TKT-2025-0004 has been assigned to Krishna Moorthy (Carpenter)', 'IN_APP', false, '750e8400-e29b-41d4-a716-446655440004', NOW() - INTERVAL '8 hours'),

('950e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440117', 'Ticket In Progress', 'Work has started on your ticket TKT-2025-0005 (Power outlet not working)', 'IN_APP', false, '750e8400-e29b-41d4-a716-446655440005', NOW() - INTERVAL '2 hours'),

('950e8400-e29b-41d4-a716-446655440003', '550e8400-e29b-41d4-a716-446655440102', 'Ticket Resolved', 'Your ticket TKT-2025-0008 has been resolved. Please verify the work.', 'IN_APP', true, '750e8400-e29b-41d4-a716-446655440008', NOW() - INTERVAL '1 day'),

-- Notifications for staff
('950e8400-e29b-41d4-a716-446655440004', '550e8400-e29b-41d4-a716-446655440026', 'New Ticket Assigned', 'New ticket TKT-2025-0004 assigned to you: Broken study table in room C102', 'IN_APP', true, '750e8400-e29b-41d4-a716-446655440004', NOW() - INTERVAL '8 hours'),

('950e8400-e29b-41d4-a716-446655440005', '550e8400-e29b-41d4-a716-446655440013', 'Emergency Ticket', 'URGENT: Emergency ticket TKT-2025-0012 assigned - Water leakage from ceiling', 'IN_APP', false, '750e8400-e29b-41d4-a716-446655440012', NOW() - INTERVAL '15 minutes'),

-- System notifications
('950e8400-e29b-41d4-a716-446655440006', '550e8400-e29b-41d4-a716-446655440000', 'System Maintenance', 'Scheduled system maintenance will be performed tonight from 2:00 AM to 4:00 AM', 'IN_APP', false, NULL, NOW() - INTERVAL '12 hours');

-- =====================================================
-- 7. VERIFICATION QUERIES
-- =====================================================

-- Verify data insertion
SELECT 'Users Created' as table_name, COUNT(*) as count FROM users
UNION ALL
SELECT 'Staff Mappings Created', COUNT(*) FROM category_staff_mappings
UNION ALL
SELECT 'Tickets Created', COUNT(*) FROM tickets
UNION ALL
SELECT 'Comments Created', COUNT(*) FROM ticket_comments
UNION ALL
SELECT 'Notifications Created', COUNT(*) FROM notifications;

-- Show user distribution by role
SELECT role, COUNT(*) as count FROM users GROUP BY role ORDER BY role;

-- Show ticket distribution by status
SELECT status, COUNT(*) as count FROM tickets GROUP BY status ORDER BY status;

-- Show mapping distribution by category
SELECT category, COUNT(*) as count FROM category_staff_mappings GROUP BY category ORDER BY category;

COMMIT;
