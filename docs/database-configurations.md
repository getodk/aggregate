# ODK Aggregate - Supported database configurations

## Disclaimer

Currently we only test PostgreSQL support. We need feedback and help to follow supporting other database backends.

# Database configuration

Wether you choose to run Aggregate on Google App Engine or Tomcat, properly configured `jdbc.properties` and `odk-settings.xml` files must exist inside the WAR/EAR artifact.

You can find these files in `src/main/resources` directories on each submodule.

## `jdbc.properties`

If you want to work with Google App Engine and Google Big Table, just leave the file empty.

All three supported RDBs use the same configuration file structure. This is an example for PostgreSQL:

```properties
jdbc.driverClassName=org.postgresql.Driver
jdbc.resourceName=jdbc/odk_aggregate
jdbc.url=jdbc:postgresql://127.0.0.1/odk_db?autoDeserialize=true
jdbc.username=odk_unit
jdbc.password=test
jdbc.schema=odk_db
```

Driver class names are:

| Database   | Driver class name                              |
| ---------- | ---------------------------------------------- |
| PostgreSQL | `org.postgresql.Driver`                        |
| MySQL      | `com.mysql.jdbc.Driver`                        |
| SQLServer  | `com.microsoft.sqlserver.jdbc.SQLServerDriver` |

## `odk-settings.xml`

You have to make sure that the `datastore` bean has the right class configured:

```xml
<bean id="datastore" class="org.opendatakit.common.persistence.engine.pgres.DatastoreImpl">
  <!-- ... -->
</bean>
```

Table of posible values:

| Database          | Class                                                               |
| ----------------- | ------------------------------------------------------------------- |
| PostgreSQL        | `org.opendatakit.common.persistence.engine.pgres.DatastoreImpl`     |
| MySQL             | `org.opendatakit.common.persistence.engine.mysql.DatastoreImpl`     |
| SQLServer         | `org.opendatakit.common.persistence.engine.sqlserver.DatastoreImpl` |
| Google App Engine | `org.opendatakit.common.persistence.engine.gae.DatastoreImpl`       |

# Database initialization

Run the following SQL scripts to create and prepare a database to be used by Aggregate

**PostgreSQL**

```sql
CREATE USER "odk_unit" WITH UNENCRYPTED PASSWORD 'test';
CREATE DATABASE odk_db WITH OWNER odk_unit;
GRANT ALL PRIVILEGES ON DATABASE odk_db TO odk_unit;
\connect odk_db;
CREATE SCHEMA odk_db;
GRANT ALL PRIVILEGES ON SCHEMA odk_db TO odk_unit;
```

**MySQL**

```sql
CREATE DATABASE `odk_db`;
CREATE USER 'odk_unit'@'localhost' IDENTIFIED BY 'test';
GRANT ALL ON `odk_db`.* TO 'odk_unit'@'localhost' IDENTIFIED BY 'test';
FLUSH PRIVILEGES;
```

**SQLServer**

```sql
USE master;
go
CREATE DATABASE odk_unit;
go
USE odk_unit;
go
CREATE SCHEMA odk_schema;
go
```

