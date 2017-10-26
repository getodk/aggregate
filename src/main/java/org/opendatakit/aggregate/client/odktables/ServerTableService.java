/*
 * Copyright (C) 2013 University of Washington
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

package org.opendatakit.aggregate.client.odktables;

import java.util.ArrayList;

import org.opendatakit.aggregate.client.exception.PermissionDeniedExceptionClient;
import org.opendatakit.aggregate.client.exception.RequestFailureException;
import org.opendatakit.aggregate.client.exception.TableAlreadyExistsExceptionClient;
import org.opendatakit.common.persistence.client.exception.DatastoreFailureException;
import org.opendatakit.common.security.client.exception.AccessDeniedException;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * This will be the TableService for the server. It will act the same way as
 * org.opendatakit.aggregate.odktables.api.TableService, except that it will be
 * for interacting with the table information on the server, rather than with a
 * phone.
 *
 * @author sudar.sam
 */

@RemoteServiceRelativePath("servertableservice")
public interface ServerTableService extends RemoteService {

  ArrayList<TableEntryClient> getTables() throws AccessDeniedException, RequestFailureException,
      DatastoreFailureException, PermissionDeniedExceptionClient;

  TableEntryClient getTable(String tableId) throws AccessDeniedException, RequestFailureException,
      DatastoreFailureException, PermissionDeniedExceptionClient;

  TableEntryClient createTable(String tableId, TableDefinitionClient definition)
      throws AccessDeniedException, RequestFailureException, DatastoreFailureException,
      PermissionDeniedExceptionClient, TableAlreadyExistsExceptionClient;

  void deleteTable(String tableId) throws AccessDeniedException, RequestFailureException,
      DatastoreFailureException, PermissionDeniedExceptionClient;

  // Not sure if I still need these methods, which are present in Dylan's
  // methods,
  // but perhaps I don't need them because I can just create the other services
  // directly?
  // Or "service" means something different in Dylan's case than in GWT speak?
  /*
   * These I think are basically already implemented above...
   * ColumnServiceClient getColumns(String tableId) throws
   * AccessDeniedException, RequestFailureException, DatastoreFailureException;
   *
   * DataServiceClient getData(String tableId) throws AccessDeniedException,
   * RequestFailureException, DatastoreFailureException;
   *
   * PropertiesServiceClient getProperties(String tableId) throws
   * AccessDeniedException, RequestFailureException, DatastoreFailureException;
   *
   * DiffServiceClient getDiff(String tabledId) throws AccessDeniedException,
   * RequestFailureException, DatastoreFailureException;
   *
   * TableAclServiceClient getAcl(String tableId) throws AccessDeniedException,
   * RequestFailureException, DatastoreFailureException;
   */

}
