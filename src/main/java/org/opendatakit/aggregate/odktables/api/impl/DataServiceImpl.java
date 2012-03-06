package org.opendatakit.aggregate.odktables.api.impl;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import lombok.val;

import org.opendatakit.aggregate.odktables.DataManager;
import org.opendatakit.aggregate.odktables.api.DataService;
import org.opendatakit.aggregate.odktables.api.TableService;
import org.opendatakit.aggregate.odktables.api.entity.RowResource;
import org.opendatakit.aggregate.odktables.entity.Row;
import org.opendatakit.aggregate.odktables.exception.RowVersionMismatchException;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;
import org.opendatakit.common.web.CallingContext;

public class DataServiceImpl implements DataService {

  private DataManager dm;
  private UriInfo info;

  public DataServiceImpl(String tableId, UriInfo info, CallingContext cc)
      throws ODKEntityNotFoundException, ODKDatastoreException {
    this.dm = new DataManager(tableId, cc);
    this.info = info;
  }

  @Override
  public List<RowResource> getRows() throws ODKDatastoreException {
    val rows = dm.getRows();
    val resources = new ArrayList<RowResource>();
    for (val row : rows) {
      resources.add(getResource(row));
    }
    return resources;
  }

  @Override
  public RowResource getRow(String rowId) throws ODKDatastoreException {
    val row = dm.getRowNullSafe(rowId);
    val resource = getResource(row);
    return resource;
  }

  @Override
  public RowResource createOrUpdateRow(String rowId, Row row) throws ODKTaskLockException,
      ODKDatastoreException, RowVersionMismatchException {
    row.setRowId(rowId);
    val dbRow = dm.getRow(rowId);
    if (dbRow == null) {
      row = dm.insertRow(row);
    } else {
      row = dm.updateRow(row);
    }
    val resource = getResource(row);
    return resource;
  }

  @Override
  public void deleteRow(String rowId) throws ODKDatastoreException, ODKTaskLockException {
    dm.deleteRow(rowId);
  }

  private RowResource getResource(Row row) {
    String tableId = dm.getTableId();
    String rowId = row.getRowId();

    UriBuilder ub = info.getBaseUriBuilder();
    ub.path(TableService.class);
    ub.path(TableService.class, "getData");
    URI self = ub.clone().path(DataService.class, "getRow").build(tableId, rowId);
    URI table = ub.clone().build(tableId);

    RowResource resource = new RowResource(row);
    resource.setSelfUri(self.toASCIIString());
    resource.setTableUri(table.toASCIIString());
    return resource;
  }
}
