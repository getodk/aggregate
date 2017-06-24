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
import java.util.TreeSet;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opendatakit.aggregate.odktables.TableManager;
import org.opendatakit.aggregate.odktables.api.DataService;
import org.opendatakit.aggregate.odktables.api.DiffService;
import org.opendatakit.aggregate.odktables.api.InstanceFileService;
import org.opendatakit.aggregate.odktables.api.OdkTables;
import org.opendatakit.aggregate.odktables.api.QueryService;
import org.opendatakit.aggregate.odktables.api.RealizedTableService;
import org.opendatakit.aggregate.odktables.api.TableService;
import org.opendatakit.aggregate.odktables.exception.AppNameMismatchException;
import org.opendatakit.aggregate.odktables.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.exception.SchemaETagMismatchException;
import org.opendatakit.aggregate.odktables.exception.TableNotFoundException;
import org.opendatakit.aggregate.odktables.relation.DbInstallationInteractionLog;
import org.opendatakit.aggregate.odktables.rest.ApiConstants;
import org.opendatakit.aggregate.odktables.rest.entity.TableDefinition;
import org.opendatakit.aggregate.odktables.rest.entity.TableDefinitionResource;
import org.opendatakit.aggregate.odktables.security.TablesUserPermissions;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;
import org.opendatakit.common.security.common.GrantedAuthorityName;
import org.opendatakit.common.security.server.SecurityServiceUtil;
import org.opendatakit.common.web.CallingContext;

import com.fasterxml.jackson.databind.ObjectMapper;

public class RealizedTableServiceImpl implements RealizedTableService {
  private static final Log logger = LogFactory.getLog(RealizedTableServiceImpl.class);
  
  private static final ObjectMapper mapper = new ObjectMapper();

  private final ServletContext sc;
  private final HttpServletRequest req;
  private final HttpHeaders headers;
  private final UriInfo info;
  private final String appId;
  private final String tableId;
  private final String schemaETag;
  private final boolean notActiveSchema;
  private final TablesUserPermissions userPermissions;
  private final TableManager tm;
  private final CallingContext cc;

  public RealizedTableServiceImpl(ServletContext sc, HttpServletRequest req,
      HttpHeaders headers, UriInfo info,
      String appId, String tableId, String schemaETag, boolean notActiveSchema, 
      TablesUserPermissions userPermissions, TableManager tm, CallingContext cc)
      throws ODKEntityNotFoundException, ODKDatastoreException {
    this.sc = sc;
    this.req = req;
    this.headers = headers;
    this.info = info;
    this.appId = appId;
    this.tableId = tableId;
    this.schemaETag = schemaETag;
    this.notActiveSchema = notActiveSchema;
    this.userPermissions = userPermissions;
    this.tm = tm;
    this.cc = cc;
  }

  @Override
  public Response deleteTable() throws ODKDatastoreException, ODKTaskLockException,
      PermissionDeniedException {

    TreeSet<GrantedAuthorityName> ui = SecurityServiceUtil.getCurrentUserSecurityInfo(cc);
    if ( !ui.contains(GrantedAuthorityName.ROLE_ADMINISTER_TABLES) ) {
      throw new PermissionDeniedException("User does not belong to the 'Administer Tables' group");
    }

    tm.deleteTable(tableId);
    logger.info("tableId: " + tableId);

    {
      // if the request includes an installation header, 
      // log that the user that has been changing the configuration from that installation.
      String installationId = req.getHeader(ApiConstants.OPEN_DATA_KIT_INSTALLATION_HEADER);
      try {
        if ( installationId != null ) {
          DbInstallationInteractionLog.recordChangeConfigurationEntry(installationId, tableId, cc);
        }
      } catch ( Exception e ) {
        LogFactory.getLog(FileServiceImpl.class).error("Unable to recordChangeConfigurationEntry", e);
      }
    }

    return Response.ok()
        .header(ApiConstants.OPEN_DATA_KIT_VERSION_HEADER, ApiConstants.OPEN_DATA_KIT_VERSION)
        .header("Access-Control-Allow-Origin", "*")
        .header("Access-Control-Allow-Credentials", "true").build();
  }

  @Override
  public DataService getData() throws ODKDatastoreException, PermissionDeniedException, SchemaETagMismatchException, AppNameMismatchException, ODKTaskLockException, TableNotFoundException {

    if ( notActiveSchema ) {
      throw new TableNotFoundException(TableServiceImpl.ERROR_TABLE_NOT_FOUND + "\n" + tableId);
    }
    DataService service = new DataServiceImpl(appId, tableId, schemaETag, info, userPermissions, cc);
    return service;
  }

  @Override
  public DiffService getDiff() throws ODKDatastoreException, PermissionDeniedException, SchemaETagMismatchException, AppNameMismatchException, ODKTaskLockException, TableNotFoundException {

    if ( notActiveSchema ) {
      throw new TableNotFoundException(TableServiceImpl.ERROR_TABLE_NOT_FOUND + "\n" + tableId);
    }
    DiffService service = new DiffServiceImpl(appId, tableId, schemaETag, info, userPermissions, cc);
    return service;
  }

  @Override
  public QueryService getQuery() throws ODKDatastoreException, PermissionDeniedException, SchemaETagMismatchException, AppNameMismatchException, ODKTaskLockException, TableNotFoundException {

    if ( notActiveSchema ) {
      throw new TableNotFoundException(TableServiceImpl.ERROR_TABLE_NOT_FOUND + "\n" + tableId);
    }
    QueryService service = new QueryServiceImpl(appId, tableId, schemaETag, info, userPermissions, cc);
    return service;
  }

  @Override
  public InstanceFileService getInstanceFileService() throws PermissionDeniedException {
    throw new PermissionDeniedException("rowId is required");
  }

  @Override
  public InstanceFileService getInstanceFiles(@PathParam("rowId") String rowId) throws ODKDatastoreException, PermissionDeniedException, SchemaETagMismatchException, AppNameMismatchException, ODKTaskLockException, TableNotFoundException {

    if ( notActiveSchema ) {
      throw new TableNotFoundException(TableServiceImpl.ERROR_TABLE_NOT_FOUND + "\n" + tableId);
    }
    InstanceFileService service = new InstanceFileServiceImpl(appId, tableId, schemaETag, rowId, info, userPermissions, cc);
    return service;
  }

  @Override
  public Response getDefinition() throws ODKDatastoreException, PermissionDeniedException, ODKTaskLockException, AppNameMismatchException, TableNotFoundException {

    if ( notActiveSchema ) {
      throw new TableNotFoundException(TableServiceImpl.ERROR_TABLE_NOT_FOUND + "\n" + tableId);
    }
    TableDefinition definition = tm.getTableDefinition(tableId);
    TableDefinitionResource definitionResource = new TableDefinitionResource(definition);
    UriBuilder ub = info.getBaseUriBuilder();
    ub.path(OdkTables.class, "getTablesService");
    URI selfUri = ub.clone().path(TableService.class, "getRealizedTable").build(appId, tableId, schemaETag);
    URI tableUri = ub.clone().build(appId, tableId);
    try {
      definitionResource.setSelfUri(selfUri.toURL().toExternalForm());
      definitionResource.setTableUri(tableUri.toURL().toExternalForm());
    } catch (MalformedURLException e) {
      e.printStackTrace();
      throw new IllegalArgumentException("Unable to convert to URL");
    }
    return Response.ok(definitionResource)
        .header(ApiConstants.OPEN_DATA_KIT_VERSION_HEADER, ApiConstants.OPEN_DATA_KIT_VERSION)
        .header("Access-Control-Allow-Origin", "*")
        .header("Access-Control-Allow-Credentials", "true").build();
  }

  @Override
  public Response /*OK*/ postInstallationStatus(Object syncDetails) 
      throws AppNameMismatchException, PermissionDeniedException, ODKDatastoreException, ODKTaskLockException {

    {
      // if the request includes an installation header, 
      // log that the user that has been changing the configuration from that installation.
      String installationId = req.getHeader(ApiConstants.OPEN_DATA_KIT_INSTALLATION_HEADER);
      try {
        if ( installationId != null ) {
          DbInstallationInteractionLog.recordSyncStatusEntry(installationId, tableId, mapper.writeValueAsString(syncDetails), cc);
        }
      } catch ( Exception e ) {
        LogFactory.getLog(RealizedTableServiceImpl.class).error("Unable to recordSyncStatusEntry", e);
      }
    }

    return Response.ok()
        .header(ApiConstants.OPEN_DATA_KIT_VERSION_HEADER, ApiConstants.OPEN_DATA_KIT_VERSION)
        .header("Access-Control-Allow-Origin", "*")
        .header("Access-Control-Allow-Credentials", "true").build();
  }

}