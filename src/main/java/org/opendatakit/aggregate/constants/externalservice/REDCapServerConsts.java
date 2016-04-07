/*
 * Copyright (C) 2013 University of Washington
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
public class REDCapServerConsts {

  public static final long BACKOFF_DELAY_MILLISECONDS = 90000L;

  public static final Map<ElementType, REDCapServerType> typeMap = new HashMap<ElementType, REDCapServerType>();
  static {
    typeMap.put(ElementType.STRING, REDCapServerType.STRING);
    typeMap.put(ElementType.JRDATETIME, REDCapServerType.DATE);
    typeMap.put(ElementType.JRDATE, REDCapServerType.DATE);
    typeMap.put(ElementType.JRTIME, REDCapServerType.DATE);
    typeMap.put(ElementType.INTEGER, REDCapServerType.NUMBER);
    typeMap.put(ElementType.DECIMAL, REDCapServerType.NUMBER);
    typeMap.put(ElementType.GEOPOINT, REDCapServerType.GPS);
    typeMap.put(ElementType.GEOSHAPE, REDCapServerType.STRING);
    typeMap.put(ElementType.GEOTRACE, REDCapServerType.STRING);
    
    typeMap.put(ElementType.BOOLEAN, REDCapServerType.STRING);
    typeMap.put(ElementType.BINARY, REDCapServerType.CONTENT_TYPE);
    typeMap.put(ElementType.SELECT1, REDCapServerType.STRING);
    typeMap.put(ElementType.SELECTN, REDCapServerType.STRING);
    typeMap.put(ElementType.REPEAT, REDCapServerType.STRING);
    typeMap.put(ElementType.GROUP, REDCapServerType.STRING);
  }
  public static final int CONNECTION_TIMEOUT = 10000;
}
