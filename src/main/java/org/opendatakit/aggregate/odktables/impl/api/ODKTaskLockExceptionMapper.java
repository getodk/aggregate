package org.opendatakit.aggregate.odktables.impl.api;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.opendatakit.aggregate.odktables.entity.api.Error;
import org.opendatakit.aggregate.odktables.entity.api.Error.ErrorType;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;

@Provider
public class ODKTaskLockExceptionMapper implements ExceptionMapper<ODKTaskLockException> {

  @Override
  public Response toResponse(ODKTaskLockException e) {
    return Response
        .status(Status.INTERNAL_SERVER_ERROR)
        .entity(
            new Error(ErrorType.LOCK_TIMEOUT, "Please try again later. "
                + "Timed out waiting for lock: " + e.getMessage())).type(MediaType.TEXT_XML)
        .build();
  }
}
