package org.opendatakit.aggregate.client.odktables;

import java.util.List;

import javax.ws.rs.core.UriInfo;

import org.opendatakit.aggregate.client.exception.PermissionDeniedExceptionClient;
import org.opendatakit.aggregate.client.exception.RequestFailureException;
import org.opendatakit.common.persistence.client.exception.DatastoreFailureException;
import org.opendatakit.common.security.client.exception.AccessDeniedException;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * This will be the TablesACLService for the server. It will act the 
 * same way as org.opendatakit.aggregate.odktables.api.TablesACLService,
 * except that it will be for interacting with the table information
 * on the server, rather than with a phone.
 * 
 * @author sudar.sam
 */

@RemoteServiceRelativePath("servertableaclservice")
public interface ServerTableACLService extends RemoteService {

	
	List<TableAclClient> getAcls(String tableId) throws AccessDeniedException, 
		RequestFailureException, DatastoreFailureException, PermissionDeniedExceptionClient;
	
	List<TableAclClient> getUserAcls(String tableId) throws AccessDeniedException, 
		RequestFailureException, DatastoreFailureException, PermissionDeniedExceptionClient;
	
	List<TableAclClient> getGroupAcls(String tableId) throws AccessDeniedException, 
		RequestFailureException, DatastoreFailureException, PermissionDeniedExceptionClient;
	
	TableAclClient getDefaultAcl(String tableId) throws AccessDeniedException, 
		RequestFailureException, DatastoreFailureException, PermissionDeniedExceptionClient;
	
	TableAclClient getUserAcl(String userId, String tableId) throws 
		AccessDeniedException, RequestFailureException, DatastoreFailureException,
		PermissionDeniedExceptionClient;
	
	TableAclClient getGroupAcl(String groupId, String tableId) throws 
		AccessDeniedException, RequestFailureException, DatastoreFailureException,
		PermissionDeniedExceptionClient;
	
	TableAclClient setDefaultAcl(TableAclClient acl, String tableId) throws 
		AccessDeniedException, RequestFailureException, DatastoreFailureException,
		PermissionDeniedExceptionClient;
	
	TableAclClient setUserAcl(String userId, TableAclClient acl, String tableId) 
			throws AccessDeniedException, RequestFailureException, DatastoreFailureException,
			PermissionDeniedExceptionClient;
	
	TableAclClient setGroupAcl(String groupId, TableAclClient acl, String tableId) 
			throws AccessDeniedException, RequestFailureException, DatastoreFailureException,
			PermissionDeniedExceptionClient;
	
	void deleteDefaultAcl(String tableId) throws AccessDeniedException, 
		RequestFailureException, DatastoreFailureException, PermissionDeniedExceptionClient;
	
	void deleteUserAcl(String userId, String tableId) throws AccessDeniedException, 
		RequestFailureException, DatastoreFailureException, PermissionDeniedExceptionClient;
	
	void deleteGroupAcl(String groupId, String tableId) throws AccessDeniedException, 
		RequestFailureException, DatastoreFailureException, PermissionDeniedExceptionClient;
	
	
}
