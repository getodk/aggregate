package org.opendatakit.aggregate.odktables.impl.api;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.opendatakit.aggregate.odktables.AuthFilter;
import org.opendatakit.aggregate.odktables.DataManager;
import org.opendatakit.aggregate.odktables.api.DataService;
import org.opendatakit.aggregate.odktables.api.DiffService;
import org.opendatakit.aggregate.odktables.api.TableService;
import org.opendatakit.aggregate.odktables.entity.Row;
import org.opendatakit.aggregate.odktables.entity.Scope;
import org.opendatakit.aggregate.odktables.entity.TableRole.TablePermission;
import org.opendatakit.aggregate.odktables.entity.api.RowResource;
import org.opendatakit.aggregate.odktables.exception.PermissionDeniedException;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.web.CallingContext;

public class DiffServiceImpl implements DiffService {
  private CallingContext cc;
  private DataManager dm;
  private UriInfo info;
  private AuthFilter af;

  public DiffServiceImpl(String tableId, UriInfo info, CallingContext cc)
      throws ODKEntityNotFoundException, ODKDatastoreException {
    this.cc = cc;
    this.dm = new DataManager(tableId, cc);
    this.info = info;
    this.af = new AuthFilter(tableId, cc);
  }

  @Override
  public List<RowResource> getRowsSince(String dataEtag) throws ODKDatastoreException,
      PermissionDeniedException {
    af.checkPermission(TablePermission.READ_ROW);
    List<Row> rows;
    if (af.hasPermission(TablePermission.UNFILTERED_READ)) {
      rows = dm.getRowsSince(dataEtag);
    } else {
      List<Scope> scopes = AuthFilter.getScopes(cc);
      rows = dm.getRowsSince(dataEtag, scopes);
    }
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
