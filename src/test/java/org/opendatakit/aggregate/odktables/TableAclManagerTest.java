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
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opendatakit.aggregate.odktables.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.rest.entity.Scope;
import org.opendatakit.aggregate.odktables.rest.entity.TableAcl;
import org.opendatakit.aggregate.odktables.rest.entity.TableEntry;
import org.opendatakit.aggregate.odktables.rest.entity.TableRole;
import org.opendatakit.aggregate.odktables.rest.entity.TableRole.TablePermission;
import org.opendatakit.aggregate.odktables.security.TablesUserPermissions;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.TestContextFactory;

public class TableAclManagerTest {

  private CallingContext cc;
  private TablesUserPermissions userPermissions;
  private String tableId;
  private TableManager tm;
  private Scope scope;
  private TableRole role;
  private TableAclManager am;

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

    this.scope = new Scope(Scope.Type.USER, T.user);
    this.role = TableRole.FILTERED_READER;
    this.am = new TableAclManager(T.appId, tableId, userPermissions, cc);
  }

  @After
  public void tearDown() throws ODKDatastoreException, ODKTaskLockException, PermissionDeniedException {
    try {
      tm.deleteTable(tableId);
    } catch (ODKEntityNotFoundException e) {
      // ignore
    }
  }

  @Test
  public void testGetAclsEmpty() throws ODKDatastoreException, PermissionDeniedException {
    List<TableAcl> acls = am.getAcls();
    assertEquals(1, acls.size());
  }

  @Test
  public void testGetAcls() throws ODKDatastoreException, PermissionDeniedException {
    am.setAcl(scope, role);
    scope.setValue(scope.getValue() + "diff");
    am.setAcl(scope, role);
    List<TableAcl> acls = am.getAcls();
    assertEquals(3, acls.size());
  }

  @Test
  public void testGetUserAcls() throws ODKDatastoreException, PermissionDeniedException {
    am.setAcl(scope, role);
    scope.setValue(scope.getValue() + "diff");
    am.setAcl(scope, role);
    List<TableAcl> acls = am.getAcls(Scope.Type.USER);
    assertEquals(3, acls.size());
  }

  @Test
  public void testGetGroupAcls() throws ODKDatastoreException, PermissionDeniedException {
    am.setAcl(scope, role);
    scope.setValue(scope.getValue() + "diff");
    am.setAcl(scope, role);
    List<TableAcl> acls = am.getAcls(Scope.Type.GROUP);
    assertEquals(0, acls.size());
  }

  @Test
  public void testGetAclEmpty() throws ODKDatastoreException, PermissionDeniedException {
    TableAcl acl = am.getAcl(scope);
    assertTrue(acl == null);
  }

  @Test
  public void testSetAcl() throws ODKDatastoreException, PermissionDeniedException {
    TableAcl expected = am.setAcl(scope, role);
    TableAcl actual = am.getAcl(scope);
    assertEquals(expected, actual);
  }

}
