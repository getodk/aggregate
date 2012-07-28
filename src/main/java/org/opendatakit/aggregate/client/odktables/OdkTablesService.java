package org.opendatakit.aggregate.client.odktables;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import org.opendatakit.aggregate.client.exception.RequestFailureException;
import org.opendatakit.aggregate.odktables.entity.TableEntry;
import org.opendatakit.common.persistence.client.exception.DatastoreFailureException;
import org.opendatakit.common.security.client.exception.AccessDeniedException;

/**
 * This is the interface to deal with the OdkTablesService. It has basic
 * functionality to interact with the ODK Tables data in the datastore.
 * <br>
 * More generally, there are two halves to each service with GWT, synchronous 
 * and asynchronous. The asynchronous are the ones that deal with the server, 
 * as the server executes and returns in an indeterminate amount of time. 
 * Both the synchronous and asynchronous interfaces must be present.
 * @author sudar.sam@gmail.com
 * @author sudar.sam@gmail.com
 *
 */
// not sure what this remote service thing is...
@RemoteServiceRelativePath("odktables")
public interface OdkTablesService extends RemoteService {

	TableEntry [] listTables() throws AccessDeniedException, RequestFailureException, DatastoreFailureException;
	
	// Many of the methods I want on the server side have already been implemented on the client side
	// in order to present them on the phones. So, I am going to be adding these methods to the
	// services possible on the client, and then implementing them and adding additional classes as necessary.
	
	// The following are based on methods from org.opendatakit.aggregate.odktables.api
	// It looks like I might need a tasklock and an etag exception.
	
	// from DataService.java:
	
	// RowResourceClient should be a similar thing to org.opendatakit.aggregate.odktables.entity.api.RowResource. 
	// Calling it client to distinguish.
	RowResourceClient getRow(String rowId) throws AccessDeniedException, RequestFailureException, 
		DatastoreFailureException;
	
	RowResourceClient createOrUpdateRow(String rowId, RowClient row) throws AccessDeniedException, RequestFailureException, 
		DatastoreFailureException;
	
	void deleteRow(String rowId) throws AccessDeniedException, RequestFailureException, DatastoreFailureException;
	
	
	// from DiffService.java:
	
	List<RowResourceClient> getRowsSince(String dataEtag) throws AccessDeniedException, RequestFailureException, 
		DatastoreFailureException;
	
	
	// from PropertiesService:
	
	PropertiesResourceClient getProperties() throws AccessDeniedException, RequestFailureException, 
		DatastoreFailureException;
	
	// maybe should also throw etag mismatch and ODKTaskLockException?
	PropertiesResourceClient setProperties(TablePropertiesClient properties) throws AccessDeniedException, 
		RequestFailureException, DatastoreFailureException;
	
	
	// from TableAclService:
	
	List<TableAclResourceClient> getAcls() throws AccessDeniedException, RequestFailureException, DatastoreFailureException;
	
	List<TableAclResourceClient> getUserAcls() throws AccessDeniedException, RequestFailureException, DatastoreFailureException;
	
	List<TableAclResourceClient> getGroupAcls() throws AccessDeniedException, RequestFailureException, DatastoreFailureException;
	
	TableAclResourceClient getDefaultAcl() throws AccessDeniedException, RequestFailureException, DatastoreFailureException;
	
	TableAclResourceClient getUserAcl(String userId) throws AccessDeniedException, RequestFailureException, DatastoreFailureException;
	
	TableAclResourceClient getGroupAcl(String groupId) throws AccessDeniedException, RequestFailureException, DatastoreFailureException;
	
	TableAclResourceClient setDefaultAcl(TableAclClient acl) throws AccessDeniedException, RequestFailureException, DatastoreFailureException;
	
	TableAclResourceClient setUserAcl(String userId, TableAclClient acl) throws AccessDeniedException, RequestFailureException, DatastoreFailureException;
	
	TableAclResourceClient setGroupAcl(String groupId, TableAclClient acl) throws AccessDeniedException, RequestFailureException, DatastoreFailureException;
	
	void deleteDefaultAcl() throws AccessDeniedException, RequestFailureException, DatastoreFailureException;
	
	void deleteUserAcl(String userId) throws AccessDeniedException, RequestFailureException, DatastoreFailureException;
	
	void deleteGroupAcl(String groupId) throws AccessDeniedException, RequestFailureException, DatastoreFailureException;
	
	
	// from TableService:
	
	List<TableResourceClient> getTables() throws AccessDeniedException, RequestFailureException, DatastoreFailureException;
	
	TableResourceClient getTable(String tableId) throws AccessDeniedException, RequestFailureException, DatastoreFailureException;
	
	TableResourceClient createTable(String tableId, TableDefinitionClient definition) throws AccessDeniedException, RequestFailureException, DatastoreFailureException;
	
	void deleteTable(String tableId) throws AccessDeniedException, RequestFailureException, DatastoreFailureException;
	
	/* These I think are basically already implemented above...
	ColumnServiceClient getColumns(String tableId) throws AccessDeniedException, RequestFailureException, DatastoreFailureException;
	
	DataServiceClient getData(String tableId) throws AccessDeniedException, RequestFailureException, DatastoreFailureException;
	
	PropertiesServiceClient getProperties(String tableId) throws AccessDeniedException, RequestFailureException, DatastoreFailureException;
	
	DiffServiceClient getDiff(String tabledId) throws AccessDeniedException, RequestFailureException, DatastoreFailureException;
	
	TableAclServiceClient getAcl(String tableId) throws AccessDeniedException, RequestFailureException, DatastoreFailureException;
	*/
	
	
	
}