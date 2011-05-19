// script to stop java applications in windows
var debugFlag = false;

// killApp - terminates the given application
//
//  processName -- full process name for the application (e.g., java.exe)
//  entryPointClass -- optional: a first command line argument located before jarName
//  jarName -- optional: a second command line argument located after entryPointClass
//
function killApp( processName, entryPointClass, jarName)
{
  if ( debugFlag ) WScript.Echo("We are here: find " + processName + " command line containing " + entryPointClass + " and " + jarName );
  if ( processName.indexOf("'") != -1 ) {
    WScript.Echo("Error: process name contains a single-quote character");
    return;
  }
  var strComputer = ".";
  var objWMIService = GetObject("winmgmts:{impersonationLevel=impersonate}!\\\\" + strComputer + "\\root\\cimv2");
  var colProcessList = objWMIService.ExecQuery("Select * from Win32_Process Where Name = '" + processName + "'");

  if ( debugFlag ) WScript.Echo("looping...");
  var enumItems = new Enumerator(colProcessList);
  for (; !enumItems.atEnd(); enumItems.moveNext()) {
     var objProcess = enumItems.item();
     var lastJar = objProcess.CommandLine.length;
     var firstEntryPoint = 0;
     if ( jarName ) {
       lastJar = objProcess.CommandLine.lastIndexOf(jarName);
     }
     if ( entryPointClass ) {
       firstEntryPoint = objProcess.CommandLine.indexOf(entryPointClass);
     }
     if ( lastJar > firstEntryPoint && firstEntryPoint > -1 ) {
       WScript.Echo("killing this process: " + objProcess.CommandLine );
       objProcess.Terminate();
     }
  }
}

if ( debugFlag ) WScript.Echo("main code");
killApp("java.exe", "com.google.appengine.tools.KickStart", "com.google.appengine.tools.development.DevAppServerMain ODKAggregate");
killApp("java.exe", "com.google.appengine.tools.development.DevAppServerMain", "ODKAggregate");
WScript.Echo("---------------------------------------------");
WScript.Echo("The ODK Aggregate processes have been stopped");
