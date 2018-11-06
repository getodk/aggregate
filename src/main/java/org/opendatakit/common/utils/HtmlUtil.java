/*
 * Copyright (C) 2011 University of Washington
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

package org.opendatakit.common.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Set;
import org.opendatakit.common.web.constants.BasicConsts;
import org.opendatakit.common.web.constants.HtmlConsts;
import org.opendatakit.common.web.constants.HtmlStrUtil;

public class HtmlUtil extends HtmlStrUtil {

  public static final String createHrefWithProperties(String urlBase, Map<String, String> properties, String displayText, boolean openInNewWindow) {
    return createHref(HtmlUtil.createLinkWithProperties(urlBase, properties), displayText, openInNewWindow);
  }

  public static final String createHtmlButtonToHttpMethodServlet(String httpMethod, String servletAddr, String label, Map<String, String> properties) throws UnsupportedEncodingException {
    StringBuilder html = new StringBuilder();
    html.append(HtmlStrUtil.createFormBeginTag(servletAddr, null, httpMethod));

    if (properties != null) {
      Set<Map.Entry<String, String>> propSet = properties.entrySet();
      for (Map.Entry<String, String> property : propSet) {
        String valueEncoded = URLEncoder.encode(property.getValue(), HtmlConsts.UTF8_ENCODE);
        html.append(HtmlStrUtil.createInput(HtmlConsts.INPUT_TYPE_HIDDEN, property.getKey(), valueEncoded));
      }
    }
    html.append(HtmlStrUtil.createInput(HtmlConsts.INPUT_TYPE_SUBMIT, null, label));
    html.append(HtmlConsts.FORM_CLOSE);
    return html.toString();
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

  public static final String createHtmlButtonToGetServlet(String servletAddr, String label, Map<String, String> properties) throws UnsupportedEncodingException {
    return createHtmlButtonToHttpMethodServlet(HtmlConsts.GET,
        servletAddr, label, properties);
  }
}
