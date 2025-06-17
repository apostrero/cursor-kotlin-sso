-- Technology Portfolio Service Database Initialization
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Organizations table
CREATE TABLE IF NOT EXISTS organizations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Portfolios table
CREATE TABLE IF NOT EXISTS portfolios (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    organization_id UUID REFERENCES organizations(id),
    portfolio_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    created_by VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Technologies table
CREATE TABLE IF NOT EXISTS technologies (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    version VARCHAR(100),
    vendor VARCHAR(255),
    category VARCHAR(100),
    maturity_level VARCHAR(50),
    lifecycle_stage VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Portfolio-Technology mapping table
CREATE TABLE IF NOT EXISTS portfolio_technologies (
    portfolio_id UUID REFERENCES portfolios(id) ON DELETE CASCADE,
    technology_id UUID REFERENCES technologies(id) ON DELETE CASCADE,
    usage_context TEXT,
    adoption_date DATE,
    retirement_date DATE,
    business_criticality VARCHAR(50),
    technical_debt_score INTEGER DEFAULT 0,
    PRIMARY KEY (portfolio_id, technology_id)
);

-- Technology assessments table
CREATE TABLE IF NOT EXISTS technology_assessments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    technology_id UUID REFERENCES technologies(id) ON DELETE CASCADE,
    portfolio_id UUID REFERENCES portfolios(id) ON DELETE CASCADE,
    assessment_date DATE NOT NULL,
    security_score INTEGER CHECK (security_score >= 0 AND security_score <= 100),
    performance_score INTEGER CHECK (performance_score >= 0 AND performance_score <= 100),
    maintainability_score INTEGER CHECK (maintainability_score >= 0 AND maintainability_score <= 100),
    cost_efficiency_score INTEGER CHECK (cost_efficiency_score >= 0 AND cost_efficiency_score <= 100),
    overall_score INTEGER CHECK (overall_score >= 0 AND overall_score <= 100),
    notes TEXT,
    assessed_by VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert sample organizations
INSERT INTO organizations (name, description) VALUES 
    ('Engineering', 'Software Engineering Division'),
    ('Data Science', 'Data Science and Analytics Division'),
    ('DevOps', 'DevOps and Infrastructure Division'),
    ('Security', 'Information Security Division')
ON CONFLICT DO NOTHING;

-- Insert sample technologies
INSERT INTO technologies (name, description, version, vendor, category, maturity_level, lifecycle_stage) VALUES 
    ('Spring Boot', 'Java-based framework for building microservices', '3.4', 'VMware', 'Framework', 'MATURE', 'ACTIVE'),
    ('PostgreSQL', 'Advanced open source relational database', '15', 'PostgreSQL Global Development Group', 'Database', 'MATURE', 'ACTIVE'),
    ('Kotlin', 'Modern programming language for JVM', '1.9', 'JetBrains', 'Language', 'MATURE', 'ACTIVE'),
    ('Docker', 'Container platform', '24.0', 'Docker Inc', 'Platform', 'MATURE', 'ACTIVE'),
    ('Kubernetes', 'Container orchestration platform', '1.28', 'CNCF', 'Platform', 'MATURE', 'ACTIVE'),
    ('Redis', 'In-memory data structure store', '7.0', 'Redis Ltd', 'Database', 'MATURE', 'ACTIVE'),
    ('Prometheus', 'Monitoring and alerting toolkit', '2.45', 'CNCF', 'Monitoring', 'MATURE', 'ACTIVE'),
    ('Grafana', 'Analytics and monitoring platform', '10.0', 'Grafana Labs', 'Monitoring', 'MATURE', 'ACTIVE'),
    ('SimpleSAMLphp', 'SAML Identity Provider', '2.0', 'SimpleSAMLphp', 'Security', 'MATURE', 'ACTIVE'),
    ('Eureka', 'Service discovery server', '3.0', 'Netflix/Spring', 'Infrastructure', 'MATURE', 'ACTIVE')
ON CONFLICT DO NOTHING;

-- Insert sample portfolios
INSERT INTO portfolios (name, description, organization_id, portfolio_type, created_by) 
SELECT 
    'Microservices Platform',
    'Core microservices platform for enterprise applications',
    o.id,
    'PLATFORM',
    'system'
FROM organizations o WHERE o.name = 'Engineering'
ON CONFLICT DO NOTHING;

INSERT INTO portfolios (name, description, organization_id, portfolio_type, created_by) 
SELECT 
    'Data Analytics Stack',
    'Technologies for data processing and analytics',
    o.id,
    'APPLICATION',
    'system'
FROM organizations o WHERE o.name = 'Data Science'
ON CONFLICT DO NOTHING;

INSERT INTO portfolios (name, description, organization_id, portfolio_type, created_by) 
SELECT 
    'Infrastructure Platform',
    'Core infrastructure and deployment technologies',
    o.id,
    'INFRASTRUCTURE',
    'system'
FROM organizations o WHERE o.name = 'DevOps'
ON CONFLICT DO NOTHING;

-- Link technologies to portfolios
INSERT INTO portfolio_technologies (portfolio_id, technology_id, usage_context, adoption_date, business_criticality)
SELECT p.id, t.id, 'Core framework for microservices', CURRENT_DATE, 'HIGH'
FROM portfolios p, technologies t 
WHERE p.name = 'Microservices Platform' AND t.name = 'Spring Boot'
ON CONFLICT DO NOTHING;

INSERT INTO portfolio_technologies (portfolio_id, technology_id, usage_context, adoption_date, business_criticality)
SELECT p.id, t.id, 'Primary database for applications', CURRENT_DATE, 'HIGH'
FROM portfolios p, technologies t 
WHERE p.name = 'Microservices Platform' AND t.name = 'PostgreSQL'
ON CONFLICT DO NOTHING;

INSERT INTO portfolio_technologies (portfolio_id, technology_id, usage_context, adoption_date, business_criticality)
SELECT p.id, t.id, 'Primary programming language', CURRENT_DATE, 'HIGH'
FROM portfolios p, technologies t 
WHERE p.name = 'Microservices Platform' AND t.name = 'Kotlin'
ON CONFLICT DO NOTHING;

INSERT INTO portfolio_technologies (portfolio_id, technology_id, usage_context, adoption_date, business_criticality)
SELECT p.id, t.id, 'Container orchestration', CURRENT_DATE, 'HIGH'
FROM portfolios p, technologies t 
WHERE p.name = 'Infrastructure Platform' AND t.name = 'Kubernetes'
ON CONFLICT DO NOTHING;

INSERT INTO portfolio_technologies (portfolio_id, technology_id, usage_context, adoption_date, business_criticality)
SELECT p.id, t.id, 'Containerization platform', CURRENT_DATE, 'HIGH'
FROM portfolios p, technologies t 
WHERE p.name = 'Infrastructure Platform' AND t.name = 'Docker'
ON CONFLICT DO NOTHING; 