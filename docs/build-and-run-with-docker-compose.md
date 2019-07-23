# ODK Aggregate - Build and run with Docker Compose

## Requirements

- Install [Docker](https://www.docker.com)
- Install [Docker Compose](https://docs.docker.com/compose)

## Quick start

- Build the setup with `./gradlew clean dockerComposeBuild -xtest -PwarMode=complete`.
- Copy the setup at `build/docker-compose` to some other location in your system. 

If you get the error below when running `./gradlew clean dockerComposeBuild -xtest -PwarMode=complete`

    Error: Could not find or load main class org.gradle.wrapper.GradleWrapperMain
    Caused by: java.lang.ClassNotFoundException: org.gradle.wrapper.GradleWrapperMain
    
First run `gradle wrapper`

Run the following commands where you have copied your built setup. **Don't run them directly in `build/docker-compose` because it will compromise Aggregate's build tasks**.
    
- Before running Aggregate for the first time, prepare the database container with `docker-compose run --rm db`
  
  Press `ctrl+c` when you see the message `"PostgreSQL init process complete; ready for start up"`
  
- Start Aggregate with `docker-compose up`.

  You will see the output from the containers. 
  
  Aggregate is ready when you see a message containing `"Server startup in 2355 ms"` (the actual number may be different).
  
  Access Aggregate at [http:/localhost:8080](http:/localhost:8080)

- Stop Aggregate by pressing `ctrl+c`

## Running Aggregate on detached mode

- Start Aggregate with `docker-compose up -d`
- You can get the logs with `docker-compose logs`
- Stop Aggregate with `docker-compose stop`

## Advanced topics

## About this Docker Compose setup

This setup will launch two containers:

- A PostgreSQL 9.6 server

  All data will be stored on the host system, at a `pgdata` directory inside the setup's root directory. This directory is owned by the container's `postgres` user and you will require superuser privileges to manipulate it.
  
- A Tomcat 8 server with Aggregate as the ROOT webapp

  The `webapps` folder is inside the setup's root directory and is owned by the host system.

### Backup

The simplest way to backup your Aggregate server is to copy the whole Docker Compose setup directory while Aggregate is stopped.

If you need to dump Aggregate's database contents:

- Find out the PostgreSQL server's container's name with `docker ps -a`. The following is an example output of that command, asuming that the Docker Compose setup is in a directory called `cocotero`:

    ```
    $ docker ps
    CONTAINER ID        IMAGE                 COMMAND                  CREATED             STATUS              PORTS                    NAMES
    22d4c6d4029d        tomcat:alpine         "catalina.sh run"        About an hour ago   Up 6 minutes        0.0.0.0:8080->8080/tcp   cocotero_tomcat8_1
    ee892f368b95        postgres:9.6-alpine   "docker-entrypoint.s…"   About an hour ago   Up 6 minutes        0.0.0.0:5432->5432/tcp   cocotero_db_1
    ```

  In this example, PostgreSQL is running in a container called `cocotero_db_1`.
  
- Get a dump of the database with `docker exec --user postgres -it %NAME_OF_THE_CONTAINER% pg_dump aggregate`. Replace `%NAME_OF_THE_CONTAINER%` with the name you got on the previous step.
 
### Custom networking configuration

This setup is configured to get Docker to bridge the Tomcat container's 8080 port to the host's 8080 port. If you need to set another host port, edit the `services.tomcat8.ports` section of the `docker-compose.yml` file accordingly. More information about this on the [official Docker Compose networking docs](https://docs.docker.com/compose/networking/).   
