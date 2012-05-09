package org.opendatakit.aggregate.odktables.relation;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.opendatakit.aggregate.odktables.entity.TablePermission;

/**
 * @author the.dylan.price@gmail.com
 * 
 */
public class RUtil {

  public static final String NAMESPACE = "ODKTABLES";

  /**
   * Converts a uuid to a datastore table and column name friendly format.
   */
  public static String convertIdentifier(String id) {
    id = "table:" + id;
    return id.replace('-', '_').replace(':', '_').toUpperCase();
  }

  /**
   * Convert a list of {@link TablePermission} into a string as it should be
   * stored in the datastore
   */
  public static String toPermissionsString(List<TablePermission> permissions) {
    return StringUtils.join(permissions, ',');
  }

  /**
   * Convert a string of permissions coming from the datastore to a list of
   * {@link TablePermission}
   */
  public static List<TablePermission> toPermissionsList(String permissions) {
    String[] strings = StringUtils.split(permissions, ',');
    List<TablePermission> perms = new ArrayList<TablePermission>();
    for (String string : strings) {
      TablePermission permission = TablePermission.valueOf(string);
      perms.add(permission);
    }
    return perms;
  }
}
