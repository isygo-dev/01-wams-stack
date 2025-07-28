--DROP SCHEMA IF EXISTS PUBLIC;
-- Create model (optional in H2, but allowed)
CREATE SCHEMA IF NOT EXISTS public;

SET SCHEMA public;

-- Table: account
CREATE TABLE IF NOT EXISTS account (
                                       id BIGINT NOT NULL,
                                       create_date TIMESTAMP,
                                       created_by VARCHAR(255),
    update_date TIMESTAMP,
    updated_by VARCHAR(255),
    email VARCHAR(255) NOT NULL,
    login VARCHAR(255) NOT NULL,
    pass_key VARCHAR(255) NOT NULL,
    tenant_id VARCHAR(255) NOT NULL,
    CONSTRAINT account_pkey PRIMARY KEY (id),
    CONSTRAINT uk_account_login UNIQUE (login)
    );

-- Table: contract
CREATE TABLE IF NOT EXISTS contract (
                                        id BIGINT NOT NULL,
                                        create_date TIMESTAMP,
                                        created_by VARCHAR(255),
    update_date TIMESTAMP,
    updated_by VARCHAR(255),
    active BOOLEAN NOT NULL,
    code VARCHAR(255) NOT NULL,
    description VARCHAR(255) NOT NULL,
    end_date TIMESTAMP NOT NULL,
    start_date TIMESTAMP NOT NULL,
    tenant_id VARCHAR(255) NOT NULL,
    title VARCHAR(255) NOT NULL,
    CONSTRAINT contract_pkey PRIMARY KEY (id)
    );

-- Table: contract_file
CREATE TABLE IF NOT EXISTS contract_file (
                                             extension VARCHAR(255),
    file_name VARCHAR(255),
    original_name VARCHAR(255),
    path VARCHAR(255) DEFAULT 'NA',
    type VARCHAR(255),
    id BIGINT NOT NULL,
    create_date TIMESTAMP,
    created_by VARCHAR(255),
    update_date TIMESTAMP,
    updated_by VARCHAR(255),
    code VARCHAR(20),
    CONSTRAINT contract_file_pkey PRIMARY KEY (id),
    CONSTRAINT fk_contract_file_contract FOREIGN KEY (id)
    REFERENCES contract (id)
    ON UPDATE NO ACTION
    ON DELETE NO ACTION
    );

-- Table: contract_file_tags
CREATE TABLE IF NOT EXISTS contract_file_tags (
                                                  contract BIGINT NOT NULL,
                                                  tag_owner VARCHAR(255),
    CONSTRAINT fk_tags_ref_contract_file FOREIGN KEY (contract)
    REFERENCES contract_file (id)
    ON UPDATE NO ACTION
    ON DELETE NO ACTION
    );

-- Table: events
CREATE TABLE IF NOT EXISTS events (
                                      id BIGINT NOT NULL,
                                      create_date TIMESTAMP,
                                      created_by VARCHAR(255),
    update_date TIMESTAMP,
    updated_by VARCHAR(255),
    attributes JSON,
    element_type VARCHAR(255) NOT NULL,
    tenant_id VARCHAR(255) NOT NULL,
    CONSTRAINT events_pkey PRIMARY KEY (id)
    );

-- Table: resume
CREATE TABLE IF NOT EXISTS resume (
                                      id BIGINT NOT NULL,
                                      create_date TIMESTAMP,
                                      created_by VARCHAR(255),
    update_date TIMESTAMP,
    updated_by VARCHAR(255),
    active BOOLEAN NOT NULL,
    code VARCHAR(255) NOT NULL,
    description VARCHAR(255) NOT NULL,
    end_date TIMESTAMP NOT NULL,
    image_path VARCHAR(255),
    start_date TIMESTAMP NOT NULL,
    tenant_id VARCHAR(255) NOT NULL,
    title VARCHAR(255) NOT NULL,
    CONSTRAINT resume_pkey PRIMARY KEY (id)
    );

-- Table: resume_file
CREATE TABLE IF NOT EXISTS resume_file (
                                           extension VARCHAR(255),
    file_name VARCHAR(255),
    original_name VARCHAR(255),
    path VARCHAR(255) DEFAULT 'NA',
    type VARCHAR(255),
    id BIGINT NOT NULL,
    create_date TIMESTAMP,
    created_by VARCHAR(255),
    update_date TIMESTAMP,
    updated_by VARCHAR(255),
    code VARCHAR(20),
    CONSTRAINT resume_file_pkey PRIMARY KEY (id),
    CONSTRAINT fk_resume_file_resume FOREIGN KEY (id)
    REFERENCES resume (id)
    ON UPDATE NO ACTION
    ON DELETE NO ACTION
    );

-- Table: resume_file_tags
CREATE TABLE IF NOT EXISTS resume_file_tags (
                                                resume BIGINT NOT NULL,
                                                tag_owner VARCHAR(255),
    CONSTRAINT fk_tags_ref_resume_file FOREIGN KEY (resume)
    REFERENCES resume_file (id)
    ON UPDATE NO ACTION
    ON DELETE NO ACTION
    );

-- Table: resume_linked_file
CREATE TABLE IF NOT EXISTS resume_linked_file (
                                                  id BIGINT NOT NULL,
                                                  create_date TIMESTAMP,
                                                  created_by VARCHAR(255),
    update_date TIMESTAMP,
    updated_by VARCHAR(255),
    code VARCHAR(20),
    extension VARCHAR(8) DEFAULT 'NA',
    file_name VARCHAR(128) DEFAULT 'NA',
    original_name VARCHAR(128),
    path VARCHAR(255) NOT NULL DEFAULT 'NA',
    type VARCHAR(80),
    crc_16 BIGINT,
    crc_32 BIGINT,
    mimetype VARCHAR(255),
    size BIGINT NOT NULL,
    version BIGINT,
    tenant_id VARCHAR(255) NOT NULL,
    resume BIGINT,
    CONSTRAINT resume_linked_file_pkey PRIMARY KEY (id),
    CONSTRAINT fk_additional_file_ref_resume FOREIGN KEY (resume)
    REFERENCES resume (id)
    ON UPDATE NO ACTION
    ON DELETE NO ACTION
    );

-- Table: t_app_next_code
CREATE TABLE IF NOT EXISTS t_app_next_code (
                                               id BIGINT NOT NULL,
                                               attribute VARCHAR(255) NOT NULL,
    code_value BIGINT NOT NULL,
    entity VARCHAR(255) NOT NULL,
    increment INTEGER NOT NULL,
    prefix VARCHAR(20),
    suffix VARCHAR(20),
    tenant VARCHAR(100) NOT NULL DEFAULT 'default-tenant',
    value_length BIGINT NOT NULL,
    CONSTRAINT t_app_next_code_pkey PRIMARY KEY (id),
    CONSTRAINT uc_next_code_entity UNIQUE (entity, attribute, tenant)
    );

-- Table: time_line
CREATE TABLE IF NOT EXISTS time_line (
                                         id BIGINT NOT NULL,
                                         attributes JSON,
                                         elementid VARCHAR(255) NOT NULL,
    element_type VARCHAR(255) NOT NULL,
    eventtype VARCHAR(255) NOT NULL,
    modifiedby VARCHAR(255),
    timestamp TIMESTAMP NOT NULL,
    tenant_id VARCHAR(255) NOT NULL,
    CONSTRAINT time_line_pkey PRIMARY KEY (id),
    CONSTRAINT time_line_eventtype_check CHECK (eventtype IN ('CREATED', 'UPDATED', 'DELETED'))
    );

-- Table: tutorials
CREATE TABLE IF NOT EXISTS tutorials (
                                         id BIGINT NOT NULL,
                                         create_date TIMESTAMP,
                                         created_by VARCHAR(255),
    update_date TIMESTAMP,
    updated_by VARCHAR(255),
    description VARCHAR(255),
    published BOOLEAN,
    tenant_id VARCHAR(255) NOT NULL,
    title VARCHAR(255),
    CONSTRAINT tutorials_pkey PRIMARY KEY (id)
    );

-- Table: user_details
CREATE TABLE IF NOT EXISTS user_details (
                                            id BIGINT NOT NULL,
                                            create_date TIMESTAMP,
                                            created_by VARCHAR(255),
    update_date TIMESTAMP,
    updated_by VARCHAR(255),
    active BOOLEAN NOT NULL,
    code VARCHAR(255) NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    image_path VARCHAR(255),
    last_name VARCHAR(255) NOT NULL,
    tenant_id VARCHAR(255) NOT NULL,
    CONSTRAINT user_details_pkey PRIMARY KEY (id)
    );

-- Sequences
CREATE SEQUENCE IF NOT EXISTS account_sequence
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807;

CREATE SEQUENCE IF NOT EXISTS contract_file_sequence
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807;

CREATE SEQUENCE IF NOT EXISTS contract_sequence
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807;

CREATE SEQUENCE IF NOT EXISTS events_sequence
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807;

CREATE SEQUENCE IF NOT EXISTS next_code_sequence
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807;

CREATE SEQUENCE IF NOT EXISTS resume_file_sequence
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807;

CREATE SEQUENCE IF NOT EXISTS resume_multi_file_sequence
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807;

CREATE SEQUENCE IF NOT EXISTS resume_sequence
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807;

CREATE SEQUENCE IF NOT EXISTS timeline_event_sequence
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807;

CREATE SEQUENCE IF NOT EXISTS tutorials_sequence
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807;

CREATE SEQUENCE IF NOT EXISTS user_sequence
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807;