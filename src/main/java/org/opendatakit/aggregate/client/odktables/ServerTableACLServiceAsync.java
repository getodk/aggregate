package org.opendatakit.aggregate.client.odktables;

import java.util.List;

import javax.ws.rs.core.UriInfo;

import org.opendatakit.aggregate.client.exception.RequestFailureException;
import org.opendatakit.aggregate.odktables.exception.PermissionDeniedException;
import org.opendatakit.common.persistence.client.exception.DatastoreFailureException;
import org.opendatakit.common.security.client.exception.AccessDeniedException;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ServerTableACLServiceAsync {

	void getAcls(String tableId, UriInfo info, AsyncCallback<List<TableAclResourceClient>> callback) 
			throws AccessDeniedException, RequestFailureException, DatastoreFailureException,
			PermissionDeniedException;
	
	void getUserAcls(String tableId, UriInfo info, AsyncCallback<List<TableAclResourceClient>> callback) 
			throws AccessDeniedException, RequestFailureException, DatastoreFailureException,
			PermissionDeniedException;
	
	void getGroupAcls(String tableId, UriInfo info, AsyncCallback<List<TableAclResourceClient>> callback) 
			throws AccessDeniedException, RequestFailureException, DatastoreFailureException,
			PermissionDeniedException;
	
	void getDefaultAcl(String tableId, UriInfo info, AsyncCallback<TableAclResourceClient> callback) 
			throws AccessDeniedException, RequestFailureException, DatastoreFailureException,
			PermissionDeniedException;
	
	void getUserAcl(String userId, String tableId, UriInfo info, AsyncCallback<TableAclResourceClient> callback) 
			throws AccessDeniedException, RequestFailureException, DatastoreFailureException,
			PermissionDeniedException;
	
	void getGroupAcl(String groupId, String tableId, UriInfo info, AsyncCallback<TableAclResourceClient> callback) 
			throws AccessDeniedException, RequestFailureException, DatastoreFailureException,
			PermissionDeniedException;
	
	void setDefaultAcl(TableAclClient acl, String tableId, UriInfo info, AsyncCallback<TableAclResourceClient> callback) 
			throws AccessDeniedException, RequestFailureException, DatastoreFailureException,
			PermissionDeniedException;
	
	void setUserAcl(String userId, TableAclClient acl, 
			String tableId, UriInfo info, AsyncCallback<TableAclResourceClient> callback) 
			throws AccessDeniedException, RequestFailureException, DatastoreFailureException,
			PermissionDeniedException;
	
	void setGroupAcl(String groupId, TableAclClient acl, 
			String tableId, UriInfo info, AsyncCallback<TableAclResourceClient> callback) 
			throws AccessDeniedException, RequestFailureException, DatastoreFailureException,
			PermissionDeniedException;
	
	void deleteDefaultAcl(String tableId, UriInfo info, AsyncCallback<Void> callback) 
			throws AccessDeniedException, RequestFailureException, DatastoreFailureException,
			PermissionDeniedException;
	
	void deleteUserAcl(String userId, String tableId, UriInfo info, AsyncCallback<Void> callback) 
			throws AccessDeniedException, RequestFailureException, DatastoreFailureException,
			PermissionDeniedException;
	
	void deleteGroupAcl(String groupId, String tableId, UriInfo info, AsyncCallback<Void> callback) 
			throws AccessDeniedException, RequestFailureException, DatastoreFailureException,
			PermissionDeniedException;
	
	
}
