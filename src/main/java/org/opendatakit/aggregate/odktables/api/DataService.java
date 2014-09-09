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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.opendatakit.aggregate.odktables.exception.BadColumnNameException;
import org.opendatakit.aggregate.odktables.exception.ETagMismatchException;
import org.opendatakit.aggregate.odktables.exception.InconsistentStateException;
import org.opendatakit.aggregate.odktables.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.rest.ApiConstants;
import org.opendatakit.aggregate.odktables.rest.entity.Row;
import org.opendatakit.aggregate.odktables.rest.entity.RowList;
import org.opendatakit.aggregate.odktables.rest.entity.RowOutcomeList;
import org.opendatakit.aggregate.odktables.rest.entity.RowResource;
import org.opendatakit.aggregate.odktables.rest.entity.RowResourceList;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;

public interface DataService {

  public static final String QUERY_ROW_ETAG = "row_etag";
  public static final String CURSOR_PARAMETER = "cursor";
  public static final String FETCH_LIMIT = "fetchLimit";

  /**
   *
   * @param cursor - null or a websafeCursor value from the RowResourceList of a previous call
   * @param fetchLimit - null or the number of rows to fetch. If null, server will choose the limit.
   * @return {@link RowResourceList} containing the rows being returned.
   * @throws ODKDatastoreException
   * @throws PermissionDeniedException
   * @throws InconsistentStateException
   * @throws ODKTaskLockException
   * @throws BadColumnNameException
   */
  @GET
  @Produces({MediaType.APPLICATION_JSON, ApiConstants.MEDIA_TEXT_XML_UTF8, ApiConstants.MEDIA_APPLICATION_XML_UTF8})
  public Response /*RowResourceList*/ getRows(@QueryParam(CURSOR_PARAMETER) String cursor, @QueryParam(FETCH_LIMIT) String fetchLimit) throws ODKDatastoreException, PermissionDeniedException, InconsistentStateException, ODKTaskLockException, BadColumnNameException;

  /**
   *
   * @param rows
   * @return {@link RowOutcomeList} of the newly added/modified/deleted rows.
   * @throws ODKTaskLockException
   * @throws ODKDatastoreException
   * @throws ETagMismatchException
   * @throws PermissionDeniedException
   * @throws BadColumnNameException
   * @throws InconsistentStateException
   */
  @PUT
  @Consumes({MediaType.APPLICATION_JSON, ApiConstants.MEDIA_TEXT_XML_UTF8, ApiConstants.MEDIA_APPLICATION_XML_UTF8})
  @Produces({MediaType.APPLICATION_JSON, ApiConstants.MEDIA_TEXT_XML_UTF8, ApiConstants.MEDIA_APPLICATION_XML_UTF8})
  public Response /*RowOutcomeList*/ alterRows(RowList rows)
      throws ODKTaskLockException, ODKDatastoreException, ETagMismatchException,
      PermissionDeniedException, BadColumnNameException, InconsistentStateException;

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
  @Produces({MediaType.APPLICATION_JSON, ApiConstants.MEDIA_TEXT_XML_UTF8, ApiConstants.MEDIA_APPLICATION_XML_UTF8})
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
  @Produces({MediaType.APPLICATION_JSON, ApiConstants.MEDIA_TEXT_XML_UTF8, ApiConstants.MEDIA_APPLICATION_XML_UTF8})
  public Response /*RowResource*/ createOrUpdateRow(@PathParam("rowId") String rowId, Row row)
      throws ODKTaskLockException, ODKDatastoreException, ETagMismatchException,
      PermissionDeniedException, BadColumnNameException, InconsistentStateException;

  /**
   *
   * @param rowId
   * @param rowETag -- the row's ETag as known to the client (must match on server)
   * @return String dataETag on the table that marks this row as deleted.
   * @throws ODKDatastoreException
   * @throws ODKTaskLockException
   * @throws PermissionDeniedException
   * @throws InconsistentStateException
   * @throws BadColumnNameException
   * @throws ETagMismatchException
   */
  @DELETE
  @Path("{rowId}")
  @Produces({MediaType.APPLICATION_JSON, ApiConstants.MEDIA_TEXT_XML_UTF8, ApiConstants.MEDIA_APPLICATION_XML_UTF8})
  public Response /*String*/ deleteRow(@PathParam("rowId") String rowId, @QueryParam(QUERY_ROW_ETAG) String rowETag) throws ODKDatastoreException,
      ODKTaskLockException, PermissionDeniedException, InconsistentStateException, BadColumnNameException, ETagMismatchException;

}