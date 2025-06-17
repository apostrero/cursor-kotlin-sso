-- Authorization Service Database Initialization
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Roles table
CREATE TABLE IF NOT EXISTS roles (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Permissions table
CREATE TABLE IF NOT EXISTS permissions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL UNIQUE,
    resource VARCHAR(255) NOT NULL,
    action VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- User-Role mapping table
CREATE TABLE IF NOT EXISTS user_roles (
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    role_id UUID REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- Role-Permission mapping table
CREATE TABLE IF NOT EXISTS role_permissions (
    role_id UUID REFERENCES roles(id) ON DELETE CASCADE,
    permission_id UUID REFERENCES permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

-- Insert default roles
INSERT INTO roles (name, description) VALUES 
    ('ADMIN', 'System Administrator with full access'),
    ('PORTFOLIO_MANAGER', 'Can manage technology portfolios'),
    ('VIEWER', 'Read-only access to portfolios')
ON CONFLICT (name) DO NOTHING;

-- Insert default permissions
INSERT INTO permissions (name, resource, action, description) VALUES 
    ('READ_PORTFOLIO', 'portfolio', 'read', 'Read portfolio information'),
    ('WRITE_PORTFOLIO', 'portfolio', 'write', 'Create and update portfolios'),
    ('DELETE_PORTFOLIO', 'portfolio', 'delete', 'Delete portfolios'),
    ('MANAGE_USERS', 'user', 'manage', 'Manage user accounts'),
    ('VIEW_ANALYTICS', 'analytics', 'read', 'View analytics and reports')
ON CONFLICT (name) DO NOTHING;

-- Assign permissions to roles
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id 
FROM roles r, permissions p 
WHERE r.name = 'ADMIN'
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id 
FROM roles r, permissions p 
WHERE r.name = 'PORTFOLIO_MANAGER' 
AND p.name IN ('READ_PORTFOLIO', 'WRITE_PORTFOLIO', 'VIEW_ANALYTICS')
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id 
FROM roles r, permissions p 
WHERE r.name = 'VIEWER' 
AND p.name IN ('READ_PORTFOLIO')
ON CONFLICT DO NOTHING;

-- Insert test users (these match the SimpleSAMLphp test users)
INSERT INTO users (username, email, first_name, last_name) VALUES 
    ('user1', 'user1@example.com', 'Test', 'User1'),
    ('user2', 'user2@example.com', 'Test', 'User2'),
    ('admin', 'admin@example.com', 'Admin', 'User')
ON CONFLICT (username) DO NOTHING;

-- Assign roles to test users
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id 
FROM users u, roles r 
WHERE u.username = 'admin' AND r.name = 'ADMIN'
ON CONFLICT DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id 
FROM users u, roles r 
WHERE u.username = 'user1' AND r.name = 'PORTFOLIO_MANAGER'
ON CONFLICT DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id 
FROM users u, roles r 
WHERE u.username = 'user2' AND r.name = 'VIEWER'
ON CONFLICT DO NOTHING; 