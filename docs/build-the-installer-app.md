# ODK Aggregate - Build the Installer app

ODK Aggregate uses BitRock InstallBuilder to generate installer apps for Windows, Linux and Mac hosts.

The InstallBuilder project files are located under `installer` directory.

## Requirements

**BitRock InstallBuilder**

You can download it from [this website](https://installbuilder.bitrock.com/).

## Instructions

1. Copy the `gradle.properties.example` file at the root of the project to the same location, removing the `.example` extension

    Open the file and uncomment the line that corresponds to the operating system of your computer. 

2. Build the project for the installer build process with `./gradlew clean build installerBuild -xtest -PwarMode=installer` command

3. Open BitRock InstallBuilder app and open `build/installer/buildWar.xml`

For more information about using BitRock InstallBuilder, you can check the [online documentation](https://installbuilder.bitrock.com/docs/installbuilder-userguide/index.html)

## Automated installer generation

InstallBuilder has a command-line interface that can be used to produce the installers automatically:

```bash
/path/to/installbuilder/bin/builder build build/installer/buildWar.xml linux-x64
/path/to/installbuilder/bin/builder build build/installer/buildWar.xml linux
/path/to/installbuilder/bin/builder build build/installer/buildWar.xml windows
/path/to/installbuilder/bin/builder build build/installer/buildWar.xml osx
```

The installers will be built at `/path/to/installbuilder/output`
