package org.opendatakit.aggregate.client.odktables;

import org.opendatakit.aggregate.odktables.entity.Column;

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

  public enum ColumnType {
    STRING,
    INTEGER,
    DECIMAL,
    BOOLEAN,
    DATETIME;

  }

  @SuppressWarnings("unused")
  private ColumnClient() {}

  public ColumnClient(final String name, final ColumnType type) {
    this.name = name;
    this.type = type;
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
