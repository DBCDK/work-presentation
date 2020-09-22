-- Due to JSON schema change (objects instead of strings for subjects/creators)
-- New data should be built into new tables
-- This allows old service/worker to continue while building new
-- work objects

CREATE TABLE workObjectV2 (
  version INTEGER NOT NULL DEFAULT 0,
  persistentWorkId TEXT NOT NULL PRIMARY KEY,
  corepoWorkId TEXT NOT NULL,
  modified TIMESTAMP NOT NULL DEFAULT timeofday()::timestamp,
  content JSONB NOT NULL
);

CREATE TABLE workContainsV2 (
  version INTEGER NOT NULL DEFAULT 0,
  corepoWorkId TEXT NOT NULL,
  manifestationId TEXT NOT NULL PRIMARY KEY
);

CREATE INDEX workContainsV2_corepoWorkId ON workContainsV2(corepoWorkId);

CREATE TABLE cacheV2 (
  version INTEGER NOT NULL DEFAULT 0,
  manifestationId TEXT NOT NULL PRIMARY KEY,
  modified TIMESTAMP NOT NULL DEFAULT timeofday()::timestamp,
  content JSONB NOT NULL
);
