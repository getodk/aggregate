/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

import org.opendatakit.aggregate.odktables.rest.ApiConstants;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.util.Assert;

/**
 * Implementation of
 * {@link org.springframework.http.converter.HttpMessageConverter
 * HttpMessageConverter} that can read and write XML using Simple's
 * {@link Persister} abstraction. *
 * <p>
 * By default, this converter supports {@code text/xml} and
 * {@code application/xml}. This can be overridden by setting the
 * {@link #setSupportedMediaTypes(java.util.List) supportedMediaTypes} property.
 *
 * @author Roy Clarkson
 * @since 1.0.0
 */
public class OdkXmlHttpMessageConverter extends AbstractHttpMessageConverter<Object> {

  private static final String DEFAULT_ENCODING = "utf-8";

  private Serializer serializer;

  @Context
  private HttpHeaders requestHeaders;

  public OdkXmlHttpMessageConverter() {
    super(MediaType.APPLICATION_XML, MediaType.TEXT_XML, new MediaType("application", "*+xml"));
    serializer = SimpleXMLSerializerForAggregate.getSerializer();
  }

  @Override
  protected boolean supports(Class<?> clazz) {
    return true;
  }

  @Override
  protected Object readInternal(Class<? extends Object> clazz, HttpInputMessage inputMessage)
      throws IOException {
    Assert.notNull(this.serializer, "Property 'serializer' is required");
    InputStream stream;
    HttpHeaders headers = inputMessage.getHeaders();
    String charset = getCharsetAsString(headers.getContentType());
    if ( !charset.equalsIgnoreCase(DEFAULT_ENCODING) ) {
      throw new IllegalArgumentException("charset for the request is not utf-8");
    }
    stream = inputMessage.getBody();
    InputStreamReader r = new InputStreamReader(stream, Charset.forName(ApiConstants.UTF8_ENCODE));
    try {
      Object result = this.serializer.read(clazz, r);
      if (!clazz.isInstance(result)) {
        throw new TypeMismatchException(result, clazz);
      }
      return result;
    } catch (Exception ex) {
      throw new HttpMessageNotReadableException("Could not read [" + clazz + "]", ex);
    }
  }

  @Override
  protected void writeInternal(Object o, HttpOutputMessage outputMessage) throws IOException {
    Assert.notNull(this.serializer, "Property 'serializer' is required");
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
      // the actual GZipping is done by a wrapper based upon the Content-Encoding
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

      headers
      .setContentType(new MediaType("text", "xml", Charset.forName(ApiConstants.UTF8_ENCODE)));
      Writer writer = new OutputStreamWriter(rawStream, Charset.forName(ApiConstants.UTF8_ENCODE));
      this.serializer.write(o, writer);
    } catch (Exception ex) {
      throw new HttpMessageNotWritableException("Could not write [" + o + "]", ex);
    }
  }

  protected static String getCharsetAsString(MediaType m) {
    if (m == null) {
      return DEFAULT_ENCODING;
    }
    String result = m.getParameters().get("charset");
    if ( result != null && result.startsWith("\"") && result.endsWith("\"") ) {
      // work-around for parameters being wrapped in quotes in Springframework.
      result = result.substring(1, result.length()-1);
    }
    return (result == null) ? DEFAULT_ENCODING : result;
  }
}