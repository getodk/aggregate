/*
 * Copyright (C) 2012-2013 University of Washington
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

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.Validate;
import org.opendatakit.aggregate.odktables.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.rest.entity.Scope;
import org.opendatakit.aggregate.odktables.rest.entity.Scope.Type;
import org.opendatakit.aggregate.odktables.rest.entity.TableAcl;
import org.opendatakit.aggregate.odktables.rest.entity.TableRole.TablePermission;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.web.CallingContext;

public class AuthFilter {

  private CallingContext cc;
  private TableAclManager am;
  private OdkTablesUserInfoTable userInfo;

  public AuthFilter(String tableId, OdkTablesUserInfoTable userInfo, CallingContext cc) throws ODKEntityNotFoundException,
      ODKDatastoreException {
    this.cc = cc;
    this.userInfo = userInfo;
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
      throw new PermissionDeniedException(String.format("Denied permission %s to user %s",
          permission, userInfo.getOdkTablesUserId()));
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
    Set<TablePermission> permissions = getPermissions(userInfo.getOdkTablesUserId());
    return true;
//    return permissions.contains(permission);
  }

  /**
   * Check that the user either has the given permission or is within the scope
   * of the filter on the given row.
   *
   * In other words, if the user has the given permission, then they pass the
   * check and the method returns. However, if the user does not have the given
   * permission, then they must fall within the scope of the filter on the given
   * row.
   *
   * @param permission
   *          the permission that guards access to the row. Should be one of
   *          {@link TablePermission#UNFILTERED_READ},
   *          {@link TablePermission#UNFILTERED_WRITE}, or
   *          {@link TablePermission#UNFILTERED_DELETE}.
   * @param row
   *          the row to check
   * @throws ODKDatastoreException
   * @throws PermissionDeniedException
   *           if the current user does not have the given permission and is not
   *           within the scope of the filter on the row
   */
  public void checkFilter(TablePermission permission, String rowId, Scope filter)
      throws ODKDatastoreException, PermissionDeniedException {
    Validate.notNull(permission);
    Validate.notNull(rowId);

    Set<TablePermission> permissions = getPermissions(userInfo.getOdkTablesUserId());

    if (!permissions.contains(permission)) {
      if (filter == null || filter.equals(Scope.EMPTY_SCOPE)) {
        // empty scope, no one allowed
        throwPermissionDenied(rowId, userInfo);
      }
      switch (filter.getType()) {
      case USER:
        String filterUser = filter.getValue();
        if (userInfo == null && filterUser != null) {
          throwPermissionDenied(rowId, userInfo);
        } else if (userInfo != null && !userInfo.getOdkTablesUserId().equals(filter.getValue())) {
          throwPermissionDenied(rowId, userInfo);
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

  private void throwPermissionDenied(String rowId, OdkTablesUserInfoTable userInfo) throws PermissionDeniedException {
    throw new PermissionDeniedException(String.format(
        "Denied permission to access row %s to user %s", rowId, userInfo.getOdkTablesUserId()));
  }

  private Set<TablePermission> getPermissions(String odkTablesUserId) throws ODKDatastoreException {
    Set<TablePermission> permissions = new HashSet<TablePermission>();

    // get default permissions
    TableAcl def = am.getAcl(new Scope(Type.DEFAULT, null));
    if (def != null) {
      permissions.addAll(def.getRole().getPermissions());
    }

    // get user's permissions
    TableAcl user = am.getAcl(new Scope(Type.USER, odkTablesUserId));
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
