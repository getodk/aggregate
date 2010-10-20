del jars\*.jar
cd gae
jar -cf ..\jars\odk-gae-settings.jar *.xml *.properties
cd ..\mysql
jar -cf ..\jars\odk-mysql-settings.jar *.xml *.properties
cd ..\postgres
jar -cf ..\jars\odk-postgres-settings.jar *.xml *.properties
