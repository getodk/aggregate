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
import org.opendatakit.aggregate.odktables.FileManager;
import org.opendatakit.aggregate.odktables.FileManager.FileChangeDetail;
import org.opendatakit.aggregate.odktables.FileManager.FileContentInfo;
import org.opendatakit.aggregate.odktables.api.FileService;
import org.opendatakit.aggregate.odktables.api.OdkTables;
import org.opendatakit.aggregate.odktables.exception.FileNotFoundException;
import org.opendatakit.aggregate.odktables.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.relation.DbTableFileInfo;
import org.opendatakit.aggregate.odktables.rest.entity.TableRole.TablePermission;
import org.opendatakit.aggregate.odktables.security.TablesUserPermissions;
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

  public FileServiceImpl(ServletContext sc, HttpServletRequest req, HttpHeaders headers,
      UriInfo info, String appId, CallingContext cc) throws ODKEntityNotFoundException,
      ODKDatastoreException, PermissionDeniedException, ODKTaskLockException {
    this.sc = sc;
    this.req = req;
    this.headers = headers;
    this.cc = cc;
    this.appId = appId;
    this.info = info;
    this.userPermissions = ContextFactory.getTablesUserPermissions(cc);

  }

  public static String getPropertiesFilePath(String tableId) {
    return TABLES_FOLDER + BasicConsts.FORWARDSLASH + tableId + BasicConsts.FORWARDSLASH
        + "properties.csv";
  }

  @Override
  public Response getFile(@PathParam("odkClientVersion") String odkClientVersion,
      @PathParam("filePath") List<PathSegment> segments,
      @QueryParam(PARAM_AS_ATTACHMENT) String asAttachment) throws IOException,
      ODKTaskLockException, PermissionDeniedException, ODKDatastoreException, FileNotFoundException {

    // First we need to get the table id from the path. We're
    // going to be assuming that you're passing the entire path of the file
    // under /sdcard/opendatakit/appId/ e.g., tables/tableid/the/rest/of/path.
    // So we'll reclaim the tidbits and then reconstruct the entire path.
    // If you are getting general files, there will be no recoverable tableId,
    // and these are then app-level files.
    if (segments.size() < 1) {
      return Response.status(Status.BAD_REQUEST).entity(FileService.ERROR_MSG_INSUFFICIENT_PATH)
          .build();
    }
    String tableId = getTableIdFromPathSegments(segments);
    String wholePath = constructPathFromSegments(segments);

    FileContentInfo fi = null;

    // DbTableFileInfo.NO_TABLE_ID -- means that we are working with app-level
    // permissions
    if (!DbTableFileInfo.NO_TABLE_ID.equals(tableId)) {
      userPermissions.checkPermission(appId, tableId, TablePermission.READ_PROPERTIES);
    }

    FileManager fm = new FileManager(appId, cc);
    fi = fm.getFile(odkClientVersion, tableId, wholePath);

    // And now prepare everything to be returned to the caller.
    if (fi.fileBlob != null && fi.contentType != null && fi.contentLength != null
        && fi.contentLength != 0L) {
      ResponseBuilder rBuild = Response.ok(fi.fileBlob, fi.contentType);
      if (asAttachment != null && !"".equals(asAttachment)) {
        // Set the filename we're downloading to the disk.
        rBuild.header(HtmlConsts.CONTENT_DISPOSITION, "attachment; " + "filename=\"" + wholePath
            + "\"");
      }
      return rBuild.build();
    } else {
      return Response.status(Status.NOT_FOUND)
          .entity("File content not yet available for: " + wholePath).build();
    }
  }

  @Override
  public Response putFile(@PathParam("odkClientVersion") String odkClientVersion,
      @PathParam("filePath") List<PathSegment> segments, byte[] content) throws IOException,
      ODKTaskLockException, PermissionDeniedException, ODKDatastoreException {

    TreeSet<GrantedAuthorityName> ui = SecurityServiceUtil.getCurrentUserSecurityInfo(cc);
    if (!ui.contains(GrantedAuthorityName.ROLE_ADMINISTER_TABLES)) {
      throw new PermissionDeniedException("User does not belong to the 'Administer Tables' group");
    }

    if (segments.size() < 1) {
      return Response.status(Status.BAD_REQUEST).entity(FileService.ERROR_MSG_INSUFFICIENT_PATH)
          .build();
    }
    String tableId = getTableIdFromPathSegments(segments);
    String filePath = constructPathFromSegments(segments);
    String contentType = req.getContentType();

    // DbTableFileInfo.NO_TABLE_ID -- means that we are working with app-level
    // permissions
    if (!DbTableFileInfo.NO_TABLE_ID.equals(tableId)) {
      userPermissions.checkPermission(appId, tableId, TablePermission.WRITE_PROPERTIES);
    }

    FileManager fm = new FileManager(appId, cc);

    FileContentInfo fi = new FileContentInfo(contentType, Long.valueOf(content.length), content);

    FileChangeDetail outcome = fm.putFile(odkClientVersion, tableId, filePath,
        userPermissions, fi);

    UriBuilder ub = info.getBaseUriBuilder();
    ub.path(OdkTables.class, "getFilesService");
    URI self = ub.path(FileService.class, "getFile").build(appId, odkClientVersion, filePath);

    String locationUrl = self.toURL().toExternalForm();

    return Response
        .status(
            (outcome == FileChangeDetail.FILE_UPDATED) ? Status.ACCEPTED : Status.CREATED)
        .header("Location", locationUrl).build();
  }

  @Override
  public Response deleteFile(@PathParam("odkClientVersion") String odkClientVersion,
      @PathParam("filePath") List<PathSegment> segments) throws IOException, ODKTaskLockException,
      PermissionDeniedException, ODKDatastoreException {

    TreeSet<GrantedAuthorityName> ui = SecurityServiceUtil.getCurrentUserSecurityInfo(cc);
    if (!ui.contains(GrantedAuthorityName.ROLE_ADMINISTER_TABLES)) {
      throw new PermissionDeniedException("User does not belong to the 'Administer Tables' group");
    }

    if (segments.size() < 1) {
      return Response.status(Status.BAD_REQUEST).entity(FileService.ERROR_MSG_INSUFFICIENT_PATH)
          .build();
    }
    String tableId = getTableIdFromPathSegments(segments);
    String wholePath = constructPathFromSegments(segments);
    
    // DbTableFileInfo.NO_TABLE_ID -- means that we are working with app-level permissions
    if (!DbTableFileInfo.NO_TABLE_ID.equals(tableId)) {
      userPermissions.checkPermission(appId, tableId, TablePermission.WRITE_PROPERTIES);
    }

    FileManager fm = new FileManager(appId, cc);
    fm.deleteFile(odkClientVersion, tableId, wholePath);
    
    return Response.ok().build();
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
   * Retrieve the table id given the path. The first segment (position 0) is a
   * directory under /sdcard/opendatakit/{app_id}/, as all files must be
   * associated with an app id. Not all files must be associated with a table,
   * however, so it parses to find the table id. Otherwise it returns the
   * {@link DEFAULT_TABLE_ID}.
   * <p>
   * The convention is that any table-related file must be under:
   * /tables/tableid OR a csv file: /assets/csv/tableid....csv
   *
   * So the 2nd position (0 indexed) will be the table id if the first position
   * is "tables", and the 3rd position (0 indexed) will begin with the table id
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
        if (splits[splits.length - 1].toLowerCase(Locale.ENGLISH).equals("csv")) {
          tableId = splits[0];
        }
      }
    }
    return tableId;
  }

}
