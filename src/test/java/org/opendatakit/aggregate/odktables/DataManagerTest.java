package org.opendatakit.aggregate.odktables;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opendatakit.aggregate.odktables.entity.Row;
import org.opendatakit.aggregate.odktables.entity.Scope;
import org.opendatakit.aggregate.odktables.entity.Scope.Type;
import org.opendatakit.aggregate.odktables.entity.TableEntry;
import org.opendatakit.aggregate.odktables.exception.BadColumnNameException;
import org.opendatakit.aggregate.odktables.exception.EtagMismatchException;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.TestContextFactory;

import com.google.common.collect.Maps;

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
      ODKTaskLockException, BadColumnNameException {
    List<Row> actualRows = dm.insertRows(rows);
    assertEquals(2, actualRows.size());
    for (int i = 0; i < rows.size(); i++) {
      Row expected = rows.get(i);
      Row actual = actualRows.get(i);
      expected.setRowEtag(actual.getRowEtag());
      expected.setCreateUser(actual.getCreateUser());
      expected.setLastUpdateUser(actual.getLastUpdateUser());
    }
    assertEquals(rows.get(0), actualRows.get(0));
    assertEquals(rows.get(1), actualRows.get(1));
  }

  @Test(expected = ODKEntityPersistException.class)
  public void testInsertRowsAlreadyExist() throws ODKEntityPersistException, ODKDatastoreException,
      ODKTaskLockException, BadColumnNameException {
    dm.insertRows(rows);
    dm.insertRows(rows);
  }

  @Test
  public void testGetRows() throws ODKDatastoreException, ODKTaskLockException,
      BadColumnNameException {
    dm.insertRows(rows);
    List<Row> actualRows = dm.getRows();
    for (int i = 0; i < rows.size(); i++) {
      Row expected = rows.get(i);
      Row actual = actualRows.get(i);
      assertEquals(expected.getRowId(), actual.getRowId());
      assertEquals(expected.getValues(), actual.getValues());
    }
  }

  @Test
  public void testGetRowsByScope() throws ODKEntityPersistException, ODKDatastoreException,
      ODKTaskLockException, EtagMismatchException, BadColumnNameException {
    List<Row> rows = setupTestRows();
    Row expected = rows.get(0);
    List<Row> actualRows = dm.getRows(expected.getFilterScope());
    assertEquals(1, actualRows.size());
    Row actual = actualRows.get(0);
    assertEquals(actual, expected);
  }

  @Test
  public void testGetRowsByScopes() throws ODKEntityPersistException, EtagMismatchException,
      BadColumnNameException, ODKDatastoreException, ODKTaskLockException {
    List<Row> rows = setupTestRows();
    Row row1 = rows.get(0);
    Row row2 = rows.get(1);
    List<Scope> scopes = new ArrayList<Scope>();
    scopes.add(row1.getFilterScope());
    scopes.add(row2.getFilterScope());
    List<Row> results = dm.getRows(scopes);
    assertEquals(2, results.size());
  }

  @Test
  public void testGetRow() throws ODKDatastoreException, ODKTaskLockException,
      BadColumnNameException {
    Row expected = Row.forInsert(T.Data.DYLAN.getId(), T.Data.DYLAN.getValues());
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
      ODKTaskLockException, BadColumnNameException {
    Row expected = Row.forInsert(T.Data.DYLAN.getId(), T.Data.DYLAN.getValues());
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
      ODKTaskLockException, EtagMismatchException, BadColumnNameException {
    rows = dm.insertRows(rows);
    Row expected = rows.get(0);
    expected.getValues().put(T.Columns.age, "24");
    Row actual = dm.updateRow(expected);
    assertFalse(expected.getRowEtag().equals(actual.getRowEtag()));
    expected.setRowEtag(actual.getRowEtag());
    assertEquals(expected, actual);
  }

  @Test(expected = EtagMismatchException.class)
  public void testUpdateRowVersionMismatch() throws ODKEntityPersistException,
      ODKDatastoreException, ODKTaskLockException, EtagMismatchException, BadColumnNameException {
    rows = dm.insertRows(rows);
    Row row = rows.get(0);
    row.setRowEtag(UUID.randomUUID().toString());
    dm.updateRow(row);
  }

  @Test(expected = ODKEntityNotFoundException.class)
  public void testUpdateRowDoesNotExist() throws ODKEntityNotFoundException, ODKDatastoreException,
      ODKTaskLockException, EtagMismatchException, BadColumnNameException {
    Row row = rows.get(0);
    row.setRowEtag(UUID.randomUUID().toString());
    dm.updateRow(row);
  }

  @Test
  public void testUpdateRowDoesNotChangeFilter() throws ODKEntityPersistException,
      ODKDatastoreException, ODKTaskLockException, EtagMismatchException, BadColumnNameException {
    Row expected = rows.get(0);
    expected.setFilterScope(new Scope(Type.USER, T.user));
    expected = dm.insertRow(expected);
    Row actual = Row.forUpdate(expected.getRowId(), expected.getRowEtag(),
        Maps.<String, String> newHashMap());
    actual = dm.updateRow(actual);
    expected.setRowEtag(actual.getRowEtag());
    assertEquals(expected.getFilterScope(), actual.getFilterScope());
  }

  @Test
  public void testUpdateRowDoesChangeFilter() throws ODKEntityNotFoundException,
      EtagMismatchException, ODKDatastoreException, ODKTaskLockException, BadColumnNameException {
    Row row = rows.get(0);
    row.setFilterScope(new Scope(Type.USER, T.user));
    row = dm.insertRow(row);
    Row actual = Row
        .forUpdate(row.getRowId(), row.getRowEtag(), Maps.<String, String> newHashMap());
    actual.setFilterScope(Scope.EMPTY_SCOPE);
    actual = dm.updateRow(actual);
    row.setRowEtag(actual.getRowEtag());
    assertEquals(Scope.EMPTY_SCOPE, actual.getFilterScope());
  }

  @Test(expected = BadColumnNameException.class)
  public void testUpdateRowBadColumnName() throws ODKEntityPersistException,
      BadColumnNameException, ODKDatastoreException, ODKTaskLockException, EtagMismatchException {
    Row row = rows.get(0);
    row = dm.insertRow(row);
    Map<String, String> values = Maps.newHashMap();
    values.put(T.Columns.name + "diff", "value");
    row = Row.forUpdate(row.getRowId(), row.getRowEtag(), values);
    dm.updateRow(row);
  }

  @Test
  public void testDeleteRows() throws ODKEntityPersistException, ODKDatastoreException,
      ODKTaskLockException, BadColumnNameException {
    dm.insertRows(rows);
    dm.deleteRows(Util.list(T.Data.DYLAN.getId(), T.Data.JOHN.getId()));
    List<Row> rows = dm.getRows();
    assertTrue(rows.isEmpty());
  }

  @Test
  public void testGetRowsSince() throws ODKEntityPersistException, ODKDatastoreException,
      ODKTaskLockException, EtagMismatchException, BadColumnNameException {
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

  @Test
  public void testGetRowsSinceByScope() throws ODKEntityPersistException, EtagMismatchException,
      BadColumnNameException, ODKDatastoreException, ODKTaskLockException {
    TableEntry entry = tm.getTableNullSafe(tableId);
    String beginEtag = entry.getDataEtag();
    List<Row> rows = setupTestRows();
    Scope scope = rows.get(0).getFilterScope();
    List<Row> expected = dm.getRows(scope);
    List<Row> actual = dm.getRowsSince(beginEtag, scope);
    Util.assertCollectionSameElements(expected, actual);
  }

  @Test
  public void testGetRowsSinceByScopes() throws ODKDatastoreException, EtagMismatchException,
      BadColumnNameException, ODKTaskLockException {
    TableEntry entry = tm.getTableNullSafe(tableId);
    String beginEtag = entry.getDataEtag();
    List<Row> rows = setupTestRows();
    Row row2 = rows.get(1);
    Row row3 = rows.get(2);
    List<Scope> scopes = new ArrayList<Scope>();
    scopes.add(row2.getFilterScope());
    scopes.add(row3.getFilterScope());
    List<Row> expected = dm.getRows(scopes);
    List<Row> actual = dm.getRowsSince(beginEtag, scopes);
    Util.assertCollectionSameElements(expected, actual);
  }

  private List<Row> setupTestRows() throws ODKEntityPersistException, ODKDatastoreException,
      ODKTaskLockException, EtagMismatchException, BadColumnNameException {
    Map<String, String> values = Maps.newHashMap();
    List<Row> rows = new ArrayList<Row>();

    Row row = Row.forInsert("1", values);
    row.setFilterScope(new Scope(Type.DEFAULT, null));
    rows.add(row);

    row = Row.forInsert("2", values);
    row.setFilterScope(new Scope(Type.USER, T.user));
    rows.add(row);

    row = Row.forInsert("3", values);
    row.setFilterScope(new Scope(Type.GROUP, T.group));
    rows.add(row);

    row = Row.forInsert("4", values);
    row.setFilterScope(Scope.EMPTY_SCOPE);
    rows.add(row);

    rows = dm.insertRows(rows);
    values.put(T.Columns.name, "some name");

    for (Row update : rows) {
      update.setValues(values);
      update.setFilterScope(null);
    }
    rows = dm.updateRows(rows);

    return rows;
  }

}
