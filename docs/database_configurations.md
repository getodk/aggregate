# OpenDataKit Aggregate - Supported database configurations

## Disclaimer

Currently we only test PostgreSQL support. We need the communities feedback and help to follow supporting other database backends.

## Database configuration

Wether you choose to run Aggregate on Google App Engine or Tomcat, properly configured `jdbc.properties` and `odk-settings.xml` files must exist in the classpath.

You can find these files in `src/main/resources` directories on each submodule.

## `jdbc.properties`

All three supported RDBs have the same configuration keys. This is an example for PostgreSQL:

```properties
jdbc.driverClassName=org.postgresql.Driver
jdbc.resourceName=jdbc/odk_aggregate
jdbc.url=jdbc:postgresql://127.0.0.1/odk_db?autoDeserialize=true
jdbc.username=odk_unit
jdbc.password=test
jdbc.schema=odk_db
```

Driver class names are:

- PostgreSQL: `org.postgresql.Driver`
- MySQL: `com.mysql.jdbc.Driver`
- SQLServer: `com.microsoft.sqlserver.jdbc.SQLServerDriver`

If you want to work with Google App Engine, just leave the file empty

## `odk-settings.xml`

- For PostgreSQL, `datastore` bean should be a `org.opendatakit.common.persistence.engine.pgres.DatastoreImpl` instance
- For MySQL, `datastore` bean should be a `org.opendatakit.common.persistence.engine.mysql.DatastoreImpl` instance
- For SQLServer, `datastore` bean should be a `org.opendatakit.common.persistence.engine.sqlserver.DatastoreImpl` instance
- For Google App Engine, `datastore` bean should be a `org.opendatakit.common.persistence.engine.gae.DatastoreImpl` instance

