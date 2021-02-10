-- Due to JSON schema change (priorityKeys)
-- New data should be built into new tables
-- This allows old service/worker to continue while building new
-- work objects

CREATE TABLE workObjectV4 (
  version INTEGER NOT NULL DEFAULT 0,
  persistentWorkId TEXT NOT NULL PRIMARY KEY,
  corepoWorkId TEXT NOT NULL,
  modified TIMESTAMP NOT NULL DEFAULT timeofday()::timestamp,
  content JSONB NOT NULL
);

CREATE TABLE workContainsV4 (
  version INTEGER NOT NULL DEFAULT 0,
  corepoWorkId TEXT NOT NULL,
  manifestationId TEXT NOT NULL PRIMARY KEY
);

CREATE INDEX workContainsV4_corepoWorkId ON workContainsV4(corepoWorkId);

CREATE TABLE cacheV4 (
  version INTEGER NOT NULL DEFAULT 0,
  manifestationId TEXT NOT NULL PRIMARY KEY,
  modified TIMESTAMP NOT NULL DEFAULT timeofday()::timestamp,
  content JSONB NOT NULL
);

CREATE UNIQUE INDEX workobjectV4_corepoworkid ON workobjectV4( corepoworkid );

DROP VIEW IF EXISTS workContains;
DROP VIEW IF EXISTS workObject;
CREATE VIEW workContains AS SELECT * FROM workContainsV4;
CREATE VIEW workObject AS SELECT * FROM workObjectV4;
