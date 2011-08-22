These are snapshot builds of the javarosa library and the Aggregate Components' GaeHttpClient project.
They need to be installed into the local M2_REPO repository.

javarosa-libraries:

mvn install:install-file -Dfile=javarosa-libraries.jar -DgroupId=org.javarosa -DartifactId=javarosa-libraries -Dversion=latest -Dpackaging=jar

odk-httpclient-gae:

This can be installed by pulling the Aggregate (Components) sources
and running 'mvn install' in the GaeHttpClient project.  Or,

mvn install:install-file -Dfile=odk-httpclient-gae-1.0.jar -DgroupId=org.opendatakit -DartifactId=odk-httpclient-gae -Dversion=1.0 -Dpackaging=jar

gwt-maps-1.1.0:

NOTE: this is a special build posted on this thread 
http://code.google.com/p/gwt-platform/issues/detail?id=292
for resolving a compilation failure concerning JSClassType

mvn install:install-file -Dfile=gwt-maps-1.1.0.jar -DgroupId=com.google.gwt.google-apis -DartifactId=gwt-maps -Dversion=1.1.0 -Dpackaging=jar

mvn install:install-file -Dfile=odk-tomcatutil.jar -DgroupId=org.opendatakit -DartifactId=odk-tomcatutil -Dversion=1.0 -Dpackaging=jar

mvn install:install-file -Dfile=appengine-remote-api.jar -DgroupId=com.google.appengine -DartifactId=appengine-remote-api -Dversion=1.5.1 -Dpackaging=jar
