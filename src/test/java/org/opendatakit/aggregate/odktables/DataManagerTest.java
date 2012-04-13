package org.opendatakit.aggregate.odktables;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opendatakit.aggregate.odktables.entity.Row;
import org.opendatakit.aggregate.odktables.entity.TableEntry;
import org.opendatakit.aggregate.odktables.exception.RowEtagMismatchException;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.TestContextFactory;

public class DataManagerTest {

  private CallingContext cc;
  private String tableId;
  private String tableName;
  private String tableProperties;
  private TableManager tm;
  private DataManager dm;
  private List<Row> rows;

  @Before
  public void setUp() throws Exception {
    this.cc = TestContextFactory.getCallingContext();

    this.tableId = T.tableId;
    this.tableName = T.tableName;
    this.tableProperties = T.tableMetadata;
    this.tm = new TableManager(cc);

    tm.createTable(tableId, tableName, T.columns, tableProperties);

    this.dm = new DataManager(tableId, cc);
    this.rows = T.rows;
  }

  @After
  public void tearDown() throws Exception {
    try {
      tm.deleteTable(tableId);
    } catch (ODKEntityNotFoundException e) {
      // ignore
    }
  }

  @Test
  public void testGetRowsEmpty() throws ODKDatastoreException {
    List<Row> rows = dm.getRows();
    assertTrue(rows.isEmpty());
  }

  @Test
  public void testInsertRows() throws ODKEntityPersistException, ODKDatastoreException,
      ODKTaskLockException {
    List<Row> actualRows = dm.insertRows(rows);
    assertEquals(2, actualRows.size());
    for (int i = 0; i < rows.size(); i++) {
      Row expected = rows.get(i);
      Row actual = actualRows.get(i);
      expected.setRowEtag(actual.getRowEtag());
      expected.setGroupOrUserId(actual.getGroupOrUserId());
    }
    assertEquals(rows.get(0), actualRows.get(0));
    assertEquals(rows.get(1), actualRows.get(1));
  }

  @Test(expected = ODKEntityPersistException.class)
  public void testInsertRowsAlreadyExist() throws ODKEntityPersistException, ODKDatastoreException,
      ODKTaskLockException {
    dm.insertRows(rows);
    dm.insertRows(rows);
  }

  @Test
  public void testGetRows() throws ODKDatastoreException, ODKTaskLockException {
    dm.insertRows(rows);
    List<Row> actualRows = dm.getRows();
    for (int i = 0; i < rows.size(); i++) {
      Row expected = rows.get(i);
      Row actual = actualRows.get(i);
      assertEquals(expected.getRowId(), actual.getRowId());
      assertEquals(expected.getGroupOrUserId(), actual.getGroupOrUserId());
      assertEquals(expected.getValues(), actual.getValues());
    }
  }

  @Test
  public void testGetRow() throws ODKDatastoreException, ODKTaskLockException {
    Row expected = Row.forInsert(T.Data.DYLAN.getId(), null, T.Data.DYLAN.getValues());
    expected = dm.insertRow(expected);
    Row actual = dm.getRow(T.Data.DYLAN.getId());
    assertEquals(expected, actual);
  }

  @Test
  public void testGetRowDoeNotExist() throws ODKDatastoreException {
    Row row = dm.getRow(T.Data.DYLAN.getId());
    assertNull(row);
  }

  @Test
  public void testGetRowNullSafe() throws ODKEntityPersistException, ODKDatastoreException,
      ODKTaskLockException {
    Row expected = Row.forInsert(T.Data.DYLAN.getId(), null, T.Data.DYLAN.getValues());
    expected = dm.insertRow(expected);
    Row actual = dm.getRowNullSafe(T.Data.DYLAN.getId());
    assertEquals(expected, actual);
  }

  @Test(expected = ODKEntityNotFoundException.class)
  public void testGetRowNullSafeDoesNotExist() throws ODKEntityNotFoundException,
      ODKDatastoreException {
    dm.getRowNullSafe(T.Data.DYLAN.getId());
  }

  @Test
  public void testUpdateRow() throws ODKEntityPersistException, ODKDatastoreException,
      ODKTaskLockException, RowEtagMismatchException {
    rows = dm.insertRows(rows);
    Row expected = rows.get(0);
    expected.getValues().put(T.Columns.age, "24");
    Row actual = dm.updateRow(expected);
    assertFalse(expected.getRowEtag().equals(actual.getRowEtag()));
    expected.setRowEtag(actual.getRowEtag());
    assertEquals(expected, actual);
  }

  @Test(expected = RowEtagMismatchException.class)
  public void testUpdateRowVersionMismatch() throws ODKEntityPersistException,
      ODKDatastoreException, ODKTaskLockException, RowEtagMismatchException {
    rows = dm.insertRows(rows);
    Row row = rows.get(0);
    row.setRowEtag(UUID.randomUUID().toString());
    dm.updateRow(row);
  }

  @Test(expected = ODKEntityNotFoundException.class)
  public void testUpdateRowDoesNotExist() throws ODKEntityNotFoundException, ODKDatastoreException,
      ODKTaskLockException, RowEtagMismatchException {
    Row row = rows.get(0);
    row.setRowEtag(UUID.randomUUID().toString());
    dm.updateRow(row);
  }

  @Test
  public void testDeleteRows() throws ODKEntityPersistException, ODKDatastoreException,
      ODKTaskLockException {
    dm.insertRows(rows);
    dm.deleteRows(Util.list(T.Data.DYLAN.getId(), T.Data.JOHN.getId()));
    List<Row> rows = dm.getRows();
    assertTrue(rows.isEmpty());
  }

  @Test
  public void testGetRowsSince() throws ODKEntityPersistException, ODKDatastoreException,
      ODKTaskLockException, RowEtagMismatchException {
    TableEntry entry = tm.getTableNullSafe(tableId);
    String beginEtag = entry.getDataEtag();
    rows = dm.insertRows(rows);

    Map<String, Row> expected = new HashMap<String, Row>();

    for (Row row : rows) {
      row.getValues().put(T.Columns.age, "99");
    }

    rows = dm.updateRows(rows);

    for (Row row : rows) {
      expected.put(row.getRowId(), row);
    }

    Row row = rows.get(0);
    row.getValues().put(T.Columns.age, "444");

    row = dm.updateRow(row);
    expected.put(row.getRowId(), row);

    List<Row> actual = dm.getRowsSince(beginEtag);
    Util.assertCollectionSameElements(expected.values(), actual);
  }

}
