#!/usr/bin/env bash

pubKeyPath=$1;
domain=$2;
aggregateVersion=$3
pubKeyEscaped=$(cat ${pubKeyPath} | sed -e 's/\//\\\//g')

cat ../assets/cloud-config.yml.tpl | \
  sed -e 's/{{pubKey}}/'"${pubKeyEscaped}"'/g' | \
  sed -e 's/{{aggregateVersion}}/'"${aggregateVersion}"'/g' | \
  sed -e 's/{{forceHttps}}/true/g' | \
  sed -e 's/{{domain}}/'"${domain}"'/g' | \
  sed -e 's/{{httpPort}}/80/g' | \
  sed -e 's/{{aggregateWarUrl}}/https:\/\/github.com\/opendatakit\/aggregate\/releases\/download\/'"${aggregateVersion}"'\/ODK-Aggregate-'"${aggregateVersion}"'.war/g' \
  > cloud-config.yml

echo "CloudConfig script generated at $(pwd)/cloud-init.yml"

