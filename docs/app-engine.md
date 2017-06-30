# Minimal AppEngine Eclipse Setup

This assumes you have completed [**"Minimal Eclipse Installation Setup"**][eclipse]

1. Start Eclipse (Mars) and select the cloned source code directory/folder as the workspace folder.
1. Go to Help / Install New Software.
   - Choose Add...
   - And register an entry for this URL  https://dl.google.com/eclipse/plugin/4.4

   (While this is for Luna, it also supports Mars and Neon). After registering, when it presents available software to install, do not install the SDKs. At a minimum, you need to install:
 
    Google Plugin for Eclipse / Google Plugin for Eclipse 4.4/4.5/4.6
 
    (you don't need anything else)

    Proceed with installing this and restarting Eclipse.
1. Re-open Eclipse, go to Window / Preferences

   - Open Google / App Engine and add the AppEngine SDK path that you downloaded and exploded in (5) in earlier section
   - Open Google / Web Toolkit and add the GWT SDK path of what you downloaded and exploded in (6) in earlier section
   - Choose OK to accept changes and close the preferences dialog.
1. Once again, go to Window / Preferences

   - Open Server /Runtime Environment
   - Select Google AppEngine
   - If it complains about not having an AppEngine SDK then delete this and Select Add, choose Google / AppEngine, and accept the defaults to re-create it.
   - Choose OK to accept changes and close the preferences dialog.

   (At this point, Eclipse has the basic AppEngine / GWT configuration.)

1. If you haven't already, go to the Workbench view. Then,
   - Import / Import... / General / Existing Projects into Workspace
   - import these existing projects:
     - odk-gae-settings
     - eclipse-default
     - eclipse-n-background
     - eclipse-ear
1. Within odk-gae-settings, open common and edit security.settings

   Update the security.server.hostname to correspond to your system IP address. Remember the IP address. You could use "localhost" for this if you are not attempting to debug the communications between ODK Aggregate and a device (i.e., only working on the web UI).

   You can find out your IP address by opening a cmd window and running ipconfig (or, on Mac/Linux, running ifconfig -a).

    By default, when running the AppEngine Development Server, it uses port 8888.

    Be sure that this file has

    ```
    security.server.port=8888
    ```

    Also in this file, edit the value for:

    ```
    security.server.superUserUsername=msundt
    ```

    To be a username that you will use as your server's privileged super-user.

    Save changes.
1. Select build.xml within odk-gae-settings. Right-click, Run As / Ant Build
1. Select eclipse-default, right-click, Refresh
1. Select eclipse-n-background, right-click, Refresh
1. Choose File / New / Other... / Server / Server
    - Select Google / Google App Engine as the server type
    - Enter your computer's IP address as the Server's host name.
    - Click Next.
    - Enter 0 in HRD unapplied job percentage.
    - Click Next.
    - Add eclipse-ear to the configured resources.
    - Click Finish.
1. Now, Select eclipse-default, right-click, Properties
    - Select "Deployment Assembly"
    - Select any "Google App Engine Web Libraries" or "Google App Engine" entries and remove them.
    - Select and remove the "App Engine SDK" entry.
    - Click Apply
    - Go to Java Build Path / Libraries
    - Select "Server Library" and remove it. 
    - Select any "Google App Engine Web Libraries" or "Google App Engine" entries and remove them.
    - Select and remove the "App Engine SDK" entry.
    - Click "Add Library"
    - Choose Server Runtime / Google AppEngine (1.9.42)
    - And accept it (this will create a Google App Engine entry).
    - Click "Add Library"
    - Choose Google App Engine
    - And accept it (this will create an App Engine SDK entry).
    - Go to Java Build Path / Order and Export
    - Ensure that all of the below are checked and in the following order:
      - Web App Libraries
      - GWT
      - App Engine SDK
    - Click Apply
    - Select "Deployment Assembly"
    - Choose Add... / Java Build Path Entries
    - Select AppEngine SDK
    - Click Apply (you may need to repeat this twice to get it to remain)
    - Select "Project Facets"
    - Choose "Google AppEngine (for single module)"
    - Click on the Runtimes tab.
    - Check the "Google App Engine" entry that has the stylized airplane icon. (if there is one that looks like a server box, that App Engine sdk is missing from your workspace; uncheck it).
    - Click Apply
    - Choose "Server"
    - Choose the server that you configured earlier (in step (10)).
    - Click Apply
    - Click OK
1. Now, Select eclipse-n-background, right-click, Properties

   Repeat all the steps in (11) above.
1. Now, Select eclipse-ear, right-click, Properties
    - Select "Project Facets"
    - Choose "Google AppEngine (for multiple modules)"
    - Click on the Runtimes tab.
    - Check the "Google App Engine" entry that has the stylized airplane icon. (the one that looks like a server box is missing from your workspace; uncheck that).
    - Click Apply
    - Choose "Server"
    - Choose the server that you configured earlier (in step (10)).
    - Click Apply
    - Click OK
1. Select eclipse-default, right-click Google / GWT Compile
    - open Advanced
    - Verify that "Additional compiler arguments" has: `war WebContent`
    - Verify that VM Arguments has: `-Xmx512m`
    - Choose Compile.
    - Wait for the compile to complete (it is compiling the Java code into 5 different sets of Javascript to support 5 different browsers).
1. Select eclipse-default, right-click Refresh to pick up the changes to the compiled GWT files (this is important!!!)
1. Create an explicit development server configuration
    - Select eclipse-ear, right-click, Debug As / Debug Configurations
    - Double-click to choose Google App Engine
    - Rename "New_Configuration" to something of your choosing (e.g., devServerGAE)
    - Click Apply
    - Close.
1. Now, choose eclipse-ear, right-click, Debug As / Debug on Server

    This will initialize and start the server configuration you created above. It will fail with an out-of-memory error.
    
    Stop the server by clicking the red square box on the Console output window.
1. Select eclipse-ear, right-click, Debug As / Debug Configurations
    - Select the Google App Engine configuration you created in step (16).
    - Select the (X)= Arguments tab.
    - In the Program Arguments section, add: `--address=0.0.0.0` before the `--port` entry.
    - In the VM Arguments section, replace "-Xmx512m" with `"-XX:PermSize=1536m -XX:MaxPermSize=1536m -Xms256m -Xmx3048m "`
    - Click Apply
    - Choose Close
    - If you've done this correctly, the Program Arguments would be something like:

        ```
        --address=0.0.0.0 --port=8888 --disable_update_check [path...to...]\.metadata\.plugins\org.eclipse.wst.server.core\tmp0\eclipse-ear
        ```

         And the VM Arguments section should become something like:

         ```
         -javaagent:[path...to...]\appengine-java-sdk-1.9.53\lib\agent\appengine-agent.jar -XX:PermSize=1536m -XX:MaxPermSize=1536m -Xms256m -Xmx3048m  -Dappengine.fullscan.seconds=5
         ```

         If you tried to skip a step, the server might not be configured correctly.

    *NOTE*: if you need to preserve the datastore across cleans and publishing, you can add to the Program Arguments:

    ```
       --generated_dir=[path to]\generated
    ```

    and the datastore and index hints file will be written to the directory 'generated' at location.

    (after Applying these changes, choose Close if you haven't already)
1. Select the Servers tab in the output area.
    - Right-click on the server y# Minimal AppEngine Eclipse Setup

This assumes you have completed [**"Minimal Eclipse Installation Setup"**][eclipse]

1. Start Eclipse (Mars) and select the cloned source code directory/folder as the workspace folder.
1. Go to Help / Install New Software.
   - Choose Add...
   - And register an entry for this URL  https://dl.google.com/eclipse/plugin/4.4

   (While this is for Luna, it also supports Mars and Neon). After registering, when it presents available software to install, do not install the SDKs. At a minimum, you need to install:
 
    Google Plugin for Eclipse / Google Plugin for Eclipse 4.4/4.5/4.6
 
    (you don't need anything else)

    Proceed with installing this and restarting Eclipse.
1. Re-open Eclipse, go to Window / Preferences

   - Open Google / App Engine and add the AppEngine SDK path that you downloaded and exploded in (5) in earlier section
   - Open Google / Web Toolkit and add the GWT SDK path of what you downloaded and exploded in (6) in earlier section
   - Choose OK to accept changes and close the preferences dialog.
1. Once again, go to Window / Preferences

   - Open Server /Runtime Environment
   - Select Google AppEngine
   - If it complains about not having an AppEngine SDK then delete this and Select Add, choose Google / AppEngine, and accept the defaults to re-create it.
   - Choose OK to accept changes and close the preferences dialog.

   (At this point, Eclipse has the basic AppEngine / GWT configuration.)

1. If you haven't already, go to the Workbench view. Then,
   - Import / Import... / General / Existing Projects into Workspace
   - import these existing projects:
     - odk-gae-settings
     - eclipse-default
     - eclipse-n-background
     - eclipse-ear
1. Within odk-gae-settings, open common and edit security.settings

   Update the security.server.hostname to correspond to your system IP address. Remember the IP address. You could use "localhost" for this if you are not attempting to debug the communications between ODK Aggregate and a device (i.e., only working on the web UI).

   You can find out your IP address by opening a cmd window and running ipconfig (or, on Mac/Linux, running ifconfig -a).

    By default, when running the AppEngine Development Server, it uses port 8888.

    Be sure that this file has

    ```
    security.server.port=8888
    ```

    Also in this file, edit the value for:

    ```
    security.server.superUserUsername=msundt
    ```

    To be a username that you will use as your server's privileged super-user.

    Save changes.
1. Select build.xml within odk-gae-settings. Right-click, Run As / Ant Build
1. Select eclipse-default, right-click, Refresh
1. Select eclipse-n-background, right-click, Refresh
1. Choose File / New / Other... / Server / Server
    - Select Google / Google App Engine as the server type
    - Enter your computer's IP address as the Server's host name.
    - Click Next.
    - Enter 0 in HRD unapplied job percentage.
    - Click Next.
    - Add eclipse-ear to the configured resources.
    - Click Finish.
1. Now, Select eclipse-default, right-click, Properties
    - Select "Deployment Assembly"
    - Select any "Google App Engine Web Libraries" or "Google App Engine" entries and remove them.
    - Select and remove the "App Engine SDK" entry.
    - Click Apply
    - Go to Java Build Path / Libraries
    - Select "Server Library" and remove it. 
    - Select any "Google App Engine Web Libraries" or "Google App Engine" entries and remove them.
    - Select and remove the "App Engine SDK" entry.
    - Click "Add Library"
    - Choose Server Runtime / Google AppEngine (1.9.42)
    - And accept it (this will create a Google App Engine entry).
    - Click "Add Library"
    - Choose Google App Engine
    - And accept it (this will create an App Engine SDK entry).
    - Go to Java Build Path / Order and Export
    - Ensure that all of the below are checked and in the following order:
      - Web App Libraries
      - GWT
      - App Engine SDK
    - Click Apply
    - Select "Deployment Assembly"
    - Choose Add... / Java Build Path Entries
    - Select AppEngine SDK
    - Click Apply (you may need to repeat this twice to get it to remain)
    - Select "Project Facets"
    - Choose "Google AppEngine (for single module)"
    - Click on the Runtimes tab.
    - Check the "Google App Engine" entry that has the stylized airplane icon. (if there is one that looks like a server box, that App Engine sdk is missing from your workspace; uncheck it).
    - Click Apply
    - Choose "Server"
    - Choose the server that you configured earlier (in step (10)).
    - Click Apply
    - Click OK
1. Now, Select eclipse-n-background, right-click, Properties

   Repeat all the steps in (11) above.
1. Now, Select eclipse-ear, right-click, Properties
    - Select "Project Facets"
    - Choose "Google AppEngine (for multiple modules)"
    - Click on the Runtimes tab.
    - Check the "Google App Engine" entry that has the stylized airplane icon. (the one that looks like a server box is missing from your workspace; uncheck that).
    - Click Apply
    - Choose "Server"
    - Choose the server that you configured earlier (in step (10)).
    - Click Apply
    - Click OK
1. Select eclipse-default, right-click Google / GWT Compile
    - open Advanced
    - Verify that "Additional compiler arguments" has: `war WebContent`
    - Verify that VM Arguments has: `-Xmx512m`
    - Choose Compile.
    - Wait for the compile to complete (it is compiling the Java code into 5 different sets of Javascript to support 5 different browsers).
1. Select eclipse-default, right-click Refresh to pick up the changes to the compiled GWT files (this is important!!!)
1. Create an explicit development server configuration
    - Select eclipse-ear, right-click, Debug As / Debug Configurations
    - Double-click to choose Google App Engine
    - Rename "New_Configuration" to something of your choosing (e.g., devServerGAE)
    - Click Apply
    - Close.
1. Now, choose eclipse-ear, right-click, Debug As / Debug on Server

    This will initialize and start the server configuration you created above. It will fail with an out-of-memory error.
    
    Stop the server by clicking the red square box on the Console output window.
1. Select eclipse-ear, right-click, Debug As / Debug Configurations
    - Select the Google App Engine configuration you created in step (16).
    - Select the (X)= Arguments tab.
    - In the Program Arguments section, add: `--address=0.0.0.0` before the `--port` entry.
    - In the VM Arguments section, replace "-Xmx512m" with `"-XX:PermSize=1536m -XX:MaxPermSize=1536m -Xms256m -Xmx3048m "`
    - Click Apply
    - Choose Close
    - If you've done this correctly, the Program Arguments would be something like:

        ```
        --address=0.0.0.0 --port=8888 --disable_update_check [path...to...]\.metadata\.plugins\org.eclipse.wst.server.core\tmp0\eclipse-ear
        ```

         And the VM Arguments section should become something like:

         ```
         -javaagent:[path...to...]\appengine-java-sdk-1.9.53\lib\agent\appengine-agent.jar -XX:PermSize=1536m -XX:MaxPermSize=1536m -Xms256m -Xmx3048m  -Dappengine.fullscan.seconds=5
         ```

         If you tried to skip a step, the server might not be configured correctly.

    *NOTE*: if you need to preserve the datastore across cleans and publishing, you can add to the Program Arguments:

    ```
       --generated_dir=[path to]\generated
    ```

    and the datastore and index hints file will be written to the directory 'generated' at location.

    (after Applying these changes, choose Close if you haven't already)
1. Select the Servers tab in the output area.
    - Right-click on the server you created. Choose Clean....
    - This will delete the datastore and rebuild the server based upon the updated configuration.
1. To run or debug, **YOU CANNOT right-click the server and choose Run or Debug** If you do this, all of the settings you changed in step (18) will be lost. Instead, **YOU MUST** select eclipse-ear, right-click, Debug As / Debug Configurations (Run As / Run Configurations). This will open up the configuration you made in step (18).
    - Choose Debug (Run)
    - Now open a browser at http://localhost:8888
    - It should redirect to your Aggregate instance at the IP address you specified.
    - You may need to clear your browser cache if you are using GWT SuperDevMode and re-configure the browser for that (e.g., the bookmark buttons).

## AppEngine Edit-Debug Cycle Considerations

Now, you should be able to Debug the server-side code using the
AppEngine development server. When you are developing, as you
change code, you will probably need to start and stop the server.

If you change the UI layer, you will want to re-run the GWT compiler,
and Refresh the project (in the eclipse-default project, where the UI
code is).

You may need to clear your browser cache if you are using GWT SuperDevMode
and re-configure the browser for that (e.g., the bookmark buttons).

If you are working with GWT code, you can work in SuperDevMode
where you set breakpoints within the Chrome development environment.

See farther down (below) for configuring GWT.

If something is not picked up, you can try cleaning all the projects
and also Clean the server (via right-click on the server on the Servers
tab). This should refresh everything.  Note, however, that this will
delete the datastore, too.

The most likely problem during development is the inadvertent clearing of
the settings you configured in step 17, above.

[eclipse]: https://github.com/opendatakit/aggregate/blob/master/docs/eclipse.md

ou created. Choose Clean....
    - This will delete the datastore and rebuild the server based upon the updated configuration.
1. To run or debug, **YOU CANNOT right-click the server and choose Run or Debug** If you do this, all of the settings you changed in step (18) will be lost. Instead, **YOU MUST** select eclipse-ear, right-click, Debug As / Debug Configurations (Run As / Run Configurations). This will open up the configuration you made in step (18).
    - Choose Debug (Run)
    - Now open a browser at http://localhost:8888
    - It should redirect to your Aggregate instance at the IP address you specified.
    - You may need to clear your browser cache if you are using GWT SuperDevMode and re-configure the browser for that (e.g., the bookmark buttons).

## AppEngine Edit-Debug Cycle Considerations

Now, you should be able to Debug the server-side code using the
AppEngine development server. When you are developing, as you
change code, you will probably need to start and stop the server.

If you change the UI layer, you will want to re-run the GWT compiler,
and Refresh the project (in the eclipse-default project, where the UI
code is).

You may need to clear your browser cache if you are using GWT SuperDevMode
and re-configure the browser for that (e.g., the bookmark buttons).

If you are working with GWT code, you can work in SuperDevMode
where you set breakpoints within the Chrome development environment.

See farther down (below) for configuring GWT.

If something is not picked up, you can try cleaning all the projects
and also Clean the server (via right-click on the server on the Servers
tab). This should refresh everything.  Note, however, that this will
delete the datastore, too.

The most likely problem during development is the inadvertent clearing of
the settings you configured in step 17, above.

[eclipse]: https://github.com/opendatakit/aggregate/blob/master/docs/eclipse.md


