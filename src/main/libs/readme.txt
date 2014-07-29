These are snapshot builds of the javarosa library and the Aggregate Components' GaeHttpClient project.
They need to be installed into the local M2_REPO repository.

javarosa-libraries:

This is a special build of javarosa using the tree at https://bitbucket.org/m.sundt/javarosa
It incorporates multithread-safe KoBo collect changes (from Clayton), abandons J2ME support, 
exposes bind and prompt attributes, and numerous contributed fixes from SurveyCTO and others.

mvn install:install-file -Dfile=javarosa-libraries-2014-04-29.jar -DgroupId=org.javarosa -DartifactId=javarosa-libraries -Dversion=2014-04-29 -Dpackaging=jar

odk-httpclient-gae:

This can be installed by pulling the Aggregate (Components) sources
and running 'mvn install' in the GaeHttpClient project.  Or,

mvn install:install-file -Dfile=odk-httpclient-gae-1.1.1.jar -DgroupId=org.opendatakit -DartifactId=odk-httpclient-gae -Dversion=1.1.1 -Dpackaging=jar

openid4java-nodeps-0.9.6.662.1.odk-SNAPSHOT:

mvn install:install-file -Dfile=openid4java-parent-pom.xml -DgroupId=org.openid4java -DartifactId=openid4java-parent -Dversion=0.9.6.662.1.odk-SNAPSHOT -Dpackaging=pom

mvn install:install-file -Dfile=openid4java-nodeps-0.9.6.662.1.odk-SNAPSHOT.jar -DgroupId=org.openid4java -DartifactId=openid4java-nodeps -Dversion=0.9.6.662.1.odk-SNAPSHOT -Dpackaging=jar

spring-security-openid-3.2.4.odk-SNAPSHOT:

mvn install:install-file -Dfile=spring-security-crypto-3.2.4.odk-SNAPSHOT.jar -DgroupId=org.springframework.security -DartifactId=spring-security-crypto -Dversion=3.2.4.odk-SNAPSHOT -Dpackaging=jar

mvn install:install-file -Dfile=spring-security-config-3.2.4.odk-SNAPSHOT.jar -DgroupId=org.springframework.security -DartifactId=spring-security-config -Dversion=3.2.4.odk-SNAPSHOT -Dpackaging=jar

mvn install:install-file -Dfile=spring-security-core-3.2.4.odk-SNAPSHOT.jar -DgroupId=org.springframework.security -DartifactId=spring-security-core -Dversion=3.2.4.odk-SNAPSHOT -Dpackaging=jar

mvn install:install-file -Dfile=spring-security-web-3.2.4.odk-SNAPSHOT.jar -DgroupId=org.springframework.security -DartifactId=spring-security-web -Dversion=3.2.4.odk-SNAPSHOT -Dpackaging=jar

mvn install:install-file -Dfile=spring-security-openid-3.2.4.odk-SNAPSHOT.jar -DgroupId=org.springframework.security -DartifactId=spring-security-openid -Dversion=3.2.4.odk-SNAPSHOT -Dpackaging=jar

odk-tomcatutil:

This can be installed by pulling the Aggregate (Components) sources
and running 'mvn install' in the TomcatUtils project.  Or,

mvn install:install-file -Dfile=odk-tomcatutil.jar -DgroupId=org.opendatakit -DartifactId=odk-tomcatutil -Dversion=1.0 -Dpackaging=jar

gwt-google-maps-v3-snapshot:

See the Aggregate (Components) README.txt file for how this was built. It
uses the sources from http://code.google.com/p/gwt-google-maps-v3/

mvn install:install-file -Dfile=gwt-google-maps-v3-snapshot.jar -DgroupId=com.googlecode.gwt-google-maps-v3 -DartifactId=gwt-google-maps-v3 -Dversion=snapshot -Dpackaging=jar

gwt-visualization-1.1.1:

mvn install:install-file -Dfile=gwt-visualization-1.1.1.jar -DgroupId=com.google.gwt.google-apis -DartifactId=gwt-visualization -Dversion=1.1.1 -Dpackaging=jar

appengine-remote-api:

mvn install:install-file -Dfile=appengine-remote-api.jar -DgroupId=com.google.appengine -DartifactId=appengine-remote-api -Dversion=1.5.4 -Dpackaging=jar

And the gdata libraries need to be locally installed into Maven (the maven mandubian repository only has up through 1.41.5).  
Download the 1.47.1 gdata libraries from http://code.google.com/p/gdata-java-client/downloads/list
Unzip and in the java/lib directory, execute:

mvn install:install-file -Dfile=gdata-core-1.0.jar -DgroupId=com.google.gdata -DartifactId=gdata-core-1.0 -Dversion=1.47.1 -Dpackaging=jar -DgeneratePom=true

mvn install:install-file -Dfile=gdata-client-1.0.jar -DgroupId=com.google.gdata -DartifactId=gdata-client-1.0 -Dversion=1.47.1 -Dpackaging=jar -DgeneratePom=true

mvn install:install-file -Dfile=gdata-client-meta-1.0.jar -DgroupId=com.google.gdata -DartifactId=gdata-client-meta-1.0 -Dversion=1.47.1 -Dpackaging=jar -DgeneratePom=true

mvn install:install-file -Dfile=gdata-docs-3.0.jar -DgroupId=com.google.gdata -DartifactId=gdata-docs-3.0 -Dversion=1.47.1 -Dpackaging=jar -DgeneratePom=true

mvn install:install-file -Dfile=gdata-docs-meta-3.0.jar -DgroupId=com.google.gdata -DartifactId=gdata-docs-meta-3.0 -Dversion=1.47.1 -Dpackaging=jar -DgeneratePom=true

mvn install:install-file -Dfile=gdata-maps-2.0.jar -DgroupId=com.google.gdata -DartifactId=gdata-maps-2.0 -Dversion=1.47.1 -Dpackaging=jar -DgeneratePom=true

mvn install:install-file -Dfile=gdata-maps-meta-2.0.jar -DgroupId=com.google.gdata -DartifactId=gdata-maps-meta-2.0 -Dversion=1.47.1 -Dpackaging=jar -DgeneratePom=true

mvn install:install-file -Dfile=gdata-media-1.0.jar -DgroupId=com.google.gdata -DartifactId=gdata-media-1.0 -Dversion=1.47.1 -Dpackaging=jar -DgeneratePom=true

mvn install:install-file -Dfile=gdata-spreadsheet-3.0.jar -DgroupId=com.google.gdata -DartifactId=gdata-spreadsheet-3.0 -Dversion=1.47.1 -Dpackaging=jar -DgeneratePom=true

mvn install:install-file -Dfile=gdata-spreadsheet-meta-3.0.jar -DgroupId=com.google.gdata -DartifactId=gdata-spreadsheet-meta-3.0 -Dversion=1.47.1 -Dpackaging=jar -DgeneratePom=true

Download the "Xerces2 Java 2.11.0 - zip" from http://xerces.apache.org/mirrors.cgi
Unzip and in the Xerces-J-bin.2.11.0/xerces-2_11_0 directory:
rename xercesImpl.jar to xercesImpl-2.11.0.jar
mvn install:install-file -Dfile=xercesImpl-2.11.0.jar -DgroupId=xerces -DartifactId=xercesImpl -Dversion=2.11.0 -Dpackaging=jar -DgeneratePom=true

This file: jai-imageio-core-standalone-1.2-pre-dr-b04-2010-04-30.jar 
is built from the sources here: git://github.com/stain/jai-imageio-core.git
It is copied into the war at the time the installer is created.  This file 
is only used on Tomcat deployments by the odk-tomcat-util.jar.
