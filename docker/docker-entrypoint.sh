#!/usr/bin/env bash

AGGREGATE_CONF_DIR=/usr/local/tomcat/webapps/ROOT/WEB-INF/classes
SECURITY_PROPS=${AGGREGATE_CONF_DIR}/security.properties
JDBC_PROPS=${AGGREGATE_CONF_DIR}/jdbc.properties
TOMCAT_CONF=/usr/local/tomcat/conf/server.xml
AGGREGATE_HOST=${AGGREGATE_HOST:-""}
DB_HOST=${DB_HOST:-localhost}
DB_PORT=${DB_PORT:-"5432"}
DB_NAME=${DB_NAME:-"aggregate"}
DB_SCHEMA=${DB_SCHEMA:-"aggregate"}
DB_USERNAME=${DB_USERNAME:-"aggregate"}
DB_PASSWORD=${DB_PASSWORD:-"aggregate"}

replace() {
	declare file="$1" s="$2" r="$3"
  cp ${file} ${file}.bak
  cat ${file} | sed -E -e "s/${s}/${r}/g" > /tmp/temp_file
  cp /tmp/temp_file ${file}
  rm /tmp/temp_file || true
}

if [ ! -z ${AGGREGATE_HOST} ] && [ ${AGGREGATE_HOST} = "auto" ]; then
  replace ${SECURITY_PROPS} "^security\.server\.hostname=.*$" "security.server.hostname="
fi

if [ ! -z ${AGGREGATE_HOST} ] && [ ! ${AGGREGATE_HOST} = "auto" ]; then
  replace ${SECURITY_PROPS} "^security\.server\.hostname=.*$" "security.server.hostname=${AGGREGATE_HOST}"
fi

if [ ! -z ${DB_HOST} ] && [ -z ${DB_PORT} ]; then
  replace ${JDBC_PROPS} "^jdbc\.url=jdbc\\\:postgresql\\\:\/\/.+\/" "jdbc.url=jdbc\\\:postgresql\\\:\/\/${DB_HOST}\/"
fi

if [ ! -z ${DB_HOST} ] && [ ! -z ${DB_PORT} ]; then
  replace ${JDBC_PROPS} "^jdbc\.url=jdbc\\\:postgresql\\\:\/\/.+\/" "jdbc.url=jdbc\\\:postgresql\\\:\/\/${DB_HOST}\\\:${DB_PORT}\/"
fi

replace ${JDBC_PROPS} "^jdbc\.url=(.+)\/.+$" "jdbc.url=\1\/${DB_NAME}?autoDeserialize=true"
replace ${JDBC_PROPS} "^jdbc\.schema=.*$" "jdbc.schema=${DB_SCHEMA}"
replace ${JDBC_PROPS} "^jdbc\.username=.*$" "jdbc.username=${DB_USERNAME}"
replace ${JDBC_PROPS} "^jdbc\.password=.*$" "jdbc.password=${DB_PASSWORD}"

echo
echo
echo
echo
echo
echo "Running Aggregate with the following config parameters:"
echo
echo "Aggregate FQDN:           ${AGGREGATE_HOST}"
echo "Database host:port:       ${DB_HOST}:${DB_PORT}"
echo "Database name.schema:     ${DB_NAME}.${DB_SCHEMA}"
echo "Database username:        ${DB_USERNAME} (password hidden)"
echo
echo "Tomcat conf at:           ${TOMCAT_CONF}"
echo "Aggregate conf files at:  ${AGGREGATE_CONF_DIR}"
echo
echo "Starting Tomcat. Please wait..."
echo
echo
echo
echo
echo
catalina.sh run