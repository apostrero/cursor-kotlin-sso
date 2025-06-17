-- Create user_roles table
CREATE TABLE user_roles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uk_user_roles_user_role UNIQUE (user_id, role)
);

-- Create indexes for better performance
CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX idx_user_roles_role ON user_roles(role);
CREATE INDEX idx_user_roles_created_at ON user_roles(created_at);

-- Add comments for documentation
COMMENT ON TABLE user_roles IS 'Roles assigned to users';
COMMENT ON COLUMN user_roles.id IS 'Unique identifier for the user role';
COMMENT ON COLUMN user_roles.user_id IS 'ID of the user';
COMMENT ON COLUMN user_roles.role IS 'Role assigned to the user (USER, ADMIN, etc.)';
COMMENT ON COLUMN user_roles.created_at IS 'Timestamp when the role was assigned';

-- Insert default roles
INSERT INTO user_roles (user_id, role) VALUES 
(1, 'USER'),
(1, 'ADMIN'); 