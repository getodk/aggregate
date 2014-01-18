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

package org.opendatakit.aggregate.odktables.rest.serialization;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.opendatakit.aggregate.odktables.rest.ApiConstants;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class JsonObjectHttpMessageConverter extends AbstractHttpMessageConverter<JsonObject> {

  private JsonParser parser;
  private Gson gson;

  public JsonObjectHttpMessageConverter() {
    super(MediaType.APPLICATION_JSON);
    this.parser = new JsonParser();
    this.gson = new Gson();
  }

  @Override
  protected JsonObject readInternal(Class<? extends JsonObject> clazz, HttpInputMessage message)
      throws IOException, HttpMessageNotReadableException {
    return parser.parse(new InputStreamReader(message.getBody())).getAsJsonObject();
  }

  @Override
  protected boolean supports(Class<?> clazz) {
    return clazz.equals(JsonObject.class);
  }

  @Override
  protected void writeInternal(JsonObject object, HttpOutputMessage outputMessage)
      throws IOException, HttpMessageNotWritableException {
    HttpHeaders headers = outputMessage.getHeaders();
    headers.add(ApiConstants.OPEN_DATA_KIT_VERSION_HEADER, ApiConstants.OPEN_DATA_KIT_VERSION);
    GregorianCalendar g = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
    g.setTime(new Date());
    SimpleDateFormat formatter = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss zz");
    formatter.setCalendar(g);
    headers.add(ApiConstants.DATE_HEADER, formatter.format(new Date()));
    headers.setContentType(new MediaType("application", "json")); // assumes UTF-8

    Writer writer = new OutputStreamWriter(outputMessage.getBody(), ApiConstants.UTF8_ENCODE);
    gson.toJson(object, writer);
  }

}
