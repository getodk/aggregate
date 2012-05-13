package org.opendatakit.aggregate.odktables.api;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.opendatakit.aggregate.odktables.entity.Row;
import org.opendatakit.aggregate.odktables.entity.api.RowResource;
import org.opendatakit.aggregate.odktables.exception.EtagMismatchException;
import org.opendatakit.aggregate.odktables.exception.PermissionDeniedException;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;

@Produces(MediaType.TEXT_XML)
public interface DataService {

  @GET
  public List<RowResource> getRows() throws ODKDatastoreException, PermissionDeniedException;

  @GET
  @Path("{rowId}")
  public RowResource getRow(@PathParam("rowId") String rowId) throws ODKDatastoreException,
      PermissionDeniedException;

  @PUT
  @Path("{rowId}")
  @Consumes(MediaType.TEXT_XML)
  public RowResource createOrUpdateRow(@PathParam("rowId") String rowId, Row row)
      throws ODKTaskLockException, ODKDatastoreException, EtagMismatchException,
      PermissionDeniedException;

  @DELETE
  @Path("{rowId}")
  public void deleteRow(@PathParam("rowId") String rowId) throws ODKDatastoreException,
      ODKTaskLockException, PermissionDeniedException;

}