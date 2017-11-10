# ODK Aggregate - IntelliJ Setup

IntelliJ can import Gradle projects selecting their root `build.gradle` file.

Make sure you check `Use auto-import` option in the `Import Project from Gradle` dialog.

Once IntelliJ opens your Aggregate project, you can optionally follow any on-screen popups to configure any detected GWT, Spring or web Facets.

Be sure to read other guides before continuing:

- [Supported database configurations][database_configurations]
- [Aggregate configurations][aggregate_config]
- [Functional tests module instructions][functional_tests_instructions]

## Debugging with embedded Jetty

(WIP)

- Run the server in debug mode with `./gradlew appRunWarDebug`
  - Debug listener should start at port **5005**
- Create a new `Run/Debug Configuration`
  - Choose new `Remote` configuration
  - This configuration links to a debugging process waiting at port **5005** by default
  - Give it a name
  - Click OK
- Debug the `Remote` configuration you've just created

[database_configurations]: ./database_configurations.md
[functional_tests_instructions]: ../functionalTest/README.md
[aggregate_config]: ./aggregate_config.md