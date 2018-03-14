#!/usr/bin/env bash

set -o errexit

PROPS_FILE=/var/lib/tomcat8/webapps/ROOT/WEB-INF/classes/security.properties
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
    --fqn)
    FQN="$2"
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
    *)
    POSITIONAL+=("$1")
    shift
    ;;
esac
done
set -- "${POSITIONAL[@]}"

help() {
  echo "Usage: aggregate-config <flags> [<arguments>...]"
  echo ""
  echo "Flags:"
  echo "--help                  Show this help message"
  echo ""
  echo "Arguments:"
  echo "--fqn        <value>    Set a new FQN (fully qualified name)"
  echo "--fqn        auto       Enable automatic detection of FQN"
  echo "                        (not recommended for production environments)"
  echo "--http-port  <value>    Set a new HTTP port"
  echo "--https-port <value>    Set a new HTTPS port"
}

if [ ${HELP} = YES ]; then
  help
  exit 0
fi

if [ ${HELP} = NO ] && [ -z ${FQN} ] && [ -z ${HTTP_PORT} ] && [ -z ${HTTPS_PORT} ]; then
  echo "Error: You need to set at least one argument"
  echo ""
  help
  exit 1
fi

echo "Stopping Tomcat. Please wait..."
echo ""
service tomcat8 stop

if [ ! -z ${FQN} ] && [ ${FQN} = "auto" ]; then
  cat ${PROPS_FILE} | sed -e "s/^security\.server\.hostname=.*$/security.server.hostname=/" > /tmp/temp_file
  cp /tmp/temp_file ${PROPS_FILE}
fi

if [ ! -z ${FQN} ] && [ ! ${FQN} = "auto" ]; then
  cat ${PROPS_FILE} | sed -e "s/^security\.server\.hostname=.*$/security.server.hostname=${FQN}/" > /tmp/temp_file
  cp /tmp/temp_file ${PROPS_FILE}
fi

if [ ! -z ${HTTP_PORT} ]; then
  cat ${PROPS_FILE} | sed -e "s/^security\.server\.port=.*$/security.server.port=${HTTP_PORT}/" > /tmp/temp_file
  cp /tmp/temp_file ${PROPS_FILE}
fi

if [ ! -z ${HTTPS_PORT} ]; then
  cat ${PROPS_FILE} | sed -e "s/^security\.server\.securePort=.*$/security.server.securePort=${HTTPS_PORT}/" > /tmp/temp_file
  cp /tmp/temp_file ${PROPS_FILE}
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