package org.opendatakit.aggregate.odktables.relation;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.opendatakit.aggregate.odktables.entity.TablePermission;

public class RUtilTest {

  private List<TablePermission> permissionsList;
  private String permissionsString;

  @Before
  public void setUp() throws Exception {
    permissionsList = new ArrayList<TablePermission>() {
      {
        add(TablePermission.READ_ROW);
        add(TablePermission.WRITE_ROW);
        add(TablePermission.READ_PROPERTIES);
        add(TablePermission.READ_ACL);
      }
    };

    permissionsString = "READ_ROW,WRITE_ROW,READ_PROPERTIES,READ_ACL";
  }

  @Test
  public void testToPermissionsString() {
    String permissions = RUtil.toPermissionsString(permissionsList);
    assertEquals(permissionsString, permissions);
  }

  @Test
  public void testToPermissionsList() {
    List<TablePermission> permissions = RUtil.toPermissionsList(permissionsString);
    assertEquals(permissionsList, permissions);
  }

}
