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

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.odktables.FileManifestManager;
import org.opendatakit.aggregate.odktables.api.FileManifestService;
import org.opendatakit.aggregate.odktables.rest.entity.OdkTablesFileManifest;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * The implementation of the file manifest service. Handles the actual requests
 * and spits back out the manifests.
 *
 * @author sudar.sam@gmail.com
 */
public class FileManifestServiceImpl implements FileManifestService {

  @Override
  @GET
  public Response getFileManifest(@Context ServletContext servletContext,
      @Context HttpServletRequest req, @Context HttpServletResponse resp,
      @QueryParam(PARAM_APP_ID) String appId, @QueryParam(PARAM_TABLE_ID) String tableId,
      @QueryParam(PARAM_APP_LEVEL_FILES) String appLevel) {
    ServiceUtils.examineRequest(servletContext, req);
    // First we need to get the calling context.
    CallingContext cc = ContextFactory.getCallingContext(servletContext, req);
    // Now make sure we have an app id.
    if (appId == null || "".equals(appId)) {
      return Response.status(Status.BAD_REQUEST).entity("Invalid request. App id must be present and valid.").build();
    }
    FileManifestManager manifestManager = new FileManifestManager(appId, cc);
    OdkTablesFileManifest manifest = null;
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
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Unable to retrieve manifest.").build();
    } else {
      return Response.ok(manifest).build();
    }
  }

}
