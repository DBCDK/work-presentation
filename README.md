# work-presentation
Middleware for presenting works in a web application

## Content Migration

If the JSON schema of the built records changes then the migration path is:

1. Create new tables (workObject, workContains & cache) with a new version number in database migration.
1. Alter `VERSION` in `api/src/main/java/dk/dbc/search/work/presentation/api/jpa/JsonSchemaVersion.java` to reflect the version
1. Deploy extra `work-presentation-worker` with new version and new `consumer` name
1. Add new `consumer` to `corepo`'s `queue_rules`
1. Queue all corepo works (by running `scripts/queue-all.sh`)
1. Wait for queue to drain
1. Deploy `work-presentation-service` in the new version
1. Remove old `consumer` from `corepo`'s `queue_rules`
1. Remove old `work-presentation-worker`
1. Drop old tables in database migration
1. Ensure new migration is applied (deploy service or run by hand from `work-presentation-postgresql-jar-with-dependencies.jar`)

If only the content of the JSON fields has changed, but the schema remains the same

1. Truncate the `CACHE` table
1. Queue all corepo works (both steps are performed by running `scripts/queue-all.sh`)
1. Wait for queue to drain
