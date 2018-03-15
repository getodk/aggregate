#!/usr/bin/env bash

PROPS_FILE=/var/lib/tomcat8/webapps/ROOT/WEB-INF/classes/security.properties

rawHostname=$(grep hostname ${PROPS_FILE} | awk '{split($0,a,"="); print a[2]}')
hostname=${rawHostname:-localhost}
httpPort=$(grep port ${PROPS_FILE} | awk '{split($0,a,"="); print a[2]}')
ips=$(ip address | grep "inet " | grep -v "127.0.0.1" | awk '{print $2}' | awk -F'/' '{print $1}')
VERSION=cocotero

echo "> Welcome to ODK Aggregate VM $VERSION"
echo "> 1. Open the web browser on your computer"
if [ ${httpPort} = "80" ]; then
  echo "> 2. Go to http://${hostname}"
else
  echo "> 2. Go to http://${hostname}:${httpPort}"
fi
echo "> 3. Sign in with the Aggregate password"
echo "> Need the password? Read the readme.txt file."

if [ ! -z ${ips} ]; then
  echo ">"
  echo "> If the above URL does not work, these IP addresses instead"
  echo "${ips}" | while IFS= read -r line ; do echo "> ${line}"; done
fi