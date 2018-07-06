FROM tomcat:8.0-jre8-alpine
RUN rm -rf /usr/local/tomcat/webapps
COPY webapps /usr/local/tomcat/webapps
COPY docker-entrypoint.sh /usr/local/bin/docker-entrypoint.sh

ENTRYPOINT ["docker-entrypoint.sh"]