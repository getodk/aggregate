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
import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.constants.ErrorConsts;
import org.opendatakit.aggregate.odktables.api.FileService;
import org.opendatakit.aggregate.odktables.relation.DbTableFiles;
import org.opendatakit.aggregate.odktables.relation.EntityCreator;
import org.opendatakit.common.ermodel.BlobEntitySet;
import org.opendatakit.common.ermodel.simple.Entity;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.constants.HtmlConsts;

public class FileServiceImpl implements FileService {
  
  private static final Log LOGGER = LogFactory.getLog(FileServiceImpl.class);
  
  /** String to stand in for those things in the app's root directory. */
  private static final String DEFAULT_TABLE_ID = "defaultTableId";
  
  private static final String PATH_DELIMITER = "/";

  @Override
  @GET
  @Path("{filePath:.*}") // because we want to get the whole path
  public void getFile(@Context ServletContext servletContext, 
      @PathParam("filePath") List<PathSegment> segments,
      @Context HttpServletRequest req, @Context HttpServletResponse resp) 
      throws IOException {
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
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
          FileService.ERROR_MSG_INSUFFICIENT_PATH);
      return;
    }
    String appId = segments.get(0).toString();
    String tableId;
    if (segments.size() == 2) {
      // Then the second parameter is the file name, not the id.
      tableId = DEFAULT_TABLE_ID;
    } else {
      tableId = segments.get(1).toString();
    }
    // Now construct the whole path.
    StringBuilder sb = new StringBuilder();
    int i = 0;
    for (PathSegment segment : segments) {
      sb.append(segment.toString());
      if (i < segments.size() - 1) {
        sb.append(PATH_DELIMITER);
      }
      ++i;
    }
    String wholePath = sb.toString();
    
    CallingContext cc = ContextFactory.getCallingContext(servletContext, req);
    // Now get the necessary parameters. The only thing we'll expect from the 
    // client is the recognition that they're about to get a big ol' file.
    String downloadAsAttachmentString = 
        req.getParameter(FileService.PARAM_AS_ATTACHMENT);
    byte[] fileBlob;
    String unrootedFileName; // should always be the same as the wholePath.
    String contentType;
    Long contentLength;
    try {
      DbTableFiles dbTableFiles = new DbTableFiles(cc);
      BlobEntitySet blobEntitySet = 
          dbTableFiles.getBlobEntitySet(wholePath, cc);
      // We should only ever have one, as wholePath is the primary key.
      if (blobEntitySet.getAttachmentCount(cc) > 1) {
        resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
            "More than one file specified for: " + wholePath);
      }
      if (blobEntitySet.getAttachmentCount(cc) < 1) {
        resp.sendError(HttpServletResponse.SC_NOT_FOUND,
            "No file found for path: " + wholePath);
        return;
      }
      fileBlob = blobEntitySet.getBlob(1, cc);
      unrootedFileName = dbTableFiles.getBlobEntitySet(wholePath, cc)
          .getUnrootedFilename(1, cc);
      if (!wholePath.equals(unrootedFileName)) {
        resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
            "Unrooted filename didn't match path.");
      }
      contentType = dbTableFiles.getBlobEntitySet(wholePath, cc)
          .getContentType(1, cc);
      contentLength = dbTableFiles.getBlobEntitySet(wholePath, cc)
          .getContentLength(1, cc);
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
      if (downloadAsAttachmentString != null 
          && !"".equals(downloadAsAttachmentString)) {
        // Set the filename we're downloading to the disk.
        if (unrootedFileName != null) {
          resp.addHeader(HtmlConsts.CONTENT_DISPOSITION, "attachment; " +
          		"filename=\" unrootedFileName + \"");
        }
      }
      OutputStream os = resp.getOutputStream();
      os.write(fileBlob);
    } else {
      resp.setContentType(HtmlConsts.RESP_TYPE_PLAIN);
      resp.getWriter().print(ErrorConsts.NO_IMAGE_EXISTS);
    }
  }

  @Override
  @POST
  @Path("{filePath:.*}") // because we want to get the whole path
  public void putFile(@Context ServletContext servletContext,
      @PathParam("filePath") List<PathSegment> segments, 
      @Context HttpServletRequest req, @Context HttpServletResponse resp) 
      throws IOException {
    if (segments.size() <= 1) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
          FileService.ERROR_MSG_INSUFFICIENT_PATH);
      return;
    }
    // TODO This stuff all needs to be handled in the log table somehow, and 
    // it currently isn't.
    CallingContext cc = ContextFactory.getCallingContext(servletContext, req);
    // First parse the url to get the correct app and table ids.
    String appId = segments.get(0).toString();
    String tableId;
    if (segments.size() == 2) {
      // Then the second parameter is the file name, not the id.
      tableId = DEFAULT_TABLE_ID;
    } else {
      tableId = segments.get(1).toString();
    }
    if (!appId.equals("tables")) {
      // For now we'll just do tables. eventually we want all apps.
      // TODO: incorporate checking for apps
      // TODO: incorporate checking for access control
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, 
          FileService.ERROR_MSG_UNRECOGNIZED_APP_ID + appId);
    }
    // Now construct up the whole path from the segments.
    StringBuilder sb = new StringBuilder();
    int i = 0;
    for (PathSegment segment : segments) {
      sb.append(segment.toString());
      if (i < segments.size() - 1) {
        sb.append(PATH_DELIMITER);
      }
      i++;
    }
    String wholePath = sb.toString();
    
    String contentType = req.getContentType();
    try {
      // Process the file.
      InputStream is = req.getInputStream();
      byte[] fileBlob = IOUtils.toByteArray(is);
      // Now that we have retrieved the file from the request, we have two 
      // things to do: 1) persist the actual file itself. 2) update the user-
      // friendly table that keeps the information about the tables and the
      // apps/tableids they're associated with.
      //
      // 1) Persist the file.
      DbTableFiles dbTableFiles = new DbTableFiles(cc);
      // Although this is called an entity set, it in fact represents a single 
      // file, because we have chosen to use it this way in this case. For more 
      // information see the docs in DbTableFiles.
      BlobEntitySet instance = dbTableFiles.newBlobEntitySet(wholePath, cc);
      // TODO: this being set to true is probably where some sort of versioning
      // should happen.
      instance.addBlob(fileBlob, contentType, wholePath, true, cc);
      // 2) Update the user-friendly table.
      // Create the row entity.
      EntityCreator ec = new EntityCreator();
      Entity tableFileInfoRow = 
          ec.newTableFileInfoEntity(appId, tableId, wholePath, cc);
      // Persist the entity.
      tableFileInfoRow.put(cc);
    } catch (ODKDatastoreException e) {
      LOGGER.error(("ODKTables file upload persistence error: " 
          + e.getMessage()));
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
          ErrorConsts.PERSISTENCE_LAYER_PROBLEM + "\n" + e.getMessage());
    }
  }

}
