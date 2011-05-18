This directory contains the files needed to build the
installer for Aggregate using the Bitrock InstallBuilder 7.1
or higher.

Notably, it contains a subset of the Google AppEngine SDK 1.5.0.1
so that the GAE install facilitates an easy deployment of the 
configured app to Google's cloud infrastructure.

The steps to do this are to:

(1) copy any built war file from anywhere into this project directory
(2) mvn clean
(3) mvn install
(4) open the buildWar.xml project file in Bitrock
(5) build for the platform(s) you need.

