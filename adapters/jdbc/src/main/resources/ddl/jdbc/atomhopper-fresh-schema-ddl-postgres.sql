-- This file can be used to create fresh AH DB schema.
-- It is useful when we create new feeds. 
--
-- This file needs to be kept up-to-date, everytime we change 
-- AH schema.
--
-- You will have to run this SQL as the schema user.
-- For example, if you are adding a new feed called 'new_feed'
-- you will have to run psql like this:
--     psql -h localhost -U new_feed -d new_feed -f <this file>
--

SET statement_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

CREATE TABLE entries (
    id bigserial,
    entryid text NOT NULL,
    creationdate timestamp without time zone NOT NULL,
    datelastupdated timestamp without time zone NOT NULL,
    entrybody text,
    feed text,
    categories character varying[],
    PRIMARY KEY(datelastupdated, id)
);
CREATE INDEX entryid_idx on entries(entryid);
CREATE INDEX categories_idx on entries(categories);
CREATE INDEX feed_idx on entries(feed);
CREATE INDEX feed_entryid_idx on entries(feed, entryid);

