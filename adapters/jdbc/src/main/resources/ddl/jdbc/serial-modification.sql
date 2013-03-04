BEGIN;

ALTER TABLE entries ADD id bigserial;
ALTER TABLE entries DROP CONSTRAINT entries_pkey;
ALTER TABLE entries ADD PRIMARY KEY (datelastupdated, id);

COMMIT;
