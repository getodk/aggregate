#!/usr/bin/env bash

latestVersion=$(curl --silent -XGET -i -H 'Accept: application/vnd.github.v3+json' -H 'User-Agent: Aggregate Updater' https://api.github.com/repos/opendatakit/aggregate/releases  | grep -Po '"tag_name":"\K.*?(?=")' | grep -m1 "^v2\.")
requestedVersion=${1:-${latestVersion}}
versionFile=/home/odk/aggregate-version.txt
installedVersion=$(cat ${versionFile})
webAppRoot="/var/lib/tomcat8/webapps/ROOT"
backup="/home/odk/backup"

echo "Aggregate updater"
echo
echo "- Installed version is: ${installedVersion}"
echo "- Latest version is: ${latestVersion}"
echo "- Requested version is: ${requestedVersion} (latest version by omission)"
echo

if [[ "${requestedVersion}" = "${installedVersion}" ]]; then
  echo "No action needed"
  echo
  exit 0
fi

echo "- Stopping Apache Tomcat"
service tomcat8 stop

echo "- Backing up conf files"
mkdir -p ${backup}
cp ${webAppRoot}/WEB-INF/classes/jdbc.properties ${backup}/
cp ${webAppRoot}/WEB-INF/classes/security.properties ${backup}/

echo "- Backing up current Aggregate"
zip -q -r ${backup}/aggregate_$(date --iso-8601).zip ${webAppRoot}

echo "- Cleaning webapp root"
rm -rf ${webAppRoot}/*

echo "- Deploying Aggregate version ${requestedVersion}"
wget -O /tmp/aggregate.war https://github.com/opendatakit/aggregate/releases/download/${requestedVersion}/ODK-Aggregate-${requestedVersion}.war
unzip -qq /tmp/aggregate.war -d ${webAppRoot}
rm /tmp/aggregate.war

echo "- Moving original conf files"
cp ${webAppRoot}/WEB-INF/classes/jdbc.properties ${webAppRoot}/WEB-INF/classes/jdbc.properties.original
cp ${webAppRoot}/WEB-INF/classes/security.properties ${webAppRoot}/WEB-INF/classes/security.properties.original

echo "Copying conf files"
mv ${backup}/*.properties ${webAppRoot}/WEB-INF/classes/

echo
echo "Please review conf file diffs"
echo
echo "JDBC conf file diff:"
diff -u --color ${webAppRoot}/WEB-INF/classes/jdbc.properties ${webAppRoot}/WEB-INF/classes/jdbc.properties.original
echo
echo "security.properties diff:"
diff -u --color ${webAppRoot}/WEB-INF/classes/security.properties ${webAppRoot}/WEB-INF/classes/security.properties.original
echo

# Cleanup
chown -R odk:odk ${backup}
chown -R tomcat8:tomcat8 ${webAppRoot}
echo ${requestedVersion} > ${versionFile}

echo "- Starting Apache Tomcat"
service tomcat8 start
