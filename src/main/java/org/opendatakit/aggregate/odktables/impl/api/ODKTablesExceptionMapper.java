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

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;

import org.opendatakit.aggregate.odktables.exception.AppNameMismatchException;
import org.opendatakit.aggregate.odktables.exception.BadColumnNameException;
import org.opendatakit.aggregate.odktables.exception.ETagMismatchException;
import org.opendatakit.aggregate.odktables.exception.FileNotFoundException;
import org.opendatakit.aggregate.odktables.exception.InconsistentStateException;
import org.opendatakit.aggregate.odktables.exception.InstanceFileModificationException;
import org.opendatakit.aggregate.odktables.exception.NotModifiedException;
import org.opendatakit.aggregate.odktables.exception.ODKTablesException;
import org.opendatakit.aggregate.odktables.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.exception.SchemaETagMismatchException;
import org.opendatakit.aggregate.odktables.exception.TableAlreadyExistsException;
import org.opendatakit.aggregate.odktables.exception.TableDataETagMismatchException;
import org.opendatakit.aggregate.odktables.exception.TableNotFoundException;
import org.opendatakit.aggregate.odktables.rest.ApiConstants;
import org.opendatakit.aggregate.odktables.rest.entity.Error;
import org.opendatakit.aggregate.odktables.rest.entity.Error.ErrorType;

public class ODKTablesExceptionMapper implements ExceptionMapper<ODKTablesException> {

  MediaType type;
  
  ODKTablesExceptionMapper(MediaType type) {
    this.type = type;
  }
  @Override
  public Response toResponse(ODKTablesException e) {
    e.printStackTrace();

    String msg = e.getMessage();
    if (msg == null) {
      msg = e.toString();
    }
    if (e instanceof AppNameMismatchException) {
      return Response.status(Status.PRECONDITION_FAILED)
        .entity(new Error(ErrorType.APP_NAME_MISMATCH, msg)).type(type)
        .header(ApiConstants.OPEN_DATA_KIT_VERSION_HEADER, ApiConstants.OPEN_DATA_KIT_VERSION)
        .header("Access-Control-Allow-Origin", "*")
        .header("Access-Control-Allow-Credentials", "true").build();
    } else if (e instanceof BadColumnNameException) {
      return Response.status(Status.BAD_REQUEST).entity(new Error(ErrorType.BAD_COLUMN_NAME, msg))
          .type(type)
          .header(ApiConstants.OPEN_DATA_KIT_VERSION_HEADER, ApiConstants.OPEN_DATA_KIT_VERSION)
          .header("Access-Control-Allow-Origin", "*")
          .header("Access-Control-Allow-Credentials", "true").build();
    } else if (e instanceof ETagMismatchException) {
      return Response.status(Status.PRECONDITION_FAILED)
          .entity(new Error(ErrorType.ETAG_MISMATCH, msg)).type(type)
          .header(ApiConstants.OPEN_DATA_KIT_VERSION_HEADER, ApiConstants.OPEN_DATA_KIT_VERSION)
          .header("Access-Control-Allow-Origin", "*")
          .header("Access-Control-Allow-Credentials", "true").build();
    } else if (e instanceof TableDataETagMismatchException) {
      return Response.status(Status.CONFLICT)
          .entity(new Error(ErrorType.ETAG_MISMATCH, msg)).type(type)
          .header(ApiConstants.OPEN_DATA_KIT_VERSION_HEADER, ApiConstants.OPEN_DATA_KIT_VERSION)
          .header("Access-Control-Allow-Origin", "*")
          .header("Access-Control-Allow-Credentials", "true").build();
    } else if (e instanceof InconsistentStateException) {
      return Response.status(Status.INTERNAL_SERVER_ERROR)
          .entity(new Error(ErrorType.INTERNAL_ERROR, msg)).type(type)
          .header(ApiConstants.OPEN_DATA_KIT_VERSION_HEADER, ApiConstants.OPEN_DATA_KIT_VERSION)
          .header("Access-Control-Allow-Origin", "*")
          .header("Access-Control-Allow-Credentials", "true").build();
    } else if (e instanceof NotModifiedException) {
      return Response.status(Status.NOT_MODIFIED).header(HttpHeaders.ETAG, ((NotModifiedException) e).getETag())
          .header(ApiConstants.OPEN_DATA_KIT_VERSION_HEADER, ApiConstants.OPEN_DATA_KIT_VERSION)
          .header("Access-Control-Allow-Origin", "*")
          .header("Access-Control-Allow-Credentials", "true").build();
    } else if (e instanceof PermissionDeniedException) {
      return Response.status(Status.UNAUTHORIZED).entity(new Error(ErrorType.PERMISSION_DENIED, msg))
          .type(type)
          .header(ApiConstants.OPEN_DATA_KIT_VERSION_HEADER, ApiConstants.OPEN_DATA_KIT_VERSION)
          .header("Access-Control-Allow-Origin", "*")
          .header("Access-Control-Allow-Credentials", "true").build();
    } else if (e instanceof SchemaETagMismatchException) {
      return Response.status(Status.PRECONDITION_FAILED)
          .entity(new Error(ErrorType.SCHEMA_ETAG_MISMATCH, msg)).type(type)
          .header(ApiConstants.OPEN_DATA_KIT_VERSION_HEADER, ApiConstants.OPEN_DATA_KIT_VERSION)
          .header("Access-Control-Allow-Origin", "*")
          .header("Access-Control-Allow-Credentials", "true").build();
    } else if (e instanceof TableAlreadyExistsException) {
      return Response.status(Status.CONFLICT).entity(new Error(ErrorType.TABLE_EXISTS, msg))
          .type(type)
          .header(ApiConstants.OPEN_DATA_KIT_VERSION_HEADER, ApiConstants.OPEN_DATA_KIT_VERSION)
          .header("Access-Control-Allow-Origin", "*")
          .header("Access-Control-Allow-Credentials", "true").build();
    } else if (e instanceof TableNotFoundException) {
      return Response.status(Status.NOT_FOUND).entity(new Error(ErrorType.TABLE_NOT_FOUND, msg))
          .type(type)
          .header(ApiConstants.OPEN_DATA_KIT_VERSION_HEADER, ApiConstants.OPEN_DATA_KIT_VERSION)
          .header("Access-Control-Allow-Origin", "*")
          .header("Access-Control-Allow-Credentials", "true").build();
    } else if (e instanceof FileNotFoundException) {
      return Response.status(Status.NOT_FOUND).entity(new Error(ErrorType.RESOURCE_NOT_FOUND, msg))
          .type(type)
          .header(ApiConstants.OPEN_DATA_KIT_VERSION_HEADER, ApiConstants.OPEN_DATA_KIT_VERSION)
          .header("Access-Control-Allow-Origin", "*")
          .header("Access-Control-Allow-Credentials", "true").build();
    } else if (e instanceof InstanceFileModificationException) {
      return Response.status(Status.BAD_REQUEST).entity(new Error(ErrorType.BAD_REQUEST, msg))
          .type(type)
          .header(ApiConstants.OPEN_DATA_KIT_VERSION_HEADER, ApiConstants.OPEN_DATA_KIT_VERSION)
          .header("Access-Control-Allow-Origin", "*")
          .header("Access-Control-Allow-Credentials", "true").build();
    } else {
      return Response.status(Status.BAD_REQUEST).entity(new Error(ErrorType.BAD_REQUEST, msg))
          .type(type)
          .header(ApiConstants.OPEN_DATA_KIT_VERSION_HEADER, ApiConstants.OPEN_DATA_KIT_VERSION)
          .header("Access-Control-Allow-Origin", "*")
          .header("Access-Control-Allow-Credentials", "true").build();
    }
  }

}
