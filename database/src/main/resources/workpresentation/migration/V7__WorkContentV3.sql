-- Due to JSON schema change (relations)
-- New data should be built into new tables
-- This allows old service/worker to continue while building new
-- work objects

CREATE TABLE workObjectV3 (
  version INTEGER NOT NULL DEFAULT 0,
  persistentWorkId TEXT NOT NULL PRIMARY KEY,
  corepoWorkId TEXT NOT NULL,
  modified TIMESTAMP NOT NULL DEFAULT timeofday()::timestamp,
  content JSONB NOT NULL
);

CREATE TABLE workContainsV3 (
  version INTEGER NOT NULL DEFAULT 0,
  corepoWorkId TEXT NOT NULL,
  manifestationId TEXT NOT NULL PRIMARY KEY
);

CREATE INDEX workContainsV3_corepoWorkId ON workContainsV3(corepoWorkId);

CREATE TABLE cacheV3 (
  version INTEGER NOT NULL DEFAULT 0,
  manifestationId TEXT NOT NULL PRIMARY KEY,
  modified TIMESTAMP NOT NULL DEFAULT timeofday()::timestamp,
  content JSONB NOT NULL
);

CREATE UNIQUE INDEX workobjectV3_corepoworkid ON workobjectV3( corepoworkid );

DROP VIEW IF EXISTS workContains;
DROP VIEW IF EXISTS workObject;
CREATE VIEW workContains AS SELECT * FROM workContainsV3;
CREATE VIEW workObject AS SELECT * FROM workObjectV3;
