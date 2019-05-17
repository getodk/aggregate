package org.opendatakit.aggregate.gradle

import org.apache.tools.ant.taskdefs.condition.Os

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

class Util {
  static void truncate(path) {
    Path p = Paths.get(path.toString())
    Files.delete(p)
    Files.write(p, [], StandardOpenOption.CREATE)
    println("Truncated file at ${path}")
  }

  static def setXmlValue(path, String tag, value) {
    def xml = new XmlParser().parse(path.toString())
    xml[tag][0].setValue(value.toString())
    def printer = new XmlNodePrinter(new PrintWriter(new FileWriter(path.toString())))
    printer.setPreserveWhitespace(true)
    printer.print(xml)
    println("Set ${path}:<${tag}> to \"${value}\"")
  }

  static def setPropertiesValue(path, String key, value) {
    Properties props = new Properties()
    File propsFile = new File(path.toString())
    props.load(propsFile.newDataInputStream())

    props.setProperty(key, value.toString())
    props.store(propsFile.newWriter(), null)
    println("Set ${path}:${key} to \"${value}\"")
  }

  static String getVersionName() {
    if (Os.isFamily(Os.FAMILY_WINDOWS))
      "cmd /c git describe --tags --dirty --always".execute().text.trim()
    else
      "git describe --tags --dirty --always".execute().text.trim()
  }

  static String getValue(obj, String key, String defaultValue) {
    if (obj.hasProperty(key))
      return obj.getProperty(key)
    return defaultValue
  }

  static void run(String... command) {
    def proc = command.execute()
    def b = new StringBuffer()
    proc.consumeProcessErrorStream(b)

    println proc.text
    println b.toString()
  }

  static void runInWorkingDir(wd, String... command) {
    def proc = command.execute(null, new File(wd.toString()))
    def b = new StringBuffer()
    proc.consumeProcessErrorStream(b)

    println proc.text
    println b.toString()
  }
}
