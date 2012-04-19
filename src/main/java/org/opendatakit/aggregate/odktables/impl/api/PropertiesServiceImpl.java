package org.opendatakit.aggregate.odktables.impl.api;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.opendatakit.aggregate.odktables.PropertiesManager;
import org.opendatakit.aggregate.odktables.api.PropertiesService;
import org.opendatakit.aggregate.odktables.api.TableService;
import org.opendatakit.aggregate.odktables.entity.TableProperties;
import org.opendatakit.aggregate.odktables.entity.api.PropertiesResource;
import org.opendatakit.aggregate.odktables.exception.EtagMismatchException;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;
import org.opendatakit.common.web.CallingContext;

public class PropertiesServiceImpl implements PropertiesService {

  private PropertiesManager pm;
  private UriInfo info;

  public PropertiesServiceImpl(String tableId, UriInfo info, CallingContext cc)
      throws ODKEntityNotFoundException, ODKDatastoreException {
    this.pm = new PropertiesManager(tableId, cc);
    this.info = info;
  }

  @Override
  public PropertiesResource getProperties() throws ODKDatastoreException {
    TableProperties properties = pm.getProperties();
    return getResource(properties);
  }

  @Override
  public PropertiesResource setProperties(TableProperties properties) throws ODKDatastoreException,
      EtagMismatchException, ODKTaskLockException {
    properties = pm.setProperties(properties);
    return getResource(properties);
  }

  private PropertiesResource getResource(TableProperties properties) {
    PropertiesResource propertiesResource = new PropertiesResource(properties);

    String tableId = pm.getTableId();
    UriBuilder ub = info.getBaseUriBuilder();
    ub.path(TableService.class);
    URI self = ub.clone().path(TableService.class, "getData")
        .path(PropertiesService.class, "getProperties").build(tableId);
    URI table = ub.clone().path(TableService.class, "getTable").build(tableId);

    propertiesResource.setSelfUri(self.toASCIIString());
    propertiesResource.setTableUri(table.toASCIIString());

    return propertiesResource;
  }

}
