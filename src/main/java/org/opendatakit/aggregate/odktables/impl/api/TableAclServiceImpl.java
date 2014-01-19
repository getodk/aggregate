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

package org.opendatakit.aggregate.odktables.impl.api;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.opendatakit.aggregate.odktables.TableAclManager;
import org.opendatakit.aggregate.odktables.api.TableAclService;
import org.opendatakit.aggregate.odktables.api.TableService;
import org.opendatakit.aggregate.odktables.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.rest.entity.Scope;
import org.opendatakit.aggregate.odktables.rest.entity.TableAcl;
import org.opendatakit.aggregate.odktables.rest.entity.TableAclResource;
import org.opendatakit.aggregate.odktables.rest.entity.TableAclResourceList;
import org.opendatakit.aggregate.odktables.security.TablesUserPermissions;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.web.CallingContext;

public class TableAclServiceImpl implements TableAclService {

  private TableAclManager am;
  private UriInfo info;

  public TableAclServiceImpl(String tableId, UriInfo info, TablesUserPermissions userPermissions, CallingContext cc)
      throws ODKEntityNotFoundException, ODKDatastoreException {
    this.am = new TableAclManager(tableId, userPermissions, cc);
    this.info = info;
  }

  @Override
  public TableAclResourceList getAcls() throws ODKDatastoreException, PermissionDeniedException {
    List<TableAcl> acls = am.getAcls();
    return new TableAclResourceList(getResources(acls));
  }

  @Override
  public TableAclResourceList getUserAcls() throws ODKDatastoreException,
      PermissionDeniedException {
    List<TableAcl> acls = am.getAcls(Scope.Type.USER);
    return new TableAclResourceList(getResources(acls));
  }

  @Override
  public TableAclResourceList getGroupAcls() throws ODKDatastoreException,
      PermissionDeniedException {
    List<TableAcl> acls = am.getAcls(Scope.Type.GROUP);
    return new TableAclResourceList(getResources(acls));
  }

  @Override
  public TableAclResource getUserAcl(String odkTablesUserId) throws ODKDatastoreException,
      PermissionDeniedException {
    if (odkTablesUserId.equals("null")) {
      odkTablesUserId = null;
    }
    TableAcl acl = am.getAcl(new Scope(Scope.Type.USER, odkTablesUserId));
    return getResource(acl);
  }

  @Override
  public TableAclResource getGroupAcl(String groupId) throws ODKDatastoreException,
      PermissionDeniedException {
    TableAcl acl = am.getAcl(new Scope(Scope.Type.GROUP, groupId));
    return getResource(acl);
  }

  @Override
  public TableAclResource getDefaultAcl() throws ODKDatastoreException, PermissionDeniedException {
    TableAcl acl = am.getAcl(new Scope(Scope.Type.DEFAULT, null));
    return getResource(acl);
  }

  @Override
  public TableAclResource setUserAcl(String odkTablesUserId, TableAcl acl) throws ODKDatastoreException,
      PermissionDeniedException {
    if (odkTablesUserId.equals("null"))
      odkTablesUserId = null;
    acl = am.setAcl(new Scope(Scope.Type.USER, odkTablesUserId), acl.getRole());
    return getResource(acl);
  }

  @Override
  public TableAclResource setGroupAcl(String groupId, TableAcl acl) throws ODKDatastoreException,
      PermissionDeniedException {
    acl = am.setAcl(new Scope(Scope.Type.GROUP, groupId), acl.getRole());
    return getResource(acl);
  }

  @Override
  public TableAclResource setDefaultAcl(TableAcl acl) throws ODKDatastoreException,
      PermissionDeniedException {
    acl = am.setAcl(new Scope(Scope.Type.DEFAULT, null), acl.getRole());
    return getResource(acl);
  }

  @Override
  public void deleteDefaultAcl() throws ODKDatastoreException, PermissionDeniedException {
    am.deleteAcl(new Scope(Scope.Type.DEFAULT, null));
  }

  @Override
  public void deleteUserAcl(String odkTablesUserId) throws ODKDatastoreException, PermissionDeniedException {
    am.deleteAcl(new Scope(Scope.Type.USER, odkTablesUserId));
  }

  @Override
  public void deleteGroupAcl(String groupId) throws ODKDatastoreException,
      PermissionDeniedException {
    am.deleteAcl(new Scope(Scope.Type.USER, groupId));
  }

  private TableAclResource getResource(TableAcl acl) {
    String tableId = am.getTableId();
    Scope.Type type = acl.getScope().getType();
    String value = acl.getScope().getValue();
    if (value == null)
      value = "null";

    UriBuilder ub = info.getBaseUriBuilder();
    ub.path(TableService.class);
    UriBuilder selfBuilder = ub.clone().path(TableService.class, "getAcl");
    URI self;
    switch (type) {
    case USER:
      self = selfBuilder.path(TableAclService.class, "getUserAcl").build(tableId, value);
      break;
    case GROUP:
      self = selfBuilder.path(TableAclService.class, "getGroupAcl").build(tableId, value);
      break;
    case DEFAULT:
    default:
      self = selfBuilder.path(TableAclService.class, "getDefaultAcl").build(tableId);
      break;
    }
    URI acls = ub.clone().path(TableService.class, "getAcl").build(tableId);
    URI table = ub.clone().path(TableService.class, "getTable").build(tableId);

    TableAclResource resource = new TableAclResource(acl);
    resource.setSelfUri(self.toASCIIString());
    resource.setAclUri(acls.toASCIIString());
    resource.setTableUri(table.toASCIIString());
    return resource;
  }

  private List<TableAclResource> getResources(List<TableAcl> acls) {
    List<TableAclResource> resources = new ArrayList<TableAclResource>();
    for (TableAcl acl : acls) {
      resources.add(getResource(acl));
    }
    return resources;
  }

}
