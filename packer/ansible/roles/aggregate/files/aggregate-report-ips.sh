#!/usr/bin/env bash

PROPS_FILE=/var/lib/tomcat8/webapps/ROOT/WEB-INF/classes/security.properties

rawHostname=$(grep hostname ${PROPS_FILE} | awk '{split($0,a,"="); print a[2]}')
hostname=${rawHostname:-localhost}
httpPort=$(grep port ${PROPS_FILE} | awk '{split($0,a,"="); print a[2]}')
httpsPort=$(grep securePort ${PROPS_FILE} | awk '{split($0,a,"="); print a[2]}')

echo "Open a browser and use one of these URLs to access ODK Aggregate"
if [ ${httpPort} = "80" ]; then
  echo "http://${hostname}"
else
  echo "http://${hostname}:${httpPort}"
fi
if [ ${httpsPort} = "443" ]; then
  echo "https://${hostname}"
else
  echo "https://${hostname}:${httpsPort}"
fi
echo ""

echo "This VM uses these IP addresses:"
ip address | grep "inet " | grep -v "127.0.0.1" | awk '{print $2}' | awk -F'/' '{print $1}'
echo ""