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

import org.opendatakit.aggregate.odktables.api.entity.NewTable;
import org.opendatakit.aggregate.odktables.api.entity.TableResource;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;

@Path("/tables")
@Produces(MediaType.TEXT_PLAIN)
public interface TableService {

  @GET
  @Produces(MediaType.TEXT_XML)
  public List<TableResource> getTables() throws ODKDatastoreException;

  @GET
  @Path("{tableId}")
  @Produces(MediaType.TEXT_XML)
  public TableResource getTable(@PathParam("tableId") String tableId) throws ODKDatastoreException;

  @PUT
  @Path("{tableId}")
  @Consumes(MediaType.TEXT_XML)
  public TableResource createTable(NewTable newTable) throws ODKDatastoreException;

  @DELETE
  @Path("{tableId}")
  public void deleteTable(@PathParam("tableId") String tableId) throws ODKDatastoreException,
      ODKTaskLockException;

  @Path("{tableId}/columns")
  public ColumnResource getColumns(@PathParam("tableId") String tableId)
      throws ODKDatastoreException;

  @Path("{tableId}/rows")
  public DataResource getData(@PathParam("tableId") String tableId) throws ODKDatastoreException;
}
