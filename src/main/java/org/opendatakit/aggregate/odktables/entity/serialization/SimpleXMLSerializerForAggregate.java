package org.opendatakit.aggregate.odktables.entity.serialization;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.convert.Registry;
import org.simpleframework.xml.convert.RegistryStrategy;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.strategy.Strategy;

public class SimpleXMLSerializerForAggregate {
  public static Serializer getSerializer() {
    Registry registry = new Registry();
    Strategy strategy = new RegistryStrategy(registry);
    Serializer serializer = new Persister(strategy);
    ListConverter converter = new ListConverter(serializer);
    try {
      registry.bind(List.class, converter);
      registry.bind(ArrayList.class, converter);
      registry.bind(LinkedList.class, converter);
    } catch (Exception e) {
      throw new RuntimeException("Failed to register list converters!", e);
    }
    return serializer;
  }
}
