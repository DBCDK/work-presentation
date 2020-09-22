#!/bin/bash -e

VERSION=$(perl -0777 -p -e 's/\n/ /msg;s/\s\s*/ /g' "${0%/*}"/../api/src/main/java/dk/dbc/search/work/presentation/api/jpa/JsonSchemaVersion.java | \
    grep -Po '(?<=public static final String VERSION = ")\d+')

if [ x$VERSION = x ]; then
    echo "Cannot determine schema version (table suffix)" >&2
    exit 1
fi

if [ x$WORK_PRESENTATION_POSTGRES_URL = x ] || \
    [ x$COREPO_POSTGRES_URL = x ] || \
    [ x$JENKINS_BASE_URL = x ]; then
    echo "Required environment variables: " >&2
    echo "WORK_PRESENTATION_POSTGRES_URL (user:pass@host:port/base)" >&2
    echo "COREPO_POSTGRES_URL (user:pass@host:port/base)" >&2
    echo "JENKINS_BASE_URL (https://jenkins/)" >&2
    echo "Optional environment variables: " >&2
    echo "QUEUE defaluts to (work-$VERSION-slow)"
    echo "" >&2
    exit 1
fi
if [ x$QUEUE = x ]; then
    QUEUE=work-$VERSION-slow
fi

    
CHUNK_INSERT_JAR=/tmp/$USER-chunk-insert-$$.jar
curl -so $CHUNK_INSERT_JAR "${JENKINS_BASE_URL%/}/job/chunk-insert/job/master/lastSuccessfulBuild/artifact/target/chunk-insert.jar"
trap "rm -vf $CHUNK_INSERT_JAR" EXIT

psql postgres://$WORK_PRESENTATION_POSTGRES_URL -c "TRUNCATE cacheV$VERSION"
java -jar $CHUNK_INSERT_JAR --dry-run --database=$COREPO_POSTGRES_URL --commit=25000 --vacuum=10 "INSERT INTO queue(pid, consumer, trackingid) SELECT pid, '$QUEUE', 'requeue-by-$USER' FROM records WHERE NOT deleted AND pid LIKE 'work:%'"
