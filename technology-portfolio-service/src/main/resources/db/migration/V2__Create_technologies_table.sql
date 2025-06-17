-- Create technologies table
CREATE TABLE technologies (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(100) NOT NULL,
    version VARCHAR(50),
    type VARCHAR(50) NOT NULL,
    maturity_level VARCHAR(50) NOT NULL,
    risk_level VARCHAR(50) NOT NULL,
    annual_cost DECIMAL(15,2),
    license_cost DECIMAL(15,2),
    maintenance_cost DECIMAL(15,2),
    vendor_name VARCHAR(255),
    vendor_contact VARCHAR(255),
    support_contract_expiry TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    portfolio_id BIGINT NOT NULL,
    CONSTRAINT fk_technologies_portfolio FOREIGN KEY (portfolio_id) REFERENCES portfolios(id) ON DELETE CASCADE
);

-- Create indexes for better performance
CREATE INDEX idx_technologies_portfolio_id ON technologies(portfolio_id);
CREATE INDEX idx_technologies_name ON technologies(name);
CREATE INDEX idx_technologies_category ON technologies(category);
CREATE INDEX idx_technologies_type ON technologies(type);
CREATE INDEX idx_technologies_vendor_name ON technologies(vendor_name);
CREATE INDEX idx_technologies_maturity_level ON technologies(maturity_level);
CREATE INDEX idx_technologies_risk_level ON technologies(risk_level);
CREATE INDEX idx_technologies_is_active ON technologies(is_active);
CREATE INDEX idx_technologies_created_at ON technologies(created_at);
CREATE INDEX idx_technologies_support_contract_expiry ON technologies(support_contract_expiry);

-- Add comments for documentation
COMMENT ON TABLE technologies IS 'Technologies within portfolios';
COMMENT ON COLUMN technologies.id IS 'Unique identifier for the technology';
COMMENT ON COLUMN technologies.name IS 'Name of the technology';
COMMENT ON COLUMN technologies.description IS 'Optional description of the technology';
COMMENT ON COLUMN technologies.category IS 'Category of the technology (Database, Application, Infrastructure, etc.)';
COMMENT ON COLUMN technologies.version IS 'Version of the technology (optional)';
COMMENT ON COLUMN technologies.type IS 'Type of technology (DATABASE, APPLICATION, INFRASTRUCTURE, etc.)';
COMMENT ON COLUMN technologies.maturity_level IS 'Maturity level of the technology (PILOT, DEVELOPMENT, PRODUCTION, etc.)';
COMMENT ON COLUMN technologies.risk_level IS 'Risk level of the technology (LOW, MEDIUM, HIGH, CRITICAL)';
COMMENT ON COLUMN technologies.annual_cost IS 'Annual cost of the technology';
COMMENT ON COLUMN technologies.license_cost IS 'License cost of the technology';
COMMENT ON COLUMN technologies.maintenance_cost IS 'Maintenance cost of the technology';
COMMENT ON COLUMN technologies.vendor_name IS 'Name of the technology vendor';
COMMENT ON COLUMN technologies.vendor_contact IS 'Contact information for the vendor';
COMMENT ON COLUMN technologies.support_contract_expiry IS 'Expiry date of the support contract';
COMMENT ON COLUMN technologies.is_active IS 'Whether the technology is currently active';
COMMENT ON COLUMN technologies.created_at IS 'Timestamp when the technology was created';
COMMENT ON COLUMN technologies.updated_at IS 'Timestamp when the technology was last updated';
COMMENT ON COLUMN technologies.portfolio_id IS 'ID of the portfolio this technology belongs to'; 