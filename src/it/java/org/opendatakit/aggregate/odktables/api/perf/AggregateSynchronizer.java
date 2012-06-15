/*
 * Copyright (C) 2012 University of Washington
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
package org.opendatakit.aggregate.odktables.api.perf;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.opendatakit.aggregate.odktables.api.client.AggregateRequestInterceptor;
import org.opendatakit.aggregate.odktables.entity.Column;
import org.opendatakit.aggregate.odktables.entity.Row;
import org.opendatakit.aggregate.odktables.entity.TableProperties;
import org.opendatakit.aggregate.odktables.entity.api.PropertiesResource;
import org.opendatakit.aggregate.odktables.entity.api.RowResource;
import org.opendatakit.aggregate.odktables.entity.api.TableDefinition;
import org.opendatakit.aggregate.odktables.entity.api.TableResource;
import org.opendatakit.aggregate.odktables.entity.serialization.JsonObjectHttpMessageConverter;
import org.opendatakit.aggregate.odktables.entity.serialization.ListConverter;
import org.opendatakit.aggregate.odktables.entity.serialization.SimpleXMLSerializerForAggregate;
import org.opendatakit.aggregate.odktables.entity.serialization.SimpleXmlHttpMessageConverter;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.convert.Registry;
import org.simpleframework.xml.convert.RegistryStrategy;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.strategy.Strategy;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * @author the.dylan.price@gmail.com
 */
public class AggregateSynchronizer {

  private static final String TOKEN_INFO = "https://www.googleapis.com/oauth2/v1/tokeninfo?access_token=";

  private final RestTemplate rt;
  private final HttpHeaders requestHeaders;
  private final URI baseUri;
  private final Map<String, TableResource> resources;

  public AggregateSynchronizer(String aggregateUri, String accessToken)
      throws InvalidAuthTokenException {
    URI uri = URI.create(aggregateUri).normalize();
    uri = uri.resolve("/odktables/tables/").normalize();
    this.baseUri = uri;

    List<ClientHttpRequestInterceptor> interceptors = new ArrayList<ClientHttpRequestInterceptor>();
    interceptors.add(new AggregateRequestInterceptor(accessToken));

    this.rt = new RestTemplate();
//    this.rt.setInterceptors(interceptors);

    Serializer serializer = SimpleXMLSerializerForAggregate.getSerializer();
    List<HttpMessageConverter<?>> converters = new ArrayList<HttpMessageConverter<?>>();

    converters.add(new JsonObjectHttpMessageConverter());
    converters.add(new SimpleXmlHttpMessageConverter(serializer));
    this.rt.setMessageConverters(converters);

    List<MediaType> acceptableMediaTypes = new ArrayList<MediaType>();
    acceptableMediaTypes.add(new MediaType("text", "xml"));

    this.requestHeaders = new HttpHeaders();
    this.requestHeaders.setAccept(acceptableMediaTypes);
    this.requestHeaders.setContentType(new MediaType("text", "xml"));

    this.resources = new ConcurrentHashMap<String, TableResource>();

//    checkAccessToken(accessToken);
  }

  private void checkAccessToken(String accessToken) throws InvalidAuthTokenException {
    try {
      rt.getForObject(TOKEN_INFO + accessToken, JsonObject.class);
    } catch (HttpClientErrorException e) {
      JsonParser parser = new JsonParser();
      JsonObject resp = parser.parse(e.getResponseBodyAsString()).getAsJsonObject();
      if (resp.has("error") && resp.get("error").getAsString().equals("invalid_token")) {
        throw new InvalidAuthTokenException("Invalid auth token: " + accessToken, e);
      }
    }
  }

  @SuppressWarnings("unchecked")
  public Map<String, String> getTables() throws IOException {
    Map<String, String> tables = new HashMap<String, String>();

    List<TableResource> tableResources;
    try {
      tableResources = rt.getForObject(baseUri, List.class);
    } catch (ResourceAccessException e) {
      throw new IOException(e.getMessage());
    }

    for (TableResource tableResource : tableResources)
      tables.put(tableResource.getTableId(), tableResource.getTableName());

    return tables;
  }

  public TableResource createTable(String tableId, String tableName, List<Column> columns,
      String tableProperties) throws IOException {

    // build request
    URI uri = baseUri.resolve(tableId);
    TableDefinition definition = new TableDefinition(tableName, columns, tableProperties);
    HttpEntity<TableDefinition> requestEntity = new HttpEntity<TableDefinition>(definition,
        requestHeaders);

    // create table
    ResponseEntity<TableResource> resourceEntity;
    try {
      resourceEntity = rt.exchange(uri, HttpMethod.PUT, requestEntity, TableResource.class);
    } catch (ResourceAccessException e) {
      throw new IOException(e.getMessage());
    }
    TableResource resource = resourceEntity.getBody();

    // save resource
    this.resources.put(resource.getTableId(), resource);

    return resource;
  }

  private TableResource getResource(String tableId) throws IOException {
    if (resources.containsKey(tableId)) {
      return resources.get(tableId);
    } else {
      return refreshResource(tableId);
    }
  }

  private TableResource refreshResource(String tableId) throws IOException {
    URI uri = baseUri.resolve(tableId);
    TableResource resource;
    try {
      resource = rt.getForObject(uri, TableResource.class);
    } catch (ResourceAccessException e) {
      throw new IOException(e.getMessage());
    }
    resources.put(resource.getTableId(), resource);
    return resource;
  }

  public void deleteTable(String tableId) {
    rt.delete(baseUri.resolve(tableId));
  }

  public RowResource putRow(String tableId, Row row) throws IOException {
    TableResource resource = getResource(tableId);
    Map<String, String> rowEtags = new HashMap<String, String>();

    URI url = URI.create(resource.getDataUri() + "/" + row.getRowId()).normalize();
    HttpEntity<Row> requestEntity = new HttpEntity<Row>(row, requestHeaders);
    ResponseEntity<RowResource> insertedEntity;
    try {
      insertedEntity = rt.exchange(url, HttpMethod.PUT, requestEntity, RowResource.class);
    } catch (ResourceAccessException e) {
      throw new IOException(e.getMessage());
    }
    RowResource putRow = insertedEntity.getBody();
    rowEtags.put(putRow.getRowId(), putRow.getRowEtag());

    return putRow;
  }

  public void deleteRow(String tableId, String rowId) throws IOException {
    TableResource resource = getResource(tableId);

    URI url = URI.create(resource.getDataUri() + "/" + rowId).normalize();
    try {
      rt.delete(url);
    } catch (ResourceAccessException e) {
      throw new IOException(e.getMessage());
    }
  }

  public PropertiesResource setTableProperties(String tableId, String propertiesEtag,
      String tableName, String tableProperties) throws IOException {
    TableResource resource = getResource(tableId);

    // put new properties
    TableProperties properties = new TableProperties(propertiesEtag, tableName, tableProperties);
    HttpEntity<TableProperties> entity = new HttpEntity<TableProperties>(properties, requestHeaders);
    ResponseEntity<PropertiesResource> updatedEntity;
    try {
      updatedEntity = rt.exchange(resource.getPropertiesUri(), HttpMethod.PUT, entity,
          PropertiesResource.class);
    } catch (ResourceAccessException e) {
      throw new IOException(e.getMessage());
    }
    PropertiesResource propsResource = updatedEntity.getBody();

    return propsResource;
  }

  public class InvalidAuthTokenException extends Exception {

    private static final long serialVersionUID = 1L;

    public InvalidAuthTokenException() {
      super();
    }

    public InvalidAuthTokenException(String message, Throwable cause) {
      super(message, cause);
    }

    public InvalidAuthTokenException(String message) {
      super(message);
    }

    public InvalidAuthTokenException(Throwable cause) {
      super(cause);
    }

  }
}
