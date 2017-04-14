# Configuring your Build Environment

The build tree relies on Maven 3 for executing unit tests and
integration tests across the 4 platforms (GAE, MySQL, PostgreSQL, SQLServer).

Sources are located under

 - `src/`  -- main source code, configuration files, and libraries
 - `war-base/` -- static web content and libraries for Eclipse environment

Because GWT and AppEngine have not historically respected and worked
with Maven projects inside Eclipse, the non-Maven directories of our
Eclipse projects begin with "eclipse-" prefix.

There are two sets of these:

For AppEngine:

- eclipse-ear
- eclipse-default
- eclipse-n-background

For MySQL or PostgreSQL or SQLServer:

- eclipse-tomcat8

Additionally, when using Eclipse, you must also import one of these projects
into your environment:

- odk-gae-settings
- odk-mysql-settings
- odk-postgresql-settings
- odk-sqlserver-settings


If you're a Maven expert and have suggestions about the Maven
project tree, please contact mitchellsundt@gmail.com

# Critically Important Configuration Notes!!!!

### You MUST use Java 7.

Java 8 (and anything other than Servlet 2.5) are not supported by AppEngine.
While the codebase now uses Tomcat8 (Servlet 3.1) for non-AppEngine deployments,
it does not make use of functionality above Servlet 2.5 for this reason.
If you use Java 8, Springframework will use a blacklisted reflection API
and you will NOT be able to run the server locally under AppEngine or
deploy it remotely.

### When running Google's AppEngine Eclipse plug-in and development server, the plug-in will scan files and delete any that contain Servlet-3.x functionality.

Unfortunately, this includes the spring-web jar. To work around this, you must
explode the jar, remove the overview.html and WEB-INF/web-fragment.xml files,
and then re-construct the jar.  To work around this problem, there is a
suitably modified jar here: war-base/WEB-INF/lib/spring-web-SEE-CONFIGURE-TXT-....jar

# Individual Configuration Guides

- [**Minimal Eclipse Installation Setup**][eclipse]
  - [**Tomcat Installation Setup**][tomcat]
  - [**AppEngine Installation Setup**][appengine]
- [**GWT debugging with SuperDevMode**][gwt]
- [**Full Maven Development Environment Configuration**][maven]

[eclipse]: https://github.com/opendatakit/aggregate/blob/master/docs/eclipse.md
[tomcat]: https://github.com/opendatakit/aggregate/blob/master/docs/tomcat.md
[appengine]: https://github.com/opendatakit/aggregate/blob/master/docs/app-engine.md
[gwt]: https://github.com/opendatakit/aggregate/blob/master/docs/gwt-debugging.md
[maven]: https://github.com/opendatakit/aggregate/blob/master/docs/maven-full.md
