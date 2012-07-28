package org.opendatakit.aggregate.client.odktables;

import java.util.List;

import javax.ws.rs.core.UriInfo;

import org.opendatakit.aggregate.client.exception.RequestFailureException;
import org.opendatakit.aggregate.odktables.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.exception.TableAlreadyExistsException;
import org.opendatakit.common.persistence.client.exception.DatastoreFailureException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;
import org.opendatakit.common.security.client.exception.AccessDeniedException;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ServerTableServiceAsync {

	void getTables(AsyncCallback<List<TableEntryClient>> callback) 
			throws AccessDeniedException, RequestFailureException, DatastoreFailureException, 
			PermissionDeniedException;
	
	void getTable(String tableId, AsyncCallback<TableEntryClient> callback) 
			throws AccessDeniedException, RequestFailureException, DatastoreFailureException, 
			PermissionDeniedException;
	
	void createTable(String tableId, TableDefinitionClient definition,
			AsyncCallback<TableEntryClient> callback) 
					throws AccessDeniedException, RequestFailureException, DatastoreFailureException, 
					PermissionDeniedException, TableAlreadyExistsException;
	
	void deleteTable(String tableId, AsyncCallback<Void> callback) 
			throws AccessDeniedException, RequestFailureException, DatastoreFailureException, 
			PermissionDeniedException, ODKTaskLockException;
			
}
