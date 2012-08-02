package org.opendatakit.aggregate.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.client.exception.BadColumnNameExceptionClient;
import org.opendatakit.aggregate.client.exception.EtagMismatchExceptionClient;
import org.opendatakit.aggregate.client.exception.PermissionDeniedExceptionClient;
import org.opendatakit.aggregate.client.exception.RequestFailureException;
import org.opendatakit.aggregate.client.odktables.RowClient;
import org.opendatakit.aggregate.client.odktables.ServerDataService;
import org.opendatakit.aggregate.odktables.AuthFilter;
import org.opendatakit.aggregate.odktables.DataManager;
import org.opendatakit.aggregate.odktables.api.DataService;
import org.opendatakit.aggregate.odktables.api.TableService;
import org.opendatakit.aggregate.odktables.entity.Row;
import org.opendatakit.aggregate.odktables.entity.Scope;
import org.opendatakit.aggregate.odktables.entity.TableRole.TablePermission;
import org.opendatakit.aggregate.odktables.entity.UtilTransforms;
import org.opendatakit.aggregate.odktables.exception.BadColumnNameException;
import org.opendatakit.aggregate.odktables.exception.EtagMismatchException;
import org.opendatakit.aggregate.odktables.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.relation.DbTable;
import org.opendatakit.aggregate.odktables.relation.DbColumn;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.client.exception.DatastoreFailureException;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;
import org.opendatakit.common.security.client.exception.AccessDeniedException;
import org.opendatakit.common.web.CallingContext;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * For ODKTables.
 * @author sudar.sam@gmail.com
 *
 */
public class ServerDataServiceImpl extends RemoteServiceServlet implements
		ServerDataService {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5051558217315955180L;

	@Override
	public List<RowClient> getRows(String tableId) 
			throws AccessDeniedException,
			RequestFailureException, DatastoreFailureException, PermissionDeniedExceptionClient {
	    HttpServletRequest req = this.getThreadLocalRequest();
	    CallingContext cc = ContextFactory.getCallingContext(this, req);
	    try { // Must use try so that you can catch the ODK specific errors.
		    DataManager dm = new DataManager(tableId, cc);
		    AuthFilter af = new AuthFilter(tableId, cc);
		    af.checkPermission(TablePermission.READ_ROW);
		    List<Row> rows;
		    if (af.hasPermission(TablePermission.UNFILTERED_READ)) {
		      rows = dm.getRows();
		    } else {
		      List<Scope> scopes = AuthFilter.getScopes(cc);
		      rows = dm.getRows(scopes);
		    	//rows = dm.getRows();
		    }
		    return transformRows(rows);
	    } catch (ODKDatastoreException e) {
			   e.printStackTrace();
			   throw new DatastoreFailureException(e);
	    } catch (PermissionDeniedException e) {
	    	e.printStackTrace();
	    	throw new PermissionDeniedExceptionClient(e);
	    }
	}
	
	@Override
	public RowClient getRow(String tableId, String rowId) throws
			AccessDeniedException, RequestFailureException, DatastoreFailureException,
			PermissionDeniedExceptionClient {
		try {
		    HttpServletRequest req = this.getThreadLocalRequest();
		    CallingContext cc = ContextFactory.getCallingContext(this, req);
		    DataManager dm = new DataManager(tableId, cc);
		    AuthFilter af = new AuthFilter(tableId, cc);
			af.checkPermission(TablePermission.READ_ROW);
			Row row = dm.getRowNullSafe(rowId);
			af.checkFilter(TablePermission.UNFILTERED_READ, row);
			return row.transform();
		} catch (ODKDatastoreException e) {
			e.printStackTrace();
			throw new DatastoreFailureException(e);
		} catch (PermissionDeniedException e) {
	    	e.printStackTrace();
	    	throw new PermissionDeniedExceptionClient(e);
	    }	
	}

	@Override
	public RowClient createOrUpdateRow(String tableId, String rowId, RowClient row)
			throws AccessDeniedException, RequestFailureException,
			DatastoreFailureException, EtagMismatchExceptionClient, PermissionDeniedExceptionClient,
		      BadColumnNameExceptionClient {
		try {
			// first transform row into a server-side row
			Row serverRow = UtilTransforms.transform(row);
		    HttpServletRequest req = this.getThreadLocalRequest();
		    CallingContext cc = ContextFactory.getCallingContext(this, req);
		    DataManager dm = new DataManager(tableId, cc);
		    AuthFilter af = new AuthFilter(tableId, cc);
		    af.checkPermission(TablePermission.WRITE_ROW);
		    row.setRowId(rowId);
		    Row dbRow = dm.getRow(rowId);
		    if (dbRow == null) {
		      serverRow = dm.insertRow(serverRow);
		    } else {
		      af.checkFilter(TablePermission.UNFILTERED_WRITE, dbRow);
		      serverRow = dm.updateRow(serverRow);
		    }
		    return serverRow.transform();
		} catch (ODKDatastoreException e) {
			e.printStackTrace();
			throw new DatastoreFailureException(e);
		} catch (ODKTaskLockException e) {
			e.printStackTrace();
			throw new DatastoreFailureException(e);
		} catch (PermissionDeniedException e) {
	    	e.printStackTrace();
	    	throw new PermissionDeniedExceptionClient(e);
	    } catch (BadColumnNameException e) {
	    	e.printStackTrace();
	    	throw new BadColumnNameExceptionClient(e);
	    } catch (EtagMismatchException e) {
	    	e.printStackTrace();
	    	throw new EtagMismatchExceptionClient(e);
	    }
	}

	@Override
	public void deleteRow(String tableId, String rowId) throws AccessDeniedException,
			RequestFailureException, DatastoreFailureException, PermissionDeniedExceptionClient {
	    HttpServletRequest req = this.getThreadLocalRequest();
	    CallingContext cc = ContextFactory.getCallingContext(this, req);
	    try { // Must use try so that you can catch the ODK specific errors.
		    DataManager dm = new DataManager(tableId, cc);
		    AuthFilter af = new AuthFilter(tableId, cc);
	    
		    af.checkPermission(TablePermission.DELETE_ROW);
		    Row row = dm.getRowNullSafe(rowId);
		    af.checkFilter(TablePermission.UNFILTERED_DELETE, row);
		    dm.deleteRow(rowId);
	    } catch (ODKDatastoreException e) {
	    	e.printStackTrace();
	    	throw new DatastoreFailureException(e);
	    } catch (PermissionDeniedException e) {
	    	e.printStackTrace();
	    	throw new PermissionDeniedExceptionClient(e);
	    } catch (ODKTaskLockException e) {
	    	e.printStackTrace();
	    	throw new RequestFailureException(e);
	    }

	}
	
	/**
	 * Gets the user-defined/pertinent column names. 
	 * @return List<String> of the column names
	 */
	@Override
	public List<String> getColumnNames(String tableId) throws DatastoreFailureException {
	    
		HttpServletRequest req = this.getThreadLocalRequest();
	    CallingContext cc = ContextFactory.getCallingContext(this, req);
	    try {
			List<String> columnNames = DbColumn.queryForColumnNames(tableId, cc);
			return columnNames;
		} catch (ODKDatastoreException e) {
			e.printStackTrace();
			throw new DatastoreFailureException(e);
		}
	}
	
	/* don't think i need this, but i'm keeping it so that i can use the 
	 * code for a reference later if i need to. 
	 *
	/**
	 * Gets ALL the column names, keeping the user-defined ones pretty.
	 * @param rows
	 * @return
	 */
	/*
	 * Uses the other getColumnNames, which gets the pretty ones. Gets all
	 * the names, removes the "TABLE_UUID_.*" ones, and then adds them back
	 * in after being translated to the pretty ones.
	 
	@Override
	public List<String> getAllColumnNames(String tableId) throws DatastoreFailureException {
	    HttpServletRequest req = this.getThreadLocalRequest();
	    CallingContext cc = ContextFactory.getCallingContext(this, req);
	    try { // Must use try so that you can catch the ODK specific errors.
		    DataManager dm = new DataManager(tableId, cc);
		    AuthFilter af = new AuthFilter(tableId, cc);
		    Set<DataField> dataFields = DbTable.getRelation(tableId, cc).getDataFields();
		    
		    // TODO: look into seeing if you need to have a scope on column names.
		    // TODO: see if there is a better way to remove the pretty ones, or make
		    // sure that you can't name a column TABLE_UUID_
		    List<String> columnNames = getColumnNames(tableId);
		    for (DataField df : dataFields) {
		    	// don't add if it starts with this, as you'll add it later
		    	if (!df.getName().substring(0, 11).equals("TABLE_UUID_")) {
			    	columnNames.add(df.getName());		
		    	}
		    }
		    
		    return columnNames;
	    } catch (ODKDatastoreException e) {
	    	e.printStackTrace();
	    	throw new DatastoreFailureException(e);
	    }	
	}*/
	
	private List<RowClient> transformRows(List<Row> rows) {
		List<RowClient> clientRows = new ArrayList<RowClient>();
		for (Row row : rows) {
			clientRows.add(row.transform());
		}
		return clientRows;
	}
	

}
