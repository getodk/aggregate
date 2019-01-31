# ODK Aggregate - Supported database configurations

Although we only test actively the integration with PostgreSQL, Aggregate also support the MySQL and SQLServer database engines.

# Database configuration

Configuring the database backend involves the files `jdbc.properties` and `odk-settings.xml` which are located at `src/main/resources` in this codebase, or at `WEB-INF/classes` in an exploded WAR webapp.

## `jdbc.properties`

All three supported RDBs use the same configuration file structure. This is an example for PostgreSQL:

```properties
jdbc.driverClassName=org.postgresql.Driver
jdbc.resourceName=jdbc/odk_aggregate
jdbc.url=jdbc:postgresql://127.0.0.1/aggregate?autoDeserialize=true
jdbc.username=aggregate
jdbc.password=aggregate
jdbc.schema=aggregate
```

Driver class names are:

| Database   | Driver class name                              |
| ---------- | ---------------------------------------------- |
| PostgreSQL | `org.postgresql.Driver`                        |
| MySQL      | `com.mysql.jdbc.Driver`                        |
| SQLServer  | `com.microsoft.sqlserver.jdbc.SQLServerDriver` |

## `odk-settings.xml`

Set the `class` attribute of the `datastore` bean with the value that corresponds to the database engine you're using:

```xml
<bean id="datastore" class="org.opendatakit.common.persistence.engine.pgres.DatastoreImpl">
  ...
</bean>
```

Table of posible values:

| Database          | Class                                                               |
| ----------------- | ------------------------------------------------------------------- |
| PostgreSQL        | `org.opendatakit.common.persistence.engine.pgres.DatastoreImpl`     |
| MySQL             | `org.opendatakit.common.persistence.engine.mysql.DatastoreImpl`     |
| SQLServer         | `org.opendatakit.common.persistence.engine.sqlserver.DatastoreImpl` |

# Database initialization

Run the following SQL scripts to create and prepare a database to be used by Aggregate

**PostgreSQL**

```sql
CREATE USER odk WITH PASSWORD 'odk';
CREATE DATABASE odk WITH OWNER odk;
GRANT ALL PRIVILEGES ON DATABASE odk TO odk;
\connect odk;
CREATE SCHEMA aggregate;
GRANT ALL PRIVILEGES ON SCHEMA aggregate TO odk;
```

**MySQL**

```sql
CREATE DATABASE odk;
CREATE USER odk@'%' IDENTIFIED BY 'odk';
GRANT ALL ON odk.* TO odk@'%' IDENTIFIED BY 'odk';
FLUSH PRIVILEGES;
```

**SQLServer**

```sql
USE master;
go
CREATE DATABASE odk;
go
USE odk;
go
CREATE SCHEMA aggregate;
go
```

