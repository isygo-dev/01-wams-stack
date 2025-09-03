DROP SCHEMA public CASCADE;
CREATE SCHEMA IF NOT EXISTS public;

SET
search_path TO public;

-- Table: public.account

-- DROP TABLE IF EXISTS public.account;

CREATE TABLE IF NOT EXISTS public.account
(
    id
    bigint
    NOT
    NULL,
    create_date
    timestamp
(
    6
) without time zone,
    created_by character varying
(
    255
) COLLATE pg_catalog."default",
    update_date timestamp
(
    6
)
  without time zone,
    updated_by character varying
(
    255
) COLLATE pg_catalog."default",
    email character varying
(
    255
) COLLATE pg_catalog."default" NOT NULL,
    login character varying
(
    255
) COLLATE pg_catalog."default" NOT NULL,
    pass_key character varying
(
    255
) COLLATE pg_catalog."default" NOT NULL,
    tenant_id character varying
(
    255
) COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT account_pkey PRIMARY KEY
(
    id
),
    CONSTRAINT uk_6hblpild7y6if5b0eu51s96ca UNIQUE
(
    login
)
    )
    TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.account
    OWNER to postgres;

-- Table: public.contract

-- DROP TABLE IF EXISTS public.contract;

CREATE TABLE IF NOT EXISTS public.contract
(
    id
    bigint
    NOT
    NULL,
    create_date
    timestamp
(
    6
) without time zone,
    created_by character varying
(
    255
) COLLATE pg_catalog."default",
    update_date timestamp
(
    6
)
  without time zone,
    updated_by character varying
(
    255
) COLLATE pg_catalog."default",
    active boolean NOT NULL,
    code character varying
(
    255
) COLLATE pg_catalog."default" NOT NULL,
    description character varying
(
    255
) COLLATE pg_catalog."default" NOT NULL,
    end_date timestamp
(
    6
)
  without time zone NOT NULL,
    start_date timestamp
(
    6
)
  without time zone NOT NULL,
    tenant_id character varying
(
    255
) COLLATE pg_catalog."default" NOT NULL,
    title character varying
(
    255
) COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT contract_pkey PRIMARY KEY
(
    id
)
    )
    TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.contract
    OWNER to postgres;

-- Table: public.contract_file

-- DROP TABLE IF EXISTS public.contract_file;

CREATE TABLE IF NOT EXISTS public.contract_file
(
    extension
    character
    varying
(
    255
) COLLATE pg_catalog."default",
    file_name character varying
(
    255
) COLLATE pg_catalog."default",
    original_name character varying
(
    255
) COLLATE pg_catalog."default",
    path character varying
(
    255
) COLLATE pg_catalog."default" DEFAULT 'NA':: character varying,
    type character varying
(
    255
) COLLATE pg_catalog."default",
    id bigint NOT NULL,
    create_date timestamp
(
    6
) without time zone,
    created_by character varying
(
    255
) COLLATE pg_catalog."default",
    update_date timestamp
(
    6
)
  without time zone,
    updated_by character varying
(
    255
) COLLATE pg_catalog."default",
    code character varying
(
    20
) COLLATE pg_catalog."default",
    CONSTRAINT contract_file_pkey PRIMARY KEY
(
    id
),
    CONSTRAINT fk6k9evso3eggjfivi35ngxr9m4 FOREIGN KEY
(
    id
)
    REFERENCES public.contract
(
    id
) MATCH SIMPLE
  ON UPDATE NO ACTION
  ON DELETE NO ACTION
    )
    TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.contract_file
    OWNER to postgres;

-- Table: public.contract_file_tags

-- DROP TABLE IF EXISTS public.contract_file_tags;

CREATE TABLE IF NOT EXISTS public.contract_file_tags
(
    contract
    bigint
    NOT
    NULL,
    tag_owner
    character
    varying
(
    255
) COLLATE pg_catalog."default",
    CONSTRAINT fk_tags_ref_contract_file FOREIGN KEY
(
    contract
)
    REFERENCES public.contract_file
(
    id
) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE NO ACTION
    )
    TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.contract_file_tags
    OWNER to postgres;

-- Table: public.events

-- DROP TABLE IF EXISTS public.events;

CREATE TABLE IF NOT EXISTS public.events
(
    id
    bigint
    NOT
    NULL,
    create_date
    timestamp
(
    6
) without time zone,
    created_by character varying
(
    255
) COLLATE pg_catalog."default",
    update_date timestamp
(
    6
)
  without time zone,
    updated_by character varying
(
    255
) COLLATE pg_catalog."default",
    attributes jsonb,
    element_type character varying
(
    255
) COLLATE pg_catalog."default" NOT NULL,
    tenant_id character varying
(
    255
) COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT events_pkey PRIMARY KEY
(
    id
)
    )
    TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.events
    OWNER to postgres;

-- Table: public.resume

-- DROP TABLE IF EXISTS public.resume;

CREATE TABLE IF NOT EXISTS public.resume
(
    id
    bigint
    NOT
    NULL,
    create_date
    timestamp
(
    6
) without time zone,
    created_by character varying
(
    255
) COLLATE pg_catalog."default",
    update_date timestamp
(
    6
)
  without time zone,
    updated_by character varying
(
    255
) COLLATE pg_catalog."default",
    active boolean NOT NULL,
    code character varying
(
    255
) COLLATE pg_catalog."default" NOT NULL,
    description character varying
(
    255
) COLLATE pg_catalog."default" NOT NULL,
    end_date timestamp
(
    6
)
  without time zone NOT NULL,
    image_path character varying
(
    255
) COLLATE pg_catalog."default",
    start_date timestamp
(
    6
)
  without time zone NOT NULL,
    tenant_id character varying
(
    255
) COLLATE pg_catalog."default" NOT NULL,
    title character varying
(
    255
) COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT resume_pkey PRIMARY KEY
(
    id
)
    )
    TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.resume
    OWNER to postgres;

-- Table: public.resume_file

-- DROP TABLE IF EXISTS public.resume_file;

CREATE TABLE IF NOT EXISTS public.resume_file
(
    extension
    character
    varying
(
    255
) COLLATE pg_catalog."default",
    file_name character varying
(
    255
) COLLATE pg_catalog."default",
    original_name character varying
(
    255
) COLLATE pg_catalog."default",
    path character varying
(
    255
) COLLATE pg_catalog."default" DEFAULT 'NA':: character varying,
    type character varying
(
    255
) COLLATE pg_catalog."default",
    id bigint NOT NULL,
    create_date timestamp
(
    6
) without time zone,
    created_by character varying
(
    255
) COLLATE pg_catalog."default",
    update_date timestamp
(
    6
)
  without time zone,
    updated_by character varying
(
    255
) COLLATE pg_catalog."default",
    code character varying
(
    20
) COLLATE pg_catalog."default",
    CONSTRAINT resume_file_pkey PRIMARY KEY
(
    id
),
    CONSTRAINT fk9rhnkv7e5ho3o1gqwrb3c2sdo FOREIGN KEY
(
    id
)
    REFERENCES public.resume
(
    id
) MATCH SIMPLE
  ON UPDATE NO ACTION
  ON DELETE NO ACTION
    )
    TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.resume_file
    OWNER to postgres;

-- Table: public.resume_file_tags

-- DROP TABLE IF EXISTS public.resume_file_tags;

CREATE TABLE IF NOT EXISTS public.resume_file_tags
(
    resume
    bigint
    NOT
    NULL,
    tag_owner
    character
    varying
(
    255
) COLLATE pg_catalog."default",
    CONSTRAINT fk_tags_ref_resume_file FOREIGN KEY
(
    resume
)
    REFERENCES public.resume_file
(
    id
) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE NO ACTION
    )
    TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.resume_file_tags
    OWNER to postgres;

-- Table: public.resume_linked_file

-- DROP TABLE IF EXISTS public.resume_linked_file;

CREATE TABLE IF NOT EXISTS public.resume_linked_file
(
    id
    bigint
    NOT
    NULL,
    create_date
    timestamp
(
    6
) without time zone,
    created_by character varying
(
    255
) COLLATE pg_catalog."default",
    update_date timestamp
(
    6
)
  without time zone,
    updated_by character varying
(
    255
) COLLATE pg_catalog."default",
    code character varying
(
    20
) COLLATE pg_catalog."default",
    extension character varying
(
    8
) COLLATE pg_catalog."default" DEFAULT 'NA':: character varying,
    file_name character varying
(
    128
) COLLATE pg_catalog."default" DEFAULT 'NA':: character varying,
    original_name character varying
(
    128
) COLLATE pg_catalog."default",
    path character varying
(
    255
) COLLATE pg_catalog."default" NOT NULL DEFAULT 'NA':: character varying,
    type character varying
(
    80
) COLLATE pg_catalog."default",
    crc_16 bigint,
    crc_32 bigint,
    mimetype character varying
(
    255
) COLLATE pg_catalog."default",
    size bigint NOT NULL,
    version bigint,
    tenant_id character varying
(
    255
) COLLATE pg_catalog."default" NOT NULL,
    resume bigint,
    CONSTRAINT resume_linked_file_pkey PRIMARY KEY
(
    id
),
    CONSTRAINT fk_additional_file_ref_resume FOREIGN KEY
(
    resume
)
    REFERENCES public.resume
(
    id
) MATCH SIMPLE
  ON UPDATE NO ACTION
  ON DELETE NO ACTION
    )
    TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.resume_linked_file
    OWNER to postgres;

-- Table: public.t_app_next_code

-- DROP TABLE IF EXISTS public.t_app_next_code;

CREATE TABLE IF NOT EXISTS public.t_app_next_code
(
    id
    bigint
    NOT
    NULL,
    attribute
    character
    varying
(
    255
) COLLATE pg_catalog."default" NOT NULL,
    code_value bigint NOT NULL,
    entity character varying
(
    255
) COLLATE pg_catalog."default" NOT NULL,
    increment integer NOT NULL,
    prefix character varying
(
    20
) COLLATE pg_catalog."default",
    suffix character varying
(
    20
) COLLATE pg_catalog."default",
    tenant character varying
(
    100
) COLLATE pg_catalog."default" NOT NULL DEFAULT 'default-tenant':: character varying,
    value_length bigint NOT NULL,
    CONSTRAINT t_app_next_code_pkey PRIMARY KEY
(
    id
),
    CONSTRAINT uc_next_code_entity UNIQUE
(
    entity,
    attribute,
    tenant
)
    )
    TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.t_app_next_code
    OWNER to postgres;

-- Table: public.time_line

-- DROP TABLE IF EXISTS public.time_line;

CREATE TABLE IF NOT EXISTS public.time_line
(
    id
    bigint
    NOT
    NULL,
    attributes
    jsonb,
    elementid
    character
    varying
(
    255
) COLLATE pg_catalog."default" NOT NULL,
    element_type character varying
(
    255
) COLLATE pg_catalog."default" NOT NULL,
    eventtype character varying
(
    255
) COLLATE pg_catalog."default" NOT NULL,
    modifiedby character varying
(
    255
) COLLATE pg_catalog."default",
    "timestamp" timestamp
(
    6
) without time zone NOT NULL,
    tenant_id character varying
(
    255
) COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT time_line_pkey PRIMARY KEY
(
    id
),
    CONSTRAINT time_line_eventtype_check CHECK
(
    eventtype
    :
    :
    text =
    ANY (
    ARRAY[
    'CREATED'
    :
    :
    character
    varying,
    'UPDATED'
    :
    :
    character
    varying,
    'DELETED'
    :
    :
    character
    varying]
    :
    :
    text
[]
))
    )
    TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.time_line
    OWNER to postgres;

-- Table: public.tutorials

-- DROP TABLE IF EXISTS public.tutorials;

CREATE TABLE IF NOT EXISTS public.tutorials
(
    id
    bigint
    NOT
    NULL,
    create_date
    timestamp
(
    6
) without time zone,
    created_by character varying
(
    255
) COLLATE pg_catalog."default",
    update_date timestamp
(
    6
)
  without time zone,
    updated_by character varying
(
    255
) COLLATE pg_catalog."default",
    description character varying
(
    255
) COLLATE pg_catalog."default",
    published boolean,
    tenant_id character varying
(
    255
) COLLATE pg_catalog."default" NOT NULL,
    title character varying
(
    255
) COLLATE pg_catalog."default",
    CONSTRAINT tutorials_pkey PRIMARY KEY
(
    id
)
    )
    TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.tutorials
    OWNER to postgres;

-- Table: public.user_details

-- DROP TABLE IF EXISTS public.user_details;

CREATE TABLE IF NOT EXISTS public.user_details
(
    id
    bigint
    NOT
    NULL,
    create_date
    timestamp
(
    6
) without time zone,
    created_by character varying
(
    255
) COLLATE pg_catalog."default",
    update_date timestamp
(
    6
)
  without time zone,
    updated_by character varying
(
    255
) COLLATE pg_catalog."default",
    active boolean NOT NULL,
    code character varying
(
    255
) COLLATE pg_catalog."default" NOT NULL,
    first_name character varying
(
    255
) COLLATE pg_catalog."default" NOT NULL,
    image_path character varying
(
    255
) COLLATE pg_catalog."default",
    last_name character varying
(
    255
) COLLATE pg_catalog."default" NOT NULL,
    tenant_id character varying
(
    255
) COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT user_details_pkey PRIMARY KEY
(
    id
)
    )
    TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.user_details
    OWNER to postgres;

-- SEQUENCE: public.account_sequence

-- DROP SEQUENCE IF EXISTS public.account_sequence;

CREATE SEQUENCE IF NOT EXISTS public.account_sequence
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

ALTER SEQUENCE public.account_sequence
    OWNER TO postgres;

-- SEQUENCE: public.contract_file_sequence

-- DROP SEQUENCE IF EXISTS public.contract_file_sequence;

CREATE SEQUENCE IF NOT EXISTS public.contract_file_sequence
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

ALTER SEQUENCE public.contract_file_sequence
    OWNER TO postgres;

-- SEQUENCE: public.contract_sequence

-- DROP SEQUENCE IF EXISTS public.contract_sequence;

CREATE SEQUENCE IF NOT EXISTS public.contract_sequence
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

ALTER SEQUENCE public.contract_sequence
    OWNER TO postgres;

-- SEQUENCE: public.events_sequence

-- DROP SEQUENCE IF EXISTS public.events_sequence;

CREATE SEQUENCE IF NOT EXISTS public.events_sequence
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

ALTER SEQUENCE public.events_sequence
    OWNER TO postgres;

-- SEQUENCE: public.next_code_sequence

-- DROP SEQUENCE IF EXISTS public.next_code_sequence;

CREATE SEQUENCE IF NOT EXISTS public.next_code_sequence
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

ALTER SEQUENCE public.next_code_sequence
    OWNER TO postgres;

-- SEQUENCE: public.resume_file_sequence

-- DROP SEQUENCE IF EXISTS public.resume_file_sequence;

CREATE SEQUENCE IF NOT EXISTS public.resume_file_sequence
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

ALTER SEQUENCE public.resume_file_sequence
    OWNER TO postgres;

-- SEQUENCE: public.resume_multi_file_sequence

-- DROP SEQUENCE IF EXISTS public.resume_multi_file_sequence;

CREATE SEQUENCE IF NOT EXISTS public.resume_multi_file_sequence
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

ALTER SEQUENCE public.resume_multi_file_sequence
    OWNER TO postgres;

-- SEQUENCE: public.resume_sequence

-- DROP SEQUENCE IF EXISTS public.resume_sequence;

CREATE SEQUENCE IF NOT EXISTS public.resume_sequence
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

ALTER SEQUENCE public.resume_sequence
    OWNER TO postgres;

-- SEQUENCE: public.timeline_event_sequence

-- DROP SEQUENCE IF EXISTS public.timeline_event_sequence;

CREATE SEQUENCE IF NOT EXISTS public.timeline_event_sequence
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

ALTER SEQUENCE public.timeline_event_sequence
    OWNER TO postgres;

-- SEQUENCE: public.tutorials_sequence

-- DROP SEQUENCE IF EXISTS public.tutorials_sequence;

CREATE SEQUENCE IF NOT EXISTS public.tutorials_sequence
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

ALTER SEQUENCE public.tutorials_sequence
    OWNER TO postgres;

-- SEQUENCE: public.user_sequence

-- DROP SEQUENCE IF EXISTS public.user_sequence;

CREATE SEQUENCE IF NOT EXISTS public.user_sequence
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

ALTER SEQUENCE public.user_sequence
    OWNER TO postgres;