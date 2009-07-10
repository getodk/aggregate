/*
 * Copyright (C) 2009 Google Inc.
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

package org.odk.aggregate.constants;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Set;

import org.odk.aggregate.table.ResultTable;

/**
 * Static HTML utility functions used to generate proper HTML
 * for ODK Aggregate visual outputs
 *  
 * @author wbrunette@gmail.com
 *
 */
public class HtmlUtil {

  private static final String INPUT_WIDGET_SIZE_LIMIT = "50";

  private static final String HREF = "href";
  private static final String A = "a";
  private static final String INPUT = "input";
  private static final String ATTR_VALUE = "value";
  private static final String ATTR_NAME = "name";
  private static final String ATTR_TYPE = "type";
  private static final String ATTR_METHOD = "method";
  private static final String ATTR_ENCTYPE = "enctype";
  private static final String ATTR_ACTION = "action";
  private static final String ATTR_SIZE = "size";

  static String createEndTag(String tag) {
    return HtmlConsts.BEGIN_CLOSE_TAG + tag + HtmlConsts.END_TAG;
  }

  static String createBeginTag(String tag) {
    return HtmlConsts.BEGIN_OPEN_TAG + tag + HtmlConsts.END_TAG;
  }

  public static String createAttribute(String name, String value) {
    return name + BasicConsts.EQUALS + HtmlConsts.QUOTE + value + HtmlConsts.QUOTE;
  }

  public static String wrapWithHtmlTags(String htmlTag, String text) {
    return createBeginTag(htmlTag) + text + createEndTag(htmlTag);
  }

  public static String createHref(String url, String displayText) {
    return HtmlConsts.BEGIN_OPEN_TAG + A + BasicConsts.SPACE + createAttribute(HREF, url)
        + HtmlConsts.END_TAG + displayText + createEndTag(A);
  }

  public static String createHrefWithProperties(String urlBase, Map<String, String> properties,
      String displayText) {
    return createHref(createLinkWithProperties(urlBase, properties), displayText);
  }

  public static String createLinkWithProperties(String url, Map<String, String> properties) {
    if (properties != null) {
      Set<Map.Entry<String, String>> propSet = properties.entrySet();
      if (!propSet.isEmpty()) {
        url += ServletConsts.BEGIN_PARAM;
        boolean firstParam = true;
        for (Map.Entry<String, String> property : propSet) {
          if(firstParam) {
            firstParam = false;
          } else {
            url += ServletConsts.PARAM_DELIMITER;
          }
          String valueEncoded;
          try {
            valueEncoded = URLEncoder.encode(property.getValue(), ServletConsts.ENCODE_SCHEME);
          } catch (UnsupportedEncodingException e) {
            valueEncoded = BasicConsts.EMPTY_STRING;
          }
          url += property.getKey() + BasicConsts.EQUALS + valueEncoded;
        }
      }
    }
    return url;
  }

  public static String createInput(String type, String name, String value) {
    String html = HtmlConsts.BEGIN_OPEN_TAG + INPUT;
    if (type != null) {
      html += BasicConsts.SPACE + createAttribute(ATTR_TYPE, type);
    }
    if (name != null) {
      html += BasicConsts.SPACE + createAttribute(ATTR_NAME, name);
    }
    if (value != null) {
      html += BasicConsts.SPACE + createAttribute(ATTR_VALUE, value);
    }
    return html + BasicConsts.SPACE + createAttribute(ATTR_SIZE, INPUT_WIDGET_SIZE_LIMIT)
        + HtmlConsts.END_SELF_CLOSING_TAG;
  }

  public static String createFormBeginTag(String action, String encodingType, String method) {
    String html = HtmlConsts.BEGIN_OPEN_TAG + HtmlConsts.FORM;
    if (action != null) {
      html += BasicConsts.SPACE + createAttribute(ATTR_ACTION, action);
    }
    if (encodingType != null) {
      html += BasicConsts.SPACE + createAttribute(ATTR_ENCTYPE, encodingType);
    }
    if (method != null) {
      html += BasicConsts.SPACE + createAttribute(ATTR_METHOD, method);
    }
    return html + HtmlConsts.END_TAG;
  }

  /**
   * Helper function that creates a button with the following parameters
   * 
   * @param servletAddr
   *    http action
   * @param label
   *    button's label
   * @param properties
   *    key/value pairs to be encoded as hidden input types to be used as parameters
   * @return
   *    html to generate specified button
   *    
   * @throws UnsupportedEncodingException
   */
  public static String createHtmlButtonToGetServlet(String servletAddr, String label, Map<String,String> properties)
      throws UnsupportedEncodingException {
    String html = createFormBeginTag(servletAddr, null, ServletConsts.GET);
    
    if(properties != null) {
      Set<Map.Entry<String, String>> propSet = properties.entrySet();
      for(Map.Entry<String, String> property: propSet) {
        String valueEncoded = URLEncoder.encode(property.getValue(), ServletConsts.ENCODE_SCHEME);
        html += createInput(HtmlConsts.INPUT_TYPE_HIDDEN, property.getKey(), valueEncoded);
      }
    }
    return html + createInput(HtmlConsts.INPUT_TYPE_SUBMIT, null, label) + HtmlConsts.FORM_CLOSE;
  }

  public static String wrapResultTableWithHtmlTags(ResultTable resultTable) {
    String html = HtmlConsts.TABLE_OPEN;
  
    for (String header : resultTable.getHeader()) {
      html += wrapWithHtmlTags(HtmlConsts.TABLE_HEADER, header);
    }
  
    for (String[] row : resultTable.getRows()) {
      html = html + HtmlConsts.TABLE_ROW_OPEN;
      for (String item : row) {
        html += wrapWithHtmlTags(HtmlConsts.TABLE_DATA, item);
      }
      html += HtmlConsts.TABLE_ROW_CLOSE;
    }
  
    return html + HtmlConsts.TABLE_CLOSE;
  }

}
