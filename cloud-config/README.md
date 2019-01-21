# Aggregate CloudConfig setups

## Supported providers

- [Digital Ocean](digital-ocean)
- [Local VirtualBox for development and testing](local-virtualbox)

## What's included in the stack

This CloudConfig stack should be used with the Ubuntu Cloud 18.04 Linux distribution. It includes the following software:

- Apache Tomcat 8.5
- NGINX 1.14 HTTP server
  - HTTPS support with LetsEncrypt SSL certificates and automatic monthly renewal
- Certbot 0.28
- PostgreSQL 10.6
- ODK Aggregate v2.0 (or greater)
  - ODK Aggregate Updater tool
  
## Useful information

**Access to the machine & users**

- The stack exposes the standard SSH port (22) and the standard HTTP/HTTPS ports (80/443) only.
- You can log into the machine using SSH and the user `odk`.
  - The `odk` user has your SSH public key installed, which grants you the ability to log into the machine without using passwords.
  - If you prefer to log in using a password, run the `passwd` command in your first SSH session to give the `odk`.

**Access to ODK Aggregate**

- ODK Aggregate is the only webapp deployed in Apache Tomcat (in the ROOT webapp slot).
- You will need to provide a domain name for your machine in order to access ODK Aggregate using HTTPS secure connections. 
- You can access ODK Aggregate by browsing http://your.domain or https://your.domain.

## Maintenance tips

**Backups**
- Doing database Backups is strongly recommended.
  - You can produce a backup with `pg_dump odk > odk-aggregate.sql`.
  - You can copy files into/out from your server with the command `scp`.
- You can backup your forms and submissions using [ODK Briefcase](https://docs.opendatakit.org/briefcase-intro/).

**Starting and stopping services**
- You can start/stop/restart services with `sudo service {name of service} (start|stop|restart)`
- Services: `tomcat8`, `nginx`, `postgresql`

**Log files**
- ODK Aggregate logs into `/var/log/tomcat8/catalina.out`.
- The rest of services use the standard `/var/log/` location for their log files.

**Configuration files**
- Always backup the configuration files before changing them.
- ODK Aggregate configuration files are at `/var/lib/tomcat8/webapps/ROOT/WEB-INF/classes/`
  - Stop Apache Tomcat before changing any configuration file here.
- The rest of services use the standard `/etc/` location for their configuration files.

## Using the updater tool
The stack includes the `aggregate-updater` command that will help you update to newer ODK Aggregate versions

- It only supports ODK Aggregate v2.x (or greater) versions.
- You can get help and usage information with `aggregate-updater -h`
- You can list the available versions with `aggregate-updater -l`
  - You can add the pre-releases (beta version) by adding the `-ib` flag to the command

**Updating ODK Aggregate**

- You need to use `sudo` to update ODK Aggregate
- Technically, you can also downgrade to previous versions as long as they are v2.0 or greater.
- Update to the latest release available with `sudo aggregate-updater -u`
- Update to the latest pre-relase available with `sudo aggregate-updater -u -ib`
- There are other params and flags that you can use to perform advanced update operations:
  - Update to a specific available version with `sudo aggregate-updater -u -rv {version}` (add the `-ib` flag if you want to update to a pre-release version)
  - Force the update even if it involves redeploying the same version with the `-f` flag
  - Skip the Tomcat start confirmation step with the `-y` flag



  


