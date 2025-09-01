-- Minimal schema initialization for Render PostgreSQL
-- This only creates essential PostgreSQL extensions
-- Hibernate will handle table creation with ddl-auto: create-drop

-- Create extensions if they don't exist
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- The rest of the schema (tables, indexes, etc.) will be created by Hibernate
-- Initial data will be populated by data.sql
