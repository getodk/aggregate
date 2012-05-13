package org.opendatakit.aggregate.odktables.impl.api;

import java.util.HashSet;
import java.util.Set;

import org.opendatakit.aggregate.odktables.TableAclManager;
import org.opendatakit.aggregate.odktables.entity.Scope;
import org.opendatakit.aggregate.odktables.entity.Scope.Type;
import org.opendatakit.aggregate.odktables.entity.TableAcl;
import org.opendatakit.aggregate.odktables.entity.TableRole.TablePermission;
import org.opendatakit.aggregate.odktables.exception.PermissionDeniedException;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.web.CallingContext;

public class AuthFilter {

  private CallingContext cc;
  private TableAclManager am;

  public AuthFilter(String tableId, CallingContext cc) throws ODKEntityNotFoundException,
      ODKDatastoreException {
    this.cc = cc;
    this.am = new TableAclManager(tableId, cc);
  }

  public void checkPermission(TablePermission permission) throws ODKDatastoreException,
      PermissionDeniedException {
    // TODO: change to getEmail()
    String userUri = cc.getCurrentUser().getUriUser();

    Set<TablePermission> permissions = new HashSet<TablePermission>();

    // get default permissions
    TableAcl def = am.getAcl(new Scope(Type.DEFAULT, null));
    permissions.addAll(def.getRole().getPermissions());

    // get user's permissions
    TableAcl user = am.getAcl(new Scope(Type.USER, userUri));
    permissions.addAll(user.getRole().getPermissions());

    // TODO: get groups' permissions
    // List<Group> groups = ....getGroups(userUri);
    // for (Group group : groups) {
    // TableAcl group = am.getAcl(new Scope(Type.GROUP, group.getName()));
    // roles.addAll(group.getRole().getPermissions());
    // }

    if (!permissions.contains(permission)) {
      throw new PermissionDeniedException(String.format("Denied permission %s to user %s",
          permission, userUri));
    }
  }
}
