-- Creating model for multi-tenancy
CREATE SCHEMA IF NOT EXISTS public;

-- Set model to tenants
SET SCHEMA public;

-- Creating sequence for USER_DETAILS table
CREATE SEQUENCE IF NOT EXISTS user_seq
    START WITH 1
    INCREMENT BY 1
    CACHE 1;

-- Creating sequence for ACCOUNT table
CREATE SEQUENCE IF NOT EXISTS account_seq
    START WITH 1
    INCREMENT BY 1
    CACHE 1;

-- Creating sequence for T_APP_NEXT_CODE table
CREATE SEQUENCE IF NOT EXISTS next_code_sequence
    START WITH 1
    INCREMENT BY 1
    CACHE 1;

-- Creating USER_DETAILS table
CREATE TABLE IF NOT EXISTS "USER_DETAILS"
(
    id
    BIGINT
    PRIMARY
    KEY
    DEFAULT
    NEXT
    VALUE
    FOR
    user_seq,
    TENANT_ID
    VARCHAR
(
    255
) NOT NULL,
    CODE VARCHAR
(
    255
) NOT NULL,
    FIRST_NAME VARCHAR
(
    255
) NOT NULL,
    LAST_NAME VARCHAR
(
    255
) NOT NULL,
    ACTIVE BOOLEAN NOT NULL DEFAULT FALSE,
    IMAGE_PATH VARCHAR
(
    255
),
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

-- Creating ACCOUNT table
CREATE TABLE IF NOT EXISTS ACCOUNT
(
    id
    BIGINT
    PRIMARY
    KEY
    DEFAULT
    NEXT
    VALUE
    FOR
    account_seq,
    TENANT_ID
    VARCHAR
(
    255
) NOT NULL,
    LOGIN VARCHAR
(
    255
) NOT NULL,
    EMAIL VARCHAR
(
    255
) NOT NULL,
    PASS_KEY VARCHAR
(
    255
) NOT NULL,
    create_date TIMESTAMP,
    created_by VARCHAR
(
    255
),
    update_date TIMESTAMP,
    updated_by VARCHAR
(
    255
),
    CONSTRAINT uc_account_login UNIQUE
(
    LOGIN
)
    );

-- Creating T_APP_NEXT_CODE table
CREATE TABLE IF NOT EXISTS T_APP_NEXT_CODE
(
    id
    BIGINT
    PRIMARY
    KEY
    DEFAULT
    NEXT
    VALUE
    FOR
    next_code_sequence,
    C_ENTITY
    VARCHAR
(
    255
) NOT NULL,
    C_ATTRIBUTE VARCHAR
(
    255
) NOT NULL,
    C_TENANT VARCHAR
(
    255
) NOT NULL DEFAULT 'TENANT_DEFAULT',
    C_PREFIX VARCHAR
(
    255
),
    C_SUFFIX VARCHAR
(
    255
),
    C_VALUE BIGINT NOT NULL DEFAULT 0,
    C_VALUE_LENGTH BIGINT NOT NULL DEFAULT 6,
    C_INCREMENT INTEGER NOT NULL DEFAULT 1,
    create_date TIMESTAMP,
    created_by VARCHAR
(
    255
),
    update_date TIMESTAMP,
    updated_by VARCHAR
(
    255
),
    CONSTRAINT uc_next_code_entity UNIQUE
(
    C_ENTITY,
    C_ATTRIBUTE,
    C_TENANT
)
    );