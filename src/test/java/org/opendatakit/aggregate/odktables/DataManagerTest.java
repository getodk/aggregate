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
import org.opendatakit.aggregate.odktables.exception.EtagMismatchException;
import org.opendatakit.aggregate.odktables.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.rest.entity.Row;
import org.opendatakit.aggregate.odktables.rest.entity.Scope;
import org.opendatakit.aggregate.odktables.rest.entity.TableEntry;
import org.opendatakit.aggregate.odktables.rest.entity.Scope.Type;
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

  private CallingContext cc;
  private String tableId;
  private String tableProperties;
  private AuthFilter af;
  private TableManager tm;
  private DataManager dm;
  private List<Row> rows;

  @Before
  public void setUp() throws Exception {
    this.cc = TestContextFactory.getCallingContext();

    this.tableProperties = T.tableMetadata;
    this.tm = new TableManager(cc);

    tm.createTable(tableId,
        T.columns, T.kvsEntries);

    this.dm = new DataManager(tableId, cc);
    this.af = new AuthFilter(tableId, cc);

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
  public void testGetRowsEmpty() throws ODKDatastoreException {
    List<Row> rows = dm.getRows();
    assertTrue(rows.isEmpty());
  }

  @Test
  public void testInsertRows() throws ODKEntityPersistException, ODKDatastoreException,
      ODKTaskLockException, BadColumnNameException, EtagMismatchException, PermissionDeniedException {
    List<Row> actualRows = dm.insertOrUpdateRows(af, rows);
    assertEquals(2, actualRows.size());
    for (int i = 0; i < rows.size(); i++) {
      Row expected = rows.get(i);
      Row actual = actualRows.get(i);
      expected.setRowEtag(actual.getRowEtag());
      expected.setDataEtagAtModification(actual.getDataEtagAtModification());
      expected.setCreateUser(actual.getCreateUser());
      expected.setLastUpdateUser(actual.getLastUpdateUser());
    }
    assertEquals(rows.get(0), actualRows.get(0));
    assertEquals(rows.get(1), actualRows.get(1));
  }

  @Test(expected = EtagMismatchException.class)
  public void testInsertRowsAlreadyExist() throws ODKEntityPersistException, ODKDatastoreException,
      ODKTaskLockException, BadColumnNameException, EtagMismatchException, PermissionDeniedException {
    dm.insertOrUpdateRows(af, rows);
    dm.insertOrUpdateRows(af, rows);
  }

  @Test
  public void testGetRows() throws ODKDatastoreException, ODKTaskLockException,
      BadColumnNameException, EtagMismatchException, PermissionDeniedException {
    dm.insertOrUpdateRows(af, rows);
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
      ODKTaskLockException, EtagMismatchException, BadColumnNameException, PermissionDeniedException {
    List<Row> rows = setupTestRows();
    Row expected = rows.get(0);
    List<Row> actualRows = dm.getRows(expected.getFilterScope());
    assertEquals(1, actualRows.size());
    Row actual = actualRows.get(0);
    assertEquals(actual, expected);
  }

  @Test
  public void testGetRowsByScopes() throws ODKEntityPersistException, EtagMismatchException,
      BadColumnNameException, ODKDatastoreException, ODKTaskLockException, PermissionDeniedException {
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
      BadColumnNameException, EtagMismatchException, PermissionDeniedException {
    Row expected = Row.forInsert(T.Data.DYLAN.getId(), T.Data.DYLAN.getValues());
    List<Row> changes = dm.insertOrUpdateRows(af, Collections.singletonList(expected));
    expected = changes.get(0);
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
      ODKTaskLockException, BadColumnNameException, EtagMismatchException, PermissionDeniedException {
    Row expected = Row.forInsert(T.Data.DYLAN.getId(), T.Data.DYLAN.getValues());
    List<Row> changes = dm.insertOrUpdateRows(af, Collections.singletonList(expected));
    expected = changes.get(0);
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
      ODKTaskLockException, EtagMismatchException, BadColumnNameException, PermissionDeniedException {
    List<Row> expectedChanges = dm.insertOrUpdateRows(af, rows);
    Row expected = expectedChanges.get(0);
    expected.getValues().put(T.Columns.column_age.getElementKey(), "24");
    List<Row> changes = dm.insertOrUpdateRows(af, Collections.singletonList(expected));
    Row actual = changes.get(0);
    assertFalse(expected.getRowEtag().equals(actual.getRowEtag()));
    expected.setRowEtag(actual.getRowEtag());
    expected.setDataEtagAtModification(actual.getDataEtagAtModification());
    assertEquals(expected, actual);
  }

  @Test(expected = EtagMismatchException.class)
  public void testUpdateRowVersionMismatch() throws ODKEntityPersistException,
      ODKDatastoreException, ODKTaskLockException, EtagMismatchException,
      BadColumnNameException, PermissionDeniedException {
    rows = dm.insertOrUpdateRows(af, rows);
    Row row = rows.get(0);
    row.setRowEtag(CommonFieldsBase.newUri());
    dm.insertOrUpdateRows(af, Collections.singletonList(row));
  }

  @Test(expected = ODKEntityNotFoundException.class)
  public void testUpdateRowDoesNotExist() throws ODKEntityNotFoundException, ODKDatastoreException,
      ODKTaskLockException, EtagMismatchException, BadColumnNameException, PermissionDeniedException {
    Row row = rows.get(0);
    row.setRowEtag(CommonFieldsBase.newUri());
    dm.insertOrUpdateRows(af, Collections.singletonList(row));
  }

  @Test
  public void testUpdateRowDoesNotChangeFilter() throws ODKEntityPersistException,
      ODKDatastoreException, ODKTaskLockException, EtagMismatchException,
      BadColumnNameException, PermissionDeniedException {
    Row expected = rows.get(0);
    expected.setFilterScope(new Scope(Type.USER, T.user));
    List<Row> changes = dm.insertOrUpdateRows(af, Collections.singletonList(expected));
    expected = changes.get(0);
    Row actual = Row.forUpdate(expected.getRowId(), expected.getRowEtag(),
        Maps.<String, String> newHashMap());
    List<Row> actuals = dm.insertOrUpdateRows(af, Collections.singletonList(actual));
    actual = actuals.get(0);
    expected.setRowEtag(actual.getRowEtag());
    expected.setDataEtagAtModification(actual.getDataEtagAtModification());
    assertEquals(expected.getFilterScope(), actual.getFilterScope());
  }

  @Test
  public void testUpdateRowDoesChangeFilter() throws ODKEntityNotFoundException,
      EtagMismatchException, ODKDatastoreException, ODKTaskLockException,
      BadColumnNameException, PermissionDeniedException {
    Row row = rows.get(0);
    row.setFilterScope(new Scope(Type.USER, T.user));
    List<Row> changes = dm.insertOrUpdateRows(af, Collections.singletonList(row));
    row = changes.get(0);
    Row actual = Row
        .forUpdate(row.getRowId(), row.getRowEtag(), Maps.<String, String> newHashMap());
    actual.setFilterScope(Scope.EMPTY_SCOPE);
    List<Row> actualChanges = dm.insertOrUpdateRows(af, Collections.singletonList(actual));
    actual = actualChanges.get(0);
    row.setRowEtag(actual.getRowEtag());
    row.setDataEtagAtModification(actual.getDataEtagAtModification());
    assertEquals(Scope.EMPTY_SCOPE, actual.getFilterScope());
  }

  @Test(expected = BadColumnNameException.class)
  public void testUpdateRowBadColumnName() throws ODKEntityPersistException,
      BadColumnNameException, ODKDatastoreException, ODKTaskLockException,
      EtagMismatchException, PermissionDeniedException {
    Row row = rows.get(0);
    List<Row> changes = dm.insertOrUpdateRows(af, Collections.singletonList(row));
    row = changes.get(0);
    Map<String, String> values = Maps.newHashMap();
    values.put(T.Columns.name + "diff", "value");
    row = Row.forUpdate(row.getRowId(), row.getRowEtag(), values);
    List<Row> actualChanges = dm.insertOrUpdateRows(af, Collections.singletonList(row));
    Row actual = actualChanges.get(0);
  }

  @Test
  public void testDeleteRows() throws ODKEntityPersistException, ODKDatastoreException,
      ODKTaskLockException, BadColumnNameException, EtagMismatchException, PermissionDeniedException {
    List<Row> changes = dm.insertOrUpdateRows(af, rows);
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
      ODKTaskLockException, EtagMismatchException, BadColumnNameException, PermissionDeniedException {
    TableEntry entry = tm.getTableNullSafe(tableId);
    String beginEtag = entry.getDataEtag();
    List<Row> changes = dm.insertOrUpdateRows(af, rows);

    Map<String, Row> expected = new HashMap<String, Row>();

    for (Row row : changes) {
      row.getValues().put(T.Columns.column_age.getElementKey(), "99");
    }

    List<Row> round2changes = dm.insertOrUpdateRows(af, rows);

    for (Row row : round2changes) {
      expected.put(row.getRowId(), row);
    }

    Row row = round2changes.get(0);
    row.getValues().put(T.Columns.column_age.getElementKey(), "444");

    List<Row> round3changes = dm.insertOrUpdateRows(af, Collections.singletonList(row));
    row = round3changes.get(0);
    expected.put(row.getRowId(), row);

    List<Row> actual = dm.getRowsSince(beginEtag);
    Util.assertCollectionSameElements(expected.values(), actual);
  }

  @Test
  public void testGetRowsSinceByScope() throws ODKEntityPersistException, EtagMismatchException,
      BadColumnNameException, ODKDatastoreException, ODKTaskLockException, PermissionDeniedException {
    TableEntry entry = tm.getTableNullSafe(tableId);
    String beginEtag = entry.getDataEtag();
    List<Row> rows = setupTestRows();
    Scope scope = rows.get(0).getFilterScope();
    List<Row> expected = dm.getRows(scope);
    List<Row> actual = dm.getRowsSince(beginEtag, scope);
    Util.assertCollectionSameElements(expected, actual);
  }

  // TODO: fix this -- since-last-change is BROKEN!!!
  @Ignore
  public void testGetRowsSinceByScopes() throws ODKDatastoreException, EtagMismatchException,
      BadColumnNameException, ODKTaskLockException, PermissionDeniedException {
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

  @Ignore
  private void clearRows() throws ODKDatastoreException, ODKTaskLockException {
    List<Row> rows = dm.getRows();
    for ( Row old : rows ) {
      dm.deleteRow(old.getRowId());
    }
  }

  @Ignore
  private List<Row> setupTestRows() throws ODKEntityPersistException, ODKDatastoreException,
      ODKTaskLockException, EtagMismatchException, BadColumnNameException, PermissionDeniedException {
    clearRows();
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

    List<Row> changes = dm.insertOrUpdateRows(af, rows);
    values.put(T.Columns.column_name.getElementKey(), "some name");

    for (Row update : changes) {
      update.setValues(values);
      update.setFilterScope(null);
    }
    changes = dm.insertOrUpdateRows(af, rows);

    return changes;
  }

}
