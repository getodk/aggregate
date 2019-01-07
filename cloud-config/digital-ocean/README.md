This CloudConfig setup is intended to be used with Ubuntu 18.10 droplets in [Digital Ocean](https://cloud.digitalocean.com)

## Requirements

- Select the Ubuntu 18.10 distribution for your new Droplet
- Be able to link a domain name to the Droplet's IP address

## What is included

- Apache Tomcat 8.5.37
- PostgreSQL 10
- Aggregate [latest release](https://github.com/opendatakit/aggregate/releases)
- HTTPS support with LetsEncrypt and automatic renewal
  
## Instructions

**Get your custom CloudConfig script**

- Run the `./build.sh` script providing:
  - The path to your SSH public key as first argument (usually `~/.ssh/id_rsa.pub`)
  - The domain you wish to use as second argument
  - The email linked to the domain you wish to use as third argument

  Example: `./build.sh ~/.ssh/id_rsa.pub aggregate.mycomputer.com`

**Create your Droplet**  

WIP

**Set up your domain**

- Once the Droplet is running it will be assigned an IP address that you must link to the domain you want to use. Follow your DNS provider's instructions for that.
- New domain names usually take some minutes (up to few hours) to propagate. **You won't be able to continue the installation process until the changes to the domain have been propagated**

**Enable SSL**

- SSH into your Droplet using `ssh odk@your.domain.com`
- Run the command: `sudo certbot run --nginx --non-interactive --agree-tos -m {YOUR_EMAIL} --redirect -d {THE_DOMAIN}`

  Be sure to replace `{YOUR_EMAIL}` and `{THE_DOMAIN}` with the actual values you want to use. LetsEncrypt uses the email you provide to send notifications about expiration of certificates.
  
**Check Aggregate**

- Go to https::{THE_DOMAIN} and check that Aggregate is running.
- **Don't forget to change the administrator account's password**

## To do

- Create a param for the Aggregate version
