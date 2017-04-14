# GWT debugging with SuperDevMode

These instructions are taken from this video:  https://www.youtube.com/watch?v=w9270Yqt-5I

1. In either the eclipse-ear or eclipse-tomcat8 project, right-click, Debug As / Debug Configurations
1. Double-click "Java Application" to configure a Java Application execution.
1. Change the name to, e.g., "AppEngine GWT Codeserver" or "Tomcat8 GWT Codeserver"
1. Choose Search... and type in Code
1. Select "CodeServer"
1. Choose Apply
1. Click on the (X)= Arguments tab
1. In Program Arguments, insert the 4 arguments (on one line):

   ```
   --bindAddress [your IP address]
   -src [path-to-workspace]\src\main\java
   -launcherDir [path-to-workspace]\.metadata\.plugins\org.eclipse.wst.server.core\tmp0\wtpwebapps\eclipse-tomcat8
   org.opendatakit.aggregate.AggregateUI
   ```

   The -launcherDir "tmp0" may be a different directory name (e.g., tmp1, tmp2...).
   
   Whichever one contains the eclipse-tomcat8 or eclipse-default is the one you would use.
   
   If confused, you can stop all servers, blow away all the tmpX directories, and re-publish and re-run the server you have configured to determine what directory has the active content.
1. Choose Apply
1. Close
1. Now, run the server (EAR or Tomcat8)
1. And run the codeserver you just configured via Debug As / Debug Configurations, and select the configuration.

   This will take a long long time, and then emit a URL that the code server is using.
1. Open a Chrome browser to this URL.

   Copy the Dev Mode On / Dev Mode Off buttons up to the bookmarks bar.
  
   You may need to delete an earlier copy of these by right-click / Delete.
  
   And if you have been using Dev Mode, you may need to clear your browser cache.
1. Now, open the URL to your server (tomcat8 or eclipse-ear).
    - It will pause compiling aggregateui
    - And then render the page.
    - Choose "Dev Mode On"
1. Then, to debug, go to Developer Tools

  There will be the standard Javascript Sources tab, and a new tab, Content Scripts.
  
  The content Scripts tab will contain the Java code, and you can set breakpoints in that to take effect on the browser, and step through everything there.
  
  I believe you can make code changes in the Java source in Eclipse, and, when you save those changes, they will be picked up and applied after a short delay.
  
  More info here:
  
  http://www.gwtproject.org/articles/superdevmode.html

## Troubleshooting Debugging/Running

#### Javascript refresh loop

If the database schema has changed, the browser may flash
and be stuck in a javascript refresh loop.  To remedy,
delete your local datastore (instructions below)

#### Odd behaviors under Eclipse

When you stop your server, be sure to recompile GWT files
(click on the red toolbox). And then right-click, Refresh.

You may also need to Clean and Publish to the server to
ensure that the server has the very latest copies of those
files.

Otherwise, when you restart the server, it may use a stale
copy of the compiled code.

You may also need to stop and start the codeserver during this
process.

Third, periodically clear your browser cache to force a complete
re-loading of the clientside javascript.

Fourth, if you have moved or changed client interfaces, you
may need to manually browse to the war diretory and delete
the contents of the war/aggregateui and war/WEB-INF/deploy
directories.

#### Odd errors about locking scopes.

If you are debugging code within a transaction
region (these are presently isolated to TaskLockImpl.java),
the datastore can get confused about the transaction scopes
that are active.  You may need to close eclipse, re-open,
and delete the datastore to clear this problem.

## Clearing your Datastore

To delete the local datastore, the easiest way to do this is to
Clean your server, which will delete everything and republish it.
If you do this, you will first need to stop your server, stop
your codeserver (if debugging the UI), and then Clean, start the
server, and then start the codeserver (if debugging the UI).

