# Minimal Eclipse Installation Setup

See [Full Maven Development Environment Configuration][maven] if you want a fully configured Maven build environment.

You will probably want a computer with at least 8GB of main memory for development purposes.

On Windows, you can confirm the signature of the downloads using certutil. See https://opendatakit.org/help/verifying-downloaded-files/ (This is a good habit for you to adopt).

1. Download and install the Java 7 JDK ( http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html )
1. Download the "Mars.2 (4.5.2)" "Eclipse IDE for Java EE Developers" (be sure it is the Java EE version) ( https://eclipse.org/downloads/ ) we use the 64-bit IDE.
1. Extract Eclipse into a directory (e.g., c:\Users\your-user\eclipse
1. Use Notepad++ or similar editor to edit the eclipse.ini within this folder (this file is in the same directory as the eclipse.exe.). 

    We need to increase the start-up memory sizes for the Eclipse workspace as detailed below. The GAE database consumes huge amounts of space in the JVM and the GAE environment runs two independent copies of the server code to mimic the existence of a foreground and background process. This all requires very large perm space and max memory for the underlying Java environments.

    To do this, change -Xms... and the following lines in that file to:

    ```
    -Xms256m
    -Xmx3048m
    -XX:PermSize=1536m
    -XX:MaxPermSize=1536m
    ```

    This is my eclipse.ini:

    ```
    -startup
    plugins/org.eclipse.equinox.launcher_1.3.100.v20150511-1540.jar
    --launcher.library
    plugins/org.eclipse.equinox.launcher.win32.win32.x86_64_1.1.300.v20150602-1417
    -product
    org.eclipse.epp.package.jee.product
    --launcher.defaultAction
    openFile
    --launcher.XXMaxPermSize
    256M
    -showsplash
    org.eclipse.platform
    --launcher.XXMaxPermSize
    256m
    --launcher.defaultAction
    openFile
    --launcher.appendVmargs
    -vmargs
    -Dosgi.requiredJavaVersion=1.7
    -Xms256m
    -Xmx3048m
    -XX:PermSize=1536m
    -XX:MaxPermSize=1536m
    ```
1.  Download "Google AppEngine SDK for Java". We currently use 1.9.53 https://cloud.google.com/appengine/downloads (scroll down). 
    - Extract the SDK.
1. Download "GWT SDK". We currently use 2.7.0 http://www.gwtproject.org/download.html
    - Extract the SDK.

[maven]: https://github.com/opendatakit/aggregate/blob/master/docs/maven-full.md


