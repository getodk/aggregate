package org.opendatakit.aggregate.odktables;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.opendatakit.aggregate.odktables.entity.Row;
import org.opendatakit.aggregate.odktables.entity.Scope;
import org.opendatakit.aggregate.odktables.entity.Scope.Type;
import org.opendatakit.aggregate.odktables.entity.TableAcl;
import org.opendatakit.aggregate.odktables.entity.TableRole.TablePermission;
import org.opendatakit.aggregate.odktables.exception.PermissionDeniedException;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.web.CallingContext;

import com.google.gwt.thirdparty.guava.common.collect.Lists;

public class AuthFilter {

  private CallingContext cc;
  private TableAclManager am;

  public AuthFilter(String tableId, CallingContext cc) throws ODKEntityNotFoundException,
      ODKDatastoreException {
    this.cc = cc;
    this.am = new TableAclManager(tableId, cc);
  }

  /**
   * Checks that the current user has the given permission.
   * 
   * @param permission
   *          the permission to check
   * @throws ODKDatastoreException
   * @throws PermissionDeniedException
   *           if the current user does not have the permission
   */
  public void checkPermission(TablePermission permission) throws ODKDatastoreException,
      PermissionDeniedException {
    if (!hasPermission(permission)) {
      // TODO: change to getEmail()
      String userUri = cc.getCurrentUser().getUriUser();
      throw new PermissionDeniedException(String.format("Denied permission %s to user %s",
          permission, userUri));
    }
  }

  /**
   * Check if the current user has the given permission. An exception-safe
   * alternative to {@link #checkPermission(TablePermission)}
   * 
   * @param permission
   *          the permission to check
   * @return true if the user has the given permission, false otherwise
   * @throws ODKDatastoreException
   */
  public boolean hasPermission(TablePermission permission) throws ODKDatastoreException {
    // TODO: change to getEmail()
    String userUri = cc.getCurrentUser().getUriUser();
    Set<TablePermission> permissions = getPermissions(userUri);
    return permissions.contains(permission);
  }

  /**
   * Check that the user is within the scope of the filter on the given row.
   * Does not, for instance, check if the user has permission to read the row.
   * 
   * @param row
   *          the row to check
   * @throws ODKDatastoreException
   * @throws PermissionDeniedException
   *           if the current user is not within the scope of the filter on the
   *           row
   */
  public void checkFilter(Row row) throws ODKDatastoreException, PermissionDeniedException {
    // TODO: change to getEmail()
    String userUri = cc.getCurrentUser().getUriUser();

    Set<TablePermission> permissions = getPermissions(userUri);

    if (!permissions.contains(TablePermission.UNFILTERED_ROWS)) {
      Scope filter = row.getFilterScope();
      if (filter == null || filter.equals(Scope.EMPTY_SCOPE)) {
        // empty scope, no one allowed
        throwPermissionDenied(row.getRowId(), userUri);
      }
      switch (filter.getType()) {
      case USER:
        if (!userUri.equals(filter.getValue())) {
          throwPermissionDenied(row.getRowId(), userUri);
        }
        break;
      case GROUP:
        // TODO: add this
        // List<String> groups = getGroupNames(userUri);
        // if (!groups.contains(filter.getValue()))
        // {
        // throwPermissionDenied(row.getRowId(), userUri);
        // }
        break;
      default:
      case DEFAULT:
        // everyone is allowed to see it
        break;
      }
    }
  }

  private void throwPermissionDenied(String rowId, String userUri) throws PermissionDeniedException {
    throw new PermissionDeniedException(String.format(
        "Denied permission to access row %s to user %s", rowId, userUri));
  }

  /**
   * @return a list of all scopes which the current user is within
   */
  public List<Scope> getScopes() {
    // TODO: change to getEmail()
    String userUri = cc.getCurrentUser().getUriUser();

    List<Scope> scopes = Lists.newArrayList();
    scopes.add(new Scope(Type.DEFAULT, null));
    scopes.add(new Scope(Type.USER, userUri));

    // TODO: add this
    // List<String> groups = getGroupNames(userUri);
    // for (String group : groups)
    // {
    // scopes.add(new Scope(Type.GROUP, group));
    // }

    return scopes;
  }

  private Set<TablePermission> getPermissions(String userUri) throws ODKDatastoreException {
    Set<TablePermission> permissions = new HashSet<TablePermission>();

    // get default permissions
    TableAcl def = am.getAcl(new Scope(Type.DEFAULT, null));
    if (def != null) {
      permissions.addAll(def.getRole().getPermissions());
    }

    // get user's permissions
    TableAcl user = am.getAcl(new Scope(Type.USER, userUri));
    if (user != null) {
      permissions.addAll(user.getRole().getPermissions());
    }

    // TODO: get groups' permissions
    // List<Group> groups = ....getGroups(userUri);
    // for (Group group : groups) {
    // TableAcl group = am.getAcl(new Scope(Type.GROUP, group.getName()));
    // if (group != null)
    // {
    // roles.addAll(group.getRole().getPermissions());
    // }
    // }

    return permissions;
  }
}
