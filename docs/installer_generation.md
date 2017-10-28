# ODK Aggregate - Installer generation

ODK Aggregate uses BitRock InstallBuilder to generate installer apps for Windows, Linux and Mac hosts.

The InstallBuilder project files are located under `bitrock-installer` directory.

## Requirements

**BitRock InstallBuilder**

You can download it from [this website](https://installbuilder.bitrock.com/).

**Maven**

At the moment, the project leverages Maven to prepare it for the InstallBuilder application.

**Notice:** This will be replaced by gradle tasks on the main Aggregate project

## Instructions

1. Delete any WAR file present at `bitrock-installer` directory
2. Generate a WAR file without any configuration files in it using `gradle clean build -x test -Pwar_mode=installer` command
3. copy the generated WAR file at `build/lib/aggregate-%VERSION%.war` to `bitrock-installer` directory
4. Run `mvn clean package -f bitrock-installer/pom.xml`
5. Open BitRock InstallBuilder app and open `bitrock-installer/buildWar.xml`

For more information about using BitRock InstallBuilder, you can check the [online documentation](https://installbuilder.bitrock.com/docs/installbuilder-userguide/index.html)