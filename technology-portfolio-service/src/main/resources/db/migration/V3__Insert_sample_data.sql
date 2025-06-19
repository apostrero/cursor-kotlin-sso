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