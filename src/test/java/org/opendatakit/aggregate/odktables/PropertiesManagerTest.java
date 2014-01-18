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
import org.junit.Ignore;
import org.junit.Test;
import org.opendatakit.aggregate.odktables.exception.ETagMismatchException;
import org.opendatakit.aggregate.odktables.rest.entity.TableEntry;
import org.opendatakit.aggregate.odktables.rest.entity.TableProperties;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.TestContextFactory;

public class PropertiesManagerTest {
  private CallingContext cc;
  private OdkTablesUserInfoTable userInfo;
  private String ePropertiesTag;
  private String tableId;
  private TableManager tm;
  private PropertiesManager pm;

  @Before
  public void setUp() throws Exception {
    this.cc = TestContextFactory.getCallingContext();

    userInfo = cc.getDatastore().createEntityUsingRelation(OdkTablesUserInfoTable.assertRelation(cc), cc.getCurrentUser());
    userInfo.setOdkTablesUserId("myId");
    userInfo.setUriUser(cc.getCurrentUser().getUriUser());

    this.tableId = T.tableId;
    this.tm = new TableManager(userInfo, cc);

    TableEntry te = tm.createTable(tableId,
        T.columns, T.kvsEntries);
    this.ePropertiesTag = te.getPropertiesETag();

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
    TableProperties expected = new TableProperties(this.ePropertiesTag,
        T.tableId, T.kvsEntries);
    TableProperties actual = pm.getProperties();
    assertEquals(expected.getTableId(), actual.getTableId());
    Util.assertCollectionSameElements(expected.getKeyValueStoreEntries(),
        actual.getKeyValueStoreEntries());
  }

  // TODO: fix this when tableId and tableKey get sorted out...
  @Ignore
  public void testSetTableName() throws ODKTaskLockException, ODKDatastoreException,
      ETagMismatchException {
    TableProperties expected = pm.getProperties();
    expected.setTableId("a new name"); // don't see how this would work...

    doTestSetProperties(expected);
  }

  @Test
  public void testSetTableMetadata() throws ODKTaskLockException, ODKDatastoreException,
      ETagMismatchException {
    TableProperties expected = pm.getProperties();
    expected.setKeyValueStoreEntries(T.kvsEntries);

    doTestSetProperties(expected);
  }

  private void doTestSetProperties(TableProperties expected)
      throws ETagMismatchException, ODKTaskLockException,
      ODKDatastoreException {
    pm.setProperties(expected);

    TableProperties actual = pm.getProperties();

    assertEquals(expected.getTableId(), actual.getTableId());
    Util.assertCollectionSameElements(expected.getKeyValueStoreEntries(),
        actual.getKeyValueStoreEntries());
  }

  @Test
  public void testSetTableNameChangesPropertiesModNum() throws ODKDatastoreException,
      ODKTaskLockException, ETagMismatchException {
    TableProperties properties = pm.getProperties();
    properties.setTableId("a new table name"); // don't see how this would work

    doTestSetPropertiesChangesModNum(properties);
  }

  @Test
  public void testSetTableMetadataChangesPropertiesModNum() throws ODKTaskLockException,
      ODKDatastoreException, ETagMismatchException {
    TableProperties properties = pm.getProperties();
    properties.setKeyValueStoreEntries(T.kvsEntries);

    doTestSetPropertiesChangesModNum(properties);
  }

  private void doTestSetPropertiesChangesModNum(TableProperties properties)
      throws ODKDatastoreException, ETagMismatchException, ODKTaskLockException {
    String startingPropertiesETag = tm.getTable(tableId).getPropertiesETag();
    String startingPropertiesETagTwo = properties.getPropertiesETag();
    assertEquals(startingPropertiesETag, startingPropertiesETagTwo);

    properties = pm.setProperties(properties);

    String endingPropertiesETag = tm.getTable(tableId).getPropertiesETag();
    String endingPropertiesETagTwo = properties.getPropertiesETag();
    assertEquals(endingPropertiesETag, endingPropertiesETagTwo);

    assertFalse(startingPropertiesETag.equals(endingPropertiesETag));
    assertFalse(startingPropertiesETagTwo.equals(endingPropertiesETagTwo));
  }

  @Test(expected = ETagMismatchException.class)
  public void testCantChangePropertiesWithOldETag() throws ODKDatastoreException,
      ETagMismatchException, ODKTaskLockException {
    TableProperties properties = pm.getProperties();
    properties.setTableId("new name");
    pm.setProperties(properties);
    properties.setTableId("new name 2");
    pm.setProperties(properties);
  }
}
