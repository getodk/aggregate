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
import javax.ws.rs.core.Response;

import org.opendatakit.aggregate.odktables.api.entity.RowResource;
import org.opendatakit.aggregate.odktables.entity.Row;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;

public interface DataResource {

  @GET
  @Produces(MediaType.TEXT_XML)
  public List<RowResource> getRows() throws ODKDatastoreException;

  @GET
  @Path("{rowId}")
  @Produces(MediaType.TEXT_XML)
  public Response getRow(@PathParam("rowId") String rowId) throws ODKDatastoreException;

  @PUT
  @Path("{rowId}")
  @Consumes(MediaType.TEXT_XML)
  public Response createOrUpdateRow(@PathParam("rowId") String rowId, Row row)
      throws ODKTaskLockException, ODKDatastoreException;

  @DELETE
  @Path("{rowId}")
  public Response deleteRow(@PathParam("rowId") String rowId) throws ODKDatastoreException,
      ODKTaskLockException;

}