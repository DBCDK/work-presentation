CREATE TABLE records (
  version INTEGER NOT NULL DEFAULT 0,
  persistentWorkId TEXT NOT NULL PRIMARY KEY,
  corepoWorkId TEXT NOT NULL,
  modified TIMESTAMP NOT NULL DEFAULT timeofday()::timestamp,
  content JSONB NOT NULL
);

CREATE UNIQUE INDEX records_corepoWorkId ON records(corepoWorkId);

CREATE TABLE workContains (
  version INTEGER NOT NULL DEFAULT 0,
  corepoWorkId TEXT NOT NULL,
  unitId TEXT NOT NULL,
  manifestationId TEXT NOT NULL PRIMARY KEY
);

CREATE INDEX workContains_corepoWorkId ON workContains(corepoWorkId);

CREATE TABLE cache (
  version INTEGER NOT NULL DEFAULT 0,
  manifestationId TEXT NOT NULL PRIMARY KEY,
  modified TIMESTAMP NOT NULL DEFAULT timeofday()::timestamp,
  content JSONB NOT NULL
);
