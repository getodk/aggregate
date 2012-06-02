package org.opendatakit.aggregate.odktables.impl.api;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.odktables.AuthFilter;
import org.opendatakit.aggregate.odktables.TableManager;
import org.opendatakit.aggregate.odktables.api.ColumnService;
import org.opendatakit.aggregate.odktables.api.DataService;
import org.opendatakit.aggregate.odktables.api.DiffService;
import org.opendatakit.aggregate.odktables.api.PropertiesService;
import org.opendatakit.aggregate.odktables.api.TableAclService;
import org.opendatakit.aggregate.odktables.api.TableService;
import org.opendatakit.aggregate.odktables.entity.Column;
import org.opendatakit.aggregate.odktables.entity.Scope;
import org.opendatakit.aggregate.odktables.entity.TableEntry;
import org.opendatakit.aggregate.odktables.entity.TableRole.TablePermission;
import org.opendatakit.aggregate.odktables.entity.api.TableDefinition;
import org.opendatakit.aggregate.odktables.entity.api.TableResource;
import org.opendatakit.aggregate.odktables.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.exception.TableAlreadyExistsException;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;
import org.opendatakit.common.web.CallingContext;

public class TableServiceImpl implements TableService {
  private CallingContext cc;
  private TableManager tm;
  private UriInfo info;

  public TableServiceImpl(@Context ServletContext sc, @Context HttpServletRequest req,
      @Context UriInfo info) {
    this.cc = ContextFactory.getCallingContext(sc, req);
    this.tm = new TableManager(cc);
    this.info = info;
  }

  @Override
  public List<TableResource> getTables() throws ODKDatastoreException {
    List<Scope> scopes = AuthFilter.getScopes(cc);
    List<TableEntry> entries = tm.getTables(scopes);
    ArrayList<TableResource> resources = new ArrayList<TableResource>();
    for (TableEntry entry : entries) {
      resources.add(getResource(entry));
    }
    return resources;
  }

  @Override
  public TableResource getTable(String tableId) throws ODKDatastoreException,
      PermissionDeniedException {
    new AuthFilter(tableId, cc).checkPermission(TablePermission.READ_TABLE_ENTRY);
    TableEntry entry = tm.getTableNullSafe(tableId);
    TableResource resource = getResource(entry);
    return resource;
  }

  @Override
  public TableResource createTable(String tableId, TableDefinition definition)
      throws ODKDatastoreException, TableAlreadyExistsException {
    // TODO: add access control stuff
    String tableName = definition.getTableName();
    List<Column> columns = definition.getColumns();
    String metadata = definition.getMetadata();
    TableEntry entry = tm.createTable(tableId, tableName, columns, metadata);
    TableResource resource = getResource(entry);
    return resource;
  }

  @Override
  public void deleteTable(String tableId) throws ODKDatastoreException, ODKTaskLockException,
      PermissionDeniedException {
    new AuthFilter(tableId, cc).checkPermission(TablePermission.DELETE_TABLE);
    tm.deleteTable(tableId);
  }

  @Override
  public ColumnService getColumns(String tableId) throws ODKDatastoreException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public DataService getData(String tableId) throws ODKDatastoreException {
    return new DataServiceImpl(tableId, info, cc);
  }

  @Override
  public PropertiesService getProperties(String tableId) throws ODKDatastoreException {
    return new PropertiesServiceImpl(tableId, info, cc);
  }

  @Override
  public DiffService getDiff(String tableId) throws ODKDatastoreException {
    return new DiffServiceImpl(tableId, info, cc);
  }

  @Override
  public TableAclService getAcl(String tableId) throws ODKDatastoreException {
    return new TableAclServiceImpl(tableId, info, cc);
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

    TableResource resource = new TableResource(entry);
    resource.setSelfUri(self.toASCIIString());
    resource.setPropertiesUri(properties.toASCIIString());
    resource.setDataUri(data.toASCIIString());
    resource.setDiffUri(diff.toASCIIString());
    resource.setAclUri(acl.toASCIIString());
    return resource;
  }

}