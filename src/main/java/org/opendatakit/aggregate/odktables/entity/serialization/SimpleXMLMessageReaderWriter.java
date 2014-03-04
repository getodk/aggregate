/*
 * Copyright (C) 2012-2013 University of Washington
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

package org.opendatakit.aggregate.odktables.entity.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.opendatakit.aggregate.odktables.rest.ApiConstants;
import org.opendatakit.aggregate.odktables.rest.serialization.SimpleXMLSerializerForAggregate;
import org.simpleframework.xml.Serializer;

@Produces({ MediaType.TEXT_XML, MediaType.APPLICATION_XML })
@Consumes({ MediaType.TEXT_XML, MediaType.APPLICATION_XML })
@Provider
public class SimpleXMLMessageReaderWriter implements MessageBodyReader<Object>,
    MessageBodyWriter<Object> {

  private static final Serializer serializer;
  static {
    serializer = SimpleXMLSerializerForAggregate.getSerializer();
  }
  private static final String DEFAULT_ENCODING = "utf-8";

  @Context
  HttpHeaders headers;

  @Override
  public boolean isReadable(Class<?> type, Type genericType, Annotation annotations[],
      MediaType mediaType) {
    return (mediaType.getType().equals("text") || mediaType.getType().equals("application"))
        && mediaType.getSubtype().equals("xml");
  }

  @Override
  public boolean isWriteable(Class<?> type, Type genericType, Annotation annotations[],
      MediaType mediaType) {
    return (mediaType.getType().equals("text") || mediaType.getType().equals("application"))
        && mediaType.getSubtype().equals("xml");
  }

  @Override
  public Object readFrom(Class<Object> aClass, Type genericType, Annotation[] annotations,
      MediaType mediaType, MultivaluedMap<String, String> map, InputStream stream)
      throws IOException, WebApplicationException {
    String charset = getCharsetAsString(mediaType);
    if (!charset.equalsIgnoreCase(DEFAULT_ENCODING)) {
      throw new IllegalArgumentException("charset for the request is not utf-8");
    }
    try {
      Reader r = new InputStreamReader(stream, Charset.forName(ApiConstants.UTF8_ENCODE));
      return serializer.read(aClass, r);
    } catch (Exception e) {
      throw new IOException(e);
    }
  }

  @Override
  public void writeTo(Object o, Class<?> aClass, Type type, Annotation[] annotations,
      MediaType mediaType, MultivaluedMap<String, Object> map, OutputStream rawStream)
      throws IOException, WebApplicationException {
    String encoding = getCharsetAsString(mediaType);
    if (!encoding.equalsIgnoreCase(DEFAULT_ENCODING)) {
      throw new IllegalArgumentException("charset for the response is not utf-8");
    }
    try {
      if (mediaType.getParameters().get("charset") == null) {
        mediaType.getParameters().put("charset", DEFAULT_ENCODING);
      }
      Writer writer = new OutputStreamWriter(rawStream, Charset.forName(ApiConstants.UTF8_ENCODE));
      serializer.write(o, writer);
    } catch (Exception e) {
      throw new IOException(e);
    }
  }

  @Override
  public long getSize(Object arg0, Class<?> arg1, Type arg2, Annotation[] arg3, MediaType arg4) {
    return -1;
  }

  protected static String getCharsetAsString(MediaType m) {
    if (m == null) {
      return DEFAULT_ENCODING;
    }
    String result = m.getParameters().get("charset");
    return (result == null) ? DEFAULT_ENCODING : result;
  }
}
