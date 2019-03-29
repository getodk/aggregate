This Cloud-Config setup is intended to be used with the Ubuntu Server 18.04 LTS AMI in AWS. 

**To use this setup, you must able to link a domain name to EC2 machine's IP address. If you don't own a domain, services such as [FreeDNS](https://freedns.afraid.org) offer free sub-domains under a range of domains.**

## Prerequisites

**Warning**: Make sure at all time that you have selected the availability zone where you want to perform your actions. You can choose the availability zone using the dropdown menu at the top-right corner of the AWS console website. It's recommended to choose a region that's close to the location where data is going to be collected.

### Create a VPC

- Go to the [VPC Dashboard](https://console.aws.amazon.com/vpc/home#dashboard)
  
  Click on `Launch VPC Wizard`.

- Configure the new VPC

  Follow the wizard for the `VPC with a Single Public Subnet` configuration.
  
  Enter `Aggregate VPC` in the `VPC Name` field.
  
  All the defaults are OK. 
  
  Click on `Create VPC`.
  
### Create a security group

- Go to the [VPC - Security groups panel](https://console.aws.amazon.com/vpc/home#SecurityGroups:sort=groupId)

  Click on `Create security group`.

- Create the new security group

  Enter `Aggregate Security Group` as the name and description.
  
  Select the VPC you've created in the previous step.
  
  Click on `create`.
- Set the inbound rules in the security group

  Click on the newly created security group from the list.

  Click on the `Inbound rules` tab, and click on the `Edit rules` button.

  Add the following rules:
  
  | Type | Source |
  | --- | --- |
  | SSH | Anywhere |
  | HTTP | Anywhere |
  | HTTPS | Anywhere |
  
  Click on `Save rules`.

## Create an IAM role

The EC2 machine needs an IAM role to query its tags.

- Go to the [IAM - Roles panel](https://console.aws.amazon.com/iam/home#/roles)

  Click on `Create role`.
- Configure the new role

  Make sure that the `AWS service` box is selected, and click on the `EC2` link.
  
  Click on `Next: Permissions`.
- Add policies to the role

  Search for `AmazonEC2ReadOnlyAccess`, and select it.
  
  Click on `Next: Tags`. (nothing to do here)
  
  Click on `Next: Review`.
- Enter `aggregate_role` as the new role's name.

  Click on `Create role`.

## Create the EC2 machine

- Go to the [EC2 Dashboard](https://console.aws.amazon.com/ec2/v2/home#Home:)
  
  Click on `Launch instance`
- Search for the `Ubuntu Server 18.04` AMI
  
  Make sure you select the `64-bit (x86)` option under the `Select` button.
  
  Click on `Select`.
- Select the instance type you want to use.

  A minimum setup would involve using a `t2.small` instance type (1 vCPU, 2GiB RAM), although you should review your requirements and choose a bigger instance type according to your specific needs.

  Click on `Next: Configure Instance Details`

- Set the instance details.

  Select the VPC you've created in the `Network` dropdown.
  
  Select `Enable` in the `Auto-assign Public IP` dropdown.
  
  Select the IAM role you've created in the previous step from the `IAM role` dropdown.
  
  Toggle the `Advanced Details` section.
  
  Copy the contents of the [Aggregate Cloud Config stack for AWS](https://raw.githubusercontent.com/opendatakit/aggregate/master/cloud-config/aws/cloud-config.yml) into the `User data` text box.

  Click on `Next: Add Storage`.
- Edit the storage settings. 

  A minimum setup would involve having 30 GiB of storage, although you should review your requirements and adjust the value of the `Size (GiB)` field according to your specific needs.
 
  Click on `Next: Add Tags`.
- Add a tag with the domain name you want to use for Aggregate:
  
  - Key: `aggregate.hostname`
  - Value: `aggregate.your-domain.foo`  

  Click on `Next: Configure Security Group`

- Select the `Select an existing security group` option of the `Assign a security group` field

  Then select the security group you've created.
  
  Click on `Review and Launch`.
- Review all the settings.

  When you're ready, click on `Launch`.

  **Important**: At this point, a dialog will pop-up and you will be offered the option of using an existing key pair or creating one. It's very important that you follow the dialog's instructions carefully to be able to access your machine once it's created.

  When you're ready, click on `Launch instances`.

## Set up your domain

1. Take note of the public IP address of your EC2 machine (e.g., 12.34.56.79) and set a *DNS A record* pointing to it.

  You can get your instance's IP address on the [EC2 - Instances panel](https://console.aws.amazon.com/ec2/v2/home#Instances:). After clicking on the instance from the list, check the value of the `IPv4 Public IP` field on the right column, under the `Description` tab.
  
	* If you own a domain, check your domain registrar's instructions.
	* If you don't own a domain, we recommend using [FreeDNS](https://freedns.afraid.org) to get a free sub-domain.

1. Your domain's *TTL* setting will affect to how much time you will have to wait until you can proceed to the next step.
	* A TTL value of 3600 means that a change will take up to one hour (3,600 seconds) to propagate.
	* If your provider gives you the option of setting a TTL, use the lowest value you can.

1. Open a web browser, and periodically check the domain until you see the ODK Aggregate website.
	* **You won't be able to continue the install until you see the website load**.

#### Enable HTTPS

1. SSH into your EC2 machine using `ssh -i /path/to/the/key.pem ubuntu@your.domain`
	* Make sure that your PEM key pair file has the correct file permissions following [these instructions](https://docs.aws.amazon.com/es_es/AWSEC2/latest/UserGuide/TroubleshootingInstancesConnecting.html#troubleshoot-unprotected-key)
1. Run the command: `sudo certbot run --nginx --non-interactive --agree-tos -m {YOUR_EMAIL} --redirect -d {YOUR_DOMAIN}`
	* Lets Encrypt uses the email you provide to send notifications about expiration of certificates.
  
#### Log into Aggregate

1. Go to https::{YOUR_DOMAIN} and check that Aggregate is running.
	* **Login and change the administrator account's password!**
