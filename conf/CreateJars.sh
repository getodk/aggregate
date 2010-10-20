#!/bin/sh

rm jars/*.jar
cd gae
jar -cf ../jars/odk-gae-settings.jar *
cd ../mysql
jar -cf ../jars/odk-mysql-settings.jar *
cd ../postgres
jar -cf ../jars/odk-postgres-settings.jar *
