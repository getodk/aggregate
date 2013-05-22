/*
 * Copyright (C) 2012-2013 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

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
