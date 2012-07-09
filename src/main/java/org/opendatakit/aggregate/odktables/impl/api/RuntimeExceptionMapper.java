package org.opendatakit.aggregate.odktables.impl.api;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.opendatakit.aggregate.odktables.entity.api.Error;
import org.opendatakit.aggregate.odktables.entity.api.Error.ErrorType;

@Provider
public class RuntimeExceptionMapper implements ExceptionMapper<RuntimeException> {

  @Override
  public Response toResponse(RuntimeException e) {
    if (e instanceof IllegalArgumentException) {
      return Response.status(Status.BAD_REQUEST)
          .entity(new Error(ErrorType.BAD_REQUEST, "Bad arguments: " + e.getMessage())).build();
    } else {
      return Response.status(Status.INTERNAL_SERVER_ERROR)
          .entity(new Error(ErrorType.INTERNAL_ERROR, e.getMessage())).build();
    }
  }

}
