**To use this setup, you must be able create DNS domain entries. If you don't own a domain, services such as [FreeDNS](https://freedns.afraid.org) offer free sub-domains under a range of domains.**

## Create the GCE VM

- Go to the [GCE - VM instances dashboard](https://console.cloud.google.com/compute/instances)

  Click on `Create`.

- Set a name.
  
- Select the desired region and zone.
  
- Select the desired machine type.
  
- Click on `Change` under the `Boot disk` section.
  
  - Select `Ubuntu 18.04 LTS`.
      
  - Set the desired storage size for your VM.
  
  - Click on `Select`.
    
- Check `Allow HTTP traffic`, and `Allow HTTPS traffic` under the `Firewall` section.
  
- Click on the `Management, security, disks, networking, sole tenancy`.
  
  - Click on the `Management` tab, and copy the contents of the [Aggregate Startup Script for GCE script](https://raw.githubusercontent.com/opendatakit/aggregate/master/cloud-config/google-cloud/startup-script.sh) into the `Automation` > `Startup script` text box.
    
  - Click on the `Networking` tab, and set the `Hostname` field with the domain name you want to use for Aggregate.
  
- Click on `Launch`.

## Set up your domain

1. Take note of the public IP address of your EC2 machine (e.g., 12.34.56.79) and set a *DNS A record* pointing to it.

  You can find your VM's external IP address on the [GCE - VM instances dashboard](https://console.cloud.google.com/compute/instances), under the `External IP` column.
  
	* If you own a domain, check your domain registrar's instructions.
	* If you don't own a domain, we recommend using [FreeDNS](https://freedns.afraid.org) to get a free sub-domain.

1. Your domain's *TTL* setting will affect to how much time you will have to wait until you can proceed to the next step.
	* A TTL value of 3600 means that a change will take up to one hour (3,600 seconds) to propagate.
	* If your provider gives you the option of setting a TTL, use the lowest value you can.

1. Open a web browser, and periodically check the domain until you see the ODK Aggregate website.
	* **You won't be able to continue the install until you see the website load**.

## Enable HTTPS

1. SSH into your VM clicking the `SSH` button on the [GCE - VM instances dashboard](https://console.cloud.google.com/compute/instances).
1. Run the command: `sudo certbot run --nginx --non-interactive --agree-tos -m {YOUR_EMAIL} --redirect -d {YOUR_DOMAIN}`
	* Lets Encrypt uses the email you provide to send notifications about expiration of certificates.
  
## Log into Aggregate

1. Go to https::{YOUR_DOMAIN} and check that Aggregate is running.
	* **Login and change the administrator account's password!**
	
## Reserve the external IP address

GCE VMs use ephemeral IP addresses which can change under certain circumstances. To ensure your Aggregate instance will always be reachable using the same IP address, you can promote the ephemeral IP address to a static IP address using [these instructions](https://cloud.google.com/compute/docs/ip-addresses/reserve-static-external-ip-address#promote_ephemeral_ip)
