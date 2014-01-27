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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.PathSegment;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.constants.ErrorConsts;
import org.opendatakit.aggregate.odktables.api.FileService;
import org.opendatakit.aggregate.odktables.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.relation.DbTableFileInfo;
import org.opendatakit.aggregate.odktables.relation.DbTableFileInfo.DbTableFileInfoEntity;
import org.opendatakit.aggregate.odktables.relation.DbTableFiles;
import org.opendatakit.aggregate.odktables.relation.EntityCreator;
import org.opendatakit.aggregate.odktables.security.TablesUserPermissionsImpl;
import org.opendatakit.common.ermodel.BlobEntitySet;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.constants.HtmlConsts;

public class FileServiceImpl implements FileService {

  private static final Log LOGGER = LogFactory.getLog(FileServiceImpl.class);

  /**
   * String to stand in for those things in the app's root directory.
   *
   * NOTE: This cannot be null -- GAE doesn't like that!
   */
  public static final String NO_TABLE_ID = "";

  private static final String PATH_DELIMITER = "/";

  /**
   * The name of the folder that contains the files associated with a table in
   * an app.
   *
   * @see #getTableIdFromPathSegments(List)
   */
  private static final String TABLES_FOLDER = "tables";

  @Override
  @GET
  @Path("{filePath:.*}")
  // because we want to get the whole path
  public void getFile(@Context ServletContext servletContext,
      @PathParam("filePath") List<PathSegment> segments, @Context HttpServletRequest req,
      @Context HttpServletResponse resp) throws IOException {
    // Basing this off of OdkTablesTableFileDownloadServlet, which in turn was
    // based off of XFormsDownloadServlet.

    // First we need to get the app id and the table id from the path. We're
    // going to be assuming that you're passing the entire path of the file you
    // want to get. By convention, this is: appid/tableid/the/rest/of/path.
    // So we'll reclaim the tidbits and then reconstruct the entire path.
    // Note that even if you're getting general files, like perhaps the jquery
    // library, you will be sure to have an appid and the table name, so these
    // calls should never fail. Try to enforce this, however.
    if (segments.size() <= 1) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, FileService.ERROR_MSG_INSUFFICIENT_PATH);
      return;
    }
    String appId = segments.get(0).toString();
    String tableId = getTableIdFromPathSegments(segments);
    // Now construct the whole path.
    String wholePath = constructPathFromSegments(segments);

    CallingContext cc = ContextFactory.getCallingContext(servletContext, req);
    String downloadAsAttachmentString = req.getParameter(FileService.PARAM_AS_ATTACHMENT);
    byte[] fileBlob;
    String contentType;
    Long contentLength;
    try {
      List<DbTableFileInfoEntity> entities = DbTableFileInfo.queryForEntity(tableId, wholePath, cc);
      if (entities.size() > 1) {
        Log log = LogFactory.getLog(DbTableFileInfo.class);
        log.error("more than one entity for appId: " + appId + ", tableId: " + tableId
            + ", pathToFile: " + wholePath);
      } else if (entities.size() < 1) {
        resp.sendError(HttpServletResponse.SC_NOT_FOUND, "no file found for: " + wholePath);
        return;
      }
      DbTableFileInfoEntity dbTableFileInfoRow = entities.get(0);
      String uri = dbTableFileInfoRow.getId();
      DbTableFiles dbTableFiles = new DbTableFiles(cc);
      BlobEntitySet blobEntitySet = dbTableFiles.getBlobEntitySet(uri, cc);
      // We should only ever have one, as wholePath is the primary key.
      if (blobEntitySet.getAttachmentCount(cc) > 1) {
        resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
            "More than one file specified for: " + wholePath);
      }
      if (blobEntitySet.getAttachmentCount(cc) < 1) {
        resp.sendError(HttpServletResponse.SC_NOT_FOUND, "No file found for path: " + wholePath);
        return;
      }
      fileBlob = blobEntitySet.getBlob(1, cc);
      contentType = blobEntitySet.getContentType(1, cc);
      contentLength = blobEntitySet.getContentLength(1, cc);
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
          "Unable to retrieve attachment and access attributes.");
      return;
    }
    // And now prepare everything to be returned to the caller.
    if (fileBlob != null) {
      if (contentType == null) {
        resp.setContentType(HtmlConsts.RESP_TYPE_IMAGE_JPEG);
      } else {
        resp.setContentType(contentType);
      }
      if (contentLength != null) {
        resp.setContentType(contentType);
      }
      if (downloadAsAttachmentString != null && !"".equals(downloadAsAttachmentString)) {
        // Set the filename we're downloading to the disk.
        resp.addHeader(HtmlConsts.CONTENT_DISPOSITION, "attachment; " + "filename=\"" + wholePath
            + "\"");
      }
      OutputStream os = resp.getOutputStream();
      os.write(fileBlob);
      resp.setStatus(HttpStatus.SC_OK);
    } else {
      resp.setContentType(HtmlConsts.RESP_TYPE_PLAIN);
      resp.getWriter().print(ErrorConsts.NO_IMAGE_EXISTS);
    }
  }

  @Override
  @POST
  @Path("{filePath:.*}")
  // because we want to get the whole path
  public void putFile(@Context ServletContext servletContext,
      @PathParam("filePath") List<PathSegment> segments, @Context HttpServletRequest req,
      @Context HttpServletResponse resp) throws IOException {
    if (segments.size() <= 1) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, FileService.ERROR_MSG_INSUFFICIENT_PATH);
      return;
    }
    // TODO This stuff all needs to be handled in the log table somehow, and
    // it currently isn't.
    CallingContext cc = ContextFactory.getCallingContext(servletContext, req);

    // First parse the url to get the correct app and table ids.
    String appId = segments.get(0).toString();
    String tableId = getTableIdFromPathSegments(segments);
    if (!appId.equals("tables")) {
      // For now we'll just do tables. eventually we want all apps.
      // TODO: incorporate checking for apps
      // TODO: incorporate checking for access control
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, FileService.ERROR_MSG_UNRECOGNIZED_APP_ID
          + appId);
      return;
    }
    String wholePath = constructPathFromSegments(segments);
    String contentType = req.getContentType();
    try {
      TablesUserPermissionsImpl userPermissions = new TablesUserPermissionsImpl(cc.getCurrentUser()
          .getUriUser(), cc);

      // Process the file.
      InputStream is = req.getInputStream();
      byte[] fileBlob = IOUtils.toByteArray(is);
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
      DbTableFileInfoEntity tableFileInfoRow = ec.newTableFileInfoEntity(tableId, wholePath,
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
      instance.addBlob(fileBlob, contentType, null, true, cc);

      // 3) persist the user-friendly table entry about the blob
      tableFileInfoRow.put(cc);

      resp.setStatus(HttpServletResponse.SC_CREATED);
      resp.addHeader("Location", wholePath);
    } catch (ODKDatastoreException e) {
      LOGGER.error(("ODKTables file upload persistence error: " + e.getMessage()));
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
          ErrorConsts.PERSISTENCE_LAYER_PROBLEM + "\n" + e.getMessage());
    } catch (PermissionDeniedException e) {
      LOGGER.error(("ODKTables file upload permissions error: " + e.getMessage()));
      resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Permission denied");
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
    // with a path of appid/myDir/myFile.html, the path will be stored as
    // myDir/myFile.html. This is so that when you get the filename on the
    // manifest, it won't matter what is the root directory of your app on your
    // device. Otherwise you might have to strip the first path segment or do
    // something similar.
    StringBuilder sb = new StringBuilder();
    int i = 0;
    for (PathSegment segment : segments) {
      if (i == 0) {
        i++;
        continue;
      }
      sb.append(segment.toString());
      if (i < segments.size() - 1) {
        sb.append(PATH_DELIMITER);
      }
      i++;
    }
    String wholePath = sb.toString();
    return wholePath;
  }

  /**
   * Retrieve the table id given the path. The first segment (position 0) is
   * known to be the app id, as all files must be associated with an app id. Not
   * all files must be associated with a table, however, so it parses to find
   * the table id. Otherwise it returns the {@link DEFAULT_TABLE_ID}.
   * <p>
   * The convention is that any table id must be of the form:
   * /appid/tables/tableid. So the 2nd position (0 indexed) will be the table
   * idea if the first position is "tables".
   *
   * @param segments
   * @return
   */
  private String getTableIdFromPathSegments(List<PathSegment> segments) {
    String tableId;
    if (segments.size() < 4) {
      // Then we aren't a file name, b/c we're assuming it must be
      // appid/tables/tableid/file
      tableId = NO_TABLE_ID;
    } else if (segments.get(1).toString().equals(TABLES_FOLDER)) {
      // We have to see if it could be a tableId. If it can, then we assume it
      // is a table id. Otherwise we give it the default tableId.
      tableId = segments.get(2).toString();
    } else {
      tableId = NO_TABLE_ID;
    }
    return tableId;
  }

}
