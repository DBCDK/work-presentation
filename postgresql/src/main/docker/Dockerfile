FROM docker.dbc.dk/dbc-postgres-13:latest
MAINTAINER os-team

ENV POSTGRES_DB=workpresentation
ENV POSTGRES_USER=workpresentation
ENV POSTGRES_PASSWORD=workpresentation

USER root
RUN chown -R $POSTGRES_LINUX_USER:$POSTGRES_LINUX_USER /docker-entrypoint.d/
ADD target/workpresentation.sql /docker-entrypoint.d/
USER $POSTGRES_LINUX_USER

LABEL POSTGRES_DB="Name of db (optional). Default is 'workpresentation'" \
      POSTGRES_USER="(optional). Default is 'workpresentation'" \
      POSTGRES_PASSWORD="(optional). Default is 'workpresentation'"
