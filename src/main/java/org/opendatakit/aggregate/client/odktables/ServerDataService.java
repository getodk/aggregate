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

/**
 * This will be the DataService for the server. It will act the 
 * same way as org.opendatakit.aggregate.odktables.api.DataService,
 * except that it will be for interacting with the table information
 * on the server, rather than with a phone.
 * 
 * @author sudar.sam@gmail.com
 *
 */
public interface ServerDataService {

	List<RowResourceClient> getRows(String tableId, String rowId, UriInfo info) throws AccessDeniedException, RequestFailureException, 
	DatastoreFailureException, PermissionDeniedException;
	
	// RowResourceClient should be a similar thing to org.opendatakit.aggregate.odktables.entity.api.RowResource. 
	// Calling it client to distinguish.
	RowResourceClient getRow(String tableId, String rowId, UriInfo info) throws AccessDeniedException, RequestFailureException, 
		DatastoreFailureException, PermissionDeniedException;
	
	RowResourceClient createOrUpdateRow(String tableId, String rowId, RowClient row, UriInfo info) throws AccessDeniedException, RequestFailureException, 
		DatastoreFailureException, PermissionDeniedException, 
		EtagMismatchException, BadColumnNameException;
	
	void deleteRow(String tableId, String rowId) throws AccessDeniedException, 
		RequestFailureException, DatastoreFailureException, PermissionDeniedException,
		ODKTaskLockException;
	
		
	
}
