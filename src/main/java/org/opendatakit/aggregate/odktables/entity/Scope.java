package org.opendatakit.aggregate.odktables.entity;

import org.simpleframework.xml.Element;

public class Scope {

  public enum Type {
    USER,
    GROUP,
  }

  @Element
  private Type type;

  @Element
  private String value;

  /**
   * @param type
   * @param value
   */
  public Scope(Type type, String value) {
    this.type = type;
    this.value = value;
  }

  /**
   * @return the type
   */
  public Type getType() {
    return type;
  }

  /**
   * @param type
   *          the type to set
   */
  public void setType(Type type) {
    this.type = type;
  }

  /**
   * @return the value
   */
  public String getValue() {
    return value;
  }

  /**
   * @param value
   *          the value to set
   */
  public void setValue(String value) {
    this.value = value;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((type == null) ? 0 : type.hashCode());
    result = prime * result + ((value == null) ? 0 : value.hashCode());
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (!(obj instanceof Scope))
      return false;
    Scope other = (Scope) obj;
    if (type != other.type)
      return false;
    if (value == null) {
      if (other.value != null)
        return false;
    } else if (!value.equals(other.value))
      return false;
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("Scope [type=");
    builder.append(type);
    builder.append(", value=");
    builder.append(value);
    builder.append("]");
    return builder.toString();
  }

}