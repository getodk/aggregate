package org.opendatakit.aggregate.odktables.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opendatakit.aggregate.odktables.rest.SavepointTypeManipulator;
import org.opendatakit.aggregate.odktables.rest.entity.Row;
import org.opendatakit.aggregate.odktables.rest.entity.RowFilterScope;
import org.opendatakit.aggregate.odktables.rest.entity.RowList;
import org.opendatakit.aggregate.odktables.rest.entity.RowOutcomeList;
import org.opendatakit.aggregate.odktables.rest.entity.RowResourceList;
import org.opendatakit.aggregate.odktables.rest.entity.TableResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;


public class DataServiceTest extends AbstractServiceTest {

  protected URI resourceUri;
  
  @Before
  public void setUp() throws Exception, Throwable {
    super.abstractServiceSetUp();
    super.createTable();
    TableResource resource = rt.getForObject(resolveUri(TABLE_API + T.tableId), TableResource.class);
    resourceUri = URI.create(resource.getDataUri());
  }

  @Test
  public void testGetRowsNoRows() {
    RowResourceList rows = rt.getForObject(resourceUri, RowResourceList.class);
    assertTrue(rows.getRows().isEmpty());
  }

  @Test
  public void testInsertRow() throws Throwable {
    String rowId = T.Data.DYLAN.getId();
    String uri = Util.buildUri(resourceUri.toASCIIString());

    Row expected = Row.forInsert(rowId, T.form_id_1, T.locale_1, SavepointTypeManipulator.complete(),
        T.savepoint_timestamp_1, T.savepoint_creator_1, RowFilterScope.EMPTY_ROW_FILTER, T.Data.DYLAN.getValues());

    RowList list = new RowList();
    ArrayList<Row> rows = new ArrayList<Row>();
    rows.add(expected);
    list.setRows(rows);
    
    HttpEntity<RowList> entity = super.entity(list);
    ResponseEntity<RowOutcomeList> resp;
    try {
      resp = rt.exchange(uri, HttpMethod.PUT, entity, RowOutcomeList.class);
    } catch (Throwable t) {
      t.printStackTrace();
      throw t;
    }
    RowOutcomeList outcomes = resp.getBody();
    Row actual = outcomes.getRows().get(0);
    assertEquals(expected.getRowId(), actual.getRowId());
    assertNotNull(actual.getRowETag());
  }

}
