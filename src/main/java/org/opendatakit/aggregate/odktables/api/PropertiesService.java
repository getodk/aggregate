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

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.opendatakit.aggregate.odktables.exception.ETagMismatchException;
import org.opendatakit.aggregate.odktables.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.rest.ApiConstants;
import org.opendatakit.aggregate.odktables.rest.entity.PropertiesResource;
import org.opendatakit.aggregate.odktables.rest.entity.TableProperties;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;

@Produces({MediaType.APPLICATION_JSON, ApiConstants.MEDIA_TEXT_XML_UTF8, ApiConstants.MEDIA_APPLICATION_XML_UTF8})
public interface PropertiesService {

  /**
   *
   * @return {@link PropertiesResource}
   * @throws ODKDatastoreException
   * @throws PermissionDeniedException
   * @throws ODKTaskLockException
   * @throws ETagMismatchException
   */
  @GET
  @Path("")
  public Response /*PropertiesResource*/ getProperties() throws ODKDatastoreException, PermissionDeniedException, ODKTaskLockException, ETagMismatchException;

  /**
   *
   * @param properties
   * @return {@link PropertiesResource}
   * @throws ODKDatastoreException
   * @throws ODKTaskLockException
   * @throws ETagMismatchException
   * @throws PermissionDeniedException
   */
  @PUT
  @Path("")
  @Consumes({MediaType.APPLICATION_JSON, ApiConstants.MEDIA_TEXT_XML_UTF8, ApiConstants.MEDIA_APPLICATION_XML_UTF8})
  public Response /*PropertiesResource*/ setProperties(TableProperties properties) throws ODKDatastoreException,
      ODKTaskLockException, ETagMismatchException, PermissionDeniedException;

}
