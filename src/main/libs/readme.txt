These are snapshot builds of the javarosa library and the Aggregate Components' GaeHttpClient project.
They need to be installed into the local M2_REPO repository.

javarosa-libraries:

mvn install:install-file -Dfile=javarosa-libraries.jar -DgroupId=org.javarosa -DartifactId=javarosa-libraries -Dversion=latest -Dpackaging=jar

odk-httpclient-gae:

This can be installed by pulling the Aggregate (Components) sources
and running 'mvn install' in the GaeHttpClient project.  Or,

mvn install:install-file -Dfile=odk-httpclient-gae-1.0.jar -DgroupId=org.opendatakit -DartifactId=odk-httpclient-gae -Dversion=1.0 -Dpackaging=jar

odk-tomcatutil:

This can be installed by pulling the Aggregate (Components) sources
and running 'mvn install' in the TomcatUtils project.  Or,

mvn install:install-file -Dfile=odk-tomcatutil.jar -DgroupId=org.opendatakit -DartifactId=odk-tomcatutil -Dversion=1.0 -Dpackaging=jar

gwt-maps-1.1.1-rc1:

mvn install:install-file -Dfile=gwt-maps-1.1.1-rc1.jar -DgroupId=com.google.gwt.google-apis -DartifactId=gwt-maps -Dversion=1.1.1-rc1 -Dpackaging=jar

gwt-visualization-1.1.1:

mvn install:install-file -Dfile=gwt-visualization-1.1.1.jar -DgroupId=com.google.gwt.google-apis -DartifactId=gwt-visualization -Dversion=1.1.1 -Dpackaging=jar

appengine-remote-api:

mvn install:install-file -Dfile=appengine-remote-api.jar -DgroupId=com.google.appengine -DartifactId=appengine-remote-api -Dversion=1.5.4 -Dpackaging=jar

And the gdata libraries need to be locally installed into Maven (the maven mandubian repository only has up through 1.41.5).  
Download the 1.46.0 gdata libraries from http://code.google.com/p/gdata-java-client/downloads/list
Unzip and in the java/libs directory, execute:

mvn install:install-file -Dfile=gdata-core-1.0.jar -DgroupId=com.google.gdata -DartifactId=gdata-core-1.0 -Dversion=1.46.0 -Dpackaging=jar -DgeneratePom=true

mvn install:install-file -Dfile=gdata-client-1.0.jar -DgroupId=com.google.gdata -DartifactId=gdata-client-1.0 -Dversion=1.46.0 -Dpackaging=jar -DgeneratePom=true

mvn install:install-file -Dfile=gdata-client-meta-1.0.jar -DgroupId=com.google.gdata -DartifactId=gdata-client-meta-1.0 -Dversion=1.46.0 -Dpackaging=jar -DgeneratePom=true

mvn install:install-file -Dfile=gdata-docs-3.0.jar -DgroupId=com.google.gdata -DartifactId=gdata-docs-3.0 -Dversion=1.46.0 -Dpackaging=jar -DgeneratePom=true

mvn install:install-file -Dfile=gdata-docs-meta-3.0.jar -DgroupId=com.google.gdata -DartifactId=gdata-docs-meta-3.0 -Dversion=1.46.0 -Dpackaging=jar -DgeneratePom=true

mvn install:install-file -Dfile=gdata-maps-2.0.jar -DgroupId=com.google.gdata -DartifactId=gdata-maps-2.0 -Dversion=1.46.0 -Dpackaging=jar -DgeneratePom=true

mvn install:install-file -Dfile=gdata-maps-meta-2.0.jar -DgroupId=com.google.gdata -DartifactId=gdata-maps-meta-2.0 -Dversion=1.46.0 -Dpackaging=jar -DgeneratePom=true

mvn install:install-file -Dfile=gdata-media-1.0.jar -DgroupId=com.google.gdata -DartifactId=gdata-media-1.0 -Dversion=1.46.0 -Dpackaging=jar -DgeneratePom=true

mvn install:install-file -Dfile=gdata-spreadsheet-3.0.jar -DgroupId=com.google.gdata -DartifactId=gdata-spreadsheet-3.0 -Dversion=1.46.0 -Dpackaging=jar -DgeneratePom=true

mvn install:install-file -Dfile=gdata-spreadsheet-meta-3.0.jar -DgroupId=com.google.gdata -DartifactId=gdata-spreadsheet-meta-3.0 -Dversion=1.46.0 -Dpackaging=jar -DgeneratePom=true

