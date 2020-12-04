FROM docker.dbc.dk/payara5-micro:latest

ENV \
    MAX_POOL_SIZE=16 \
    USER_AGENT="WorkPresentationService/1.0" \
    CACHE_DEFAULT="5" \
    CACHE_DEFAULT_UNIT="Minutes" \
    CACHE_VIPCORE="3" \
    CACHE_VIPCORE_UNIT="Hours" \
    CACHE_VIPCORE_ERROR="3" \
    CACHE_VIPCORE_ERROR_UNIT="Minutes" \
    CACHE_SOLR="5" \
    CACHE_SOLR_UNIT="Minutes" \
    CACHE_SOLR_ERROR="10" \
    CACHE_SOLR_ERROR_UNIT="Seconds"

LABEL \
      SYSTEM_NAME="environment in which it runs for hazelcast naming (required)" \
      COREPO_SOLR_URL="Solr searcher url (http://host:port/solr/searcher | zk://cluster/[chroot/]collection) (required)" \
      SOLR_APPID="Application ID pass to SOLR, for logging in SOLR (required)" \
      VIP_CORE_URL="Vip-Core rest url (including /api/ path) (http) (required)" \
      WORK_PRESENTATION_POSTGRES_URL="database url (required: user:pass@host:port/database)" \
      \
      USER_AGENT="Http request header value for 'User-Agent' (default: WorkPresentationWorker/1.0)" \
      MAX_POOL_SIZE="Number of database connections to allocate (default: 16)" \
      \
      CACHE_DEFAULT="Caching timeout for for unspecified caching" \
      CACHE_DEFAULT_UNIT="Caching unit (Seconds/Minutes/Hours) for unspecified caching" \
      CACHE_VIPCORE="Caching timeout for vip-core success" \
      CACHE_VIPCORE_UNIT="Caching unit (Seconds/Minutes/Hours) for vip-core success" \
      CACHE_VIPCORE_ERROR="Caching timeout for vip-core error" \
      CACHE_VIPCORE_ERROR_UNIT="Caching unit (Seconds/Minutes/Hours) for vip-core error" \
      CACHE_SOLR="Caching timeout for SolR success" \
      CACHE_SOLR_UNIT="Caching unit (Seconds/Minutes/Hours) for SolR success" \
      CACHE_SOLR_ERROR="Caching timeout for SolR error" \
      CACHE_SOLR_ERROR_UNIT="Caching unit (Seconds/Minutes/Hours) for SolR error" \

COPY target/docker/*.jar /opt/payara5/jars/

COPY target/${project.build.finalName}.war target/docker/app.json deployments/
