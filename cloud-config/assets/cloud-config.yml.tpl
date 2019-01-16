#cloud-config
users:
  - name: odk
    ssh-authorized-keys:
      - {{pubKey}}
    sudo: ['ALL=(ALL) NOPASSWD:ALL']
    groups: sudo
    shell: /bin/bash

packages:
  - tar
  - zip
  - unzip
  - vim
  - wget
  - curl
  - tree
  - tomcat8
  - tomcat8-common
  - tomcat8-admin
  - tomcat8-user
  - postgresql-10
  - openjdk-8-jdk-headless
  - nginx
  - software-properties-common

write_files:
  - path: /home/odk/aggregate-version.txt
    content: |
      {{aggregateVersion}}
  - path: /home/odk/aggregate-updater.sh
    permissions: '0755'
    content: |
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
  - path: /tmp/jdbc.properties
    content: |
      jdbc.driverClassName=org.postgresql.Driver
      jdbc.resourceName=jdbc/odk_aggregate
      jdbc.url=jdbc:postgresql://127.0.0.1/odk?autoDeserialize=true
      jdbc.username=odk
      jdbc.password=odk
      jdbc.schema=aggregate
  - path: /tmp/security.properties
    content: |
      security.server.deviceAuthentication=basic
      security.server.secureChannelType=REQUIRES_INSECURE_CHANNEL
      security.server.channelType=REQUIRES_INSECURE_CHANNEL
      security.server.forceHttpsLinks={{forceHttps}}
      security.server.hostname=
      security.server.port={{httpPort}}
      security.server.securePort=443
      wink.handlersFactoryClass=org.opendatakit.aggregate.odktables.impl.api.wink.AppEngineHandlersFactory
      security.server.superUser=
      security.server.superUserUsername=administrator
      security.server.realm.realmString=example ODK Aggregate
  - path: /tmp/10-aggregate
    content: |
      server {
          client_max_body_size 100m;
          server_name {{domain}};

          location / {
              proxy_pass http://127.0.0.1:8080;
          }
      }

runcmd:
  - service nginx stop
  - service tomcat8 stop

  - sed -i -e '/^PermitRootLogin/s/^.*$/PermitRootLogin no/' /etc/ssh/sshd_config
  - sed -i -e '$aAllowUsers odk' /etc/ssh/sshd_config
  - service ssh restart

  - apt-get -y remove openjdk-11-jre-headless
  - wget -O /tmp/aggregate.war {{aggregateWarUrl}}
  - rm -r /var/lib/tomcat8/webapps/ROOT/*
  - unzip /tmp/aggregate.war -d /var/lib/tomcat8/webapps/ROOT
  - mv /tmp/jdbc.properties /var/lib/tomcat8/webapps/ROOT/WEB-INF/classes/jdbc.properties
  - mv /tmp/security.properties /var/lib/tomcat8/webapps/ROOT/WEB-INF/classes/security.properties
  - chown -R tomcat8:tomcat8 /var/lib/tomcat8/webapps

  - rm /etc/nginx/sites-enabled/default
  - mv /tmp/10-aggregate /etc/nginx/sites-available/10-aggregate
  - ln -s /etc/nginx/sites-available/10-aggregate ../etc/nginx/sites-enabled/10-aggregate

  - add-apt-repository -y universe
  - add-apt-repository -y ppa:certbot/certbot
  - apt-get -y update
  - apt-get -y install python-certbot-nginx

  - rm /tmp/aggregate.war

  - su postgres -c "psql -c \"CREATE ROLE odk WITH LOGIN PASSWORD 'odk'\""
  - su postgres -c "psql -c \"CREATE DATABASE odk WITH OWNER odk\""
  - su postgres -c "psql -c \"GRANT ALL PRIVILEGES ON DATABASE odk TO odk\""
  - su postgres -c "psql -c \"CREATE SCHEMA aggregate\" odk"
  - su postgres -c "psql -c \"GRANT ALL PRIVILEGES ON SCHEMA aggregate TO odk\" odk"

  - service tomcat8 start
  - service nginx start
