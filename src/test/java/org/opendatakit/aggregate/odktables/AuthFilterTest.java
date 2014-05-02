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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.opendatakit.aggregate.odktables.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.rest.SavepointTypeManipulator;
import org.opendatakit.aggregate.odktables.rest.entity.Row;
import org.opendatakit.aggregate.odktables.rest.entity.Scope;
import org.opendatakit.aggregate.odktables.rest.entity.Scope.Type;
import org.opendatakit.aggregate.odktables.rest.entity.TableEntry;
import org.opendatakit.aggregate.odktables.rest.entity.TableRole;
import org.opendatakit.aggregate.odktables.rest.entity.TableRole.TablePermission;
import org.opendatakit.aggregate.odktables.security.AuthFilter;
import org.opendatakit.aggregate.odktables.security.TablesUserPermissions;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.TestContextFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@Ignore
public class AuthFilterTest {

  private CallingContext cc;
  private String tableId;
  private TableManager tm;
  private TableAclManager am;
  private AuthFilter af;
  private Scope currentUserScope;
  private TablesUserPermissions userPermissions;

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

  @Before
  public void setUp() throws Exception {
    this.cc = TestContextFactory.getCallingContext();

    userPermissions = new MockCurrentUserPermissions();
    this.tableId = T.tableId;

    this.tm = new TableManager(T.appId, userPermissions, cc);

    TableEntry te = tm.createTable(tableId, T.columns);

    this.am = new TableAclManager(T.appId, tableId, userPermissions, cc);
    List<Scope> scopes = Lists.newArrayList();
    scopes.add(new Scope(Type.DEFAULT, null));
    scopes.add(new Scope(Type.USER, userPermissions.getOdkTablesUserId()));

    this.af = new AuthFilter(T.appId, tableId, userPermissions, scopes, cc);
    this.currentUserScope = new Scope(Type.USER, userPermissions.getOdkTablesUserId());
  }

  @After
  public void tearDown() throws ODKDatastoreException, ODKTaskLockException,
      PermissionDeniedException {
    try {
      tm.deleteTable(tableId);
    } catch (ODKEntityNotFoundException e) {
      // ignore
    }
  }

  @Test(expected = PermissionDeniedException.class)
  public void testCheckPermissionPermissionDenied() throws PermissionDeniedException,
      ODKDatastoreException {
    am.deleteAcl(currentUserScope);
    af.checkPermission(TablePermission.READ_ROW);
  }

  @Test
  public void testCheckPermissionUserHasPermission() throws ODKDatastoreException,
      PermissionDeniedException {
    am.deleteAcl(currentUserScope);
    am.setAcl(currentUserScope, TableRole.READER);
    af.checkPermission(TablePermission.READ_ROW);
  }

  @Test
  public void testCheckPermissionDefaultHasPermission() throws ODKDatastoreException,
      PermissionDeniedException {
    am.deleteAcl(currentUserScope);
    am.setAcl(new Scope(Type.DEFAULT, null), TableRole.READER);
    af.checkPermission(TablePermission.READ_ROW);
  }

  @Test
  public void testHasPermission() throws ODKDatastoreException, PermissionDeniedException {
    assertTrue(af.hasPermission(TablePermission.READ_ROW));
  }

  @Test
  public void testHasPermissionNotTrue() throws ODKDatastoreException, PermissionDeniedException {
    am.deleteAcl(currentUserScope);
    assertFalse(af.hasPermission(TablePermission.READ_ROW));
  }

  @Test
  public void testCheckFilterSucceedsDefaultScope() throws PermissionDeniedException,
      ODKDatastoreException {
    am.deleteAcl(currentUserScope);
    am.setAcl(currentUserScope, TableRole.FILTERED_READER);
    Row row = Row.forInsert("1", T.form_id_1, T.locale_1, SavepointTypeManipulator.complete(),
        T.savepoint_timestamp_1, T.savepoint_creator_1, Scope.EMPTY_SCOPE, Maps.<String, String> newHashMap());
    row.setFilterScope(new Scope(Type.DEFAULT, null));
    assertTrue(af.hasFilterScope(TablePermission.READ_ROW, row.getRowId(), row.getFilterScope()));
  }

  @Test
  public void testCheckFilterFailsEmptyScope() throws PermissionDeniedException,
      ODKDatastoreException {
    am.deleteAcl(currentUserScope);
    am.setAcl(currentUserScope, TableRole.FILTERED_READER);
    Row row = Row.forInsert("1", T.form_id_1, T.locale_1, SavepointTypeManipulator.complete(),
        T.savepoint_timestamp_1, T.savepoint_creator_1, Scope.EMPTY_SCOPE, Maps.<String, String> newHashMap());
    row.setFilterScope(Scope.EMPTY_SCOPE);
    assertFalse(af.hasFilterScope(TablePermission.READ_ROW, row.getRowId(), row.getFilterScope()));
  }

  @Test
  public void testCheckFilterSucceedsUserScope() throws PermissionDeniedException,
      ODKDatastoreException {
    am.deleteAcl(currentUserScope);
    am.setAcl(currentUserScope, TableRole.FILTERED_READER);
    Row row = Row.forInsert("1", T.form_id_1, T.locale_1, SavepointTypeManipulator.complete(),
        T.savepoint_timestamp_1, T.savepoint_creator_1, Scope.EMPTY_SCOPE, Maps.<String, String> newHashMap());
    row.setFilterScope(currentUserScope);
    assertTrue(af.hasFilterScope(TablePermission.UNFILTERED_READ, row.getRowId(), row.getFilterScope()));
  }

  @Test
  public void testCheckFilterFailsUserScope() throws PermissionDeniedException,
      ODKDatastoreException {
    am.deleteAcl(currentUserScope);
    am.setAcl(currentUserScope, TableRole.FILTERED_READER);
    Row row = Row.forInsert("1", T.form_id_1, T.locale_1, SavepointTypeManipulator.complete(),
        T.savepoint_timestamp_1, T.savepoint_creator_1, Scope.EMPTY_SCOPE, Maps.<String, String> newHashMap());
    row.setFilterScope(new Scope(Type.USER, currentUserScope.getValue() + "diff"));
    assertFalse(af.hasFilterScope(TablePermission.UNFILTERED_READ, row.getRowId(), row.getFilterScope()));
  }
}
