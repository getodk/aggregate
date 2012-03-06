package org.opendatakit.aggregate.odktables.api.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opendatakit.aggregate.odktables.entity.Column;
import org.opendatakit.aggregate.odktables.entity.Column.ColumnType;
import org.opendatakit.aggregate.odktables.entity.api.TableResource;

import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.UniformInterfaceException;

public class ClientTableServiceTest {

  private String uri;
  private ClientTableService cts;
  private String tableId;
  private List<Column> columns;

  @Before
  public void setUp() throws Exception {
    this.uri = "http://localhost:8888";
    this.cts = new ClientTableService(uri, "");

    this.tableId = "people";
    this.columns = new ArrayList<Column>();
    this.columns.add(new Column("name", ColumnType.STRING));
    this.columns.add(new Column("age", ColumnType.INTEGER));
    this.columns.add(new Column("weight", ColumnType.DECIMAL));
  }

  @After
  public void tearDown() throws Exception {
    try {
      cts.deleteTable(tableId);
    } catch (UniformInterfaceException e) {
      Util.checkUniformInterfaceException(e, Status.NOT_FOUND);
    }
  }

  public String getUri() {
    return uri;
  }

  public ClientTableService getCts() {
    return cts;
  }

  @Test
  public void testGetTablesEmpty() {
    List<TableResource> tables = cts.getTables();
    assertEquals(0, tables.size());
  }

  @Test
  public void testCreateTable() {
    String selfUri = Util.buildUri(this.uri, ClientTableService.API_PATH, tableId);
    TableResource table = cts.createTable(tableId, columns);
    assertEquals(tableId, table.getTableId());
    assertEquals(selfUri, table.getSelfUri());
  }

  @Test
  public void testGetTables() {
    TableResource table = cts.createTable(tableId, columns);
    List<TableResource> tables = cts.getTables();
    assertEquals(1, tables.size());
    assertEquals(table, tables.get(0));
  }

  @Test
  public void testCreateTableAlreadyExists() {
    cts.createTable(tableId, columns);
    cts.createTable(tableId, columns);
  }

  @Test
  public void testGetTable() {
    TableResource expected = cts.createTable(tableId, columns);
    TableResource actual = cts.getTable(tableId);
    assertEquals(expected, actual);
  }

  @Test(expected = UniformInterfaceException.class)
  public void testDeleteTable() {
    cts.createTable(tableId, columns);
    cts.deleteTable(tableId);
    cts.getTable(tableId);
  }

  @Test
  public void testGetData() {
    ClientDataService cds = cts.getData(tableId);
    assertNotNull(cds);
  }
}
