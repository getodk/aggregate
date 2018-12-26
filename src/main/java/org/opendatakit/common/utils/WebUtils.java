/*
 * Copyright (C) 2011 University of Washington
 * Copyright (C) 2018 Nafundi
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.opendatakit.common.web.constants.BasicConsts;
import org.opendatakit.common.web.constants.HtmlConsts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebUtils {

  static final String IS_FORWARD_CURSOR_VALUE_TAG = "isForwardCursor";
  static final String URI_LAST_RETURNED_VALUE_TAG = "uriLastReturnedValue";
  static final String ATTRIBUTE_VALUE_TAG = "attributeValue";
  static final String ATTRIBUTE_NAME_TAG = "attributeName";
  static final String CURSOR_TAG = "cursor";
  static final Logger logger = LoggerFactory.getLogger(WebUtils.class);

  private WebUtils() {
  }

  public static Boolean parseBoolean(String value) {
    Boolean b = null;
    if (value != null && value.length() != 0) {
      b = Boolean.FALSE;
      if (value.compareToIgnoreCase("ok") == 0) {
        b = Boolean.TRUE;
      } else if (value.compareToIgnoreCase("yes") == 0) {
        b = Boolean.TRUE;
      } else if (value.compareToIgnoreCase("true") == 0) {
        b = Boolean.TRUE;
      } else if (value.compareToIgnoreCase("T") == 0) {
        b = Boolean.TRUE;
      } else if (value.compareToIgnoreCase("Y") == 0) {
        b = Boolean.TRUE;
      }
    }
    return b;
  }

  public static String readResponse(HttpResponse resp) throws IOException {

    HttpEntity e = resp.getEntity();
    if (e != null) {
      return WebUtils.readResponseHelper(e.getContent());
    }

    return BasicConsts.EMPTY_STRING;
  }

  public static String readGoogleResponse(com.google.api.client.http.HttpResponse resp) throws IOException {
    if (resp != null) {
      return WebUtils.readResponseHelper(resp.getContent());
    }

    return BasicConsts.EMPTY_STRING;
  }

  private static String readResponseHelper(InputStream content) {
    StringBuffer response = new StringBuffer();

    if (content != null) {
      // TODO: this section of code is possibly causing 'WARNING: Going to
      // buffer
      // response body of large or unknown size. Using getResponseBodyAsStream
      // instead is recommended.'
      // The WARNING is most likely only happening when running appengine
      // locally,
      // but we should investigate to make sure
      BufferedReader reader = null;
      InputStreamReader isr = null;
      try {
        reader = new BufferedReader(isr = new InputStreamReader(content, HtmlConsts.UTF8_ENCODE));
        String responseLine;
        while ((responseLine = reader.readLine()) != null) {
          response.append(responseLine);
        }
      } catch (UnsupportedEncodingException ex) {
        ex.printStackTrace();
      } catch (IllegalStateException ex) {
        ex.printStackTrace();
      } catch (IOException ex) {
        ex.printStackTrace();
      } finally {
        try {
          if (reader != null) {
            reader.close();
          }
        } catch (IOException ex) {
          // no-op
        }
        try {
          if (isr != null) {
            isr.close();
          }
        } catch (IOException ex) {
          // no-op
        }
      }
    }
    return response.toString();
  }

}
