# Aggregate Cloud-Config Setups

## Supported providers

- [Digital Ocean](digital-ocean)
- [VirtualBox](virtualbox) (for local development and testing)

## What's included in the stack

This Cloud-Config stack should be used with Ubuntu 18.04. It includes the following software:

- Apache Tomcat 8.5
- NGINX 1.14 HTTP server
- Certbot 0.28
  - Lets Encrypt SSL certificates with automatic monthly renewal
- PostgreSQL 10.6
- ODK Aggregate v2.0 (or greater)
  - ODK Aggregate CLI tool
  
## Access

**Access to the machine and users**
- The stack only exposes the standard SSH port (22) and the standard HTTP/HTTPS ports (80/443).
- You can log into the machine using SSH and the user `root`.

**Access to ODK Aggregate**
- ODK Aggregate is the only webapp deployed in Apache Tomcat (in the ROOT webapp folder).
- You will need to provide a domain name for your machine in order to access ODK Aggregate using secure HTTPS connections. 
- You can access ODK Aggregate by going to http://your.domain or https://your.domain.

There could be some differences with the provider you use. Check the specific README file for more information. 

## Updates

The stack includes the `aggregate-cli` command that will help you update to newer ODK Aggregate versions.

**Basic information** 
- The updater only supports ODK Aggregate v2.x (or greater) versions.
- You can get help and usage information with `aggregate-cli -h`.
- You can list the available versions with `aggregate-cli -l`.
  - You can add the pre-releases by adding the `-ip` flag to the command.

**Updating Aggregate** 
- You need to use `sudo` to update ODK Aggregate.
- Update to the latest release available with `sudo aggregate-cli -c /root/aggregate-config.json -u`.
- Update to the latest pre-release available with `sudo aggregate-cli -c /root/aggregate-config.json -u -ip`.
- Update to the latest pre-release available with `sudo aggregate-cli -c /root/aggregate-config.json -u -ip`.
- You can also downgrade to previous versions as long as they are v2.0 or greater.

**Advanced operations** 
- Update to a specific available version with `sudo aggregate-cli -c /root/aggregate-config.json -u -rv {version}` (add the `-ip` flag if you want to update to a pre-release version).
- Force the update even if it involves redeploying the same version with the `-f` flag.
- Skip the Tomcat start confirmation step with the `-y` flag.

## Maintenance

**Backups**
- We strongly recommend you backup your forms and submissions using [ODK Briefcase](https://docs.opendatakit.org/briefcase-intro/).
- Doing database backups is also recommended.
  - You can produce a backup with `pg_dump aggregate > aggregate.sql`.

**Log files**
- ODK Aggregate logs into `/var/log/tomcat8/catalina.out`.
- The rest of the services use the standard `/var/log/` location for their log files.

**Configuration files**
- Always backup the configuration files before changing them.
- ODK Aggregate configuration files are at `/var/lib/tomcat8/webapps/ROOT/WEB-INF/classes/`.
  - Stop Apache Tomcat before changing any configuration files.
- The rest of the services use the standard `/etc/` location for their configuration files.

**Starting and stopping services**
- You can start/stop/restart services with `sudo service {name of service} (start|stop|restart)`.
- Services: `tomcat8`, `nginx`, `postgresql`
