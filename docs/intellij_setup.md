# ODK Aggregate - IntelliJ Setup

IntelliJ can import Gradle projects selecting their root `build.gradle` file.

Make sure you check `Use auto-import` option in the `Import Project from Gradle` dialog.

Once IntelliJ opens your Aggregate project, you can optionally follow any on-screen popups to configure any detected GWT, Spring or web Facets.

Be sure to read other guides before continuing:

- [Functional tests module instructions][functional_tests_instructions]
- [Aggregate configurations][aggregate_config]
- [Supported database configurations][database_configurations]

## Debugging with Tomcat 8.0

**Requirements**

- These instructions asume that you have installed a Tomcat 8.0 server in your computer. You can download Tomcat 8.0 and see installation instructions [here](https://tomcat.apache.org/download-80.cgi).

- You need to install and activate `Tomcat and TomEE integration` plugin in IntelliJ.

- You have to build Aggregate to be able to choose an existing WAR file for deployment. You can do so with `./gradlew clean build -x test` command

**Instructions**

IntelliJ lets you define a `Run/Debug Configuration` to build Aggregate and deploy in in a local Tomcat 8.0 server. This lets you easily check you changes and even debug server code.

!["Edit Configurations..." in the dropdown menu][intellij_0_png]

We're going to create a new **Run Configuration** in Intellij. Select `Edit Configurations...` from the dropdown menu:

![Selecting "Tomcat Server > Local" option][intellij_1_png]

In the `Run/Debug Configurations` dialog, click on the green plus sign to create a new entry. Find `Tomcat Server > Local` option and click on it.

Be sure to check inside `31 items more (irrelevant)` option if you can't find the `Tomcat Server` option in the dropdown. IntelliJ might hide it the first time. 

![Blank local Tomcat server dialog][intellij_2_png]

Next, in the `Server` tab we are going to tell IntelliJ to trigger a build before running this configuration.

If you haven't configured a Tomcat 8.0 server inside IntelliJ yet, click on `Configure...` button in the `Application server` section (above) and add an entry for your Tomcat 8.0 server. You only need to provide a path to its installation directory.

![Tomcat server entry creation in IntelliJ][intellij_tomcat_png]

Remove any action in the `Before launch` section (below). 

Then, add a `Run Grails task` task and fill in the dialog to run a `./gradlew build -x test` command:

![Gradle task configuration][intellij_3_png]

Back in the dialog, the `Before launch` section should look like this:

![Tomcat server dialog with the build task][intellij_4_png]

This Gradle task will produce the WAR file that we want IntelliJ to deploy using our Tomcat server.

![Choosing an external source for deployment][intellij_5_png]

In the `Deployment` tab, click on the green plus sign and select `External Source...` option. You need to select the WAR file located in `build/libs/aggregate-x.x.x.war`. 

Now the tab should look like this:

![Tomcat 8 deployment configured][intellij_6_png]

Finally, give this configuration a name and click `OK` button.

Now you can run or debug this configuration. Be sure to read and understand [Aggregate configuration files][aggregate_config] before running Aggregate.

Now, you should be able to Debug the server-side code using the Tomcat8 development server. When you are developing, as you change code, you will probably need to start and stop the server.

GWT Dev and SuperDev modes are not supported yet. Help and feedback is very welcome in this matter.  

[database_configurations]: ./database_configurations.md
[functional_tests_instructions]: ../functionalTests/README.md
[aggregate_config]: ./aggregate_config.md

[intellij_0_png]: ./images/intellij_0.png
[intellij_1_png]: ./images/intellij_1.png
[intellij_2_png]: ./images/intellij_2.png
[intellij_3_png]: ./images/intellij_3.png
[intellij_4_png]: ./images/intellij_4.png
[intellij_5_png]: ./images/intellij_5.png
[intellij_6_png]: ./images/intellij_6.png
[intellij_tomcat_png]: ./images/intellij_tomcat.png
