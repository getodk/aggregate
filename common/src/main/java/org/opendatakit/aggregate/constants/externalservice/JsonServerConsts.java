/*
 * Copyright (C) 2010 University of Washington
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
package org.opendatakit.aggregate.constants.externalservice;

import java.util.HashMap;
import java.util.Map;

import org.opendatakit.aggregate.datamodel.FormElementModel.ElementType;

/**
 *
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 *
 */
public class JsonServerConsts {

  public static final long BACKOFF_DELAY_MILLISECONDS = 90000L;

  public static final Map<ElementType, JsonServerType> typeMap = new HashMap<ElementType, JsonServerType>();
  static {
    typeMap.put(ElementType.STRING, JsonServerType.STRING);
    typeMap.put(ElementType.JRDATETIME, JsonServerType.DATE);
    typeMap.put(ElementType.JRDATE, JsonServerType.DATE);
    typeMap.put(ElementType.JRTIME, JsonServerType.DATE);
    typeMap.put(ElementType.INTEGER, JsonServerType.NUMBER);
    typeMap.put(ElementType.DECIMAL, JsonServerType.NUMBER);
    typeMap.put(ElementType.GEOPOINT, JsonServerType.GPS);
    typeMap.put(ElementType.GEOSHAPE, JsonServerType.STRING);
    typeMap.put(ElementType.GEOTRACE, JsonServerType.STRING);
    typeMap.put(ElementType.BOOLEAN, JsonServerType.STRING);
    typeMap.put(ElementType.BINARY, JsonServerType.CONTENT_TYPE);
    typeMap.put(ElementType.SELECT1, JsonServerType.STRING);
    typeMap.put(ElementType.SELECTN, JsonServerType.STRING);
    typeMap.put(ElementType.REPEAT, JsonServerType.STRING);
    typeMap.put(ElementType.GROUP, JsonServerType.STRING);
  }
  public static final int CONNECTION_TIMEOUT = 10000;
}
