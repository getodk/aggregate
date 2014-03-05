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

package org.opendatakit.aggregate.server;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.client.exception.PermissionDeniedExceptionClient;
import org.opendatakit.aggregate.client.exception.RequestFailureException;
import org.opendatakit.aggregate.client.odktables.ServerTableACLService;
import org.opendatakit.aggregate.client.odktables.TableAclClient;
import org.opendatakit.aggregate.client.odktables.TableAclResourceClient;
import org.opendatakit.aggregate.client.odktables.TableRoleClient;
import org.opendatakit.aggregate.odktables.TableAclManager;
import org.opendatakit.aggregate.odktables.api.TableAclService;
import org.opendatakit.aggregate.odktables.api.TableService;
import org.opendatakit.aggregate.odktables.entity.UtilTransforms;
import org.opendatakit.aggregate.odktables.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.rest.entity.Scope;
import org.opendatakit.aggregate.odktables.rest.entity.TableAcl;
import org.opendatakit.aggregate.odktables.rest.entity.TableAclResource;
import org.opendatakit.aggregate.odktables.rest.entity.TableRole;
import org.opendatakit.aggregate.odktables.security.TablesUserPermissionsImpl;
import org.opendatakit.aggregate.odktables.security.TablesUserPermissions;
import org.opendatakit.common.persistence.client.exception.DatastoreFailureException;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;
import org.opendatakit.common.security.client.exception.AccessDeniedException;
import org.opendatakit.common.web.CallingContext;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class ServerTableACLServiceImpl extends RemoteServiceServlet implements
    ServerTableACLService {

  /**
	 *
	 */
  private static final long serialVersionUID = 9023285673934784466L;

  @Override
  public ArrayList<TableAclClient> getAcls(String tableId) throws AccessDeniedException,
      RequestFailureException, DatastoreFailureException, PermissionDeniedExceptionClient {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);
    try {
      TablesUserPermissions userPermissions = new TablesUserPermissionsImpl(cc.getCurrentUser()
          .getUriUser(), cc);
      String appId = ServerPreferencesProperties.getOdkTablesAppId(cc);
      TableAclManager am = new TableAclManager(appId, tableId, userPermissions, cc);
      List<TableAcl> acls = am.getAcls();
      return transformTableAclList(acls);
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      throw new DatastoreFailureException(e);
    } catch (ODKTaskLockException e) {
      e.printStackTrace();
      throw new RequestFailureException(e);
    } catch (PermissionDeniedException e) {
      e.printStackTrace();
      throw new PermissionDeniedExceptionClient(e);
    }
  }

  @Override
  public ArrayList<TableAclClient> getUserAcls(String tableId) throws AccessDeniedException,
      RequestFailureException, DatastoreFailureException, PermissionDeniedExceptionClient {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);
    try {
      TablesUserPermissions userPermissions = new TablesUserPermissionsImpl(cc.getCurrentUser()
          .getUriUser(), cc);
      String appId = ServerPreferencesProperties.getOdkTablesAppId(cc);
      TableAclManager am = new TableAclManager(appId, tableId, userPermissions, cc);
      List<TableAcl> acls = am.getAcls(Scope.Type.USER);
      return transformTableAclList(acls);
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      throw new DatastoreFailureException(e);
    } catch (ODKTaskLockException e) {
      e.printStackTrace();
      throw new RequestFailureException(e);
    } catch (PermissionDeniedException e) {
      e.printStackTrace();
      throw new PermissionDeniedExceptionClient(e);
    }
  }

  @Override
  public ArrayList<TableAclClient> getGroupAcls(String tableId) throws AccessDeniedException,
      RequestFailureException, PermissionDeniedExceptionClient, DatastoreFailureException {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);
    try {
      TablesUserPermissions userPermissions = new TablesUserPermissionsImpl(cc.getCurrentUser()
          .getUriUser(), cc);
      String appId = ServerPreferencesProperties.getOdkTablesAppId(cc);
      TableAclManager am = new TableAclManager(appId, tableId, userPermissions, cc);
      List<TableAcl> acls = am.getAcls(Scope.Type.GROUP);
      return transformTableAclList(acls);
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      throw new DatastoreFailureException(e);
    } catch (ODKTaskLockException e) {
      e.printStackTrace();
      throw new RequestFailureException(e);
    } catch (PermissionDeniedException e) {
      e.printStackTrace();
      throw new PermissionDeniedExceptionClient(e);
    }
  }

  @Override
  public TableAclClient getDefaultAcl(String tableId) throws AccessDeniedException,
      RequestFailureException, DatastoreFailureException, PermissionDeniedExceptionClient {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);
    try {
      TablesUserPermissions userPermissions = new TablesUserPermissionsImpl(cc.getCurrentUser()
          .getUriUser(), cc);
      String appId = ServerPreferencesProperties.getOdkTablesAppId(cc);
      TableAclManager am = new TableAclManager(appId, tableId, userPermissions, cc);
      TableAcl acl = am.getAcl(new Scope(Scope.Type.DEFAULT, null));
      return UtilTransforms.transform(acl);
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      throw new DatastoreFailureException(e);
    } catch (ODKTaskLockException e) {
      e.printStackTrace();
      throw new RequestFailureException(e);
    } catch (PermissionDeniedException e) {
      e.printStackTrace();
      throw new PermissionDeniedExceptionClient(e);
    }
  }

  @Override
  public TableAclClient getUserAcl(String odkTablesUserId, String tableId)
      throws AccessDeniedException, RequestFailureException, DatastoreFailureException,
      PermissionDeniedExceptionClient {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);
    try {
      TablesUserPermissions userPermissions = new TablesUserPermissionsImpl(cc.getCurrentUser()
          .getUriUser(), cc);
      String appId = ServerPreferencesProperties.getOdkTablesAppId(cc);
      TableAclManager am = new TableAclManager(appId, tableId, userPermissions, cc);
      if (odkTablesUserId.equals("null"))
        odkTablesUserId = null;
      TableAcl acl = am.getAcl(new Scope(Scope.Type.USER, odkTablesUserId));
      return UtilTransforms.transform(acl);
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      throw new DatastoreFailureException(e);
    } catch (ODKTaskLockException e) {
      e.printStackTrace();
      throw new RequestFailureException(e);
    } catch (PermissionDeniedException e) {
      e.printStackTrace();
      throw new PermissionDeniedExceptionClient(e);
    }
  }

  @Override
  public TableAclClient getGroupAcl(String groupId, String tableId) throws AccessDeniedException,
      RequestFailureException, DatastoreFailureException, PermissionDeniedExceptionClient {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);
    try {
      TablesUserPermissions userPermissions = new TablesUserPermissionsImpl(cc.getCurrentUser()
          .getUriUser(), cc);
      String appId = ServerPreferencesProperties.getOdkTablesAppId(cc);
      TableAclManager am = new TableAclManager(appId, tableId, userPermissions, cc);
      TableAcl acl = am.getAcl(new Scope(Scope.Type.GROUP, groupId));
      return UtilTransforms.transform(acl);
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      throw new DatastoreFailureException(e);
    } catch (ODKTaskLockException e) {
      e.printStackTrace();
      throw new RequestFailureException(e);
    } catch (PermissionDeniedException e) {
      e.printStackTrace();
      throw new PermissionDeniedExceptionClient(e);
    }
  }

  @Override
  public TableAclClient setDefaultAcl(TableAclClient acl, String tableId)
      throws AccessDeniedException, RequestFailureException, DatastoreFailureException,
      PermissionDeniedExceptionClient {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);
    try {
      TablesUserPermissions userPermissions = new TablesUserPermissionsImpl(cc.getCurrentUser()
          .getUriUser(), cc);
      String appId = ServerPreferencesProperties.getOdkTablesAppId(cc);
      TableAclManager am = new TableAclManager(appId, tableId, userPermissions, cc);
      acl = UtilTransforms.transform(am.setAcl(new Scope(Scope.Type.DEFAULT, null),
          this.transformTableRoleClient(acl.getRole())));
      // Need to be careful here. A lot of transforming going on,
      // and it isn't clear on if the acl parameter is passed in
      // to be modified in place. Need to be very careful about this.
      // Similar worry in another method in one of these service impls.
      return acl;
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      throw new DatastoreFailureException(e);
    } catch (ODKTaskLockException e) {
      e.printStackTrace();
      throw new RequestFailureException(e);
    } catch (PermissionDeniedException e) {
      e.printStackTrace();
      throw new PermissionDeniedExceptionClient(e);
    }
  }

  @Override
  public TableAclClient setUserAcl(String odkTablesUserId, TableAclClient acl, String tableId)
      throws AccessDeniedException, RequestFailureException, DatastoreFailureException,
      PermissionDeniedExceptionClient {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);
    try {
      TablesUserPermissions userPermissions = new TablesUserPermissionsImpl(cc.getCurrentUser()
          .getUriUser(), cc);
      String appId = ServerPreferencesProperties.getOdkTablesAppId(cc);
      TableAclManager am = new TableAclManager(appId, tableId, userPermissions, cc);
      if (odkTablesUserId.equals("null"))
        odkTablesUserId = null;
      acl = UtilTransforms.transform(am.setAcl(new Scope(Scope.Type.USER, odkTablesUserId),
          this.transformTableRoleClient(acl.getRole())));
      // Need to be careful here. A lot of transforming going on,
      // and it isn't clear on if the acl parameter is passed in
      // to be modified in place. Need to be very careful about this.
      // Similar worry in another method in one of these service impls.
      return acl;
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      throw new DatastoreFailureException(e);
    } catch (ODKTaskLockException e) {
      e.printStackTrace();
      throw new RequestFailureException(e);
    } catch (PermissionDeniedException e) {
      e.printStackTrace();
      throw new PermissionDeniedExceptionClient(e);
    }
  }

  @Override
  public TableAclClient setGroupAcl(String groupId, TableAclClient acl, String tableId)
      throws AccessDeniedException, RequestFailureException, DatastoreFailureException,
      PermissionDeniedExceptionClient {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);
    try {
      TablesUserPermissions userPermissions = new TablesUserPermissionsImpl(cc.getCurrentUser()
          .getUriUser(), cc);
      String appId = ServerPreferencesProperties.getOdkTablesAppId(cc);
      TableAclManager am = new TableAclManager(appId, tableId, userPermissions, cc);
      acl = UtilTransforms.transform(am.setAcl(new Scope(Scope.Type.GROUP, groupId),
          this.transformTableRoleClient(acl.getRole())));
      // Need to be careful here. A lot of transforming going on,
      // and it isn't clear on if the acl parameter is passed in
      // to be modified in place. Need to be very careful about this.
      // Similar worry in another method in one of these service impls.
      return acl;
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      throw new DatastoreFailureException(e);
    } catch (ODKTaskLockException e) {
      e.printStackTrace();
      throw new RequestFailureException(e);
    } catch (PermissionDeniedException e) {
      e.printStackTrace();
      throw new PermissionDeniedExceptionClient(e);
    }
  }

  @Override
  public void deleteDefaultAcl(String tableId) throws AccessDeniedException,
      RequestFailureException, DatastoreFailureException, PermissionDeniedExceptionClient {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);
    try {
      TablesUserPermissions userPermissions = new TablesUserPermissionsImpl(cc.getCurrentUser()
          .getUriUser(), cc);
      String appId = ServerPreferencesProperties.getOdkTablesAppId(cc);
      TableAclManager am = new TableAclManager(appId, tableId, userPermissions, cc);
      am.deleteAcl(new Scope(Scope.Type.DEFAULT, null));
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      throw new DatastoreFailureException(e);
    } catch (ODKTaskLockException e) {
      e.printStackTrace();
      throw new RequestFailureException(e);
    } catch (PermissionDeniedException e) {
      e.printStackTrace();
      throw new PermissionDeniedExceptionClient(e);
    }
  }

  @Override
  public void deleteUserAcl(String odkTablesUserId, String tableId) throws AccessDeniedException,
      RequestFailureException, DatastoreFailureException, PermissionDeniedExceptionClient {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);
    try {
      TablesUserPermissions userPermissions = new TablesUserPermissionsImpl(cc.getCurrentUser()
          .getUriUser(), cc);
      String appId = ServerPreferencesProperties.getOdkTablesAppId(cc);
      TableAclManager am = new TableAclManager(appId, tableId, userPermissions, cc);
      am.deleteAcl(new Scope(Scope.Type.USER, odkTablesUserId));
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      throw new DatastoreFailureException(e);
    } catch (ODKTaskLockException e) {
      e.printStackTrace();
      throw new RequestFailureException(e);
    } catch (PermissionDeniedException e) {
      e.printStackTrace();
      throw new PermissionDeniedExceptionClient(e);
    }
  }

  @Override
  public void deleteGroupAcl(String groupId, String tableId) throws AccessDeniedException,
      RequestFailureException, DatastoreFailureException, PermissionDeniedExceptionClient {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);
    try {
      TablesUserPermissions userPermissions = new TablesUserPermissionsImpl(cc.getCurrentUser()
          .getUriUser(), cc);
      String appId = ServerPreferencesProperties.getOdkTablesAppId(cc);
      TableAclManager am = new TableAclManager(appId, tableId, userPermissions, cc);
      am.deleteAcl(new Scope(Scope.Type.USER, groupId));
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      throw new DatastoreFailureException(e);
    } catch (ODKTaskLockException e) {
      e.printStackTrace();
      throw new RequestFailureException(e);
    } catch (PermissionDeniedException e) {
      e.printStackTrace();
      throw new PermissionDeniedExceptionClient(e);
    }
  }

  private TableAclResourceClient getResource(TableAcl acl, TableAclManager am, UriInfo info) {
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
    return UtilTransforms.transform(resource);
  }

  private ArrayList<TableAclResourceClient> getResources(List<TableAcl> acls, TableAclManager am,
      UriInfo info) {
    ArrayList<TableAclResourceClient> resources = new ArrayList<TableAclResourceClient>();
    for (TableAcl acl : acls) {
      resources.add(getResource(acl, am, info));
    }
    return resources;
  }

  /*
   * This method transforms a TableRole into a TableRoleClient.
   */
  private TableRole transformTableRoleClient(TableRoleClient role) {
    // first start with the name of the role
    switch (role) {
    case NONE:
      return TableRole.NONE;
    case FILTERED_WRITER:
      return TableRole.FILTERED_WRITER;
    case UNFILTERED_READER_FILTERED_WRITER:
      return TableRole.UNFILTERED_READER_FILTERED_WRITER;
    case READER:
      return TableRole.READER;
    case WRITER:
      return TableRole.WRITER;
    case OWNER:
      return TableRole.OWNER;
    default:
      throw new IllegalStateException("No assignable permissions in transforming table role, "
          + "ServerTableACLServiceImpl.");
    }
  }

  /*
   * This transforms a list of TableAcl objects to a list of TableAclClient
   * objects.
   */
  private ArrayList<TableAclClient> transformTableAclList(List<TableAcl> acls) {
    ArrayList<TableAclClient> clientAcls = new ArrayList<TableAclClient>();
    for (TableAcl acl : acls) {
      clientAcls.add(UtilTransforms.transform(acl));
    }
    return clientAcls;
  }

}
