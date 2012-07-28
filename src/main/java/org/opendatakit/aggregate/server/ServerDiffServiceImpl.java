package org.opendatakit.aggregate.server;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.client.exception.RequestFailureException;
import org.opendatakit.aggregate.client.odktables.RowResourceClient;
import org.opendatakit.aggregate.client.odktables.ServerDiffService;
import org.opendatakit.aggregate.odktables.AuthFilter;
import org.opendatakit.aggregate.odktables.DataManager;
import org.opendatakit.aggregate.odktables.api.DataService;
import org.opendatakit.aggregate.odktables.api.TableService;
import org.opendatakit.aggregate.odktables.entity.Row;
import org.opendatakit.aggregate.odktables.entity.Scope;
import org.opendatakit.aggregate.odktables.entity.TableRole.TablePermission;
import org.opendatakit.aggregate.odktables.entity.api.RowResource;
import org.opendatakit.aggregate.odktables.exception.PermissionDeniedException;
import org.opendatakit.common.persistence.client.exception.DatastoreFailureException;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.security.client.exception.AccessDeniedException;
import org.opendatakit.common.web.CallingContext;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class ServerDiffServiceImpl extends RemoteServiceServlet implements
		ServerDiffService {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5472352346806984818L;

	@Override
	public List<RowResourceClient> getRowsSince(String dataEtag, String tableId, UriInfo info)
			throws AccessDeniedException, RequestFailureException,
			DatastoreFailureException, PermissionDeniedException {
	    HttpServletRequest req = this.getThreadLocalRequest();
	    CallingContext cc = ContextFactory.getCallingContext(this, req);
	    try {
		    DataManager dm = new DataManager(tableId, cc);
		    AuthFilter af = new AuthFilter(tableId, cc);
		    af.checkPermission(TablePermission.READ_ROW);	    
		    List<Row> rows;
		    if (af.hasPermission(TablePermission.UNFILTERED_READ)) {
		      rows = dm.getRowsSince(dataEtag);
		    } else {
		      List<Scope> scopes = AuthFilter.getScopes(cc);
		      rows = dm.getRowsSince(dataEtag, scopes);
		    }
		    return getResources(rows, dm, info);
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
