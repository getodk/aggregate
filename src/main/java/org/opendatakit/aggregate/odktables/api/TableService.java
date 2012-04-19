package org.opendatakit.aggregate.odktables.api;

import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.opendatakit.aggregate.odktables.entity.api.TableDefinition;
import org.opendatakit.aggregate.odktables.entity.api.TableResource;
import org.opendatakit.aggregate.odktables.exception.TableAlreadyExistsException;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;

@Path("/tables")
@Produces(MediaType.TEXT_XML)
public interface TableService {

  @GET
  public List<TableResource> getTables() throws ODKDatastoreException;

  @GET
  @Path("{tableId}")
  public TableResource getTable(@PathParam("tableId") String tableId) throws ODKDatastoreException;

  @PUT
  @Path("{tableId}")
  public TableResource createTable(@PathParam("tableId") String tableId, TableDefinition definition)
      throws ODKDatastoreException, TableAlreadyExistsException;

  @DELETE
  @Path("{tableId}")
  public void deleteTable(@PathParam("tableId") String tableId) throws ODKDatastoreException,
      ODKTaskLockException;

  @Path("{tableId}/columns")
  public ColumnService getColumns(@PathParam("tableId") String tableId)
      throws ODKDatastoreException;

  @Path("{tableId}/rows")
  public DataService getData(@PathParam("tableId") String tableId) throws ODKDatastoreException;

  @Path("{tableId}/properties")
  public PropertiesService getProperties(@PathParam("tableId") String tableId)
      throws ODKDatastoreException;
}
