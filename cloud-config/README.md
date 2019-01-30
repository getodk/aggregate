# Aggregate Cloud-Config Setups

## Supported providers

- [Digital Ocean](digital-ocean)
- [Local VirtualBox for development and testing](local-virtualbox)

## What's included in the stack

This Cloud-Config stack should be used with Ubuntu 18.04. It includes the following software:

- Apache Tomcat 8.5
- NGINX 1.14 HTTP server
- Certbot 0.28
  - LetsEncrypt SSL certificates with automatic monthly renewal
- PostgreSQL 10.6
- ODK Aggregate v2.0 (or greater)
  - ODK Aggregate Updater tool
  
## Access

**Access to the machine and users**
- The stack only exposes the standard SSH port (22) and the standard HTTP/HTTPS ports (80/443).
- You can log into the machine using SSH and the user `aggregate`.
  - The `aggregate` user has your SSH public key installed, which grants you the ability to log into the machine without using passwords.
  - If you prefer to log in using a password, run the `passwd` command in your first SSH session to give the `aggregate` user a password.

**Access to ODK Aggregate**
- ODK Aggregate is the only webapp deployed in Apache Tomcat (in the ROOT webapp folder).
- You will need to provide a domain name for your machine in order to access ODK Aggregate using secure HTTPS connections. 
- You can access ODK Aggregate by going to http://your.domain or https://your.domain.

## Updates

The stack includes the `aggregate-updater` command that will help you update to newer ODK Aggregate versions.

**Basic information** 
- The updater only supports ODK Aggregate v2.x (or greater) versions.
- You can get help and usage information with `aggregate-updater -h`.
- You can list the available versions with `aggregate-updater -l`.
  - You can add the pre-releases (beta version) by adding the `-ib` flag to the command.

**Updating Aggregate** 
- You need to use `sudo` to update ODK Aggregate.
- Update to the latest release available with `sudo aggregate-updater -u`.
- Update to the latest pre-release available with `sudo aggregate-updater -u -ib`.
- You can also downgrade to previous versions as long as they are v2.0 or greater.

**Advanced operations** 
- Update to a specific available version with `sudo aggregate-updater -u -rv {version}` (add the `-ib` flag if you want to update to a pre-release version).
- Force the update even if it involves redeploying the same version with the `-f` flag.
- Skip the Tomcat start confirmation step with the `-y` flag.

## Maintenance

**Backups**
- We strongly recommend you backup your forms and submissions using [ODK Briefcase](https://docs.opendatakit.org/briefcase-intro/).
- Doing database backups is also recommended.
  - You can produce a backup with `pg_dump aggregate > odk-aggregate.sql`.

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