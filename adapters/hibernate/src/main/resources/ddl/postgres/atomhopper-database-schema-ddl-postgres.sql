--
-- PostgreSQL database dump
--

SET statement_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

--
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


SET search_path = public, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: categories; Type: TABLE; Schema: public; Owner: atomschema; Tablespace: 
--

CREATE TABLE categories (
    term character varying(255) NOT NULL
);


ALTER TABLE public.categories OWNER TO atomschema;

--
-- Name: categoryentryreferences; Type: TABLE; Schema: public; Owner: atomschema; Tablespace: 
--

CREATE TABLE categoryentryreferences (
    entryid character varying(255) NOT NULL,
    category character varying(255) NOT NULL
);


ALTER TABLE public.categoryentryreferences OWNER TO atomschema;

--
-- Name: entries; Type: TABLE; Schema: public; Owner: atomschema; Tablespace: 
--

CREATE TABLE entries (
    entryid character varying(255) NOT NULL,
    creationdate timestamp without time zone NOT NULL,
    datelastupdated timestamp without time zone NOT NULL,
    entrybody text,
    feed character varying(255)
);


ALTER TABLE public.entries OWNER TO atomschema;

--
-- Name: feeds; Type: TABLE; Schema: public; Owner: atomschema; Tablespace: 
--

CREATE TABLE feeds (
    name character varying(255) NOT NULL,
    feedid character varying(255)
);


ALTER TABLE public.feeds OWNER TO atomschema;

--
-- Name: categories_pkey; Type: CONSTRAINT; Schema: public; Owner: atomschema; Tablespace: 
--

ALTER TABLE ONLY categories
    ADD CONSTRAINT categories_pkey PRIMARY KEY (term);


--
-- Name: categoryentryreferences_pkey; Type: CONSTRAINT; Schema: public; Owner: atomschema; Tablespace: 
--

ALTER TABLE ONLY categoryentryreferences
    ADD CONSTRAINT categoryentryreferences_pkey PRIMARY KEY (entryid, category);


--
-- Name: entries_pkey; Type: CONSTRAINT; Schema: public; Owner: atomschema; Tablespace: 
--

ALTER TABLE ONLY entries
    ADD CONSTRAINT entries_pkey PRIMARY KEY (entryid);


--
-- Name: feeds_pkey; Type: CONSTRAINT; Schema: public; Owner: atomschema; Tablespace: 
--

ALTER TABLE ONLY feeds
    ADD CONSTRAINT feeds_pkey PRIMARY KEY (name);


--
-- Name: fk45b1c704d6f525e; Type: FK CONSTRAINT; Schema: public; Owner: atomschema
--

ALTER TABLE ONLY entries
    ADD CONSTRAINT fk_feed_feeds_name FOREIGN KEY (feed) REFERENCES feeds(name);


--
-- Name: fkef969f9ca3d8a01e; Type: FK CONSTRAINT; Schema: public; Owner: atomschema
--

ALTER TABLE ONLY categoryentryreferences
    ADD CONSTRAINT fk_category_categories_term FOREIGN KEY (category) REFERENCES categories(term);


--
-- Name: fkef969f9cfbc7269d; Type: FK CONSTRAINT; Schema: public; Owner: atomschema
--

ALTER TABLE ONLY categoryentryreferences
    ADD CONSTRAINT fk_entryid_entries_entryid FOREIGN KEY (entryid) REFERENCES entries(entryid);


--
-- Name: public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;


--
-- PostgreSQL database dump complete
--