#!/usr/bin/env bash

AGGREGATE_CONF_DIR=/usr/local/tomcat/webapps/ROOT/WEB-INF/classes
ODK_SETTINGS=${AGGREGATE_CONF_DIR}/odk-settings.xml
SECURITY_PROPS=${AGGREGATE_CONF_DIR}/security.properties
JDBC_PROPS=${AGGREGATE_CONF_DIR}/jdbc.properties
TOMCAT_CONF=/usr/local/tomcat/conf/server.xml

if [ -f "/etc/config/server.xml" ]; then 
  rm  ${TOMCAT_CONF}
  ln -s /etc/config/server.xml ${TOMCAT_CONF}
fi

if [ -f "/etc/config/odk-settings.xml" ]; then 
  rm  ${ODK_SETTINGS}
  ln -s /etc/config/odk-settings.xml ${ODK_SETTINGS}
fi

if [ -f "/etc/config/security.properties" ]; then 
  rm  ${SECURITY_PROPS}
  ln -s /etc/config/security.properties ${SECURITY_PROPS}
fi

if [ -f "/etc/config/jdbc.properties" ]; then 
  rm  ${JDBC_PROPS}
  ln -s /etc/config/jdbc.properties ${JDBC_PROPS}
fi

if [ -f "/etc/config/security.properties" ]; then 
  rm  ${SECURITY_PROPS}
  ln -s /etc/config/security.properties ${SECURITY_PROPS}
fi

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

if [ ! -z ${DB_NAME} ]; then
  replace ${JDBC_PROPS} "^jdbc\.url=(.+)\/.+$" "jdbc.url=\1\/${DB_NAME}?autoDeserialize=true"
fi

if [ ! -z ${DB_SCHEMA} ]; then
  replace ${JDBC_PROPS} "^jdbc\.schema=.*$" "jdbc.schema=${DB_SCHEMA}"
fi

if [ ! -z ${DB_USERNAME} ]; then
  replace ${JDBC_PROPS} "^jdbc\.username=.*$" "jdbc.username=${DB_USERNAME}"
fi

if [ ! -z ${DB_PASSWORD} ]; then
  replace ${JDBC_PROPS} "^jdbc\.password=.*$" "jdbc.password=${DB_PASSWORD}"
fi

echo
echo
echo
echo
echo
echo "Running Aggregate with the following config parameters:"
echo
echo
echo "${AGGREGATE_CONF_DIR}/jdbc.properties:"
echo "-----"
cat ${JDBC_PROPS} | sed -E -e "s/^jdbc\.password=.*$/jdbc.password=<hidden>/g"
echo
echo "${AGGREGATE_CONF_DIR}/security.properties:"
echo "-----"
cat ${SECURITY_PROPS}
echo 
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
