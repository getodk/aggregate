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

import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.opendatakit.aggregate.odktables.rest.entity.Error;
import org.opendatakit.aggregate.odktables.rest.entity.Error.ErrorType;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.persistence.exception.ODKOverQuotaException;

@Provider
public class ODKDatastoreExceptionMapper implements ExceptionMapper<ODKDatastoreException> {

  @Context
  private HttpHeaders headers;

  @Override
  public Response toResponse(ODKDatastoreException e) {
    MediaType type;
    e.printStackTrace();
    type = (headers.getAcceptableMediaTypes().size() != 0) ? headers.getAcceptableMediaTypes().get(
        0) : MediaType.APPLICATION_JSON_TYPE;

    String msg = e.getMessage();
    if (msg == null) {
      msg = e.toString();
    }

    if (e instanceof ODKEntityNotFoundException) {
      return Response.status(Status.NOT_FOUND).entity(new Error(ErrorType.RESOURCE_NOT_FOUND, msg))
          .type(type).build();
    } else if (e instanceof ODKEntityPersistException) {
      return Response.status(Status.INTERNAL_SERVER_ERROR)
          .entity(new Error(ErrorType.INTERNAL_ERROR, "Could not save: " + msg)).type(type).build();
    } else if (e instanceof ODKOverQuotaException) {
      return Response.status(Status.INTERNAL_SERVER_ERROR)
          .entity(new Error(ErrorType.INTERNAL_ERROR, "Over quota: " + msg)).type(type).build();
    } else {
      return Response.status(Status.INTERNAL_SERVER_ERROR)
          .entity(new Error(ErrorType.INTERNAL_ERROR, msg)).type(type).build();
    }
  }

}
