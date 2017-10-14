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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.UriBuilder;

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
import org.opendatakit.aggregate.odktables.DataManager.WebsafeRows;
import org.opendatakit.aggregate.odktables.FileManager;
import org.opendatakit.aggregate.odktables.TableManager;
import org.opendatakit.aggregate.odktables.api.FileService;
import org.opendatakit.aggregate.odktables.api.InstanceFileService;
import org.opendatakit.aggregate.odktables.api.OdkTables;
import org.opendatakit.aggregate.odktables.api.RealizedTableService;
import org.opendatakit.aggregate.odktables.api.TableService;
import org.opendatakit.aggregate.odktables.entity.UtilTransforms;
import org.opendatakit.aggregate.odktables.exception.BadColumnNameException;
import org.opendatakit.aggregate.odktables.exception.ETagMismatchException;
import org.opendatakit.aggregate.odktables.exception.InconsistentStateException;
import org.opendatakit.aggregate.odktables.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.relation.DbColumnDefinitions;
import org.opendatakit.aggregate.odktables.relation.DbTableFileInfo;
import org.opendatakit.aggregate.odktables.relation.DbTableFiles;
import org.opendatakit.aggregate.odktables.relation.DbTableInstanceFiles;
import org.opendatakit.aggregate.odktables.rest.entity.Row;
import org.opendatakit.aggregate.odktables.rest.entity.TableEntry;
import org.opendatakit.aggregate.odktables.rest.entity.TableRole.TablePermission;
import org.opendatakit.aggregate.odktables.security.TablesUserPermissions;
import org.opendatakit.aggregate.odktables.security.TablesUserPermissionsImpl;
import org.opendatakit.common.datamodel.BinaryContent;
import org.opendatakit.common.ermodel.BlobEntitySet;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.QueryResumePoint;
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

  private WebsafeRows getRows(String tableId, QueryResumePoint resumePoint) throws AccessDeniedException,
      RequestFailureException, DatastoreFailureException, PermissionDeniedExceptionClient,
      EntityNotFoundExceptionClient, BadColumnNameExceptionClient {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);
    try { // Must use try so that you can catch the ODK specific errors.
      TablesUserPermissions userPermissions = new TablesUserPermissionsImpl(cc);
      String appId = ServerPreferencesProperties.getOdkTablesAppId(cc);
      DataManager dm = new DataManager(appId, tableId, userPermissions, cc);
      return dm.getRows(resumePoint, 100);
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
      TablesUserPermissions userPermissions = new TablesUserPermissionsImpl(cc);
      String appId = ServerPreferencesProperties.getOdkTablesAppId(cc);
      DataManager dm = new DataManager(appId, tableId, userPermissions, cc);
      Row row = dm.getRow(rowId);

      TableContentsClient tcc = new TableContentsClient();
      tcc.columnNames = this.getColumnNames(tableId);
      ArrayList<RowClient> rows = new ArrayList<RowClient>();
      rows.add(UtilTransforms.transform(row));
      tcc.rows = rows;
      tcc.websafeBackwardCursor = null;
      tcc.websafeRefetchCursor = null;
      tcc.websafeResumeCursor = null;
      tcc.hasMore = false;
      tcc.hasPrior = false;
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
      userPermissions = new TablesUserPermissionsImpl(cc);
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
  public void deleteRow(String tableId, String rowId, String rowETag) throws AccessDeniedException,
      RequestFailureException, DatastoreFailureException, PermissionDeniedExceptionClient,
      EntityNotFoundExceptionClient, BadColumnNameExceptionClient {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);
    try { // Must use try so that you can catch the ODK specific errors.
      TablesUserPermissions userPermissions = new TablesUserPermissionsImpl(cc);
      String appId = ServerPreferencesProperties.getOdkTablesAppId(cc);
      DataManager dm = new DataManager(appId, tableId, userPermissions, cc);
      dm.deleteRow(rowId, rowETag);
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
    } catch (ETagMismatchException e) {
      e.printStackTrace();
      throw new RequestFailureException(e);
    }

  }

  /**
   * Gets the element_keys of the columns.
   * The element_names are insufficient for displaying the row contents.
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
      TablesUserPermissions userPermissions = new TablesUserPermissionsImpl(cc);
      String appId = ServerPreferencesProperties.getOdkTablesAppId(cc);
      TableManager tm = new TableManager(appId, userPermissions, cc);
      TableEntry entry = tm.getTable(tableId);
      if ( entry == null || entry.getSchemaETag() == null ) {
        throw new ODKEntityNotFoundException();
      }
      ArrayList<String> elementKeys = DbColumnDefinitions.queryForDbColumnNames(tableId,
          entry.getSchemaETag(), cc);
      return elementKeys;
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
  public TableContentsClient getTableContents(String tableId, String resumeCursor) throws AccessDeniedException,
      RequestFailureException, DatastoreFailureException, PermissionDeniedExceptionClient,
      EntityNotFoundExceptionClient, BadColumnNameExceptionClient {
    TableContentsClient tcc = new TableContentsClient();
    
    WebsafeRows websafeResult = getRows(tableId, 
    		QueryResumePoint.fromWebsafeCursor(resumeCursor));
    List<Row> rows = websafeResult.rows;
    tcc.rows = transformRows(rows);
    tcc.columnNames = getColumnNames(tableId);
    tcc.websafeBackwardCursor = websafeResult.websafeBackwardCursor;
    tcc.websafeRefetchCursor = websafeResult.websafeRefetchCursor;
    tcc.websafeResumeCursor = websafeResult.websafeResumeCursor;
    tcc.hasMore = websafeResult.hasMore;
    tcc.hasPrior = websafeResult.hasPrior;

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
      TablesUserPermissions userPermissions = new TablesUserPermissionsImpl(cc);
      String appId = ServerPreferencesProperties.getOdkTablesAppId(cc);
      TableManager tm = new TableManager(appId, userPermissions, cc);
      List<DbTableFileInfo.DbTableFileInfoEntity> entities = DbTableFileInfo.queryForAllOdkClientVersionsOfAppLevelFiles(cc);
      DbTableFiles dbTableFiles = new DbTableFiles(cc);

      UriBuilder ub;
      try {
        ub = UriBuilder.fromUri(new URI(cc.getServerURL() + BasicConsts.FORWARDSLASH + ServletConsts.ODK_TABLES_SERVLET_BASE_PATH));
      } catch (URISyntaxException e) {
        e.printStackTrace();
        throw new RequestFailureException(e);
      }
      ub.path(OdkTables.class, "getFilesService");

      ArrayList<FileSummaryClient> completedSummaries = new ArrayList<FileSummaryClient>();
      for (DbTableFileInfo.DbTableFileInfoEntity entry : entities) {
        BlobEntitySet blobEntitySet = dbTableFiles.getBlobEntitySet(entry.getId(), cc);
        if (blobEntitySet.getAttachmentCount(cc) != 1) {
          continue;
        }

        String odkClientVersion = entry.getOdkClientVersion();

        UriBuilder tmp = ub.clone().path(FileService.class, "getFile");
        URI getFile = tmp.build(appId, odkClientVersion, entry.getPathToFile());
        String downloadUrl;
        try {
          downloadUrl = getFile.toURL().toExternalForm() + "?" + FileService.PARAM_AS_ATTACHMENT + "=true";
        } catch (MalformedURLException e) {
          e.printStackTrace();
          throw new RequestFailureException("Unable to convert to URL");
        }

        FileSummaryClient sum = new FileSummaryClient(entry.getPathToFile(),
            blobEntitySet.getContentType(1, cc),
            blobEntitySet.getContentLength(1, cc),
            entry.getId(), odkClientVersion, "", downloadUrl);
        completedSummaries.add(sum);
      }
      Collections.sort(completedSummaries, new Comparator<FileSummaryClient>(){

        @Override
        public int compare(FileSummaryClient arg0, FileSummaryClient arg1) {
          return arg0.getFilename().compareTo(arg1.getFilename());
        }});

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
      TablesUserPermissions userPermissions = new TablesUserPermissionsImpl(cc);
      String appId = ServerPreferencesProperties.getOdkTablesAppId(cc);
      TableManager tm = new TableManager(appId, userPermissions, cc);
      TableEntry table = tm.getTable(tableId);
      if (table == null || table.getSchemaETag() == null) { // you couldn't find the table
        throw new ODKEntityNotFoundException();
      }
      List<DbTableFileInfo.DbTableFileInfoEntity> entities = DbTableFileInfo.queryForAllOdkClientVersionsOfTableIdFiles(table.getTableId(), cc);
      DbTableFiles dbTableFiles = new DbTableFiles(cc);

      UriBuilder ub;
      try {
        ub = UriBuilder.fromUri(new URI(cc.getServerURL() + BasicConsts.FORWARDSLASH + ServletConsts.ODK_TABLES_SERVLET_BASE_PATH));
      } catch (URISyntaxException e) {
        e.printStackTrace();
        throw new RequestFailureException(e);
      }
      ub.path(OdkTables.class, "getFilesService");

      ArrayList<FileSummaryClient> completedSummaries = new ArrayList<FileSummaryClient>();
      for (DbTableFileInfo.DbTableFileInfoEntity entry : entities) {
        BlobEntitySet blobEntitySet = dbTableFiles.getBlobEntitySet(entry.getId(), cc);
        if (blobEntitySet.getAttachmentCount(cc) != 1) {
          continue;
        }

        String odkClientVersion = entry.getOdkClientVersion();

        UriBuilder tmp = ub.clone().path(FileService.class, "getFile");
        URI getFile = tmp.build(appId, odkClientVersion, entry.getPathToFile());
        String downloadUrl;
        try {
          downloadUrl = getFile.toURL().toExternalForm() + "?" + FileService.PARAM_AS_ATTACHMENT + "=true";
        } catch (MalformedURLException e) {
          e.printStackTrace();
          throw new RequestFailureException("Unable to convert to URL");
        }

        FileSummaryClient sum = new FileSummaryClient(entry.getPathToFile(),
            blobEntitySet.getContentType(1, cc),
            blobEntitySet.getContentLength(1, cc),
            entry.getId(), odkClientVersion, tableId, downloadUrl);
        completedSummaries.add(sum);
      }

      Collections.sort(completedSummaries, new Comparator<FileSummaryClient>(){

        @Override
        public int compare(FileSummaryClient arg0, FileSummaryClient arg1) {
          return arg0.getFilename().compareTo(arg1.getFilename());
        }});

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
      TablesUserPermissions userPermissions = new TablesUserPermissionsImpl(cc);
      String appId = ServerPreferencesProperties.getOdkTablesAppId(cc);
      TableManager tm = new TableManager(appId, userPermissions, cc);
      TableEntry table = tm.getTable(tableId);
      if (table == null || table.getSchemaETag() == null) { // you couldn't find the table
        throw new ODKEntityNotFoundException();
      }
      String schemaETag = table.getSchemaETag();

      DbTableInstanceFiles blobStore = new DbTableInstanceFiles(tableId, cc);
      List<BinaryContent> contents = blobStore.getAllBinaryContents(cc);

      UriBuilder ub;
      try {
        ub = UriBuilder.fromUri(new URI(cc.getServerURL() + BasicConsts.FORWARDSLASH + ServletConsts.ODK_TABLES_SERVLET_BASE_PATH));
      } catch (URISyntaxException e) {
        e.printStackTrace();
        throw new RequestFailureException(e);
      }
      ub.path(OdkTables.class, "getTablesService");

      ArrayList<FileSummaryClient> completedSummaries = new ArrayList<FileSummaryClient>();
      for (BinaryContent entry : contents) {
        if (entry.getUnrootedFilePath() == null) {
          continue;
        }

        // the rowId is the top-level auri for this record
        String rowId = entry.getTopLevelAuri();

        UriBuilder tmp = ub.clone().path(TableService.class, "getRealizedTable").path(RealizedTableService.class,"getInstanceFiles").path(InstanceFileService.class, "getFile");
        URI getFile = tmp.build(appId, tableId, schemaETag, rowId, entry.getUnrootedFilePath());
        String downloadUrl = getFile.toASCIIString() + "?" + FileService.PARAM_AS_ATTACHMENT + "=true";

        FileSummaryClient sum = new FileSummaryClient(entry.getUnrootedFilePath(),
            entry.getContentType(),
            entry.getContentLength(),
            entry.getUri(), null, tableId, downloadUrl);
        sum.setInstanceId(entry.getTopLevelAuri());
        completedSummaries.add(sum);
      }

      Collections.sort(completedSummaries, new Comparator<FileSummaryClient>(){

        @Override
        public int compare(FileSummaryClient arg0, FileSummaryClient arg1) {
          return arg0.getFilename().compareTo(arg1.getFilename());
        }});

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

  @Override
  public void deleteAppLevelFile(String odkClientApiVersion, String filepath) throws AccessDeniedException,
      RequestFailureException, DatastoreFailureException, PermissionDeniedExceptionClient,
      EntityNotFoundExceptionClient {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);
    try {
      TablesUserPermissions userPermissions = new TablesUserPermissionsImpl(cc);
      String appId = ServerPreferencesProperties.getOdkTablesAppId(cc);
      
      FileManager fm = new FileManager(appId, cc);
      fm.deleteFile(odkClientApiVersion, DbTableFileInfo.NO_TABLE_ID, filepath);
      return;
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

  @Override
  public void deleteTableFile(String odkClientApiVersion, String tableId, String filepath) throws AccessDeniedException,
      RequestFailureException, DatastoreFailureException, PermissionDeniedExceptionClient,
      EntityNotFoundExceptionClient {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);
    try {
      TablesUserPermissions userPermissions = new TablesUserPermissionsImpl(cc);
      String appId = ServerPreferencesProperties.getOdkTablesAppId(cc);

      userPermissions.checkPermission(appId, tableId, TablePermission.WRITE_PROPERTIES);

      FileManager fm = new FileManager(appId, cc);
      fm.deleteFile(odkClientApiVersion, tableId, filepath);
      return;
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

  @Override
  public void deleteInstanceFile(String tableId, String rowId, String filepath)
      throws AccessDeniedException, RequestFailureException, DatastoreFailureException,
      PermissionDeniedExceptionClient, EntityNotFoundExceptionClient {

    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);
    try {
      TablesUserPermissions userPermissions = new TablesUserPermissionsImpl(cc);
      String appId = ServerPreferencesProperties.getOdkTablesAppId(cc);
      TableManager tm = new TableManager(appId, userPermissions, cc);
      TableEntry table = tm.getTable(tableId);
      if (table == null || table.getSchemaETag() == null) { // you couldn't find the table
        throw new ODKEntityNotFoundException();
      }
      if( !filepath.startsWith(rowId + "/")) {
        throw new RequestFailureException("filename does not start with the instanceId");
      }

      DbTableInstanceFiles blobStore = new DbTableInstanceFiles(tableId, cc);
      BlobEntitySet blobEntitySet = blobStore.getBlobEntitySet(filepath, cc);

      blobEntitySet.remove(cc);
      return;
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

}
