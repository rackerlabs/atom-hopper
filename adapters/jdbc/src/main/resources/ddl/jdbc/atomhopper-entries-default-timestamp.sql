BEGIN;

ALTER TABLE entries ALTER COLUMN creationdate SET DEFAULT current_timestamp;
ALTER TABLE entries ALTER COLUMN datelastupdated SET DEFAULT current_timestamp;

COMMIT;
