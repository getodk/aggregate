# ODK Aggregate
![Platform](https://img.shields.io/badge/platform-Java-blue.svg)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Slack status](http://slack.opendatakit.org/badge.svg)](http://slack.opendatakit.org)
 
ODK Aggregate provides a ready-to-deploy server and data repository to:

- provide blank forms to ODK Collect (or other OpenRosa clients),
- accept finalized forms (submissions) from ODK Collect and manage collected data,
- visualize the collected data using maps and simple graphs,
- export data (e.g., as CSV files for spreadsheets, or as KML files for Google Earth), and
- publish data to external systems (e.g., Google Spreadsheets or Google Fusion Tables).

ODK Aggregate can be deployed on Google's App Engine, enabling users to quickly get running without facing the complexities of setting up their own scalable web service. ODK Aggregate can also be deployed locally on a Tomcat server (or any servlet 2.5-compatible (or higher) web container) backed with a MySQL or PostgreSQL database server.

* ODK website: [https://opendatakit.org](https://opendatakit.org)
* ODK Aggregate usage instructions: [https://opendatakit.org/use/aggregate/](https://opendatakit.org/use/aggregate/)
* ODK forum: [https://forum.opendatakit.org](https://forum.opendatakit.org)
* ODK developer Slack chat: [http://slack.opendatakit.org](http://slack.opendatakit.org) 
* ODK developer Slack archive: [http://opendatakit.slackarchive.io](http://opendatakit.slackarchive.io) 
* ODK developer wiki: [https://github.com/opendatakit/opendatakit/wiki](https://github.com/opendatakit/opendatakit/wiki)

## Getting the code

1. Fork the Aggregate project ([why and how to fork](https://help.github.com/articles/fork-a-repo/))

1. Clone your fork of the project locally. At the command line:

        git clone https://github.com/YOUR-GITHUB-USERNAME/aggregate

## Setting up the database

Aggregate supports a variety of DBs, but we strongly recommend you use PostgreSQL first to ensure everything is working. If you wish to use another DB (e.g., Google App Engine, MySQL, or SQLServer databases) after that see [database configurations][docs/database_configurations].

1. Download and install [PostgreSQL 9](https://www.postgresql.org/download) or later

1. Setup your database with these commands

    ```sql
    CREATE USER "odk_unit" WITH UNENCRYPTED PASSWORD 'test';
    CREATE DATABASE odk_db WITH OWNER odk_unit;
    GRANT ALL PRIVILEGES ON DATABASE odk_db TO odk_unit;
    \connect odk_db;
    CREATE SCHEMA odk_db;
    GRANT ALL PRIVILEGES ON SCHEMA odk_db TO odk_unit;
    ```

## Running the project
Aggregate is built using Gradle and Gretty, but we strongly recommend you use [IntelliJ IDEA](https://www.jetbrains.com/idea/) first to ensure everything is working. If you wish to use another development environment after that, run `./gradlew tasks` to get a sense of your options.

### Import 

1. On the welcome screen, click `Import Project`, navigate to your aggregate folder, and select the `build.gradle` file

1. Make sure you check `Use auto-import` option in the `Import Project from Gradle` dialog 

1. Once the project is imported, IntelliJ may ask you configure any detected GWT, Spring or web Facets, you can ignore these messages

### Run

1. In the `Run` menu, select `Edit Configurations...`

1. Press the + button to add a `Gradle` configuration

    * Name: `appStartWar` (or whatever you'd like)
    * Gradle project: `odk-aggregate`
    * Tasks: `appStartWar`

1. Press `OK`

1. To run Aggregate, go to the `Run` menu, then to `Run...` and `Run` the `appStartWar` configuration. This will start Aggregate. 

1. You should now be able to browse [http://localhost:8080](http://localhost:8080)

### Debug

1. In the `Run` menu, select `Edit Configurations...`

1. Press the + button to add a `Gradle` configuration

     * Name: `appStartWarDebug` (or whatever you'd like)
     * Gradle project: `odk-aggregate`
     * Tasks: `appStartWarDebug`

1. Press `Apply` and then press the + button to add a `Remote` configuration

     * Name: `appServer` (or whatever you'd like)
     * Host: `localhost`
     * Port: `5005`
     * Search sources using module's classpath: `aggregate`

1. Press `OK`

1. To debug Aggregate, go to the `Run` menu, then to `Run...` and `Run` (not Debug!) the `appStartWarDebug` configuration. This will start Aggregate in debug mode and wait for a debugging session to be connected to the server's debugging listener.

1. Now, go to the `Run` menu, then to `Run...` and `Debug` the `appServer`. This will connect the debugger. 

1. You should now be able to browse [http://localhost:8080](http://localhost:8080) and debug

### Connections from an external device

By default, Gretty will launch a server using a `localhost` address which will not be accessible by external devices (e.g., ODK Collect in an emulator, ODK Briefcase on another computer). To set a non-localhost address, edit the following files:

- In `src/main/resources/security.properties`, change `security.server.hostname` to the address
- In `build.gradle`, inside the `gretty` block, change `host` to the same address

## Contributing

Any and all contributions to the project are welcome. ODK Aggregate is used across the world primarily by organizations with a social purpose so you can have real impact!

If you're ready to contribute code, see [the contribution guide](CONTRIBUTING.md).

The best way to help us test is to build from source! We are currently focusing on stabilizing the build process.

## Troubleshooting

* If you are having problems with hung Tomcat/Jetty processes, try running the `appStop` Gradle task to stop running all instances.	

* If you're using Chrome and are seeing blank pages or refreshing not working, connect to Aggregate with the dev tools window open. Then in the `Network` tab, check `Disable cache`.