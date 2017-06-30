package org.opendatakit.aggregate.odktablesperf.api;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.apache.wink.common.internal.providers.entity.FileProvider;

public class ODKClientApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
      final java.util.HashSet<java.lang.Class<?>> classes = new HashSet<Class<?>>();
      
      // standard content stream reader/writer
      classes.add(SimpleJSONMessageReaderWriter.class);
      classes.add(FileProvider.class);
      return classes;
    }
}
