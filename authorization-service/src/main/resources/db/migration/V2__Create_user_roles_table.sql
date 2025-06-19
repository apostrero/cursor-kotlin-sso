-- Create roles table
CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create permissions table
CREATE TABLE permissions (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    resource VARCHAR(255) NOT NULL,
    action VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create user-role mapping table
CREATE TABLE user_roles (
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    role_id BIGINT REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- Create role-permission mapping table
CREATE TABLE role_permissions (
    role_id BIGINT REFERENCES roles(id) ON DELETE CASCADE,
    permission_id BIGINT REFERENCES permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

-- Create indexes for better performance
CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX idx_user_roles_role_id ON user_roles(role_id);
CREATE INDEX idx_role_permissions_role_id ON role_permissions(role_id);
CREATE INDEX idx_role_permissions_permission_id ON role_permissions(permission_id);

-- Add comments for documentation
COMMENT ON TABLE user_roles IS 'Roles assigned to users';
COMMENT ON COLUMN user_roles.user_id IS 'ID of the user';
COMMENT ON COLUMN user_roles.role_id IS 'ID of the role';

COMMENT ON TABLE roles IS 'Roles available in the system';
COMMENT ON COLUMN roles.id IS 'Unique identifier for the role';
COMMENT ON COLUMN roles.name IS 'Name of the role';
COMMENT ON COLUMN roles.description IS 'Description of the role';
COMMENT ON COLUMN roles.created_at IS 'Timestamp when the role was created';
COMMENT ON COLUMN roles.updated_at IS 'Timestamp when the role was last updated';

COMMENT ON TABLE permissions IS 'Permissions available in the system';
COMMENT ON COLUMN permissions.id IS 'Unique identifier for the permission';
COMMENT ON COLUMN permissions.name IS 'Name of the permission';
COMMENT ON COLUMN permissions.resource IS 'Resource associated with the permission';
COMMENT ON COLUMN permissions.action IS 'Action associated with the permission';
COMMENT ON COLUMN permissions.description IS 'Description of the permission';
COMMENT ON COLUMN permissions.created_at IS 'Timestamp when the permission was created';
COMMENT ON COLUMN permissions.updated_at IS 'Timestamp when the permission was last updated';

COMMENT ON TABLE role_permissions IS 'Mapping between roles and permissions';
COMMENT ON COLUMN role_permissions.role_id IS 'ID of the role';
COMMENT ON COLUMN role_permissions.permission_id IS 'ID of the permission'; 