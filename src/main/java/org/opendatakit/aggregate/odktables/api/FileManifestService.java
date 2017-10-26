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
package org.opendatakit.aggregate.odktables.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.opendatakit.aggregate.odktables.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.rest.ApiConstants;
import org.opendatakit.aggregate.odktables.rest.entity.OdkTablesFileManifest;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.persistence.exception.ODKOverQuotaException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;

/**
 * Servlet for downloading a manifest of files to the phone for the correct app
 * and the correct table.
 *
 * @author sudar.sam@gmail.com
 *
 */
public interface FileManifestService {

  /**
   *
   * @param httpHeaders
   * @param odkClientVersion
   * @return {@link OdkTablesFileManifest} of all the files meeting the filter criteria.
   * @throws ODKOverQuotaException
   * @throws ODKEntityNotFoundException
   * @throws ODKTaskLockException
   * @throws ODKDatastoreException
   * @throws PermissionDeniedException
   */
  @GET
  @Path("{odkClientVersion}")
  @Produces({MediaType.APPLICATION_JSON, ApiConstants.MEDIA_TEXT_XML_UTF8, ApiConstants.MEDIA_APPLICATION_XML_UTF8})
  public Response /*OdkTablesFileManifest*/ getAppLevelFileManifest(@Context HttpHeaders httpHeaders, @PathParam("odkClientVersion") String odkClientVersion) throws ODKEntityNotFoundException, ODKOverQuotaException, PermissionDeniedException, ODKDatastoreException, ODKTaskLockException;

  /**
   *
   * @param httpHeaders
   * @param odkClientVersion
   * @param tableId
   * @return {@link OdkTablesFileManifest} of all the files meeting the filter criteria.
   * @throws ODKOverQuotaException
   * @throws ODKEntityNotFoundException
   * @throws ODKTaskLockException
   * @throws ODKDatastoreException
   * @throws PermissionDeniedException
   */
  @GET
  @Path("{odkClientVersion}/{tableId}")
  @Produces({MediaType.APPLICATION_JSON, ApiConstants.MEDIA_TEXT_XML_UTF8, ApiConstants.MEDIA_APPLICATION_XML_UTF8})
  public Response /*OdkTablesFileManifest*/ getTableIdFileManifest(@Context HttpHeaders httpHeaders, @PathParam("odkClientVersion") String odkClientVersion, @PathParam("tableId") String tableId) throws ODKEntityNotFoundException, ODKOverQuotaException, PermissionDeniedException, ODKDatastoreException, ODKTaskLockException;
}
