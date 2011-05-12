#!/bin/sh
rm target/odk-gae-settings-latest.jar
jar -cf target/odk-gae-settings-latest.jar -C ../src/main/resources/gae . -C ../src/main/resources/common .
cp -f target/odk-gae-settings-latest.jar ../eclipse-aggregate-gae/war/WEB-INF/lib
