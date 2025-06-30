CREATE SCHEMA IF NOT EXISTS public;

SET
search_path TO public;

CREATE SEQUENCE IF NOT EXISTS tutorials_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE IF NOT EXISTS tutorials
(
    id
    BIGINT
    PRIMARY
    KEY
    DEFAULT
    nextval
(
    'tutorials_seq'
),
    tenant_id VARCHAR
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