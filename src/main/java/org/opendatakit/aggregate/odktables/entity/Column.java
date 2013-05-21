package org.opendatakit.aggregate.odktables.entity;

import java.io.Serializable;

import org.opendatakit.aggregate.odktables.relation.DbColumnDefinitions;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

/**
 * The XML document that represents a column. This is the XML representation
 * of a column definition as stored in the {@link DbColumnDefinitions} table.
 * @author dylan price?
 * @author sudar.sam@gmail.com
 *
 */
@Root
public class Column implements Serializable {

  /**
	 *
	 */
	private static final long serialVersionUID = -6624997293167731653L;

	public enum ColumnType {
	  STRING,
	  INTEGER,
	  DECIMAL,
	  BOOLEAN,
	  DATETIME;
	}

  @Attribute(required = true)
  private String tableId;

  @Attribute(required = true)
  private String elementKey;

  @Attribute(required = true)
  private String elementName;

  @Attribute(required = false)
  private ColumnType elementType;

  @Attribute(required = false)
  private String listChildElementKeys;

  @Attribute(required = true)
  private int isPersisted;

  @Attribute(required = false)
  private String joins;

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
   * @param tableId
   * @param elementKey
   * @param elementName
   * @param elementType
   * @param listChildElementKeys
   * @param isPersisted
   * @param joins
   */
  public Column(final String tableId, final String elementKey,
      final String elementName, final ColumnType elementType,
      final String listChildElementKeys, final int isPersisted,
      final String joins) {
    this.tableId = tableId;
    this.elementKey = elementKey;
    this.elementName = elementName;
    this.elementType = elementType;
    this.listChildElementKeys = listChildElementKeys;
    this.isPersisted = isPersisted;
    this.joins = joins;
  }

  public String getTableId() {
    return this.tableId;
  }

  public String getElementKey() {
    return this.elementKey;
  }

  public String getElementName() {
    return this.elementName;
  }

  public ColumnType getElementType() {
    return this.elementType;
  }

  public String getListChildElementKeys() {
    return this.listChildElementKeys;
  }

  public int getIsPersisted() {
    return this.isPersisted;
  }

  public String getJoins() {
    return this.joins;
  }

  @Override
  public String toString() {
    return "Column(tableId=" + getTableId()
        + ", elementKey=" + this.getElementKey()
        + ", elementName=" + this.getElementName()
        + ", elementType= " + this.getElementType().name()
        + ", listChildElementKeys=" + this.getListChildElementKeys()
        + ", isPersisted=" + this.getIsPersisted()
        + ", joins=" + this.getJoins()
        + ")";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((tableId == null) ? 0 : tableId.hashCode());
    result = prime * result + ((elementKey == null) ? 0 : elementKey.hashCode());
    result = prime * result + ((elementName == null) ? 0 : elementName.hashCode());
    result = prime * result + ((elementType == null) ? 0 : elementType.hashCode());
    result = prime * result + ((listChildElementKeys == null) ? 0 : listChildElementKeys.hashCode());
    result = prime * result + isPersisted;
    result = prime * result + ((joins == null) ? 0 : joins.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if ( obj == null ) {
      return false;
    }
    if ( obj == this ) {
      return true;
    }
    if (!(obj instanceof Column)) {
      return false;
    }
    Column other = (Column) obj;
    return (tableId == null ? other.tableId == null : tableId.equals(other.tableId))
        && (elementKey == null ? other.elementKey == null : elementKey.equals(other.elementKey))
        && (elementName == null ? other.elementName == null : elementName.equals(other.elementName))
        && (elementType == null ? other.elementType == null : elementType.equals(other.elementType))
        && (listChildElementKeys == null ? other.listChildElementKeys == null : listChildElementKeys.equals(other.listChildElementKeys))
        && (isPersisted == other.isPersisted)
        && (joins == null ? other.joins == null : joins.equals(other.joins));
  }
}