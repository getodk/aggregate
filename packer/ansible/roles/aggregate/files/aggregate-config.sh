#!/usr/bin/env bash

set -o errexit

PROPS_FILE=/var/lib/tomcat8/webapps/ROOT/WEB-INF/classes/security.properties
TOMCAT_CONFIG_FILE=/var/lib/tomcat8/conf/server.xml
HELP=NO
POSITIONAL=()
while [[ $# -gt 0 ]]
do
key="$1"

case $key in
    --help)
    HELP=YES
    shift
    ;;
    --fqdn)
    FQDN="$2"
    shift
    shift
    ;;
    --http-port)
    HTTP_PORT="$2"
    shift
    shift
    ;;
    --https-port)
    HTTPS_PORT="$2"
    shift
    shift
    ;;
    --net-mode)
    NET_MODE="$2"
    shift
    shift
    ;;
    *)
    POSITIONAL+=("$1")
    shift
    ;;
esac
done
set -- "${POSITIONAL[@]}"

showHelp() {
  echo "Usage: aggregate-config <flags> [<arguments>...]"
  echo ""
  echo "Flags:"
  echo "--help               Show this help message"
  echo ""
  echo "Arguments:"
  echo "--fqdn       <value> Set a new FQDN (fully qualified domain name)"
  echo "--fqdn       auto    Enable automatic detection of FQDN"
  echo "                     (not recommended if your IP address changes)"
  echo "--http-port  <value> Set a new HTTP port"
  echo "--https-port <value> Set a new HTTPS port"
  echo "--net-mode   <value> Set the VM's network mode. Use 'nat' or 'bridge'"
}

if [ "${HELP}" = YES ]; then
  showHelp
  exit 0
fi

if [ "${NET_MODE}" != "nat" ] && [ "${NET_MODE}" != "bridge" ]; then
  echo "Error: Invalid --net-mode value. Use 'nat' or 'bridge' according to the VM's networking configuration"
  echo ""
  showHelp
  exit 1
fi

if [ "${NET_MODE}" == "bridge" ] && ([ -z "${HTTP_PORT}" ] || [ -z "${HTTPS_PORT}" ]); then
  echo "Error: In net mode 'bridge' you need to provide values for --http-port and --https-port"
  echo ""
  showHelp
  exit 1
fi

if [ "${HELP}" = NO ] && [ -z "${FQDN}" ] && [ -z "${HTTP_PORT}" ] && [ -z "${HTTPS_PORT}" ]; then
  echo "Error: You need to set at least one argument"
  echo ""
  showHelp
  exit 1
fi

echo "Stopping Tomcat. Please wait..."
echo ""
service tomcat8 stop

if [ ! -z "${FQDN}" ] && [ "${FQDN}" = "auto" ]; then
  sed -e "s/^security\.server\.hostname=.*$/security.server.hostname=/" ${PROPS_FILE} > /tmp/temp_file
  cp /tmp/temp_file ${PROPS_FILE}
fi

if [ ! -z "${FQDN}" ] && [ ! "${FQDN}" = "auto" ]; then
  sed -e "s/^security\.server\.hostname=.*$/security.server.hostname=${FQDN}/" ${PROPS_FILE} > /tmp/temp_file
  cp /tmp/temp_file ${PROPS_FILE}
fi

if [ ! -z "${HTTP_PORT}" ]; then
  sed -e "s/^security\.server\.port=.*$/security.server.port=${HTTP_PORT}/" ${PROPS_FILE} > /tmp/temp_file
  cp /tmp/temp_file ${PROPS_FILE}
fi

if [ ! -z "${HTTPS_PORT}" ]; then
  sed -e "s/^security\.server\.securePort=.*$/security.server.securePort=${HTTPS_PORT}/" ${PROPS_FILE} > /tmp/temp_file
  cp /tmp/temp_file ${PROPS_FILE}
fi

if [ "${NET_MODE}" = "nat" ]; then
  sed -e "s/^.*Connector.*$/    <Connector port=\"80\" protocol=\"HTTP\/1.1\" connectionTimeout=\"20000\" redirectPort=\"443\"\/>/" ${TOMCAT_CONFIG_FILE} > /tmp/temp_file
  cp /tmp/temp_file ${TOMCAT_CONFIG_FILE}
fi

if [ "${NET_MODE}" = "bridge" ]; then
  sed -e "s/^.*Connector.*$/    <Connector port=\"${HTTP_PORT}\" protocol=\"HTTP\/1.1\" connectionTimeout=\"20000\" redirectPort=\"${HTTPS_PORT}\"\/>/" ${TOMCAT_CONFIG_FILE} > /tmp/temp_file
  cp /tmp/temp_file ${TOMCAT_CONFIG_FILE}
fi

rm /tmp/temp_file || true

echo "New settings are:"
grep hostname ${PROPS_FILE}
grep port ${PROPS_FILE}
grep securePort ${PROPS_FILE}
echo ""

echo "Starting Tomcat. Please wait..."
echo ""
service tomcat8 start
echo "Done"
echo ""

aggregate-update-issue
aggregate-report-ips
