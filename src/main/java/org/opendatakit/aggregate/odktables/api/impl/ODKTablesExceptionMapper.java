package org.opendatakit.aggregate.odktables.api.impl;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.opendatakit.aggregate.odktables.exception.ODKTablesException;
import org.opendatakit.aggregate.odktables.exception.RowVersionMismatchException;

@Provider
public class ODKTablesExceptionMapper implements ExceptionMapper<ODKTablesException> {

  @Override
  public Response toResponse(ODKTablesException e) {
    if (e instanceof RowVersionMismatchException) {
      return Response.status(Status.PRECONDITION_FAILED).entity(e.getMessage()).build();
    } else {
      return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
    }
  }

}
