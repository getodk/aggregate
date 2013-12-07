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
import org.junit.Test;
import org.opendatakit.aggregate.odktables.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.rest.entity.Row;
import org.opendatakit.aggregate.odktables.rest.entity.Scope;
import org.opendatakit.aggregate.odktables.rest.entity.TableRole;
import org.opendatakit.aggregate.odktables.rest.entity.Scope.Type;
import org.opendatakit.aggregate.odktables.rest.entity.TableRole.TablePermission;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.TestContextFactory;

import com.google.common.collect.Maps;

public class AuthFilterTest {

  private CallingContext cc;
  private String tableId;
  private TableManager tm;
  private TableAclManager am;
  private AuthFilter af;
  private Scope currentUserScope;

  @Before
  public void setUp() throws Exception {
    this.cc = TestContextFactory.getCallingContext();
    this.tableId = T.tableId;
    this.tm = new TableManager(cc);

    tm.createTable(tableId,
        T.columns, T.kvsEntries);

    this.am = new TableAclManager(tableId, cc);
    this.af = new AuthFilter(tableId, cc);
    this.currentUserScope = new Scope(Type.USER, cc.getCurrentUser().getEmail());
  }

  @After
  public void tearDown() throws ODKDatastoreException, ODKTaskLockException {
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
  public void testHasPermission() throws ODKDatastoreException {
    assertTrue(af.hasPermission(TablePermission.READ_ROW));
  }

  @Test
  public void testHasPermissionNotTrue() throws ODKDatastoreException {
    am.deleteAcl(currentUserScope);
    assertFalse(af.hasPermission(TablePermission.READ_ROW));
  }

  @Test
  public void testCheckFilterSucceedsDefaultScope() throws PermissionDeniedException,
      ODKDatastoreException {
    am.deleteAcl(currentUserScope);
    am.setAcl(currentUserScope, TableRole.FILTERED_READER);
    Row row = Row.forInsert("1", Maps.<String, String> newHashMap());
    row.setFilterScope(new Scope(Type.DEFAULT, null));
    af.checkFilter(TablePermission.UNFILTERED_READ, row);
  }

  @Test(expected = PermissionDeniedException.class)
  public void testCheckFilterFailsEmptyScope() throws PermissionDeniedException,
      ODKDatastoreException {
    am.deleteAcl(currentUserScope);
    am.setAcl(currentUserScope, TableRole.FILTERED_READER);
    Row row = Row.forInsert("1", Maps.<String, String> newHashMap());
    row.setFilterScope(Scope.EMPTY_SCOPE);
    af.checkFilter(TablePermission.UNFILTERED_READ, row);
  }

  @Test
  public void testCheckFilterSucceedsUserScope() throws PermissionDeniedException,
      ODKDatastoreException {
    am.deleteAcl(currentUserScope);
    am.setAcl(currentUserScope, TableRole.FILTERED_READER);
    Row row = Row.forInsert("1", Maps.<String, String> newHashMap());
    row.setFilterScope(currentUserScope);
    af.checkFilter(TablePermission.UNFILTERED_READ, row);
  }

  @Test(expected = PermissionDeniedException.class)
  public void testCheckFilterFailsUserScope() throws PermissionDeniedException,
      ODKDatastoreException {
    am.deleteAcl(currentUserScope);
    am.setAcl(currentUserScope, TableRole.FILTERED_READER);
    Row row = Row.forInsert("1", Maps.<String, String> newHashMap());
    row.setFilterScope(new Scope(Type.USER, currentUserScope.getValue() + "diff"));
    af.checkFilter(TablePermission.UNFILTERED_READ, row);
  }

  @Test
  public void testGetScopes() {
    List<Scope> scopes = AuthFilter.getScopes(cc);
    assertTrue(scopes.contains(new Scope(Type.DEFAULT, null)));
    assertTrue(scopes.contains(currentUserScope));
    // TODO: assert group scopes
  }

}
