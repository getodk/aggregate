package org.opendatakit.aggregate.odktables.entity;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root
public class Column {

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

  public Column(final String name, final ColumnType type) {
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