package org.opendatakit.aggregate.odktables.api;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.opendatakit.aggregate.odktables.entity.api.TableDefinition;
import org.opendatakit.aggregate.odktables.entity.api.TableResource;
import org.opendatakit.aggregate.odktables.entity.serialization.SimpleXMLSerializerForAggregate;
import org.simpleframework.xml.Serializer;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

public abstract class AbstractServiceTest {

  protected URI baseUri;
  protected RestTemplate rt;
  protected HttpHeaders reqHeaders;

  @Before
  public void setUp() throws Exception {
    this.baseUri = URI.create("http://localhost:8888/odktables/tables/");

    // RestTemplate
    this.rt = new RestTemplate();
    Serializer serializer = SimpleXMLSerializerForAggregate.getSerializer();
    List<HttpMessageConverter<?>> converters = new ArrayList<HttpMessageConverter<?>>();

    converters.add(new SimpleXmlHttpMessageConverter(serializer));
    this.rt.setMessageConverters(converters);

    // HttpHeaders
    List<MediaType> acceptableMediaTypes = new ArrayList<MediaType>();
    acceptableMediaTypes.add(new MediaType("text", "xml"));

    this.reqHeaders = new HttpHeaders();
    reqHeaders.setAccept(acceptableMediaTypes);
    reqHeaders.setContentType(new MediaType("text", "xml"));
  }

  @After
  public void tearDown() throws Exception {
    try {
      baseUri = baseUri.resolve("/odktables/tables/");
      URI uri = baseUri.resolve(T.tableId);
      this.rt.delete(uri);
    } catch (Exception e) {
      // ignore
      System.out.println(e);
    }
  }

  protected TableResource createTable() {
    URI uri = baseUri.resolve(T.tableId);

    TableDefinition definition = new TableDefinition(T.tableName, T.columns, T.tableMetadata);

    HttpEntity<TableDefinition> entity = entity(definition);

    ResponseEntity<TableResource> resp = rt.exchange(uri, HttpMethod.PUT, entity,
        TableResource.class);
    return resp.getBody();
  }

  protected <V> HttpEntity<V> entity(V entity) {
    return new HttpEntity<V>(entity, reqHeaders);
  }
}
