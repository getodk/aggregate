# Full Maven Development Environment Configuration

1. Install Maven 3.  This document assumes Maven 3.0.4 or higher.
    This will generally set up a maven repository under
    the user's home directory:  ${HOME}/.m2/repository
1. Install Java 7 JDK.
1. Install Eclipse Mars.
1. Install Google Eclipse Plugin with App Engine SDK and Google Web Toolkit SDK.
1. Optionally Install Tomcat 8.0.
    This is required unless you do not import or always keep closed
    the MySQL and Postgres projects and don't use maven.
1. Optionally: Install Postgres

   For Postgres, run these commands:

   ```
   create database "odk_unit";
   create schema "odk_unit";
   create user "odk_unit" with unencrypted password 'odk_unit';
   grant all privileges on database "odk_unit" to "odk_unit";
   alter database "odk_unit" owner to "odk_unit";
   ```

   From the Postgres SQL shell (psql) commandline client,
   using the root account and password, if the above commands
   are in the file postgres.sql, you can type:

   ```
   \cd C:/your_path_no_spaces_forward_slashes_only
   \i postgres.sql
   \q
   ```
1. Optionally: Install MySQL

   For MySQL, run this script:

   ```
   UPDATE mysql.user SET Password=PASSWORD('odk_unit') WHERE User='root';
   FLUSH PRIVILEGES;
   CREATE USER 'odk_unit'@'localhost' IDENTIFIED BY 'odk_unit';
   CREATE DATABASE odk_unit;
   GRANT ALL PRIVILEGES ON odk_unit.* TO 'odk_unit'@'localhost' WITH GRANT OPTION;
   ```

   For MySQL, download and copy the MySQL Connector J jar into the Tomcat /lib directory (mysql-connector-java-5.1.40.jar to apache-tomcat-8.0.38/lib).
   
   This **MUST** be version 5.1.40 or higher. It is known that there are issues with 5.1.6 and earlier. We have only tested with 5.1.40. You must stop tomcat, if it is running, in order for the library to be detected.

   For Maven (3) is optional; (4), (5) and (6) are required in order
   to perform a full build.
1. Optionally: Install SQL Server

   We use Windows authentication for connecting to SQL Server.
   This requires running on a Windows platform.
1. For SQLServer, run this script:

   ```
   USE master;
   go
   CREATE DATABASE odk_unit;
   go
   USE odk_unit;
   go
   CREATE SCHEMA odk_schema;
   go
   ```
1. For SQLServer, copy the src\main\libs\sqlserver-auth\sqljdbc_auth.dll to your C:\Windows\System32 directory. Or, place it in your PATH and reboot your machine.

   For Maven installing Eclipse is optional; Google SDK, Tomcat and Postgres (or SQLServer) are required in order to perform a full build.
1. Register libraries in Maven: (this is also required for Eclipse builds)

   Run the ANT script (build.xml) under:

   `src/main/libs/` -- registers various jars into your local maven repo.

   To run, just cd to this directory and type `ant`

   See the src/main/libs/readme.txt for information about these jars.
1. Download and install Chrome. The test scripts now use Chrome for the selenium testing rather than Firefox, which changed so often as to be unusable. You might need to update selenium and the Chrome Driver for UI testing to work.  This is done in the build\build.xml file.
1. Download the App Engine SDK and selenium java client for full-stack integration / web UI tests.

   Run the ANT script (build.xml) under:

   `build/`   -- downloads the App Engine SDK and selenium java client (for full-stack integration / web UI tests)

   To run, just cd to this directory and type `ant`
1. Edit Maven's settings.xml file (this is in the .m2 directory).

A minimal file is:

```
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                      http://maven.apache.org/xsd/settings-1.0.0.xsd">
  <servers>
	<server>
		<id>local.gae</id>
		<username>mitchellsundt@gmail.com</username>
		<password></password>
	</server>
  </servers>
  <profiles>
  	<profile>
  	  <id>gae</id>
  	  <activation><activeByDefault>true</activeByDefault></activation>
  	  <properties>
		<localRepository>${user.home}/.m2/repository</localRepository>
		<temp.home>C:\\Users\\Administrator\\AppData\\Local\\Temp</temp.home>
		<bitrock.home>C:\Program Files (x86)\BitRock InstallBuilder Professional 15.9.0</bitrock.home>
	    <keystore.propertyfile>\C:\\Users\\Administrator\\keystore\\jarSignerDetails.txt</keystore.propertyfile>
		<headless.operation>no</headless.operation>
		<mysql.client.executable>C:\\Program Files\\MySQL\\MySQL Server 5.6\\bin\\mysql.exe</mysql.client.executable>
		<mysql.root.password>MYSQLROOTPASSWORDHERE</mysql.root.password>
		<postgres.client.executable>C:\\Program Files\\PostgreSQL\\9.4\\bin\\psql.exe</postgres.client.executable>
		<postgres.root.password>POSTGRESQLROOTPASSWORDHERE</postgres.root.password>
		<sqlserver.client.executable>C:\\Program Files\\Microsoft SQL Server\\Client SDK\\ODBC\\130\\Tools\\Binn\\sqlcmd.exe</sqlserver.client.executable>
		<test.server.hostname>YOUR.FULLY.QUALIFIED.HOSTNAME.AND.ORG</test.server.hostname>
		<test.server.port>7070</test.server.port>
		<test.server.secure.port>7443</test.server.secure.port>
		<test.server.gae.monitor.port>7075</test.server.gae.monitor.port>
		<unix.display>:20.0</unix.display>
		<webdriver.chrome.drivername>chromedriver_win32</webdriver.chrome.drivername>
		<webdriver.chrome.driverext>.exe</webdriver.chrome.driverext>
	  </properties>
  	</profile>
  </profiles>
</settings>
```

Be sure to update the paths and passwords to match those of your environment.

If you are running on a Mac, change the webdriver.chrome.driver* entries to:

```
		<webdriver.chrome.drivername>chromedriver_mac64</webdriver.chrome.drivername>
		<webdriver.chrome.driverext></webdriver.chrome.driverext>
```

If you are running on Linux, you will need to modify the build.xml to download the Linux drivers
and modify these entries with the appropriate values for those drivers.

The installer is not hooked into the parent Maven project, but identifies that project as its
parent.  So you can build the top-level project to build and run unit tests, integration tests,
etc. and do not need bitrock installed.

The aggregate-mysql war file is used as the starting point for the installer build process.

## Maven Command Line Builds

Maven command-line builds are done as follows:

```
mvn clean
```

This cleans the workspace, removing all temporary files.
If this errors out, verify that there are no orphaned java
executables running. If the GAE tests crash, they can leave
a java database background process running.

```
mvn install
```

This will build and install the projects, running the unit tests
against the 4 datastores (Google BigTable, MySQL, Postgresql, SQLServer),
and building the wars for the 4 platforms.

If you have bitrock installed and licensed, you can build the bitrock installer.  First,

```
 copy aggregate-mysql\target\aggregate-mysql-1.0.war bitrock-installer
 cd bitrock-installer
 mvn clean
 mvn install
```

Open bitrock and open the buildWar.xml project file in this directory.
On the packaging page, build for windows, linux, linux-64 and OSX.

On Windows, the generated installers are placed under:

```
C:\Users\Administrator\Documents\InstallBuilder\output
```

*NOTE*: the bitrock installer construction process copies configuration files
from the eclipse-ear (EarContent/appengine-application.xml),
eclipse-default (WebContent/appengine-web.xml, WebContent/cron.xml, WebContent/queue.xml)
and eclipse-n-background (WebContent/appengine-web.xml) projects.

Changing those configuration files will alter the installer image.

