package org.opendatakit.aggregate.server;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.client.exception.RequestFailureException;
import org.opendatakit.aggregate.client.odktables.RowClient;
import org.opendatakit.aggregate.client.odktables.RowResourceClient;
import org.opendatakit.aggregate.client.odktables.ServerDataService;
import org.opendatakit.aggregate.odktables.AuthFilter;
import org.opendatakit.aggregate.odktables.DataManager;
import org.opendatakit.aggregate.odktables.api.DataService;
import org.opendatakit.aggregate.odktables.api.TableService;
import org.opendatakit.aggregate.odktables.entity.Row;
import org.opendatakit.aggregate.odktables.entity.Scope;
import org.opendatakit.aggregate.odktables.entity.TableRole.TablePermission;
import org.opendatakit.aggregate.odktables.entity.api.RowResource;
import org.opendatakit.aggregate.odktables.exception.BadColumnNameException;
import org.opendatakit.aggregate.odktables.exception.EtagMismatchException;
import org.opendatakit.aggregate.odktables.exception.PermissionDeniedException;
import org.opendatakit.common.persistence.client.exception.DatastoreFailureException;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;
import org.opendatakit.common.security.client.exception.AccessDeniedException;
import org.opendatakit.common.web.CallingContext;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * For ODKTables.
 * @author sudar.sam@gmail.com
 *
 */
public class ServerDataServiceImpl extends RemoteServiceServlet implements
		ServerDataService {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5051558217315955180L;

	@Override
	public List<RowResourceClient> getRows(String tableId, String rowId, UriInfo info) 
			throws AccessDeniedException,
			RequestFailureException, DatastoreFailureException, PermissionDeniedException {
	    HttpServletRequest req = this.getThreadLocalRequest();
	    CallingContext cc = ContextFactory.getCallingContext(this, req);
	    try { // Must use try so that you can catch the ODK specific errors.
		    DataManager dm = new DataManager(tableId, cc);
		    AuthFilter af = new AuthFilter(tableId, cc);
		    af.checkPermission(TablePermission.READ_ROW);
		    List<Row> rows;
		    if (af.hasPermission(TablePermission.UNFILTERED_READ)) {
		      rows = dm.getRows();
		    } else {
		      List<Scope> scopes = AuthFilter.getScopes(cc);
		      rows = dm.getRows(scopes);
		    }
		    return getResources(rows, dm, info);
	    } catch (ODKDatastoreException e) {
			   e.printStackTrace();
			   throw new DatastoreFailureException(e);
	    }
	}
	
	public RowResourceClient getRow(String tableId, String rowId, UriInfo info) throws
			AccessDeniedException, RequestFailureException, DatastoreFailureException,
			PermissionDeniedException {
		try {
		    HttpServletRequest req = this.getThreadLocalRequest();
		    CallingContext cc = ContextFactory.getCallingContext(this, req);
		    DataManager dm = new DataManager(tableId, cc);
		    AuthFilter af = new AuthFilter(tableId, cc);
			af.checkPermission(TablePermission.READ_ROW);
			Row row = dm.getRowNullSafe(rowId);
			af.checkFilter(TablePermission.UNFILTERED_READ, row);
			RowResourceClient resource = getResource(row, dm, info);
			return resource;
		} catch (ODKDatastoreException e) {
			e.printStackTrace();
			throw new DatastoreFailureException(e);
		}		
	}

	@Override
	public RowResourceClient createOrUpdateRow(String tableId, String rowId, RowClient row, UriInfo info)
			throws AccessDeniedException, RequestFailureException,
			DatastoreFailureException, EtagMismatchException, PermissionDeniedException,
		      BadColumnNameException {
		try {
			// first transform row into a server-side row
			Row serverRow = row.transform();
		    HttpServletRequest req = this.getThreadLocalRequest();
		    CallingContext cc = ContextFactory.getCallingContext(this, req);
		    DataManager dm = new DataManager(tableId, cc);
		    AuthFilter af = new AuthFilter(tableId, cc);
		    af.checkPermission(TablePermission.WRITE_ROW);
		    row.setRowId(rowId);
		    Row dbRow = dm.getRow(rowId);
		    if (dbRow == null) {
		      serverRow = dm.insertRow(serverRow);
		    } else {
		      af.checkFilter(TablePermission.UNFILTERED_WRITE, dbRow);
		      serverRow = dm.updateRow(serverRow);
		    }
		    RowResourceClient resource = getResource(serverRow, dm, info);
		    return resource;
		} catch (ODKDatastoreException e) {
			e.printStackTrace();
			throw new DatastoreFailureException(e);
		} catch (ODKTaskLockException e) {
			e.printStackTrace();
			throw new DatastoreFailureException(e);
		}
	}

	@Override
	public void deleteRow(String tableId, String rowId) throws AccessDeniedException,
			RequestFailureException, DatastoreFailureException, PermissionDeniedException,
			ODKTaskLockException {
	    HttpServletRequest req = this.getThreadLocalRequest();
	    CallingContext cc = ContextFactory.getCallingContext(this, req);
	    try { // Must use try so that you can catch the ODK specific errors.
		    DataManager dm = new DataManager(tableId, cc);
		    AuthFilter af = new AuthFilter(tableId, cc);
	    
		    af.checkPermission(TablePermission.DELETE_ROW);
		    Row row = dm.getRowNullSafe(rowId);
		    af.checkFilter(TablePermission.UNFILTERED_DELETE, row);
		    dm.deleteRow(rowId);
	    } catch (ODKDatastoreException e) {
	    	e.printStackTrace();
	    	throw new DatastoreFailureException(e);
	    } 

	}
	
	private RowResourceClient getResource(Row row, DataManager dm, UriInfo info) {
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
		return resource.transform();
	}
	
	private List<RowResourceClient> getResources(List<Row> rows, DataManager dm, UriInfo info) {
		ArrayList<RowResourceClient> resources = new ArrayList<RowResourceClient>();
		for (Row row : rows) {
			resources.add(getResource(row, dm, info));
		}
		return resources;
	}	

}
