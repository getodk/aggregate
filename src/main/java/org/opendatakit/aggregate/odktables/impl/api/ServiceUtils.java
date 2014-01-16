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
package org.opendatakit.aggregate.odktables.impl.api;

import java.util.Enumeration;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**
 * Class to extract and format the request information coming in from ODK Tables
 * clients so that we can debug protocols more easily.
 *
 * @author mitchellsundt@gmail.com
 *
 */
public class ServiceUtils {

  private ServiceUtils() {
  };

  public static void examineRequest(ServletContext sc, HttpServletRequest req) {
    Enumeration headers = req.getHeaderNames();
    StringBuilder b = new StringBuilder();
    while (headers.hasMoreElements()) {
      String headerName = (String) headers.nextElement();
      Enumeration fieldValues = req.getHeaders(headerName);
      while (fieldValues.hasMoreElements()) {
        String fieldValue = (String) fieldValues.nextElement();
        b.append(headerName).append(": ").append(fieldValue).append("\n");
      }
    }
    String contentType = req.getContentType();
    String charEncoding = req.getCharacterEncoding();
    String headerSet = b.toString();
    Cookie[] cookies = req.getCookies();
    String method = req.getMethod();
    String ctxtPath = req.getContextPath();
    String pathInfo = req.getPathInfo();
    String query = req.getQueryString();
    boolean sessionId = req.isRequestedSessionIdValid();
  }
}
