
DROP TABLE IF EXISTS workObjectV2 CASCADE;
DROP TABLE IF EXISTS workContainsV2 CASCADE;
DROP TABLE IF EXISTS cacheV2 CASCADE;

DROP VIEW IF EXISTS workContains;
DROP VIEW IF EXISTS workObject;
CREATE VIEW workContains AS SELECT * FROM workContainsV4;
CREATE VIEW workObject AS SELECT * FROM workObjectV4;
