/*
 * Copyright (C) 2013 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.opendatakit.aggregate.odktables;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opendatakit.aggregate.odktables.exception.EtagMismatchException;
import org.opendatakit.aggregate.odktables.rest.entity.TableProperties;
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

    tm.createTable(tableId, T.tableKey, T.dbTableName,
        T.tableType, T.tableIdAccessControls, T.columns,
        T.kvsEntries);

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
  public void testGetTableProperties() throws ODKDatastoreException {
    TableProperties expected = new TableProperties(T.propertiesEtag,
        T.tableKey, T.kvsEntries);
    TableProperties actual = pm.getProperties();
    assertEquals(expected.getTableKey(), actual.getTableKey());
    // not sure this will work...
    assertEquals(expected.getKeyValueStoreEntries(),
        actual.getKeyValueStoreEntries());
  }

  @Test
  public void testSetTableName() throws ODKTaskLockException, ODKDatastoreException,
      EtagMismatchException {
    TableProperties expected = pm.getProperties();
    expected.setTableName("a new name");

    doTestSetProperties(expected);
  }

  @Test
  public void testSetTableMetadata() throws ODKTaskLockException, ODKDatastoreException,
      EtagMismatchException {
    TableProperties expected = pm.getProperties();
    expected.setKeyValueStoreEntries(T.kvsEntries);

    doTestSetProperties(expected);
  }

  private void doTestSetProperties(TableProperties expected)
      throws EtagMismatchException, ODKTaskLockException,
      ODKDatastoreException {
    pm.setProperties(expected);

    TableProperties actual = pm.getProperties();

    assertEquals(expected.getTableKey(), actual.getTableKey());
    assertEquals(expected.getKeyValueStoreEntries(),
        actual.getKeyValueStoreEntries());
  }

  @Test
  public void testSetTableNameChangesPropertiesModNum() throws ODKDatastoreException,
      ODKTaskLockException, EtagMismatchException {
    TableProperties properties = pm.getProperties();
    properties.setTableName("a new table name");

    doTestSetPropertiesChangesModNum(properties);
  }

  @Test
  public void testSetTableMetadataChangesPropertiesModNum() throws ODKTaskLockException,
      ODKDatastoreException, EtagMismatchException {
    TableProperties properties = pm.getProperties();
    properties.setKeyValueStoreEntries(T.kvsEntries);

    doTestSetPropertiesChangesModNum(properties);
  }

  private void doTestSetPropertiesChangesModNum(TableProperties properties)
      throws ODKDatastoreException, EtagMismatchException, ODKTaskLockException {
    String startingPropertiesEtag = tm.getTable(tableId).getPropertiesEtag();
    String startingPropertiesEtagTwo = properties.getPropertiesEtag();
    assertEquals(startingPropertiesEtag, startingPropertiesEtagTwo);

    properties = pm.setProperties(properties);

    String endingPropertiesEtag = tm.getTable(tableId).getPropertiesEtag();
    String endingPropertiesEtagTwo = properties.getPropertiesEtag();
    assertEquals(endingPropertiesEtag, endingPropertiesEtagTwo);

    assertFalse(startingPropertiesEtag.equals(endingPropertiesEtag));
    assertFalse(startingPropertiesEtagTwo.equals(endingPropertiesEtagTwo));
  }

  @Test(expected = EtagMismatchException.class)
  public void testCantChangePropertiesWithOldEtag() throws ODKDatastoreException,
      EtagMismatchException, ODKTaskLockException {
    TableProperties properties = pm.getProperties();
    properties.setTableName("new name");
    pm.setProperties(properties);
    properties.setTableName("new name 2");
    pm.setProperties(properties);
  }
}
