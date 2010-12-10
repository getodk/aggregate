#!/bin/sh

rm jars/*.jar
cd gae
jar -cf ../jars/odk-gae-settings.jar * -C ../common .
cd ../mysql
jar -cf ../jars/odk-mysql-settings.jar * -C ../common .
cd ../postgres
jar -cf ../jars/odk-postgres-settings.jar * -C ../common .
