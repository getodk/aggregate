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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opendatakit.aggregate.odktables.LockTemplate;
import org.opendatakit.aggregate.odktables.ODKTablesTaskLockType;
import org.opendatakit.aggregate.odktables.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.rest.entity.Scope;
import org.opendatakit.aggregate.odktables.rest.entity.Scope.Type;
import org.opendatakit.aggregate.odktables.rest.entity.TableRole.TablePermission;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;
import org.opendatakit.common.security.SecurityBeanDefs;
import org.opendatakit.common.security.SecurityUtils;
import org.opendatakit.common.security.User;
import org.opendatakit.common.security.common.GrantedAuthorityName;
import org.opendatakit.common.security.spring.RegisteredUsersTable;
import org.opendatakit.common.security.spring.UserGrantedAuthority;
import org.opendatakit.common.web.CallingContext;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.google.common.collect.Lists;

public class TablesUserPermissionsImpl implements TablesUserPermissions {

  private final CallingContext cc;
  private final RegisteredUsersTable user;
  private final OdkTablesUserInfoTable userInfo;
  private final Map<String, AuthFilter> authFilters = new HashMap<String, AuthFilter>();


  public static final boolean deleteUser(String uriUser, CallingContext cc) throws ODKDatastoreException {
    OdkTablesUserInfoTable userToDelete =
        OdkTablesUserInfoTable.getCurrentUserInfo(uriUser, cc);
    cc.getDatastore().deleteEntity(userToDelete.getEntityKey(), cc.getCurrentUser());
    // TODO: delete the ACLs for this user???
    return true;
  }

  public TablesUserPermissionsImpl(String uriUser, CallingContext cc) throws ODKDatastoreException,
      PermissionDeniedException, ODKTaskLockException {
    this.cc = cc;
    Datastore ds = cc.getDatastore();
    if ( uriUser.equals(User.ANONYMOUS_USER) ) {
      throw new PermissionDeniedException("User does not have access to ODK Tables");
    }
    user = RegisteredUsersTable.getUserByUri(uriUser, ds, cc.getCurrentUser());
    OdkTablesUserInfoTable odkTablesUserInfo = null;
    odkTablesUserInfo = OdkTablesUserInfoTable.getCurrentUserInfo(uriUser, cc);
    if (odkTablesUserInfo == null) {
      OdkTablesUserInfoTable prototype = OdkTablesUserInfoTable.assertRelation(cc);
      Set<GrantedAuthority> grants = UserGrantedAuthority.getGrantedAuthorities(uriUser, ds,
          cc.getCurrentUser());

      RoleHierarchy rh = (RoleHierarchy) cc.getBean(SecurityBeanDefs.ROLE_HIERARCHY_MANAGER);
      Collection<? extends GrantedAuthority> roles = rh.getReachableGrantedAuthorities(grants);
      if (roles.contains(new SimpleGrantedAuthority(GrantedAuthorityName.ROLE_SYNCHRONIZE_TABLES
          .name()))
          || roles.contains(new SimpleGrantedAuthority(GrantedAuthorityName.ROLE_ADMINISTER_TABLES
              .name()))) {
        //
        // Determine the external UID that will identify this user
        String externalUID = null;
        if (user.getEmail() != null) {
          externalUID = user.getEmail();
        } else if (user.getUsername() != null) {
          externalUID = SecurityUtils.USERNAME_COLON + user.getUsername();
        }
        // GAIN LOCK
        LockTemplate tablesUserPermissions = new LockTemplate(externalUID, ODKTablesTaskLockType.TABLES_USER_PERMISSION_CREATION, cc);
        try {
          tablesUserPermissions.acquire();
          // attempt to re-fetch the record.
          // If this succeeds, then we had multiple suitors; the other one beat us.
          odkTablesUserInfo = OdkTablesUserInfoTable.getCurrentUserInfo(uriUser, cc);
          if ( odkTablesUserInfo != null ) {
            userInfo = odkTablesUserInfo;
            return;
          }
          // otherwise, create a record
          odkTablesUserInfo = ds.createEntityUsingRelation(prototype, cc.getCurrentUser());
          odkTablesUserInfo.setUriUser(uriUser);
          odkTablesUserInfo.setOdkTablesUserId(externalUID);
          odkTablesUserInfo.persist(cc);
          userInfo = odkTablesUserInfo;
        } finally {
          tablesUserPermissions.release();
        }
      } else {
        throw new PermissionDeniedException("User does not have access to ODK Tables");
      }
    } else {
      userInfo = odkTablesUserInfo;
    }
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
  public boolean hasFilterScope(String appId, String tableId, TablePermission permission, String rowId, Scope filterScope) throws ODKEntityNotFoundException, ODKDatastoreException {
    AuthFilter authFilter = getAuthFilter(appId, tableId);
    if (authFilter != null) {
      return authFilter.hasFilterScope(permission, rowId, filterScope);
    }
    return false;
  }
}
