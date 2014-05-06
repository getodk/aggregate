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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opendatakit.aggregate.client.exception.BadColumnNameExceptionClient;
import org.opendatakit.aggregate.client.exception.ETagMismatchExceptionClient;
import org.opendatakit.aggregate.client.exception.EntityNotFoundExceptionClient;
import org.opendatakit.aggregate.client.exception.PermissionDeniedExceptionClient;
import org.opendatakit.aggregate.client.exception.RequestFailureException;
import org.opendatakit.aggregate.client.exception.TableAlreadyExistsExceptionClient;
import org.opendatakit.aggregate.client.odktables.ColumnClient;
import org.opendatakit.aggregate.client.odktables.FileSummaryClient;
import org.opendatakit.aggregate.client.odktables.RowClient;
import org.opendatakit.aggregate.client.odktables.TableDefinitionClient;
import org.opendatakit.aggregate.client.odktables.TableEntryClient;
import org.opendatakit.aggregate.odktables.DataManager;
import org.opendatakit.aggregate.odktables.TableManager;
import org.opendatakit.aggregate.odktables.entity.UtilTransforms;
import org.opendatakit.aggregate.odktables.exception.BadColumnNameException;
import org.opendatakit.aggregate.odktables.exception.ETagMismatchException;
import org.opendatakit.aggregate.odktables.exception.InconsistentStateException;
import org.opendatakit.aggregate.odktables.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.exception.TableAlreadyExistsException;
import org.opendatakit.aggregate.odktables.relation.DbTableFileInfo;
import org.opendatakit.aggregate.odktables.relation.DbTableFiles;
import org.opendatakit.aggregate.odktables.rest.entity.Column;
import org.opendatakit.aggregate.odktables.rest.entity.Row;
import org.opendatakit.aggregate.odktables.rest.entity.TableEntry;
import org.opendatakit.aggregate.odktables.security.TablesUserPermissions;
import org.opendatakit.common.persistence.client.exception.DatastoreFailureException;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;
import org.opendatakit.common.security.client.exception.AccessDeniedException;
import org.opendatakit.common.web.CallingContext;

/**
 * The idea is that this will house methods for OdkTables that could exist in
 * the Server.*Impl methods but that also need to be called not via gwt, eg
 * through a servlet. In this case various things like getting CallingContext
 * change, and this will be the home for that level of indirection.
 *
 * @author sudar.sam@gmail.com
 *
 */
public class ServerOdkTablesUtil {

  /**
   * Create a table in the datastore.
   *
   * @param appId
   * @param tableId
   * @param definition
   * @param cc
   * @return
   * @throws DatastoreFailureException
   * @throws TableAlreadyExistsExceptionClient
   * @throws PermissionDeniedExceptionClient
   * @throws ETagMismatchException
   */
  public static TableEntryClient createTable(String appId, String tableId, TableDefinitionClient definition,
      TablesUserPermissions userPermissions, CallingContext cc) throws DatastoreFailureException,
      TableAlreadyExistsExceptionClient, PermissionDeniedExceptionClient, ETagMismatchException {
    Log logger = LogFactory.getLog(ServerOdkTablesUtil.class);
    // TODO: add access control stuff
    // Have to be careful of all the transforms going on here.
    // Make sure they actually work as expected!
    // also have to be sure that I am passing in an actual column and not a
    // column resource or something, in which case the transform() method is not
    // altering all of the requisite fields.
    try {
      TableManager tm = new TableManager(appId, userPermissions, cc);

      List<ColumnClient> columns = definition.getColumns();
      List<Column> columnsServer = new ArrayList<Column>();
      for (ColumnClient column : columns) {
        columnsServer.add(UtilTransforms.transform(column));
      }
      TableEntry entry = tm.createTable(tableId, columnsServer);
      TableEntryClient entryClient = UtilTransforms.transform(entry);
      logger.info(String.format("tableId: %s, definition: %s", tableId, definition));
      return entryClient;
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      throw new DatastoreFailureException(e);
    } catch (TableAlreadyExistsException e) {
      e.printStackTrace();
      throw new TableAlreadyExistsExceptionClient(e);
    } catch (PermissionDeniedException e) {
      e.printStackTrace();
      throw new PermissionDeniedExceptionClient(e);
    } catch (ODKTaskLockException e) {
      e.printStackTrace();
      throw new DatastoreFailureException(e);
    }
  }

  /**
   * Create or update a row in the datastore.
   *
   * @param appId
   * @param tableId
   * @param rowId
   * @param row
   * @param cc
   * @return
   * @throws AccessDeniedException
   * @throws RequestFailureException
   * @throws DatastoreFailureException
   * @throws ETagMismatchExceptionClient
   * @throws PermissionDeniedExceptionClient
   * @throws BadColumnNameExceptionClient
   * @throws EntityNotFoundExceptionClient
   * @throws InconsistentStateException
   */
  public static RowClient createOrUpdateRow(String appId, String tableId, String rowId, RowClient row,
      TablesUserPermissions userPermissions, CallingContext cc) throws AccessDeniedException,
      RequestFailureException, DatastoreFailureException, ETagMismatchExceptionClient,
      PermissionDeniedExceptionClient, BadColumnNameExceptionClient, EntityNotFoundExceptionClient, InconsistentStateException {
    try {
      // first transform row into a server-side row
      Row serverRow = UtilTransforms.transform(row);
      DataManager dm = new DataManager(appId, tableId, userPermissions, cc);
      row.setRowId(rowId);

      Row newServerRow = dm.insertOrUpdateRow(serverRow);
      return UtilTransforms.transform(newServerRow);
    } catch (ODKEntityNotFoundException e) {
      e.printStackTrace();
      throw new EntityNotFoundExceptionClient(e);
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      throw new DatastoreFailureException(e);
    } catch (ODKTaskLockException e) {
      e.printStackTrace();
      throw new DatastoreFailureException(e);
    } catch (PermissionDeniedException e) {
      e.printStackTrace();
      throw new PermissionDeniedExceptionClient(e);
    } catch (BadColumnNameException e) {
      e.printStackTrace();
      throw new BadColumnNameExceptionClient(e);
    } catch (ETagMismatchException e) {
      e.printStackTrace();
      throw new ETagMismatchExceptionClient(e);
    }
  }

  /**
   * Create a FileSummaryClient object from a row that originated from
   * EntityConverter.
   *
   * @param row
   * @param blobSetRelation
   * @param cc
   * @return
   * @throws ODKDatastoreException
   */
  public static FileSummaryClient getFileSummaryClientFromRow(Row row, String odkClientVersion, String tableId,
      DbTableFiles blobSetRelation, CallingContext cc) throws ODKDatastoreException {
    String filename = blobSetRelation.getBlobEntitySet(
        row.getValues().get(DbTableFileInfo.PATH_TO_FILE), cc).getUnrootedFilename(1, cc);
    Long contentLength = blobSetRelation.getBlobEntitySet(filename, cc).getContentLength(1, cc);
    String contentType = blobSetRelation.getBlobEntitySet(filename, cc).getContentType(1, cc);
    String id = row.getRowId();
    String downloadUrl = null;
    FileSummaryClient summary = new FileSummaryClient(filename, contentType, contentLength, id, odkClientVersion, tableId, downloadUrl);
    return summary;
  }
}
