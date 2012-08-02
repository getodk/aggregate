package org.opendatakit.aggregate.odktables.entity;

import org.opendatakit.aggregate.client.odktables.ColumnClient;
import org.opendatakit.aggregate.client.odktables.RowClient;
import org.opendatakit.aggregate.client.odktables.ScopeClient;
import org.opendatakit.aggregate.client.odktables.TableAclClient;

/**
 * Various methods for transforming objects from client to server code.
 * 
 * @author sudar.sam@gmail.com
 *
 */
public class UtilTransforms {

	
	  /**
	   * Transform the object into a server-side Column object.
	   */
	  public static Column transform(ColumnClient client) {
		  // have to fix this to get the appropriate type out of the enum.
		  Column column;
		  switch (client.getType()) {
		  case BOOLEAN:
			  column = new Column(client.getName(), Column.ColumnType.BOOLEAN);
			  break;
		  case STRING:
			  column = new Column(client.getName(), Column.ColumnType.STRING);
			  break;
		  case INTEGER:
			  column = new Column(client.getName(), Column.ColumnType.INTEGER);
			  break;
		  case DECIMAL:
			  column = new Column(client.getName(), Column.ColumnType.DECIMAL);
			  break;
		  case DATETIME:
			  column = new Column(client.getName(), Column.ColumnType.DATETIME);
			  break;
		  default:
			  throw new IllegalStateException("cannot transform ColumnClient to Column, no type match.");
		  }
		  return column;	
	  }
	  
	  
	  /**
	   * Transform into the server-side Row.
	   */
	  public static Row transform(RowClient client) {
		  Row serverRow = new Row();
		  serverRow.setCreateUser(client.getCreateUser());
		  serverRow.setDeleted(client.isDeleted());
		  serverRow.setFilterScope(transform(client.getFilterScope()));
		  serverRow.setLastUpdateUser(client.getLastUpdateUser());
		  serverRow.setRowEtag(client.getRowEtag());
		  serverRow.setRowId(client.getRowId());
		  serverRow.setValues(client.getValues());
		  return serverRow;
	  }	  
	  
	  
	  /**
	   * Transforms into the server-side Scope.
	   */
	  public static Scope transform(ScopeClient client) {
		  Scope serverScope = null;
		  switch(client.getType()) {
			  case DEFAULT:
				  serverScope = new Scope(Scope.Type.DEFAULT, client.getValue());
				  break;
			  case USER:
				  serverScope = new Scope(Scope.Type.USER, client.getValue());
				  break;
			  case GROUP:
				  serverScope = new Scope(Scope.Type.GROUP, client.getValue());
				  break;
		  }
		  if (serverScope == null) serverScope = Scope.EMPTY_SCOPE;
		  
		  return serverScope;	  
	  }	 
	  
	  /**
	   * Transforms the object into a TableAcl object.
	   */
	  public TableAcl transform(TableAclClient client) {
		  TableAcl ta = new TableAcl();
		  switch (client.getRole()) {
		  case NONE:
			  ta.setRole(TableRole.NONE);
			  break;
		  case FILTERED_WRITER:
			  ta.setRole(TableRole.FILTERED_WRITER);
			  break;
		  case UNFILTERED_READER_FILTERED_WRITER:
			  ta.setRole(TableRole.UNFILTERED_READER_FILTERED_WRITER);
			  break;
		  case READER:
			  ta.setRole(TableRole.READER);
			  break;
		  case WRITER:
			  ta.setRole(TableRole.WRITER);
			  break;
		  case OWNER:
			  ta.setRole(TableRole.OWNER);
			  break;
		  default:
			  throw new IllegalStateException("No assignable permissions in transforming table role."); 		
		  }
		  ta.setScope(transform(client.getScope()));
		  return ta;	  
	  }	  
}
