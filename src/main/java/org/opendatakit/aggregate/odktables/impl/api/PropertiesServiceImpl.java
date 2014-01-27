/*
 * Copyright (C) 2012-2013 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.opendatakit.aggregate.odktables.impl.api;

import java.net.URI;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.opendatakit.aggregate.odktables.PropertiesManager;
import org.opendatakit.aggregate.odktables.api.PropertiesService;
import org.opendatakit.aggregate.odktables.api.TableService;
import org.opendatakit.aggregate.odktables.exception.ETagMismatchException;
import org.opendatakit.aggregate.odktables.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.rest.entity.PropertiesResource;
import org.opendatakit.aggregate.odktables.rest.entity.TableProperties;
import org.opendatakit.aggregate.odktables.security.TablesUserPermissions;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;
import org.opendatakit.common.web.CallingContext;

public class PropertiesServiceImpl implements PropertiesService {

  private PropertiesManager pm;
  private UriInfo info;

  public PropertiesServiceImpl(String tableId, UriInfo info, TablesUserPermissions userPermissions, CallingContext cc)
      throws ODKEntityNotFoundException, ODKDatastoreException {
    this.pm = new PropertiesManager(tableId, userPermissions, cc);
    this.info = info;
  }

  @Override
  public Response getProperties() throws ODKDatastoreException, PermissionDeniedException, ODKTaskLockException {
    TableProperties properties = pm.getProperties();
    PropertiesResource resource = getResource(properties);
    return Response.ok(resource).build();
  }

  @Override
  public Response setProperties(TableProperties properties) throws ODKDatastoreException,
      ETagMismatchException, ODKTaskLockException, PermissionDeniedException {
    properties = pm.setProperties(properties);
    PropertiesResource resource = getResource(properties);
    return Response.ok(resource).build();
  }

  private PropertiesResource getResource(TableProperties properties) {
    PropertiesResource propertiesResource = new PropertiesResource(properties);

    String tableId = pm.getTableId();
    UriBuilder ub = info.getBaseUriBuilder();
    ub.path(TableService.class);
    URI self = ub.clone().path(TableService.class, "getProperties")
        .path(PropertiesService.class, "getProperties").build(tableId);
    URI table = ub.clone().path(TableService.class, "getTable").build(tableId);

    propertiesResource.setSelfUri(self.toASCIIString());
    propertiesResource.setTableUri(table.toASCIIString());

    return propertiesResource;
  }

}
