#cloud-config
users:
  - name: odk
    ssh-authorized-keys:
      - {{pubKey}}
    sudo: ['ALL=(ALL) NOPASSWD:ALL']
    groups: sudo
    shell: /bin/bash
  - name: tomcat
    lock_passwd: true
    sudo: false
    no_create_home: true
    no_user_group: false

packages:
  - tar
  - zip
  - unzip
  - vim
  - wget
  - curl
  - tree
  - postgresql-10
  - openjdk-8-jdk-headless
  - nginx
  - software-properties-common

write_files:
  - path: /etc/init.d/tomcat
    permissions: '0755'
    content: |
      #!/bin/bash
      #
      # description: Apache Tomcat init script
      # processname: tomcat
      # chkconfig: 234 20 80

      export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64/jre
      export PATH=$JAVA_HOME/bin:$PATH
      export CATALINA_HOME=/opt/apache-tomcat-8.5.37
      export CATALINA_BASE=$CATALINA_HOME
      export TOMCAT_USER=tomcat
      TOMCAT_USAGE="Usage: $0 {\e[00;32mstart\e[00m|\e[00;31mstop\e[00m|\e[00;31mkill\e[00m|\e[00;32mstatus\e[00m|\e[00;31mrestart\e[00m}"
      SHUTDOWN_WAIT=20

      tomcat_pid() {
        echo `ps -fe | grep $CATALINA_BASE | grep -v grep | tr -s " "|cut -d" " -f2`
      }

      start() {
        pid=$(tomcat_pid)
        if [ -n "$pid" ]; then
          echo -e "\e[00;31mTomcat is already running (pid: $pid)\e[00m"
        else
          echo -e "\e[00;32mStarting tomcat\e[00m"
            if [ `user_exists $TOMCAT_USER` = "1" ]; then
              /bin/su $TOMCAT_USER -c $CATALINA_HOME/bin/startup.sh
            else
              echo -e "\e[00;31mTomcat user $TOMCAT_USER does not exists. Starting with $(id)\e[00m"
              sh $CATALINA_HOME/bin/startup.sh
            fi
            status
        fi
        return 0
      }

      status(){
        pid=$(tomcat_pid)
        if [ -n "$pid" ]; then
          echo -e "\e[00;32mTomcat is running with pid: $pid\e[00m"
        else
          echo -e "\e[00;31mTomcat is not running\e[00m"
          return 3
        fi
      }

      terminate() {
      	echo -e "\e[00;31mTerminating Tomcat\e[00m"
      	kill -9 $(tomcat_pid)
      }

      stop() {
        pid=$(tomcat_pid)
        if [ -n "$pid" ]; then
          echo -e "\e[00;31mStoping Tomcat\e[00m"
          sh $CATALINA_HOME/bin/shutdown.sh
          let kwait=$SHUTDOWN_WAIT
          count=0;
          until [ `ps -p "$pid" | grep -c "$pid"` = '0' ] || [ $count -gt $kwait ]
          do
            echo -n -e "\n\e[00;31mwaiting for processes to exit\e[00m";
            sleep 1
            let count=$count+1;
          done
          if [ $count -gt $kwait ]; then
            echo -n -e "\n\e[00;31mkilling processes didn't stop after $SHUTDOWN_WAIT seconds\e[00m"
            terminate
          fi
        else
          echo -e "\e[00;31mTomcat is not running\e[00m"
        fi
        return 0
      }

      user_exists(){
        if id -u $1 >/dev/null 2>&1; then
          echo "1"
        else
          echo "0"
        fi
      }

      case $1 in
      	start)
      	  start
      	;;
      	stop)
      	  stop
      	;;
      	restart)
      	  stop
      	  start
      	;;
      	status)
      	  status
      	  exit $?
      	;;
      	kill)
      	  terminate
      	;;
      	*)
      	  echo -e $TOMCAT_USAGE
      	;;
      esac
      exit 0
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
  - sed -i -e '/^PermitRootLogin/s/^.*$/PermitRootLogin no/' /etc/ssh/sshd_config
  - sed -i -e '$aAllowUsers odk' /etc/ssh/sshd_config
  - service ssh restart
  - su postgres -c "psql -c \"CREATE ROLE odk WITH LOGIN PASSWORD 'odk'\""
  - su postgres -c "psql -c \"CREATE DATABASE odk WITH OWNER odk\""
  - su postgres -c "psql -c \"GRANT ALL PRIVILEGES ON DATABASE odk TO odk\""
  - su postgres -c "psql -c \"CREATE SCHEMA aggregate\" odk"
  - su postgres -c "psql -c \"GRANT ALL PRIVILEGES ON SCHEMA aggregate TO odk\" odk"
  - apt-get -y remove openjdk-11-jre-headless
  - wget -O /tmp/apache-tomcat-8.5.37.tar.gz {{tomcatTarballUrl}}
  - tar zxvf /tmp/apache-tomcat-8.5.37.tar.gz -C /opt
  - rm -rf /opt/apache-tomcat-8.5.37/webapps/ROOT/*
  - rm -rf /opt/apache-tomcat-8.5.37/webapps/docs
  - rm -rf /opt/apache-tomcat-8.5.37/webapps/examples
  - wget -O /tmp/aggregate.war {{aggregateWarUrl}}
  - unzip /tmp/aggregate.war -d /opt/apache-tomcat-8.5.37/webapps/ROOT
  - mv /tmp/jdbc.properties /opt/apache-tomcat-8.5.37/webapps/ROOT/WEB-INF/classes/jdbc.properties
  - mv /tmp/security.properties /opt/apache-tomcat-8.5.37/webapps/ROOT/WEB-INF/classes/security.properties
  - chown -R tomcat:tomcat /opt/apache-tomcat-8.5.37
  - systemctl daemon-reload
  - service tomcat start
  - service nginx stop
  - rm /etc/nginx/sites-enabled/default
  - mv /tmp/10-aggregate /etc/nginx/sites-available/10-aggregate
  - ln -s /etc/nginx/sites-available/10-aggregate ../etc/nginx/sites-enabled/10-aggregate
  - service nginx start
  - add-apt-repository -y universe
  - add-apt-repository -y ppa:certbot/certbot
  - apt-get -y update
  - apt-get -y install python-certbot-nginx
  - rm /tmp/aggregate.war
  - rm /tmp/apache-tomcat-8.5.37.tar.gz
