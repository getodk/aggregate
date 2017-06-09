# Minimal Tomcat8 MySQL/PostgreSQL Eclipse Setup

This assumes you have completed the [**"Minimal Eclipse Installation Setup"**][eclipse]

(including the download of the AppEngine SDK and GWT SDK. These are needed to resolve symbols when
compiling the source code, even when running under Tomcat8.)

1. Install Tomcat8 on your computer.
1. Database Dependencies
   - download MySQL Connector/J and place it in the lib directory of the Tomcat install. This **MUST** be version 5.1.40 or higher. It is known that there are issues with 5.1.6 and earlier. We have only tested with 5.1.40. Stop and restart the Tomcat8 server so it picks up that library. This must be present for MySQL connections to work. It does not harm anything if this is present when using PostgreSQL.
   - SQLServer configuration requires running on a Windows system. Copy the src\main\sqlserver-auth\sqljdbc_auth.dll to your C:\Windows\System32 directory. This is the authentication library for JDBC 4.1 that uses Windows authentication to verify user identity.
1. install the database server of your choice (MySQL or PostgreSQL or SQLServer). **NOTE**:  Be sure that it is configured using a UTF-8 character set as the default.

    For MySQL: Stop the MySQL database server, then configure the database (via the "my.cnf" or the "my.ini" file) with these lines added to the [mysqld] section:

    ```
    character_set_server=utf8
    collation_server=utf8_unicode_ci
    max_allowed_packet=1073741824
    ```
 
    For SQLServer, we configure it to use Windows authentication mode, but it can be mixed-mode.
1. Start Eclipse (Mars) and select this ODK Aggregate workspace.
    - Go to Help / Install New Software.
    - Choose Add...
    - And register an entry for this URL  https://dl.google.com/eclipse/plugin/4.4
    - After registering, when it presents available software to install, do not install the SDKs. At a minimum, you need to install:
      - Google Plugin for Eclipse / Google Plugin for Eclipse 4.4/4.5 (you don't need anything else)
    - Proceed with installing this and restarting Eclipse.
1. Re-open Eclipse, go to Window / Preferences
    - Open Google / App Engine and add the AppEngine SDK path that you downloaded and exploded in (5) in earlier section
    - Open Google / Web Toolkit and add the GWT SDK path of what you downloaded and exploded in (6) in earlier section
    - Choose OK to accept changes and close the preferences dialog.
1. Once again, go to Window / Preferences
    - Open Server /Runtime Environment 
    - Select Google AppEngine. If it complains about not having an AppEngine SDK the delete this and select Add, choose Google / AppEngine, and accept the defaults to re-create it.
    - Select Apache Tomcat v8.0. If it complains about not having a configured runtime, then delete this.
    - Select Add..., Select Apache / Apache Tomcat v8.0 and set it up to point to your installation of Tomcat v8.0 on your system. Do not choose to create a new server; you are re-using the existing server.
    - Click OK to save changes.
1. If you haven't already, go to the Workbench view. Then, Import / Import... / General / Existing Projects into Workspace
      - import these existing projects:
        - odk-mysql-settings
        - odk-posgres-settings
        - eclipse-tomcat8
1. Depending upon which database you want to use:

    **MySQL**

    - open odk-mysql-settings/common
    - edit security.properties to set the hostname to the IP address of your computer.
    - open odk-mysql-settings/mysql
    - edit jdbc.properties to specify a username, password and database name in the url. 

    ```
    jdbc.driverClassName=com.mysql.jdbc.Driver
    jdbc.resourceName=jdbc/odk_aggregate
    jdbc.url=jdbc:mysql://127.0.0.1/odk_db?autoDeserialize=true
    jdbc.username=odk_unit
    jdbc.password=test
    jdbc.schema=odk_db
    ```

    - Save changes.
    - Now open MySQL Workbench. If you have not yet created that database, issue the following commands, with the names changed for what you specified above. The names to substitute above/below are:
      - `odk_db` -- replace with your database name
      - `odk_unit` -- replace with your username
      - `test` -- replace with your password

    ```
    create database `odk_db`;
    create user 'odk_unit'@'localhost' identified by 'test';
    grant all on `odk_db`.* to 'odk_unit'@'localhost' identified by 'test';
    flush privileges;
    ```
    - Finally, return to Eclipse, select the build.xml script within the odk-mysql-settings
    - project, right-click, Run As / Ant Build.
    - This will bundle up these changes and copy the changes into the eclipse-tomcat8 project.

    **PostgreSQL**

    - open odk-postgres-settings/common
    - edit security.properties to set the hostname to the IP address of your computer.
    - open odk-postgres-settings/postgres
    - edit jdbc.properties to specify a username, password and database name in the url.

    ```
    jdbc.driverClassName=org.postgresql.Driver
    jdbc.resourceName=jdbc/odk_aggregate
    jdbc.url=jdbc:postgresql://127.0.0.1/odk_db?autoDeserialize=true
    jdbc.username=odk_unit
    jdbc.password=test
    jdbc.schema=odk_db
    ```

    - Save changes.
    - Now open pgAdmin III. If you have not yet created that database, issue the following commands, with the names changed for what you specified above. The names to substitute above/below are:
      - odk_db -- replace with your database name
      - odk_unit -- replace with your username
      - test -- replace with your password

    ```
    create database "odk_db";
    SELECT datname FROM pg_database WHERE datistemplate = false;
    create user "odk_unit" with unencrypted password 'test';
    grant all privileges on database "odk_db" to "odk_unit";
    alter database "odk_db" owner to "odk_unit";
    \c "odk_db";
    create schema "odk_db";
    grant all privileges on schema "odk_db" to "odk_unit";
    ```

    - Finally, return to Eclipse, select the build.xml script within the odk-postgres-settings project, right-click, Run As / Ant Build.
    - This will bundle up these changes and copy the changes into the eclipse-tomcat8 project.

    **SQLServer**

    - open odk-sqlserver-settings/common
    - edit security.properties to set the hostname to the IP address of your computer.
    - open odk-sqlserver-settings/sqlserver
    - edit jdbc.properties to specify a database name in the url. The url is configured to use Windows authentication for accessing the database, so no username or password is present in this file. If you do not want to use Windows authentication, compare the odk_settings.xml file for sqlserver with that for postgres to see where to add settings for username and password so that you can use those for authentication.

    ```
    jdbc.driverClassName=com.microsoft.sqlserver.jdbc.SQLServerDriver
    jdbc.resourceName=jdbc/odk_aggregate
    jdbc.url=jdbc:sqlserver://127.0.0.1\\MSSQLSERVER:1433;databaseName=odk_unit;applicationName=ODKAggregate;encrypt=true;trustServerCertificate=true;integratedSecurity=true;authentication=ActiveDirectoryIntegrated;
    jdbc.schema=odk_schema
    ```

    - Save changes.
    - Now open Microsoft SQL Server Management Studio. If you have not yet created that database, issue the following commands, with the names changed for what you specified above. The names to substitute above/below are:
      - odk_unit -- replace with your database name
      - odk_schema -- replace with the schema name

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

    - Because we are using Windows authentication, the run-as user under Eclipse will be your user. Since you are an admin on the database, we don't need to set permissions.
    - Finally, return to Eclipse, select the build.xml script within the odk-postgres-settings project, right-click, Run As / Ant Build.
    - This will bundle up these changes and copy the changes into the eclipse-tomcat8 project.
1. Select eclipse-tomcat8, right-click, Refresh. (to pick up file changes).
1. Select eclipse-tomcat8, right-click, Google / GWT Compile

    Verify the program arguments are

    ```
       -war WebContent
    ```

    And the VM arguments are

    ```
    -Xmx512m
    ```

    Apply and Compile.
1. Select eclipse-tomcat8, right-click, Refresh. (to pick up GWT file changes).
1. Select eclipse-tomcat8, right-click, properties,
1. Select the Servers tab in the Output area
    - Delete any existing Tomcat v8.0 server.
    - Click to create a server.
    - Choose Tomcat v8.0 Server
    - Enter the IP address of your computer. If you leave this as localhost, then ODK Collect and ODK Briefcase will not be able to fully communicate with your development server.
    - Click Next
    - Configure to deploy eclipse-tomcat8 on this server.
    - Click Finish
1. Select eclipse-tomcat8 project, right-click Properties
    - Go to Project Facets, select Dynamic Web App, click Runtimes tab, verify that Tomcat v8.0 is chosen.
    - Apply
    - Go to Server, select the Tomcat v8.0 server that you just created
    - Apply
    - Click OK
1. You should now be able to run ODK Aggregate on this Tomcat8 server by right-click, Debug As / Debug on Server

    The project may report a validation error (web.xml not found in WebContent). You can ignore this. The web.xml is provided in war-base.

    You may need to clear your browser cache if you are using GWT SuperDevMode and re-configure the browser for that (e.g., the bookmark buttons).

## Tomcat8 Edit-Debug Cycle Considerations

Now, you should be able to Debug the server-side code using the
Tomcat8 development server. When you are developing, as you
change code, you will probably need to start and stop the server.

If you change the UI layer, you will want to re-run the GWT compiler,
and Refresh the eclipse-tomcat8 project, where the UI
code is.

You may need to clear your browser cache if you are using GWT SuperDevMode
and re-configure the browser for that (e.g., the bookmark buttons).

If you are working with GWT code, you can work in SuperDevMode
where you set breakpoints within the Chrome development environment.

See farther down (below) for configuring GWT.

If something is not picked up, you can try cleaning the project
and also Clean the server (via right-click on the server on the Servers
tab). This should refresh everything.  Unlike with AppEngine, this
will not clear the content of your database. You would need to do
that through your database admin tool (MySQL Workbench or pgAdmin III).

[eclipse]: https://github.com/opendatakit/aggregate/blob/master/docs/eclipse.md


