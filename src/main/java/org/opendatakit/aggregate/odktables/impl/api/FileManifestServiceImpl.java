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

import java.net.URI;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.resteasy.annotations.GZIP;
import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.odktables.FileManifestManager;
import org.opendatakit.aggregate.odktables.api.FileManifestService;
import org.opendatakit.aggregate.odktables.api.FileService;
import org.opendatakit.aggregate.odktables.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.rest.entity.OdkTablesFileManifest;
import org.opendatakit.aggregate.odktables.rest.entity.OdkTablesFileManifestEntry;
import org.opendatakit.aggregate.odktables.security.TablesUserPermissions;
import org.opendatakit.aggregate.odktables.security.TablesUserPermissionsImpl;
import org.opendatakit.aggregate.server.ServerPreferencesProperties;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.constants.BasicConsts;

/**
 * The implementation of the file manifest service. Handles the actual requests
 * and spits back out the manifests.
 *
 * @author sudar.sam@gmail.com
 */
public class FileManifestServiceImpl implements FileManifestService {

  private static final String ERROR_APP_ID_DIFFERS = "AppName differs";

  private CallingContext cc;
  private String appId;
  private TablesUserPermissions userPermissions;
  private UriInfo info;

  public FileManifestServiceImpl(@Context ServletContext sc, @Context HttpServletRequest req, @Context HttpHeaders httpHeaders,
      @Context UriInfo info) throws ODKDatastoreException, PermissionDeniedException, ODKTaskLockException {
    ServiceUtils.examineRequest(sc, req, httpHeaders);
    this.cc = ContextFactory.getCallingContext(sc, req);
    this.appId = ServerPreferencesProperties.getOdkTablesAppId(cc);
    this.userPermissions = new TablesUserPermissionsImpl(this.cc.getCurrentUser().getUriUser(), cc);
    this.info = info;
  }

  @Override
  @GET
  @GZIP
  public Response getAppLevelFileManifest(@PathParam("appId") String appId, @PathParam("odkClientVersion") String odkClientVersion) {
    appId = ServiceUtils.decodeSegment(appId);
    odkClientVersion = ServiceUtils.decodeSegment(odkClientVersion);
    if ( !this.appId.equals(appId) ) {
      return Response.status(Status.BAD_REQUEST)
          .entity(ERROR_APP_ID_DIFFERS + "\n" + appId).build();
    }
    FileManifestManager manifestManager = new FileManifestManager(appId, odkClientVersion, cc);
    OdkTablesFileManifest manifest = null;
    try {
      // we want just the app-level files.
      manifest = manifestManager.getManifestForAppLevelFiles();
    } catch (ODKDatastoreException e) {
      Log log = LogFactory.getLog(FileManifestServiceImpl.class);
      log.error("Datastore exception in getting the file manifest");
      e.printStackTrace();
    }
    if (manifest == null) {
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Unable to retrieve manifest.").build();
    } else {
      // now supply the downloadUrl...
      for ( OdkTablesFileManifestEntry entry : manifest.getEntries() ) {
        UriBuilder ub = info.getBaseUriBuilder();
        String[] segments = entry.filename.split(BasicConsts.FORWARDSLASH);
        String[] fullArgs = new String[segments.length+2];
        fullArgs[0] = appId;
        fullArgs[1] = odkClientVersion;
        for ( int i = 0 ; i < segments.length ; ++i ) {
          fullArgs[i+2] = segments[i];
        }
        URI self = ub.clone().path(FileService.class).path(FileService.class, "getFile").build(fullArgs, true);
        entry.downloadUrl = self.toASCIIString();
      }

      return Response.ok(manifest).build();
    }
  }


  @Override
  @GET
  @GZIP
  public Response getTableIdFileManifest(@PathParam("appId") String appId, @PathParam("odkClientVersion") String odkClientVersion, @PathParam("tableId") String tableId) {
    appId = ServiceUtils.decodeSegment(appId);
    odkClientVersion = ServiceUtils.decodeSegment(odkClientVersion);
    tableId = ServiceUtils.decodeSegment(tableId);
    if ( !this.appId.equals(appId) ) {
      return Response.status(Status.BAD_REQUEST)
          .entity(ERROR_APP_ID_DIFFERS + "\n" + appId).build();
    }
    FileManifestManager manifestManager = new FileManifestManager(appId, odkClientVersion, cc);
    OdkTablesFileManifest manifest = null;
    try {
      // we want just the files for the table.
      manifest = manifestManager.getManifestForTable(tableId);
    } catch (ODKDatastoreException e) {
      Log log = LogFactory.getLog(FileManifestServiceImpl.class);
      log.error("Datastore exception in getting the file manifest");
      e.printStackTrace();
    }
    if (manifest == null) {
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Unable to retrieve manifest.").build();
    } else {
      // now supply the downloadUrl...
      for ( OdkTablesFileManifestEntry entry : manifest.getEntries() ) {
        UriBuilder ub = info.getBaseUriBuilder();
        String[] segments = entry.filename.split(BasicConsts.FORWARDSLASH);
        String[] fullArgs = new String[segments.length+2];
        fullArgs[0] = appId;
        fullArgs[1] = odkClientVersion;
        for ( int i = 0 ; i < segments.length ; ++i ) {
          fullArgs[i+2] = segments[i];
        }
        URI self = ub.clone().path(FileService.class).path(FileService.class, "getFile").build(fullArgs, true);
        entry.downloadUrl = self.toASCIIString();
      }

      return Response.ok(manifest).build();
    }
  }

}
