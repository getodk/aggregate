package org.opendatakit.aggregate.odktables.impl.api;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import org.opendatakit.aggregate.odktables.DataManager;
import org.opendatakit.aggregate.odktables.api.DataService;
import org.opendatakit.aggregate.odktables.api.TableService;
import org.opendatakit.aggregate.odktables.entity.Row;
import org.opendatakit.aggregate.odktables.entity.api.RowResource;
import org.opendatakit.aggregate.odktables.exception.RowEtagMismatchException;
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
		List<Row> rows = dm.getRows();
		return getResources(rows);
	}

	@Override
	public List<RowResource> getRowsSince(String dataEtag)
			throws ODKDatastoreException {
		List<Row> rows = dm.getRowsSince(dataEtag);
		return getResources(rows);
	}

	@Override
	public RowResource getRow(String rowId) throws ODKDatastoreException {
		Row row = dm.getRowNullSafe(rowId);
		RowResource resource = getResource(row);
		return resource;
	}

	@Override
	public RowResource createOrUpdateRow(String rowId, Row row)
			throws ODKTaskLockException, ODKDatastoreException,
			RowEtagMismatchException {
		row.setRowId(rowId);
		Row dbRow = dm.getRow(rowId);
		if (dbRow == null) {
			row = dm.insertRow(row);
		} else {
			row = dm.updateRow(row);
		}
		RowResource resource = getResource(row);
		return resource;
	}

	@Override
	public void deleteRow(String rowId) throws ODKDatastoreException,
			ODKTaskLockException {
		dm.deleteRow(rowId);
	}

	private RowResource getResource(Row row) {
		String tableId = dm.getTableId();
		String rowId = row.getRowId();
		UriBuilder ub = info.getBaseUriBuilder();
		ub.path(TableService.class);
		URI self = ub.clone().path(TableService.class, "getData")
				.path(DataService.class, "getRow").build(tableId, rowId);
		URI table = ub.clone().path(TableService.class, "getTable")
				.build(tableId);
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