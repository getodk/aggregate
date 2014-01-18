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
import org.opendatakit.aggregate.odktables.AuthFilter;
import org.opendatakit.aggregate.odktables.OdkTablesUserInfoTable;
import org.opendatakit.aggregate.odktables.TableManager;
import org.opendatakit.aggregate.odktables.entity.UtilTransforms;
import org.opendatakit.aggregate.odktables.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.relation.DbKeyValueStore;
import org.opendatakit.aggregate.odktables.rest.entity.Scope;
import org.opendatakit.aggregate.odktables.rest.entity.TableEntry;
import org.opendatakit.aggregate.odktables.rest.entity.TableRole.TablePermission;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.client.exception.DatastoreFailureException;
import org.opendatakit.common.persistence.engine.gae.DatastoreImpl;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;
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
  public List<TableEntryClient> getTables() throws AccessDeniedException, RequestFailureException,
      DatastoreFailureException, PermissionDeniedExceptionClient {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);
    try {
      OdkTablesUserInfoTable userInfo = OdkTablesUserInfoTable.getUserData(cc.getCurrentUser()
          .getUriUser(), cc);
      TableManager tm = new TableManager(userInfo, cc);
      List<Scope> scopes = tm.getScopes(cc);
      List<TableEntry> entries = tm.getTables(scopes);
      ArrayList<TableEntryClient> clientEntries = new ArrayList<TableEntryClient>();
      for (TableEntry entry : entries) {
        String displayName = DbKeyValueStore.getDisplayName(entry.getTableId(),
            entry.getPropertiesETag(), cc);
        clientEntries.add(UtilTransforms.transform(entry, displayName));
      }
      Collections.sort(clientEntries, new Comparator<TableEntryClient>() {
        @Override
        public int compare(TableEntryClient o1, TableEntryClient o2) {
          return o1.getDisplayName().compareToIgnoreCase(o2.getDisplayName());
        }
      });

      return clientEntries;
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      throw new DatastoreFailureException(e);
    }
  }

  @Override
  public TableEntryClient getTable(String tableId) throws AccessDeniedException,
      RequestFailureException, DatastoreFailureException, PermissionDeniedExceptionClient {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);
    try {
      OdkTablesUserInfoTable userInfo = OdkTablesUserInfoTable.getUserData(cc.getCurrentUser()
          .getUriUser(), cc);
      TableManager tm = new TableManager(userInfo, cc);
      new AuthFilter(tableId, userInfo, cc).checkPermission(TablePermission.READ_TABLE_ENTRY);
      TableEntry entry = tm.getTableNullSafe(tableId);
      String displayName = DbKeyValueStore.getDisplayName(tableId, entry.getPropertiesETag(), cc);
      TableEntryClient resource = UtilTransforms.transform(entry, displayName);
      return resource;
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      throw new DatastoreFailureException(e);
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

    OdkTablesUserInfoTable userInfo;
    try {
      userInfo = OdkTablesUserInfoTable.getUserData(cc.getCurrentUser().getUriUser(), cc);
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      throw new DatastoreFailureException(e);
    }

    return ServerOdkTablesUtil.createTable(tableId, definition, userInfo, cc);
    // TableManager tm = new TableManager(cc);
    // TODO: add access control stuff
    // Have to be careful of all the transforms going on here.
    // Make sure they actually work as expected!
    // also have to be sure that I am passing in an actual column and not a
    // column resource or something, in which case the transform() method is not
    // altering all of the requisite fields.
    // moved this to ServerOdkTablesUtil
    // try {
    // String tableName = definition.getTableName();
    // List<ColumnClient> columns = definition.getColumns();
    // List<Column> columnsServer = new ArrayList<Column>();
    // for (ColumnClient column : columns) {
    // columnsServer.add(UtilTransforms.transform(column));
    // }
    // String metadata = definition.getMetadata();
    // TableEntry entry = tm.createTable(tableId, tableName, columnsServer,
    // metadata);
    // TableEntryClient entryClient = entry.transform();
    // logger.info(String.format("tableId: %s, definition: %s", tableId,
    // definition));
    // return entryClient;
    // } catch (ODKDatastoreException e) {
    // e.printStackTrace();
    // throw new DatastoreFailureException(e);
    // } catch (TableAlreadyExistsException e) {
    // e.printStackTrace();
    // throw new TableAlreadyExistsExceptionClient(e);
    // }
  }

  @Override
  public void deleteTable(String tableId) throws AccessDeniedException, RequestFailureException,
      DatastoreFailureException, PermissionDeniedExceptionClient {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);
    try {
      OdkTablesUserInfoTable userInfo = OdkTablesUserInfoTable.getUserData(cc.getCurrentUser()
          .getUriUser(), cc);
      TableManager tm = new TableManager(userInfo, cc);
      new AuthFilter(tableId, userInfo, cc).checkPermission(TablePermission.DELETE_TABLE);
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
