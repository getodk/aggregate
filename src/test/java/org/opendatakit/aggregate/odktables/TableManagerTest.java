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
import org.opendatakit.aggregate.odktables.entity.Column.ColumnType;
import org.opendatakit.aggregate.odktables.entity.TableEntry;
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
    this.tableId = "people";
    this.columns = new ArrayList<Column>();
    this.columns.add(new Column("name", ColumnType.STRING));
    this.columns.add(new Column("age", ColumnType.INTEGER));
    this.columns.add(new Column("weight", ColumnType.DECIMAL));
    this.tableId2 = "people2";
  }

  @After
  public void tearDown() throws Exception {
    try {
      tm.deleteTable(tableId);
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
  public void testCreateTable() throws ODKDatastoreException {
    TableEntry entry = tm.createTable(tableId, columns);
    assertEquals(tableId, entry.getTableId());
    assertNotNull(entry.getDataEtag());
  }

  @Test(expected = ODKEntityPersistException.class)
  public void testCreateTableAlreadyExists() throws ODKDatastoreException {
    tm.createTable(tableId, columns);
    tm.createTable(tableId, columns);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateTableNullTableId() throws ODKEntityPersistException, ODKDatastoreException {
    tm.createTable(null, columns);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateTableNullColumns() throws ODKEntityPersistException, ODKDatastoreException {
    tm.createTable(tableId, null);
  }

  @Test
  public void testGetTable() throws ODKDatastoreException {
    TableEntry expected = tm.createTable(tableId, columns);
    TableEntry actual = tm.getTable(tableId);
    assertEquals(expected, actual);
  }

  @Test(expected = ODKEntityNotFoundException.class)
  public void testGetTableDoesNotExist() throws ODKEntityNotFoundException, ODKDatastoreException {
    tm.getTable(tableId);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetTableNullTableId() throws ODKEntityNotFoundException, ODKDatastoreException {
    tm.getTable(null);
  }

  @Test
  public void testGetTables() throws ODKEntityPersistException, ODKDatastoreException,
      ODKTaskLockException {
    tm.createTable(tableId, columns);
    tm.createTable(tableId2, columns);
    List<TableEntry> tables = tm.getTables();
    assertEquals(2, tables.size());
    TableEntry one = tables.get(0);
    assertEquals(tableId, one.getTableId());
    assertNotNull(one.getDataEtag());
    TableEntry two = tables.get(1);
    assertEquals(tableId2, two.getTableId());
    assertNotNull(two.getDataEtag());
  }

  @Test(expected = ODKEntityNotFoundException.class)
  public void testDeleteTable() throws ODKDatastoreException, ODKTaskLockException {
    tm.createTable(tableId, columns);
    tm.deleteTable(tableId);
    tm.getTable(tableId);
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
