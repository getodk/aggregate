package org.opendatakit.aggregate.odktables.entity.serialization;

import java.util.ArrayList;
import java.util.List;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.convert.Converter;
import org.simpleframework.xml.stream.InputNode;
import org.simpleframework.xml.stream.OutputNode;

public class ListConverter implements Converter<List<?>> {

  private Serializer serializer;

  public ListConverter(Serializer serializer) {
    this.serializer = serializer;
  }

  @Override
  public List<?> read(InputNode node) throws Exception {
    List<Object> list = new ArrayList<Object>();
    InputNode typeNode = node.getAttribute("type");
    if (typeNode != null) {
      String type = typeNode.getValue();
      Class<?> clazz = Class.forName(type);
      InputNode innerNode;
      while ((innerNode = node.getNext()) != null) {
        Object object = serializer.read(clazz, innerNode);
        list.add(object);
      }
    }
    return list;
  }

  @Override
  public void write(OutputNode node, List<?> list) throws Exception {
    node.setName("list");
    if (list.size() > 0) {
      node.setAttribute("type", list.get(0).getClass().getCanonicalName());
      for (Object object : list) {
        serializer.write(object, node);
      }
    }
  }

}
