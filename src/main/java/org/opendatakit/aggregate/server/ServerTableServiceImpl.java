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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.client.exception.PermissionDeniedExceptionClient;
import org.opendatakit.aggregate.client.exception.RequestFailureException;
import org.opendatakit.aggregate.client.exception.TableAlreadyExistsExceptionClient;
import org.opendatakit.aggregate.client.odktables.ServerTableService;
import org.opendatakit.aggregate.client.odktables.TableDefinitionClient;
import org.opendatakit.aggregate.client.odktables.TableEntryClient;
import org.opendatakit.aggregate.odktables.TableManager;
import org.opendatakit.aggregate.odktables.entity.UtilTransforms;
import org.opendatakit.aggregate.odktables.exception.ETagMismatchException;
import org.opendatakit.aggregate.odktables.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.rest.entity.TableEntry;
import org.opendatakit.aggregate.odktables.security.TablesUserPermissions;
import org.opendatakit.aggregate.odktables.security.TablesUserPermissionsImpl;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.client.exception.DatastoreFailureException;
import org.opendatakit.common.persistence.engine.gae.DatastoreImpl;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.security.client.exception.AccessDeniedException;
import org.opendatakit.common.web.CallingContext;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class ServerTableServiceImpl extends RemoteServiceServlet implements ServerTableService {

  /**
	 *
	 */
  private static final long serialVersionUID = 3291707708959185034L;
  private static final Log logger = LogFactory.getLog(ServerTableServiceImpl.class);

  @Override
  public ArrayList<TableEntryClient> getTables() throws AccessDeniedException, RequestFailureException,
      DatastoreFailureException, PermissionDeniedExceptionClient {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);
    try {
      ArrayList<TableEntryClient> clientEntries = new ArrayList<TableEntryClient>();
      User user = cc.getCurrentUser();
      if (user.isAnonymous()) {
        throw new AccessDeniedException("Anonymous users cannot view ODK Tables datasets");
      }
      TablesUserPermissions userPermissions = null;
      try {
        userPermissions = new TablesUserPermissionsImpl(user.getUriUser(), cc);
      } catch (PermissionDeniedException e) {
        return clientEntries;
      }
      String appId = ServerPreferencesProperties.getOdkTablesAppId(cc);
      TableManager tm = new TableManager(appId, userPermissions, cc);
      List<TableEntry> entries = tm.getTables();
      for (TableEntry entry : entries) {
        clientEntries.add(UtilTransforms.transform(entry));
      }
      Collections.sort(clientEntries, new Comparator<TableEntryClient>() {
        @Override
        public int compare(TableEntryClient o1, TableEntryClient o2) {
          return o1.getTableId().compareToIgnoreCase(o2.getTableId());
        }
      });

      return clientEntries;
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      throw new DatastoreFailureException(e);
    } catch (ODKTaskLockException e) {
      e.printStackTrace();
      throw new RequestFailureException(e);
    }
  }

  @Override
  public TableEntryClient getTable(String tableId) throws AccessDeniedException,
      RequestFailureException, DatastoreFailureException, PermissionDeniedExceptionClient {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);
    try {
      User user = cc.getCurrentUser();
      if (user.isAnonymous()) {
        throw new AccessDeniedException("Anonymous users cannot access ODK Tables datasets");
      }
      TablesUserPermissions userPermissions = new TablesUserPermissionsImpl(user.getUriUser(), cc);
      String appId = ServerPreferencesProperties.getOdkTablesAppId(cc);
      TableManager tm = new TableManager(appId, userPermissions, cc);
      TableEntry entry = tm.getTableNullSafe(tableId);
      if (entry == null) {
        return null;
      }
      TableEntryClient resource = UtilTransforms.transform(entry);
      return resource;
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

  /**
   * Create a table. If tableId is null, a random UUID is generated using
   * CommonfieldsBase.
   */
  @Override
  public TableEntryClient createTable(String tableId, TableDefinitionClient definition)
      throws AccessDeniedException, RequestFailureException, DatastoreFailureException,
      PermissionDeniedExceptionClient, TableAlreadyExistsExceptionClient {
    // check for null UUID, assign random if true.
    if (tableId == null) {
      tableId = CommonFieldsBase.newUri();
    }
    HttpServletRequest req = this.getThreadLocalRequest();
    // trying this method to try and not get null from a servlet
    CallingContext cc = ContextFactory.getCallingContext(this, req);

    TablesUserPermissions userPermissions = null;
    try {
      User user = cc.getCurrentUser();
      if (user.isAnonymous()) {
        throw new AccessDeniedException("Anonymous users cannot create ODK Tables datasets");
      }
      userPermissions = new TablesUserPermissionsImpl(cc.getCurrentUser().getUriUser(), cc);
      String appId = ServerPreferencesProperties.getOdkTablesAppId(cc);
      return ServerOdkTablesUtil.createTable(appId, tableId, definition, userPermissions, cc);
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      throw new DatastoreFailureException(e);
    } catch (PermissionDeniedException e) {
      e.printStackTrace();
      throw new PermissionDeniedExceptionClient(e);
    } catch (ODKTaskLockException e) {
      e.printStackTrace();
      throw new RequestFailureException(e);
    } catch (ETagMismatchException e) {
      e.printStackTrace();
      throw new TableAlreadyExistsExceptionClient(e);
    }
  }

  @Override
  public void deleteTable(String tableId) throws AccessDeniedException, RequestFailureException,
      DatastoreFailureException, PermissionDeniedExceptionClient {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);
    try {
      User user = cc.getCurrentUser();
      if (user.isAnonymous()) {
        throw new AccessDeniedException("Anonymous users cannot delete ODK Tables datasets");
      }
      TablesUserPermissions userPermissions = new TablesUserPermissionsImpl(user.getUriUser(), cc);
      String appId = ServerPreferencesProperties.getOdkTablesAppId(cc);
      TableManager tm = new TableManager(appId, userPermissions, cc);
      tm.deleteTable(tableId);
      logger.info("tableId: " + tableId);
      DatastoreImpl ds = (DatastoreImpl) cc.getDatastore();
      ds.getDam().logUsage();
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      throw new DatastoreFailureException(e);
    } catch (PermissionDeniedException e) {
      e.printStackTrace();
      throw new PermissionDeniedExceptionClient(e);
    } catch (ODKTaskLockException e) {
      e.printStackTrace();
      throw new RequestFailureException(e);
    }
  }

}
