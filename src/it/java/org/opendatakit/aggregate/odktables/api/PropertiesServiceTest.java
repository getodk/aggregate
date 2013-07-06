package org.opendatakit.aggregate.odktables.api;

import static org.junit.Assert.assertEquals;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.opendatakit.aggregate.odktables.rest.entity.OdkTablesKeyValueStoreEntry;
import org.opendatakit.aggregate.odktables.rest.entity.TableProperties;
import org.opendatakit.aggregate.odktables.rest.entity.PropertiesResource;
import org.opendatakit.aggregate.odktables.rest.entity.TableResource;
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
    assertEquals(T.tableId, properties.getTableId());
    // TODO: fix this!
    // assertEquals(T.tableMetadata, properties.getMetadata());
  }

  // TODO: fix this -- once we straighten out TableKey and TableId
  @Ignore
  public void testSetTableName() {
    String expected = T.tableName + " a different name";

    PropertiesResource resource = rt.getForObject(baseUri, PropertiesResource.class);
    TableProperties properties = resource;
    properties.setTableId(expected);

    ResponseEntity<PropertiesResource> response = rt.exchange(baseUri, HttpMethod.PUT,
        entity(properties), PropertiesResource.class);
    resource = response.getBody();
    assertEquals(expected, resource.getTableId());
  }

  @Test
  public void testSetTableMetadata() {
    String expected = T.tableMetadata + "some metadata here";
    List<OdkTablesKeyValueStoreEntry> list = new ArrayList<OdkTablesKeyValueStoreEntry>();
    OdkTablesKeyValueStoreEntry entry = new OdkTablesKeyValueStoreEntry();
    entry.partition = "Table";
    entry.aspect = "testing";
    entry.key = "value";
    entry.type = "text";
    list.add(entry);
    PropertiesResource resource = rt.getForObject(baseUri, PropertiesResource.class);
    TableProperties properties = resource;
    properties.setKeyValueStoreEntries(list);

    ResponseEntity<PropertiesResource> response = rt.exchange(baseUri, HttpMethod.PUT,
        entity(properties), PropertiesResource.class);
    resource = response.getBody();

    List<OdkTablesKeyValueStoreEntry> returnedList = resource.getKeyValueStoreEntries();
    assertEquals(list.size(), returnedList.size());
    for ( int i = 0 ; i < list.size() ; ++i ) {
      assertEquals( list.get(i), returnedList.get(i));
    }
  }

}
