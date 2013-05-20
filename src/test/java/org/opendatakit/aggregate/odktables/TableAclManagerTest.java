package org.opendatakit.aggregate.odktables;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opendatakit.aggregate.odktables.entity.Scope;
import org.opendatakit.aggregate.odktables.entity.TableAcl;
import org.opendatakit.aggregate.odktables.entity.TableRole;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.TestContextFactory;

public class TableAclManagerTest {

  private CallingContext cc;
  private String tableId;
  private TableManager tm;
  private Scope scope;
  private TableRole role;
  private TableAclManager am;

  @Before
  public void setUp() throws Exception {
    this.cc = TestContextFactory.getCallingContext();

    this.tableId = T.tableId;
    String tableName = T.tableName;
    String tableProperties = T.tableMetadata;
    this.tm = new TableManager(cc);

    tm.createTable(tableId, T.tableKey, T.dbTableName, 
        T.tableType, T.tableIdAccessControls, T.columns, 
        T.kvsEntries);
    
    this.scope = new Scope(Scope.Type.USER, T.user);
    this.role = TableRole.FILTERED_READER;
    this.am = new TableAclManager(tableId, cc);
  }

  @After
  public void tearDown() throws ODKDatastoreException, ODKTaskLockException {
    try {
      tm.deleteTable(tableId);
    } catch (ODKEntityNotFoundException e) {
      // ignore
    }
  }

  @Test
  public void testGetAclsEmpty() throws ODKDatastoreException {
    List<TableAcl> acls = am.getAcls();
    assertEquals(1, acls.size());
  }

  @Test
  public void testGetAcls() throws ODKDatastoreException {
    am.setAcl(scope, role);
    scope.setValue(scope.getValue() + "diff");
    am.setAcl(scope, role);
    List<TableAcl> acls = am.getAcls();
    assertEquals(3, acls.size());
  }

  @Test
  public void testGetUserAcls() throws ODKDatastoreException {
    am.setAcl(scope, role);
    scope.setValue(scope.getValue() + "diff");
    am.setAcl(scope, role);
    List<TableAcl> acls = am.getAcls(Scope.Type.USER);
    assertEquals(3, acls.size());
  }

  @Test
  public void testGetGroupAcls() throws ODKDatastoreException {
    am.setAcl(scope, role);
    scope.setValue(scope.getValue() + "diff");
    am.setAcl(scope, role);
    List<TableAcl> acls = am.getAcls(Scope.Type.GROUP);
    assertEquals(0, acls.size());
  }

  @Test
  public void testGetAclEmpty() throws ODKDatastoreException {
    TableAcl acl = am.getAcl(scope);
    assertTrue(acl == null);
  }

  @Test
  public void testSetAcl() throws ODKDatastoreException {
    TableAcl expected = am.setAcl(scope, role);
    TableAcl actual = am.getAcl(scope);
    assertEquals(expected, actual);
  }

}
