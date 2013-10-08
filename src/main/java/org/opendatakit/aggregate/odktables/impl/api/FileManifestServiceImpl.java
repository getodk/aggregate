package org.opendatakit.aggregate.odktables.impl.api;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.odktables.api.FileManifestService;
import org.opendatakit.aggregate.odktables.entity.serialization.FileManifestManager;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * The implementation of the file manifest service. Handles the actual requests
 * and spits back out the manifests.
 * @author sudar.sam@gmail.com
 */
public class FileManifestServiceImpl implements FileManifestService {

  @Override
  @GET
  @Produces("application/json")
  public String getFileManifest(@Context ServletContext servletContext, 
      @Context HttpServletRequest req, @Context HttpServletResponse resp,
      @QueryParam (PARAM_APP_ID) String appId,
      @QueryParam (PARAM_TABLE_ID) String tableId, 
      @QueryParam (PARAM_APP_LEVEL_FILES) String appLevel) throws IOException {
    // First we need to get the calling context.
    CallingContext cc = ContextFactory.getCallingContext(servletContext, req);
    // Now make sure we have an app id.
    if (appId == null || "".equals(appId)) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, 
          "Invalid request. App id must be present and valid.");
      return "";
    }
    FileManifestManager manifestManager = new FileManifestManager(appId, cc);
    String manifest = null;
    try {
      // Now we need to decide what level on this app we're going to use. We 
      // can do table-level, all files, or app-level. The app-level param 
      // trumps the table-level.
      if (appLevel != null && !"".equals(appLevel)) {
        // we want just the app-level files.
        manifest = manifestManager.getManifestForAppLevelFiles();
      } else if (tableId != null && !"".equals(tableId)) {
        // we want just the files for the table.
        manifest = manifestManager.getManifestForTable(tableId);
      } else {
        // we want them all!
        manifest = manifestManager.getManifestForAllAppFiles();
      }
    } catch (ODKDatastoreException e) {
      Log log = LogFactory.getLog(FileManifestServiceImpl.class);
      log.error("Datastore exception in getting the file manifest");
      e.printStackTrace();
    }
    if (manifest == null) {
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
          "Unable to retrieve manifest.");
      // TODO: is this what I should be sending?
      return null;
    } else {
      return manifest;
    }
  }

}
