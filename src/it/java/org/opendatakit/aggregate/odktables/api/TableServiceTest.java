package org.opendatakit.aggregate.odktables.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.List;

import org.junit.Test;
import org.opendatakit.aggregate.odktables.rest.entity.TableResource;
import org.springframework.web.client.HttpClientErrorException;

public class TableServiceTest extends AbstractServiceTest {

  @Test
  public void testGetTablesEmpty() {
    List<?> resp = rt.getForObject(baseUri, List.class);
    assertTrue(resp.isEmpty());
  }

  @Test(expected = HttpClientErrorException.class)
  public void testGetTableDoesNotExist() {
    URI uri = baseUri.resolve("non-existent-table");
    rt.getForObject(uri, TableResource.class);
  }

  @Test
  public void testCreateTable() {
    TableResource resource = createTable();
    assertEquals(T.tableId, resource.getTableId());
  }

  @Test(expected = HttpClientErrorException.class)
  public void testCreateTableAlreadyExists() {
    createTable();
    createTable();
  }

  @Test
  public void testGetTable() {
    TableResource expected = createTable();
    String uri = expected.getSelfUri();
    TableResource actual = rt.getForObject(uri, TableResource.class);
    assertEquals(expected, actual);
  }

}
