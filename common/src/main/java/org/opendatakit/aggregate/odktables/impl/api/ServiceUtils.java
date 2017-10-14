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
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;

import org.opendatakit.aggregate.odktables.rest.ApiConstants;

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

  @SuppressWarnings({ "rawtypes", "unused" })
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
    String ace = req.getHeader(ApiConstants.ACCEPT_CONTENT_ENCODING_HEADER);
    boolean sessionId = req.isRequestedSessionIdValid();
  }

  @SuppressWarnings("unused")
  public static void examineRequest(ServletContext sc, HttpServletRequest req, HttpHeaders httpHeaders) {
    MultivaluedMap<String,String> headers = httpHeaders.getRequestHeaders();
    StringBuilder b = new StringBuilder();
    for ( String headerName : headers.keySet() ) {
      List<String> fieldValues = headers.get(headerName);
      for (String fieldValue : fieldValues) {
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
