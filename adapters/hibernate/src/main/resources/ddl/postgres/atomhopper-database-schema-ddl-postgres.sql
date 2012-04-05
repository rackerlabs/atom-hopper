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

CREATE TABLE feeds (
    name character varying(255) CONSTRAINT feeds_pkey PRIMARY KEY,
    feedid character varying(255)
);
ALTER TABLE public.feeds OWNER TO atomschema;

CREATE TABLE entries (
    entryid character varying(255) CONSTRAINT entries_pkey PRIMARY KEY,
    creationdate timestamp without time zone NOT NULL,
    datelastupdated timestamp without time zone NOT NULL,
    entrybody text,
    feed character varying(255),
    CONSTRAINT fk_feed_feeds_name FOREIGN KEY (feed) REFERENCES feeds(name) ON DELETE CASCADE
);
ALTER TABLE public.entries OWNER TO atomschema;
CREATE INDEX datelastupdated_idx on entries(datelastupdated);

CREATE TABLE categories (
    term character varying(255) CONSTRAINT categories_pkey PRIMARY KEY
);
ALTER TABLE public.categories OWNER TO atomschema;

CREATE TABLE categoryentryreferences (
    entryid character varying(255) CONSTRAINT fk_entryid_entries_entryid REFERENCES entries(entryid) ON DELETE CASCADE,
    category character varying(255) CONSTRAINT fk_category_categories_term REFERENCES categories(term) ON DELETE CASCADE,
    CONSTRAINT categoryentryreferences_pkey PRIMARY KEY (entryid, category)
);
ALTER TABLE public.categoryentryreferences OWNER TO atomschema;

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;
