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
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.client.exception.BadColumnNameExceptionClient;
import org.opendatakit.aggregate.client.exception.PermissionDeniedExceptionClient;
import org.opendatakit.aggregate.client.exception.RequestFailureException;
import org.opendatakit.aggregate.client.odktables.RowClient;
import org.opendatakit.aggregate.client.odktables.ServerDiffService;
import org.opendatakit.aggregate.odktables.DataManager;
import org.opendatakit.aggregate.odktables.entity.UtilTransforms;
import org.opendatakit.aggregate.odktables.exception.BadColumnNameException;
import org.opendatakit.aggregate.odktables.exception.InconsistentStateException;
import org.opendatakit.aggregate.odktables.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.rest.entity.Row;
import org.opendatakit.aggregate.odktables.security.TablesUserPermissionsImpl;
import org.opendatakit.aggregate.odktables.security.TablesUserPermissions;
import org.opendatakit.common.persistence.client.exception.DatastoreFailureException;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;
import org.opendatakit.common.security.client.exception.AccessDeniedException;
import org.opendatakit.common.web.CallingContext;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class ServerDiffServiceImpl extends RemoteServiceServlet implements ServerDiffService {

  /**
	 *
	 */
  private static final long serialVersionUID = -5472352346806984818L;

  @Override
  public ArrayList<RowClient> getRowsSince(String dataETag, String tableId)
      throws AccessDeniedException, RequestFailureException, DatastoreFailureException,
      PermissionDeniedExceptionClient, BadColumnNameExceptionClient {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);
    try {
      TablesUserPermissions userPermissions = new TablesUserPermissionsImpl(cc.getCurrentUser()
          .getUriUser(), cc);
      String appId = ServerPreferencesProperties.getOdkTablesAppId(cc);
      DataManager dm = new DataManager(appId, tableId, userPermissions, cc);
      List<Row> rows;
      rows = dm.getRowsSince(dataETag);
      return transformRows(rows);
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      throw new DatastoreFailureException(e);
    } catch (PermissionDeniedException e) {
      e.printStackTrace();
      throw new PermissionDeniedExceptionClient(e);
    } catch (InconsistentStateException e) {
      e.printStackTrace();
      throw new RequestFailureException(e);
    } catch (ODKTaskLockException e) {
      e.printStackTrace();
      throw new RequestFailureException(e);
    } catch (BadColumnNameException e) {
      e.printStackTrace();
      throw new BadColumnNameExceptionClient(e);
    }
  }

  // very basic transformation method
  private ArrayList<RowClient> transformRows(List<Row> rows) {
    ArrayList<RowClient> clientRows = new ArrayList<RowClient>();
    for (Row row : rows) {
      clientRows.add(UtilTransforms.transform(row));
    }
    return clientRows;
  }

}
