package org.opendatakit.aggregate.odktables.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.URI;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opendatakit.aggregate.odktables.rest.entity.TableResource;
import org.opendatakit.aggregate.odktables.rest.entity.TableResourceList;
import org.springframework.web.client.HttpClientErrorException;


public class TableServiceTest extends AbstractServiceTest {
  
  protected URI resourceUri;
  
  @Before
  public void setup() throws Exception, Throwable {
    super.abstractServiceSetUp();
  }

  @Test
  public void testGetTablesEmpty() {
    URI url = resolveUri(TABLE_API);
    TableResourceList tables = rt.getForObject(url, TableResourceList.class);
    assertTrue(tables.getTables().isEmpty());
  }

  @Test(expected = HttpClientErrorException.class)
  public void testGetTableDoesNotExist() {
    URI uri = resolveUri(TABLE_API + "non-existent-table");
    rt.getForObject(uri, TableResource.class);
  }

  @Test
  public void testCreateTable() throws Throwable  {
    TableResource resource = createTable();
    assertEquals(T.tableId, resource.getTableId());
  }

  @Test
  public void testCreateTableTwice() throws Throwable  {
    createTable();
    createTable();
  }

  @Test(expected = HttpClientErrorException.class)
  public void testCreateTableAlreadyExists() throws Throwable  {
    createTable();
    createAltTable();
  }

  @Test
  public void testGetTable() throws Throwable  {
    TableResource expected = createTable();
    String uri = expected.getSelfUri();
    TableResource actual = rt.getForObject(uri, TableResource.class);
    assertEquals(expected, actual);
  }

}
