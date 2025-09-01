-- IIM Trichy Hostel Ticket Management System Database Schema
-- Based on Product Design Document v1.0

-- Create extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- =====================================================
-- ENUM TYPES BASED ON PRODUCT DESIGN
-- =====================================================

-- User roles with strict access control
CREATE TYPE user_role AS ENUM ('STUDENT', 'STAFF', 'ADMIN');

-- Ticket status workflow (8-stage workflow as per design)
CREATE TYPE ticket_status AS ENUM (
    'OPEN', 'ASSIGNED', 'IN_PROGRESS', 'ON_HOLD', 
    'RESOLVED', 'CLOSED', 'CANCELLED', 'REOPENED'
);

-- Priority levels as defined in design
CREATE TYPE ticket_priority AS ENUM ('LOW', 'MEDIUM', 'HIGH', 'EMERGENCY');

-- Comprehensive ticket categories based on product design
CREATE TYPE ticket_category AS ENUM (
    -- Infrastructure Categories
    'ELECTRICAL_ISSUES',
    'PLUMBING_WATER',
    'HVAC',
    'STRUCTURAL_CIVIL',
    'FURNITURE_FIXTURES',
    
    -- IT & Technology Categories
    'NETWORK_INTERNET',
    'COMPUTER_HARDWARE',
    'AUDIO_VISUAL_EQUIPMENT',
    'SECURITY_SYSTEMS',
    
    -- General Maintenance Categories
    'HOUSEKEEPING_CLEANLINESS',
    'SAFETY_SECURITY',
    'LANDSCAPING_OUTDOOR',
    
    -- General
    'GENERAL'
);

-- Staff verticals based on IIM Trichy requirements
CREATE TYPE staff_vertical AS ENUM (
    -- Technical Staff
    'ELECTRICAL',
    'PLUMBING',
    'HVAC',
    'CARPENTRY',
    'IT_SUPPORT',
    'NETWORK_ADMIN',
    'SECURITY_SYSTEMS',
    
    -- General Maintenance
    'HOUSEKEEPING',
    'LANDSCAPING',
    'GENERAL_MAINTENANCE',
    
    -- Administrative
    'HOSTEL_WARDEN',
    'BLOCK_SUPERVISOR',
    'SECURITY_OFFICER',
    'ADMIN_STAFF'
);

-- Notification types
CREATE TYPE notification_type AS ENUM ('EMAIL', 'SMS', 'IN_APP', 'PUSH');

-- =====================================================
-- CORE TABLES
-- =====================================================

-- Users table with comprehensive role-based access
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    role user_role NOT NULL DEFAULT 'STUDENT',
    staff_vertical staff_vertical,
    
    -- IDs and identification
    staff_id VARCHAR(20) UNIQUE,
    student_id VARCHAR(20) UNIQUE,
    employee_code VARCHAR(20),
    
    -- Location information (IIM Trichy specific)
    room_number VARCHAR(10),
    hostel_block VARCHAR(50), -- IIM Trichy hostel blocks
    floor_number INTEGER,
    
    -- Contact information
    phone VARCHAR(20),
    emergency_contact VARCHAR(20),
    
    -- Status and metadata
    is_active BOOLEAN DEFAULT true,
    last_login TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT chk_student_has_student_id CHECK (
        (role = 'STUDENT' AND student_id IS NOT NULL) OR 
        (role != 'STUDENT')
    ),
    CONSTRAINT chk_staff_has_staff_id CHECK (
        (role = 'STAFF' AND staff_id IS NOT NULL AND staff_vertical IS NOT NULL) OR 
        (role != 'STAFF')
    )
);

-- IIM Trichy Hostel Blocks (based on research)
CREATE TABLE hostel_blocks (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    block_name VARCHAR(50) NOT NULL UNIQUE,
    block_code VARCHAR(10) NOT NULL UNIQUE,
    total_floors INTEGER NOT NULL DEFAULT 3,
    rooms_per_floor INTEGER NOT NULL DEFAULT 18,
    total_rooms INTEGER NOT NULL,
    is_female_block BOOLEAN DEFAULT false,
    has_disabled_access BOOLEAN DEFAULT true,
    warden_id UUID REFERENCES users(id),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tickets table with comprehensive tracking
CREATE TABLE tickets (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    ticket_number VARCHAR(30) UNIQUE NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    
    -- Category handling (enum or custom)
    category_enum ticket_category,
    custom_category VARCHAR(100),
    
    -- Priority and status
    priority ticket_priority NOT NULL DEFAULT 'MEDIUM',
    status ticket_status NOT NULL DEFAULT 'OPEN',
    
    -- User relationships
    created_by UUID NOT NULL REFERENCES users(id),
    assigned_to UUID REFERENCES users(id),
    
    -- Location information (hostel_block is mandatory)
    room_number VARCHAR(10),
    hostel_block VARCHAR(50) NOT NULL,
    floor_number INTEGER,
    location_details TEXT,
    
    -- Time tracking
    estimated_resolution_time TIMESTAMP,
    actual_resolution_time TIMESTAMP,
    sla_breach_time TIMESTAMP,
    
    -- Status timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    assigned_at TIMESTAMP,
    started_at TIMESTAMP,
    resolved_at TIMESTAMP,
    closed_at TIMESTAMP,
    
    -- Additional metadata
    is_emergency BOOLEAN DEFAULT false,
    is_recurring BOOLEAN DEFAULT false,
    parent_ticket_id UUID REFERENCES tickets(id),
    estimated_cost DECIMAL(10,2),
    actual_cost DECIMAL(10,2),
    
    -- Satisfaction and feedback
    satisfaction_rating INTEGER CHECK (satisfaction_rating >= 1 AND satisfaction_rating <= 5),
    feedback TEXT
);

-- Staff-Hostel-Category Mapping (Multi-dimensional mapping as per design)
CREATE TABLE category_staff_mappings (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    staff_id UUID NOT NULL REFERENCES users(id),
    hostel_block VARCHAR(50), -- NULL means all blocks
    category VARCHAR(100) NOT NULL, -- Can be enum value or custom category
    priority_level INTEGER NOT NULL DEFAULT 1, -- 1 = highest priority
    capacity_weight DECIMAL(3,2) NOT NULL DEFAULT 1.0, -- Staff capacity multiplier
    expertise_level INTEGER NOT NULL DEFAULT 1 CHECK (expertise_level >= 1 AND expertise_level <= 5),
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Unique constraint for staff-block-category combination
    UNIQUE(staff_id, hostel_block, category)
);

-- Ticket Comments with internal/external distinction
CREATE TABLE ticket_comments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    ticket_id UUID NOT NULL REFERENCES tickets(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id),
    comment TEXT NOT NULL,
    is_internal BOOLEAN DEFAULT false, -- Internal comments for staff only
    is_system_generated BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Ticket Attachments
CREATE TABLE ticket_attachments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    ticket_id UUID NOT NULL REFERENCES tickets(id) ON DELETE CASCADE,
    filename VARCHAR(255) NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT NOT NULL,
    mime_type VARCHAR(100),
    uploaded_by UUID NOT NULL REFERENCES users(id),
    is_before_photo BOOLEAN DEFAULT false,
    is_after_photo BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Ticket History for audit trail
CREATE TABLE ticket_history (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    ticket_id UUID NOT NULL REFERENCES tickets(id) ON DELETE CASCADE,
    field_name VARCHAR(50) NOT NULL,
    old_value TEXT,
    new_value TEXT,
    changed_by UUID NOT NULL REFERENCES users(id),
    change_reason TEXT,
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Notifications system
CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id),
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    type notification_type NOT NULL DEFAULT 'IN_APP',
    is_read BOOLEAN DEFAULT false,
    related_ticket_id UUID REFERENCES tickets(id),
    action_url VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP
);

-- Ticket Escalations
CREATE TABLE ticket_escalations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    ticket_id UUID NOT NULL REFERENCES tickets(id) ON DELETE CASCADE,
    escalated_from UUID REFERENCES users(id),
    escalated_to UUID NOT NULL REFERENCES users(id),
    escalation_level INTEGER NOT NULL DEFAULT 1,
    reason TEXT NOT NULL,
    escalated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP,
    is_auto_escalated BOOLEAN DEFAULT false
);

-- =====================================================
-- INDEXES FOR PERFORMANCE
-- =====================================================

-- Users indexes
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_hostel_block ON users(hostel_block);
CREATE INDEX idx_users_room_number ON users(room_number);
CREATE INDEX idx_users_staff_vertical ON users(staff_vertical);
CREATE INDEX idx_users_is_active ON users(is_active);

-- Tickets indexes
CREATE INDEX idx_tickets_status ON tickets(status);
CREATE INDEX idx_tickets_priority ON tickets(priority);
CREATE INDEX idx_tickets_category_enum ON tickets(category_enum);
CREATE INDEX idx_tickets_custom_category ON tickets(custom_category);
CREATE INDEX idx_tickets_created_by ON tickets(created_by);
CREATE INDEX idx_tickets_assigned_to ON tickets(assigned_to);
CREATE INDEX idx_tickets_created_at ON tickets(created_at);
CREATE INDEX idx_tickets_hostel_block ON tickets(hostel_block);
CREATE INDEX idx_tickets_room_number ON tickets(room_number);
CREATE INDEX idx_tickets_is_emergency ON tickets(is_emergency);
CREATE INDEX idx_tickets_status_priority ON tickets(status, priority);

-- Mapping indexes
CREATE INDEX idx_mappings_staff_id ON category_staff_mappings(staff_id);
CREATE INDEX idx_mappings_hostel_block ON category_staff_mappings(hostel_block);
CREATE INDEX idx_mappings_category ON category_staff_mappings(category);
CREATE INDEX idx_mappings_is_active ON category_staff_mappings(is_active);
CREATE INDEX idx_mappings_priority_level ON category_staff_mappings(priority_level);

-- Comments and attachments indexes
CREATE INDEX idx_comments_ticket_id ON ticket_comments(ticket_id);
CREATE INDEX idx_comments_created_at ON ticket_comments(created_at);
CREATE INDEX idx_comments_is_internal ON ticket_comments(is_internal);

CREATE INDEX idx_attachments_ticket_id ON ticket_attachments(ticket_id);
CREATE INDEX idx_attachments_uploaded_by ON ticket_attachments(uploaded_by);

-- Notifications indexes
CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_is_read ON notifications(is_read);
CREATE INDEX idx_notifications_created_at ON notifications(created_at);
CREATE INDEX idx_notifications_type ON notifications(type);

-- History and escalations indexes
CREATE INDEX idx_history_ticket_id ON ticket_history(ticket_id);
CREATE INDEX idx_history_changed_at ON ticket_history(changed_at);

CREATE INDEX idx_escalations_ticket_id ON ticket_escalations(ticket_id);
CREATE INDEX idx_escalations_escalated_to ON ticket_escalations(escalated_to);

-- =====================================================
-- FUNCTIONS AND TRIGGERS
-- =====================================================

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Triggers for updated_at
CREATE TRIGGER update_tickets_updated_at BEFORE UPDATE ON tickets
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_mappings_updated_at BEFORE UPDATE ON category_staff_mappings
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Function to generate ticket number
CREATE OR REPLACE FUNCTION generate_ticket_number()
RETURNS TRIGGER AS $$
BEGIN
    NEW.ticket_number := 'IIM-TKT-' || EXTRACT(YEAR FROM CURRENT_DATE) || '-' || 
                        LPAD(EXTRACT(DOY FROM CURRENT_DATE)::TEXT, 3, '0') || '-' ||
                        LPAD((SELECT COUNT(*) + 1 FROM tickets WHERE DATE(created_at) = CURRENT_DATE)::TEXT, 4, '0');
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Trigger for ticket number generation
CREATE TRIGGER generate_ticket_number_trigger BEFORE INSERT ON tickets
    FOR EACH ROW EXECUTE FUNCTION generate_ticket_number();

-- Function to update ticket timestamps based on status changes
CREATE OR REPLACE FUNCTION update_ticket_status_timestamps()
RETURNS TRIGGER AS $$
BEGIN
    -- Update assigned_at when ticket is assigned
    IF OLD.status != 'ASSIGNED' AND NEW.status = 'ASSIGNED' THEN
        NEW.assigned_at = CURRENT_TIMESTAMP;
    END IF;
    
    -- Update started_at when ticket moves to IN_PROGRESS
    IF OLD.status != 'IN_PROGRESS' AND NEW.status = 'IN_PROGRESS' THEN
        NEW.started_at = CURRENT_TIMESTAMP;
    END IF;
    
    -- Update resolved_at when ticket is resolved
    IF OLD.status != 'RESOLVED' AND NEW.status = 'RESOLVED' THEN
        NEW.resolved_at = CURRENT_TIMESTAMP;
        NEW.actual_resolution_time = CURRENT_TIMESTAMP;
    END IF;
    
    -- Update closed_at when ticket is closed
    IF OLD.status != 'CLOSED' AND NEW.status = 'CLOSED' THEN
        NEW.closed_at = CURRENT_TIMESTAMP;
    END IF;
    
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Trigger for status timestamp updates
CREATE TRIGGER update_ticket_status_timestamps_trigger BEFORE UPDATE ON tickets
    FOR EACH ROW EXECUTE FUNCTION update_ticket_status_timestamps();

-- =====================================================
-- INITIAL DATA - IIM TRICHY HOSTEL BLOCKS
-- =====================================================

-- Insert IIM Trichy hostel blocks based on research
INSERT INTO hostel_blocks (block_name, block_code, total_floors, rooms_per_floor, total_rooms, is_female_block) VALUES
('Hostel Block A', 'BLK-A', 3, 18, 54, false),
('Hostel Block B', 'BLK-B', 3, 18, 54, false),
('Hostel Block C', 'BLK-C', 3, 18, 54, false),
('Hostel Block D', 'BLK-D', 3, 18, 54, false),
('Hostel Block E', 'BLK-E', 3, 18, 54, false),
('Hostel Block F', 'BLK-F', 3, 18, 54, false),
('Hostel Block G', 'BLK-G', 3, 18, 54, true), -- Female block
('Hostel Block H', 'BLK-H', 8, 41, 328, false); -- 8-story building

-- Sample admin user
INSERT INTO users (username, email, password_hash, first_name, last_name, role, phone) VALUES
('admin', 'admin@iimtrichy.ac.in', crypt('admin123', gen_salt('bf')), 'System', 'Administrator', 'ADMIN', '+91-9876543210');

-- Sample staff members with IIM Trichy specific roles
INSERT INTO users (username, email, password_hash, first_name, last_name, role, staff_vertical, staff_id, hostel_block, phone) VALUES
-- Technical Staff
('electrical_tech', 'electrical@iimtrichy.ac.in', crypt('staff123', gen_salt('bf')), 'Rajesh', 'Kumar', 'STAFF', 'ELECTRICAL', 'ELE001', 'Hostel Block A', '+91-9876543211'),
('plumbing_tech', 'plumbing@iimtrichy.ac.in', crypt('staff123', gen_salt('bf')), 'Suresh', 'Babu', 'STAFF', 'PLUMBING', 'PLB001', 'Hostel Block B', '+91-9876543212'),
('hvac_tech', 'hvac@iimtrichy.ac.in', crypt('staff123', gen_salt('bf')), 'Priya', 'Sharma', 'STAFF', 'HVAC', 'HVC001', 'Hostel Block C', '+91-9876543213'),
('it_support', 'itsupport@iimtrichy.ac.in', crypt('staff123', gen_salt('bf')), 'Arun', 'Prasad', 'STAFF', 'IT_SUPPORT', 'ITS001', NULL, '+91-9876543214'),

-- Housekeeping and General
('housekeeping_a', 'housekeeping.a@iimtrichy.ac.in', crypt('staff123', gen_salt('bf')), 'Lakshmi', 'Devi', 'STAFF', 'HOUSEKEEPING', 'HKP001', 'Hostel Block A', '+91-9876543215'),
('housekeeping_b', 'housekeeping.b@iimtrichy.ac.in', crypt('staff123', gen_salt('bf')), 'Kamala', 'Bai', 'STAFF', 'HOUSEKEEPING', 'HKP002', 'Hostel Block B', '+91-9876543216'),

-- Wardens
('warden_a', 'warden.a@iimtrichy.ac.in', crypt('staff123', gen_salt('bf')), 'Dr. Venkat', 'Raman', 'STAFF', 'HOSTEL_WARDEN', 'HWA001', 'Hostel Block A', '+91-9876543217'),
('warden_g', 'warden.g@iimtrichy.ac.in', crypt('staff123', gen_salt('bf')), 'Dr. Meera', 'Nair', 'STAFF', 'HOSTEL_WARDEN', 'HWG001', 'Hostel Block G', '+91-9876543218'),

-- Security
('security_main', 'security@iimtrichy.ac.in', crypt('staff123', gen_salt('bf')), 'Murugan', 'S', 'STAFF', 'SECURITY_OFFICER', 'SEC001', NULL, '+91-9876543219');

-- Sample students
INSERT INTO users (username, email, password_hash, first_name, last_name, role, student_id, room_number, hostel_block, floor_number, phone) VALUES
('student001', 'student001@iimtrichy.ac.in', crypt('student123', gen_salt('bf')), 'Arjun', 'Reddy', 'STUDENT', 'MBA2024001', '101', 'Hostel Block A', 1, '+91-9876543220'),
('student002', 'student002@iimtrichy.ac.in', crypt('student123', gen_salt('bf')), 'Priya', 'Menon', 'STUDENT', 'MBA2024002', '201', 'Hostel Block G', 2, '+91-9876543221'),
('student003', 'student003@iimtrichy.ac.in', crypt('student123', gen_salt('bf')), 'Rohit', 'Sharma', 'STUDENT', 'MBA2024003', '102', 'Hostel Block A', 1, '+91-9876543222'),
('student004', 'student004@iimtrichy.ac.in', crypt('student123', gen_salt('bf')), 'Sneha', 'Patel', 'STUDENT', 'MBA2024004', '202', 'Hostel Block G', 2, '+91-9876543223');

-- Sample category staff mappings based on product design
INSERT INTO category_staff_mappings (staff_id, hostel_block, category, priority_level, capacity_weight, expertise_level) VALUES
-- Electrical technician mappings
((SELECT id FROM users WHERE username = 'electrical_tech'), 'Hostel Block A', 'ELECTRICAL_ISSUES', 1, 1.0, 5),
((SELECT id FROM users WHERE username = 'electrical_tech'), 'Hostel Block B', 'ELECTRICAL_ISSUES', 2, 0.8, 4),
((SELECT id FROM users WHERE username = 'electrical_tech'), NULL, 'ELECTRICAL_ISSUES', 3, 0.6, 5),

-- Plumbing technician mappings
((SELECT id FROM users WHERE username = 'plumbing_tech'), 'Hostel Block B', 'PLUMBING_WATER', 1, 1.0, 5),
((SELECT id FROM users WHERE username = 'plumbing_tech'), 'Hostel Block A', 'PLUMBING_WATER', 2, 0.8, 4),
((SELECT id FROM users WHERE username = 'plumbing_tech'), NULL, 'PLUMBING_WATER', 3, 0.6, 5),

-- HVAC technician mappings
((SELECT id FROM users WHERE username = 'hvac_tech'), 'Hostel Block C', 'HVAC', 1, 1.0, 5),
((SELECT id FROM users WHERE username = 'hvac_tech'), NULL, 'HVAC', 2, 0.8, 5),

-- IT Support mappings (all blocks)
((SELECT id FROM users WHERE username = 'it_support'), NULL, 'NETWORK_INTERNET', 1, 1.0, 5),
((SELECT id FROM users WHERE username = 'it_support'), NULL, 'COMPUTER_HARDWARE', 1, 1.0, 4),
((SELECT id FROM users WHERE username = 'it_support'), NULL, 'SECURITY_SYSTEMS', 2, 0.8, 3),

-- Housekeeping mappings
((SELECT id FROM users WHERE username = 'housekeeping_a'), 'Hostel Block A', 'HOUSEKEEPING_CLEANLINESS', 1, 1.0, 4),
((SELECT id FROM users WHERE username = 'housekeeping_b'), 'Hostel Block B', 'HOUSEKEEPING_CLEANLINESS', 1, 1.0, 4),

-- Warden mappings
((SELECT id FROM users WHERE username = 'warden_a'), 'Hostel Block A', 'GENERAL', 1, 1.0, 5),
((SELECT id FROM users WHERE username = 'warden_g'), 'Hostel Block G', 'GENERAL', 1, 1.0, 5),

-- Security mappings
((SELECT id FROM users WHERE username = 'security_main'), NULL, 'SAFETY_SECURITY', 1, 1.0, 5);
