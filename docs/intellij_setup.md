# ODK Aggregate - IntelliJ Setup

IntelliJ can import Gradle projects selecting their root `build.gradle` file.

Make sure you check `Use auto-import` option in the `Import Project from Gradle` dialog.

Once IntelliJ opens your Aggregate project, you can optionally follow any on-screen popups to configure any detected GWT, Spring or web Facets.

Be sure to read other guides before continuing:

- [Supported database configurations][database_configurations]
- [Aggregate configurations][aggregate_config]
- [Functional tests module instructions][functional_tests_instructions]

## Running and debugging Aggregate for development purposes

Aggregate leverages Gradle's Gretty plugin to start an embedded Jetty or Tomcat instance with Aggregate. **Warning**: This is not intended to be used as a real production server.

### Running Aggregate 

Start a development Aggregate instance by executing the `appStartWar` Gradle task. If you need help to run Gradle tasks, please, read the [Using the Gradle Command-Line](https://docs.gradle.org/current/userguide/tutorial_gradle_command_line.html). 

**Important**: Be sure to run the `appStop` Gradle task to stop a running instance. This will prevent problems with hung Tomcat/Jetty processes.

You should now be able to browse [http://localhost:8080](http://localhost:8080)

### Debugging Aggregate

Start a development Aggregate instance in debug mode by executing the `appStartWarDebug` Gradle task. If you need help to run Gradle tasks, please, read the [Using the Gradle Command-Line](https://docs.gradle.org/current/userguide/tutorial_gradle_command_line.html).

**Important**: Be sure to run the `appStop` Gradle task to stop a running instance. This will prevent problems with hung Tomcat/Jetty processes.

The launch process will then wait for a debugging session to be connected to the embedded server's JVM debugging listener running on port **5005**. 

To do so, create a new `Remote` `Run/Debug configuration` in IntelliJ. The default values will work but make sure that `Search sources using module's classpath` is set to `aggregate` (or the name you gave to your module while creating your IntelliJ project). Give it a name and click `OK`.

Now select the newly created `Remote` `Run/Debug configuration` and click on the debug button next to it.

A debugger session will connect to the debugging listener port and Aggregate will launch.

You should now be able to browse [http://localhost:8080](http://localhost:8080)

[database_configurations]: ./database_configurations.md
[functional_tests_instructions]: ../functionalTest/README.md
[aggregate_config]: ./aggregate_config.md