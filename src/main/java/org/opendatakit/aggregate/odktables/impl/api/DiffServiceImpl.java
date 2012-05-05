package org.opendatakit.aggregate.odktables.impl.api;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.opendatakit.aggregate.odktables.DataManager;
import org.opendatakit.aggregate.odktables.api.DataService;
import org.opendatakit.aggregate.odktables.api.DiffService;
import org.opendatakit.aggregate.odktables.api.TableService;
import org.opendatakit.aggregate.odktables.entity.Row;
import org.opendatakit.aggregate.odktables.entity.api.RowResource;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.web.CallingContext;

public class DiffServiceImpl implements DiffService {
  private DataManager dm;
  private UriInfo info;

  public DiffServiceImpl(String tableId, UriInfo info, CallingContext cc)
      throws ODKEntityNotFoundException, ODKDatastoreException {
    this.dm = new DataManager(tableId, cc);
    this.info = info;
  }

  @Override
  public List<RowResource> getRowsSince(String dataEtag) throws ODKDatastoreException {
    List<Row> rows = dm.getRowsSince(dataEtag);
    return getResources(rows);
  }

  private RowResource getResource(Row row) {
    String tableId = dm.getTableId();
    String rowId = row.getRowId();
    UriBuilder ub = info.getBaseUriBuilder();
    ub.path(TableService.class);
    URI self = ub.clone().path(TableService.class, "getData").path(DataService.class, "getRow")
        .build(tableId, rowId);
    URI table = ub.clone().path(TableService.class, "getTable").build(tableId);
    RowResource resource = new RowResource(row);
    resource.setSelfUri(self.toASCIIString());
    resource.setTableUri(table.toASCIIString());
    return resource;
  }

  private List<RowResource> getResources(List<Row> rows) {
    ArrayList<RowResource> resources = new ArrayList<RowResource>();
    for (Row row : rows) {
      resources.add(getResource(row));
    }
    return resources;
  }
}
