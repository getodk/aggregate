package org.opendatakit.aggregate.client.odktables;

import java.util.List;

import javax.ws.rs.core.UriInfo;

import org.opendatakit.aggregate.client.exception.RequestFailureException;
import org.opendatakit.aggregate.odktables.exception.BadColumnNameException;
import org.opendatakit.aggregate.odktables.exception.EtagMismatchException;
import org.opendatakit.aggregate.odktables.exception.PermissionDeniedException;
import org.opendatakit.common.persistence.client.exception.DatastoreFailureException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;
import org.opendatakit.common.security.client.exception.AccessDeniedException;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The async server side.
 * @author sudar.sam@gmail.com
 *
 */
public interface ServerDataServiceAsync {

	
	void getRows(String tableId, String rowId, UriInfo info, 
			AsyncCallback<List<RowResourceClient>> callback) throws AccessDeniedException, RequestFailureException, 
	DatastoreFailureException, PermissionDeniedException;
	
	void getRow(String tableId, String rowId, UriInfo info, AsyncCallback<RowResourceClient> callback) throws AccessDeniedException, RequestFailureException, 
	DatastoreFailureException, PermissionDeniedException;

	void createOrUpdateRow(String tableId, String rowId, RowClient row, UriInfo info, AsyncCallback<RowResourceClient> callback) 
			throws AccessDeniedException, RequestFailureException, 
			DatastoreFailureException, PermissionDeniedException, 
			EtagMismatchException, BadColumnNameException;
	
	void deleteRow(String tableId, String rowId, AsyncCallback<Void> callback) throws AccessDeniedException, 
		RequestFailureException, DatastoreFailureException, PermissionDeniedException, ODKTaskLockException;
	
}
