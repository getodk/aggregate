package org.opendatakit.aggregate.odktables.entity.serialization;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

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
  protected void writeInternal(JsonObject object, HttpOutputMessage message) throws IOException,
      HttpMessageNotWritableException {
    gson.toJson(object, new OutputStreamWriter(message.getBody()));
  }

}
