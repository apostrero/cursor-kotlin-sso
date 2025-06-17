-- Create portfolios table
CREATE TABLE portfolios (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    owner_id BIGINT NOT NULL,
    organization_id BIGINT
);

-- Create indexes for better performance
CREATE INDEX idx_portfolios_owner_id ON portfolios(owner_id);
CREATE INDEX idx_portfolios_organization_id ON portfolios(organization_id);
CREATE INDEX idx_portfolios_type ON portfolios(type);
CREATE INDEX idx_portfolios_status ON portfolios(status);
CREATE INDEX idx_portfolios_is_active ON portfolios(is_active);
CREATE INDEX idx_portfolios_created_at ON portfolios(created_at);

-- Add comments for documentation
COMMENT ON TABLE portfolios IS 'Technology portfolios managed by users';
COMMENT ON COLUMN portfolios.id IS 'Unique identifier for the portfolio';
COMMENT ON COLUMN portfolios.name IS 'Name of the portfolio (must be unique)';
COMMENT ON COLUMN portfolios.description IS 'Optional description of the portfolio';
COMMENT ON COLUMN portfolios.type IS 'Type of portfolio (ENTERPRISE, DEPARTMENT, PROJECT, etc.)';
COMMENT ON COLUMN portfolios.status IS 'Current status of the portfolio (ACTIVE, ARCHIVED, etc.)';
COMMENT ON COLUMN portfolios.is_active IS 'Whether the portfolio is currently active';
COMMENT ON COLUMN portfolios.created_at IS 'Timestamp when the portfolio was created';
COMMENT ON COLUMN portfolios.updated_at IS 'Timestamp when the portfolio was last updated';
COMMENT ON COLUMN portfolios.owner_id IS 'ID of the user who owns this portfolio';
COMMENT ON COLUMN portfolios.organization_id IS 'ID of the organization this portfolio belongs to (optional)'; 