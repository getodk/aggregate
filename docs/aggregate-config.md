# ODK Aggregate - Configuration files

## Database configuration

Please refer to [Database configurations][database-configurations] document where this is covered in detail.

## Other configurations

Aggregate uses a `security.properties file` to configure different security and networking settings. Check the following table:

| Key                        | Description        | 
| -------------------------- | ------------------ | 
| security.server.hostname   | When running under Tomcat, you need to set the hostname and port for the server so that the background tasks can generate properly-constructed links in their documents and in their publications to the external services. If left blank, Aggregate discovers an IP address |
| security.server.port       | Port to be used for HTTP service |
| security.server.securePort | Port to be used for HTTPS service |   

[database-configurations]: ./database-configurations.md
