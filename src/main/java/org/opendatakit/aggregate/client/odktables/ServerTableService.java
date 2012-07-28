package org.opendatakit.aggregate.client.odktables;

import java.util.List;

import javax.ws.rs.core.UriInfo;

import org.opendatakit.aggregate.client.exception.RequestFailureException;
import org.opendatakit.aggregate.odktables.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.exception.TableAlreadyExistsException;
import org.opendatakit.common.persistence.client.exception.DatastoreFailureException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;
import org.opendatakit.common.security.client.exception.AccessDeniedException;

/**
 * This will be the TableService for the server. It will act the 
 * same way as org.opendatakit.aggregate.odktables.api.TableService,
 * except that it will be for interacting with the table information
 * on the server, rather than with a phone.
 * 
 * @author sudar.sam
 */
public interface ServerTableService {

	
	
	List<TableResourceClient> getTables(UriInfo info) throws AccessDeniedException, 
		RequestFailureException, DatastoreFailureException, PermissionDeniedException;
	
	TableResourceClient getTable(String tableId, UriInfo info) throws AccessDeniedException, 
		RequestFailureException, DatastoreFailureException, PermissionDeniedException;
	
	TableResourceClient createTable(String tableId, TableDefinitionClient definition,
			UriInfo info) throws 
		AccessDeniedException, RequestFailureException, DatastoreFailureException, 
		PermissionDeniedException, TableAlreadyExistsException;
	
	void deleteTable(String tableId, UriInfo info) throws AccessDeniedException, RequestFailureException, 
		DatastoreFailureException, PermissionDeniedException, ODKTaskLockException;
	
	// Not sure if I still need these methods, which are present in Dylan's methods,
	// but perhaps I don't need them because I can just create the other services directly?
	// Or "service" means something different in Dylan's case than in GWT speak?
	/* These I think are basically already implemented above...
	ColumnServiceClient getColumns(String tableId) throws AccessDeniedException, RequestFailureException, DatastoreFailureException;
	
	DataServiceClient getData(String tableId) throws AccessDeniedException, RequestFailureException, DatastoreFailureException;
	
	PropertiesServiceClient getProperties(String tableId) throws AccessDeniedException, RequestFailureException, DatastoreFailureException;
	
	DiffServiceClient getDiff(String tabledId) throws AccessDeniedException, RequestFailureException, DatastoreFailureException;
	
	TableAclServiceClient getAcl(String tableId) throws AccessDeniedException, RequestFailureException, DatastoreFailureException;
	*/
	
}
