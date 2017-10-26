package org.opendatakit.aggregate.odktables.rest.entity;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

/**
 * Serialization:
 * Holds the key-value entry for one user-defined field of the row
 *
 * @author mitchellsundt@gmail.com
 *
 */
public class DataKeyValue {

  @JacksonXmlProperty(isAttribute=true)
  public String column;

  @JacksonXmlText
  public String value;

  public DataKeyValue() {
  }
  
  public DataKeyValue(String column, String value) {
    this.column = column;
    this.value = value;
  }

  @Override
  public boolean equals(Object obj) {
    if ( obj != null && obj instanceof DataKeyValue ) {
      DataKeyValue kv = (DataKeyValue) obj;
      return column.equals(kv.column) &&
          ((value == null) ? (kv.value == null) : value.equals(kv.value));
    }
    return false;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((column == null) ? 0 : column.hashCode());
    result = prime * result + ((value == null) ? 0 : value.hashCode());
    return result;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("[");
    builder.append(column);
    builder.append(" : ");
    builder.append(value);
    builder.append("]");
    return builder.toString();
  }

}