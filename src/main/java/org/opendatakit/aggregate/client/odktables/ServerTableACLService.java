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

	
	List<TableAclResourceClient> getAcls(String tableId, UriInfo info) throws AccessDeniedException, 
		RequestFailureException, DatastoreFailureException, PermissionDeniedException;
	
	List<TableAclResourceClient> getUserAcls(String tableId, UriInfo info) throws AccessDeniedException, 
		RequestFailureException, DatastoreFailureException, PermissionDeniedException;
	
	List<TableAclResourceClient> getGroupAcls(String tableId, UriInfo info) throws AccessDeniedException, 
		RequestFailureException, DatastoreFailureException, PermissionDeniedException;
	
	TableAclResourceClient getDefaultAcl(String tableId, UriInfo info) throws AccessDeniedException, 
		RequestFailureException, DatastoreFailureException, PermissionDeniedException;
	
	TableAclResourceClient getUserAcl(String userId, String tableId, UriInfo info) throws 
		AccessDeniedException, RequestFailureException, DatastoreFailureException,
		PermissionDeniedException;
	
	TableAclResourceClient getGroupAcl(String groupId, String tableId, UriInfo info) throws 
		AccessDeniedException, RequestFailureException, DatastoreFailureException,
		PermissionDeniedException;
	
	TableAclResourceClient setDefaultAcl(TableAclClient acl, String tableId, UriInfo info) throws 
		AccessDeniedException, RequestFailureException, DatastoreFailureException,
		PermissionDeniedException;
	
	TableAclResourceClient setUserAcl(String userId, TableAclClient acl, String tableId, UriInfo info) 
			throws AccessDeniedException, RequestFailureException, DatastoreFailureException,
			PermissionDeniedException;
	
	TableAclResourceClient setGroupAcl(String groupId, TableAclClient acl, String tableId, UriInfo info) 
			throws AccessDeniedException, RequestFailureException, DatastoreFailureException,
			PermissionDeniedException;
	
	void deleteDefaultAcl(String tableId, UriInfo info) throws AccessDeniedException, 
		RequestFailureException, DatastoreFailureException, PermissionDeniedException;
	
	void deleteUserAcl(String userId, String tableId, UriInfo info) throws AccessDeniedException, 
		RequestFailureException, DatastoreFailureException, PermissionDeniedException;
	
	void deleteGroupAcl(String groupId, String tableId, UriInfo info) throws AccessDeniedException, 
		RequestFailureException, DatastoreFailureException, PermissionDeniedException;
	
	
}
