package org.opendatakit.aggregate.odktables.impl.api;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.opendatakit.aggregate.odktables.exception.ODKTablesException;
import org.opendatakit.aggregate.odktables.exception.RowEtagMismatchException;
import org.opendatakit.aggregate.odktables.exception.TableAlreadyExistsException;

@Provider
public class ODKTablesExceptionMapper implements ExceptionMapper<ODKTablesException> {

  @Override
  public Response toResponse(ODKTablesException e) {
    if (e instanceof RowEtagMismatchException) {
      return Response.status(Status.PRECONDITION_FAILED).entity(e.getMessage()).build();
    } else if (e instanceof TableAlreadyExistsException) {
      return Response.status(Status.CONFLICT).entity(e.getMessage()).build();
    } else {
      return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
    }
  }

}
