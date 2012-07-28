package org.opendatakit.aggregate.server;

import java.util.List;

import org.opendatakit.aggregate.client.exception.RequestFailureException;
import org.opendatakit.aggregate.client.odktables.OdkTablesService;
import org.opendatakit.aggregate.client.odktables.PropertiesResourceClient;
import org.opendatakit.aggregate.client.odktables.RowClient;
import org.opendatakit.aggregate.client.odktables.RowResourceClient;
import org.opendatakit.aggregate.client.odktables.TableAclClient;
import org.opendatakit.aggregate.client.odktables.TableAclResourceClient;
import org.opendatakit.aggregate.client.odktables.TableDefinitionClient;
import org.opendatakit.aggregate.client.odktables.TablePropertiesClient;
import org.opendatakit.aggregate.client.odktables.TableResourceClient;
import org.opendatakit.aggregate.odktables.entity.TableEntry;
import org.opendatakit.common.persistence.client.exception.DatastoreFailureException;
import org.opendatakit.common.security.client.exception.AccessDeniedException;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * This is the implementation of the ODKTablesService interfaces.
 * <br>
 * The idea is that this class houses the actual muscle for the 
 * methods described in the interfaces. 
 * <br>
 * The actual implementations are planning on relying on the methods
 * in Dylan's Impl classes.
 * 
 * @author sudar.sam@gmail.com
 *
 */
public class ODKTablesServiceImpl extends RemoteServiceServlet implements
		OdkTablesService {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7940701091830161022L;

	@Override
	public TableEntry[] listTables() throws AccessDeniedException,
			RequestFailureException, DatastoreFailureException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RowResourceClient getRow(String rowId) throws AccessDeniedException,
			RequestFailureException, DatastoreFailureException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RowResourceClient createOrUpdateRow(String rowId, RowClient row)
			throws AccessDeniedException, RequestFailureException,
			DatastoreFailureException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteRow(String rowId) throws AccessDeniedException,
			RequestFailureException, DatastoreFailureException {
		// TODO Auto-generated method stub

	}

	@Override
	public List<RowResourceClient> getRowsSince(String dataEtag)
			throws AccessDeniedException, RequestFailureException,
			DatastoreFailureException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PropertiesResourceClient getProperties()
			throws AccessDeniedException, RequestFailureException,
			DatastoreFailureException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PropertiesResourceClient setProperties(
			TablePropertiesClient properties) throws AccessDeniedException,
			RequestFailureException, DatastoreFailureException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<TableAclResourceClient> getAcls() throws AccessDeniedException,
			RequestFailureException, DatastoreFailureException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<TableAclResourceClient> getUserAcls()
			throws AccessDeniedException, RequestFailureException,
			DatastoreFailureException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<TableAclResourceClient> getGroupAcls()
			throws AccessDeniedException, RequestFailureException,
			DatastoreFailureException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TableAclResourceClient getDefaultAcl() throws AccessDeniedException,
			RequestFailureException, DatastoreFailureException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TableAclResourceClient getUserAcl(String userId)
			throws AccessDeniedException, RequestFailureException,
			DatastoreFailureException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TableAclResourceClient getGroupAcl(String groupId)
			throws AccessDeniedException, RequestFailureException,
			DatastoreFailureException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TableAclResourceClient setDefaultAcl(TableAclClient acl)
			throws AccessDeniedException, RequestFailureException,
			DatastoreFailureException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TableAclResourceClient setUserAcl(String userId, TableAclClient acl)
			throws AccessDeniedException, RequestFailureException,
			DatastoreFailureException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TableAclResourceClient setGroupAcl(String groupId, TableAclClient acl)
			throws AccessDeniedException, RequestFailureException,
			DatastoreFailureException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteDefaultAcl() throws AccessDeniedException,
			RequestFailureException, DatastoreFailureException {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteUserAcl(String userId) throws AccessDeniedException,
			RequestFailureException, DatastoreFailureException {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteGroupAcl(String groupId) throws AccessDeniedException,
			RequestFailureException, DatastoreFailureException {
		// TODO Auto-generated method stub

	}

	@Override
	public List<TableResourceClient> getTables() throws AccessDeniedException,
			RequestFailureException, DatastoreFailureException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TableResourceClient getTable(String tableId)
			throws AccessDeniedException, RequestFailureException,
			DatastoreFailureException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TableResourceClient createTable(String tableId,
			TableDefinitionClient definition) throws AccessDeniedException,
			RequestFailureException, DatastoreFailureException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteTable(String tableId) throws AccessDeniedException,
			RequestFailureException, DatastoreFailureException {
		// TODO Auto-generated method stub

	}

}
