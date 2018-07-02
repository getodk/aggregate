- Edit `src/main/resources/jdbc.properties`
```properties
jdbc.url=jdbc:postgresql://db/odk_db?autoDeserialize=true
```
- Build Aggregate with `warMode=complete`
- unzip the WAR file at `build/libs` in `docker/webapps/ROOT`
- Run `docker-compose up` in `docker` directory