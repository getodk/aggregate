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
import org.opendatakit.common.web.constants.HtmlConsts;

/**
 *
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 *
 */
public final class FusionTableConsts {

  public static final String CREATE_FUSION_RESP_HEADER = "tableid";
  public static final String FUSTABLE_ENCODE = HtmlConsts.UTF8_ENCODE;
  public static final String FUSTABLE_CONTENT_TYPE = "application/x-www-form-urlencoded";
  public static final String VALUES_STMT = " VALUES ";
  public static final String INSERT_STMT = "INSERT INTO ";
  public static final String CREATE_STMT = "CREATE TABLE ";
  public static final String SINGLE_QUOTE = "'";
  public static final String HTML_ESCAPED_SINGLE_QUOTE = "&#39;";

  public static final int SOCKET_ESTABLISHMENT_TIMEOUT_MILLISECONDS = 60000;
  public static final int SERVICE_TIMEOUT_MILLISECONDS = 60000;

  public static final long BACKOFF_DELAY_MILLISECONDS = 60000L;

  public static final Map<ElementType, FusionTableType> typeMap = new HashMap<ElementType, FusionTableType>();
  static {
    typeMap.put(ElementType.STRING, FusionTableType.STRING);
    typeMap.put(ElementType.JRDATETIME, FusionTableType.DATE);
    typeMap.put(ElementType.JRDATE, FusionTableType.DATE);
    typeMap.put(ElementType.JRTIME, FusionTableType.DATE);
    typeMap.put(ElementType.INTEGER, FusionTableType.NUMBER);
    typeMap.put(ElementType.DECIMAL, FusionTableType.NUMBER);
    typeMap.put(ElementType.GEOPOINT, FusionTableType.GPS);
    typeMap.put(ElementType.GEOSHAPE, FusionTableType.STRING);
    typeMap.put(ElementType.GEOTRACE, FusionTableType.STRING);
    typeMap.put(ElementType.BOOLEAN, FusionTableType.STRING);
    typeMap.put(ElementType.BINARY, FusionTableType.STRING);
    typeMap.put(ElementType.SELECT1, FusionTableType.STRING);
    typeMap.put(ElementType.SELECTN, FusionTableType.STRING);
    typeMap.put(ElementType.REPEAT, FusionTableType.STRING);
    typeMap.put(ElementType.GROUP, FusionTableType.STRING);
    typeMap.put(ElementType.METADATA, FusionTableType.STRING);
  }

}
