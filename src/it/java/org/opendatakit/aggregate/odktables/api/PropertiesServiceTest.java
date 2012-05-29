package org.opendatakit.aggregate.odktables.api;

import static org.junit.Assert.assertEquals;

import java.net.URI;

import org.junit.Before;
import org.junit.Test;
import org.opendatakit.aggregate.odktables.entity.TableProperties;
import org.opendatakit.aggregate.odktables.entity.api.PropertiesResource;
import org.opendatakit.aggregate.odktables.entity.api.TableResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

public class PropertiesServiceTest extends AbstractServiceTest {

  @Before
  public void setUp() throws Exception {
    super.createTable();
    TableResource resource = rt.getForObject(baseUri.resolve(T.tableId), TableResource.class);
    baseUri = URI.create(resource.getPropertiesUri());
  }

  @Test
  public void testGetProperties() {
    PropertiesResource properties = rt.getForObject(baseUri, PropertiesResource.class);
    assertEquals(T.tableName, properties.getTableName());
    assertEquals(T.tableMetadata, properties.getMetadata());
  }

  @Test
  public void testSetTableName() {
    String expected = T.tableName + " a different name";

    PropertiesResource resource = rt.getForObject(baseUri, PropertiesResource.class);
    TableProperties properties = resource;
    properties.setTableName(expected);

    ResponseEntity<PropertiesResource> response = rt.exchange(baseUri, HttpMethod.PUT,
        entity(properties), PropertiesResource.class);
    resource = response.getBody();
    assertEquals(expected, resource.getTableName());
  }

  @Test
  public void testSetTableMetadata() {
    String expected = T.tableMetadata + "some metadata here";

    PropertiesResource resource = rt.getForObject(baseUri, PropertiesResource.class);
    TableProperties properties = resource;
    properties.setMetadata(expected);

    ResponseEntity<PropertiesResource> response = rt.exchange(baseUri, HttpMethod.PUT,
        entity(properties), PropertiesResource.class);
    resource = response.getBody();
    assertEquals(expected, resource.getMetadata());
  }

}
