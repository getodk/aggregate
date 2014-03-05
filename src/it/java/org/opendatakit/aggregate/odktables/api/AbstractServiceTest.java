package org.opendatakit.aggregate.odktables.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.opendatakit.aggregate.odktables.api.T;
import org.opendatakit.aggregate.odktables.rest.entity.TableDefinition;
import org.opendatakit.aggregate.odktables.rest.entity.TableResource;
import org.opendatakit.aggregate.odktables.rest.serialization.OdkXmlHttpMessageConverter;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

public abstract class AbstractServiceTest {

  private String appId = "tables";
  protected URI baseUri;
  protected RestTemplate rt;
  protected HttpHeaders reqHeaders;

  @Before
  public void abstractServiceSetUp() throws Exception {
    this.baseUri = URI.create("http://localhost:8888/odktables/tables/" + appId + "/");

    // RestTemplate
    this.rt = new RestTemplate();
    this.rt.setErrorHandler(new ErrorHandler());
    List<HttpMessageConverter<?>> converters = new ArrayList<HttpMessageConverter<?>>();

    converters.add(new OdkXmlHttpMessageConverter());
    this.rt.setMessageConverters(converters);

    // HttpHeaders
    List<MediaType> acceptableMediaTypes = new ArrayList<MediaType>();
    acceptableMediaTypes.add(new MediaType("text", "xml"));

    this.reqHeaders = new HttpHeaders();
    reqHeaders.setAccept(acceptableMediaTypes);
    reqHeaders.setContentType(new MediaType("text", "xml"));
  }

  @After
  public void abstractServiceTearDown() throws Exception {
    try {
      baseUri = baseUri.resolve("/odktables/tables/" + appId + "/");
      URI uri = baseUri.resolve(T.tableId);
      this.rt.delete(uri);
    } catch (Exception e) {
      // ignore
      System.out.println(e);
    }
  }

  protected TableResource createTable() {
    URI uri = baseUri.resolve("/odktables/tables/" + appId + "/" + T.tableId);

    TableDefinition definition = new TableDefinition(T.tableId, null, T.columns);
    HttpEntity<TableDefinition> entity = entity(definition);

    ResponseEntity<TableResource> resp = rt.exchange(uri, HttpMethod.PUT, entity,
        TableResource.class);
    return resp.getBody();
  }

  protected <V> HttpEntity<V> entity(V entity) {
    return new HttpEntity<V>(entity, reqHeaders);
  }

  private class ErrorHandler implements ResponseErrorHandler {
    @Override
    public void handleError(ClientHttpResponse resp) throws IOException {
      HttpStatus status = resp.getStatusCode();
      String body = readInput(resp.getBody());
      if (status.value() / 100 == 4)
        throw new HttpClientErrorException(status, body);
      else if (status.value() / 100 == 5)
        throw new HttpServerErrorException(status, body);
    }

    @Override
    public boolean hasError(ClientHttpResponse resp) throws IOException {
      return resp.getStatusCode().value() / 100 != 2;
    }

    private String readInput(InputStream is) throws IOException {
      BufferedReader br = new BufferedReader(new InputStreamReader(is));
      StringBuilder sb = new StringBuilder();
      String line;
      while ((line = br.readLine()) != null) {
        sb.append(line);
      }
      return sb.toString();
    }
  }
}
