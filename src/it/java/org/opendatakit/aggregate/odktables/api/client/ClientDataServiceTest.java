package org.opendatakit.aggregate.odktables.api.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opendatakit.aggregate.odktables.entity.Row;
import org.opendatakit.aggregate.odktables.entity.api.RowResource;

import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.UniformInterfaceException;

public class ClientDataServiceTest {

  private ClientTableServiceTest ctst;
  private String uri;
  private ClientDataService cds;
  private Row row;

  @Before
  public void setUp() throws Exception {
    this.ctst = new ClientTableServiceTest();

    ctst.setUp();
    ClientTableService cts = ctst.getCts();
    cts.createTable(T.tableId, T.columns);

    this.uri = Util.buildUri(ctst.getUri(), ClientTableService.API_PATH, T.tableId,
        ClientDataService.API_PATH);
    this.cds = ctst.getCts().getData(T.tableId);
    this.row = T.rows.get(0);
  }

  @After
  public void tearDown() throws Exception {
    try {
      cds.deleteRow(row.getRowId());
    } catch (UniformInterfaceException e) {
      Util.checkUniformInterfaceException(e, Status.NOT_FOUND);
    } finally {
      try {
        ctst.getCts().deleteTable(T.tableId);
      } catch (UniformInterfaceException e) {
        Util.checkUniformInterfaceException(e, Status.NOT_FOUND);
      }
    }
  }

  @Test
  public void testGetRowsEmpty() {
    List<RowResource> resources = cds.getRows();
    assert (resources.isEmpty());
  }

  @Test(expected = UniformInterfaceException.class)
  public void testGetRowDoesNotExist() {
    cds.getRow("doesn't exist");
  }

  @Test
  public void testCreateRow() {
    String selfUri = Util.buildUri(this.uri, row.getRowId());
    RowResource resource = cds.createOrUpdateRow(row.getRowId(), row);
    assertEquals(row.getRowId(), resource.getRowId());
    assertEquals(selfUri, resource.getSelfUri());
  }

  @Test
  public void testUpdateRow() {
    RowResource resource = cds.createOrUpdateRow(row.getRowId(), row);
    row.setRowEtag(resource.getRowEtag());
    row.getValues().put(T.Columns.age, "999");
    RowResource update = cds.createOrUpdateRow(row.getRowId(), row);
    assertEquals(resource.getRowId(), update.getRowId());
    assertFalse(resource.getRowEtag().equals(update.getRowEtag()));
    assertFalse(resource.getValues().equals(update.getValues()));
  }

  @Test
  public void testGetRows() {
    RowResource expected = cds.createOrUpdateRow(row.getRowId(), row);
    List<RowResource> resources = cds.getRows();
    assertEquals(1, resources.size());
    RowResource actual = resources.get(0);
    assertEquals(expected, actual);
  }

  @Test
  public void testGetRow() {
    RowResource expected = cds.createOrUpdateRow(row.getRowId(), row);
    RowResource actual = cds.getRow(row.getRowId());
    assertEquals(expected, actual);
  }

  @Test
  public void testDeleteRow() {
    RowResource resource = cds.createOrUpdateRow(row.getRowId(), row);
    cds.deleteRow(row.getRowId());
    RowResource deleted = cds.getRow(row.getRowId());
    assertEquals(resource.getRowId(), deleted.getRowId());
    assertFalse(resource.getRowEtag().equals(deleted.getRowEtag()));
    assertTrue(deleted.isDeleted());
  }

}
