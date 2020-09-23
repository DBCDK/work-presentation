# work-presentation
Middleware for presenting works in a web application

## Content Migration

If the JSON schema of the built records changes then the migration path is:

1. Prepare the project
    1. Create new tables (workObject, workContains & cache), and indexes upon the tables. With a new version number in a new database migration step. i.e.
        ```
        CREATE TABLE workObjectV${version} ...
        CREATE TABLE workContainsV${version} ...
        CREATE TABLE cacheV${version} ...
        ```
    1. Alter `VERSION` in `api/src/main/java/dk/dbc/search/work/presentation/api/jpa/JsonSchemaVersion.java` to reflect the version (numeric)
1. Lock existing workers (version and instances)
1. Deploy extra worker that should build the new JSON schema documents
    1. Deploy extra `work-presentation-worker` (work-presentation-worker-v\${version}) with the new version and new `consumer` name (work-\${version},work-\${version}-slow)
    1. Add new `consumer` (work-\${version}) to `corepo`'s `queue_rules`
1. Build the new documents
    1. Queue all corepo works (by running `scripts/queue-all.sh`)
    1. Wait for queue to drain (from corepo `SELECT pid FROM queue WHERE consumer = 'work-${version}-slow' LIMIT 1` returns no rows)
1. Make the new documents available to the users
    1. Deploy `work-presentation-service` in the new version
1. Cleanup the obsolete worker version
    1. Remove old `consumer` from `corepo`'s `queue_rules`
    1. Remove old `work-presentation-worker`
1. Remove decommissioned tables
    1. Drop old tables and indexes in a new database migration step. i.e.
        ```
        DROP TABLE workObjectV${version-1} ...
        DROP TABLE workContainsV${version-1} ...
        DROP TABLE cacheV${version-1} ...
        ```
    1. Ensure new migration is applied
        * Deploy a new version of either service or worker, that contain the `drop table` migration step
        * Or run `work-presentation-postgresql-jar-with-dependencies.jar` by hand to apply the new migration step

If only the content of the JSON fields has changed, but the schema remains the same

1. Truncate the `CACHE` table to remove old values
1. Queue all corepo works (both steps are performed by running `scripts/queue-all.sh`)
1. Wait for queue to drain (checking corepo database)
