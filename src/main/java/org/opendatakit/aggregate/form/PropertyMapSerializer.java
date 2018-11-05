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
package org.opendatakit.aggregate.form;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringEscapeUtils;

/**
 * Static methods to serialize and deserialize a Map<String,String> to an
 * xml-formatted string.
 *
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 */
public final class PropertyMapSerializer {
  private static final String K_XML_END_PARAMETERS = "</parameters>\n";
  private static final String K_XML_BEGIN_PARAMETER_BEGIN_KEY = "<parameter><key>";
  private static final String K_XML_END_KEY_BEGIN_VALUE = "</key><value>";
  private static final String K_XML_END_VALUE_END_PARAMETER = "</value></parameter>\n";
  private static final String K_XML_BEGIN_PARAMETERS = "<parameters>\n";

  /**
   * Deserialize the XML representation for a key-value map.
   *
   * @param parameterDocument
   * @return parameter map as a Map<String,String> key-value map.
   */
  public static Map<String, String> deserializeRequestParameters(String parameterDocument) {
    Map<String, String> parameters = new HashMap<String, String>();
    if (parameterDocument == null) return parameters;
    if (!parameterDocument.startsWith(K_XML_BEGIN_PARAMETERS)) {
      throw new IllegalArgumentException("bad parameter list -- not beginning with " +
          K_XML_BEGIN_PARAMETERS);
    }
    int iNext = K_XML_BEGIN_PARAMETERS.length();
    while (parameterDocument.regionMatches(iNext,
        K_XML_BEGIN_PARAMETER_BEGIN_KEY,
        0,
        K_XML_BEGIN_PARAMETER_BEGIN_KEY.length())) {
      iNext += K_XML_BEGIN_PARAMETER_BEGIN_KEY.length();
      int iEnd = parameterDocument.indexOf(K_XML_END_KEY_BEGIN_VALUE, iNext);
      if (iEnd == -1) {
        throw new IllegalArgumentException("bad parameter list -- end-key-begin-value not found");
      }
      String key = StringEscapeUtils.unescapeXml(parameterDocument.substring(iNext, iEnd));
      iNext = iEnd + K_XML_END_KEY_BEGIN_VALUE.length();
      iEnd = parameterDocument.indexOf(K_XML_END_VALUE_END_PARAMETER, iNext);
      if (iEnd == -1) {
        throw new IllegalArgumentException("bad parameter list -- end-value-end-parameter not found");
      }
      String value = StringEscapeUtils.unescapeXml(parameterDocument.substring(iNext, iEnd));
      iNext = iEnd + K_XML_END_VALUE_END_PARAMETER.length();
      parameters.put(key, value);
    }
    if (!parameterDocument.regionMatches(iNext,
        K_XML_END_PARAMETERS,
        0,
        K_XML_END_PARAMETERS.length())) {
      throw new IllegalArgumentException("bad parameter list -- end-parameters not found");
    }
    iNext += K_XML_END_PARAMETERS.length();
    if (iNext != parameterDocument.length()) {
      throw new IllegalArgumentException("bad parameter list -- extra characters found");
    }
    return parameters;
  }

  /**
   * Serialize a key-value map into an XML-formatted document.
   *
   * @param value - a Map<String,String> of key-value pairs.
   * @return the XML document representing the serialization of that key-value map.
   */
  public static String serializeRequestParameters(Map<String, String> value) {
    if (value == null) {
      return null;
    }
    StringBuilder b = new StringBuilder();
    b.append(K_XML_BEGIN_PARAMETERS);
    for (Map.Entry<String, String> e : value.entrySet()) {
      b.append(K_XML_BEGIN_PARAMETER_BEGIN_KEY);
      b.append(StringEscapeUtils.escapeXml10(e.getKey()));
      b.append(K_XML_END_KEY_BEGIN_VALUE);
      b.append(StringEscapeUtils.escapeXml10(e.getValue()));
      b.append(K_XML_END_VALUE_END_PARAMETER);
    }
    b.append(K_XML_END_PARAMETERS);
    return b.toString();
  }
}
