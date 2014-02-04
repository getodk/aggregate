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
import org.opendatakit.aggregate.odktables.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.rest.entity.Scope;
import org.opendatakit.aggregate.odktables.rest.entity.TableEntry;
import org.opendatakit.aggregate.odktables.rest.entity.TableProperties;
import org.opendatakit.aggregate.odktables.rest.entity.TableRole.TablePermission;
import org.opendatakit.aggregate.odktables.security.TablesUserPermissions;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.TestContextFactory;

public class PropertiesManagerTest {
  private CallingContext cc;
  private TablesUserPermissions userPermissions;
  private String eSchemaTag;
  private String ePropertiesTag;
  private String tableId;
  private TableManager tm;
  private PropertiesManager pm;

  private class MockCurrentUserPermissions implements TablesUserPermissions {

    @Override
    public String getOdkTablesUserId() {
      return "myid";
    }

    @Override
    public String getPhoneNumber() {
      return null;
    }

    @Override
    public String getXBearerCode() {
      return null;
    }

    @Override
    public void checkPermission(String tableId, TablePermission permission)
        throws ODKDatastoreException, PermissionDeniedException {
      return;
    }

    @Override
    public boolean hasPermission(String tableId, TablePermission permission)
        throws ODKDatastoreException {
      return true;
    }

    @Override
    public boolean hasFilterScope(String tableId, TablePermission permission, String rowId, Scope filterScope) {
      return true;
    }

  }

  @Before
  public void setUp() throws Exception {
    this.cc = TestContextFactory.getCallingContext();

    userPermissions = new MockCurrentUserPermissions();

    this.tableId = T.tableId;
    this.tm = new TableManager(userPermissions, cc);

    TableEntry te = tm.createTable(tableId, T.columns);
    this.pm = new PropertiesManager( tableId, userPermissions, cc);
    TableProperties tableProperties = new TableProperties(te.getSchemaETag(), null, tableId, T.kvsEntries);
    TableProperties tpNew = pm.setProperties(tableProperties);
    this.eSchemaTag = tpNew.getSchemaETag();
    this.ePropertiesTag = tpNew.getPropertiesETag();
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
  public void testGetTableProperties() throws ODKDatastoreException, PermissionDeniedException, ODKTaskLockException, ETagMismatchException {
    TableProperties expected = new TableProperties(this.eSchemaTag, this.ePropertiesTag,
        T.tableId, T.kvsEntries);
    TableProperties actual = pm.getProperties();
    assertEquals(expected.getTableId(), actual.getTableId());
    Util.assertCollectionSameElements(expected.getKeyValueStoreEntries(),
        actual.getKeyValueStoreEntries());
  }

  // TODO: fix this when tableId and tableKey get sorted out...
  @Ignore
  public void testSetTableName() throws ODKTaskLockException, ODKDatastoreException,
      ETagMismatchException, PermissionDeniedException, ETagMismatchException {
    TableProperties expected = pm.getProperties();
    expected.setTableId("a new name"); // don't see how this would work...

    doTestSetProperties(expected);
  }

  @Test
  public void testSetTableMetadata() throws ODKTaskLockException, ODKDatastoreException,
      ETagMismatchException, PermissionDeniedException, ETagMismatchException {
    TableProperties expected = pm.getProperties();
    expected.setKeyValueStoreEntries(T.kvsEntries);

    doTestSetProperties(expected);
  }

  private void doTestSetProperties(TableProperties expected)
      throws ETagMismatchException, ODKTaskLockException,
      ODKDatastoreException, PermissionDeniedException, ETagMismatchException {
    pm.setProperties(expected);

    TableProperties actual = pm.getProperties();

    assertEquals(expected.getTableId(), actual.getTableId());
    Util.assertCollectionSameElements(expected.getKeyValueStoreEntries(),
        actual.getKeyValueStoreEntries());
  }

  @Test
  public void testSetTableNameChangesPropertiesModNum() throws ODKDatastoreException,
      ODKTaskLockException, ETagMismatchException, PermissionDeniedException, ETagMismatchException {
    TableProperties properties = pm.getProperties();
    properties.setTableId("a new table name"); // don't see how this would work

    doTestSetPropertiesChangesModNum(properties);
  }

  @Test
  public void testSetTableMetadataChangesPropertiesModNum() throws ODKTaskLockException,
      ODKDatastoreException, ETagMismatchException, PermissionDeniedException, ETagMismatchException {
    TableProperties properties = pm.getProperties();
    properties.setKeyValueStoreEntries(T.kvsEntries);

    doTestSetPropertiesChangesModNum(properties);
  }

  private void doTestSetPropertiesChangesModNum(TableProperties properties)
      throws ODKDatastoreException, ETagMismatchException, ODKTaskLockException, PermissionDeniedException {
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
      ETagMismatchException, ODKTaskLockException, PermissionDeniedException, ETagMismatchException {
    TableProperties properties = pm.getProperties();
    properties.setTableId("new name");
    pm.setProperties(properties);
    properties.setTableId("new name 2");
    pm.setProperties(properties);
  }
}
