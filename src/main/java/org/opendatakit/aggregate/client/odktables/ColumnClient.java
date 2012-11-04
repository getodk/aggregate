package org.opendatakit.aggregate.client.odktables;

import org.opendatakit.aggregate.odktables.entity.Column;
import org.opendatakit.aggregate.odktables.entity.Column.ColumnType;

/**
 * This is the client-side code of
 * org.opendatakit.aggregate.odktables.entity.Column.java.
 * <br>
 * The idea is that it will perform the same function but for the
 * client. Usual caveat that it's not yet clear if this is necessary
 * or if a new non-phone analog has to exist on the server.
 * @author sudar.sam@gmail.com
 *
 */
public class ColumnClient {

  private String name;

  private ColumnType type;
 
  /*
   * SS: I am taking this to be the dbName--ie how it exists in the colProp
   * on the phone, as the fixed one that does not change--they can alter the
   * display name.
   */
  private String dbName;

  public enum ColumnType {
    STRING,
    INTEGER,
    DECIMAL,
    BOOLEAN,
    DATETIME;

  }

  @SuppressWarnings("unused")
  private ColumnClient() {}

  /**
   * Create a column. Spaces will be replaced by underscores. The backing 
   * dbName of the column will be the displayName changed to lower case and
   * prepended with an underscore.
   * @param displayName
   * @param type
   */
  public ColumnClient(final String displayName, final ColumnType type) {
    String nameToBeEntered = displayName.toLowerCase().replace(" ", "_");
    this.name = nameToBeEntered;
    this.type = type;
    this.dbName = "_" + nameToBeEntered;
  }

  public String getName() {
    return this.name;
  }

  public ColumnType getType() {
    return this.type;
  }

  public void setName(final String name) {
    this.name = name;
  }
  
  /**
   * Get the dbName for the column. This cannot be changed, so no setter is
   * provided.
   * @return
   */
  public String getDbName() {
    return String.valueOf(this.dbName);
  }

  public void setType(final ColumnType type) {
    this.type = type;
  }
  
  /**
   * Transform the object into a server-side Column object.
   *
  public Column transform() {
	  // have to fix this to get the appropriate type out of the enum.
	  Column column;
	  switch (this.getType()) {
	  case BOOLEAN:
		  column = new Column(this.getName(), Column.ColumnType.BOOLEAN);
		  break;
	  case STRING:
		  column = new Column(this.getName(), Column.ColumnType.STRING);
		  break;
	  case INTEGER:
		  column = new Column(this.getName(), Column.ColumnType.INTEGER);
		  break;
	  case DECIMAL:
		  column = new Column(this.getName(), Column.ColumnType.DECIMAL);
		  break;
	  case DATETIME:
		  column = new Column(this.getName(), Column.ColumnType.DATETIME);
		  break;
	  default:
		  throw new IllegalStateException("cannot transform ColumnClient to Column, no type match.");
	  }
	  return column;
			  
  }*/
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((type == null) ? 0 : type.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    ColumnClient other = (ColumnClient) obj;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (type != other.type)
      return false;
    return true;
  }

  public String toString() {
    return "Column(name=" + this.getName() + ", type=" + this.getType() + ")";
  }
}
