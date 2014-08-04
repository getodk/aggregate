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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.lang3.CharEncoding;
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

  /**
   * Handles properly decoding a path segment of a URL for use over the wire.
   * Converts URL segment back into arbitrary characters.
   *
   * @param encodedSegment
   * @return decodedSegment
   */
  public static String decodeSegment(String segment) {
    // the Android side uses UTF-8 encoding. This may or may not be appropriate...
    String encoding = CharEncoding.UTF_8;
    String decodedSegment;
    try {
      decodedSegment = segment
          .replaceAll("~", "\\%7E")
          .replaceAll("\\)", "\\%29")
          .replaceAll("\\(", "\\%28")
          .replaceAll("'", "\\%27")
          .replaceAll("!", "\\%21")
          .replaceAll("%20", "\\+");
      decodedSegment = URLDecoder.decode(decodedSegment, encoding);
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
      throw new IllegalStateException("Should be able to encode with " + encoding);
    }

    return decodedSegment;
  }


  private static final String URL_TO_ESCAPE = "uuid:3A826bc1df-d018-427d-863d-aa93818c10ee";// www.le\u6f22\u8a9ev(elu p.om?so' ti:he ;'me(var=ab)!~c123&some:other var";

  public static String encodeSegmentTest() {
    boolean bad = false;
    String twoD = encodeSegmentImpl(URL_TO_ESCAPE);
    String out = decodeSegment(twoD);
    if ( !out.equals(URL_TO_ESCAPE)) {
      bad = true;
    }
    twoD = encodeSegmentImpl("http://f.c/" + URL_TO_ESCAPE);
    out = decodeSegment(twoD);
    if ( !out.equals("http://f.c/" + URL_TO_ESCAPE)) {
      bad = true;
    }
    return twoD;
  }

  /**
   * Handles properly encoding a path segment of a URL for use over the wire.
   * Converts arbitrary characters into those appropriate for a segment in a
   * URL path.
   *
   * @param segment
   * @return encodedSegment
   */
  public static String encodeSegment(String segment) {
    encodeSegmentTest();
    return encodeSegmentImpl(segment);
  }

  public static String encodeSegmentImpl(String segment) {
    // the segment can have URI-inappropriate characters. Encode it first...
    // the Android side uses UTF-8 encoding. This may or may not be appropriate...
//    String encodedSegment = Uri.encode(segment, null);
    String encoding = CharEncoding.UTF_8;
    String encodedSegment;
    try {
      encodedSegment = URLEncoder.encode(segment, encoding)
                  .replaceAll("\\+", "%20")
                  .replaceAll("\\%21", "!")
                  .replaceAll("\\%27", "'")
                  .replaceAll("\\%28", "(")
                  .replaceAll("\\%29", ")")
                  .replaceAll("\\%7E", "~");

    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
      throw new IllegalStateException("Should be able to encode with " + encoding);
    }
    return encodedSegment;
  }
}
