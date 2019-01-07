This CloudConfig setup is intended for development and testing purposes by deploying Aggregate locally using VirtualBox

## Prerequisites

- Ubuntu 18.04
- VirtualBox
- Required packages:
  - socat
  - qemu-utils 
  - genisoimage 
  - cloud-utils
  
  `sudo apt-get install socat qemu-utils genisoimage cloud-utils`

## Instructions

- Run the `./build.sh` script providing:
  - The path to your SSH public key as first argument (usually `~/.ssh/id_rsa.pub`)
  - Optionally, your host machine's non-loopback (usually 192.168.x.y) IP address as second argument
  
    If you don't provide this argument, the script will try to guess one

  Example: `./build.sh ~/.ssh/id_rsa.pub`
  
  Example: `./build.sh ~/.ssh/id_rsa.pub 192.168.1.129`
  
  This command will build a fresh Aggregate WAR, which will be deployed into the VM
  
- Browse Aggregate at http://{HOST_IP}:10080. In the last example above, this would be [http://192.168.1.129:10080](http://192.168.1.129:10080)

## To do

- Make the host IP detection more robust

  `ip route | grep default | sed -e "s/^.*dev.//" -e "s/.proto.*//"` Shows all non-loopback/virtual devices
  Then we can get the IP with `ip address | grep enp0s31f6 | grep inet`
  
