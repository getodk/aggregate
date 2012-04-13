package org.opendatakit.aggregate.odktables;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opendatakit.aggregate.odktables.entity.Column;
import org.opendatakit.aggregate.odktables.entity.TableEntry;
import org.opendatakit.aggregate.odktables.exception.TableAlreadyExistsException;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.TestContextFactory;

public class TableManagerTest {

  private TableManager tm;
  private String tableId;
  private String tableId2;
  private List<Column> columns;

  @Before
  public void setUp() throws Exception {
    CallingContext cc = TestContextFactory.getCallingContext();
    tm = new TableManager(cc);
    this.tableId = T.tableId;
    this.columns = T.columns;
    this.tableId2 = T.tableId + "2";
  }

  @After
  public void tearDown() throws Exception {
    try {
      tm.deleteTable(tableId);
    } catch (ODKEntityNotFoundException e) {
      // ignore
    }
    try {
      tm.deleteTable(tableId2);
    } catch (ODKEntityNotFoundException e) {
      // ignore
    }
  }

  @Test
  public void testGetTablesEmpty() throws ODKDatastoreException {
    List<TableEntry> entries = tm.getTables();
    assertTrue(entries.isEmpty());
  }

  @Test
  public void testCreateTable() throws ODKDatastoreException, TableAlreadyExistsException {
    TableEntry entry = tm.createTable(tableId, columns);
    assertEquals(tableId, entry.getTableId());
    assertNotNull(entry.getDataEtag());
  }

  @Test(expected = TableAlreadyExistsException.class)
  public void testCreateTableAlreadyExists() throws ODKDatastoreException,
      TableAlreadyExistsException {
    tm.createTable(tableId, columns);
    tm.createTable(tableId, columns);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateTableNullTableId() throws ODKEntityPersistException, ODKDatastoreException,
      TableAlreadyExistsException {
    tm.createTable(null, columns);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateTableNullColumns() throws ODKEntityPersistException, ODKDatastoreException,
      TableAlreadyExistsException {
    tm.createTable(tableId, null);
  }

  @Test
  public void testGetTable() throws ODKDatastoreException, TableAlreadyExistsException {
    TableEntry expected = tm.createTable(tableId, columns);
    TableEntry actual = tm.getTableNullSafe(tableId);
    assertEquals(expected, actual);
  }

  @Test(expected = ODKEntityNotFoundException.class)
  public void testGetTableDoesNotExist() throws ODKEntityNotFoundException, ODKDatastoreException {
    tm.getTableNullSafe(tableId);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetTableNullTableId() throws ODKEntityNotFoundException, ODKDatastoreException {
    tm.getTableNullSafe(null);
  }

  @Test
  public void testGetTables() throws ODKEntityPersistException, ODKDatastoreException,
      ODKTaskLockException, TableAlreadyExistsException {
    List<TableEntry> expected = new ArrayList<TableEntry>();

    TableEntry one = tm.createTable(tableId2, columns);
    TableEntry two = tm.createTable(tableId, columns);

    expected.add(one);
    expected.add(two);

    List<TableEntry> actual = tm.getTables();
    assertEquals(2, actual.size());

    Util.assertCollectionSameElements(expected, actual);
  }

  @Test(expected = ODKEntityNotFoundException.class)
  public void testDeleteTable() throws ODKDatastoreException, ODKTaskLockException,
      TableAlreadyExistsException {
    tm.createTable(tableId, columns);
    tm.deleteTable(tableId);
    tm.getTableNullSafe(tableId);
  }

  @Test(expected = ODKEntityNotFoundException.class)
  public void testDeleteTableDoesNotExist() throws ODKDatastoreException, ODKTaskLockException {
    tm.deleteTable(tableId);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testDeleteTableNullTableId() throws ODKDatastoreException, ODKTaskLockException {
    tm.deleteTable(null);
  }

}
