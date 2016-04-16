-----------------------
Development Environment
-----------------------

See CONFIGURE.txt for how to set up your development environment.

-----------------
GWT Upgrade Guide
-----------------

If you are updating this project to work with something other than
GWT 2.7, you should verify that the code in:

GWTAccessDeniedHandler.AccessDeniedRemoteServiceServlet.processCall()

is still appropriate for the version of GWT you are using.

-------------
Upgrade Guide
-------------

Upgrading versions of software should be done by first updating
the versions in the maven pom.xml file located in this directory.

Some of these versions are defined as properties at the top of the 
file so that entire suites of jars (e.g., Spring, GAE, GWT) can
be upgraded at the same time.

Once that is done, and mvn build executes without errors, you should
check that there are no older jars in the WEB-INF/lib directories
of the war files (all jars go everywhere at this time, so looking 
at just one should be fine).  If older jars are being pulled in, you
will need to update the dependencies to exclude pulling in those jars;
there are examples of this in the current pom.xml and online.

You'll need a pom dependency analyzer to uncover why Maven has
pulled in the down-version jar.

After that, you should copy the jars from the WEB-INF/lib directory 
of the build back into the war-base/WEB-INF/lib
directory and refresh the eclipse projects to reflect the new set
of jars.  When doing this, you will likely need to hand-edit the 
spring-web jar to remove the servlet-3.0 compatible files:

  overview.html
  META-INF/web-fragment.xml

Beginning sometime after the 1.9.28 AppEngine SDK, it now scans and
deletes jars with servlet 3.0 functionality (even if it is not 
being exercised, as is the case with ODK Aggregate).

And, of course, test to verify this all works.
