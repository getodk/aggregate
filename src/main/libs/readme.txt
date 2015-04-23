build.xml -- ANT script to install various library files into your local Mvn repository.

Run ANT in this directory to register these files with your local maven repository.

The files being installed are not available via mvnrepository.com

----------------------
JARs built by ODK team
----------------------

maven_local_installs -- contains the maven commands to register these jars into your repo.

Summary of what is installed.

# javarosa-libraries:

This is a special build of javarosa using the tree at https://bitbucket.org/m.sundt/javarosa
It incorporates multithread-safe KoBo collect changes (from Clayton), abandons J2ME support, 
exposes bind and prompt attributes, and numerous contributed fixes from SurveyCTO and others.

# odk-httpclient-gae:

This can be installed by pulling the Aggregate (Components) sources
and running 'mvn install' in the GaeHttpClient project.  

# odk-tomcatutil:

This can be installed by pulling the Aggregate (Components) sources
and running 'mvn install' in the TomcatUtils project.  Or,

# gwt-google-maps-v3-snapshot:

This can be installed by pulling the Aggregate (Components) sources
and building the gwt-google-maps-v3 project. 
See the Aggregate (Components) README.txt file for how this was built. It
uses the sources that were originally located here (but have since been removed):
http://code.google.com/p/gwt-google-maps-v3/

# jai-imageio-core-standalone-1.2-pre-dr-b04-2010-04-30.jar

(this is NOT registered with maven).

This file: jai-imageio-core-standalone-1.2-pre-dr-b04-2010-04-30.jar 
is built from the sources here: git://github.com/stain/jai-imageio-core.git
It is copied into the war at the time the installer is created.  This file 
is only used on Tomcat deployments by the odk-tomcat-util.jar.

----------------------
JARs built by others
----------------------

# gwt-visualization-1.1.1:

This is a library from here: http://code.google.com/p/gwt-google-apis/

# gdata-src.java-1.47.1 -- various jars:

maven_gdata_installs -- a list of the jars used and the maven commands to install them.

The gdata libraries need to be locally installed into Maven.  
Download the 1.47.1 gdata libraries from http://code.google.com/p/gdata-java-client/downloads/list

The ANT build script automatically handles this.
