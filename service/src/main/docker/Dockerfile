FROM docker.dbc.dk/payara5-micro:latest

ENV \
    MAX_POOL_SIZE=16\
    USER_AGENT="WorkPresentationService/1.0"

LABEL \
      SYSTEM_NAME="environment in which it runs for hazelcast naming (required)" \
      COREPO_SOLR_URL="Solr searcher url (http://host:port/solr/searcher | zk://cluster/[chroot/]collection) (required)" \
      SOLR_APPID="Application ID pass to SOLR, for logging in SOLR (required)" \
      VIP_CORE_URL="Vip-Core rest url (http) (required)" \
      WORK_PRESENTATION_POSTGRES_URL="database url (required: user:pass@host:port/database)" \
      \
      USER_AGENT="Http request header value for 'User-Agent' (default: WorkPresentationWorker/1.0)" \
      MAX_POOL_SIZE="Number of database connections to allocate (default: 16)"

COPY target/docker/*.jar /opt/payara5/jars/

COPY target/${project.build.finalName}.war target/docker/app.json deployments/
