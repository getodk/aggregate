# ODK Aggregate - Configuration files

## Main configuration file

The main configuration file is located at `src/main/resources/security.properties` in this codebase, or at `WEB-INF/classes` in an exploded WAR webapp.

This file supports the following configuration settings:

**security.server.deviceAuthentication**
- Accepted values: `basic`, `digest`
- Default value: `digest`

  Sets the authentication method that devices should use when authenticating an HTTP session with Aggregate.
  
  Use the `basic` setting with caution. Uploading submissions with attachments can be unstable.
  
**security.server.secureChannelType**
**security.server.channelType**

- Accepted values: `ANY_CHANNEL`, `REQUIRES_INSECURE_CHANNEL`, `REQUIRES_SECURE_CHANNEL`
- Default value: `REQUIRES_INSECURE_CHANNEL`
  
  Sets whether to secure all communication with HTTPS or to allow HTTP.
  
  The `REQUIRES_SECURE_CHANNEL` requires to set up SSL in your server.
    
**security.server.forceHttpsLinks*
- Accepted values: `true`, `false`
- Default value: `false`
  
  Set to `true` when Aggregate is configured to allow HTTP but there's a load balancer or proxy in front providing HTTPS.
  
  When set to `true`, Aggregate will always produce links with HTTPS scheme.
   
**security.server.hostname**
- Accepted values: Empty, a DNS domain name, or an IP address
- Default: empty
  
  Set this to the value best identifies the host machine that's serving Aggregate.
  
  When set to an empty value, Aggregate will respond to all request addressed to any name or IP address that identifies with its host machine.
  
**security.server.port**
- Accepted values: valid port numbers
- Default: `8080`
  
  Set this to the HTTP port you expect your users will be using when accessing this Aggregate server.
  
  Normally, this would be the same port configured in Apache Tomcat, but setups involving virtual machines, load balancers, or proxies could call for different port numbers.  

**security.server.securePort**
- Accepted values: valid port numbers
- Default: `8443`
  
  Set this to the HTTPS port you expect your users will be using when accessing this Aggregate server.
  
  Normally, this would be the same port configured in Apache Tomcat, but setups involving virtual machines, load balancers, or proxies could call for different port numbers.  

**security.server.superUserUsername**
- Accepted values: valid username (no spaces, no special or punctuation characters)
- Default value: `administrator`
  
  Set this to the desired account name for the super user account.

**security.server.realm.realmString**
- Accepted values: any text
- Default value: `ODK Aggregate`
  
  Set this to the security realm you want your users to see when logging into Aggregate   

**security.server.checkHostnames*
- Accepted values: `true`, `false`
- Default value: `true`
  
  Set to `false` if you're running Aggregate behind a proxy or a load balancer to avoid infinite redirects.
  
  When set to `true`, Aggregate will always check the request's hostname and redirect to the configured or detected hostname (`security.server.hostname`) if they don't match.


## Database configuration

Please refer to [Database configurations](database-configurations.md) document where this is covered in detail.
