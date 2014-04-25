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
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.resteasy.annotations.GZIP;
import org.opendatakit.aggregate.constants.ErrorConsts;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.odktables.api.FileService;
import org.opendatakit.aggregate.odktables.api.InstanceFileService;
import org.opendatakit.aggregate.odktables.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.relation.DbTableInstanceFiles;
import org.opendatakit.aggregate.odktables.rest.entity.TableRole.TablePermission;
import org.opendatakit.aggregate.odktables.security.TablesUserPermissions;
import org.opendatakit.common.datamodel.BinaryContentManipulator.BlobSubmissionOutcome;
import org.opendatakit.common.ermodel.BlobEntitySet;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.constants.BasicConsts;
import org.opendatakit.common.web.constants.HtmlConsts;

public class InstanceFileServiceImpl implements InstanceFileService {

  private static final Log LOGGER = LogFactory.getLog(InstanceFileServiceImpl.class);

  /**
   * String to stand in for those things in the app's root directory.
   *
   * NOTE: This cannot be null -- GAE doesn't like that!
   */
  public static final String NO_TABLE_ID = "";

  private static final String PATH_DELIMITER = "/";

  private static final String ERROR_FILE_VERSION_DIFFERS = "File on server does not match file being uploaded. Aborting upload. ";

  /**
   * The name of the folder that contains the files associated with a table in
   * an app.
   *
   * @see #getTableIdFromPathSegments(List)
   */
  private CallingContext cc;
  private TablesUserPermissions userPermissions;
  private UriInfo info;
  private String appId;
  private String tableId;

  public InstanceFileServiceImpl(String appId, String tableId, UriInfo info, TablesUserPermissions userPermissions, CallingContext cc)
      throws ODKEntityNotFoundException, ODKDatastoreException {
    this.cc = cc;
    this.appId = appId;
    this.tableId = tableId;
    this.info = info;
    this.userPermissions = userPermissions;
  }

  @Override
  @GET
  @Path("{filePath:.*}")
  // because we want to get the whole path
  public Response getFile(@PathParam("filePath") List<PathSegment> segments, @QueryParam(PARAM_AS_ATTACHMENT) String asAttachment, @QueryParam(PARAM_MD5_HASH_ONLY) String asMd5HashOnly) throws IOException {
    // The appId and tableId are from the surrounding TableService.
    // The entire path from appId onward mimics the path to the instances
    // directory By convention, this is: appid/tables/tableid/instances/rest/of/path.
    // So we'll reclaim the tidbits and then reconstruct the entire path.
    if (segments.size() <= 1) {
      return Response.status(Status.BAD_REQUEST).entity(FileService.ERROR_MSG_INSUFFICIENT_PATH).build();
    }
    // Now construct the whole path.
    String wholePath = constructPathFromSegments(segments);

    byte[] fileBlob;
    String contentType;
    Long contentLength;
    try {
      DbTableInstanceFiles blobStore = new DbTableInstanceFiles(tableId, cc);
      BlobEntitySet blobEntitySet = blobStore.getBlobEntitySet(wholePath, cc);
      // We should only ever have one, as wholePath is the primary key.
      if (blobEntitySet.getAttachmentCount(cc) > 1) {
        return Response.status(Status.INTERNAL_SERVER_ERROR).entity("More than one file specified for: " + wholePath).build();
      }
      if (blobEntitySet.getAttachmentCount(cc) < 1) {
        return Response.status(Status.NOT_FOUND).entity("No file found for path: " + wholePath).build();
      }
      // just the hash was requested -- return it. Perhaps this should be treated as an ETag?
      if ( asMd5HashOnly != null && asMd5HashOnly.length() != 0 ) {
        String md5 = blobEntitySet.getContentHash(1, cc);
        ResponseBuilder rBuild = Response.ok(md5, MediaType.TEXT_PLAIN);
        return rBuild.build();
      }
      fileBlob = blobEntitySet.getBlob(1, cc);
      contentType = blobEntitySet.getContentType(1, cc);
      contentLength = blobEntitySet.getContentLength(1, cc);
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Unable to retrieve attachment and access attributes for: " + wholePath).build();
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
  @POST
  @Path("{filePath:.*}")
  @Consumes({MediaType.MEDIA_TYPE_WILDCARD})
  // because we want to get the whole path
  public Response putFile(@Context HttpServletRequest req, @PathParam("filePath") List<PathSegment> segments,  @GZIP byte[] content) throws IOException, ODKTaskLockException {
    if (segments.size() <= 1) {
      return Response.status(Status.BAD_REQUEST).entity(FileService.ERROR_MSG_INSUFFICIENT_PATH).build();
    }
    String wholePath = constructPathFromSegments(segments);
    String contentType = req.getContentType();
    try {
      userPermissions.checkPermission(appId, tableId, TablePermission.WRITE_ROW);
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
      // Although this is called an entity set, it in fact represents a single
      // file, because we have chosen to use it this way in this case.
      DbTableInstanceFiles blobStore = new DbTableInstanceFiles(tableId, cc);
      BlobEntitySet instance = blobStore.newBlobEntitySet(wholePath, cc);
      // TODO: this being set to true is probably where some sort of versioning
      // should happen.
      BlobSubmissionOutcome outcome = instance.addBlob(content, contentType, null, false, cc);
      if ( outcome == BlobSubmissionOutcome.NEW_FILE_VERSION ) {
        return Response.status(Status.BAD_REQUEST).entity(ERROR_FILE_VERSION_DIFFERS + "\n" + wholePath).build();
      }

      String locationUrl = cc.getServerURL() + BasicConsts.FORWARDSLASH
          + ServletConsts.ODK_TABLES_SERVLET_BASE_PATH + BasicConsts.FORWARDSLASH
          + wholePath;

      return Response.status(Status.CREATED).header("Location",locationUrl).build();
    } catch (ODKDatastoreException e) {
      LOGGER.error(("ODKTables file upload persistence error: " + e.getMessage()));
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(ErrorConsts.PERSISTENCE_LAYER_PROBLEM + "\n" + e.getMessage()).build();
    } catch (PermissionDeniedException e) {
      LOGGER.error(("ODKTables file upload permissions error: " + e.getMessage()));
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
    // with a path of appid/myDir/myFile.html, the path will be stored as
    // myDir/myFile.html. This is so that when you get the filename on the
    // manifest, it won't matter what is the root directory of your app on your
    // device. Otherwise you might have to strip the first path segment or do
    // something similar.
    StringBuilder sb = new StringBuilder();
    sb.append("tables");
    sb.append(PATH_DELIMITER);
    sb.append(tableId);
    sb.append(PATH_DELIMITER);
    sb.append("instances");
    sb.append(PATH_DELIMITER);
    int i = 0;
    for (PathSegment segment : segments) {
      sb.append(segment.toString());
      if (i < segments.size() - 1) {
        sb.append(PATH_DELIMITER);
      }
      i++;
    }
    String wholePath = sb.toString();
    return wholePath;
  }

}
