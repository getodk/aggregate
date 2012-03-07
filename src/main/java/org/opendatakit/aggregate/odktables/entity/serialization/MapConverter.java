package org.opendatakit.aggregate.odktables.entity.serialization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.collections.AbstractCollectionConverter;
import com.thoughtworks.xstream.io.ExtendedHierarchicalStreamWriterHelper;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

@SuppressWarnings("rawtypes")
public class MapConverter extends AbstractCollectionConverter {

  public MapConverter(Mapper mapper) {
    super(mapper);
  }

  public boolean canConvert(Class type) {
    return type.equals(HashMap.class) || type.equals(Hashtable.class)
        || type.equals(LinkedHashMap.class);

  }

  public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
    Map map = (Map) source;
    for (Object o1 : map.entrySet()) {
      Map.Entry entry = (Map.Entry) o1;
      ExtendedHierarchicalStreamWriterHelper.startNode(writer, entry.getKey().toString(),
          Map.Entry.class);
      if (entry.getValue() instanceof List) {
        for (Object o : (List) entry.getValue()) {
          ExtendedHierarchicalStreamWriterHelper.startNode(writer, "value", Object.class);
          writer.setValue(o.toString());
          writer.endNode();
        }
      } else
        writer.setValue(entry.getValue().toString());
      writer.endNode();
    }
  }

  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
    Map map = (Map) createCollection(context.getRequiredType());
    populateMap(reader, context, map);
    return map;
  }

  @SuppressWarnings("unchecked")
  protected void populateMap(HierarchicalStreamReader reader, UnmarshallingContext context, Map map) {
    while (reader.hasMoreChildren()) {
      reader.moveDown();
      List<String> values = new ArrayList<String>();
      Object key = reader.getNodeName();
      while (reader.hasMoreChildren()){
        reader.moveDown();
        values.add(reader.getValue());
        reader.moveUp();
      } 
      map.put(key, values.size() != 0 ? values : reader.getValue());
      reader.moveUp();
    }
  }
}
