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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opendatakit.aggregate.odktables.TableManager;
import org.opendatakit.aggregate.odktables.TableManager.WebsafeTables;
import org.opendatakit.aggregate.odktables.api.OdkTables;
import org.opendatakit.aggregate.odktables.api.RealizedTableService;
import org.opendatakit.aggregate.odktables.api.TableAclService;
import org.opendatakit.aggregate.odktables.api.TableService;
import org.opendatakit.aggregate.odktables.exception.AppNameMismatchException;
import org.opendatakit.aggregate.odktables.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.exception.SchemaETagMismatchException;
import org.opendatakit.aggregate.odktables.exception.TableAlreadyExistsException;
import org.opendatakit.aggregate.odktables.exception.TableNotFoundException;
import org.opendatakit.aggregate.odktables.rest.entity.Column;
import org.opendatakit.aggregate.odktables.rest.entity.TableDefinition;
import org.opendatakit.aggregate.odktables.rest.entity.TableEntry;
import org.opendatakit.aggregate.odktables.rest.entity.TableResource;
import org.opendatakit.aggregate.odktables.rest.entity.TableResourceList;
import org.opendatakit.aggregate.odktables.security.TablesUserPermissions;
import org.opendatakit.aggregate.odktables.security.TablesUserPermissionsImpl;
import org.opendatakit.common.persistence.QueryResumePoint;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;
import org.opendatakit.common.security.common.GrantedAuthorityName;
import org.opendatakit.common.security.server.SecurityServiceUtil;
import org.opendatakit.common.utils.WebUtils;
import org.opendatakit.common.web.CallingContext;

public class TableServiceImpl implements TableService {
  private static final Log logger = LogFactory.getLog(TableServiceImpl.class);

  private static final String ERROR_TABLE_NOT_FOUND = "Table not found";
  private static final String ERROR_SCHEMA_DIFFERS = "SchemaETag differs";

  private final ServletContext sc;
  private final HttpServletRequest req;
  private final HttpHeaders headers;
  private final UriInfo info;
  private final String appId;
  private final String tableId;
  private final CallingContext cc;

  public TableServiceImpl(ServletContext sc, HttpServletRequest req, HttpHeaders headers, UriInfo info, String appId, CallingContext cc)
      throws ODKEntityNotFoundException, ODKDatastoreException {
    this.sc = sc;
    this.req = req;
    this.headers = headers;
    this.info = info;
    this.appId = appId;
    tableId = null;
    this.cc = cc;
  }

  public TableServiceImpl(ServletContext sc, HttpServletRequest req, HttpHeaders headers, UriInfo info, String appId, String tableId, CallingContext cc)
      throws ODKEntityNotFoundException, ODKDatastoreException {
    this.sc = sc;
    this.req = req;
    this.headers = headers;
    this.info = info;
    this.appId = appId;
    this.tableId = tableId;
    this.cc = cc;
  }

  @Override
  public Response getTables(@QueryParam(CURSOR_PARAMETER) String cursor, @QueryParam(FETCH_LIMIT) String fetchLimit) throws ODKDatastoreException, PermissionDeniedException, ODKTaskLockException {

    TablesUserPermissions userPermissions = new TablesUserPermissionsImpl(cc);

    TableManager tm = new TableManager(appId, userPermissions, cc);

    int limit = (fetchLimit == null || fetchLimit.length() == 0) ? 2000 : Integer.parseInt(fetchLimit);
    WebsafeTables websafeResult = tm.getTables(QueryResumePoint.fromWebsafeCursor(WebUtils.safeDecode(cursor)), limit);
    ArrayList<TableResource> resources = new ArrayList<TableResource>();
    for (TableEntry entry : websafeResult.tables) {
      // database cruft will have a null schemaETag -- ignore those
      if ( entry.getSchemaETag() != null ) {
        TableResource resource = getResource(info, appId, entry);
        resources.add(resource);
      }
    }
    // TODO: add QueryResumePoint support
    TableResourceList tableResourceList = new TableResourceList(resources,
        WebUtils.safeEncode(websafeResult.websafeRefetchCursor),
        WebUtils.safeEncode(websafeResult.websafeBackwardCursor),
        WebUtils.safeEncode(websafeResult.websafeResumeCursor),
        websafeResult.hasMore, websafeResult.hasPrior);
    return Response.ok(tableResourceList).build();
  }

  @Override
  public Response getTable() throws ODKDatastoreException, TableNotFoundException,
      PermissionDeniedException, ODKTaskLockException {

    TablesUserPermissions userPermissions = new TablesUserPermissionsImpl(cc);

    TableManager tm = new TableManager(appId, userPermissions, cc);
    TableEntry entry = tm.getTable(tableId);
    if ( entry == null || entry.getSchemaETag() == null ) {
      // the table doesn't exist yet (or something is there that is database cruft)
      throw new TableNotFoundException(ERROR_TABLE_NOT_FOUND + "\n" + tableId);
    }
    TableResource resource = getResource(info, appId, entry);
    return Response.ok(resource).build();
  }

  @Override
  public Response createTable(TableDefinition definition)
      throws ODKDatastoreException, TableAlreadyExistsException, PermissionDeniedException, ODKTaskLockException, IOException {

    TreeSet<GrantedAuthorityName> ui = SecurityServiceUtil.getCurrentUserSecurityInfo(cc);
    if ( !ui.contains(GrantedAuthorityName.ROLE_ADMINISTER_TABLES) ) {
      throw new PermissionDeniedException("User does not belong to the 'Administer Tables' group");
    }

    TablesUserPermissions userPermissions = new TablesUserPermissionsImpl(cc);

    TableManager tm = new TableManager(appId, userPermissions, cc);
    // TODO: add access control stuff
    List<Column> columns = definition.getColumns();

    TableEntry entry = tm.createTable(tableId, columns);
    TableResource resource = getResource(info, appId, entry);
    logger.info(String.format("tableId: %s, definition: %s", tableId, definition));
    return Response.ok(resource).build();
  }

  @Override
  public RealizedTableService getRealizedTable(@PathParam("schemaETag") String schemaETag) throws ODKDatastoreException, PermissionDeniedException, SchemaETagMismatchException, AppNameMismatchException, ODKTaskLockException, TableNotFoundException {

    TablesUserPermissions userPermissions = new TablesUserPermissionsImpl(cc);

    TableManager tm = new TableManager(appId, userPermissions, cc);
    TableEntry entry = tm.getTable(tableId);
    if ( entry == null || entry.getSchemaETag() == null ) {
      // the table doesn't exist yet (or something is there that is database cruft)
      throw new TableNotFoundException(ERROR_TABLE_NOT_FOUND + "\n" + tableId);
    }
    if ( !entry.getSchemaETag().equals(schemaETag) ) {
      throw new SchemaETagMismatchException(ERROR_SCHEMA_DIFFERS + "\n" + entry.getSchemaETag());
    }
    RealizedTableService service = new RealizedTableServiceImpl(sc, req, headers, info, appId, tableId, schemaETag, userPermissions, tm, cc);
    return service;

  }

  @Override
  public TableAclService getAcl() throws ODKDatastoreException, AppNameMismatchException, PermissionDeniedException, ODKTaskLockException {

    TablesUserPermissions userPermissions = new TablesUserPermissionsImpl(cc);

    TableManager tm = new TableManager(appId, userPermissions, cc);
    TableAclService service = new TableAclServiceImpl(appId, tableId, info, userPermissions, cc);
    return service;
  }

  private TableResource getResource(UriInfo info, String appId, TableEntry entry) {
    String tableId = entry.getTableId();
    String schemaETag = entry.getSchemaETag();

    UriBuilder ub = info.getBaseUriBuilder();
    ub.path(OdkTables.class, "getTablesService");
    URI self = ub.clone().build(appId, tableId);
    UriBuilder realized = ub.clone().path(TableService.class, "getRealizedTable");
    URI data = realized.clone().path(RealizedTableService.class, "getData").build(appId, tableId, schemaETag);
    URI instanceFiles = realized.clone().path(RealizedTableService.class, "getInstanceFileService").build(appId, tableId, schemaETag);
    URI diff = realized.clone().path(RealizedTableService.class, "getDiff").build(appId, tableId, schemaETag);
    URI acl = ub.clone().path(TableService.class, "getAcl").build(appId, tableId);
    URI definition = realized.clone().build(appId, tableId, schemaETag);

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