#!/bin/sh
# Runs some very simple tests against the generated war
# Takes one of 'mysql', 'gae' as a command line parameter

# Configure these according to your system
#BUILDER="/Applications/BitRock InstallBuilder Professional 7.0.3/bin/Builder.app/Contents/MacOS/installbuilder.sh"
#INSTALLER="/Applications/BitRock InstallBuilder Professional 7.0.3/output/generatewar-1.0-osx-installer.app/Contents/MacOS/installbuilder.sh"

BUILDER="/homes/iws/dylan/Software/installbuilder-7.0.4/bin/builder"
INSTALLER="/homes/iws/dylan/Software/installbuilder-7.0.4/output/generatewar-1.0-linux-installer.run"


PLATFORM=$1
OPTIONFILE=optionfiles/$PLATFORM.options

"$BUILDER" build ../generate_aggregate_war.xml

rm -rf temp
mkdir temp

"$INSTALLER" --mode unattended --optionfile $OPTIONFILE

cd temp
jar -xf ODK_Aggregate.war
cd WEB-INF/lib
jar -xf *-settings.jar
diff jdbc.properties ../../../expected/$PLATFORM/jdbc.properties.expected
diff security.properties ../../../expected/$PLATFORM/security.properties.expected
cd ../../..
rm -rf temp
