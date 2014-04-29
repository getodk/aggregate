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
import org.opendatakit.aggregate.client.exception.ETagMismatchExceptionClient;
import org.opendatakit.aggregate.client.exception.EntityNotFoundExceptionClient;
import org.opendatakit.aggregate.client.exception.PermissionDeniedExceptionClient;
import org.opendatakit.aggregate.client.exception.RequestFailureException;
import org.opendatakit.aggregate.client.odktables.FileSummaryClient;
import org.opendatakit.aggregate.client.odktables.RowClient;
import org.opendatakit.aggregate.client.odktables.ServerDataService;
import org.opendatakit.aggregate.client.odktables.TableContentsClient;
import org.opendatakit.aggregate.client.odktables.TableContentsForFilesClient;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.odktables.DataManager;
import org.opendatakit.aggregate.odktables.TableManager;
import org.opendatakit.aggregate.odktables.api.FileService;
import org.opendatakit.aggregate.odktables.entity.UtilTransforms;
import org.opendatakit.aggregate.odktables.exception.BadColumnNameException;
import org.opendatakit.aggregate.odktables.exception.InconsistentStateException;
import org.opendatakit.aggregate.odktables.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.relation.DbColumnDefinitions;
import org.opendatakit.aggregate.odktables.relation.DbTableFileInfo;
import org.opendatakit.aggregate.odktables.relation.DbTableFiles;
import org.opendatakit.aggregate.odktables.relation.DbTableInstanceFiles;
import org.opendatakit.aggregate.odktables.rest.entity.Row;
import org.opendatakit.aggregate.odktables.rest.entity.TableEntry;
import org.opendatakit.aggregate.odktables.security.TablesUserPermissions;
import org.opendatakit.aggregate.odktables.security.TablesUserPermissionsImpl;
import org.opendatakit.common.datamodel.BinaryContent;
import org.opendatakit.common.ermodel.BlobEntitySet;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.client.exception.DatastoreFailureException;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;
import org.opendatakit.common.security.client.exception.AccessDeniedException;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.constants.BasicConsts;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * For ODKTables.
 *
 * @author sudar.sam@gmail.com
 *
 */
public class ServerDataServiceImpl extends RemoteServiceServlet implements ServerDataService {

  /**
	 *
	 */
  private static final long serialVersionUID = -5051558217315955180L;

  @Override
  public ArrayList<RowClient> getRows(String tableId) throws AccessDeniedException,
      RequestFailureException, DatastoreFailureException, PermissionDeniedExceptionClient,
      EntityNotFoundExceptionClient, BadColumnNameExceptionClient {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);
    try { // Must use try so that you can catch the ODK specific errors.
      TablesUserPermissions userPermissions = new TablesUserPermissionsImpl(cc.getCurrentUser()
          .getUriUser(), cc);
      String appId = ServerPreferencesProperties.getOdkTablesAppId(cc);
      DataManager dm = new DataManager(appId, tableId, userPermissions, cc);
      List<Row> rows = dm.getRows();
      return transformRows(rows);
    } catch (ODKEntityNotFoundException e) {
      e.printStackTrace();
      throw new EntityNotFoundExceptionClient(e);
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      throw new DatastoreFailureException(e);
    } catch (BadColumnNameException e) {
      e.printStackTrace();
      throw new BadColumnNameExceptionClient(e);
    } catch (InconsistentStateException e) {
      e.printStackTrace();
      throw new RequestFailureException(e);
    } catch (ODKTaskLockException e) {
      e.printStackTrace();
      throw new RequestFailureException(e);
    } catch (PermissionDeniedException e) {
      e.printStackTrace();
      throw new PermissionDeniedExceptionClient(e);
    }
  }

  @Override
  public TableContentsClient getRow(String tableId, String rowId) throws AccessDeniedException,
      RequestFailureException, DatastoreFailureException, PermissionDeniedExceptionClient,
      EntityNotFoundExceptionClient, BadColumnNameExceptionClient {
    try {
      HttpServletRequest req = this.getThreadLocalRequest();
      CallingContext cc = ContextFactory.getCallingContext(this, req);
      TablesUserPermissions userPermissions = new TablesUserPermissionsImpl(cc.getCurrentUser()
          .getUriUser(), cc);
      String appId = ServerPreferencesProperties.getOdkTablesAppId(cc);
      DataManager dm = new DataManager(appId, tableId, userPermissions, cc);
      Row row = dm.getRow(rowId);

      TableContentsClient tcc = new TableContentsClient();
      tcc.columnNames = this.getColumnNames(tableId);
      ArrayList<RowClient> rows = new ArrayList<RowClient>();
      rows.add(UtilTransforms.transform(row));
      tcc.rows = rows;
      return tcc;
    } catch (ODKEntityNotFoundException e) {
      e.printStackTrace();
      throw new EntityNotFoundExceptionClient(e);
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      throw new DatastoreFailureException(e);
    } catch (InconsistentStateException e) {
      e.printStackTrace();
      throw new RequestFailureException(e);
    } catch (ODKTaskLockException e) {
      e.printStackTrace();
      throw new RequestFailureException(e);
    } catch (PermissionDeniedException e) {
      e.printStackTrace();
      throw new PermissionDeniedExceptionClient(e);
    } catch (BadColumnNameException e) {
      e.printStackTrace();
      throw new BadColumnNameExceptionClient(e);
    }
  }

  @Override
  public RowClient createOrUpdateRow(String tableId, String rowId, RowClient row)
      throws AccessDeniedException, RequestFailureException, DatastoreFailureException,
      ETagMismatchExceptionClient, PermissionDeniedExceptionClient,
      EntityNotFoundExceptionClient {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);
    TablesUserPermissions userPermissions;
    try {
      userPermissions = new TablesUserPermissionsImpl(cc.getCurrentUser()
          .getUriUser(), cc);
      String appId = ServerPreferencesProperties.getOdkTablesAppId(cc);

      return ServerOdkTablesUtil.createOrUpdateRow(appId, tableId, rowId, row, userPermissions, cc);
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      throw new DatastoreFailureException(e);
    } catch (PermissionDeniedException e) {
      e.printStackTrace();
      throw new PermissionDeniedExceptionClient(e);
    } catch (ODKTaskLockException e) {
      e.printStackTrace();
      throw new RequestFailureException(e);
    } catch (InconsistentStateException e) {
      e.printStackTrace();
      throw new RequestFailureException(e);
    }
  }

  @Override
  public void deleteRow(String tableId, String rowId) throws AccessDeniedException,
      RequestFailureException, DatastoreFailureException, PermissionDeniedExceptionClient,
      EntityNotFoundExceptionClient, BadColumnNameExceptionClient {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);
    try { // Must use try so that you can catch the ODK specific errors.
      TablesUserPermissions userPermissions = new TablesUserPermissionsImpl(cc.getCurrentUser()
          .getUriUser(), cc);
      String appId = ServerPreferencesProperties.getOdkTablesAppId(cc);
      DataManager dm = new DataManager(appId, tableId, userPermissions, cc);
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
    } catch (InconsistentStateException e) {
      e.printStackTrace();
      throw new RequestFailureException(e);
    } catch (BadColumnNameException e) {
      e.printStackTrace();
      throw new BadColumnNameExceptionClient(e);
    }

  }

  /**
   * Gets the element_names of the columns.
   *
   * @return List<String> of the column names
   * @throws PermissionDeniedExceptionClient
   * @throws RequestFailureException
   */
  @Override
  public ArrayList<String> getColumnNames(String tableId) throws DatastoreFailureException,
      EntityNotFoundExceptionClient, PermissionDeniedExceptionClient, RequestFailureException {

    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);
    try {
      TablesUserPermissions userPermissions = new TablesUserPermissionsImpl(cc.getCurrentUser()
          .getUriUser(), cc);
      String appId = ServerPreferencesProperties.getOdkTablesAppId(cc);
      TableManager tm = new TableManager(appId, userPermissions, cc);
      TableEntry entry = tm.getTable(tableId);
      ArrayList<String> columnNames = DbColumnDefinitions.queryForColumnNames(tableId,
          entry.getSchemaETag(), cc);
      return columnNames;
    } catch (ODKEntityNotFoundException e) {
      e.printStackTrace();
      throw new EntityNotFoundExceptionClient(e);
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

  private ArrayList<RowClient> transformRows(List<Row> rows) {
    ArrayList<RowClient> clientRows = new ArrayList<RowClient>();
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
   * NB: this does NOT use the same {@link Datamanager} class as the rest of the
   * DataService methods, as this is considered accessing a unique table that is
   * part of the back-end, rather than one of the tables that is created by the
   * user.
   *
   * @param tableId
   *          the string uid of the table whose files you want
   */
  @Override
  public ArrayList<FileSummaryClient> getNonMediaFiles(String tableId) throws AccessDeniedException,
      RequestFailureException, PermissionDeniedExceptionClient,
      EntityNotFoundExceptionClient {
    throw new IllegalStateException("Not implemented");
    // this is commented out after we changed the file around.
    // TODO: update this.
    // HttpServletRequest req = this.getThreadLocalRequest();
    // CallingContext cc = ContextFactory.getCallingContext(this, req);
    // try {
    // DbTableFiles blobSetRelation = new DbTableFiles(cc);
    // List<Row> rows = EntityConverter.toRowsFromFileInfo(
    // DbTableFileInfo.queryForNonMediaFiles(tableId, cc));
    // List<FileSummaryClient> nonMediaFiles =
    // new ArrayList<FileSummaryClient>();
    // for (Row row : rows) {
    // FileSummaryClient summary = ServerOdkTablesUtil
    // .getFileSummaryClientFromRow(row, tableId, blobSetRelation, cc);
    // nonMediaFiles.add(summary);
    // }
    // return nonMediaFiles;
    // } catch (ODKEntityNotFoundException e) {
    // e.printStackTrace();
    // throw new EntityNotFoundExceptionClient(e);
    // } catch (ODKDatastoreException e) {
    // e.printStackTrace();
    // throw new DatastoreFailureException(e);
    // }
  }

  @Override
  public ArrayList<FileSummaryClient> getMedialFilesKey(String tableId, String key)
      throws AccessDeniedException, RequestFailureException, DatastoreFailureException,
      PermissionDeniedExceptionClient, EntityNotFoundExceptionClient {
    throw new IllegalStateException("Not implemented");
    // this is commented out after we changed the file around.
    // TODO: see if we actually need this.
    // HttpServletRequest req = this.getThreadLocalRequest();
    // CallingContext cc = ContextFactory.getCallingContext(this, req);
    // try {
    // DbTableFiles blobSetRelation = new DbTableFiles(cc);
    // List<Row> rows = EntityConverter.toRowsFromFileInfo(
    // DbTableFileInfo.queryForMediaFiles(tableId, key, cc));
    // List<FileSummaryClient> mediaFiles =
    // new ArrayList<FileSummaryClient>();
    // for (Row row : rows) {
    // FileSummaryClient summary = ServerOdkTablesUtil
    // .getFileSummaryClientFromRow(row, tableId, blobSetRelation, cc);
    // mediaFiles.add(summary);
    // }
    // return mediaFiles;
    // } catch (ODKEntityNotFoundException e) {
    // e.printStackTrace();
    // throw new EntityNotFoundExceptionClient(e);
    // } catch (ODKDatastoreException e) {
    // e.printStackTrace();
    // throw new DatastoreFailureException(e);
    // }
  }

  // TODO make this work. atm isn't working.
  // public List<OdkTablesKeyValueStoreEntry> testFileGets(String tableId)
  // throws PermissionDeniedExceptionClient, DatastoreFailureException,
  // RequestFailureException,
  // EntityNotFoundExceptionClient, AccessDeniedException,
  // JsonGenerationException, IOException {
  // HttpServletRequest req = this.getThreadLocalRequest();
  // CallingContext cc = ContextFactory.getCallingContext(this, req);
  //
  // try {
  // List<RowClient> infoRows = getNonMediaFiles(tableId);
  // TableManager tm = new TableManager(cc);
  // TableEntry table = tm.getTable(tableId);
  // String tableName = table.getTableName();
  // DbTableFiles blobSetRelation = new DbTableFiles(cc);
  // List<OdkTablesKeyValueStoreEntry> entries = new
  // ArrayList<OdkTablesKeyValueStoreEntry>();
  // for (RowClient row : infoRows) {
  // // we only want the non-deleted rows
  // if (!row.isDeleted()) {
  // // the KeyValueStoreEntry object is the same for every entry. However,
  // // for files you need to create a FileManifestEntry for the value.
  // OdkTablesKeyValueStoreEntry entry = new OdkTablesKeyValueStoreEntry();
  // entry.tableId = tableId;
  // entry.tableName = tableName;
  // entry.key = row.getValues().get(DbTableFileInfo.KEY);
  // entry.type = row.getValues().get(DbTableFileInfo.VALUE_TYPE);
  // // if it's a file, make the file manifest entry.
  // if (entry.type.equalsIgnoreCase(DbTableFileInfo.Type.FILE.name)) {
  // OdkTablesFileManifestEntry fileEntry = new OdkTablesFileManifestEntry();
  // fileEntry.filename = blobSetRelation.getBlobEntitySet(
  // row.getValues().get(DbTableFileInfo.VALUE), cc).getUnrootedFilename(1, cc);
  // fileEntry.md5hash = blobSetRelation.getBlobEntitySet(
  // row.getValues().get(DbTableFileInfo.VALUE), cc).getContentHash(1, cc);
  // // now generate the download url. look at XFormsManifestXmlTable as
  // // an
  // // example of how Mitch did it.
  // Map<String, String> properties = new HashMap<String, String>();
  // properties.put(ServletConsts.BLOB_KEY,
  // row.getValues().get(DbTableFileInfo.VALUE));
  // properties.put(ServletConsts.AS_ATTACHMENT, "true");
  // String url = cc.getServerURL() + BasicConsts.FORWARDSLASH
  // + OdkTablesTableFileDownloadServlet.ADDR;
  // fileEntry.downloadUrl = HtmlUtil.createLinkWithProperties(url, properties);
  // // now convert this object to json and set it to the entry's value.
  // ObjectMapper mapper = new ObjectMapper();
  // entry.value = mapper.writeValueAsString(fileEntry);
  // } else {
  // // if it's not a file, we just set the value. as input.
  // entry.value = row.getValues().get(DbTableFileInfo.VALUE);
  // }
  // // and now add the completed entry to the list of entries
  // entries.add(entry);
  //
  // }
  // }
  // return entries;
  // } catch (ODKDatastoreException e) {
  // e.printStackTrace();
  // throw new DatastoreFailureException(e);
  // }
  // }

  /**
   * Get the list of columns that are in the DbTableFileInfo table. This is
   * defined in server code and thus is never in danger of throwing
   * DatastoreExceptions.
   */
  @Override
  public ArrayList<String> getFileRowInfoColumnNames() {
    List<DataField> exposedColumnNames = DbTableFileInfo.exposedColumnNames;
    ArrayList<String> columnNames = new ArrayList<String>();
    for (DataField f : exposedColumnNames) {
      columnNames.add(f.getName());
    }
    return columnNames;
  }

  @Override
  public TableContentsClient getTableContents(String tableId) throws AccessDeniedException,
      RequestFailureException, DatastoreFailureException, PermissionDeniedExceptionClient,
      EntityNotFoundExceptionClient, BadColumnNameExceptionClient {
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
  public TableContentsForFilesClient getAppLevelFileInfoContents()
      throws AccessDeniedException, RequestFailureException, DatastoreFailureException,
      PermissionDeniedExceptionClient, EntityNotFoundExceptionClient {
    TableContentsForFilesClient tcc = new TableContentsForFilesClient();
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);
    try {
      TablesUserPermissions userPermissions = new TablesUserPermissionsImpl(cc.getCurrentUser()
          .getUriUser(), cc);
      String appId = ServerPreferencesProperties.getOdkTablesAppId(cc);
      TableManager tm = new TableManager(appId, userPermissions, cc);
      List<DbTableFileInfo.DbTableFileInfoEntity> entities = DbTableFileInfo.queryForAllOdkClientVersionsOfAppLevelFiles(cc);
      DbTableFiles dbTableFiles = new DbTableFiles(cc);

      ArrayList<FileSummaryClient> completedSummaries = new ArrayList<FileSummaryClient>();
      for (DbTableFileInfo.DbTableFileInfoEntity entry : entities) {
        BlobEntitySet blobEntitySet = dbTableFiles.getBlobEntitySet(entry.getId(), cc);
        if (blobEntitySet.getAttachmentCount(cc) != 1) {
          continue;
        }
        String odkClientVersion = entry.getOdkClientVersion();
        String downloadUrl = cc.getServerURL() + BasicConsts.FORWARDSLASH
            + ServletConsts.ODK_TABLES_SERVLET_BASE_PATH + BasicConsts.FORWARDSLASH
            + appId + BasicConsts.FORWARDSLASH + FileService.SERVLET_PATH + BasicConsts.FORWARDSLASH
            + odkClientVersion + BasicConsts.FORWARDSLASH + entry.getPathToFile() + "?" + FileService.PARAM_AS_ATTACHMENT + "=true";
        FileSummaryClient sum = new FileSummaryClient(entry.getPathToFile(),
            blobEntitySet.getContentType(1, cc),
            blobEntitySet.getContentLength(1, cc),
            entry.getId(), odkClientVersion, "", downloadUrl);
        completedSummaries.add(sum);
      }
      tcc.files = completedSummaries;
      return tcc;
    } catch (ODKEntityNotFoundException e) {
      e.printStackTrace();
      throw new EntityNotFoundExceptionClient(e);
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
   * This method more or less gets the user-friendly data to be displayed. It
   * adds the correct filename and returns only the non-deleted rows.
   */
  @Override
  public TableContentsForFilesClient getTableFileInfoContents(String tableId)
      throws AccessDeniedException, RequestFailureException, DatastoreFailureException,
      PermissionDeniedExceptionClient, EntityNotFoundExceptionClient {
    TableContentsForFilesClient tcc = new TableContentsForFilesClient();
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);
    try {
      TablesUserPermissions userPermissions = new TablesUserPermissionsImpl(cc.getCurrentUser()
          .getUriUser(), cc);
      String appId = ServerPreferencesProperties.getOdkTablesAppId(cc);
      TableManager tm = new TableManager(appId, userPermissions, cc);
      TableEntry table = tm.getTable(tableId);
      if (table == null) { // you couldn't find the table
        throw new ODKEntityNotFoundException();
      }
      List<DbTableFileInfo.DbTableFileInfoEntity> entities = DbTableFileInfo.queryForAllOdkClientVersionsOfTableIdFiles(table.getTableId(), cc);
      DbTableFiles dbTableFiles = new DbTableFiles(cc);

      ArrayList<FileSummaryClient> completedSummaries = new ArrayList<FileSummaryClient>();
      for (DbTableFileInfo.DbTableFileInfoEntity entry : entities) {
        BlobEntitySet blobEntitySet = dbTableFiles.getBlobEntitySet(entry.getId(), cc);
        if (blobEntitySet.getAttachmentCount(cc) != 1) {
          continue;
        }
        String odkClientVersion = entry.getOdkClientVersion();
        String downloadUrl = cc.getServerURL() + BasicConsts.FORWARDSLASH
            + ServletConsts.ODK_TABLES_SERVLET_BASE_PATH + BasicConsts.FORWARDSLASH
            + appId + BasicConsts.FORWARDSLASH + FileService.SERVLET_PATH + BasicConsts.FORWARDSLASH
            + odkClientVersion + BasicConsts.FORWARDSLASH + entry.getPathToFile() + "?" + FileService.PARAM_AS_ATTACHMENT + "=true";
        FileSummaryClient sum = new FileSummaryClient(entry.getPathToFile(),
            blobEntitySet.getContentType(1, cc),
            blobEntitySet.getContentLength(1, cc),
            entry.getId(), odkClientVersion, tableId, downloadUrl);
        completedSummaries.add(sum);
      }
      tcc.files = completedSummaries;
      return tcc;
    } catch (ODKEntityNotFoundException e) {
      e.printStackTrace();
      throw new EntityNotFoundExceptionClient(e);
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
   * This method more or less gets the user-friendly data to be displayed. It
   * adds the correct filename and returns only the non-deleted rows.
   */
  @Override
  public TableContentsForFilesClient getInstanceFileInfoContents(String tableId)
      throws AccessDeniedException, RequestFailureException, DatastoreFailureException,
      PermissionDeniedExceptionClient, EntityNotFoundExceptionClient {
    TableContentsForFilesClient tcc = new TableContentsForFilesClient();
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);
    try {
      TablesUserPermissions userPermissions = new TablesUserPermissionsImpl(cc.getCurrentUser()
          .getUriUser(), cc);
      String appId = ServerPreferencesProperties.getOdkTablesAppId(cc);
      TableManager tm = new TableManager(appId, userPermissions, cc);
      TableEntry table = tm.getTable(tableId);
      if (table == null) { // you couldn't find the table
        throw new ODKEntityNotFoundException();
      }

      DbTableInstanceFiles blobStore = new DbTableInstanceFiles(tableId, cc);
      List<BinaryContent> contents = blobStore.getAllBinaryContents(cc);

      ArrayList<FileSummaryClient> completedSummaries = new ArrayList<FileSummaryClient>();
      for (BinaryContent entry : contents) {
        if (entry.getUnrootedFilePath() == null) {
          continue;
        }
        String downloadUrl = cc.getServerURL() + BasicConsts.FORWARDSLASH
            + ServletConsts.ODK_TABLES_SERVLET_BASE_PATH + BasicConsts.FORWARDSLASH
            + appId + BasicConsts.FORWARDSLASH + "tables" + BasicConsts.FORWARDSLASH
            + tableId + BasicConsts.FORWARDSLASH

            + "attachments/file"  + BasicConsts.FORWARDSLASH + entry.getUnrootedFilePath() + "?" + FileService.PARAM_AS_ATTACHMENT + "=true";
        FileSummaryClient sum = new FileSummaryClient(entry.getUnrootedFilePath(),
            entry.getContentType(),
            entry.getContentLength(),
            entry.getUri(), null, tableId, downloadUrl);
        completedSummaries.add(sum);
      }
      tcc.files = completedSummaries;
      return tcc;
    } catch (ODKEntityNotFoundException e) {
      e.printStackTrace();
      throw new EntityNotFoundExceptionClient(e);
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
   * Deletes the file from the datastore. Currently just marks the row as
   * deleted. It assumes that only one person will be accessing these files at
   * the same, and doesn't lock to try and prevent concurrent access or anything
   * along those lines.
   * <p>
   * This is largely based on the {@link DataManager} deleteRow method.
   */
  @Override
  public void deleteTableFile(String tableId, String rowId) throws AccessDeniedException,
      RequestFailureException, DatastoreFailureException, PermissionDeniedExceptionClient,
      EntityNotFoundExceptionClient {
    throw new IllegalStateException("Not implemented");
    // commented out when we changed the file loading.
    // TODO: re-envision, fix.
    // HttpServletRequest req = this.getThreadLocalRequest();
    // CallingContext cc = ContextFactory.getCallingContext(this, req);
    // try {
    // Relation tableInfo = DbTableFileInfo.getRelation(cc);
    // Entity entry = tableInfo.getEntity(rowId, cc);
    // String key = entry.getAsString(DbTableFileInfo.KEY);
    // // we also want to delete all the media files for this table. So get
    // // them.
    // List<FileSummaryClient> mediaFiles = getMedialFilesKey(tableId, key);
    // // first we want to get a new etag
    // String dataETag = entry.getString(DbTableEntry.DATA_ETAG);
    // dataETag = Long.toString(System.currentTimeMillis());
    // entry.set(DbTableEntry.DATA_ETAG, dataETag);
    // entry.set(DbTable.ROW_VERSION, CommonFieldsBase.newUri());
    // entry.set(DbTable.DELETED, true);
    //
    // /*
    // * // get the row and mark it as deleted List<String> rowIds = new
    // * ArrayList<String>(); rowIds.add(rowId); List<Entity> rows =
    // * DbTable.query(tableInfo, rowIds, cc); // there should only be on row as
    // * currently implemented for (Entity row : rows) {
    // * row.set(DbTable.ROW_VERSION, CommonFieldsBase.newUri());
    // * row.set(DbTable.DELETED, true); }
    // */
    //
    // // TODO log rows for deleting files
    //
    // // update db
    // // Relation.putEntities(rows, cc);
    // entry.put(cc);
    // // and now do the same for each of the media files.
    // for (FileSummaryClient sum : mediaFiles) {
    // deleteTableFile(sum.getTableId(), sum.getId());
    // }
    //
    // } catch (ODKDatastoreException e) {
    // e.printStackTrace();
    // throw new DatastoreFailureException(e);
    // }

  }

}
