# ODK Aggregate
![Platform](https://img.shields.io/badge/platform-Java-blue.svg)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Build status](https://circleci.com/gh/opendatakit/aggregate.svg?style=shield&circle-token=:circle-token)](https://circleci.com/gh/opendatakit/aggregate)
[![Slack status](http://slack.opendatakit.org/badge.svg)](http://slack.opendatakit.org)
 
ODK Aggregate provides a ready-to-deploy server and database to:

- provide blank forms to ODK Collect (or other OpenRosa clients)
- accept submissions (finalized forms) from ODK Collect and manage collected data
- visualize the collected data using maps and simple graphs
- export submissions in CSV, KML, and JSON format
- publish submissions to external systems like Google Spreadsheets

ODK Aggregate can be deployed on an Apache Tomcat server, or any servlet 2.5-compatible (or higher) web container, backed with a PostgreSQL or a MySQL database server.

* ODK website: [https://opendatakit.org](https://opendatakit.org)
* ODK Aggregate usage instructions: [https://docs.opendatakit.org/aggregate-intro/](https://docs.opendatakit.org/aggregate-intro/)
* ODK forum: [https://forum.opendatakit.org](https://forum.opendatakit.org)
* ODK developer Slack chat: [http://slack.opendatakit.org](http://slack.opendatakit.org) 

## Getting the code

1. Fork the Aggregate project ([why and how to fork](https://help.github.com/articles/fork-a-repo/))

2. Install [Git LFS](https://git-lfs.github.com/)

3. Clone your fork of the project locally. At the command line:

    `git clone https://github.com/YOUR-GITHUB-USERNAME/aggregate`

## Setting up the database

Aggregate supports a variety of database engines, but we strongly recommend PostgreSQL. If you wish to use MySQL, see the [database configurations](docs/database-configurations.md) guide.
 
### PostgreSQL with Docker

1. Install [Docker](https://www.docker.com) and [Docker Compose](https://docs.docker.com/compose)

2. Start the development server with `./gradlew postgresqlComposeUp`

    Check that the port number **5432** is not used by any other service in your computer. You can change this editing the `ports` section of the `db/postgresql/docker-compose.yml` configuration file. Be sure to check the documentation: [Compose file version 3 reference - Ports section](https://docs.docker.com/compose/compose-file/#ports).

3. Stop the server with `./gradlew postgresqlComposeDown`

### Local PostgreSQL server

1. Download and install [PostgreSQL 9](https://www.postgresql.org/download) or later

    - If you are a macOS user, we recommend [Postgres.app](http://postgresapp.com/)
    - If you are a Windows user, we recommend [BigSQL](https://www.openscg.com/bigsql/postgresql/installers.jsp)

2. In a command.line terminal, run the following commands to set up a database for Aggregate:

    (Linux and macOS)
    ```bash
    sudo su postgres -c "psql -c \"CREATE ROLE odk WITH LOGIN PASSWORD 'odk'\""
    sudo su postgres -c "psql -c \"CREATE DATABASE odk WITH OWNER odk\""
    sudo su postgres -c "psql -c \"GRANT ALL PRIVILEGES ON DATABASE odk TO odk\""
    sudo su postgres -c "psql -c \"CREATE SCHEMA aggregate\" odk"
    sudo su postgres -c "psql -c \"GRANT ALL PRIVILEGES ON SCHEMA aggregate TO odk\" odk"
    ```
    
    (Windows)
    ```powershell
    psql.exe -c "CREATE ROLE odk WITH LOGIN PASSWORD 'odk'"
    psql.exe -c "CREATE DATABASE odk WITH OWNER odk"
    psql.exe -c "GRANT ALL PRIVILEGES ON DATABASE odk TO odk"
    psql.exe -c "CREATE SCHEMA aggregate" odk
    psql.exe -c "GRANT ALL PRIVILEGES ON SCHEMA aggregate TO odk" odk
    ```

## Building and Running the project

- Copy the `jdbc.properties.example`, `odk-settings.xml.example`, and `security.properties.example` files at `/src/main/resources` to the same location, removing the `.example` extension.

  If you have followed the database configuration steps above, you don't need to make any change in these files. Otherwise, head to the [Aggregate configuration guide](docs/aggregate-config.md) and make the required changes for your environment.
  
- Start a local development Aggregate server with `./gradlew appRunWar`

  Gradle will compile the project and start the server, which can take some time.
  
  Eventually, you will see a "Press any key to stop the server" message. At this point, you can browse http://localhost:8080 to use Aggregate.

- Stop the server pressing any key in the terminal where you started the server

If you have more than one Java version installed in your computer, you can ensure that Java 8 will be used when running Gradle tasks from the command-line by adding `-Porg.gradle.java.home={PATH_TO_JAVA8_HOME}` to the task.

### Connect from an external device

By default, Gretty will launch a server using a `localhost` address which will not be accessible by external devices (e.g., ODK Collect in an emulator, ODK Briefcase on another computer). To set a non-localhost address, edit the following files:

- In `src/main/resources/security.properties`, change `security.server.hostname` to the address
- In `build.gradle`, inside the `gretty` block, change `host` to the same address

## Setting up your development environment

These instructions are for [IntelliJ IDEA Community edition](https://www.jetbrains.com/idea/), which is the (free) Java IDE we use for all the ODK toolsuite, but you don't really need any specific IDE to work with this codebase. Any Java IDE will support any of the steps we will be describing.

### Import 

- On the welcome screen, click `Import Project`, navigate to your aggregate folder, and select the `build.gradle` file. 

  Make sure you check `Use auto-import` option in the `Import Project from Gradle` dialog. 

  Ignore any message about any detected GWT, Spring or web facets.

- Make sure you set Java 8 as the project's selected SDK
    
### Run

1. Show the Gradle tool window by selecting the menu option at **View** > **Tool Windows** > **Gradle**

    You will see a new panel on the right side with all the Gradle task groups
  
2. Double click the `appRunWar` Gradle task under the `gretty` task group

    A new `Run` bottom panel will pop up.
  
    Gradle will compile the project and start the server, which can take some time.
    
    Eventually, you will see a "Press any key to stop the server" message. At this point, you can browse http://localhost:8080 to use Aggregate.
  
You can stop the server by pressing any key in the `Run` panel.
       
### Debug

1. In the `Run` menu, select `Edit Configurations...`

2. Press the + button to add a `Remote` configuration

    - Name: `Debug Aggregate` (or whatever you'd like)
    - Host: `localhost`
    - Port: `5005`
    - Search sources using module's classpath: `aggregate`

3. Press `OK`

4. Run Aggregate with the `appRunWarDebug` task (double click it on the Gradle panel at the right side)

5. Run the `Debug Aggregate` run configuration you've created (use the debug button, not the play button, which should be disabled)

Eventually, the compilation will finish and the server will be ready for you to browse [http://localhost:8080](http://localhost:8080)

To stop the debugging session, press any key in the `Run` bottom panel. This will close your debug process in the `Debug` bottom panel as well.

## Extended topics

There is a [`/docs`](https://github.com/opendatakit/aggregate/tree/master/docs) directory in the repo with more documentation files that expand on certain topics:

- [Configuration files](./docs/aggregate-config.md)
- [Supported database configurations](./docs/database-configurations.md)
- [Build the Installer app](docs/build-the-installer-app.md)
- [Build and run a Docker image](docs/build-and-run-a-docker-image.md)
- [Build and run with Docker Compose](docs/build-and-run-with-docker-compose.md)
- [Build and run a Virtual Machine](docs/build-and-run-a-virtual-machine.md)

## Contributing

Any and all contributions to the project are welcome. ODK Aggregate is used across the world primarily by organizations with a social purpose so you can have real impact!

If you're ready to contribute code, see [the contribution guide](CONTRIBUTING.md).

The best way to help us test is to build from source! We are currently focusing on stabilizing the build process.

## Troubleshooting

* We enabled Git LFS on the Aggregate codebase and reduced the repo size from 700 MB to 34 MB. No code was changed, but if you cloned before December 11th, 2017, you'll need to reclone the project.

* If you get an **Invalid Gradle JDK configuration found** error importing the code, you might not have set the `JAVA_HOME` environment variable. Try [these solutions](https://stackoverflow.com/questions/32654016/).

* If you are having problems with hung Tomcat/Jetty processes, try running the `appStop` Gradle task to stop running all instances. 

* If you're using Chrome and are seeing blank pages or refreshing not working, connect to Aggregate with the dev tools window open. Then in the `Network` tab, check `Disable cache`.
