# ODK Aggregate - Build and run a Virtual Machine

The Gradle task `packerBuild` will produce an OVA file with:

- Tomcat 8.0
- PostgreSQL 9.6
- ODK Aggregate deployed as ROOT webapp

## How to build the VM

- Configure the correct Packer binary package by editing the `gradle.properties` file at the root of the project. If it doesn't exist, create it by copying the `gradle.properties.example` in its place.
- Set the value of the `packerZip` property according to your host's operating system. The list of binary packages is available at https://releases.hashicorp.com/packer. **Use version 1.2.1 or greater**. 
  - Example for Linux hosts: `packerZip=https://releases.hashicorp.com/packer/1.2.1/packer_1.2.1_linux_amd64.zip`   
    
- Build the VM with `./gradlew clean build packerBuild -xtest -PwarMode=complete`. The VM OVA will be created in the `build/packer/build` directory.

## VM Networking

The VM is configured to use a NAT network device by default and will make the following services accessible:

| Service | Host port | Guest port |
| --- | --- | --- |
| HTTP | 10080 | 80 |
| HTTPS | 10443 | 443 |

# Advanced topics

## Root password

- The default password for the `root` user is `aggregate`. You will be asked to change the password for the `root` user after the first login.

## Network configuration

- If you need to change the network configuration, log into your Aggregate VM using a terminal and run `aggregate-config`. 
  
  This is a tool that lets you configure your VM to use different FQDN and ports. You can run the tool without arguments to get a help message.
  
  You need to use this tool if you want to use a bridged network device in your VM (or to switch back to a NAT network device)
