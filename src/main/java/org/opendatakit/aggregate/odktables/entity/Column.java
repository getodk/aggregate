package org.opendatakit.aggregate.odktables.entity;

import java.io.Serializable;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root
public class Column implements Serializable {

  /**
	 * 
	 */
	private static final long serialVersionUID = -6624997293167731653L;

	/*
	 * SS: I am taking this to mean the display name.
	 */
  @Attribute(required = true)
  private String name;

  @Attribute(required = true)
  private ColumnType type;

  public enum ColumnType {
    STRING,
    INTEGER,
    DECIMAL,
    BOOLEAN,
    DATETIME;
    
  }

  @SuppressWarnings("unused")
  private Column() {}

  /**
   * Create a column. NB: It needs to be decided if 
   * backing name and display name are different in the datastore on the server
   * in the same way they are on the phone, and if they should both be stored
   * in the COLUMN table as adjacent columns, or what exactly. Either way,
   * its implementation should be brought into alignment with ColumnClient,
   * which has both display and backing names when the answer to the above
   * questions is decided. 
   * @param displayName
   * @param type
   */
  public Column(final String displayName, final ColumnType type) {
    this.name = displayName;
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
    Column other = (Column) obj;
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