-- Create schema (optional in H2, but allowed)
CREATE SCHEMA IF NOT EXISTS public;

-- Set schema (H2 supports SET SCHEMA in PostgreSQL mode)
SET SCHEMA public;

-- Create sequence (simplified, H2 ignores NO MIN/MAX)
CREATE SEQUENCE IF NOT EXISTS tutorials_seq
    START WITH 1
    INCREMENT BY 1
    CACHE 1;

-- Create table (H2 uses NEXT VALUE FOR instead of nextval('...'))
CREATE TABLE IF NOT EXISTS tutorials
(
    id
    BIGINT
    DEFAULT
    NEXT
    VALUE
    FOR
    tutorials_seq
    PRIMARY
    KEY,
    tenant_id
    VARCHAR
(
    255
) NOT NULL,
    title VARCHAR
(
    255
),
    description VARCHAR
(
    1000
),
    published BOOLEAN,
    create_date TIMESTAMP,
    created_by VARCHAR
(
    255
),
    update_date TIMESTAMP,
    updated_by VARCHAR
(
    255
)
    );
