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

import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.odktables.TableManager;
import org.opendatakit.aggregate.odktables.api.DataService;
import org.opendatakit.aggregate.odktables.api.DiffService;
import org.opendatakit.aggregate.odktables.api.InstanceFileService;
import org.opendatakit.aggregate.odktables.api.TableAclService;
import org.opendatakit.aggregate.odktables.api.TableService;
import org.opendatakit.aggregate.odktables.exception.AppNameMismatchException;
import org.opendatakit.aggregate.odktables.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.exception.SchemaETagMismatchException;
import org.opendatakit.aggregate.odktables.exception.TableAlreadyExistsException;
import org.opendatakit.aggregate.odktables.rest.entity.Column;
import org.opendatakit.aggregate.odktables.rest.entity.TableDefinition;
import org.opendatakit.aggregate.odktables.rest.entity.TableDefinitionResource;
import org.opendatakit.aggregate.odktables.rest.entity.TableEntry;
import org.opendatakit.aggregate.odktables.rest.entity.TableResource;
import org.opendatakit.aggregate.odktables.rest.entity.TableResourceList;
import org.opendatakit.aggregate.odktables.security.TablesUserPermissions;
import org.opendatakit.aggregate.odktables.security.TablesUserPermissionsImpl;
import org.opendatakit.aggregate.server.ServerPreferencesProperties;
import org.opendatakit.common.persistence.engine.gae.DatastoreImpl;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;
import org.opendatakit.common.web.CallingContext;

public class TableServiceImpl implements TableService {
  private static final Log logger = LogFactory.getLog(TableServiceImpl.class);

  private static final String ERROR_APP_ID_DIFFERS = "AppName differs";
  private static final String ERROR_SCHEMA_DIFFERS = "SchemaETag differs";

  private CallingContext cc;
  private TablesUserPermissions userPermissions;
  private String appId;
  private TableManager tm;
  private UriInfo info;

  public TableServiceImpl(@Context ServletContext sc, @Context HttpServletRequest req, @Context HttpHeaders httpHeaders,
      @Context UriInfo info) throws ODKDatastoreException, PermissionDeniedException, ODKTaskLockException {
    ServiceUtils.examineRequest(sc, req, httpHeaders);
    this.cc = ContextFactory.getCallingContext(sc, req);
    this.userPermissions = ContextFactory.getTablesUserPermissions(this.cc.getCurrentUser().getUriUser(), cc);
    this.appId = ContextFactory.getOdkTablesAppId(cc);
    this.tm = new TableManager(appId, userPermissions, cc);
    this.info = info;
  }

  @Override
  public Response getTables(@PathParam("appId") String appId, @QueryParam(QUERY_RESUME_PARAMETER) String resumeParameter) throws ODKDatastoreException {
    if ( !this.appId.equals(appId) ) {
      return Response.status(Status.BAD_REQUEST)
          .entity(ERROR_APP_ID_DIFFERS + "\n" + appId).build();
    }
    List<TableEntry> entries = tm.getTables();
    ArrayList<TableResource> resources = new ArrayList<TableResource>();
    for (TableEntry entry : entries) {
      TableResource resource = getResource(appId, entry);
      resources.add(resource);
    }
    // TODO: add QueryResumePoint support
    TableResourceList tableResourceList = new TableResourceList(resources, null);
    return Response.ok(tableResourceList).build();
  }

  @Override
  public Response getTable(@PathParam("appId") String appId, @PathParam("tableId") String tableId) throws ODKDatastoreException,
      PermissionDeniedException {
    if ( !this.appId.equals(appId) ) {
      return Response.status(Status.BAD_REQUEST)
          .entity(ERROR_APP_ID_DIFFERS + "\n" + appId).build();
    }
    TableEntry entry = tm.getTableNullSafe(tableId);
    TableResource resource = getResource(appId, entry);
    return Response.ok(resource).build();
  }

  @Override
  public Response createTable(@PathParam("appId") String appId, @PathParam("tableId") String tableId, TableDefinition definition)
      throws ODKDatastoreException, TableAlreadyExistsException, PermissionDeniedException, ODKTaskLockException {
    if ( !this.appId.equals(appId) ) {
      return Response.status(Status.BAD_REQUEST)
          .entity(ERROR_APP_ID_DIFFERS + "\n" + appId).build();
    }
    // TODO: add access control stuff
    List<Column> columns = definition.getColumns();

    TableEntry entry = tm.createTable(tableId, columns);
    TableResource resource = getResource(appId, entry);
    logger.info(String.format("tableId: %s, definition: %s", tableId, definition));
    return Response.ok(resource).build();
  }

  @Override
  public Response deleteTable(@PathParam("appId") String appId, @PathParam("tableId") String tableId, @PathParam("schemaETag") String schemaETag) throws ODKDatastoreException, ODKTaskLockException,
      PermissionDeniedException {
    if ( !this.appId.equals(appId) ) {
      return Response.status(Status.BAD_REQUEST)
          .entity(ERROR_APP_ID_DIFFERS + "\n" + appId).build();
    }
    TableEntry entry = tm.getTable(tableId);
    if ( !entry.getSchemaETag().equals(schemaETag) ) {
      return Response.status(Status.BAD_REQUEST)
          .entity(ERROR_SCHEMA_DIFFERS + "\n" + entry.getSchemaETag()).build();
    }
    tm.deleteTable(tableId);
    logger.info("tableId: " + tableId);
    DatastoreImpl ds = (DatastoreImpl) cc.getDatastore();
    ds.getDam().logUsage();
    return Response.ok().build();
  }

  @Override
  public DataService getData(@PathParam("appId") String appId, @PathParam("tableId") String tableId, @PathParam("schemaETag") String schemaETag) throws ODKDatastoreException, PermissionDeniedException, SchemaETagMismatchException, AppNameMismatchException {
    if ( !this.appId.equals(appId) ) {
      throw new AppNameMismatchException(ERROR_SCHEMA_DIFFERS + "\n" + appId);
    }
    TableEntry entry = tm.getTable(tableId);
    if ( !entry.getSchemaETag().equals(schemaETag) ) {
      throw new SchemaETagMismatchException(ERROR_SCHEMA_DIFFERS + "\n" + entry.getSchemaETag());
    }
    DataService service = new DataServiceImpl(appId, tableId, schemaETag, info, userPermissions, cc);
    return service;
  }

  @Override
  public DiffService getDiff(@PathParam("appId") String appId, @PathParam("tableId") String tableId, @PathParam("schemaETag") String schemaETag) throws ODKDatastoreException, PermissionDeniedException, SchemaETagMismatchException, AppNameMismatchException {
    if ( !this.appId.equals(appId) ) {
      throw new AppNameMismatchException(ERROR_SCHEMA_DIFFERS + "\n" + appId);
    }
    TableEntry entry = tm.getTable(tableId);
    if ( !entry.getSchemaETag().equals(schemaETag) ) {
      throw new SchemaETagMismatchException(ERROR_SCHEMA_DIFFERS + "\n" + entry.getSchemaETag());
    }
    DiffService service = new DiffServiceImpl(appId, tableId, schemaETag, info, userPermissions, cc);
    return service;
  }

  @Override
  public TableAclService getAcl(@PathParam("appId") String appId, @PathParam("tableId") String tableId) throws ODKDatastoreException, AppNameMismatchException {
    if ( !this.appId.equals(appId) ) {
      throw new AppNameMismatchException(ERROR_SCHEMA_DIFFERS + "\n" + appId);
    }
    TableAclService service = new TableAclServiceImpl(appId, tableId, info, userPermissions, cc);
    return service;
  }

  @Override
  public InstanceFileService getInstanceFiles(@PathParam("appId") String appId, @PathParam("tableId") String tableId, @PathParam("schemaETag") String schemaETag) throws ODKDatastoreException, PermissionDeniedException, SchemaETagMismatchException, AppNameMismatchException {
    if ( !this.appId.equals(appId) ) {
      throw new AppNameMismatchException(ERROR_SCHEMA_DIFFERS + "\n" + appId);
    }
    TableEntry entry = tm.getTable(tableId);
    if ( !entry.getSchemaETag().equals(schemaETag) ) {
      throw new SchemaETagMismatchException(ERROR_SCHEMA_DIFFERS + "\n" + entry.getSchemaETag());
    }
    InstanceFileService service = new InstanceFileServiceImpl(appId, tableId, schemaETag, info, userPermissions, cc);
    return service;
  }

  @Override
  public Response getDefinition(@PathParam("appId") String appId, @PathParam("tableId") String tableId, @PathParam("schemaETag") String schemaETag) throws ODKDatastoreException, PermissionDeniedException, ODKTaskLockException, AppNameMismatchException {
    if ( !this.appId.equals(appId) ) {
      throw new AppNameMismatchException(ERROR_SCHEMA_DIFFERS + "\n" + appId);
    }
    // TODO: permissions stuff for a table, perhaps? or just at the row level?
    TableDefinition definition = tm.getTableDefinition(tableId);
    if ( !definition.getSchemaETag().equals(schemaETag) ) {
      return Response.status(Status.BAD_REQUEST)
          .entity(ERROR_SCHEMA_DIFFERS + "\n" + definition.getSchemaETag()).build();
    }

    TableDefinitionResource definitionResource = new TableDefinitionResource(definition);
    UriBuilder ub = info.getBaseUriBuilder();
    ub.path(TableService.class);
    URI selfUri = ub.clone().path(TableService.class, "getDefinition").build(appId, tableId, schemaETag);
    URI tableUri = ub.clone().path(TableService.class, "getTable").build(appId, tableId);
    try {
      definitionResource.setSelfUri(selfUri.toURL().toExternalForm());
      definitionResource.setTableUri(tableUri.toURL().toExternalForm());
    } catch (MalformedURLException e) {
      e.printStackTrace();
      throw new IllegalArgumentException("Unable to convert to URL");
    }
    return Response.ok(definitionResource).build();
  }

  private TableResource getResource(String appId, TableEntry entry) {
    String tableId = entry.getTableId();
    String schemaETag = entry.getSchemaETag();

    UriBuilder ub = info.getBaseUriBuilder();
    ub.path(TableService.class);
    URI self = ub.clone().path(TableService.class, "getTable").build(appId, tableId);
    URI data = ub.clone().path(TableService.class, "getData").build(appId, tableId, schemaETag);
    URI instanceFiles = ub.clone().path(TableService.class, "getInstanceFiles").build(appId, tableId, schemaETag);
    URI diff = ub.clone().path(TableService.class, "getDiff").build(appId, tableId, schemaETag);
    URI acl = ub.clone().path(TableService.class, "getAcl").build(appId, tableId);
    URI definition = ub.clone().path(TableService.class, "getDefinition").build(appId, tableId, schemaETag);

    TableResource resource = new TableResource(entry);
    try {
      resource.setSelfUri(self.toURL().toExternalForm());
      resource.setDefinitionUri(definition.toURL().toExternalForm());
      resource.setDataUri(data.toURL().toExternalForm());
      resource.setInstanceFilesUri(instanceFiles.toURL().toExternalForm());
      resource.setDiffUri(diff.toURL().toExternalForm());
      resource.setAclUri(acl.toURL().toExternalForm());
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }
    return resource;
  }

}