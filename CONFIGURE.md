# Configuring your Build Environment

This is a multi-project Gradle project.

## Common module

This module hols common basecode for Aggregate. It contains the GWT web client and server side APIs.

## Tomcat module

This module produces a WAR artifact that can be deployed in a Tomcat 8.0 server

## Google App Engine (`gae`) module

This module produces a WAR artifact that can be deployed in Google App Engine

**Note:** This module probably needs some work yet 

## Functional tests (`functionalTests`) module

This module holds some functional tests that were once inside an `it` folder. They're selenium and `WebProxy` tests that use an actual running Aggregate instance. See [Functional tests module instructions][functional_tests_instructions] for more info.  

# Individual Configuration Guides

- [IntelliJ setup][intellij_setup]
- [Supported database configurations][database_configurations]

# Critically Important Configuration Notes!!!!

## You MUST use Java 7.

Java 8 (and anything other than Servlet 2.5) are not supported by AppEngine.
While the codebase now uses Tomcat8 (Servlet 3.1) for non-AppEngine deployments,
it does not make use of functionality above Servlet 2.5 for this reason.
If you use Java 8, Springframework will use a blacklisted reflection API
and you will NOT be able to run the server locally under AppEngine or
deploy it remotely.

## When running Google's AppEngine Eclipse plug-in and development server, the plug-in will scan files and delete any that contain Servlet-3.x functionality.

Unfortunately, this includes the spring-web jar. To work around this, you must
explode the jar, remove the overview.html and WEB-INF/web-fragment.xml files,
and then re-construct the jar.  To work around this problem, there is a
suitably modified jar here: war-base/WEB-INF/lib/spring-web-SEE-CONFIGURE-TXT-....jar


[intellij_setup]: https://github.com/opendatakit/aggregate/blob/master/docs/intellij_setup.md
[database_configurations]: https://github.com/opendatakit/aggregate/blob/master/docs/database_configurations.md
[functional_tests_instructions]: https://github.com/opendatakit/aggregate/blob/master/functionalTests/README.md
