package org.opendatakit.aggregate.client.odktables;

import java.util.List;

import javax.ws.rs.core.UriInfo;

import org.opendatakit.aggregate.client.exception.RequestFailureException;
import org.opendatakit.aggregate.odktables.exception.PermissionDeniedException;
import org.opendatakit.common.persistence.client.exception.DatastoreFailureException;
import org.opendatakit.common.security.client.exception.AccessDeniedException;

/**
 * This will be the TablesACLService for the server. It will act the 
 * same way as org.opendatakit.aggregate.odktables.api.TablesACLService,
 * except that it will be for interacting with the table information
 * on the server, rather than with a phone.
 * 
 * @author sudar.sam
 */
public interface ServerTableACLService {

	
	List<TableAclClient> getAcls(String tableId) throws AccessDeniedException, 
		RequestFailureException, DatastoreFailureException, PermissionDeniedException;
	
	List<TableAclClient> getUserAcls(String tableId) throws AccessDeniedException, 
		RequestFailureException, DatastoreFailureException, PermissionDeniedException;
	
	List<TableAclClient> getGroupAcls(String tableId) throws AccessDeniedException, 
		RequestFailureException, DatastoreFailureException, PermissionDeniedException;
	
	TableAclClient getDefaultAcl(String tableId) throws AccessDeniedException, 
		RequestFailureException, DatastoreFailureException, PermissionDeniedException;
	
	TableAclClient getUserAcl(String userId, String tableId) throws 
		AccessDeniedException, RequestFailureException, DatastoreFailureException,
		PermissionDeniedException;
	
	TableAclClient getGroupAcl(String groupId, String tableId) throws 
		AccessDeniedException, RequestFailureException, DatastoreFailureException,
		PermissionDeniedException;
	
	TableAclClient setDefaultAcl(TableAclClient acl, String tableId) throws 
		AccessDeniedException, RequestFailureException, DatastoreFailureException,
		PermissionDeniedException;
	
	TableAclClient setUserAcl(String userId, TableAclClient acl, String tableId) 
			throws AccessDeniedException, RequestFailureException, DatastoreFailureException,
			PermissionDeniedException;
	
	TableAclClient setGroupAcl(String groupId, TableAclClient acl, String tableId) 
			throws AccessDeniedException, RequestFailureException, DatastoreFailureException,
			PermissionDeniedException;
	
	void deleteDefaultAcl(String tableId) throws AccessDeniedException, 
		RequestFailureException, DatastoreFailureException, PermissionDeniedException;
	
	void deleteUserAcl(String userId, String tableId) throws AccessDeniedException, 
		RequestFailureException, DatastoreFailureException, PermissionDeniedException;
	
	void deleteGroupAcl(String groupId, String tableId) throws AccessDeniedException, 
		RequestFailureException, DatastoreFailureException, PermissionDeniedException;
	
	
}
