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
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.ws.rs.core.Context;

import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.type.JavaType;
import org.opendatakit.aggregate.odktables.rest.ApiConstants;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
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

  public OdkJsonHttpMessageConverter() {
    super();
    getObjectMapper().disable(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES);

  }

  @Override
  protected Object readInternal(Class<? extends Object> clazz, HttpInputMessage inputMessage)
      throws IOException {
    InputStream stream;
    List<String> encodings = inputMessage.getHeaders().get(ApiConstants.CONTENT_ENCODING_HEADER);
    if ( encodings != null && encodings.contains(ApiConstants.GZIP_CONTENT_ENCODING) ) {
      stream = new GZIPInputStream(inputMessage.getBody());
    } else {
      stream = inputMessage.getBody();
    }

    JavaType javaType = getJavaType(clazz);
    try {
       return this.getObjectMapper().readValue(stream, javaType);
    }
    catch (Exception ex) {
       throw new HttpMessageNotReadableException("Could not read [" + clazz + "] JSON: " + ex.getMessage(), ex);
    }
  }

  @Override
  protected void writeInternal(Object o, HttpOutputMessage outputMessage) throws IOException {
    HttpHeaders headers = outputMessage.getHeaders();
    headers.add(ApiConstants.OPEN_DATA_KIT_VERSION_HEADER, ApiConstants.OPEN_DATA_KIT_VERSION);
    GregorianCalendar g = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
    g.setTime(new Date());
    SimpleDateFormat formatter = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss zz");
    formatter.setCalendar(g);
    headers.add(ApiConstants.DATE_HEADER, formatter.format(new Date()));
    headers.add(ApiConstants.ACCEPT_CONTENT_ENCODING_HEADER, ApiConstants.GZIP_CONTENT_ENCODING);
    // see if we should gzip the output
    OutputStream stream;
    if ( requestHeaders == null ) {
      // always send data to the server as encoded
//      headers.set(ApiConstants.CONTENT_ENCODING_HEADER, ApiConstants.GZIP_CONTENT_ENCODING);
//      stream = new GZIPOutputStream(outputMessage.getBody());
      stream = outputMessage.getBody();
    } else {
      List<String> encodings = requestHeaders.get(ApiConstants.ACCEPT_CONTENT_ENCODING_HEADER);
      if ( encodings != null && encodings.contains(ApiConstants.GZIP_CONTENT_ENCODING) ) {
        headers.set(ApiConstants.CONTENT_ENCODING_HEADER, ApiConstants.GZIP_CONTENT_ENCODING);
        stream = new GZIPOutputStream(outputMessage.getBody());
      } else {
        stream = outputMessage.getBody();
      }
    }
    // and emit the output
    JsonEncoding encoding = getJsonEncoding(outputMessage.getHeaders().getContentType());
    JsonGenerator jsonGenerator =
          this.getObjectMapper().getJsonFactory().createJsonGenerator(stream, encoding);
    try {
       this.getObjectMapper().writeValue(jsonGenerator, o);
       jsonGenerator.flush();
       stream.flush();
       jsonGenerator.close();
       stream.close();
    }
    catch (JsonProcessingException ex) {
       throw new HttpMessageNotWritableException("Could not write JSON: " + ex.getMessage(), ex);
    }
  }
}