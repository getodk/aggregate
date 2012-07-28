package org.opendatakit.aggregate.client.odktables;

import java.util.List;

import javax.ws.rs.core.UriInfo;

import org.opendatakit.aggregate.client.exception.RequestFailureException;
import org.opendatakit.aggregate.odktables.exception.PermissionDeniedException;
import org.opendatakit.common.persistence.client.exception.DatastoreFailureException;
import org.opendatakit.common.security.client.exception.AccessDeniedException;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ServerDiffServiceAsync {

	void getRowsSince(String dataEtag, String tableId, UriInfo info, AsyncCallback<List<RowResourceClient>> callback) 
			throws AccessDeniedException, RequestFailureException, DatastoreFailureException,
			PermissionDeniedException;
	
	
}
