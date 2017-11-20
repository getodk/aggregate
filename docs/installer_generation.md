# ODK Aggregate - Installer generation

ODK Aggregate uses BitRock InstallBuilder to generate installer apps for Windows, Linux and Mac hosts.

The InstallBuilder project files are located under `installer` directory.

## Requirements

**BitRock InstallBuilder**

You can download it from [this website](https://installbuilder.bitrock.com/).

## Instructions

1. Build the project for the installer build process with `gradle clean build -x test -PwarMode=installer` command
2. Build the installer project with `gradle buildInstaller` command
5. Open BitRock InstallBuilder app and open `build/installer/buildWar.xml`

For more information about using BitRock InstallBuilder, you can check the [online documentation](https://installbuilder.bitrock.com/docs/installbuilder-userguide/index.html)