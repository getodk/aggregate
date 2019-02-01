This Cloud-Config setup is intended to be used with Ubuntu 18.04 droplets in [Digital Ocean](https://cloud.digitalocean.com).

**To use this setup, you must able to link a domain name to the Droplet's IP address. If you don't own a domain, services such as [FreeDNS](https://freedns.afraid.org) offer free sub-domains under a range of domains.**

#### Create your Droplet

If you haven't already created a DigitalOcean account, use our referral link to do so: https://m.do.co/c/39937689124c. 

DigitalOcean will give you $100 of credit to spend during the first 60 days so that you can try things out. Once you've spent $25 with them, we'll get $25 to put towards our hosting costs.

1. Log into Digital Ocean and create a new Droplet.
  
1. Select the distribution for your new Droplet: Select the option `18.04 x64` from the Ubuntu box.

	![Digital Ocean - Selecting the Droplet's distribution](assets/DO_ubuntu_distribution_selection.png)
  
1. Choose a size fit for your intended usage.
	* The `$5 Standard Droplet` should be enough for light ODK Aggregate use. If you find yourself needing more, Digital Ocean makes it easy to resize to a bigger Droplet.

1. If you would like automatic weekly backups, enable them.

1. You will not need block storage.

1. Choose a datacenter region physically close to where data collection is going to happen.
  
1. Under `Select additional options`, check the `User data` checkbox.
	* Copy and paste the contents of the Cloud-Config script at [cloud-config/assets/cloud-config.yml](https://raw.githubusercontent.com/opendatakit/aggregate/master/cloud-config/assets/cloud-config.yml):
	![Digital Ocean - Inserting Cloud-Config script under User Data section](assets/DO_user_data_and_cloud_config.png)

1. In the `Choose a hostname` section, enter the domain name (e.g., aggregate.example.com).
	* **This hostname will be used by the Cloud-Config script to configure your server's domain name. You must enter the same domain name to enable HTTPS**.

1. You will not need to add public SSH keys (unless you know what that is and you want to).

1. Click on the `Create` button.
	* The Droplet takes a few seconds, the actual ODK Aggregate installation will take up to 10 minutes to complete.

#### Set up your domain

1. Once the Droplet is running, take note of its public IP address (e.g., 12.34.56.79) and set a *DNS A record* pointing to it.
	* If you own a domain, check your domain registrar's instructions.
	* If you don't own a domain, we recommend using [FreeDNS](https://freedns.afraid.org).

1. Your domain's *TTL* setting will affect to how much time you will have to wait until you can proceed to the next step.
	* A TTL value of 3600 means that a change will take up to one hour (3,600 seconds) to propagate.
	* If your provider gives you the option of setting a TTL, use the lowest value you can.

1. Open a web browser, and periodically check the domain until you see the ODK Aggregate website.
	* **You won't be able to continue the install until you see the website**.

#### Enable HTTPS

1. SSH into your Droplet using `ssh root@your.domain`
	* Your password will be the root password that Digital Ocean emailed you.
	* If you entered your public SSH keys when creating the Droplet, login with your private key.
1. Run the command: `certbot run --nginx --non-interactive --agree-tos -m {YOUR_EMAIL} --redirect -d {YOUR_DOMAIN}`
	* Lets Encrypt uses the email you provide to send notifications about expiration of certificates.
  
#### Log into Aggregate

1. Go to https::{YOUR_DOMAIN} and check that Aggregate is running.
	* **Login and change the administrator account's password!**.
