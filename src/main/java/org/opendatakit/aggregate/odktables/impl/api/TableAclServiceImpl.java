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

import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.opendatakit.aggregate.odktables.TableAclManager;
import org.opendatakit.aggregate.odktables.TableAclManager.WebsafeAcls;
import org.opendatakit.aggregate.odktables.api.OdkTables;
import org.opendatakit.aggregate.odktables.api.TableAclService;
import org.opendatakit.aggregate.odktables.api.TableService;
import org.opendatakit.aggregate.odktables.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.rest.ApiConstants;
import org.opendatakit.aggregate.odktables.rest.entity.Scope;
import org.opendatakit.aggregate.odktables.rest.entity.TableAcl;
import org.opendatakit.aggregate.odktables.rest.entity.TableAclResource;
import org.opendatakit.aggregate.odktables.rest.entity.TableAclResourceList;
import org.opendatakit.aggregate.odktables.security.TablesUserPermissions;
import org.opendatakit.common.persistence.QueryResumePoint;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.security.common.GrantedAuthorityName;
import org.opendatakit.common.security.server.SecurityServiceUtil;
import org.opendatakit.common.utils.WebUtils;
import org.opendatakit.common.web.CallingContext;

public class TableAclServiceImpl implements TableAclService {

  private final TableAclManager am;
  private final UriInfo info;
  private final CallingContext cc;

  public TableAclServiceImpl(String appId, String tableId, UriInfo info, TablesUserPermissions userPermissions, CallingContext cc)
      throws ODKEntityNotFoundException, ODKDatastoreException {
    this.am = new TableAclManager(appId, tableId, userPermissions, cc);
    this.info = info;
    this.cc = cc;
  }

  @Override
  public Response getAcls(@QueryParam(CURSOR_PARAMETER) String cursor, @QueryParam(FETCH_LIMIT) String fetchLimit) throws ODKDatastoreException, PermissionDeniedException {
    int limit = (fetchLimit == null || fetchLimit.length() == 0) ? 2000 : Integer.valueOf(fetchLimit);
    WebsafeAcls websafeResult = am.getAcls(QueryResumePoint.fromWebsafeCursor(WebUtils.safeDecode(cursor)), limit);
    TableAclResourceList list = new TableAclResourceList(getResources(websafeResult.acls),
        WebUtils.safeEncode(websafeResult.websafeRefetchCursor),
        WebUtils.safeEncode(websafeResult.websafeBackwardCursor),
        WebUtils.safeEncode(websafeResult.websafeResumeCursor),
        websafeResult.hasMore, websafeResult.hasPrior);
     return Response.ok(list)
         .header(ApiConstants.OPEN_DATA_KIT_VERSION_HEADER, ApiConstants.OPEN_DATA_KIT_VERSION)
         .header("Access-Control-Allow-Origin", "*")
         .header("Access-Control-Allow-Credentials", "true").build();
  }

  @Override
  public Response getUserAcls(@QueryParam(CURSOR_PARAMETER) String cursor, @QueryParam(FETCH_LIMIT) String fetchLimit) throws ODKDatastoreException,
      PermissionDeniedException {
    int limit = (fetchLimit == null || fetchLimit.length() == 0) ? 2000 : Integer.valueOf(fetchLimit);
    WebsafeAcls websafeResult = am.getAcls(Scope.Type.USER, QueryResumePoint.fromWebsafeCursor(WebUtils.safeDecode(cursor)), limit);
    TableAclResourceList list = new TableAclResourceList(getResources(websafeResult.acls),
        WebUtils.safeEncode(websafeResult.websafeRefetchCursor),
        WebUtils.safeEncode(websafeResult.websafeBackwardCursor),
        WebUtils.safeEncode(websafeResult.websafeResumeCursor),
        websafeResult.hasMore, websafeResult.hasPrior);
    return Response.ok(list)
        .header(ApiConstants.OPEN_DATA_KIT_VERSION_HEADER, ApiConstants.OPEN_DATA_KIT_VERSION)
        .header("Access-Control-Allow-Origin", "*")
        .header("Access-Control-Allow-Credentials", "true").build();
  }

  @Override
  public Response getGroupAcls(@QueryParam(CURSOR_PARAMETER) String cursor, @QueryParam(FETCH_LIMIT) String fetchLimit) throws ODKDatastoreException,
      PermissionDeniedException {
    int limit = (fetchLimit == null || fetchLimit.length() == 0) ? 2000 : Integer.valueOf(fetchLimit);
    WebsafeAcls websafeResult = am.getAcls(Scope.Type.GROUP, QueryResumePoint.fromWebsafeCursor(WebUtils.safeDecode(cursor)), limit);
    TableAclResourceList list = new TableAclResourceList(getResources(websafeResult.acls),
        WebUtils.safeEncode(websafeResult.websafeRefetchCursor),
        WebUtils.safeEncode(websafeResult.websafeBackwardCursor),
        WebUtils.safeEncode(websafeResult.websafeResumeCursor),
        websafeResult.hasMore, websafeResult.hasPrior);
    return Response.ok(list)
        .header(ApiConstants.OPEN_DATA_KIT_VERSION_HEADER, ApiConstants.OPEN_DATA_KIT_VERSION)
        .header("Access-Control-Allow-Origin", "*")
        .header("Access-Control-Allow-Credentials", "true").build();
  }

  @Override
  public Response getUserAcl(String odkTablesUserId) throws ODKDatastoreException,
      PermissionDeniedException {
    if (odkTablesUserId.equals("null")) {
      odkTablesUserId = null;
    }
    TableAcl acl = am.getAcl(new Scope(Scope.Type.USER, odkTablesUserId));
    TableAclResource resource = getResource(acl);
    return Response.ok(resource)
        .header(ApiConstants.OPEN_DATA_KIT_VERSION_HEADER, ApiConstants.OPEN_DATA_KIT_VERSION)
        .header("Access-Control-Allow-Origin", "*")
        .header("Access-Control-Allow-Credentials", "true").build();
  }

  @Override
  public Response getGroupAcl(String groupId) throws ODKDatastoreException,
      PermissionDeniedException {
    TableAcl acl = am.getAcl(new Scope(Scope.Type.GROUP, groupId));
    TableAclResource resource = getResource(acl);
    return Response.ok(resource)
        .header(ApiConstants.OPEN_DATA_KIT_VERSION_HEADER, ApiConstants.OPEN_DATA_KIT_VERSION)
        .header("Access-Control-Allow-Origin", "*")
        .header("Access-Control-Allow-Credentials", "true").build();
  }

  @Override
  public Response getDefaultAcl() throws ODKDatastoreException, PermissionDeniedException {
    TableAcl acl = am.getAcl(new Scope(Scope.Type.DEFAULT, null));
    TableAclResource resource = getResource(acl);
    return Response.ok(resource)
        .header(ApiConstants.OPEN_DATA_KIT_VERSION_HEADER, ApiConstants.OPEN_DATA_KIT_VERSION)
        .header("Access-Control-Allow-Origin", "*")
        .header("Access-Control-Allow-Credentials", "true").build();
  }

  @Override
  public Response setUserAcl(String odkTablesUserId, TableAcl acl) throws ODKDatastoreException,
      PermissionDeniedException {

    TreeSet<GrantedAuthorityName> ui = SecurityServiceUtil.getCurrentUserSecurityInfo(cc);
    if ( !ui.contains(GrantedAuthorityName.ROLE_ADMINISTER_TABLES) ) {
      throw new PermissionDeniedException("User does not belong to the 'Administer Tables' group");
    }

    if (odkTablesUserId.equals("null"))
      odkTablesUserId = null;
    acl = am.setAcl(new Scope(Scope.Type.USER, odkTablesUserId), acl.getRole());
    TableAclResource resource = getResource(acl);
    return Response.ok(resource)
        .header(ApiConstants.OPEN_DATA_KIT_VERSION_HEADER, ApiConstants.OPEN_DATA_KIT_VERSION)
        .header("Access-Control-Allow-Origin", "*")
        .header("Access-Control-Allow-Credentials", "true").build();
  }

  @Override
  public Response setGroupAcl(String groupId, TableAcl acl) throws ODKDatastoreException,
      PermissionDeniedException {

    TreeSet<GrantedAuthorityName> ui = SecurityServiceUtil.getCurrentUserSecurityInfo(cc);
    if ( !ui.contains(GrantedAuthorityName.ROLE_ADMINISTER_TABLES) ) {
      throw new PermissionDeniedException("User does not belong to the 'Administer Tables' group");
    }

    acl = am.setAcl(new Scope(Scope.Type.GROUP, groupId), acl.getRole());
    TableAclResource resource = getResource(acl);
    return Response.ok(resource)
        .header(ApiConstants.OPEN_DATA_KIT_VERSION_HEADER, ApiConstants.OPEN_DATA_KIT_VERSION)
        .header("Access-Control-Allow-Origin", "*")
        .header("Access-Control-Allow-Credentials", "true").build();
  }

  @Override
  public Response setDefaultAcl(TableAcl acl) throws ODKDatastoreException,
      PermissionDeniedException {

    TreeSet<GrantedAuthorityName> ui = SecurityServiceUtil.getCurrentUserSecurityInfo(cc);
    if ( !ui.contains(GrantedAuthorityName.ROLE_ADMINISTER_TABLES) ) {
      throw new PermissionDeniedException("User does not belong to the 'Administer Tables' group");
    }

    acl = am.setAcl(new Scope(Scope.Type.DEFAULT, null), acl.getRole());
    TableAclResource resource = getResource(acl);
    return Response.ok(resource)
        .header(ApiConstants.OPEN_DATA_KIT_VERSION_HEADER, ApiConstants.OPEN_DATA_KIT_VERSION)
        .header("Access-Control-Allow-Origin", "*")
        .header("Access-Control-Allow-Credentials", "true").build();
  }

  @Override
  public Response deleteDefaultAcl() throws ODKDatastoreException, PermissionDeniedException {

    TreeSet<GrantedAuthorityName> ui = SecurityServiceUtil.getCurrentUserSecurityInfo(cc);
    if ( !ui.contains(GrantedAuthorityName.ROLE_ADMINISTER_TABLES) ) {
      throw new PermissionDeniedException("User does not belong to the 'Administer Tables' group");
    }

    am.deleteAcl(new Scope(Scope.Type.DEFAULT, null));
    return Response.ok()
        .header(ApiConstants.OPEN_DATA_KIT_VERSION_HEADER, ApiConstants.OPEN_DATA_KIT_VERSION)
        .header("Access-Control-Allow-Origin", "*")
        .header("Access-Control-Allow-Credentials", "true").build();
  }

  @Override
  public Response deleteUserAcl(String odkTablesUserId) throws ODKDatastoreException, PermissionDeniedException {

    TreeSet<GrantedAuthorityName> ui = SecurityServiceUtil.getCurrentUserSecurityInfo(cc);
    if ( !ui.contains(GrantedAuthorityName.ROLE_ADMINISTER_TABLES) ) {
      throw new PermissionDeniedException("User does not belong to the 'Administer Tables' group");
    }

    am.deleteAcl(new Scope(Scope.Type.USER, odkTablesUserId));
    return Response.ok()
        .header(ApiConstants.OPEN_DATA_KIT_VERSION_HEADER, ApiConstants.OPEN_DATA_KIT_VERSION)
        .header("Access-Control-Allow-Origin", "*")
        .header("Access-Control-Allow-Credentials", "true").build();
  }

  @Override
  public Response deleteGroupAcl(String groupId) throws ODKDatastoreException,
      PermissionDeniedException {

    TreeSet<GrantedAuthorityName> ui = SecurityServiceUtil.getCurrentUserSecurityInfo(cc);
    if ( !ui.contains(GrantedAuthorityName.ROLE_ADMINISTER_TABLES) ) {
      throw new PermissionDeniedException("User does not belong to the 'Administer Tables' group");
    }

    am.deleteAcl(new Scope(Scope.Type.GROUP, groupId));
    return Response.ok()
        .header(ApiConstants.OPEN_DATA_KIT_VERSION_HEADER, ApiConstants.OPEN_DATA_KIT_VERSION)
        .header("Access-Control-Allow-Origin", "*")
        .header("Access-Control-Allow-Credentials", "true").build();
  }

  private TableAclResource getResource(TableAcl acl) {
    String appId = am.getAppId();
    String tableId = am.getTableId();
    Scope.Type type = acl.getScope().getType();
    String value = acl.getScope().getValue();
    if (value == null)
      value = "null";

    UriBuilder ub = info.getBaseUriBuilder();
    ub.path(OdkTables.class, "getTablesService");
    UriBuilder selfBuilder = ub.clone().path(TableService.class, "getAcl");
    URI self;
    switch (type) {
    case USER:
      self = selfBuilder.path(TableAclService.class, "getUserAcl").build(appId, tableId, value);
      break;
    case GROUP:
      self = selfBuilder.path(TableAclService.class, "getGroupAcl").build(appId, tableId, value);
      break;
    case DEFAULT:
    default:
      self = selfBuilder.path(TableAclService.class, "getDefaultAcl").build(appId, tableId);
      break;
    }
    URI acls = ub.clone().path(TableService.class, "getAcl").build(appId, tableId);
    URI table = ub.clone().build(appId, tableId);

    TableAclResource resource = new TableAclResource(acl);
    try {
      resource.setSelfUri(self.toURL().toExternalForm());
      resource.setAclUri(acls.toURL().toExternalForm());
      resource.setTableUri(table.toURL().toExternalForm());
    } catch (MalformedURLException e) {
      e.printStackTrace();
      throw new IllegalArgumentException("unable to convert to URL");
    }
    return resource;
  }

  private ArrayList<TableAclResource> getResources(List<TableAcl> acls) {
    ArrayList<TableAclResource> resources = new ArrayList<TableAclResource>();
    for (TableAcl acl : acls) {
      resources.add(getResource(acl));
    }
    return resources;
  }

}
