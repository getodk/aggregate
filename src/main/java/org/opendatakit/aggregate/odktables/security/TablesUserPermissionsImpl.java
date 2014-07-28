/*
 * Copyright (C) 2014 University of Washington
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

package org.opendatakit.aggregate.odktables.security;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opendatakit.aggregate.odktables.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.rest.entity.Scope;
import org.opendatakit.aggregate.odktables.rest.entity.Scope.Type;
import org.opendatakit.aggregate.odktables.rest.entity.TableRole.TablePermission;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;
import org.opendatakit.common.web.CallingContext;
import org.springframework.security.core.GrantedAuthority;

import com.google.common.collect.Lists;

public class TablesUserPermissionsImpl implements TablesUserPermissions {

  private final CallingContext cc;
  private final OdkTablesUserInfo userInfo;
  private final Map<String, AuthFilter> authFilters = new HashMap<String, AuthFilter>();


  public TablesUserPermissionsImpl(CallingContext cc, String uriUser, Set<GrantedAuthority> grants)
      throws ODKDatastoreException, PermissionDeniedException, ODKTaskLockException {
    this.cc = cc;
    this.userInfo = OdkTablesUserInfoTable.getOdkTablesUserInfo(uriUser, grants, cc);
  }

 
  public TablesUserPermissionsImpl(CallingContext cc) throws ODKDatastoreException,
      PermissionDeniedException, ODKTaskLockException {
    this(cc, cc.getCurrentUser().getUriUser(), cc.getCurrentUser().getDirectAuthorities());
  }

  /*
   * (non-Javadoc)
   *
   * @see org.opendatakit.aggregate.odktables.security.CurrentUserPermissionsIf#
   * getOdkTablesUserId()
   */
  @Override
  public String getOdkTablesUserId() {
    return userInfo.getOdkTablesUserId();
  }

  @Override
  public String getPhoneNumber() {
    return userInfo.getPhoneNumber();
  }

  @Override
  public String getXBearerCode() {
    return userInfo.getXBearerCode();
  }

  /**
   * @return a list of all scopes in which the current user participates
   */
  private List<Scope> getScopes() {
    List<Scope> scopes = Lists.newArrayList();
    scopes.add(new Scope(Type.DEFAULT, null));
    scopes.add(new Scope(Type.USER, userInfo.getOdkTablesUserId()));

    // TODO: add this
    // List<String> groups = getGroupNames(userUri);
    // for (String group : groups)
    // {
    // scopes.add(new Scope(Type.GROUP, group));
    // }

    return scopes;
  }

  private AuthFilter getAuthFilter(String appId, String tableId) throws ODKEntityNotFoundException,
      ODKDatastoreException {
    if (userInfo == null) {
      return null;
    }
    AuthFilter auth = authFilters.get(tableId);
    if (auth == null) {
      auth = new AuthFilter(appId, tableId, this, getScopes(), cc);
      authFilters.put(tableId, auth);
    }
    return auth;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.opendatakit.aggregate.odktables.security.CurrentUserPermissionsIf#
   * checkPermission(java.lang.String,
   * org.opendatakit.aggregate.odktables.rest.entity.TableRole.TablePermission)
   */
  @Override
  public void checkPermission(String appId, String tableId, TablePermission permission)
      throws ODKDatastoreException, PermissionDeniedException {
    AuthFilter authFilter = getAuthFilter(appId, tableId);
    if (authFilter != null) {
      authFilter.checkPermission(permission);
      return;
    }
    throw new PermissionDeniedException(String.format("Denied table %s permission %s to user %s",
        tableId, permission, userInfo.getOdkTablesUserId()));
  }

  /*
   * (non-Javadoc)
   *
   * @see org.opendatakit.aggregate.odktables.security.CurrentUserPermissionsIf#
   * hasPermission(java.lang.String,
   * org.opendatakit.aggregate.odktables.rest.entity.TableRole.TablePermission)
   */
  @Override
  public boolean hasPermission(String appId, String tableId, TablePermission permission)
      throws ODKDatastoreException {
    AuthFilter filter = getAuthFilter(appId, tableId);
    if (filter != null) {
      return filter.hasPermission(permission);
    }
    return false;
  }

  @Override
  public boolean hasFilterScope(String appId, String tableId, TablePermission permission,
      String rowId, Scope filterScope) throws ODKEntityNotFoundException, ODKDatastoreException {
    AuthFilter authFilter = getAuthFilter(appId, tableId);
    if (authFilter != null) {
      return authFilter.hasFilterScope(permission, rowId, filterScope);
    }
    return false;
  }
}
