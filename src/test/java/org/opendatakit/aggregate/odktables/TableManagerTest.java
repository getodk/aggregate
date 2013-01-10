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
import org.opendatakit.aggregate.odktables.entity.Scope;
import org.opendatakit.aggregate.odktables.entity.TableEntry;
import org.opendatakit.aggregate.odktables.entity.TableRole;
import org.opendatakit.aggregate.odktables.exception.TableAlreadyExistsException;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.TestContextFactory;

import com.google.common.collect.Lists;

public class TableManagerTest {

  private CallingContext cc;
  private TableManager tm;
  private String tableId;
  private String tableName;
  private String tableId2;
  private List<Column> columns;
  private String tableProperties;

  @Before
  public void setUp() throws Exception {
    this.cc = TestContextFactory.getCallingContext();
    this.tm = new TableManager(cc);
    this.tableId = T.tableId;
    this.tableName = T.tableName;
    this.tableId2 = T.tableId + "2";
    this.columns = T.columns;
    this.tableProperties = T.tableMetadata;
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
    TableEntry entry = tm.createTable(tableId, tableName, columns, tableProperties);
    assertEquals(tableId, entry.getTableId());
    assertNotNull(entry.getDataEtag());
  }

  @Test(expected = TableAlreadyExistsException.class)
  public void testCreateTableAlreadyExists() throws ODKDatastoreException,
      TableAlreadyExistsException {
    tm.createTable(tableId, tableName, columns, tableProperties);
    tm.createTable(tableId, tableName, columns, tableProperties);
  }

  @Test(expected = NullPointerException.class)
  public void testCreateTableNullTableId() throws ODKEntityPersistException, ODKDatastoreException,
      TableAlreadyExistsException {
    tm.createTable(null, tableName, columns, tableProperties);
  }

  @Test(expected = NullPointerException.class)
  public void testCreateTableNullTableName() throws ODKEntityPersistException,
      ODKDatastoreException, TableAlreadyExistsException {
    tm.createTable(tableId, null, columns, tableProperties);
  }

  @Test(expected = NullPointerException.class)
  public void testCreateTableNullColumns() throws ODKEntityPersistException, ODKDatastoreException,
      TableAlreadyExistsException {
    tm.createTable(tableId, tableName, null, tableProperties);
  }

  @Test
  public void testGetTable() throws ODKDatastoreException, TableAlreadyExistsException {
    TableEntry expected = tm.createTable(tableId, tableName, columns, tableProperties);
    TableEntry actual = tm.getTableNullSafe(tableId);
    assertEquals(expected, actual);
  }

  @Test(expected = ODKEntityNotFoundException.class)
  public void testGetTableDoesNotExist() throws ODKEntityNotFoundException, ODKDatastoreException {
    tm.getTableNullSafe(tableId);
  }

  @Test(expected = NullPointerException.class)
  public void testGetTableNullTableId() throws ODKEntityNotFoundException, ODKDatastoreException {
    tm.getTableNullSafe(null);
  }

  @Test
  public void testGetTables() throws ODKEntityPersistException, ODKDatastoreException,
      ODKTaskLockException, TableAlreadyExistsException {
    List<TableEntry> expected = new ArrayList<TableEntry>();

    TableEntry one = tm.createTable(tableId2, tableName, columns, tableProperties);
    TableEntry two = tm.createTable(tableId, tableName, columns, tableProperties);

    expected.add(one);
    expected.add(two);

    List<TableEntry> actual = tm.getTables();
    assertEquals(2, actual.size());

    Util.assertCollectionSameElements(expected, actual);
  }

  @Test
  public void testGetTablesByScopes() throws ODKEntityNotFoundException, ODKDatastoreException,
      TableAlreadyExistsException {
    List<TableEntry> expected = new ArrayList<TableEntry>();

    TableEntry one = tm.createTable(tableId2, tableName, columns, tableProperties);
    tm.createTable(tableId, tableName, columns, tableProperties);

    TableAclManager am = new TableAclManager(one.getTableId(), cc);
    Scope scope = new Scope(Scope.Type.DEFAULT, null);
    am.setAcl(scope, TableRole.READER);

    expected.add(one);

    List<TableEntry> actual = tm.getTables(Lists.newArrayList(scope));

    Util.assertCollectionSameElements(expected, actual);
  }

  @Test(expected = ODKEntityNotFoundException.class)
  public void testDeleteTable() throws ODKDatastoreException, ODKTaskLockException,
      TableAlreadyExistsException {
    tm.createTable(tableId, tableName, columns, tableProperties);
    tm.deleteTable(tableId);
    tm.getTableNullSafe(tableId);
  }

  @Test(expected = ODKEntityNotFoundException.class)
  public void testDeleteTableDoesNotExist() throws ODKDatastoreException, ODKTaskLockException {
    tm.deleteTable(tableId);
  }

  @Test(expected = NullPointerException.class)
  public void testDeleteTableNullTableId() throws ODKDatastoreException, ODKTaskLockException {
    tm.deleteTable(null);
  }

}
