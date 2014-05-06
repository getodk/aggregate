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
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.opendatakit.aggregate.odktables.exception.BadColumnNameException;
import org.opendatakit.aggregate.odktables.exception.ETagMismatchException;
import org.opendatakit.aggregate.odktables.exception.InconsistentStateException;
import org.opendatakit.aggregate.odktables.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.rest.SavepointTypeManipulator;
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
    public void checkPermission(String appId, String tableId, TablePermission permission)
        throws ODKDatastoreException, PermissionDeniedException {
      return;
    }

    @Override
    public boolean hasPermission(String appId, String tableId, TablePermission permission)
        throws ODKDatastoreException {
      return true;
    }

    @Override
    public boolean hasFilterScope(String appId, String tableId, TablePermission permission, String rowId, Scope filterScope) {
      return true;
    }

  }

  private CallingContext cc;
  private TablesUserPermissions userPermissions;
  private TableManager tm;
  private DataManager dm;
  private List<Row> rows;

  @Before
  public void setUp() throws Exception {
    this.cc = TestContextFactory.getCallingContext();

    userPermissions = new MockCurrentUserPermissions();

    this.tm = new TableManager(T.appId, userPermissions, cc);

    TableEntry te = tm.createTable(T.tableId, T.columns);

    this.dm = new DataManager(T.appId, T.tableId, userPermissions, cc);

    this.rows = T.rows;
    clearRows();
  }

  @After
  public void tearDown() throws Exception {
    try {
      tm.deleteTable(T.tableId);
    } catch (ODKEntityNotFoundException e) {
      // ignore
    }
  }

  @Test
  public void testGetRowsEmpty() throws ODKDatastoreException, PermissionDeniedException, InconsistentStateException, ODKTaskLockException, BadColumnNameException {
    List<Row> rows = dm.getRows();
    assertTrue(rows.isEmpty());
  }

  @Test
  public void testInsertRows() throws ODKEntityPersistException, ODKDatastoreException,
      ODKTaskLockException, BadColumnNameException, ETagMismatchException, PermissionDeniedException, InconsistentStateException {
    List<Row> actualRows = new ArrayList<Row>();
    for ( Row r : rows ) {
      actualRows.add(dm.insertOrUpdateRow(r));
    }
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

  @Test
  public void testInsertRowsAlreadyExist() throws ODKEntityPersistException, ODKDatastoreException,
      ODKTaskLockException, BadColumnNameException, ETagMismatchException, PermissionDeniedException, InconsistentStateException {
    List<Row> actualRows = new ArrayList<Row>();
    for ( Row r : rows ) {
      actualRows.add(dm.insertOrUpdateRow(r));
    }
    List<Row> actual2Rows = new ArrayList<Row>();
    for ( Row r : rows ) {
      actual2Rows.add(dm.insertOrUpdateRow(r));
    }
    for ( int i = 0 ; i < rows.size() ; ++i ) {
      String etag1 = actualRows.get(i).getRowETag();
      String etag2 = actual2Rows.get(i).getRowETag();

      assertEquals(etag1,etag2);
    }
  }

  @Test
  public void testGetRows() throws ODKDatastoreException, ODKTaskLockException,
      BadColumnNameException, ETagMismatchException, PermissionDeniedException, InconsistentStateException {
    List<Row> expectedRows = new ArrayList<Row>();
    for ( Row r : rows ) {
      expectedRows.add(dm.insertOrUpdateRow(r));
    }
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
  public void testGetRowNullSafe() throws ODKEntityPersistException, ODKDatastoreException,
      ODKTaskLockException, BadColumnNameException, ETagMismatchException, PermissionDeniedException, InconsistentStateException {
    Row added = Row.forInsert(T.Data.DYLAN.getId(), T.form_id_1, T.locale_1, SavepointTypeManipulator.complete(),
        T.savepoint_timestamp_1, T.savepoint_creator_1, Scope.EMPTY_SCOPE, T.Data.DYLAN.getValues());
    Row expected = dm.insertOrUpdateRow(added);
    Row actual = dm.getRow(T.Data.DYLAN.getId());
    assertEquals(expected, actual);
  }

  @Test(expected = ODKEntityNotFoundException.class)
  public void testGetRowNullSafeDoesNotExist() throws ODKEntityNotFoundException,
      ODKDatastoreException, ODKTaskLockException, PermissionDeniedException, InconsistentStateException, BadColumnNameException {
    dm.getRow(T.Data.DYLAN.getId());
  }

  @Test
  public void testUpdateRow() throws ODKEntityPersistException, ODKDatastoreException,
      ODKTaskLockException, ETagMismatchException, BadColumnNameException, PermissionDeniedException, InconsistentStateException {
    List<Row> expectedChanges = new ArrayList<Row>();
    for ( Row r : rows ) {
      expectedChanges.add(dm.insertOrUpdateRow(r));
    }
    Row expected = expectedChanges.get(0);
    expected.getValues().put(T.Columns.column_age.getElementKey(), "24");
    Row actual = dm.insertOrUpdateRow(expected);
    assertFalse(expected.getRowETag().equals(actual.getRowETag()));
    expected.setRowETag(actual.getRowETag());
    expected.setDataETagAtModification(actual.getDataETagAtModification());
    assertEquals(expected, actual);
  }

  @Test
  public void testUpdateRowVersionMismatchIdenticalValues() throws ODKEntityPersistException,
      ODKDatastoreException, ODKTaskLockException, ETagMismatchException,
      BadColumnNameException, PermissionDeniedException, InconsistentStateException {
    List<Row> expectedChanges = new ArrayList<Row>();
    for ( Row r : T.rows ) {
      expectedChanges.add(dm.insertOrUpdateRow(r));
    }
    Row row = expectedChanges.get(0);
    row.setRowETag(CommonFieldsBase.newUri());
    dm.insertOrUpdateRow(row);
  }

  @Test(expected = ETagMismatchException.class)
  public void testUpdateRowVersionMismatchDeltaValues() throws ODKEntityPersistException,
      ODKDatastoreException, ODKTaskLockException, ETagMismatchException,
      BadColumnNameException, PermissionDeniedException, InconsistentStateException {
    List<Row> expectedChanges = new ArrayList<Row>();
    for ( Row r : T.rows ) {
      expectedChanges.add(dm.insertOrUpdateRow(r));
    }
    Row row = expectedChanges.get(0);
    row.setRowETag(CommonFieldsBase.newUri());
    Map<String,String> values = row.getValues();
    values.put(T.Columns.column_age.getElementKey(), "40");
    row.setValues(values);
    dm.insertOrUpdateRow(row);
  }

  @Test
  public void testUpdateRowDoesNotExist() throws ODKEntityNotFoundException, ODKDatastoreException,
      ODKTaskLockException, ETagMismatchException, BadColumnNameException, PermissionDeniedException, InconsistentStateException {
    Row row = rows.get(0);
    row.setRowETag(CommonFieldsBase.newUri());
    dm.insertOrUpdateRow(row);
  }

  @Test
  public void testUpdateRowDoesNotChangeFilter() throws ODKEntityPersistException,
      ODKDatastoreException, ODKTaskLockException, ETagMismatchException,
      BadColumnNameException, PermissionDeniedException, InconsistentStateException {
    Row expected = rows.get(0);
    expected.setFilterScope(new Scope(Type.USER, T.user));
    expected = dm.insertOrUpdateRow(expected);
    Row actual = Row.forUpdate(expected.getRowId(), expected.getRowETag(),
        T.form_id_1, T.locale_1, SavepointTypeManipulator.complete(),
        T.savepoint_timestamp_1, T.savepoint_creator_1, new Scope(Type.USER, T.user),
        Maps.<String, String> newHashMap());
    actual = dm.insertOrUpdateRow(actual);
    expected.setRowETag(actual.getRowETag());
    expected.setDataETagAtModification(actual.getDataETagAtModification());
    assertEquals(expected.getFilterScope(), actual.getFilterScope());
  }

  @Test
  public void testUpdateRowDoesChangeFilter() throws ODKEntityNotFoundException,
      ETagMismatchException, ODKDatastoreException, ODKTaskLockException,
      BadColumnNameException, PermissionDeniedException, InconsistentStateException {
    Row row = rows.get(0);
    row.setFilterScope(new Scope(Type.USER, T.user));
    row = dm.insertOrUpdateRow(row);
    Row actual = Row
        .forUpdate(row.getRowId(), row.getRowETag(),
            T.form_id_1, T.locale_1, SavepointTypeManipulator.complete(),
            T.savepoint_timestamp_1, T.savepoint_creator_1, Scope.EMPTY_SCOPE,
            Maps.<String, String> newHashMap());
    actual.setFilterScope(Scope.EMPTY_SCOPE);
    actual = dm.insertOrUpdateRow(actual);
    row.setRowETag(actual.getRowETag());
    row.setDataETagAtModification(actual.getDataETagAtModification());
    assertEquals(Scope.EMPTY_SCOPE, actual.getFilterScope());
  }

  @Test(expected = BadColumnNameException.class)
  public void testUpdateRowBadColumnName() throws ODKEntityPersistException,
      BadColumnNameException, ODKDatastoreException, ODKTaskLockException,
      ETagMismatchException, PermissionDeniedException, InconsistentStateException {
    Row row = rows.get(0);
    row = dm.insertOrUpdateRow(row);
    Map<String, String> values = Maps.newHashMap();
    values.put(T.Columns.name + "diff", "value");
    row = Row.forUpdate(row.getRowId(), row.getRowETag(), T.form_id_1, T.locale_1, SavepointTypeManipulator.complete(),
        T.savepoint_timestamp_1, T.savepoint_creator_1, Scope.EMPTY_SCOPE, values);
    @SuppressWarnings("unused")
    Row actual = dm.insertOrUpdateRow(row);
  }

  @Test
  public void testDelete2Rows() throws ODKEntityPersistException, ODKDatastoreException,
      ODKTaskLockException, BadColumnNameException, ETagMismatchException, PermissionDeniedException, InconsistentStateException {
    List<Row> expectedChanges = new ArrayList<Row>();
    for ( Row r : rows ) {
      expectedChanges.add(dm.insertOrUpdateRow(r));
    }
    // this may actually require accessing the task lock to force a flush of the delete
    // under the new GAE development environment datastore.  Try sleeping for now...
    try {
      Thread.sleep(PersistConsts.MAX_SETTLE_MILLISECONDS);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    dm.deleteRow(T.Data.DYLAN.getId());
    dm.deleteRow(T.Data.JOHN.getId());
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
      ODKTaskLockException, ETagMismatchException, BadColumnNameException, PermissionDeniedException, InconsistentStateException {
    TableEntry entry = tm.getTableNullSafe(T.tableId);
    String beginETag = entry.getDataETag();
    List<Row> changes = new ArrayList<Row>();
    for ( Row r : rows ) {
      changes.add(dm.insertOrUpdateRow(r));
    }

    Map<String, Row> expected = new HashMap<String, Row>();

    for (Row row : changes) {
      row.getValues().put(T.Columns.column_age.getElementKey(), "99");
    }

    List<Row> round2changes = new ArrayList<Row>();
    for ( Row r : rows ) {
      round2changes.add(dm.insertOrUpdateRow(r));
    }

    for (Row row : round2changes) {
      expected.put(row.getRowId(), row);
    }

    Row row = round2changes.get(0);
    row.getValues().put(T.Columns.column_age.getElementKey(), "444");

    List<Row> round3changes = new ArrayList<Row>();
    round3changes.add(dm.insertOrUpdateRow(row));

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
  private void clearRows() throws ODKDatastoreException, ODKTaskLockException, PermissionDeniedException, InconsistentStateException, BadColumnNameException {
    List<Row> rows = dm.getRows();
    for ( Row old : rows ) {
      dm.deleteRow(old.getRowId());
    }
  }

  @Ignore
  private List<Row> setupTestRows() throws ODKEntityPersistException, ODKDatastoreException,
      ODKTaskLockException, ETagMismatchException, BadColumnNameException, PermissionDeniedException, InconsistentStateException {
    clearRows();
    Map<String, String> values = Maps.newHashMap();
    List<Row> rows = new ArrayList<Row>();

    Row row = Row.forInsert("1", T.form_id_1, T.locale_1, SavepointTypeManipulator.complete(),
        T.savepoint_timestamp_1, T.savepoint_creator_1, Scope.EMPTY_SCOPE, values);
    row.setFilterScope(new Scope(Type.DEFAULT, null));
    rows.add(row);

    row = Row.forInsert("2", T.form_id_2, T.locale_2, SavepointTypeManipulator.complete(),
        T.savepoint_timestamp_2, T.savepoint_creator_2, Scope.EMPTY_SCOPE, values);
    row.setFilterScope(new Scope(Type.USER, T.user));
    rows.add(row);

    row = Row.forInsert("3", T.form_id_1, T.locale_1, SavepointTypeManipulator.complete(),
        T.savepoint_timestamp_1, T.savepoint_creator_1, Scope.EMPTY_SCOPE, values);
    row.setFilterScope(new Scope(Type.GROUP, T.group));
    rows.add(row);

    row = Row.forInsert("4", T.form_id_2, T.locale_2, SavepointTypeManipulator.complete(),
        T.savepoint_timestamp_2, T.savepoint_creator_2, Scope.EMPTY_SCOPE, values);
    row.setFilterScope(Scope.EMPTY_SCOPE);
    rows.add(row);

    List<Row> changes = new ArrayList<Row>();
    for ( Row r : rows ) {
      changes.add(dm.insertOrUpdateRow(r));
    }
    values.put(T.Columns.column_name.getElementKey(), "some name");

    for (Row update : changes) {
      update.setValues(values);
      update.setFilterScope(null);
    }

    for ( Row r : rows ) {
      changes.add(dm.insertOrUpdateRow(r));
    }

    return changes;
  }

}
