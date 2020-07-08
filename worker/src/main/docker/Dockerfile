FROM docker.dbc.dk/payara5-micro:latest

ENV \
    MAX_POOL_SIZE=16

LABEL \
      SYSTEM_NAME="environment in which it runs for hazelcast naming (required)" \
      WORK_PRESENTATION_POSTGRES_URL="database url (required: user:pass@host:port/database)" \
      MAX_POOL_SIZE="Number of database connections to allocate (default: 16)"

COPY target/${project.build.finalName}.war target/docker/app.json deployments/
