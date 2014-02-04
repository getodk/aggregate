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
import org.opendatakit.aggregate.client.exception.EntityNotFoundExceptionClient;
import org.opendatakit.aggregate.client.exception.ETagMismatchExceptionClient;
import org.opendatakit.aggregate.client.exception.PermissionDeniedExceptionClient;
import org.opendatakit.aggregate.client.exception.RequestFailureException;
import org.opendatakit.aggregate.client.odktables.FileSummaryClient;
import org.opendatakit.aggregate.client.odktables.RowClient;
import org.opendatakit.aggregate.client.odktables.ServerDataService;
import org.opendatakit.aggregate.client.odktables.TableContentsClient;
import org.opendatakit.aggregate.client.odktables.TableContentsForFilesClient;
import org.opendatakit.aggregate.odktables.AuthFilter;
import org.opendatakit.aggregate.odktables.DataManager;
import org.opendatakit.aggregate.odktables.TableManager;
import org.opendatakit.aggregate.odktables.entity.UtilTransforms;
import org.opendatakit.aggregate.odktables.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.relation.DbColumnDefinitions;
import org.opendatakit.aggregate.odktables.relation.DbTableFileInfo;
import org.opendatakit.aggregate.odktables.rest.entity.Row;
import org.opendatakit.aggregate.odktables.rest.entity.TableEntry;
import org.opendatakit.aggregate.odktables.rest.entity.TableRole.TablePermission;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.client.exception.DatastoreFailureException;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;
import org.opendatakit.common.security.client.exception.AccessDeniedException;
import org.opendatakit.common.web.CallingContext;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * For ODKTables.
 *
 * @author sudar.sam@gmail.com
 *
 */
public class ServerDataServiceImpl extends RemoteServiceServlet
    implements ServerDataService {

  /**
	 *
	 */
  private static final long serialVersionUID = -5051558217315955180L;

  @Override
  public List<RowClient> getRows(String tableId) throws AccessDeniedException,
      RequestFailureException, DatastoreFailureException, PermissionDeniedExceptionClient,
      EntityNotFoundExceptionClient {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);
    try { // Must use try so that you can catch the ODK specific errors.
      DataManager dm = new DataManager(tableId, cc);
      AuthFilter af = new AuthFilter(tableId, cc);
      // TODO: auth stuff
//      af.checkPermission(TablePermission.READ_ROW);
      List<Row> rows;
      rows = dm.getRows();
//      if (af.hasPermission(TablePermission.UNFILTERED_READ)) {
//        rows = dm.getRows();
//      } else {
//        List<Scope> scopes = AuthFilter.getScopes(cc);
//        rows = dm.getRows(scopes);
//        // rows = dm.getRows();
//      }
      return transformRows(rows);
    } catch (ODKEntityNotFoundException e) {
      e.printStackTrace();
      throw new EntityNotFoundExceptionClient(e);
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      throw new DatastoreFailureException(e);
    }
    // TODO: auth stuff.
//    } catch (PermissionDeniedException e) {
//      e.printStackTrace();
//      throw new PermissionDeniedExceptionClient(e);
//    }
  }

  @Override
  public TableContentsClient getRow(String tableId, String rowId) throws AccessDeniedException,
      RequestFailureException, DatastoreFailureException, PermissionDeniedExceptionClient,
      EntityNotFoundExceptionClient {
    try {
      HttpServletRequest req = this.getThreadLocalRequest();
      CallingContext cc = ContextFactory.getCallingContext(this, req);
      DataManager dm = new DataManager(tableId, cc);
      AuthFilter af = new AuthFilter(tableId, cc);
      af.checkPermission(TablePermission.READ_ROW);
      Row row = dm.getRowNullSafe(rowId);
      af.checkFilter(TablePermission.UNFILTERED_READ, row.getRowId(), row.getFilterScope());

      TableContentsClient tcc = new TableContentsClient();
      tcc.columnNames = this.getColumnNames(tableId);
      List<RowClient> rows = new ArrayList<RowClient>();
      rows.add(UtilTransforms.transform(row));
      tcc.rows = rows;
      return tcc;
    } catch (ODKEntityNotFoundException e) {
      e.printStackTrace();
      throw new EntityNotFoundExceptionClient(e);
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      throw new DatastoreFailureException(e);
    } catch (PermissionDeniedException e) {
      e.printStackTrace();
      throw new PermissionDeniedExceptionClient(e);
    }
  }

  @Override
  public RowClient createOrUpdateRow(String tableId, String rowId,
      RowClient row) throws AccessDeniedException, RequestFailureException,
      DatastoreFailureException, ETagMismatchExceptionClient,
      PermissionDeniedExceptionClient, BadColumnNameExceptionClient,
      EntityNotFoundExceptionClient {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);
    return ServerOdkTablesUtil.createOrUpdateRow(tableId, rowId, row, cc);
// moved the below to ServerOdkTablesUtil
//    try {
//      // first transform row into a server-side row
//      Row serverRow = UtilTransforms.transform(row);
//      DataManager dm = new DataManager(tableId, cc);
//      AuthFilter af = new AuthFilter(tableId, cc);
//      af.checkPermission(TablePermission.WRITE_ROW);
//      row.setRowId(rowId);
//      Row dbRow = dm.getRow(rowId);
//      if (dbRow == null) {
//        serverRow = dm.insertRow(serverRow);
//      } else {
//        af.checkFilter(TablePermission.UNFILTERED_WRITE, dbRow);
//        serverRow = dm.updateRow(serverRow);
//      }
//      return serverRow.transform();
//    } catch (ODKEntityNotFoundException e) {
//      e.printStackTrace();
//      throw new EntityNotFoundExceptionClient(e);
//    } catch (ODKDatastoreException e) {
//      e.printStackTrace();
//      throw new DatastoreFailureException(e);
//    } catch (ODKTaskLockException e) {
//      e.printStackTrace();
//      throw new DatastoreFailureException(e);
//    } catch (PermissionDeniedException e) {
//      e.printStackTrace();
//      throw new PermissionDeniedExceptionClient(e);
//    } catch (BadColumnNameException e) {
//      e.printStackTrace();
//      throw new BadColumnNameExceptionClient(e);
//    } catch (ETagMismatchException e) {
//      e.printStackTrace();
//      throw new ETagMismatchExceptionClient(e);
//    }
  }

  @Override
  public void deleteRow(String tableId, String rowId) throws
      AccessDeniedException, RequestFailureException,
      DatastoreFailureException, PermissionDeniedExceptionClient,
      EntityNotFoundExceptionClient {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);
    try { // Must use try so that you can catch the ODK specific errors.
      DataManager dm = new DataManager(tableId, cc);
      AuthFilter af = new AuthFilter(tableId, cc);

      af.checkPermission(TablePermission.DELETE_ROW);
      Row row = dm.getRowNullSafe(rowId);
      af.checkFilter(TablePermission.UNFILTERED_DELETE, row.getRowId(), row.getFilterScope());
      dm.deleteRow(rowId);
    } catch (ODKEntityNotFoundException e) {
      e.printStackTrace();
      throw new EntityNotFoundExceptionClient(e);
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

  /**
   * Gets the element_names of the columns.
   *
   * @return List<String> of the column names
   */
  @Override
  public List<String> getColumnNames(String tableId) throws DatastoreFailureException,
      EntityNotFoundExceptionClient {

    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);
    try {
      TableManager tm = new TableManager(cc);
      TableEntry entry = tm.getTable(tableId);
      List<String> columnNames = DbColumnDefinitions.queryForColumnNames(tableId, entry.getSchemaETag(), cc);
      return columnNames;
    } catch (ODKEntityNotFoundException e) {
      e.printStackTrace();
      throw new EntityNotFoundExceptionClient(e);
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      throw new DatastoreFailureException(e);
    }
  }

  /*
   * don't think i need this, but i'm keeping it so that i can use the code for
   * a reference later if i need to.
   *
   * /** Gets ALL the column names, keeping the user-defined ones pretty.
   *
   * @param rows
   *
   * @return
   */
  /*
   * Uses the other getColumnNames, which gets the pretty ones. Gets all the
   * names, removes the "TABLE_UUID_.*" ones, and then adds them back in after
   * being translated to the pretty ones.
   *
   * @Override public List<String> getAllColumnNames(String tableId) throws
   * DatastoreFailureException { HttpServletRequest req =
   * this.getThreadLocalRequest(); CallingContext cc =
   * ContextFactory.getCallingContext(this, req); try { // Must use try so that
   * you can catch the ODK specific errors. DataManager dm = new
   * DataManager(tableId, cc); AuthFilter af = new AuthFilter(tableId, cc);
   * Set<DataField> dataFields = DbTable.getRelation(tableId,
   * cc).getDataFields();
   *
   * // TODO: look into seeing if you need to have a scope on column names. //
   * TODO: see if there is a better way to remove the pretty ones, or make //
   * sure that you can't name a column TABLE_UUID_ List<String> columnNames =
   * getColumnNames(tableId); for (DataField df : dataFields) { // don't add if
   * it starts with this, as you'll add it later if (!df.getName().substring(0,
   * 11).equals("TABLE_UUID_")) { columnNames.add(df.getName()); } }
   *
   * return columnNames; } catch (ODKDatastoreException e) {
   * e.printStackTrace(); throw new DatastoreFailureException(e); } }
   */

  private List<RowClient> transformRows(List<Row> rows) {
    List<RowClient> clientRows = new ArrayList<RowClient>();
    for (Row row : rows) {
      clientRows.add(UtilTransforms.transform(row));
    }
    return clientRows;
  }

  /**
   * This returns the non-media files rows from the DbTableFileInfo table about
   * the files for a certain table. In other words, this returns the files that
   * were uploaded NOT through the "upload additional media files" link on the
   * file upload servlets. This does not include their associated media files.
   * <p>
   * NB: this does NOT use the same
   * {@link Datamanager} class as the rest of the DataService methods, as this
   * is considered accessing a unique table that is part of the back-end,
   * rather than one of the tables that is created by the user.
   *
   * @param tableId
   *          the string uid of the table whose files you want
   */
  @Override
  public List<FileSummaryClient> getNonMediaFiles(String tableId) throws AccessDeniedException,
      RequestFailureException, DatastoreFailureException, PermissionDeniedExceptionClient,
      EntityNotFoundExceptionClient {
    throw new IllegalStateException("Not implemented");
// this is commented out after we changed the file around.
// TODO: update this.
//    HttpServletRequest req = this.getThreadLocalRequest();
//    CallingContext cc = ContextFactory.getCallingContext(this, req);
//    try {
//      DbTableFiles blobSetRelation = new DbTableFiles(cc);
//      List<Row> rows = EntityConverter.toRowsFromFileInfo(
//          DbTableFileInfo.queryForNonMediaFiles(tableId, cc));
//      List<FileSummaryClient> nonMediaFiles =
//          new ArrayList<FileSummaryClient>();
//      for (Row row : rows) {
//        FileSummaryClient summary = ServerOdkTablesUtil
//            .getFileSummaryClientFromRow(row, tableId, blobSetRelation, cc);
//        nonMediaFiles.add(summary);
//      }
//      return nonMediaFiles;
//    } catch (ODKEntityNotFoundException e) {
//      e.printStackTrace();
//      throw new EntityNotFoundExceptionClient(e);
//    } catch (ODKDatastoreException e) {
//      e.printStackTrace();
//      throw new DatastoreFailureException(e);
//    }
  }


  @Override
  public List<FileSummaryClient> getMedialFilesKey(String tableId, String key)
      throws AccessDeniedException, RequestFailureException, DatastoreFailureException,
      PermissionDeniedExceptionClient, EntityNotFoundExceptionClient {
    throw new IllegalStateException("Not implemented");
// this is commented out after we changed the file around.
// TODO: see if we actually need this.
//    HttpServletRequest req = this.getThreadLocalRequest();
//    CallingContext cc = ContextFactory.getCallingContext(this, req);
//    try {
//      DbTableFiles blobSetRelation = new DbTableFiles(cc);
//      List<Row> rows = EntityConverter.toRowsFromFileInfo(
//          DbTableFileInfo.queryForMediaFiles(tableId, key, cc));
//      List<FileSummaryClient> mediaFiles =
//          new ArrayList<FileSummaryClient>();
//      for (Row row : rows) {
//        FileSummaryClient summary = ServerOdkTablesUtil
//            .getFileSummaryClientFromRow(row, tableId, blobSetRelation, cc);
//        mediaFiles.add(summary);
//      }
//      return mediaFiles;
//    } catch (ODKEntityNotFoundException e) {
//      e.printStackTrace();
//      throw new EntityNotFoundExceptionClient(e);
//    } catch (ODKDatastoreException e) {
//      e.printStackTrace();
//      throw new DatastoreFailureException(e);
//    }
  }

  // TODO make this work. atm isn't working.
//  public List<OdkTablesKeyValueStoreEntry> testFileGets(String tableId)
//      throws PermissionDeniedExceptionClient, DatastoreFailureException, RequestFailureException,
//      EntityNotFoundExceptionClient, AccessDeniedException, JsonGenerationException, IOException {
//    HttpServletRequest req = this.getThreadLocalRequest();
//    CallingContext cc = ContextFactory.getCallingContext(this, req);
//
//    try {
//      List<RowClient> infoRows = getNonMediaFiles(tableId);
//      TableManager tm = new TableManager(cc);
//      TableEntry table = tm.getTable(tableId);
//      String tableName = table.getTableName();
//      DbTableFiles blobSetRelation = new DbTableFiles(cc);
//      List<OdkTablesKeyValueStoreEntry> entries = new ArrayList<OdkTablesKeyValueStoreEntry>();
//      for (RowClient row : infoRows) {
//        // we only want the non-deleted rows
//        if (!row.isDeleted()) {
//          // the KeyValueStoreEntry object is the same for every entry. However,
//          // for files you need to create a FileManifestEntry for the value.
//          OdkTablesKeyValueStoreEntry entry = new OdkTablesKeyValueStoreEntry();
//          entry.tableId = tableId;
//          entry.tableName = tableName;
//          entry.key = row.getValues().get(DbTableFileInfo.KEY);
//          entry.type = row.getValues().get(DbTableFileInfo.VALUE_TYPE);
//          // if it's a file, make the file manifest entry.
//          if (entry.type.equalsIgnoreCase(DbTableFileInfo.Type.FILE.name)) {
//            OdkTablesFileManifestEntry fileEntry = new OdkTablesFileManifestEntry();
//            fileEntry.filename = blobSetRelation.getBlobEntitySet(
//                row.getValues().get(DbTableFileInfo.VALUE), cc).getUnrootedFilename(1, cc);
//            fileEntry.md5hash = blobSetRelation.getBlobEntitySet(
//                row.getValues().get(DbTableFileInfo.VALUE), cc).getContentHash(1, cc);
//            // now generate the download url. look at XFormsManifestXmlTable as
//            // an
//            // example of how Mitch did it.
//            Map<String, String> properties = new HashMap<String, String>();
//            properties.put(ServletConsts.BLOB_KEY, row.getValues().get(DbTableFileInfo.VALUE));
//            properties.put(ServletConsts.AS_ATTACHMENT, "true");
//            String url = cc.getServerURL() + BasicConsts.FORWARDSLASH
//                + OdkTablesTableFileDownloadServlet.ADDR;
//            fileEntry.downloadUrl = HtmlUtil.createLinkWithProperties(url, properties);
//            // now convert this object to json and set it to the entry's value.
//            ObjectMapper mapper = new ObjectMapper();
//            entry.value = mapper.writeValueAsString(fileEntry);
//          } else {
//            // if it's not a file, we just set the value. as input.
//            entry.value = row.getValues().get(DbTableFileInfo.VALUE);
//          }
//          // and now add the completed entry to the list of entries
//          entries.add(entry);
//
//        }
//      }
//      return entries;
//    } catch (ODKDatastoreException e) {
//      e.printStackTrace();
//      throw new DatastoreFailureException(e);
//    }
//  }

  /**
   * Get the list of columns that are in the DbTableFileInfo table. This is
   * defined in server code and thus is never in danger of throwing
   * DatastoreExceptions.
   */
  @Override
  public List<String> getFileRowInfoColumnNames() {
    List<DataField> exposedColumnNames = DbTableFileInfo.exposedColumnNames;
    List<String> columnNames = new ArrayList<String>();
    for ( DataField f : exposedColumnNames ) {
      columnNames.add(f.getName());
    }
    return columnNames;
  }

  @Override
  public TableContentsClient getTableContents(String tableId) throws AccessDeniedException,
      RequestFailureException, DatastoreFailureException, PermissionDeniedExceptionClient,
      EntityNotFoundExceptionClient {
    TableContentsClient tcc = new TableContentsClient();
    tcc.rows = getRows(tableId);
    tcc.columnNames = getColumnNames(tableId);
    return tcc;
  }

  /**
   * This method more or less gets the user-friendly data to be displayed. It
   * adds the correct filename and returns only the non-deleted rows.
   */
  @Override
  public TableContentsForFilesClient getFileInfoContents(String tableId)
      throws AccessDeniedException, RequestFailureException,
      DatastoreFailureException, PermissionDeniedExceptionClient,
      EntityNotFoundExceptionClient {
    TableContentsForFilesClient tcc = new TableContentsForFilesClient();
    tcc.columnNames = getFileRowInfoColumnNames();
    tcc.columnNames.add(DbTableFileInfo.UI_ONLY_FILENAME_HEADING);
    tcc.columnNames.add(DbTableFileInfo.UI_ONLY_TABLENAME_HEADING);
    //tcc.rows = getNonMediaFiles(tableId);
    List<FileSummaryClient> nonMediaSummaries = new ArrayList<FileSummaryClient>();
    // TODO: fix this
    // nonMediaSummaries = getNonMediaFiles(tableId);
    // add in the user friendly filename
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);
    try {
      TableManager tm = new TableManager(cc);
      TableEntry table = tm.getTable(tableId);
      if (table == null) { // you couldn't find the table
        throw new ODKEntityNotFoundException();
      }
//      String tableName = table.getTableName();
//      DbTableFiles blobSetRelation = new DbTableFiles(cc);
//      List<RowClient> newRows = new ArrayList<RowClient>();
      // this will hold the summaries for all the non media files. the
      // media files are associated with entries.
      List<FileSummaryClient> completedSummaries =
          new ArrayList<FileSummaryClient>();
      for (FileSummaryClient summary : nonMediaSummaries) {
        // first get the media files for this key.
        //String key = row.getValues().get(DbTableFileInfo.KEY)
        // get the media files for this file.
        List<FileSummaryClient> mediaFiles = getMedialFilesKey(tableId,
            summary.getKey());
//        String filename = blobSetRelation.getBlobEntitySet(
//            row.getValues().get(DbTableFileInfo.VALUE), cc)
//            .getUnrootedFilename(1, cc);
        //FileSummaryClient summary = new FileSummaryClient(key, key, null, key, mediaFiles)
        // set the media files.
        FileSummaryClient sum = new FileSummaryClient(summary.getFilename(),
            summary.getContentType(), summary.getContentLength(),
            summary.getKey(), mediaFiles.size(), summary.getId(), tableId);
        completedSummaries.add(sum);
      }
      tcc.nonMediaFiles = completedSummaries;


//      for (RowClient row : tcc.rows) {
//        // we only want the non-deleted rows
//        if (!row.isDeleted()) {
//          String filename = blobSetRelation.getBlobEntitySet(
//              row.getValues().get(DbTableFileInfo.VALUE), cc)
//              .getUnrootedFilename(1, cc);
//          Long contentLength = blobSetRelation.getBlobEntitySet(
//              row.getValues().get(DbTableFileInfo.VALUE), cc)
//              .getContentLength(1, cc);
//          String contentType = blobSetRelation.getBlobEntitySet(
//              row.getValues().get(DbTableFileInfo.VALUE), cc)
//              .getContentType(1, cc);
//          row.getValues().put(DbTableFileInfo.UI_ONLY_FILENAME_HEADING, filename);
//          row.getValues().put(DbTableFileInfo.UI_ONLY_TABLENAME_HEADING, tableName);
//          newRows.add(row);
//        }
//      }
//      tcc.rows = newRows;
      return tcc;
    } catch (ODKEntityNotFoundException e) {
      e.printStackTrace();
      throw new EntityNotFoundExceptionClient(e);
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      throw new DatastoreFailureException(e);
    }
  }

  /**
   * Deletes the file from the datastore. Currently just marks the row as
   * deleted. It assumes that only one person will be accessing these files at
   * the same, and doesn't lock to try and prevent concurrent access or anything
   * along those lines.
   * <p>
   * This is largely based on the {@link DataManager} deleteRow method.
   */
  @Override
  public void deleteTableFile(String tableId, String rowId) throws AccessDeniedException,
      RequestFailureException, DatastoreFailureException,
      PermissionDeniedExceptionClient, EntityNotFoundExceptionClient {
    throw new IllegalStateException("Not implemented");
// commented out when we changed the file loading.
// TODO: re-envision, fix.
//    HttpServletRequest req = this.getThreadLocalRequest();
//    CallingContext cc = ContextFactory.getCallingContext(this, req);
//    try {
//      Relation tableInfo = DbTableFileInfo.getRelation(cc);
//      Entity entry = tableInfo.getEntity(rowId, cc);
//      String key = entry.getAsString(DbTableFileInfo.KEY);
//      // we also want to delete all the media files for this table. So get
//      // them.
//      List<FileSummaryClient> mediaFiles = getMedialFilesKey(tableId, key);
//      // first we want to get a new etag
//      String dataETag = entry.getString(DbTableEntry.DATA_ETAG);
//      dataETag = Long.toString(System.currentTimeMillis());
//      entry.set(DbTableEntry.DATA_ETAG, dataETag);
//      entry.set(DbTable.ROW_VERSION, CommonFieldsBase.newUri());
//      entry.set(DbTable.DELETED, true);
//
//      /*
//       * // get the row and mark it as deleted List<String> rowIds = new
//       * ArrayList<String>(); rowIds.add(rowId); List<Entity> rows =
//       * DbTable.query(tableInfo, rowIds, cc); // there should only be on row as
//       * currently implemented for (Entity row : rows) {
//       * row.set(DbTable.ROW_VERSION, CommonFieldsBase.newUri());
//       * row.set(DbTable.DELETED, true); }
//       */
//
//      // TODO log rows for deleting files
//
//      // update db
//      // Relation.putEntities(rows, cc);
//      entry.put(cc);
//      // and now do the same for each of the media files.
//      for (FileSummaryClient sum : mediaFiles) {
//        deleteTableFile(sum.getTableId(), sum.getId());
//      }
//
//    } catch (ODKDatastoreException e) {
//      e.printStackTrace();
//      throw new DatastoreFailureException(e);
//    }

  }

}
