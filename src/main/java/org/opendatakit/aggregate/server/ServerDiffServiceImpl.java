package org.opendatakit.aggregate.server;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.client.exception.PermissionDeniedExceptionClient;
import org.opendatakit.aggregate.client.exception.RequestFailureException;
import org.opendatakit.aggregate.client.odktables.RowClient;
import org.opendatakit.aggregate.client.odktables.ServerDiffService;
import org.opendatakit.aggregate.odktables.AuthFilter;
import org.opendatakit.aggregate.odktables.DataManager;
import org.opendatakit.aggregate.odktables.entity.Row;
import org.opendatakit.aggregate.odktables.entity.Scope;
import org.opendatakit.aggregate.odktables.entity.TableRole.TablePermission;
import org.opendatakit.aggregate.odktables.entity.UtilTransforms;
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
	public List<RowClient> getRowsSince(String dataEtag, String tableId)
			throws AccessDeniedException, RequestFailureException,
			DatastoreFailureException, PermissionDeniedExceptionClient {
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
		    return transformRows(rows);
	    } catch (ODKDatastoreException e) {
	    	e.printStackTrace();
	    	throw new DatastoreFailureException(e);
	    } catch (PermissionDeniedException e) {
	    	e.printStackTrace();
	    	throw new PermissionDeniedExceptionClient(e);
	    }
	}

	// very basic transformation method
	private List<RowClient> transformRows(List<Row> rows) {
		List<RowClient> clientRows = new ArrayList<RowClient>();
		for (Row row : rows) {
			clientRows.add(UtilTransforms.transform(row));
		}
		return clientRows;
	}

}
