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
import org.opendatakit.aggregate.client.exception.EntityNotFoundExceptionClient;
import org.opendatakit.aggregate.client.exception.EtagMismatchExceptionClient;
import org.opendatakit.aggregate.client.exception.PermissionDeniedExceptionClient;
import org.opendatakit.aggregate.client.exception.RequestFailureException;
import org.opendatakit.aggregate.client.exception.TableAlreadyExistsExceptionClient;
import org.opendatakit.aggregate.client.odktables.ColumnClient;
import org.opendatakit.aggregate.client.odktables.FileSummaryClient;
import org.opendatakit.aggregate.client.odktables.RowClient;
import org.opendatakit.aggregate.client.odktables.TableDefinitionClient;
import org.opendatakit.aggregate.client.odktables.TableEntryClient;
import org.opendatakit.aggregate.odktables.AuthFilter;
import org.opendatakit.aggregate.odktables.DataManager;
import org.opendatakit.aggregate.odktables.TableManager;
import org.opendatakit.aggregate.odktables.entity.UtilTransforms;
import org.opendatakit.aggregate.odktables.exception.BadColumnNameException;
import org.opendatakit.aggregate.odktables.exception.EtagMismatchException;
import org.opendatakit.aggregate.odktables.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.exception.TableAlreadyExistsException;
import org.opendatakit.aggregate.odktables.relation.DbTableFileInfo;
import org.opendatakit.aggregate.odktables.relation.DbTableFiles;
import org.opendatakit.aggregate.odktables.rest.entity.Column;
import org.opendatakit.aggregate.odktables.rest.entity.OdkTablesKeyValueStoreEntry;
import org.opendatakit.aggregate.odktables.rest.entity.Row;
import org.opendatakit.aggregate.odktables.rest.entity.TableEntry;
import org.opendatakit.aggregate.odktables.rest.entity.TableRole.TablePermission;
import org.opendatakit.aggregate.odktables.rest.entity.TableType;
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
 * @author sudar.sam@gmail.com
 *
 */
public class ServerOdkTablesUtil {

  /**
   * Create a table in the datastore.
   * @param tableId
   * @param definition
   * @param cc
   * @return
   * @throws DatastoreFailureException
   * @throws TableAlreadyExistsExceptionClient
   */
  public static TableEntryClient createTable(String tableId,
      TableDefinitionClient definition,
      CallingContext cc) throws DatastoreFailureException,
      TableAlreadyExistsExceptionClient {
    TableManager tm = new TableManager(cc);
    Log logger = LogFactory.getLog(ServerOdkTablesUtil.class);
    // TODO: add access control stuff
    // Have to be careful of all the transforms going on here.
    // Make sure they actually work as expected!
    // also have to be sure that I am passing in an actual column and not a
    // column resource or something, in which case the transform() method is not
    // altering all of the requisite fields.
    try {
      String tableKey = definition.getTableKey();
      String dbTableName = definition.getDbTableName();
      TableType type = UtilTransforms.transform(definition.getType());
      String tableIdAccessControls = definition.getTableIdAccessControls();
      // TODO: find a way to, for creation, generate a minimal list of
      // kvs entries. for now just putting in blank if you create a table
      // from the server.
      List<OdkTablesKeyValueStoreEntry> kvsEntries =
          new ArrayList<OdkTablesKeyValueStoreEntry>();
      OdkTablesKeyValueStoreEntry tt;
      tt = new OdkTablesKeyValueStoreEntry();
      tt.tableId = tableId;
      tt.partition = "Table";
      tt.aspect = "default";
      tt.key = "tableType";
      tt.type = "text";
      tt.value = type.name();
      kvsEntries.add(tt);

      if ( tableIdAccessControls != null ) {
        tt = new OdkTablesKeyValueStoreEntry();
        tt.tableId = tableId;
        tt.partition = "Table";
        tt.aspect = "default";
        tt.key = "accessControlTableId";
        tt.type = "text";
        tt.value = tableIdAccessControls;
        kvsEntries.add(tt);
      }

      List<ColumnClient> columns = definition.getColumns();
      List<Column> columnsServer = new ArrayList<Column>();
      for (ColumnClient column : columns) {
        columnsServer.add(UtilTransforms.transform(column));
      }
      TableEntry entry = tm.createTable(tableId, tableKey, dbTableName,
          columnsServer, kvsEntries);
      TableEntryClient entryClient = UtilTransforms.transform(entry);
      logger.info(String.format("tableId: %s, definition: %s", tableId, definition));
      return entryClient;
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      throw new DatastoreFailureException(e);
    } catch (TableAlreadyExistsException e) {
      e.printStackTrace();
      throw new TableAlreadyExistsExceptionClient(e);
    }
  }

  /**
   * Create or update a row in the datastore.
   * @param tableId
   * @param rowId
   * @param row
   * @param cc
   * @return
   * @throws AccessDeniedException
   * @throws RequestFailureException
   * @throws DatastoreFailureException
   * @throws EtagMismatchExceptionClient
   * @throws PermissionDeniedExceptionClient
   * @throws BadColumnNameExceptionClient
   * @throws EntityNotFoundExceptionClient
   */
  public static RowClient createOrUpdateRow(String tableId, String rowId,
      RowClient row, CallingContext cc) throws AccessDeniedException,
      RequestFailureException, DatastoreFailureException,
      EtagMismatchExceptionClient, PermissionDeniedExceptionClient,
      BadColumnNameExceptionClient, EntityNotFoundExceptionClient {
    try {
      // first transform row into a server-side row
      Row serverRow = UtilTransforms.transform(row);
      DataManager dm = new DataManager(tableId, cc);
      AuthFilter af = new AuthFilter(tableId, cc);
      af.checkPermission(TablePermission.WRITE_ROW);
      row.setRowId(rowId);
      Row dbRow = dm.getRow(rowId);
      if (dbRow == null) {
        serverRow = dm.insertRow(serverRow);
      } else {
        af.checkFilter(TablePermission.UNFILTERED_WRITE, dbRow);
        serverRow = dm.updateRow(serverRow);
      }
      return UtilTransforms.transform(serverRow);
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
    } catch (EtagMismatchException e) {
      e.printStackTrace();
      throw new EtagMismatchExceptionClient(e);
    }
  }

  /**
   * Create a FileSummaryClient object from a row that originated from
   * EntityConverter.
   * @param row
   * @param blobSetRelation
   * @param cc
   * @return
   * @throws ODKDatastoreException
   */
  public static FileSummaryClient getFileSummaryClientFromRow(Row row,
      String tableId, DbTableFiles blobSetRelation, CallingContext cc) throws
      ODKDatastoreException {
    String filename = blobSetRelation.getBlobEntitySet(
        row.getValues().get(DbTableFileInfo.PATH_TO_FILE), cc)
        .getUnrootedFilename(1, cc);
    Long contentLength = blobSetRelation.getBlobEntitySet(
        filename, cc)
        .getContentLength(1, cc);
    String contentType = blobSetRelation.getBlobEntitySet(
        filename, cc)
        .getContentType(1, cc);
    String id = row.getRowId();
    String key = "this isn't implemented.";
    FileSummaryClient summary = new FileSummaryClient(
        filename, contentType, contentLength, key, 0, id, tableId);
    return summary;
  }
}
