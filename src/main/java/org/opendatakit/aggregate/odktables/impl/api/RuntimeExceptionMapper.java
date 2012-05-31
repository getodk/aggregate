package org.opendatakit.aggregate.odktables.impl.api;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class RuntimeExceptionMapper implements ExceptionMapper<RuntimeException> {

  @Override
  public Response toResponse(RuntimeException e) {
    if (e instanceof IllegalArgumentException) {
      return Response.status(Status.BAD_REQUEST).entity("Bad arguments: " + e.getMessage()).build();
    } else {
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
    }
  }

}
