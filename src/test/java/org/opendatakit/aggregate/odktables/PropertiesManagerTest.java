package org.opendatakit.aggregate.odktables;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.TestContextFactory;

public class PropertiesManagerTest {
  private CallingContext cc;
  private String tableId;
  private String tableName;
  private String tableMetadata;
  private TableManager tm;
  private PropertiesManager pm;

  @Before
  public void setUp() throws Exception {
    this.cc = TestContextFactory.getCallingContext();

    this.tableId = T.tableId;
    this.tableName = T.tableName;
    this.tableMetadata = T.tableMetadata;
    this.tm = new TableManager(cc);

    tm.createTable(tableId, tableName, T.columns, tableMetadata);

    this.pm = new PropertiesManager(tableId, cc);
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
  public void testGetTableName() {
    assertEquals(tableName, pm.getTableName());
  }

  @Test
  public void testGetTableMetadata() {
    assertEquals(tableMetadata, pm.getTableMetadata());
  }

  @Test
  public void testSetTableName() throws ODKTaskLockException, ODKDatastoreException {
    tableName = "a new name";
    pm.setTableName(tableName);
    assertEquals(tableName, pm.getTableName());
  }

  @Test
  public void testSetTableNameChangesPropertiesModNum() throws ODKDatastoreException,
      ODKTaskLockException {
    String startingPropertiesEtag = tm.getTable(tableId).getPropertiesEtag();
    tableName = "a new name";
    pm.setTableName(tableName);
    String endingPropertiesEtag = tm.getTable(tableId).getPropertiesEtag();
    assertFalse(startingPropertiesEtag.equals(endingPropertiesEtag));
  }

  @Test
  public void testSetTableMetadata() throws ODKTaskLockException, ODKDatastoreException {
    tableMetadata = "some metadata";
    pm.setTableMetadata(tableMetadata);
    assertEquals(tableMetadata, pm.getTableMetadata());
  }

  @Test
  public void testSetTableMetadataChangesPropertiesModNum() throws ODKTaskLockException,
      ODKDatastoreException {
    String startingPropertiesEtag = tm.getTable(tableId).getPropertiesEtag();
    tableMetadata = "some metadata";
    pm.setTableMetadata(tableMetadata);
    String endingPropertiesEtag = tm.getTable(tableId).getPropertiesEtag();
    assertFalse(startingPropertiesEtag.equals(endingPropertiesEtag));
  }

}
