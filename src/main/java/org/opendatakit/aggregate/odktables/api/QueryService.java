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

import java.text.ParseException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
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

public interface QueryService {

  public static final String QUERY_DATA_ETAG = "data_etag";
  public static final String QUERY_START_TIME = "startTime";
  public static final String QUERY_END_TIME = "endTime";
  public static final String CURSOR_PARAMETER = "cursor";
  public static final String FETCH_LIMIT = "fetchLimit";

 /**
  *
  * @param startTime - timestamp in format yyyy-MM-ddTHH:mm:ss.SSSSSSSSS
  * @param endTime - timestamp in format yyyy-MM-ddTHH:mm:ss.SSSSSSSSS 
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
 @Path("lastUpdateDate")
 @Produces({MediaType.APPLICATION_JSON, ApiConstants.MEDIA_TEXT_XML_UTF8, ApiConstants.MEDIA_APPLICATION_XML_UTF8})
 public Response /*RowResourceList*/ getRowsInTimeRangeBasedOnLastUpdateDate(@QueryParam(QUERY_START_TIME) String startTime, @QueryParam(QUERY_END_TIME) String endTime, @QueryParam(CURSOR_PARAMETER) String cursor, @QueryParam(FETCH_LIMIT) String fetchLimit)
     throws ODKDatastoreException, PermissionDeniedException, InconsistentStateException, ODKTaskLockException, BadColumnNameException, ParseException;

 /**
  *
  * @param startTime - timestamp in format yyyy-MM-ddTHH:mm:ss.SSSSSSSSS
  * @param endTime - timestamp in format yyyy-MM-ddTHH:mm:ss.SSSSSSSSS 
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
 @Path("savepointTimestamp")
 @Produces({MediaType.APPLICATION_JSON, ApiConstants.MEDIA_TEXT_XML_UTF8, ApiConstants.MEDIA_APPLICATION_XML_UTF8})
 public Response /*RowResourceList*/ getRowsInTimeRangeBasedOnSavepointTimestamp(@QueryParam(QUERY_START_TIME) String startTime, @QueryParam(QUERY_END_TIME) String endTime, @QueryParam(CURSOR_PARAMETER) String cursor, @QueryParam(FETCH_LIMIT) String fetchLimit)
   throws ODKDatastoreException, PermissionDeniedException, InconsistentStateException, ODKTaskLockException, BadColumnNameException, ParseException;
}