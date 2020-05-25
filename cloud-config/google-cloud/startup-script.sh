#!/bin/bash
if [ -f /root/aggregate.version ]; then
  echo "System already configured. Skipping script"
  exit 0
fi

cat << EOF > /root/aggregate-config.json
{
  "home": "/root",
  "jdbc": {
    "host": "127.0.0.1",
    "port": 5432,
    "db": "aggregate",
    "schema": "aggregate",
    "user": "aggregate",
    "password": "aggregate"
  },
  "security": {
    "hostname": "foo.bar",
    "forceHttpsLinks": true,
    "port": 80,
    "securePort": 443,
    "checkHostnames": false
  },
  "tomcat": {
    "uid": "tomcat8",
    "gid": "tomcat8",
    "webappsPath": "/var/lib/tomcat8/webapps"
  }
}
EOF

DEBIAN_FRONTEND=noninteractive

add-apt-repository -y universe
add-apt-repository -y ppa:certbot/certbot
apt-get -y update
apt-get -y install \
 zip \
 unzip \
 wget \
 curl \
 tomcat8 \
 postgresql-10 \
 openjdk-8-jdk-headless \
 nginx \
 software-properties-common \
 python-certbot-nginx

apt-get -y remove openjdk-11-jre-headless

unattended-upgrades
apt-get -y autoremove

su postgres -c "psql -c \"CREATE ROLE aggregate WITH LOGIN PASSWORD 'aggregate'\""
su postgres -c "psql -c \"CREATE DATABASE aggregate WITH OWNER aggregate\""
su postgres -c "psql -c \"GRANT ALL PRIVILEGES ON DATABASE aggregate TO aggregate\""
su postgres -c "psql -c \"CREATE SCHEMA aggregate\" aggregate"
su postgres -c "psql -c \"ALTER SCHEMA aggregate OWNER TO aggregate\" aggregate"
su postgres -c "psql -c \"GRANT ALL PRIVILEGES ON SCHEMA aggregate TO aggregate\" aggregate"

rm /etc/nginx/sites-enabled/default
cat << EOF > /etc/nginx/sites-enabled/aggregate
server {
    client_max_body_size 100m;
    server_name foo.bar;

    location / {
        proxy_pass http://127.0.0.1:8080;
    }
}
EOF

curl -H"Metadata-Flavor: Google" http://metadata.google.internal/computeMetadata/v1/instance/hostname > /tmp/domain-name
sed -i -e 's/foo\.bar/'"$(cat /tmp/domain-name)"'/' /root/aggregate-config.json
sed -i -e 's/foo\.bar/'"$(cat /tmp/domain-name)"'/' /etc/nginx/sites-enabled/aggregate

curl -sSL https://api.github.com/repos/getodk/aggregate-cli/releases/latest \
| grep "aggregate-cli.zip" \
| cut -d: -f 2,3 \
| tr -d \" \
| wget -O /tmp/aggregate-cli.zip -qi -

unzip /tmp/aggregate-cli.zip -d /usr/local/bin
chmod +x /usr/local/bin/aggregate-cli

aggregate-cli -i -y -c /root/aggregate-config.json

service nginx restart

(crontab -l 2>/dev/null; echo "0 0 1 * * /usr/bin/certbot renew > /var/log/letsencrypt/letsencrypt.log") | crontab -
