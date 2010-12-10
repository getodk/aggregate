del jars\*.jar
cd gae
jar -cf ..\jars\odk-gae-settings.jar *.xml *.properties -C ..\common .
cd ..\mysql
jar -cf ..\jars\odk-mysql-settings.jar *.xml *.properties -C ..\common .
cd ..\postgres
jar -cf ..\jars\odk-postgres-settings.jar *.xml *.properties -C ..\common .

