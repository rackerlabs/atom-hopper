BEGIN;

ALTER TABLE entries ADD COLUMN eventtype text;
ALTER TABLE entries ADD COLUMN tenantid text;

CREATE INDEX eventtype_idx on entries( eventtype );
CREATE INDEX tenantid_idx on entries( tenantid );

COMMIT;