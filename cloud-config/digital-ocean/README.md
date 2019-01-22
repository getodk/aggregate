This CloudConfig setup is intended to be used with Ubuntu 18.04 droplets in [Digital Ocean](https://cloud.digitalocean.com)

## Requirements

- Select the Ubuntu 18.04 distribution for your new Droplet
- Be able to link a domain name to the Droplet's IP address

## Instructions

### 1 - Get your custom CloudConfig script

**Option 1: Use the CloudConfig script builder script**

- Run the `./build.sh` script providing:
  1. The path to your SSH public key (usually `~/.ssh/id_rsa.pub`)
  2. The domain you wish to use 
  3. The ODK Aggregate version you want to deploy

  Example: `./build.sh ~/.ssh/id_rsa.pub aggregate.mycomputer.com v2.0.0`
  
- You will find your CloudConfig script inside a new `cloud-config.yml` file in the same folder.
  
**Option2: Use a template**

Download the CloudConfig template from [here](../assets/cloud-config.yml.tpl) and replace the variables below.

The variables you need to replace follow the format `{{name}}` and are:

| Variable | Description | Example value |
| --- | --- | --- |
| `{{pubKey}}` | SSH public key to access the machine once it's running | `ssh-rsa AAAAB3NzaC1yc2EAAAADAQ (...some chars omited...) FWP9LG0xMK3uZhEriN6Gsn3PMkIj user@servername` |
| `{{aggregateVersion}}` | ODK Aggregate version you want to deploy| `v2.0.0` |
| `{{domain}}` | The domain under which you will be serving ODK Aggregate | `aggregate.opendatakit.org` |
| `{{forceHttps}}` | Force using the https:// schema when producing links. **Always set this to `true`** | `true` |
| `{{httpPort}}` | The port http port number to be used. **Always set this to `80`** | `80` |
| `{{aggregateWarUrl}}` | The URL to be used to download ODK Aggregate. **Always set this to `https://github.com/opendatakit/aggregate/releases/download/{{aggregateVersion}}/ODK-Aggregate-{{aggregateVersion}}.war`** | `https://github.com/opendatakit/aggregate/releases/download/v2.0.0/ODK-Aggregate-v2.0.0.war` |


### 2 - Create your Droplet  

- Head to https://www.digitalocean.com and log in.

- Start the process to create a new Droplet
  
- Select the distribution for your new Droplet: Select the option `18.04` from the Ubuntu box.

  ![Digital Ocean - Selecting the Droplet's distribution](README_assets/DO_ubuntu_distribution_selection.png)
  
- Choose a size fit for your intended usage. The standard $5 droplet size should be enough for light ODK Aggregate operations, although you might need to choose bigger sizes for extra storage, or if you expect a more intensive usage.

  ODK Aggregate will exclusively use the storage built into your droplet. Don't enable any extra block storage.

- Choose a datacenter region close to where data collection is going to happen.  
  
- Check the `User Data` checkbox under the `Select additional options` section.

- Copy the CloudConfig script you obtained on the first step of these instructions inside the new empty big textbox that will appear.
  
  ![Digital Ocean - Inserting CloudConfig script under User Data section](README_assets/DO_user_data_and_cloud_config.png)

- Give your Droplet a name on the `Choose a hostname` section.

- Click on the `create` button

Although the creation of the Droplet itself takes just some seconds to complete, **the actual ODK Aggregate installation will take up to 10 minutes to complete**.
  
In the meantime, you can continue with the next steps.

### 2 - Set up your domain

- Once the Droplet is running it will be assigned an IP address that you must link to the domain you want to use. Follow your DNS provider's instructions for that.
- New domain names usually take some minutes (up to few hours) to propagate. **You won't be able to continue the installation process until the changes to the domain have been propagated**

### 3 - Wait until the installation of ODK Aggregate is completed

Open a web browser, and check periodically the domain you've configured until you see the ODK Aggregate website showing up.

### 4 - Enable SSL

- SSH into your Droplet using `ssh odk@your.domain.com`
- Run the command: `sudo certbot run --nginx --non-interactive --agree-tos -m {YOUR_EMAIL} --redirect -d {THE_DOMAIN}`

  Be sure to replace `{YOUR_EMAIL}` and `{THE_DOMAIN}` with the actual values you want to use. LetsEncrypt uses the email you provide to send notifications about expiration of certificates.
  
### 5 - Check Aggregate

- Go to https::{THE_DOMAIN} and check that Aggregate is running.
- **Don't forget to change the administrator account's password**
