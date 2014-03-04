/*
 * Copyright (C) 2014 University of Washington
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

package org.opendatakit.aggregate.odktables.rest.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import javax.ws.rs.core.Context;

import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.type.JavaType;
import org.opendatakit.aggregate.odktables.rest.ApiConstants;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;

/**
 * Implementation of
 * {@link org.springframework.http.converter.json.MappingJacksonHttpMessageConverter}
 * that can read and write JSON using Jackson
 * <p>
 * This converter supports {@code application/json}
 *
 * @author mitchellsundt@gmail.com
 */
public class OdkJsonHttpMessageConverter extends MappingJacksonHttpMessageConverter {

  @Context
  private HttpHeaders requestHeaders;

  private static final String DEFAULT_ENCODING = "utf-8";

  public OdkJsonHttpMessageConverter(boolean ignoreContentEncoding) {
    super();
    getObjectMapper().disable(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES);

  }

  @Override
  protected Object readInternal(Class<? extends Object> clazz, HttpInputMessage inputMessage)
      throws IOException {
    InputStream stream;
    HttpHeaders headers = inputMessage.getHeaders();
    String charset = getCharsetAsString(headers.getContentType());
    if (!charset.equalsIgnoreCase(DEFAULT_ENCODING)) {
      throw new IllegalArgumentException("charset for the request is not utf-8");
    }
    try {
      // Android RestTemplate already does GZIP decoding before it gets to this message converter
      stream = inputMessage.getBody();
      InputStreamReader r = new InputStreamReader(stream, Charset.forName(ApiConstants.UTF8_ENCODE));
      JavaType javaType = getJavaType(clazz);
      return this.getObjectMapper().readValue(r, javaType);
    } catch (Exception ex) {
      throw new HttpMessageNotReadableException("Could not read [" + clazz + "] JSON: "
          + ex.getMessage(), ex);
    }
  }

  @Override
  protected void writeInternal(Object o, HttpOutputMessage outputMessage) throws IOException {
    try {
      HttpHeaders headers = outputMessage.getHeaders();
      headers.add(ApiConstants.OPEN_DATA_KIT_VERSION_HEADER, ApiConstants.OPEN_DATA_KIT_VERSION);
      GregorianCalendar g = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
      g.setTime(new Date());
      SimpleDateFormat formatter = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss zz");
      formatter.setCalendar(g);
      headers.add(ApiConstants.DATE_HEADER, formatter.format(new Date()));
      headers.add(ApiConstants.ACCEPT_CONTENT_ENCODING_HEADER, ApiConstants.GZIP_CONTENT_ENCODING);

      // see if we should gzip the output
      // The actual GZipping is done by a wrapper based upon the Content-Encoding setting
      OutputStream rawStream = outputMessage.getBody();
      if (requestHeaders == null) {
        // always send data to the server as encoded
        headers.add(ApiConstants.CONTENT_ENCODING_HEADER, ApiConstants.GZIP_CONTENT_ENCODING);
      } else {
        List<String> encodings = requestHeaders.get(ApiConstants.ACCEPT_CONTENT_ENCODING_HEADER);
        if (encodings != null && encodings.contains(ApiConstants.GZIP_CONTENT_ENCODING)) {
          headers.add(ApiConstants.CONTENT_ENCODING_HEADER, ApiConstants.GZIP_CONTENT_ENCODING);
        }
      }

      headers.setContentType(new MediaType("application", "json", Charset
          .forName(ApiConstants.UTF8_ENCODE)));
      Writer writer = new OutputStreamWriter(rawStream, Charset.forName(ApiConstants.UTF8_ENCODE));
      this.getObjectMapper().writeValue(writer, o);
    } catch (JsonProcessingException ex) {
      throw new HttpMessageNotWritableException("Could not write JSON: " + ex.getMessage(), ex);
    }
  }

  protected static String getCharsetAsString(MediaType m) {
    if (m == null) {
      return DEFAULT_ENCODING;
    }
    String result = m.getParameters().get("charset");
    if (result != null && result.startsWith("\"") && result.endsWith("\"")) {
      // work-around for parameters being wrapped in quotes in Springframework.
      result = result.substring(1, result.length() - 1);
    }
    return (result == null) ? DEFAULT_ENCODING : result;
  }
}