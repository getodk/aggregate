https://portal.azure.com/#blade/HubsExtension/Resources/resourceType/Microsoft.Compute%2FVirtualMachines

**To use this setup, you must able to link a domain name to EC2 machine's IP address. If you don't own a domain, services such as [FreeDNS](https://freedns.afraid.org) offer free sub-domains under a range of domains.**

## Prerequisites

**Warning**: Make sure at all time that you have selected the availability zone where you want to perform your actions. You can choose the availability zone using the dropdown menu at the top-right corner of the AWS console website.

### Create a VPC

- Go to the [Azure Virtual Machines Dashboard](https://portal.azure.com/?l=en.en-us#blade/HubsExtension/Resources/resourceType/Microsoft.Compute%2FVirtualMachines)
  
  Click on `Add`.

- Review the Project Details section.

  Ensure that a `Subscription` and a `Resource group` is selected.
  
- Review the Instance Details section.

  Give your VM a name in the `Virtual Machine name` field.
  
  Select a region.
  
  Select `Ubuntu Server 18.04 LTS` from the `Image` dropdown.
  
  Change the size of the Vm according to your Aggregate needs.
  
- Review the Administrator Account section.

  Fill in the credentials of the user account that will be created during the creation of the VM. You will use this user account to complete the Aggregate installation later.
  
- Review the Inbound Port Rules section.

  Select the `Allow selected ports` option in the `Public inbound ports` field.
  
  Select `HTTP`, `HTTPS`, and `SSH` in the `Select inbound ports` field.
  
- Review the Network configuration.

  Click on the `Networking` tab.
  
  Click on the `Create new` link under the `Public IP` field dropdown, give it a name, and click `OK`.

- Review the Guest Config settings.

  Click on the `Guest Config` tab.
  
  Copy the contents of [Aggregate Cloud Config stack for Azure](https://raw.githubusercontent.com/opendatakit/aggregate/master/cloud-config/azure/cloud-config.yml) into the `Ckoud init` text box.
  
- Review the tags.

  Click on the `Tags` tab.
  
  Add a tag with the `aggregate.hostname` name, and write the DNS domain name you want to use as the value.
  
- Launch the VM

  Click on the `Review + Launch` tab.
  
  Click on `Launch`.

## Set up your domain

1. Take note of the public IP address of your VM (e.g., 12.34.56.79) and set a *DNS A record* pointing to it.

  You can get your instance's IP address on the [Azure Virtual Machines Dashboard](https://portal.azure.com/?l=en.en-us#blade/HubsExtension/Resources/resourceType/Microsoft.Compute%2FVirtualMachines). After clicking on the VM from the list, check the value of the `Public IP address` field.
  
	* If you own a domain, check your domain registrar's instructions.
	* If you don't own a domain, we recommend using [FreeDNS](https://freedns.afraid.org) to get a free sub-domain.

1. Your domain's *TTL* setting will affect to how much time you will have to wait until you can proceed to the next step.
	* A TTL value of 3600 means that a change will take up to one hour (3,600 seconds) to propagate.
	* If your provider gives you the option of setting a TTL, use the lowest value you can.

1. Open a web browser, and periodically check the domain until you see the ODK Aggregate website.
	* **You won't be able to continue the install until you see the website load**.

#### Enable HTTPS

1. SSH into your VM using `ssh username@ip-address`.
  Use the credentials you defined during the VM configuration.
1. Run the command: `sudo certbot run --nginx --non-interactive --agree-tos -m {YOUR_EMAIL} --redirect -d {YOUR_DOMAIN}`
	* Lets Encrypt uses the email you provide to send notifications about expiration of certificates.
  
#### Log into Aggregate

1. Go to https::{YOUR_DOMAIN} and check that Aggregate is running.
	* **Login and change the administrator account's password!**
