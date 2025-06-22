-- H2 Database Schema for Integration Tests (converted from PostgreSQL Flyway migrations)

-- Create portfolios table
CREATE TABLE IF NOT EXISTS portfolios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
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
CREATE INDEX IF NOT EXISTS idx_portfolios_owner_id ON portfolios(owner_id);
CREATE INDEX IF NOT EXISTS idx_portfolios_organization_id ON portfolios(organization_id);
CREATE INDEX IF NOT EXISTS idx_portfolios_type ON portfolios(type);
CREATE INDEX IF NOT EXISTS idx_portfolios_status ON portfolios(status);
CREATE INDEX IF NOT EXISTS idx_portfolios_is_active ON portfolios(is_active);
CREATE INDEX IF NOT EXISTS idx_portfolios_created_at ON portfolios(created_at);

-- Create technologies table
CREATE TABLE IF NOT EXISTS technologies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
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
CREATE INDEX IF NOT EXISTS idx_technologies_portfolio_id ON technologies(portfolio_id);
CREATE INDEX IF NOT EXISTS idx_technologies_name ON technologies(name);
CREATE INDEX IF NOT EXISTS idx_technologies_category ON technologies(category);
CREATE INDEX IF NOT EXISTS idx_technologies_type ON technologies(type);
CREATE INDEX IF NOT EXISTS idx_technologies_vendor_name ON technologies(vendor_name);
CREATE INDEX IF NOT EXISTS idx_technologies_maturity_level ON technologies(maturity_level);
CREATE INDEX IF NOT EXISTS idx_technologies_risk_level ON technologies(risk_level);
CREATE INDEX IF NOT EXISTS idx_technologies_is_active ON technologies(is_active);
CREATE INDEX IF NOT EXISTS idx_technologies_created_at ON technologies(created_at);
CREATE INDEX IF NOT EXISTS idx_technologies_support_contract_expiry ON technologies(support_contract_expiry); 