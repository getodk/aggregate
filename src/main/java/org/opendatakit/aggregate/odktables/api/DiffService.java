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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.opendatakit.aggregate.odktables.exception.BadColumnNameException;
import org.opendatakit.aggregate.odktables.exception.InconsistentStateException;
import org.opendatakit.aggregate.odktables.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.rest.ApiConstants;
import org.opendatakit.aggregate.odktables.rest.entity.RowResourceList;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;

public interface DiffService {

  public static final String QUERY_ACTIVE_ONLY = "active_only";
  public static final String QUERY_DATA_ETAG = "data_etag";
  public static final String QUERY_SEQUENCE_VALUE = "sequence_value";
  public static final String CURSOR_PARAMETER = "cursor";
  public static final String FETCH_LIMIT = "fetchLimit";

  /**
   *
   * @param dataETag
   * @param cursor - null or a websafeCursor value from the RowResourceList of a previous call
   * @param fetchLimit - null or the number of rows to fetch. If null, server will choose the limit.
   * @return {@link RowResourceList} of row changes since the dataETag value
   * @throws ODKDatastoreException
   * @throws PermissionDeniedException
   * @throws InconsistentStateException
   * @throws ODKTaskLockException
   * @throws BadColumnNameException
   */
  @GET
  @Produces({MediaType.APPLICATION_JSON, ApiConstants.MEDIA_TEXT_XML_UTF8, ApiConstants.MEDIA_APPLICATION_XML_UTF8})
  public Response /*RowResourceList*/ getRowsSince(@QueryParam(QUERY_DATA_ETAG) String dataETag, @QueryParam(CURSOR_PARAMETER) String cursor, @QueryParam(FETCH_LIMIT) String fetchLimit)
      throws ODKDatastoreException, PermissionDeniedException, InconsistentStateException, ODKTaskLockException, BadColumnNameException;
  
  /**
   * Get the changeSets that have been applied since the dataETag changeSet
   * (must be a valid dataETag) or since the given sequenceValue.
   * 
   * These are returned in no meaningful order. For consistency, the values
   * are sorted alphabetically. The returned object includes a sequenceValue
   * that can be used on a subsequent call to get all changes to this table
   * since this point in time.
   * 
   * @param dataETag
   * @param sequenceValue
   * @return
   * @throws ODKDatastoreException
   * @throws PermissionDeniedException
   * @throws InconsistentStateException
   * @throws ODKTaskLockException
   * @throws BadColumnNameException
   */
  @GET
  @Path("changeSets")
  @Produces({MediaType.APPLICATION_JSON, ApiConstants.MEDIA_TEXT_XML_UTF8, ApiConstants.MEDIA_APPLICATION_XML_UTF8})
  public Response /*ChangeSetList*/ getChangeSetsSince(@QueryParam(QUERY_DATA_ETAG) String dataETag, @QueryParam(QUERY_SEQUENCE_VALUE) String sequenceValue)
      throws ODKDatastoreException, PermissionDeniedException, InconsistentStateException, ODKTaskLockException, BadColumnNameException;

  /**
   * Retrieve the rows for the given dataETag changeSet.
   * If isActive is specified, then return only the currently-active
   * row changes. I.e., if a later changeSet has revised an
   * affected row, do not return that row.

   * @param dataETag
   * @param isActive
   * @param cursor
   * @param fetchLimit
   * @return
   * @throws ODKDatastoreException
   * @throws PermissionDeniedException
   * @throws InconsistentStateException
   * @throws ODKTaskLockException
   * @throws BadColumnNameException
   */
  @GET
  @Path("changeSets/{dataETag}")
  @Produces({MediaType.APPLICATION_JSON, ApiConstants.MEDIA_TEXT_XML_UTF8, ApiConstants.MEDIA_APPLICATION_XML_UTF8})
  public Response /*RowResourceList*/ getChangeSetRows(@PathParam("dataETag") String dataETag, @QueryParam(QUERY_ACTIVE_ONLY) String isActive, @QueryParam(CURSOR_PARAMETER) String cursor, @QueryParam(FETCH_LIMIT) String fetchLimit)
      throws ODKDatastoreException, PermissionDeniedException, InconsistentStateException, ODKTaskLockException, BadColumnNameException;
}
