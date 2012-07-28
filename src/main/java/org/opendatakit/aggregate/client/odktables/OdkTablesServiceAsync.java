package org.opendatakit.aggregate.client.odktables;

import java.util.List;

import org.opendatakit.aggregate.client.exception.RequestFailureException;
import org.opendatakit.aggregate.odktables.entity.TableEntry;
import org.opendatakit.common.persistence.client.exception.DatastoreFailureException;
import org.opendatakit.common.security.client.exception.AccessDeniedException;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * This is the interface to deal with the OdkTablesService. It has basic
 * functionality to interact with the ODK Tables data in the datastore.
 * <br>
 * More generally, there are two halves to each service with GWT, synchronous 
 * and asynchronous. The asynchronous are the ones that deal with the server, 
 * as the server executes and returns in an indeterminate amount of time. 
 * Both the synchronous and asynchronous interfaces must be present.
 * @author sudar.sam@gmail.com
 *
 */
public interface OdkTablesServiceAsync {

	/**
	 * Lists the ODK Tables tables in the datastore.
	 * @param callback
	 */
	void listTables(AsyncCallback<TableEntry[]> callback);
	
	
	void getRow(String rowId, AsyncCallback<RowResourceClient> callback) throws AccessDeniedException, RequestFailureException, 
	DatastoreFailureException;

	void createOrUpdateRow(String rowId, RowClient row, AsyncCallback<RowResourceClient> callback) 
			throws AccessDeniedException, RequestFailureException, DatastoreFailureException;
	
	void deleteRow(String rowId, AsyncCallback<Void> callback) 
			throws AccessDeniedException, RequestFailureException, DatastoreFailureException;
	
	
	// from DiffService.java:
	
	void getRowsSince(String dataEtag, AsyncCallback<List<RowResourceClient>> callback) 
			throws AccessDeniedException, RequestFailureException, DatastoreFailureException;
	
	
	// from PropertiesService:
	
	void getProperties(AsyncCallback<PropertiesResourceClient> callback) 
			throws AccessDeniedException, RequestFailureException, DatastoreFailureException;
	
	// maybe should also throw etag mismatch and ODKTaskLockException?
	void setProperties(TablePropertiesClient properties, AsyncCallback<PropertiesResourceClient> callback) 
			throws AccessDeniedException, RequestFailureException, DatastoreFailureException;
	
	
	// from TableAclService:
	
	void getAcls(AsyncCallback<List<TableAclResourceClient>> callback) 
			throws AccessDeniedException, RequestFailureException, DatastoreFailureException;
	
	void getUserAcls(AsyncCallback<List<TableAclResourceClient>> callback) 
			throws AccessDeniedException, RequestFailureException, DatastoreFailureException;
	
	void getGroupAcls(AsyncCallback<List<TableAclResourceClient>> callback) 
			throws AccessDeniedException, RequestFailureException, DatastoreFailureException;
	
	void getDefaultAcl(AsyncCallback<TableAclResourceClient> callback) 
			throws AccessDeniedException, RequestFailureException, DatastoreFailureException;
	
	void getUserAcl(String userId, AsyncCallback<TableAclResourceClient> callback) 
			throws AccessDeniedException, RequestFailureException, DatastoreFailureException;
	
	void getGroupAcl(String groupId, AsyncCallback<TableAclResourceClient> callback) 
			throws AccessDeniedException, RequestFailureException, DatastoreFailureException;
	
	void setDefaultAcl(TableAclClient acl, AsyncCallback<TableAclResourceClient> callback) 
			throws AccessDeniedException, RequestFailureException, DatastoreFailureException;
	
	void setUserAcl(String userId, TableAclClient acl, AsyncCallback<TableAclResourceClient> callback) 
			throws AccessDeniedException, RequestFailureException, DatastoreFailureException;
	
	void setGroupAcl(String groupId, TableAclClient acl, AsyncCallback<TableAclResourceClient> callback) 
			throws AccessDeniedException, RequestFailureException, DatastoreFailureException;
	
	void deleteDefaultAcl(AsyncCallback<Void> callback) 
			throws AccessDeniedException, RequestFailureException, DatastoreFailureException;
	
	void deleteUserAcl(String userId, AsyncCallback<Void> callback) 
			throws AccessDeniedException, RequestFailureException, DatastoreFailureException;
	
	void deleteGroupAcl(String groupId, AsyncCallback<Void> callback) 
			throws AccessDeniedException, RequestFailureException, DatastoreFailureException;
	
	
	// from TableService:
	
	void getTables(AsyncCallback<List<TableResourceClient>> callback) 
			throws AccessDeniedException, RequestFailureException, DatastoreFailureException;
	
	void getTable(String tableId, AsyncCallback<TableResourceClient> callback) 
			throws AccessDeniedException, RequestFailureException, DatastoreFailureException;
	
	void createTable(String tableId, TableDefinitionClient definition,
			AsyncCallback<TableResourceClient> callback) 
					throws AccessDeniedException, RequestFailureException, DatastoreFailureException;
	
	void deleteTable(String tableId, AsyncCallback<Void> callback) 
			throws AccessDeniedException, RequestFailureException, DatastoreFailureException;
			
}