package org.opendatakit.aggregate.odktables.entity;

/**
 * This is a simple struct-like object that will hold the rows
 * from the key value store. It is meant to be parsed into 
 * JSON objects to passed to the phone. So this can be thought
 * of as information ODKTables on the phone needs to know about
 * an entry in the key value store.
 * @author sudar.sam@gmail.com
 *
 */
public class OdkTablesKeyValueStoreEntry {

	/**
	 * The table id of the table to which this entry belongs.
	 */
	public String tableId;
	
	/**
	 * The table name of the table to which this entry belongs.
	 */
	public String tableName;
	
	/**
	 * The key of this entry. This is important so that ODKTables
	 * knows what to do with this entry. Eg a key of "list" might
	 * mean that this entry is important to the list view of 
	 * the table.
	 */
	public String key;
	
	/**
	 * The type of this entry. This is important to taht ODKTables
	 * knows how to interpret the value of this entry. Eg type 
	 * String means that the value holds a string. Type file
	 * means that the value is a JSON object holding a 
	 * FileManifestEntry object with information relating to
	 * the version of the file and how to get it.
	 */
	public String type;
	
	/**
	 * The actual value of this entry. If the type is String, this
	 * is a string. If it is a File, it is a FileManifestEntry
	 * JSON object.
	 */
	public String value;
	
}
