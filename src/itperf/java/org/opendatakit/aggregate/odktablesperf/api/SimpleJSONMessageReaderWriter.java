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

package org.opendatakit.aggregate.odktablesperf.api;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.opendatakit.aggregate.odktables.rest.ApiConstants;

import com.fasterxml.jackson.databind.ObjectMapper;

@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_JSON})
@Provider
public class SimpleJSONMessageReaderWriter<T> implements MessageBodyReader<T>,
    MessageBodyWriter<T> {

  private static final ObjectMapper mapper = new ObjectMapper();
  private static final String DEFAULT_ENCODING = "utf-8";

  @Override
  public boolean isReadable(Class<?> type, Type genericType, Annotation annotations[],
      MediaType mediaType) {
    return mediaType.getType().equals(MediaType.APPLICATION_JSON_TYPE.getType())
        && mediaType.getSubtype().equals(MediaType.APPLICATION_JSON_TYPE.getSubtype());
  }

  @Override
  public boolean isWriteable(Class<?> type, Type genericType, Annotation annotations[],
      MediaType mediaType) {
    return mediaType.getType().equals(MediaType.APPLICATION_JSON_TYPE.getType())
        && mediaType.getSubtype().equals(MediaType.APPLICATION_JSON_TYPE.getSubtype());
  }

  @Override
  public T readFrom(Class<T> aClass, Type genericType, Annotation[] annotations,
      MediaType mediaType, MultivaluedMap<String, String> map, InputStream stream)
      throws IOException, WebApplicationException {
    String encoding = getCharsetAsString(mediaType);
    try {
      if (!encoding.equalsIgnoreCase(DEFAULT_ENCODING)) {
        throw new IllegalArgumentException("charset for the request is not utf-8");
      }

      InputStreamReader r = new InputStreamReader(stream);
      return mapper.readValue(r, aClass);
    } catch (Exception e) {
      throw new IOException(e);
    }
  }

  @Override
  public void writeTo(T o, Class<?> aClass, Type type, Annotation[] annotations,
      MediaType mediaType, MultivaluedMap<String, Object> map, OutputStream rawStream)
      throws IOException, WebApplicationException {
    String encoding = getCharsetAsString(mediaType);
    try {
      if (!encoding.equalsIgnoreCase(DEFAULT_ENCODING)) {
        throw new IllegalArgumentException("charset for the response is not utf-8");
      }
      // write it to a byte array
      ByteArrayOutputStream bas = new ByteArrayOutputStream(8192);
      OutputStreamWriter w = new OutputStreamWriter(bas,
          Charset.forName(ApiConstants.UTF8_ENCODE));
      mapper.writeValue(w, o);
      // get the array and compute md5 hash
      byte[] bytes = bas.toByteArray();
      String eTag;
      try {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(bytes);

        byte[] messageDigest = md.digest();

        BigInteger number = new BigInteger(1, messageDigest);
        String md5 = number.toString(16);
        while (md5.length() < 32)
          md5 = "0" + md5;
        eTag = "md5_" + md5;
      } catch (NoSuchAlgorithmException e) {
        throw new IllegalStateException("Unexpected problem computing md5 hash", e);
      }
      map.putSingle(HttpHeaders.ETAG, eTag);

      rawStream.write(bytes);
      rawStream.flush();

    } catch (Exception e) {
      throw new IOException(e);
    }
  }

  @Override
  public long getSize(T arg0, Class<?> arg1, Type arg2, Annotation[] arg3, MediaType arg4) {
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
