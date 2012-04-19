package org.opendatakit.aggregate.odktables.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.List;

import org.junit.Test;
import org.opendatakit.aggregate.odktables.entity.Row;
import org.opendatakit.aggregate.odktables.entity.api.RowResource;
import org.opendatakit.aggregate.odktables.entity.api.TableResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

public class DataServiceTest extends AbstractServiceTest {

  @Override
  public void setUp() throws Exception {
    super.setUp();
    super.createTable();
    TableResource resource = rt.getForObject(baseUri.resolve(T.tableId), TableResource.class);
    baseUri = URI.create(resource.getDataUri());
  }
  
  @Test
  public void testGetRowsNoRows() {
    List<?> rows = rt.getForObject(baseUri, List.class);
    assertTrue(rows.isEmpty());
  }

  @Test
  public void testInsertRow() {
    String rowId = T.Data.DYLAN.getId();
    String uri = Util.buildUri(baseUri.toASCIIString(), rowId);

    Row expected = Row.forInsert(rowId, null, T.Data.DYLAN.getValues());
    HttpEntity<Row> entity = super.entity(expected);

    ResponseEntity<RowResource> resp = rt.exchange(uri, HttpMethod.PUT, entity, RowResource.class);
    Row actual = resp.getBody();
    assertEquals(expected.getRowId(), actual.getRowId());
    assertNotNull(actual.getRowEtag());
  }

}
