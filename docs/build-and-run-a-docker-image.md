# ODK Aggregate - Build and run a Docker image

## Requirements

- Install [Docker](https://www.docker.com)
- Already have a PostgreSQL 9.6 database running
- Follow the instructions of the main [README](https://github.com/opendatakit/aggregate), including:
  - Install Git LFS, as well as its git hooks, and enable the git hooks for Git LFS.
  - Only then clone your fork of Aggregate - included .jar files only download correctly with Git LFS. See [Getting the code](https://github.com/opendatakit/aggregate#getting-the-code).
  - Install IntelliJ IDEA and import project as instructed to let IDEA download, build, and configure sources. See [Running the project - Import](https://github.com/opendatakit/aggregate#import)

  *Note*: this is a gentle reminder to follow the main [README](https://github.com/opendatakit/aggregate), not a comprehensive setup guide in itself. However, please note that it is _not_ necessary to copy the `.example` configuration files - in fact, the build script overwrites any values set in `security.properties` and `jdbc.properties`, anyway. 
  
  The current recommended way to configure ODK Aggregate database settings is through [environmental variables](#configuration-parameters).

## Quick start

This Aggregate Docker image works by using an external running PostgreSQL 9.6 database. The following quick start steps assume you have access to a PostgreSQL server and it's the first time you will be running Aggregate. 

- Connect to your database server using `psql` or any other PostgreSQL client program. 

- Create a database and a schema by running this SQL script:

  ```sql
  CREATE USER aggregate WITH PASSWORD 'aggregate';
  CREATE DATABASE aggregate WITH OWNER aggregate;
  GRANT ALL PRIVILEGES ON DATABASE aggregate TO aggregate;
  \connect odk;
  CREATE SCHEMA aggregate;
  ALTER SCHEMA aggregate OWNER TO aggregate;  
  GRANT ALL PRIVILEGES ON SCHEMA aggregate TO aggregate;
  ```
  
  **We strongly advise you use a stronger password on the first line of the script.**
  
- Take note of the database connection parameters: host, port, database name, username and password.
  
- Build the Docker image with `./gradlew clean dockerBuild -xtest -PwarMode=complete`.

  Take note of the tag of the created image. It will appear in a message like this one:
  
  ```
  ...
  Successfully built bf09fb92a6e9
  Successfully tagged aggregate:v1.6.0-beta.0-dirty
  ```

- Launch Aggregate with `docker run -d -p 8080:8080 -it --name=aggregate aggregate:v1.6.0-beta.0-dirty`
 
  Replace the version with the one you got when building the image and the database params accordingly.
  
  If you set another password, add a `-e DB_PASSWORD="%THE PASSWORD%` to the launch command before the `aggregate:v1.6.0-beta.0-dirty` part. [More info here](#configuration-parameters)
  
  Access Aggregate at [http://localhost:8080](http://localhost:8080)

- You can get the logs with `docker logs aggregate`

- Stop Aggregate with `docker stop aggregate`

- Start Aggregate again with `docker start aggregate`

## Advanced topics

## About this Docker image

- The base of this Docker image is [tomcat:8.0-jre8-alpine](https://github.com/docker-library/repo-info/blob/master/repos/tomcat/remote/8.0-jre8-alpine.md)


## Configuration

You have two options for passing configuration to ODK. 

### Environmental Variables
If you are using PostgreSQL (recommended), you can set the following environment variables when running the Aggregate Docker image:

| variable | Default value | Description |
| --- | --- | --- |
| AGGREGATE_HOST | localhost | Value of the `security.server.hostname` conf parameter. Set to `auto` for FQDN auto-discovery |
| DB_SCHEMA | aggregate | Value of the `jdbc.schema` conf parameter |
| DB_USERNAME | aggregate | Value of the `jdbc.username` conf parameter |
| DB_PASSWORD | aggregate | Value of the `jdbc.password` conf parameter |
| DB_HOST | localhost | Value used to build the `jdbc.url` conf parameter |
| DB_PORT | 5432 | Value used to build the `jdbc.url` conf parameter |
| DB_NAME | aggregate | Value used to build the `jdbc.url` conf parameter |

  These variables can be set by adding `--env VARIABLE_NAME=VALUE` arguments when running the Aggregate Docker image ([more info in the docs](https://docs.docker.com/docker-cloud/getting-started/deploy-app/6_define_environment_variables/#python-quickstart)). Example:
  
  ```shell 
  docker run -d -p 8080:8080 -it \
  --env AGGREGATE_HOST="odk.somedomain.com" \ 
  --name=aggregate aggregate:v1.6.0-beta.0-dirty
  ```

- All these conf parameters are explained in the [ODK Aggregate - Configuration files](./aggregate-config.md) document
- If you are running your PostgreSQL server in the same host where you want to run the Aggregate Docker image you will probably need to add the `--network=host` argument when running it to allow the container access the host's services.
- If you need to serve Aggregate in a different host port, use Docker's networking configuration options to redirect the guest's 8080 port to any other host port ([more info in the docs](https://docs.docker.com/config/containers/container-networking/))
- If you are using a database engine other than PostgreSQL, you will need to follow the bind-mount technique outlined below.

## Config Files

For complete control of all of Aggregate's configuration values, you can also pass a copy of the `security.properties` and `jdbc.properties` files to Docker [via a bind mount](https://docs.docker.com/storage/bind-mounts/) into the `/etc/config` directory in the Docker image. 

Follow the directions in the main `README` regarding copying the `/src/main/resources/jdbc.properties.example`, `odk-settings.xml.example` and `security.properties` files, and pass them to Docker kind of like so:

```shell 
docker run -d -p 8080:8080 -it \
--mount type=bind,source="$(pwd)"/src/main/resources/odk-settings.xml,target=/etc/config/odk-settings.xml,readonly \
--mount type=bind,source="$(pwd)"/src/main/resources/jdbc.properties,target=/etc/config/jdbc.properties,readonly \
--mount type=bind,source="$(pwd)"/src/main/resources/security.properties,target=/etc/config/security.properties,readonly \
--name=aggregate aggregate:latest
```

Of course, you can place these files anywhere on your server that you wish, as long as you update the "source" path above. You can also use this technique along with any Docker orchestration tool that supports mounting configuration values into files on the filesystem, [such as Kubernetes](https://kubernetes.io/docs/tasks/configure-pod-container/configure-pod-configmap/#populate-a-volume-with-data-stored-in-a-configmap). Just make sure the files are always mounted into `/etc/config`, as the `docker-entrypoint.sh` script makes symlinks from there into proper directory for Aggregate. You can also pass a custom Tomcat `server.xml`, though you shouldn't need to do this. 

Finally, you can also continue to use the environmental variables technique above to override any values set in the files.