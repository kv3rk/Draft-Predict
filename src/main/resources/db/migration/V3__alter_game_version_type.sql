ALTER TABLE matches
ALTER
COLUMN patch TYPE text
USING patch::text;

UPDATE matches
SET patch = '16.15';