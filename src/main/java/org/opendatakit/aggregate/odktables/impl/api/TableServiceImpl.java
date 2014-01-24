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
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.odktables.TableManager;
import org.opendatakit.aggregate.odktables.api.DataService;
import org.opendatakit.aggregate.odktables.api.DiffService;
import org.opendatakit.aggregate.odktables.api.PropertiesService;
import org.opendatakit.aggregate.odktables.api.TableAclService;
import org.opendatakit.aggregate.odktables.api.TableService;
import org.opendatakit.aggregate.odktables.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.exception.TableAlreadyExistsException;
import org.opendatakit.aggregate.odktables.relation.DbKeyValueStore;
import org.opendatakit.aggregate.odktables.rest.entity.Column;
import org.opendatakit.aggregate.odktables.rest.entity.TableDefinition;
import org.opendatakit.aggregate.odktables.rest.entity.TableDefinitionResource;
import org.opendatakit.aggregate.odktables.rest.entity.TableEntry;
import org.opendatakit.aggregate.odktables.rest.entity.TableResource;
import org.opendatakit.aggregate.odktables.rest.entity.TableResourceList;
import org.opendatakit.aggregate.odktables.security.TablesUserPermissions;
import org.opendatakit.aggregate.odktables.security.TablesUserPermissionsImpl;
import org.opendatakit.common.persistence.engine.gae.DatastoreImpl;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;
import org.opendatakit.common.web.CallingContext;

public class TableServiceImpl implements TableService {
  private static final Log logger = LogFactory.getLog(TableServiceImpl.class);

  private CallingContext cc;
  private TablesUserPermissions userPermissions;
  private TableManager tm;
  private UriInfo info;

  public TableServiceImpl(@Context ServletContext sc, @Context HttpServletRequest req,
      @Context UriInfo info) throws ODKDatastoreException, PermissionDeniedException {
    ServiceUtils.examineRequest(sc, req);
    this.cc = ContextFactory.getCallingContext(sc, req);
    this.userPermissions = new TablesUserPermissionsImpl(this.cc.getCurrentUser().getUriUser(), cc);
    this.tm = new TableManager(userPermissions, cc);
    this.info = info;
  }

  @Override
  public TableResourceList getTables() throws ODKDatastoreException {
    List<TableEntry> entries = tm.getTables();
    ArrayList<TableResource> resources = new ArrayList<TableResource>();
    for (TableEntry entry : entries) {
      TableResource resource = getResource(entry);
      if (entry.getPropertiesETag() != null) {
        String displayName = DbKeyValueStore.getDisplayName(entry.getTableId(),
            entry.getPropertiesETag(), cc);
        resource.setDisplayName(displayName);
      }
      resources.add(resource);
    }
    return new TableResourceList(resources);
  }

  @Override
  public TableResource getTable(String tableId) throws ODKDatastoreException,
      PermissionDeniedException {
    TableEntry entry = tm.getTableNullSafe(tableId);
    TableResource resource = getResource(entry);
    return resource;
  }

  @Override
  public TableResource createTable(String tableId, TableDefinition definition)
      throws ODKDatastoreException, TableAlreadyExistsException, PermissionDeniedException, ODKTaskLockException {
    // TODO: what if schemaETag is specified??? or if table already exists????
    // TODO: add access control stuff
    List<Column> columns = definition.getColumns();

    TableEntry entry = tm.createTable(tableId, columns);
    TableResource resource = getResource(entry);
    logger.info(String.format("tableId: %s, definition: %s", tableId, definition));
    return resource;
  }

  @Override
  public void deleteTable(String tableId) throws ODKDatastoreException, ODKTaskLockException,
      PermissionDeniedException {
    tm.deleteTable(tableId);
    logger.info("tableId: " + tableId);
    DatastoreImpl ds = (DatastoreImpl) cc.getDatastore();
    ds.getDam().logUsage();
  }

  @Override
  public DataService getData(String tableId) throws ODKDatastoreException {
    return new DataServiceImpl(tableId, info, userPermissions, cc);
  }

  @Override
  public PropertiesService getProperties(String tableId) throws ODKDatastoreException {
    return new PropertiesServiceImpl(tableId, info, userPermissions, cc);
  }

  @Override
  public DiffService getDiff(String tableId) throws ODKDatastoreException {
    return new DiffServiceImpl(tableId, info, userPermissions, cc);
  }

  @Override
  public TableAclService getAcl(String tableId) throws ODKDatastoreException {
    return new TableAclServiceImpl(tableId, info, userPermissions, cc);
  }

  @Override
  public TableDefinitionResource getDefinition(String tableId) throws ODKDatastoreException, PermissionDeniedException, ODKTaskLockException {
    // TODO: permissions stuff for a table, perhaps? or just at the row level?
    TableDefinition definition = tm.getTableDefinition(tableId);
    TableDefinitionResource definitionResource = new TableDefinitionResource(definition);
    UriBuilder ub = info.getBaseUriBuilder();
    ub.path(TableService.class);
    URI selfUri = ub.clone().path(TableService.class, "getDefinition").build(tableId);
    URI tableUri = ub.clone().path(TableService.class, "getTable").build(tableId);
    definitionResource.setSelfUri(selfUri.toASCIIString());
    definitionResource.setTableUri(tableUri.toASCIIString());
    return definitionResource;
  }

  private TableResource getResource(TableEntry entry) {
    String tableId = entry.getTableId();

    UriBuilder ub = info.getBaseUriBuilder();
    ub.path(TableService.class);
    URI self = ub.clone().path(TableService.class, "getTable").build(tableId);
    URI properties = ub.clone().path(TableService.class, "getProperties").build(tableId);
    URI data = ub.clone().path(TableService.class, "getData").build(tableId);
    URI diff = ub.clone().path(TableService.class, "getDiff").build(tableId);
    URI acl = ub.clone().path(TableService.class, "getAcl").build(tableId);
    URI definition = ub.clone().path(TableService.class, "getDefinition").build(tableId);

    TableResource resource = new TableResource(entry);
    resource.setSelfUri(self.toASCIIString());
    resource.setDefinitionUri(definition.toASCIIString());
    resource.setPropertiesUri(properties.toASCIIString());
    resource.setDataUri(data.toASCIIString());
    resource.setDiffUri(diff.toASCIIString());
    resource.setAclUri(acl.toASCIIString());
    return resource;
  }

}