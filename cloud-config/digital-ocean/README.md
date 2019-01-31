This Cloud-Config setup is intended to be used with Ubuntu 18.04 droplets in [Digital Ocean](https://cloud.digitalocean.com)

## Requirements

- Select the Ubuntu 18.04 distribution for your new Droplet
- Be able to link a domain name to the Droplet's IP address

## Instructions

### 1 - Create your Droplet  

- Head to https://www.digitalocean.com and log in.

- Start the process to create a new Droplet
  
- Select the distribution for your new Droplet: Select the option `18.04` from the Ubuntu box.

  ![Digital Ocean - Selecting the Droplet's distribution](assets/DO_ubuntu_distribution_selection.png)
  
- Choose a size fit for your intended usage. The standard $5 droplet size should be enough for light ODK Aggregate operations, although you might need to choose bigger sizes for extra storage, or if you expect a more intensive usage.

  ODK Aggregate will exclusively use the storage built into your droplet. Don't enable any extra block storage.

- Choose a datacenter region close to where data collection is going to happen.  
  
- Check the `User Data` checkbox under the `Select additional options` section.

- Copy the contents of the Cloud-Config script at [cloud-config/assets/cloud-config.yml](cloud-config/assets/cloud-config.yml):
  
  ![Digital Ocean - Inserting Cloud-Config script under User Data section](assets/DO_user_data_and_cloud_config.png)

- Use the domain you want to use as the Droplet's name on the `Choose a hostname` section.

  **Important**: This data will be used by the Cloud-Config script to configure your server's domain name. You have to use the same domain to enable SSL in step 4.

- Click on the `create` button

Although the creation of the Droplet itself takes just some seconds to complete, **the actual ODK Aggregate installation will take up to 10 minutes to complete**.
  
In the meantime, you can continue with the next steps.

### 2 - Set up your domain

Once the Droplet is running, take note of its public IP address and set a *DNS A record* pointing to it:

- DigitalOcean [How to manage DNS records - A records](https://www.digitalocean.com/docs/networking/dns/how-to/manage-records/#a-records).

- Check your provider's instructions if your domain is not hosted or managed by DigitalOcean.

- If you don't own a domain, services such as [FreeDNS](https://freedns.afraid.org) offer creating subdomains under a range of domains for free.

- Your domain's TTL setting (which oftentimes is fixed by your provider) will affect to how much time you will have to wait until you can proceed to step 5. A TTL value of `3600` means that a change will take up to one hour (3 600 seconds) to propagate.

  If your provider gives you the option of setting a TTL, use the lowest value you can.

**You won't be able to continue the installation process until the changes to the domain have been propagated**

### 3 - Wait until the installation of ODK Aggregate is completed

Open a web browser, and check periodically the domain you've configured until you see the ODK Aggregate website showing up.

### 4 - Enable SSL

- SSH into your Droplet using `ssh root@your.domain.com`
- Run the command: `certbot run --nginx --non-interactive --agree-tos -m {YOUR_EMAIL} --redirect -d {THE_DOMAIN}`

  Be sure to replace `{YOUR_EMAIL}` and `{THE_DOMAIN}` with the actual values you want to use. LetsEncrypt uses the email you provide to send notifications about expiration of certificates.
  
### 5 - Check Aggregate

- Go to https::{THE_DOMAIN} and check that Aggregate is running.
- **Don't forget to change the administrator account's password**
