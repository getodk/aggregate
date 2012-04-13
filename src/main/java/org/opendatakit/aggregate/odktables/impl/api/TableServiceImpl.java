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
import org.opendatakit.aggregate.odktables.TableManager;
import org.opendatakit.aggregate.odktables.api.ColumnService;
import org.opendatakit.aggregate.odktables.api.DataService;
import org.opendatakit.aggregate.odktables.api.TableService;
import org.opendatakit.aggregate.odktables.entity.Column;
import org.opendatakit.aggregate.odktables.entity.TableEntry;
import org.opendatakit.aggregate.odktables.entity.api.TableDefinition;
import org.opendatakit.aggregate.odktables.entity.api.TableResource;
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
    cc = ContextFactory.getCallingContext(sc, req);
    tm = new TableManager(cc);
    this.info = info;
  }

  @Override
  public List<TableResource> getTables() throws ODKDatastoreException {
    List<TableEntry> entries = tm.getTables();
    ArrayList<TableResource> resources = new ArrayList<TableResource>();
    for (TableEntry entry : entries) {
      resources.add(getResource(entry));
    }
    return resources;
  }

  @Override
  public TableResource getTable(String tableId) throws ODKDatastoreException {
    TableEntry entry = tm.getTableNullSafe(tableId);
    TableResource resource = getResource(entry);
    return resource;
  }

  @Override
  public TableResource createTable(String tableId, TableDefinition definition)
      throws ODKDatastoreException, TableAlreadyExistsException {
    List<Column> columns = definition.getColumns();
    // TODO: fix
    TableEntry entry = tm.createTable(tableId, tableId, columns, null);
    TableResource resource = getResource(entry);
    return resource;
  }

  @Override
  public void deleteTable(String tableId) throws ODKDatastoreException, ODKTaskLockException {
    tm.deleteTable(tableId);
  }

  @Override
  public DataService getData(String tableId) throws ODKDatastoreException {
    return new DataServiceImpl(tableId, info, cc);
  }

  @Override
  public ColumnService getColumns(String tableId) throws ODKDatastoreException {
    // TODO Auto-generated method stub
    return null;
  }

  private TableResource getResource(TableEntry entry) {
    String tableId = entry.getTableId();

    UriBuilder ub = info.getBaseUriBuilder();
    ub.path(TableService.class);
    URI self = ub.clone().path(TableService.class, "getTable").build(tableId);
    URI columns = ub.clone().path(TableService.class, "getColumns").build(tableId);
    URI data = ub.clone().path(TableService.class, "getData").build(tableId);
    URI diff = UriBuilder.fromUri(data).path(DataService.class, "getRowsSince").build();

    TableResource resource = new TableResource(entry);
    resource.setSelfUri(self.toASCIIString());
    resource.setColumnsUri(columns.toASCIIString());
    resource.setDataUri(data.toASCIIString());
    resource.setDiffUri(diff.toASCIIString());
    return resource;
  }
}