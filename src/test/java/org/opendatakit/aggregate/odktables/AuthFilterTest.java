package org.opendatakit.aggregate.odktables;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opendatakit.aggregate.odktables.entity.Row;
import org.opendatakit.aggregate.odktables.entity.Scope;
import org.opendatakit.aggregate.odktables.entity.Scope.Type;
import org.opendatakit.aggregate.odktables.entity.TableRole;
import org.opendatakit.aggregate.odktables.entity.TableRole.TablePermission;
import org.opendatakit.aggregate.odktables.exception.PermissionDeniedException;
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

    tm.createTable(tableId, T.tableName, T.columns, T.tableMetadata);

    this.am = new TableAclManager(tableId, cc);
    this.af = new AuthFilter(tableId, cc);
    this.currentUserScope = new Scope(Type.USER, cc.getCurrentUser().getUriUser());
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
    af.checkFilter(row);
  }

  @Test(expected = PermissionDeniedException.class)
  public void testCheckFilterFailsEmptyScope() throws PermissionDeniedException,
      ODKDatastoreException {
    am.deleteAcl(currentUserScope);
    am.setAcl(currentUserScope, TableRole.FILTERED_READER);
    Row row = Row.forInsert("1", Maps.<String, String> newHashMap());
    row.setFilterScope(Scope.EMPTY_SCOPE);
    af.checkFilter(row);
  }

  @Test
  public void testCheckFilterSucceedsUserScope() throws PermissionDeniedException,
      ODKDatastoreException {
    am.deleteAcl(currentUserScope);
    am.setAcl(currentUserScope, TableRole.FILTERED_READER);
    Row row = Row.forInsert("1", Maps.<String, String> newHashMap());
    row.setFilterScope(currentUserScope);
    af.checkFilter(row);
  }

  @Test(expected = PermissionDeniedException.class)
  public void testCheckFilterFailsUserScope() throws PermissionDeniedException,
      ODKDatastoreException {
    am.deleteAcl(currentUserScope);
    am.setAcl(currentUserScope, TableRole.FILTERED_READER);
    Row row = Row.forInsert("1", Maps.<String, String> newHashMap());
    row.setFilterScope(new Scope(Type.USER, currentUserScope.getValue() + "diff"));
    af.checkFilter(row);
  }

  @Test
  public void testGetScopes() {
    List<Scope> scopes = af.getScopes();
    assertTrue(scopes.contains(new Scope(Type.DEFAULT, null)));
    assertTrue(scopes.contains(currentUserScope));
    // TODO: assert group scopes
  }

}
