package org.opendatakit.aggregate.odktables.api.impl;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import lombok.val;

public class ODKTablesAPIApplication extends Application {
  @Override
  public Set<Class<?>> getClasses() {
    val classes = new HashSet<Class<?>>();
    classes.add(TableServiceImpl.class);
    classes.add(ODKDatastoreExceptionMapper.class);
    classes.add(ODKTablesExceptionMapper.class);
    classes.add(RuntimeExceptionMapper.class);
    return classes;
  }
}
