package org.opendatakit.aggregate.odktables.entity;

import java.util.ArrayList;
import java.util.List;

import org.jboss.resteasy.logging.Logger;
import org.opendatakit.aggregate.client.odktables.ColumnClient;
import org.opendatakit.aggregate.client.odktables.OdkTablesKeyValueStoreEntryClient;
import org.opendatakit.aggregate.client.odktables.RowClient;
import org.opendatakit.aggregate.client.odktables.ScopeClient;
import org.opendatakit.aggregate.client.odktables.TableAclClient;
import org.opendatakit.aggregate.client.odktables.TableTypeClient;
import org.opendatakit.aggregate.odktables.entity.api.TableType;

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
	    Column.ColumnType serverColumnType = Column.ColumnType.STRING;
	    switch (client.getElementType()) {
	    case BOOLEAN:
	      serverColumnType = Column.ColumnType.BOOLEAN;
	      break;
	    case DATETIME:
	      serverColumnType = Column.ColumnType.DATETIME;
	      break;
	    case DECIMAL:
	      serverColumnType = Column.ColumnType.DECIMAL;
	      break;
	    case INTEGER:
	      serverColumnType = Column.ColumnType.INTEGER;
	      break;
	    case STRING:
	      serverColumnType = Column.ColumnType.STRING;
	      break;
	    default:
	      Logger.getLogger(UtilTransforms.class).error(
	          "unrecognized client column type: " + client.getElementType());
	    }
		  Column transformedColumn = new Column(client.getTableId(), 
		      client.getElementKey(), client.getElementName(), 
		      serverColumnType, client.getListChildElementKeys(),
		      client.getIsPersisted(), client.getJoins());
		  return transformedColumn;	
	  }
	  
	  public static TableType transform(TableTypeClient clientType) {
	    TableType serverType = TableType.DATA;
	    switch (clientType) {
	    case DATA:
	      serverType = TableType.DATA;
	      break;
	    case SECURITY:
	      serverType = TableType.SECURITY;
	      break;
	    case SHORTCUT:
	      serverType = TableType.SHORTCUT;
	      break;
	    default:
         Logger.getLogger(UtilTransforms.class).error(
             "unrecognized client table type type: " + clientType);
       }
	    return serverType;
	  }
	  
	  /**
	   * Transform server-side {@link OdkTablesKeyValueStoreEntry} into 
	   * {@link OdkTablesKeyValueStoreEntryClient}.
	   * @param server
	   * @return
	   */
	  public static OdkTablesKeyValueStoreEntryClient 
	    transform(OdkTablesKeyValueStoreEntry server) {
	    OdkTablesKeyValueStoreEntryClient client = 
	        new OdkTablesKeyValueStoreEntryClient();
	    client.tableId = server.tableId;
	    client.partition = server.partition;
	    client.aspect = server.aspect;
	    client.key = server.key;
	    client.type = server.type;
	    client.value = server.value;
	    return client;
	  }
	  
	  /**
	   * Convenience method. Identical to calling transform on individual
	   * entries and constructing up a list.
	   * @param serverEntries
	   * @return
	   */
	  public static List<OdkTablesKeyValueStoreEntryClient> 
	      transform(List<OdkTablesKeyValueStoreEntry> serverEntries) {
	    List<OdkTablesKeyValueStoreEntryClient> clientEntries = 
	        new ArrayList<OdkTablesKeyValueStoreEntryClient>();
	    for (OdkTablesKeyValueStoreEntry serverEntry : serverEntries) {
	      clientEntries.add(transform(serverEntry));
	    }
	    return clientEntries;
	  }
	  
     /**
      * Transform client-side {@link OdkTablesKeyValueStoreEntryClient} into 
      * {@link OdkTablesKeyValueStoreEntry}.
      * @param clientEntry
      * @return
      */
     public static OdkTablesKeyValueStoreEntry 
       transform(OdkTablesKeyValueStoreEntryClient clientEntry) {
       OdkTablesKeyValueStoreEntry serverEntry = 
           new OdkTablesKeyValueStoreEntry();
       serverEntry.tableId = clientEntry.tableId;
       serverEntry.partition = clientEntry.partition;
       serverEntry.aspect = clientEntry.aspect;
       serverEntry.key = clientEntry.key;
       serverEntry.type = clientEntry.type;
       serverEntry.value = clientEntry.value;
       return serverEntry;
     }
     
     /**
      * Convenience method. Identical to calling transform on individual
      * entries and constructing up a list.
      * @param serverEntries
      * @return
      */
     public static List<OdkTablesKeyValueStoreEntry> 
         transformToServerEntries(
             List<OdkTablesKeyValueStoreEntryClient> clientEntries) {
       List<OdkTablesKeyValueStoreEntry> serverEntries = 
           new ArrayList<OdkTablesKeyValueStoreEntry>();
       for (OdkTablesKeyValueStoreEntryClient clientEntry : clientEntries) {
         serverEntries.add(transform(clientEntry));
       }
       return serverEntries;
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
		  if (client.getType() == null) {
		    serverScope = Scope.EMPTY_SCOPE;
		    return serverScope;
		  }
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
			default:
			  serverScope = Scope.EMPTY_SCOPE;
			  
		  }		  
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
