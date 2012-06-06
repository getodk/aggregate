package org.opendatakit.aggregate.odktables.impl.api;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.opendatakit.aggregate.odktables.entity.serialization.SimpleXMLMessageReaderWriter;

public class ODKTablesAPIApplication extends Application {

  @Override
  public Set<Class<?>> getClasses() {
    final java.util.HashSet<java.lang.Class<?>> classes = new HashSet<Class<?>>();
    classes.add(TableServiceImpl.class);
    classes.add(SimpleXMLMessageReaderWriter.class);
    classes.add(ODKDatastoreExceptionMapper.class);
    classes.add(ODKTablesExceptionMapper.class);
    classes.add(ODKTaskLockExceptionMapper.class);
    classes.add(RuntimeExceptionMapper.class);
    return classes;
  }
}