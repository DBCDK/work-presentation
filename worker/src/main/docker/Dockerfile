FROM docker.dbc.dk/payara5-micro:payara-ce

ENV \
    QUEUE_DEDUPLICATE=true \
    JS_POOL_SIZE=8 \
    MAX_POOL_SIZE=16 \
    USER_AGENT="WorkPresentationWorker/1.0"

LABEL \
      SYSTEM_NAME="environment in which it runs for hazelcast naming (required)" \
      USER_AGENT="Http request header value for 'User-Agent' (default: WorkPresentationWorker/1.0)" \
      WORK_PRESENTATION_POSTGRES_URL="database url (required: user:pass@host:port/database)" \
      COREPO_CONTENT_SERVICE_URL="Corepo content service URL (required)"\
      COREPO_POSTGRES_URL="database url (required: user:pass@host:port/database)" \
      QUEUE_DEDUPLICATE="If duplicate queue entries should be collapsed (default: true)" \
      COREPO_CONTENT_SERVICE_URL="Rest service (http) for corepo content (required)" \
      SOLR_DOC_STORE_URL="Rest service (http) for solr-doc-store (required) - value 'disabled' disables" \
      \
      QUEUES="Comma separated list of queue names in order to drain (required)" \
      DATABASE_THROTTLE="Database connect error throttle (default: 1/5s,3/m,5/10m)" \
      FAILURE_THROTTLE="Processing failure throttle (default: 2/100ms,3/s,5/m)" \
      EMPTY_QUEUE_SLEEP="How long to sleep when queue is empty (default: 10s)" \
      QUEUE_WINDOW="How long to look back in time when pulling new messages (default: 500ms))" \
      MAX_QUERY_TIME="When query takes longer, recalc queryplan (default: 250ms)" \
      IDLE_RESCAN_EVERY="How often to rediscover start time when idling (default: 5)" \
      RESCAN_EVERY="How often to rediscover start time when running (default: 500)" \
      MAX_TRIES="Now many times to try to process a queue entry (default: 3)" \
      THREADS="Number of parallel processings (required)" \
      JPA_POSTPONE="In case of JPA errors how long to postpone a retry duration-duration ex. 5s-3m(required)" \
      \
      JS_POOL_SIZE="Max number og JavaScript worker objects (default: 8)" \
      MAX_POOL_SIZE="Number of database connections to allocate (default: 16)"

COPY target/docker/*.jar /opt/payara5/jars/

COPY target/${project.build.finalName}.war target/docker/app.json deployments/
