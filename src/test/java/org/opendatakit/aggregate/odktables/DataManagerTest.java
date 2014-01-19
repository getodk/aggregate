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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.opendatakit.aggregate.odktables.exception.BadColumnNameException;
import org.opendatakit.aggregate.odktables.exception.ETagMismatchException;
import org.opendatakit.aggregate.odktables.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.rest.entity.Row;
import org.opendatakit.aggregate.odktables.rest.entity.Scope;
import org.opendatakit.aggregate.odktables.rest.entity.Scope.Type;
import org.opendatakit.aggregate.odktables.rest.entity.TableEntry;
import org.opendatakit.aggregate.odktables.rest.entity.TableRole.TablePermission;
import org.opendatakit.aggregate.odktables.security.TablesUserPermissions;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.PersistConsts;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.TestContextFactory;

import com.google.common.collect.Maps;

public class DataManagerTest {

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
    public boolean hasFilterScope(String tableId, Scope filterScope) {
      return true;
    }

    @Override
    public void checkFilter(String tableId, TablePermission permission, String rowId, Scope filter)
        throws ODKDatastoreException, PermissionDeniedException {
      return;
    }

  }

  private CallingContext cc;
  private TablesUserPermissions userPermissions;
  private String tableId;
  private TableManager tm;
  private DataManager dm;
  private List<Row> rows;

  @Before
  public void setUp() throws Exception {
    this.cc = TestContextFactory.getCallingContext();

    userPermissions = new MockCurrentUserPermissions();

    this.tm = new TableManager(userPermissions, cc);

    tm.createTable(tableId,
        T.columns, T.kvsEntries);

    this.dm = new DataManager(tableId, userPermissions, cc);

    this.rows = T.rows;
    clearRows();
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
  public void testGetRowsEmpty() throws ODKDatastoreException, PermissionDeniedException {
    List<Row> rows = dm.getRows();
    assertTrue(rows.isEmpty());
  }

  @Test
  public void testInsertRows() throws ODKEntityPersistException, ODKDatastoreException,
      ODKTaskLockException, BadColumnNameException, ETagMismatchException, PermissionDeniedException {
    List<Row> actualRows = dm.insertOrUpdateRows(rows);
    assertEquals(2, actualRows.size());
    for (int i = 0; i < rows.size(); i++) {
      Row expected = rows.get(i);
      Row actual = actualRows.get(i);
      expected.setRowETag(actual.getRowETag());
      expected.setDataETagAtModification(actual.getDataETagAtModification());
      expected.setCreateUser(actual.getCreateUser());
      expected.setLastUpdateUser(actual.getLastUpdateUser());
    }
    assertEquals(rows.get(0), actualRows.get(0));
    assertEquals(rows.get(1), actualRows.get(1));
  }

  @Test(expected = ETagMismatchException.class)
  public void testInsertRowsAlreadyExist() throws ODKEntityPersistException, ODKDatastoreException,
      ODKTaskLockException, BadColumnNameException, ETagMismatchException, PermissionDeniedException {
    dm.insertOrUpdateRows(rows);
    dm.insertOrUpdateRows(rows);
  }

  @Test
  public void testGetRows() throws ODKDatastoreException, ODKTaskLockException,
      BadColumnNameException, ETagMismatchException, PermissionDeniedException {
    dm.insertOrUpdateRows(rows);
    List<Row> actualRows = dm.getRows();
    for (int i = 0; i < rows.size(); i++) {
      Row expected = rows.get(i);
      Row actual = actualRows.get(i);
      assertEquals(expected.getRowId(), actual.getRowId());
      assertEquals(expected.getValues(), actual.getValues());
    }
  }

//  @Test
//  public void testGetRowsByScope() throws ODKEntityPersistException, ODKDatastoreException,
//      ODKTaskLockException, ETagMismatchException, BadColumnNameException, PermissionDeniedException {
//    List<Row> rows = setupTestRows();
//    Row expected = rows.get(0);
//    List<Row> actualRows = dm.getRows(expected.getFilterScope());
//    assertEquals(1, actualRows.size());
//    Row actual = actualRows.get(0);
//    assertEquals(actual, expected);
//  }
//
//  @Test
//  public void testGetRowsByScopes() throws ODKEntityPersistException, ETagMismatchException,
//      BadColumnNameException, ODKDatastoreException, ODKTaskLockException, PermissionDeniedException {
//    List<Row> rows = setupTestRows();
//    Row row1 = rows.get(0);
//    Row row2 = rows.get(1);
//    List<Scope> scopes = new ArrayList<Scope>();
//    scopes.add(row1.getFilterScope());
//    scopes.add(row2.getFilterScope());
//    List<Row> results = dm.getRows(scopes);
//    assertEquals(2, results.size());
//  }

  @Test
  public void testGetRow() throws ODKDatastoreException, ODKTaskLockException,
      BadColumnNameException, ETagMismatchException, PermissionDeniedException {
    Row expected = Row.forInsert(T.Data.DYLAN.getId(), T.uri_access_control_1, T.form_id_1, T.locale_1, T.savepoint_timestamp_1, T.Data.DYLAN.getValues());
    List<Row> changes = dm.insertOrUpdateRows(Collections.singletonList(expected));
    expected = changes.get(0);
    Row actual = dm.getRow(T.Data.DYLAN.getId());
    assertEquals(expected, actual);
  }

  @Test
  public void testGetRowDoeNotExist() throws ODKDatastoreException, PermissionDeniedException {
    Row row = dm.getRow(T.Data.DYLAN.getId());
    assertNull(row);
  }

  @Test
  public void testGetRowNullSafe() throws ODKEntityPersistException, ODKDatastoreException,
      ODKTaskLockException, BadColumnNameException, ETagMismatchException, PermissionDeniedException {
    Row expected = Row.forInsert(T.Data.DYLAN.getId(), T.uri_access_control_1, T.form_id_1, T.locale_1, T.savepoint_timestamp_1, T.Data.DYLAN.getValues());
    List<Row> changes = dm.insertOrUpdateRows(Collections.singletonList(expected));
    expected = changes.get(0);
    Row actual = dm.getRowNullSafe(T.Data.DYLAN.getId());
    assertEquals(expected, actual);
  }

  @Test(expected = ODKEntityNotFoundException.class)
  public void testGetRowNullSafeDoesNotExist() throws ODKEntityNotFoundException,
      ODKDatastoreException, PermissionDeniedException {
    dm.getRowNullSafe(T.Data.DYLAN.getId());
  }

  @Test
  public void testUpdateRow() throws ODKEntityPersistException, ODKDatastoreException,
      ODKTaskLockException, ETagMismatchException, BadColumnNameException, PermissionDeniedException {
    List<Row> expectedChanges = dm.insertOrUpdateRows(rows);
    Row expected = expectedChanges.get(0);
    expected.getValues().put(T.Columns.column_age.getElementKey(), "24");
    List<Row> changes = dm.insertOrUpdateRows(Collections.singletonList(expected));
    Row actual = changes.get(0);
    assertFalse(expected.getRowETag().equals(actual.getRowETag()));
    expected.setRowETag(actual.getRowETag());
    expected.setDataETagAtModification(actual.getDataETagAtModification());
    assertEquals(expected, actual);
  }

  @Test(expected = ETagMismatchException.class)
  public void testUpdateRowVersionMismatch() throws ODKEntityPersistException,
      ODKDatastoreException, ODKTaskLockException, ETagMismatchException,
      BadColumnNameException, PermissionDeniedException {
    rows = dm.insertOrUpdateRows(rows);
    Row row = rows.get(0);
    row.setRowETag(CommonFieldsBase.newUri());
    dm.insertOrUpdateRows(Collections.singletonList(row));
  }

  @Test(expected = ODKEntityNotFoundException.class)
  public void testUpdateRowDoesNotExist() throws ODKEntityNotFoundException, ODKDatastoreException,
      ODKTaskLockException, ETagMismatchException, BadColumnNameException, PermissionDeniedException {
    Row row = rows.get(0);
    row.setRowETag(CommonFieldsBase.newUri());
    dm.insertOrUpdateRows(Collections.singletonList(row));
  }

  @Test
  public void testUpdateRowDoesNotChangeFilter() throws ODKEntityPersistException,
      ODKDatastoreException, ODKTaskLockException, ETagMismatchException,
      BadColumnNameException, PermissionDeniedException {
    Row expected = rows.get(0);
    expected.setFilterScope(new Scope(Type.USER, T.user));
    List<Row> changes = dm.insertOrUpdateRows(Collections.singletonList(expected));
    expected = changes.get(0);
    Row actual = Row.forUpdate(expected.getRowId(), expected.getRowETag(),
        T.uri_access_control_1, T.form_id_1, T.locale_1, T.savepoint_timestamp_1,
        Maps.<String, String> newHashMap());
    List<Row> actuals = dm.insertOrUpdateRows(Collections.singletonList(actual));
    actual = actuals.get(0);
    expected.setRowETag(actual.getRowETag());
    expected.setDataETagAtModification(actual.getDataETagAtModification());
    assertEquals(expected.getFilterScope(), actual.getFilterScope());
  }

  @Test
  public void testUpdateRowDoesChangeFilter() throws ODKEntityNotFoundException,
      ETagMismatchException, ODKDatastoreException, ODKTaskLockException,
      BadColumnNameException, PermissionDeniedException {
    Row row = rows.get(0);
    row.setFilterScope(new Scope(Type.USER, T.user));
    List<Row> changes = dm.insertOrUpdateRows(Collections.singletonList(row));
    row = changes.get(0);
    Row actual = Row
        .forUpdate(row.getRowId(), row.getRowETag(),
            T.uri_access_control_1, T.form_id_1, T.locale_1, T.savepoint_timestamp_1,
            Maps.<String, String> newHashMap());
    actual.setFilterScope(Scope.EMPTY_SCOPE);
    List<Row> actualChanges = dm.insertOrUpdateRows(Collections.singletonList(actual));
    actual = actualChanges.get(0);
    row.setRowETag(actual.getRowETag());
    row.setDataETagAtModification(actual.getDataETagAtModification());
    assertEquals(Scope.EMPTY_SCOPE, actual.getFilterScope());
  }

  @Test(expected = BadColumnNameException.class)
  public void testUpdateRowBadColumnName() throws ODKEntityPersistException,
      BadColumnNameException, ODKDatastoreException, ODKTaskLockException,
      ETagMismatchException, PermissionDeniedException {
    Row row = rows.get(0);
    List<Row> changes = dm.insertOrUpdateRows(Collections.singletonList(row));
    row = changes.get(0);
    Map<String, String> values = Maps.newHashMap();
    values.put(T.Columns.name + "diff", "value");
    row = Row.forUpdate(row.getRowId(), row.getRowETag(), T.uri_access_control_1, T.form_id_1, T.locale_1, T.savepoint_timestamp_1, values);
    List<Row> actualChanges = dm.insertOrUpdateRows(Collections.singletonList(row));
    Row actual = actualChanges.get(0);
  }

  @Test
  public void testDeleteRows() throws ODKEntityPersistException, ODKDatastoreException,
      ODKTaskLockException, BadColumnNameException, ETagMismatchException, PermissionDeniedException {
    List<Row> changes = dm.insertOrUpdateRows(rows);
    dm.deleteRows(Util.list(T.Data.DYLAN.getId(), T.Data.JOHN.getId()));
    // this may actually require accessing the task lock to force a flush of the delete
    // under the new GAE development environment datastore.  Try sleeping for now...
    try {
      Thread.sleep(PersistConsts.MAX_SETTLE_MILLISECONDS);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    List<Row> rows = dm.getRows();
    assertTrue(rows.isEmpty());
  }

  @Test
  public void testGetRowsSince() throws ODKEntityPersistException, ODKDatastoreException,
      ODKTaskLockException, ETagMismatchException, BadColumnNameException, PermissionDeniedException {
    TableEntry entry = tm.getTableNullSafe(tableId);
    String beginETag = entry.getDataETag();
    List<Row> changes = dm.insertOrUpdateRows(rows);

    Map<String, Row> expected = new HashMap<String, Row>();

    for (Row row : changes) {
      row.getValues().put(T.Columns.column_age.getElementKey(), "99");
    }

    List<Row> round2changes = dm.insertOrUpdateRows(rows);

    for (Row row : round2changes) {
      expected.put(row.getRowId(), row);
    }

    Row row = round2changes.get(0);
    row.getValues().put(T.Columns.column_age.getElementKey(), "444");

    List<Row> round3changes = dm.insertOrUpdateRows(Collections.singletonList(row));
    row = round3changes.get(0);
    expected.put(row.getRowId(), row);

    List<Row> actual = dm.getRowsSince(beginETag);
    Util.assertCollectionSameElements(expected.values(), actual);
  }

//  @Test
//  public void testGetRowsSinceByScope() throws ODKEntityPersistException, ETagMismatchException,
//      BadColumnNameException, ODKDatastoreException, ODKTaskLockException, PermissionDeniedException {
//    TableEntry entry = tm.getTableNullSafe(tableId);
//    String beginETag = entry.getDataETag();
//    List<Row> rows = setupTestRows();
//    Scope scope = rows.get(0).getFilterScope();
//    List<Row> expected = dm.getRows(scope);
//    List<Row> actual = dm.getRowsSince(beginETag, scope);
//    Util.assertCollectionSameElements(expected, actual);
//  }
//
//  // TODO: fix this -- since-last-change is BROKEN!!!
//  @Ignore
//  public void testGetRowsSinceByScopes() throws ODKDatastoreException, ETagMismatchException,
//      BadColumnNameException, ODKTaskLockException, PermissionDeniedException {
//    TableEntry entry = tm.getTableNullSafe(tableId);
//    String beginETag = entry.getDataETag();
//    List<Row> rows = setupTestRows();
//    Row row2 = rows.get(1);
//    Row row3 = rows.get(2);
//    List<Scope> scopes = new ArrayList<Scope>();
//    scopes.add(row2.getFilterScope());
//    scopes.add(row3.getFilterScope());
//    List<Row> expected = dm.getRows(scopes);
//    List<Row> actual = dm.getRowsSince(beginETag, scopes);
//    Util.assertCollectionSameElements(expected, actual);
//  }

  @Ignore
  private void clearRows() throws ODKDatastoreException, ODKTaskLockException, PermissionDeniedException {
    List<Row> rows = dm.getRows();
    for ( Row old : rows ) {
      dm.deleteRow(old.getRowId());
    }
  }

  @Ignore
  private List<Row> setupTestRows() throws ODKEntityPersistException, ODKDatastoreException,
      ODKTaskLockException, ETagMismatchException, BadColumnNameException, PermissionDeniedException {
    clearRows();
    Map<String, String> values = Maps.newHashMap();
    List<Row> rows = new ArrayList<Row>();

    Row row = Row.forInsert("1", T.uri_access_control_1, T.form_id_1, T.locale_1, T.savepoint_timestamp_1, values);
    row.setFilterScope(new Scope(Type.DEFAULT, null));
    rows.add(row);

    row = Row.forInsert("2", T.uri_access_control_2, T.form_id_2, T.locale_2, T.savepoint_timestamp_2, values);
    row.setFilterScope(new Scope(Type.USER, T.user));
    rows.add(row);

    row = Row.forInsert("3", T.uri_access_control_1, T.form_id_1, T.locale_1, T.savepoint_timestamp_1, values);
    row.setFilterScope(new Scope(Type.GROUP, T.group));
    rows.add(row);

    row = Row.forInsert("4", T.uri_access_control_2, T.form_id_2, T.locale_2, T.savepoint_timestamp_2, values);
    row.setFilterScope(Scope.EMPTY_SCOPE);
    rows.add(row);

    List<Row> changes = dm.insertOrUpdateRows(rows);
    values.put(T.Columns.column_name.getElementKey(), "some name");

    for (Row update : changes) {
      update.setValues(values);
      update.setFilterScope(null);
    }
    changes = dm.insertOrUpdateRows(rows);

    return changes;
  }

}
