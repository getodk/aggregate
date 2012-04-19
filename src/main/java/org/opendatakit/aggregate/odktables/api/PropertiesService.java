package org.opendatakit.aggregate.odktables.api;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.opendatakit.aggregate.odktables.entity.TableProperties;
import org.opendatakit.aggregate.odktables.entity.api.PropertiesResource;
import org.opendatakit.aggregate.odktables.exception.EtagMismatchException;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;

@Produces(MediaType.TEXT_XML)
public interface PropertiesService {

  @GET
  @Path("")
  public PropertiesResource getProperties() throws ODKDatastoreException;

  @PUT
  @Path("")
  public PropertiesResource setProperties(TableProperties properties) throws ODKDatastoreException,
      ODKTaskLockException, EtagMismatchException;

}
