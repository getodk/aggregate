package org.opendatakit.aggregate.odktables.impl.api;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.opendatakit.aggregate.odktables.entity.api.Error;
import org.opendatakit.aggregate.odktables.entity.api.Error.ErrorType;
import org.opendatakit.aggregate.odktables.exception.BadColumnNameException;
import org.opendatakit.aggregate.odktables.exception.EtagMismatchException;
import org.opendatakit.aggregate.odktables.exception.ODKTablesException;
import org.opendatakit.aggregate.odktables.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.exception.TableAlreadyExistsException;

@Provider
public class ODKTablesExceptionMapper implements ExceptionMapper<ODKTablesException> {

  @Override
  public Response toResponse(ODKTablesException e) {
    if (e instanceof EtagMismatchException) {
      return Response.status(Status.PRECONDITION_FAILED)
          .entity(new Error(ErrorType.ETAG_MISMATCH, e.getMessage())).type(MediaType.TEXT_XML)
          .build();
    } else if (e instanceof TableAlreadyExistsException) {
      return Response.status(Status.CONFLICT)
          .entity(new Error(ErrorType.TABLE_EXISTS, e.getMessage())).type(MediaType.TEXT_XML)
          .build();
    } else if (e instanceof PermissionDeniedException) {
      return Response.status(Status.FORBIDDEN)
          .entity(new Error(ErrorType.PERMISSION_DENIED, e.getMessage())).type(MediaType.TEXT_XML)
          .build();
    } else if (e instanceof BadColumnNameException) {
      return Response.status(Status.BAD_REQUEST)
          .entity(new Error(ErrorType.BAD_COLUMN_NAME, e.getMessage())).type(MediaType.TEXT_XML)
          .build();
    } else {
      return Response.status(Status.BAD_REQUEST)
          .entity(new Error(ErrorType.BAD_REQUEST, e.getMessage())).type(MediaType.TEXT_XML)
          .build();
    }
  }

}
