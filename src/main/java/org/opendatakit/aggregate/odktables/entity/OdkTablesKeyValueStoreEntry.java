package org.opendatakit.aggregate.odktables.entity;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * This is a simple struct-like object that will hold the rows
 * from the key value store. It is meant to be parsed into 
 * JSON objects to passed to the phone. So this can be thought
 * of as information ODKTables on the phone needs to know about
 * an entry in the key value store.
 * <p>
 * For a more in-depth explanation of all these fields, see the 
 * KeyValueStoreManager.java class in ODK Tables. 
 * @author sudar.sam@gmail.com
 *
 */
@Root(strict = false)
public class OdkTablesKeyValueStoreEntry {

	/**
	 * The table id of the table to which this entry belongs.
	 */
  @Element(required = true)
	public String tableId;
	
	/**
	 * The partition in the key value store to which the entry belongs. For an
	 * in depth example see KeyValueStoreManager.java in the ODK Tables project.
	 * Otherwise, just know that it is essentially the identifier of the class
	 * that is responsible for managing the entry. ListView.java would have
	 * (by convention) a partition name ListView. TableProperties and 
	 * ColumnProperties are the exception, belonging simply to the partitions
	 * "Table" and "Column".
	 */
  @Element(required = false)
	public String partition;
	
	/**
	 * The aspect is essentially the scope, or the instance of the partition,
	 * to which this key/entry belongs. For instance, a table-wide property
	 * would have the aspect "default". A column's aspect would be its element
	 * key (ie its unique column identifier for the table). A particular saved
	 * graph view might have the display name of that graph.
	 */
  @Element(required = false)
	public String aspect;
	
	/**
	 * The key of this entry. This is important so that ODKTables
	 * knows what to do with this entry. Eg a key of "list" might
	 * mean that this entry is important to the list view of 
	 * the table.
	 */
  @Element(required = false)
	public String key;
	
	/**
	 * The type of this entry. This is important to taht ODKTables
	 * knows how to interpret the value of this entry. Eg type 
	 * String means that the value holds a string. Type file
	 * means that the value is a JSON object holding a 
	 * FileManifestEntry object with information relating to
	 * the version of the file and how to get it.
	 */
  @Element(required = false)
	public String type;
	
	/**
	 * The actual value of this entry. If the type is String, this
	 * is a string. If it is a File, it is a FileManifestEntry
	 * JSON object.
	 */
  @Element(required = false)
	public String value;
  
  @Override 
  public String toString() {
    return "[tableId=" + tableId
        + ", partition=" + partition
        + ", aspect=" + aspect
        + ", key=" + key
        + ", type=" + type
        + ", value=" + value
        + "]";
  }
  
  @Override
  public int hashCode() {
    int result = 17;
    result = 37 * result + tableId.hashCode();
    result = 37 * result + partition.hashCode();
    result = 37 * result + aspect.hashCode();
    result = 37 * result + key.hashCode();
    result = 37 * result + type.hashCode();
    result = 37 * result + value.hashCode();
    return result;
  }
  
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof OdkTablesKeyValueStoreEntry)) {
      return false;
    }
    OdkTablesKeyValueStoreEntry other = (OdkTablesKeyValueStoreEntry) o;
    return 
        tableId == null ? other.tableId == null : tableId.equals(other.tableId) &&
        partition == null ? other.partition == null : partition.equals(other.partition) &&
        aspect == null ? other.aspect == null : aspect.equals(other.aspect) &&
        key == null ? other.key == null : key.equals(other.key) &&
        type == null ? other.type == null : type.equals(other.type) &&
        value == null ? other.value == null : value.equals(other.value);
  }
	
}
