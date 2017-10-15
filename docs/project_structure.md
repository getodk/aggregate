# ODK Aggregate - Project structure

This is a multi-project Gradle project.

## Common module

This module holds common basecode for Aggregate. It contains the GWT web client and server side APIs.

## Tomcat module

This module produces a WAR artifact that can be deployed in a Tomcat 8.0 server

## Google App Engine (gae) module

This module produces a WAR artifact that can be deployed in Google App Engine.

**Note:** This module probably needs some work yet. Feedback is very welcome.

## Functional tests (functionalTests) module

This module holds some functional tests that were once inside [`src/it`][old_it] folder. They're Selenium and `WebProxy` tests that use an actual running Aggregate instance. See [Functional tests module instructions][functional_tests_instructions] for more info.  

# Individual Configuration Guides

- [IntelliJ setup][intellij_setup]
- [Aggregate configuration][aggregate_config]
- [Supported database configurations][database_configurations]

# Critically Important Configuration Notes!!!!

## You MUST use Java 7.

Java 8 (and anything other than Servlet 2.5) are not supported by AppEngine.
While the codebase now uses Tomcat8 (Servlet 3.1) for non-AppEngine deployments,
it does not make use of functionality above Servlet 2.5 for this reason.
If you use Java 8, Springframework will use a blacklisted reflection API
and you will NOT be able to run the server locally under AppEngine or
deploy it remotely.

[intellij_setup]: https://github.com/ggalmazor/aggregate/blob/gradle_submodules/docs/intellij_setup.md
[aggregate_config]: https://github.com/ggalmazor/aggregate/blob/gradle_submodules/docs/aggregate_config.md
[database_configurations]: https://github.com/ggalmazor/aggregate/blob/gradle_submodules/docs/database_configurations.md
[functional_tests_instructions]: https://github.com/ggalmazor/aggregate/blob/gradle_submodules/functionalTests/README.md

[old_it]: https://github.com/opendatakit/aggregate/tree/6f4b92e0ee64937c327fd5c862fe4da331ead69c/src/it