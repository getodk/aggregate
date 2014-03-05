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
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.annotations.GZIP;
import org.opendatakit.aggregate.odktables.exception.BadColumnNameException;
import org.opendatakit.aggregate.odktables.exception.ETagMismatchException;
import org.opendatakit.aggregate.odktables.exception.InconsistentStateException;
import org.opendatakit.aggregate.odktables.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.rest.ApiConstants;
import org.opendatakit.aggregate.odktables.rest.entity.Row;
import org.opendatakit.aggregate.odktables.rest.entity.RowResource;
import org.opendatakit.aggregate.odktables.rest.entity.RowResourceList;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;

@Produces({MediaType.APPLICATION_JSON, ApiConstants.MEDIA_TEXT_XML_UTF8, ApiConstants.MEDIA_APPLICATION_XML_UTF8})
public interface DataService {

  /**
   *
   * @return {@link RowResourceList} containing the rows being returned.
   * @throws ODKDatastoreException
   * @throws PermissionDeniedException
   * @throws InconsistentStateException
   * @throws ODKTaskLockException
   * @throws BadColumnNameException
   */
  @GET
  @Path("")
  @GZIP
  public Response /*RowResourceList*/ getRows() throws ODKDatastoreException, PermissionDeniedException, InconsistentStateException, ODKTaskLockException, BadColumnNameException;

  /**
   *
   * @param rowId
   * @return {@link RowResource} of the row
   * @throws ODKDatastoreException
   * @throws PermissionDeniedException
   * @throws InconsistentStateException
   * @throws ODKTaskLockException
   * @throws BadColumnNameException
   */
  @GET
  @Path("{rowId}")
  @GZIP
  public Response /*RowResource*/ getRow(@PathParam("rowId") String rowId) throws ODKDatastoreException,
      PermissionDeniedException, InconsistentStateException, ODKTaskLockException, BadColumnNameException;

  /**
   *
   * @param rowId
   * @param row
   * @return {@link RowResource} of the newly added/inserted row.
   * @throws ODKTaskLockException
   * @throws ODKDatastoreException
   * @throws ETagMismatchException
   * @throws PermissionDeniedException
   * @throws BadColumnNameException
   * @throws InconsistentStateException
   */
  @PUT
  @Path("{rowId}")
  @Consumes({MediaType.APPLICATION_JSON, ApiConstants.MEDIA_TEXT_XML_UTF8, ApiConstants.MEDIA_APPLICATION_XML_UTF8})
  @GZIP
  public Response /*RowResource*/ createOrUpdateRow(@PathParam("rowId") String rowId, @GZIP Row row)
      throws ODKTaskLockException, ODKDatastoreException, ETagMismatchException,
      PermissionDeniedException, BadColumnNameException, InconsistentStateException;

  /**
   *
   * @param rowId
   * @return String dataETag on the table that marks this row as deleted.
   * @throws ODKDatastoreException
   * @throws ODKTaskLockException
   * @throws PermissionDeniedException
   * @throws InconsistentStateException
   * @throws BadColumnNameException
   */
  @DELETE
  @Path("{rowId}")
  @Produces({MediaType.APPLICATION_JSON, ApiConstants.MEDIA_TEXT_XML_UTF8, ApiConstants.MEDIA_APPLICATION_XML_UTF8})
  public Response /*String*/ deleteRow(@PathParam("rowId") String rowId) throws ODKDatastoreException,
      ODKTaskLockException, PermissionDeniedException, InconsistentStateException, BadColumnNameException;

}