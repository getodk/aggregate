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
  - path: /var/lib/aggregate-version.txt
    content: |
      {{aggregateVersion}}
  - path: /tmp/jdbc.properties
    content: |
      jdbc.driverClassName=org.postgresql.Driver
      jdbc.resourceName=jdbc/odk_aggregate
      jdbc.url=jdbc:postgresql://127.0.0.1/aggregate?autoDeserialize=true
      jdbc.username=aggregate
      jdbc.password=aggregate
      jdbc.schema=aggregate
  - path: /tmp/security.properties
    content: |
      security.server.deviceAuthentication=digest
      security.server.secureChannelType=REQUIRES_INSECURE_CHANNEL
      security.server.channelType=REQUIRES_INSECURE_CHANNEL
      security.server.forceHttpsLinks={{forceHttps}}
      security.server.hostname=
      security.server.port={{httpPort}}
      security.server.securePort=443
      security.server.superUserUsername=administrator
      security.server.realm.realmString=ODK Aggregate
  - path: /tmp/10-aggregate
    content: |
      server {
          client_max_body_size 100m;
          server_name {{domain}};

          location / {
              proxy_pass http://127.0.0.1:8080;
          }
      }
  - path: /home/odk/bin/download-aggregate-updater
    permissions: '0755'
    content: |
      #!/bin/sh
      curl -s https://api.github.com/repos/opendatakit/aggregate-updater/releases/latest \
      | grep "aggregate-updater.zip" \
      | cut -d: -f 2,3 \
      | tr -d \" \
      | wget -O /tmp/aggregate-updater.zip -qi -

      mkdir -p /home/odk/bin
      unzip /tmp/aggregate-updater.zip -d /usr/local/bin
      chmod +x /usr/local/bin/aggregate-updater

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
  - (crontab -l 2>/dev/null; echo "0 0 1 * * /usr/bin/certbot renew > /var/log/letsencrypt/letsencrypt.log") | crontab -

  - rm /tmp/aggregate.war

  - su postgres -c "psql -c \"CREATE ROLE aggregate WITH LOGIN PASSWORD 'aggregate'\""
  - su postgres -c "psql -c \"CREATE DATABASE aggregate WITH OWNER aggregate\""
  - su postgres -c "psql -c \"GRANT ALL PRIVILEGES ON DATABASE aggregate TO aggregate\""
  - su postgres -c "psql -c \"CREATE SCHEMA aggregate\" aggregate"
  - su postgres -c "psql -c \"GRANT ALL PRIVILEGES ON SCHEMA aggregate TO aggregate\" aggregate"

  - service tomcat8 start
  - service nginx start

  - /home/odk/bin/download-aggregate-updater
  - chown -R odk:odk /home/odk

