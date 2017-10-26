/*
 * Copyright (C) 2014 University of Washington
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
package org.opendatakit.aggregate.odktables;

import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opendatakit.aggregate.odktables.exception.FileNotFoundException;
import org.opendatakit.aggregate.odktables.relation.DbManifestETags;
import org.opendatakit.aggregate.odktables.relation.DbManifestETags.DbManifestETagEntity;
import org.opendatakit.aggregate.odktables.relation.DbTableFileInfo;
import org.opendatakit.aggregate.odktables.relation.DbTableFileInfo.DbTableFileInfoEntity;
import org.opendatakit.aggregate.odktables.relation.DbTableFiles;
import org.opendatakit.aggregate.odktables.relation.EntityCreator;
import org.opendatakit.aggregate.odktables.security.TablesUserPermissions;
import org.opendatakit.common.datamodel.BinaryContentManipulator.BlobSubmissionOutcome;
import org.opendatakit.common.ermodel.BlobEntitySet;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.constants.BasicConsts;

/**
 * Implementation of file management APIs.
 * 
 * Isolated here so that the differences between Mezuri and Aggregate can be
 * isolated to this one class.
 * 
 * @author mitchellsundt@gmail.com
 *
 */
public class FileManager {

  private static final Log LOGGER = LogFactory.getLog(FileManager.class);

  /**
   * The name of the folder that contains the files associated with a table in
   * an app.
   */
  private static final String TABLES_FOLDER = "tables";
  private static final String ASSETS_FOLDER = "assets";
  private static final String CSV_FOLDER = "csv";
  private static final String CSV_EXTENSION = "csv";

  private String appId;

  private CallingContext cc;

  public FileManager(String appId, CallingContext cc) {
    this.appId = appId;
    this.cc = cc;
  }

  public static String getPropertiesFilePath(String tableId) {
    return TABLES_FOLDER + BasicConsts.FORWARDSLASH + tableId + BasicConsts.FORWARDSLASH
        + "properties.csv";
  }

  /**
   * Retrieve the table id given the appRelativeFilePath. The first path segment
   * (position 0) is a directory under /sdcard/opendatakit/{app_id}/, as all
   * files must be associated with an app id. Not all files must be associated
   * with a table, however, so it parses to find the table id. If it is not
   * associated with a table, it returns DbTableFileInfo.NO_TABLE_ID
   * <p>
   * The convention is that any table-related file must be under:
   * /tables/tableid OR a csv file: /assets/csv/tableid....csv OR
   * /assets/csv/tableid/instances/...
   *
   * So the 2nd position (0 indexed) will be the table id if the first position
   * is "tables", and the 3rd position (0 indexed) will begin with the table id
   * if it is a csv file under the assets/csv directory. Otherwise, if there are
   * more than 3 positions, it should be the table id (and there should be
   * instance files underneath that are referenced by one or more of the csv
   * files).
   * 
   * And verify that the presumptive tableId does not contain a period. If the
   * data came via a Mac OSX system, there might be hidden .DS_Store directories
   * that we should not treat as table ids.
   * 
   * @param appRelativeFilePath
   * @return tableId or DbTableFileInfo.NO_TABLE_ID
   */
  public static String getTableIdForFilePath(String appRelativeFilePath) {
    String[] pathParts = appRelativeFilePath.split(BasicConsts.FORWARDSLASH);
    String tableId = DbTableFileInfo.NO_TABLE_ID;
    String firstFolder = pathParts[0];
    if ((pathParts.length >= 2) && firstFolder.equals(FileManager.TABLES_FOLDER)) {
      // ensure that we ignore sub-directories that have periods in them
      // e.g., .DS_Store.
      String secondFolder = pathParts[1];
      if (secondFolder.indexOf('.') == -1) {
        tableId = secondFolder;
      }
    } else if ((pathParts.length >= 3) && firstFolder.equals(FileManager.ASSETS_FOLDER)) {
      String secondFolder = pathParts[1];
      if (secondFolder.equals(FileManager.CSV_FOLDER)) {
        String fileName = pathParts[2];
        if (pathParts.length > 3) {
          // ensure that we ignore sub-directories that have periods in them
          // e.g., .DS_Store.
          if (fileName.indexOf('.') == -1) {
            tableId = fileName;
          }
        } else {
          String splits[] = fileName.split("\\.");
          if (splits[splits.length - 1].toLowerCase(Locale.ENGLISH)
              .equals(FileManager.CSV_EXTENSION)) {
            tableId = splits[0];
          }
        }
      }
    }
    return tableId;
  }

  public FileContentInfo getFile(String odkClientVersion, String tableId, String wholePath)
      throws ODKDatastoreException, FileNotFoundException, ODKTaskLockException {
    // DbTableFileInfo.NO_TABLE_ID -- means that we are working with app-level

    if (wholePath == null) {
      throw new IllegalArgumentException("filePath cannot be null!");
    }

    OdkTablesLockTemplate propsLock = new OdkTablesLockTemplate(tableId, null,
        ODKTablesTaskLockType.TABLES_NON_PERMISSIONS_CHANGES, OdkTablesLockTemplate.DelayStrategy.MEDIUM, cc);
    try {
      propsLock.acquire();

      // otherwise, it is an app-level file, and that is accessible to anyone
      // with
      // synchronize tables privileges

      List<DbTableFileInfoEntity> entities = DbTableFileInfo.queryForEntity(odkClientVersion,
          tableId, wholePath, cc);
      if (entities.size() > 1) {
        LOGGER.error("more than one entity for appId: " + appId + ", tableId: " + tableId
            + ", pathToFile: " + wholePath);
      } else if (entities.size() < 1) {
        throw new FileNotFoundException("No manifest entry found for: " + wholePath);
      }
      DbTableFileInfoEntity dbTableFileInfoRow = entities.get(0);
      String uri = dbTableFileInfoRow.getId();
      DbTableFiles dbTableFiles = new DbTableFiles(cc);
      BlobEntitySet blobEntitySet = dbTableFiles.getBlobEntitySet(uri, cc);
      // We should only ever have one, as wholePath is the primary key.
      if (blobEntitySet.getAttachmentCount(cc) > 1) {
        throw new IllegalStateException("More than one file specified for: " + wholePath);
      }
      if (blobEntitySet.getAttachmentCount(cc) < 1) {
        throw new FileNotFoundException("No file found for path: " + wholePath);
      }

      FileContentInfo fo = new FileContentInfo(blobEntitySet.getUnrootedFilename(1, cc),
          blobEntitySet.getContentType(1, cc), blobEntitySet.getContentLength(1, cc),
          blobEntitySet.getContentHash(1, cc), blobEntitySet.getBlob(1, cc));
      return fo;

    } finally {
      propsLock.release();
    }
  }

  public ConfigFileChangeDetail putFile(String odkClientVersion, String tableId, FileContentInfo fi,
      TablesUserPermissions userPermissions) throws ODKDatastoreException, ODKTaskLockException {

    if (fi.partialPath == null) {
      throw new IllegalArgumentException("partialPath cannot be null!");
    }

    // DbTableFileInfo.NO_TABLE_ID -- means that we are working with app-level

    OdkTablesLockTemplate propsLock = new OdkTablesLockTemplate(tableId, null,
        ODKTablesTaskLockType.TABLES_NON_PERMISSIONS_CHANGES, OdkTablesLockTemplate.DelayStrategy.MEDIUM, cc);
    try {
      propsLock.acquire();

      // -1) clear the eTag for the manifest of this tableId
      {
        try {
          DbManifestETagEntity etag;
          if (DbTableFileInfo.NO_TABLE_ID.equals(tableId)) {
            etag = DbManifestETags.getTableIdEntry(DbManifestETags.APP_LEVEL, cc);
          } else {
            etag = DbManifestETags.getTableIdEntry(tableId, cc);
          }
          etag.delete(cc);
        } catch (ODKEntityNotFoundException e) {
          // ignore...
        }
      }

      // 0) Delete anything that is already stored

      List<DbTableFileInfoEntity> entities = DbTableFileInfo.queryForEntity(odkClientVersion,
          tableId, fi.partialPath, cc);
      for (DbTableFileInfoEntity entity : entities) {

        String uri = entity.getId();
        DbTableFiles dbTableFiles = new DbTableFiles(cc);
        BlobEntitySet blobEntitySet = dbTableFiles.getBlobEntitySet(uri, cc);
        blobEntitySet.remove(cc);
        entity.delete(cc);
      }

      // We are going to store the file in two tables: 1) a user-friendly table
      // that relates an app and table id to the name of a file; 2) a table
      // that holds the actual blob.
      //
      // Table 1 is represented by DbTableFileInfo. Each row of this table
      // contains a uri, appid, tableid, and pathToFile.
      // Table 2 is a BlobEntitySet. The top level URI of this blob entity set
      // is the uri from table 1. Each blob set here has a single attachment
      // count of 1--the blob of the file itself. The pathToFile of this
      // attachment is null.
      //
      // So, now that we have retrieved the file from the request, we have two
      // things to do: 1) create an entry in the user-friendly table so we can
      // bet a uri. 2) add the file to the blob entity set, using the top level
      // uri as the row uri from table 1.
      //
      // 1) Create an entry in the user friendly table.
      EntityCreator ec = new EntityCreator();
      DbTableFileInfoEntity tableFileInfoRow = ec.newTableFileInfoEntity(odkClientVersion, tableId,
          fi.partialPath, userPermissions, cc);
      String rowUri = tableFileInfoRow.getId();

      // 2) Put the blob in the datastore.
      DbTableFiles dbTableFiles = new DbTableFiles(cc);
      // Although this is called an entity set, it in fact represents a single
      // file, because we have chosen to use it this way in this case. For more
      // information see the docs in DbTableFiles. We'll use the uri of the
      // corresponding row in the DbTableFileInfo table.
      BlobEntitySet instance = dbTableFiles.newBlobEntitySet(rowUri, cc);
      // TODO: this being set to true is probably where some sort of versioning
      // should happen.
      BlobSubmissionOutcome outcome = null;

      if (fi.fileBlob != null && fi.contentType != null) {
        outcome = instance.addBlob(fi.fileBlob, fi.contentType, null, true, cc);
      }
      // 3) persist the user-friendly table entry about the blob
      tableFileInfoRow.put(cc);

      switch (outcome) {
      case FILE_UNCHANGED:
        return ConfigFileChangeDetail.FILE_NOT_CHANGED;
      case NEW_FILE_VERSION:
        return ConfigFileChangeDetail.FILE_UPDATED;
      case COMPLETELY_NEW_FILE:
        return ConfigFileChangeDetail.FILE_NEWLY_CREATED;
      default:
        throw new IllegalStateException("Unexpected extra status for BlobSubmissionOutcome");
      }

    } finally {
      propsLock.release();
    }
  }

  public void deleteFile(String odkClientVersion, String tableId, String wholePath)
      throws ODKDatastoreException, ODKTaskLockException {

    if (wholePath == null) {
      throw new IllegalArgumentException("wholePath cannot be null!");
    }

    // DbTableFileInfo.NO_TABLE_ID -- means that we are working with app-level

    OdkTablesLockTemplate propsLock = new OdkTablesLockTemplate(tableId, null,
        ODKTablesTaskLockType.TABLES_NON_PERMISSIONS_CHANGES, OdkTablesLockTemplate.DelayStrategy.MEDIUM, cc);
    try {
      propsLock.acquire();

      // if we find nothing, we are happy.
      List<DbTableFileInfoEntity> entities = DbTableFileInfo.queryForEntity(odkClientVersion,
          tableId, wholePath, cc);

      if (!entities.isEmpty()) {
        // -1) clear the eTag for the manifest of this tableId
        try {
          DbManifestETagEntity etag;
          if (DbTableFileInfo.NO_TABLE_ID.equals(tableId)) {
            etag = DbManifestETags.getTableIdEntry(DbManifestETags.APP_LEVEL, cc);
          } else {
            etag = DbManifestETags.getTableIdEntry(tableId, cc);
          }
          etag.delete(cc);
        } catch (ODKEntityNotFoundException e) {
          // ignore...
        }
      }

      // 0) delete the matching entities
      for (DbTableFileInfoEntity entity : entities) {
        String uri = entity.getId();
        DbTableFiles dbTableFiles = new DbTableFiles(cc);
        BlobEntitySet blobEntitySet = dbTableFiles.getBlobEntitySet(uri, cc);
        blobEntitySet.remove(cc);
        entity.delete(cc);
      }

    } finally {
      propsLock.release();
    }
  }
}
