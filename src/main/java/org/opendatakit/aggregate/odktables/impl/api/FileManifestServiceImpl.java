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

import java.net.MalformedURLException;
import java.net.URI;
import java.util.List;

import javax.ws.rs.PathParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.odktables.FileManifestManager;
import org.opendatakit.aggregate.odktables.api.FileManifestService;
import org.opendatakit.aggregate.odktables.api.FileService;
import org.opendatakit.aggregate.odktables.api.OdkTables;
import org.opendatakit.aggregate.odktables.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.relation.DbManifestETags;
import org.opendatakit.aggregate.odktables.relation.DbManifestETags.DbManifestETagEntity;
import org.opendatakit.aggregate.odktables.rest.ApiConstants;
import org.opendatakit.aggregate.odktables.rest.entity.OdkTablesFileManifest;
import org.opendatakit.aggregate.odktables.rest.entity.OdkTablesFileManifestEntry;
import org.opendatakit.aggregate.odktables.security.TablesUserPermissions;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;
import org.opendatakit.common.web.CallingContext;

/**
 * The implementation of the file manifest service. Handles the actual requests
 * and spits back out the manifests.
 *
 * @author sudar.sam@gmail.com
 */
public class FileManifestServiceImpl implements FileManifestService {

  private final CallingContext cc;
  private final String appId;
  private final UriInfo info;
  private TablesUserPermissions userPermissions;

  public FileManifestServiceImpl(UriInfo info, String appId, CallingContext cc)
      throws ODKEntityNotFoundException, ODKDatastoreException, PermissionDeniedException,
      ODKTaskLockException {
    this.cc = cc;
    this.appId = appId;
    this.info = info;
    this.userPermissions = ContextFactory.getTablesUserPermissions(cc);
  }

  public static String getAppLevelManifestETag(CallingContext cc) throws ODKDatastoreException {
    DbManifestETagEntity eTagEntity = DbManifestETags.getTableIdEntry(DbManifestETags.APP_LEVEL,
        cc);
    return eTagEntity.getManifestETag();
  }

  public static String getTableLevelManifestETag(String tableId, CallingContext cc)
      throws ODKDatastoreException {
    DbManifestETagEntity eTagEntity = DbManifestETags.getTableIdEntry(tableId, cc);
    return eTagEntity.getManifestETag();
  }

  @Override
  public Response getAppLevelFileManifest(HttpHeaders httpHeaders,
      @PathParam("odkClientVersion") String odkClientVersion)
      throws PermissionDeniedException, ODKDatastoreException, ODKTaskLockException {

    FileManifestManager manifestManager = new FileManifestManager(appId, odkClientVersion, cc);
    OdkTablesFileManifest manifest = null;

    // retrieve the incoming if-none-match eTag...
    List<String> eTags = httpHeaders.getRequestHeader(HttpHeaders.IF_NONE_MATCH);
    String eTag = (eTags == null || eTags.isEmpty()) ? null : eTags.get(0);
    DbManifestETagEntity eTagEntity = null;
    try {
      try {
        eTagEntity = DbManifestETags.getTableIdEntry(DbManifestETags.APP_LEVEL, cc);
      } catch (ODKEntityNotFoundException e) {
        // ignore...
      }
      if (eTag != null && eTagEntity != null && eTag.equals(eTagEntity.getManifestETag())) {
        return Response.status(Status.NOT_MODIFIED).header(HttpHeaders.ETAG, eTag)
            .header(ApiConstants.OPEN_DATA_KIT_VERSION_HEADER, ApiConstants.OPEN_DATA_KIT_VERSION)
            .header("Access-Control-Allow-Origin", "*")
            .header("Access-Control-Allow-Credentials", "true").build();
      }
      // we want just the app-level files.
      manifest = manifestManager.getManifestForAppLevelFiles();

    } catch (ODKDatastoreException e) {
      Log log = LogFactory.getLog(FileManifestServiceImpl.class);
      log.error("Datastore exception in getting the file manifest");
      e.printStackTrace();
    }
    if (manifest == null) {
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Unable to retrieve manifest.")
          .header(ApiConstants.OPEN_DATA_KIT_VERSION_HEADER, ApiConstants.OPEN_DATA_KIT_VERSION)
          .header("Access-Control-Allow-Origin", "*")
          .header("Access-Control-Allow-Credentials", "true").build();
    } else {
      String newETag = Integer.toHexString(manifest.hashCode());
      // create a new eTagEntity if there isn't one already...
      if (eTagEntity == null) {
        eTagEntity = DbManifestETags.createNewEntity(DbManifestETags.APP_LEVEL, cc);
        eTagEntity.setManifestETag(newETag);
        eTagEntity.put(cc);
      } else if (!newETag.equals(eTagEntity.getManifestETag())) {
        Log log = LogFactory.getLog(FileManifestServiceImpl.class);
        log.error("App-level Manifest ETag does not match computed value!");
        eTagEntity.setManifestETag(newETag);
        eTagEntity.put(cc);
      }
      // and whatever the eTag is in that entity is the eTag we should return...
      eTag = eTagEntity.getManifestETag();

      UriBuilder ub = info.getBaseUriBuilder();
      ub.path(OdkTables.class, "getFilesService");
      // now supply the downloadUrl...
      for (OdkTablesFileManifestEntry entry : manifest.getFiles()) {
        URI self = ub.clone().path(FileService.class, "getFile").build(appId, odkClientVersion,
            entry.filename);
        try {
          entry.downloadUrl = self.toURL().toExternalForm();
        } catch (MalformedURLException e) {
          e.printStackTrace();
          throw new IllegalArgumentException("Unable to convert to URL");
        }
      }

      return Response.ok(manifest).header(HttpHeaders.ETAG, eTag)
          .header(ApiConstants.OPEN_DATA_KIT_VERSION_HEADER, ApiConstants.OPEN_DATA_KIT_VERSION)
          .header("Access-Control-Allow-Origin", "*")
          .header("Access-Control-Allow-Credentials", "true").build();
    }
  }

  @Override
  public Response getTableIdFileManifest(HttpHeaders httpHeaders,
      @PathParam("odkClientVersion") String odkClientVersion, @PathParam("tableId") String tableId)
      throws PermissionDeniedException, ODKDatastoreException, ODKTaskLockException {

    FileManifestManager manifestManager = new FileManifestManager(appId, odkClientVersion, cc);
    OdkTablesFileManifest manifest = null;

    // retrieve the incoming if-none-match eTag...
    List<String> eTags = httpHeaders.getRequestHeader(HttpHeaders.IF_NONE_MATCH);
    String eTag = (eTags == null || eTags.isEmpty()) ? null : eTags.get(0);
    DbManifestETagEntity eTagEntity = null;
    try {
      try {
        eTagEntity = DbManifestETags.getTableIdEntry(tableId, cc);
      } catch (ODKEntityNotFoundException e) {
        // ignore...
      }
      if (eTag != null && eTagEntity != null && eTag.equals(eTagEntity.getManifestETag())) {
        return Response.status(Status.NOT_MODIFIED).header(HttpHeaders.ETAG, eTag)
            .header(ApiConstants.OPEN_DATA_KIT_VERSION_HEADER, ApiConstants.OPEN_DATA_KIT_VERSION)
            .header("Access-Control-Allow-Origin", "*")
            .header("Access-Control-Allow-Credentials", "true").build();
      }
      // we want just the files for the table.
      manifest = manifestManager.getManifestForTable(tableId);
    } catch (ODKDatastoreException e) {
      Log log = LogFactory.getLog(FileManifestServiceImpl.class);
      log.error("Datastore exception in getting the file manifest");
      e.printStackTrace();
    }
    if (manifest == null) {
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Unable to retrieve manifest.")
          .header(ApiConstants.OPEN_DATA_KIT_VERSION_HEADER, ApiConstants.OPEN_DATA_KIT_VERSION)
          .header("Access-Control-Allow-Origin", "*")
          .header("Access-Control-Allow-Credentials", "true").build();
    } else {
      String newETag = Integer.toHexString(manifest.hashCode());
      // create a new eTagEntity if there isn't one already...
      if (eTagEntity == null) {
        eTagEntity = DbManifestETags.createNewEntity(tableId, cc);
        eTagEntity.setManifestETag(newETag);
        eTagEntity.put(cc);
      } else if (!newETag.equals(eTagEntity.getManifestETag())) {
        Log log = LogFactory.getLog(FileManifestServiceImpl.class);
        log.error("Table-level (" + tableId + ") Manifest ETag does not match computed value!");
        eTagEntity.setManifestETag(newETag);
        eTagEntity.put(cc);
      }
      // and whatever the eTag is in that entity is the eTag we should return...
      eTag = eTagEntity.getManifestETag();

      UriBuilder ub = info.getBaseUriBuilder();
      ub.path(OdkTables.class, "getFilesService");
      // now supply the downloadUrl...
      for (OdkTablesFileManifestEntry entry : manifest.getFiles()) {
        URI self = ub.clone().path(FileService.class, "getFile").build(appId, odkClientVersion,
            entry.filename);
        try {
          entry.downloadUrl = self.toURL().toExternalForm();
        } catch (MalformedURLException e) {
          e.printStackTrace();
          throw new IllegalArgumentException("Unable to convert to URL");
        }
      }

      return Response.ok(manifest).header(HttpHeaders.ETAG, eTag)
          .header(ApiConstants.OPEN_DATA_KIT_VERSION_HEADER, ApiConstants.OPEN_DATA_KIT_VERSION)
          .header("Access-Control-Allow-Origin", "*")
          .header("Access-Control-Allow-Credentials", "true").build();
    }
  }

}
