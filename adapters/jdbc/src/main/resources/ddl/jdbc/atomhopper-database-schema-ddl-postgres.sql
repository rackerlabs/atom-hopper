SET statement_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';

SET search_path = public, pg_catalog;
SET default_tablespace = '';
SET default_with_oids = false;

CREATE TABLE entries (
    id BIGSERIAL CONSTRAINT entries_pkey PRIMARY KEY,
    entryid character varying(255),
    creationdate timestamp without time zone NOT NULL,
    datelastupdated timestamp without time zone NOT NULL,
    entrybody character varying,
    feed character varying(255),
    categories character varying[]
)
ALTER TABLE public.entries OWNER TO atomschema;
CREATE INDEX entryid_idx on entries(entryid);
CREATE INDEX datelastupdated_idx on entries(datelastupdated);
CREATE INDEX categories_idx on entries(categories);
CREATE INDEX feed_idx on entries(feed);
CREATE INDEX feed_id_idx on entries(feed, id);

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;
