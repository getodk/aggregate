/*
 * Copyright (C) 2009 Google Inc. 
 * Copyright (C) 2010 University of Washington.
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

package org.opendatakit.common.constants;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringEscapeUtils;

/**
 * Static HTML utility functions used to generate proper HTML for ODK Aggregate
 * visual outputs
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class HtmlUtil {

  private static final int INPUT_WIDGET_SIZE_LIMIT = 50;

  protected static final String HREF = "href";
  protected static final String A = "a";
  protected static final String INPUT = "input";
  protected static final String ATTR_VALUE = "value";
  protected static final String ATTR_NAME = "name";
  protected static final String ATTR_TYPE = "type";
  protected static final String ATTR_METHOD = "method";
  protected static final String ATTR_ACCEPT_CHARSET = "accept-charset";
  protected static final String ATTR_ENCTYPE = "enctype";
  protected static final String ATTR_ACTION = "action";
  protected static final String ATTR_SIZE = "size";
  protected static final String ATTR_SRC = "src";

  public static final String createEndTag(String tag) {
    return HtmlConsts.BEGIN_CLOSE_TAG + tag + HtmlConsts.END_TAG;
  }

  public static final String createBeginTag(String tag) {
    return HtmlConsts.BEGIN_OPEN_TAG + tag + HtmlConsts.END_TAG;
  }

  public static final String createSelfClosingTag(String tag) {
    return HtmlConsts.BEGIN_OPEN_TAG + tag + HtmlConsts.END_SELF_CLOSING_TAG;
  }
  
  public static final String createUrl(String serverName) {
    return HtmlConsts.HTTP + serverName + BasicConsts.FORWARDSLASH;
  }

  public static final String createAttribute(String name, String value) {
    return name + BasicConsts.EQUALS + BasicConsts.QUOTE + value + BasicConsts.QUOTE;
  }

  public static final String wrapWithHtmlTags(String htmlTag, String text) {
    return createBeginTag(htmlTag) + text + createEndTag(htmlTag);
  }

  public static final String createHref(String url, String displayText) {
    return HtmlConsts.BEGIN_OPEN_TAG + A + BasicConsts.SPACE + createAttribute(HREF, url)
        + HtmlConsts.END_TAG + displayText + createEndTag(A);
  }

  public static final String createHrefWithProperties(String urlBase, Map<String, String> properties,
      String displayText) {
    return createHref(createLinkWithProperties(urlBase, properties), displayText);
  }

  public static final String createLinkWithProperties(String url, Map<String, String> properties) {
    StringBuilder urlBuilder = new StringBuilder();
    urlBuilder.append(url);
    if (properties != null) {
      Set<Map.Entry<String, String>> propSet = properties.entrySet();
      if (!propSet.isEmpty()) {
        urlBuilder.append(HtmlConsts.BEGIN_PARAM);
        boolean firstParam = true;
        for (Map.Entry<String, String> property : propSet) {
          if (firstParam) {
            firstParam = false;
          } else {
            urlBuilder.append(HtmlConsts.PARAM_DELIMITER);
          }

          String value = property.getValue();
          if (value == null) {
            value = BasicConsts.NULL;
          }

          String valueEncoded;
          try {
            valueEncoded = URLEncoder.encode(value, HtmlConsts.UTF8_ENCODE);
          } catch (UnsupportedEncodingException e) {
            valueEncoded = BasicConsts.EMPTY_STRING;
          }
          urlBuilder.append(property.getKey() + BasicConsts.EQUALS + valueEncoded);
        }
      }
    }
    return urlBuilder.toString();
  }

  public static final String createInput(String type, String name, String value, int size) {
    StringBuilder html = new StringBuilder();
    html.append(HtmlConsts.BEGIN_OPEN_TAG + INPUT);
    if (type != null) {
      html.append(BasicConsts.SPACE);
      html.append(createAttribute(ATTR_TYPE, type));
    }
    if (name != null) {
      html.append(BasicConsts.SPACE);
      html.append(createAttribute(ATTR_NAME, name));
    }
    if (value != null) {
      html.append(BasicConsts.SPACE);
      html.append(createAttribute(ATTR_VALUE, value));
    }
    html.append(BasicConsts.SPACE);
    html.append(createAttribute(ATTR_SIZE, Integer.toString(size)));
    html.append(HtmlConsts.END_SELF_CLOSING_TAG);
    return html.toString();
  }

  public static final String createInput(String type, String name, String value) {
    return HtmlUtil.createInput(type, name, value, INPUT_WIDGET_SIZE_LIMIT);
  }
  
  public static final String createRadio(String name, String value, String desc, boolean checked) {
    StringBuilder html = new StringBuilder();
    html.append(HtmlConsts.BEGIN_OPEN_TAG + INPUT + BasicConsts.SPACE);
    html.append(createAttribute(ATTR_TYPE, HtmlConsts.INPUT_TYPE_RADIO));
    if (name != null) {
      html.append(BasicConsts.SPACE);
      html.append(createAttribute(ATTR_NAME, name));
    }
    if (value != null) {
      html.append(BasicConsts.SPACE);
      html.append(createAttribute(ATTR_VALUE, value));
    }
    html.append(BasicConsts.SPACE);
    if (checked) {
      html.append(HtmlConsts.CHECKED);
    }
    html.append(HtmlConsts.END_SELF_CLOSING_TAG);
    html.append(desc);
    html.append(HtmlConsts.LINE_BREAK);
    return html.toString();
  }

  /**
   * 
   * @param name
   *          The select name.
   * @param values
   *          A list of pairs [option value, option title (text displayed to
   *          user)] for each option.
   * @return
   */
  public static final String createSelect(String name, List<String> values) {
    if (name == null) {
      return null;
    }
    StringBuilder html = new StringBuilder();
    html.append("<select name='" + StringEscapeUtils.escapeHtml(name) + "'>");

    if (values != null) {
      for (String v : values) {
        html.append("<option value='" + StringEscapeUtils.escapeHtml(v) + "'>");
        html.append(StringEscapeUtils.escapeHtml(v));
        html.append("</option>");
      }
    }
    html.append("</select>");
    return html.toString();
  }

  public static final String createFormBeginTag(String action, String encodingType, String method) {
    StringBuilder html = new StringBuilder();
    html.append(HtmlConsts.BEGIN_OPEN_TAG + HtmlConsts.FORM);
    if (action != null) {
      html.append(BasicConsts.SPACE);
      html.append(createAttribute(ATTR_ACTION, action));
    }
    if (encodingType != null) {
      html.append(BasicConsts.SPACE);
      html.append(createAttribute(ATTR_ENCTYPE, encodingType));
    }
    if (method != null) {
      html.append(BasicConsts.SPACE);
      html.append(createAttribute(ATTR_METHOD, method));
    }
    html.append(BasicConsts.SPACE);
    html.append(createAttribute(ATTR_ACCEPT_CHARSET, HtmlConsts.UTF8_ENCODE));
    html.append(HtmlConsts.END_TAG);
    return html.toString();
  }

  /**
   * Helper function that creates an html button with the following parameters
   * 
   * @param servletAddr
   *          http action
   * @param label
   *          button's label
   * @param properties
   *          key/value pairs to be encoded as hidden input types to be used as
   *          parameters
   * @return html to generate specified button
   * 
   * @throws UnsupportedEncodingException
   */
  public static final String createHtmlButtonToGetServlet(String servletAddr, String label,
      Map<String, String> properties) throws UnsupportedEncodingException {
    StringBuilder html = new StringBuilder();
    html.append(createFormBeginTag(servletAddr, null, HtmlConsts.GET));

    if (properties != null) {
      Set<Map.Entry<String, String>> propSet = properties.entrySet();
      for (Map.Entry<String, String> property : propSet) {
        String valueEncoded = URLEncoder.encode(property.getValue(), HtmlConsts.UTF8_ENCODE);
        html.append(createInput(HtmlConsts.INPUT_TYPE_HIDDEN, property.getKey(), valueEncoded));
      }
    }
    html.append(createInput(HtmlConsts.INPUT_TYPE_SUBMIT, null, label));
    html.append(HtmlConsts.FORM_CLOSE);
    return html.toString();
  }

  public static final String createHttpServletLink(String baseWebServerUrl, String servlet) {
    return "http://" + baseWebServerUrl + "/" + servlet;
  }
}
