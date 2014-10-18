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
package org.opendatakit.aggregate.odktables.impl.api;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.constants.ErrorConsts;
import org.opendatakit.aggregate.odktables.api.FileService;
import org.opendatakit.aggregate.odktables.api.OdkTables;
import org.opendatakit.aggregate.odktables.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.relation.DbTableFileInfo;
import org.opendatakit.aggregate.odktables.relation.DbTableFileInfo.DbTableFileInfoEntity;
import org.opendatakit.aggregate.odktables.relation.DbTableFiles;
import org.opendatakit.aggregate.odktables.relation.EntityCreator;
import org.opendatakit.aggregate.odktables.rest.entity.TableRole.TablePermission;
import org.opendatakit.aggregate.odktables.security.TablesUserPermissions;
import org.opendatakit.common.datamodel.BinaryContentManipulator.BlobSubmissionOutcome;
import org.opendatakit.common.ermodel.BlobEntitySet;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;
import org.opendatakit.common.security.common.GrantedAuthorityName;
import org.opendatakit.common.security.server.SecurityServiceUtil;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.constants.BasicConsts;
import org.opendatakit.common.web.constants.HtmlConsts;

public class FileServiceImpl implements FileService {

  private static final Log LOGGER = LogFactory.getLog(FileServiceImpl.class);

  /**
   * The name of the folder that contains the files associated with a table in
   * an app.
   *
   * @see #getTableIdFromPathSegments(List)
   */
  private static final String TABLES_FOLDER = "tables";
  private static final String ASSETS_FOLDER = "assets";
  private static final String CSV_FOLDER = "csv";

  private final ServletContext sc;
  private final HttpServletRequest req;
  private final HttpHeaders headers;
  private final CallingContext cc;
  private final String appId;
  private final UriInfo info;
  private TablesUserPermissions userPermissions;

  public FileServiceImpl(ServletContext sc, HttpServletRequest req, HttpHeaders headers, UriInfo info, String appId, CallingContext cc)
      throws ODKEntityNotFoundException, ODKDatastoreException, PermissionDeniedException, ODKTaskLockException {
    this.sc = sc;
    this.req = req;
    this.headers = headers;
    this.cc = cc;
    this.appId = appId;
    this.info = info;
    this.userPermissions = ContextFactory.getTablesUserPermissions(cc);

  }

  @Override
  public Response getFile(@PathParam("odkClientVersion") String odkClientVersion, @PathParam("filePath") List<PathSegment> segments, @QueryParam(PARAM_AS_ATTACHMENT) String asAttachment) throws IOException, ODKTaskLockException, PermissionDeniedException, ODKDatastoreException {

    // First we need to get the table id from the path. We're
    // going to be assuming that you're passing the entire path of the file
    // under /sdcard/opendatakit/appId/  e.g., tables/tableid/the/rest/of/path.
    // So we'll reclaim the tidbits and then reconstruct the entire path.
    // If you are getting general files, there will be no recoverable tableId,
    // and these are then app-level files.
    if (segments.size() < 1) {
      return Response.status(Status.BAD_REQUEST).entity(FileService.ERROR_MSG_INSUFFICIENT_PATH).build();
    }
    String tableId = getTableIdFromPathSegments(segments);
    String wholePath = constructPathFromSegments(segments);

    byte[] fileBlob;
    String contentType;
    Long contentLength;
    try {
      // DbTableFileInfo.NO_TABLE_ID -- means that we are working with app-level permissions
      if ( !DbTableFileInfo.NO_TABLE_ID.equals(tableId) ) {
        userPermissions.checkPermission(appId, tableId, TablePermission.READ_PROPERTIES);
      }
      // otherwise, it is an app-level file, and that is accessible to anyone with synchronize tables privileges

      List<DbTableFileInfoEntity> entities = DbTableFileInfo.queryForEntity(odkClientVersion, tableId, wholePath, cc);
      if (entities.size() > 1) {
        Log log = LogFactory.getLog(DbTableFileInfo.class);
        log.error("more than one entity for appId: " + appId + ", tableId: " + tableId
            + ", pathToFile: " + wholePath);
      } else if (entities.size() < 1) {
        return Response.status(Status.NOT_FOUND).entity("No manifest entry found for: " + wholePath).build();
      }
      DbTableFileInfoEntity dbTableFileInfoRow = entities.get(0);
      String uri = dbTableFileInfoRow.getId();
      DbTableFiles dbTableFiles = new DbTableFiles(cc);
      BlobEntitySet blobEntitySet = dbTableFiles.getBlobEntitySet(uri, cc);
      // We should only ever have one, as wholePath is the primary key.
      if (blobEntitySet.getAttachmentCount(cc) > 1) {
        return Response.status(Status.INTERNAL_SERVER_ERROR).entity("More than one file specified for: " + wholePath).build();
      }
      if (blobEntitySet.getAttachmentCount(cc) < 1) {
        return Response.status(Status.NOT_FOUND).entity("No file found for path: " + wholePath).build();
      }
      fileBlob = blobEntitySet.getBlob(1, cc);
      contentType = blobEntitySet.getContentType(1, cc);
      contentLength = blobEntitySet.getContentLength(1, cc);
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Unable to retrieve attachment and access attributes for: " + wholePath).build();
    } catch (PermissionDeniedException e) {
      LOGGER.error(("ODKTables file upload permissions error: " + e.getMessage()));
      return Response.status(Status.UNAUTHORIZED).entity("Permission denied").build();
    }
    // And now prepare everything to be returned to the caller.
    if (fileBlob != null && contentType != null && contentLength != null && contentLength != 0L) {
      ResponseBuilder rBuild = Response.ok(fileBlob, contentType );
      if (asAttachment != null && !"".equals(asAttachment)) {
        // Set the filename we're downloading to the disk.
        rBuild.header(HtmlConsts.CONTENT_DISPOSITION, "attachment; " + "filename=\"" + wholePath
            + "\"");
      }
      return rBuild.build();
    } else {
      return Response.status(Status.NOT_FOUND).entity("File content not yet available for: " + wholePath).build();
    }
  }

  @Override
  public Response putFile(@PathParam("odkClientVersion") String odkClientVersion, @PathParam("filePath") List<PathSegment> segments,  byte[] content) throws IOException, ODKTaskLockException, PermissionDeniedException, ODKDatastoreException {

    TreeSet<GrantedAuthorityName> ui = SecurityServiceUtil.getCurrentUserSecurityInfo(cc);
    if ( !ui.contains(GrantedAuthorityName.ROLE_ADMINISTER_TABLES) ) {
      throw new PermissionDeniedException("User does not belong to the 'Administer Tables' group");
    }
    
    if (segments.size() < 1) {
      return Response.status(Status.BAD_REQUEST).entity(FileService.ERROR_MSG_INSUFFICIENT_PATH).build();
    }
    String tableId = getTableIdFromPathSegments(segments);
    String filePath = constructPathFromSegments(segments);
    String contentType = req.getContentType();
    try {
      // DbTableFileInfo.NO_TABLE_ID -- means that we are working with app-level permissions
      if ( !DbTableFileInfo.NO_TABLE_ID.equals(tableId) ) {
        userPermissions.checkPermission(appId, tableId, TablePermission.WRITE_PROPERTIES);
      }

      // 0) Delete anything that is already stored

      List<DbTableFileInfoEntity> entities = DbTableFileInfo.queryForEntity(odkClientVersion, tableId, filePath, cc);
      for ( DbTableFileInfoEntity entity : entities ) {

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
      DbTableFileInfoEntity tableFileInfoRow = ec.newTableFileInfoEntity(odkClientVersion, tableId, filePath,
          userPermissions, cc);
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
      BlobSubmissionOutcome outcome = instance.addBlob(content, contentType, null, true, cc);
      // 3) persist the user-friendly table entry about the blob
      tableFileInfoRow.put(cc);

      UriBuilder ub = info.getBaseUriBuilder();
      ub.path(OdkTables.class, "getFilesService");
      URI self = ub.path(FileService.class, "getFile").build(appId, odkClientVersion, filePath);

      String locationUrl = self.toURL().toExternalForm();

      return Response.status((outcome == BlobSubmissionOutcome.NEW_FILE_VERSION) ? Status.ACCEPTED : Status.CREATED)
                  .header("Location",locationUrl).build();
    } catch (ODKDatastoreException e) {
      LOGGER.error(("ODKTables file upload persistence error: " + e.getMessage()));
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(ErrorConsts.PERSISTENCE_LAYER_PROBLEM + "\n" + e.getMessage()).build();
    } catch (PermissionDeniedException e) {
      LOGGER.error(("ODKTables file upload permissions error: " + e.getMessage()));
      return Response.status(Status.UNAUTHORIZED).entity("Permission denied").build();
    }
  }

  @Override
  public Response deleteFile(@PathParam("odkClientVersion") String odkClientVersion, @PathParam("filePath") List<PathSegment> segments) throws IOException, ODKTaskLockException, PermissionDeniedException, ODKDatastoreException {

    TreeSet<GrantedAuthorityName> ui = SecurityServiceUtil.getCurrentUserSecurityInfo(cc);
    if ( !ui.contains(GrantedAuthorityName.ROLE_ADMINISTER_TABLES) ) {
      throw new PermissionDeniedException("User does not belong to the 'Administer Tables' group");
    }

    if (segments.size() < 1) {
      return Response.status(Status.BAD_REQUEST).entity(FileService.ERROR_MSG_INSUFFICIENT_PATH).build();
    }
    String tableId = getTableIdFromPathSegments(segments);
    String wholePath = constructPathFromSegments(segments);
    try {
      // DbTableFileInfo.NO_TABLE_ID -- means that we are working with app-level permissions
      if ( !DbTableFileInfo.NO_TABLE_ID.equals(tableId) ) {
        userPermissions.checkPermission(appId, tableId, TablePermission.WRITE_PROPERTIES);
      }

      // if we find nothing, we are happy.
      List<DbTableFileInfoEntity> entities = DbTableFileInfo.queryForEntity(odkClientVersion, tableId, wholePath, cc);
      for ( DbTableFileInfoEntity entity : entities ) {

        String uri = entity.getId();
        DbTableFiles dbTableFiles = new DbTableFiles(cc);
        BlobEntitySet blobEntitySet = dbTableFiles.getBlobEntitySet(uri, cc);
        blobEntitySet.remove(cc);
        entity.delete(cc);
      }

      return Response.ok().build();
    } catch (ODKDatastoreException e) {
      LOGGER.error(("ODKTables file delete persistence error: " + e.getMessage()));
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(ErrorConsts.PERSISTENCE_LAYER_PROBLEM + "\n" + e.getMessage()).build();
    } catch (PermissionDeniedException e) {
      LOGGER.error(("ODKTables file delete permissions error: " + e.getMessage()));
      return Response.status(Status.UNAUTHORIZED).entity("Permission denied").build();
    }
  }

  /**
   * Construct the path for the file. This is the entire path excluding the app
   * id.
   *
   * @param segments
   * @return
   */
  private String constructPathFromSegments(List<PathSegment> segments) {
    // Now construct up the path from the segments.
    // We are NOT going to include the app id. Therefore if you upload a file
    // with a path of /myDir/myFile.html, the path will be stored as
    // myDir/myFile.html. This is so that when you get the filename on the
    // manifest, it won't matter what is the root directory of your app on your
    // device. Otherwise you might have to strip the first path segment or do
    // something similar.
    StringBuilder sb = new StringBuilder();
    int i = 0;
    for (PathSegment segment : segments) {
      sb.append(segment.getPath());
      if (i < segments.size() - 1) {
        sb.append(BasicConsts.FORWARDSLASH);
      }
      i++;
    }
    String wholePath = sb.toString();
    return wholePath;
  }

  /**
   * Retrieve the table id given the path. The first segment (position 0) is
   * a directory under /sdcard/opendatakit/{app_id}/, as all files must be
   * associated with an app id. Not all files must be associated with a table,
   * however, so it parses to find the table id. Otherwise it returns the
   * {@link DEFAULT_TABLE_ID}.
   * <p>
   * The convention is that any table-related file must be under:
   * /tables/tableid
   * OR a csv file:
   * /assets/csv/tableid....csv
   *
   * So the 2nd position (0 indexed) will be the table
   * id if the first position is "tables", and the 3rd
   * position (0 indexed) will begin with the table id
   * if it is a csv file under the assets/csv directory.
   *
   * @param segments
   * @return
   */
  private String getTableIdFromPathSegments(List<PathSegment> segments) {
    String[] pathParts = constructPathFromSegments(segments).split(BasicConsts.FORWARDSLASH);
    String tableId = DbTableFileInfo.NO_TABLE_ID;
    String firstFolder = pathParts[0];
    if ((segments.size() >= 2) && firstFolder.equals(TABLES_FOLDER)) {
      tableId = pathParts[1];
    } else if ((segments.size() == 3) && firstFolder.equals(ASSETS_FOLDER)) {
      String secondFolder = pathParts[1];
      if (secondFolder.equals(CSV_FOLDER)) {
        String fileName = pathParts[2];
        String splits[] = fileName.split("\\.");
        if ( splits[splits.length-1].toLowerCase(Locale.ENGLISH).equals("csv") ) {
          tableId = splits[0];
        }
      }
    }
    return tableId;
  }

}
