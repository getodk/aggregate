package org.opendatakit.aggregate.gradle

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
    printer.setPreserveWhitespace(false)
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
}
